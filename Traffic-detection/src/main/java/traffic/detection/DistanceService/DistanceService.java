package traffic.detection.DistanceService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class DistanceService {

    @Value("${google.maps.api.key}")
    private String apiKey;

    @Value("${google.maps.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Get distance between two locations
    public String getDistance(String origin, String destination) {
        String url = String.format("%s?origins=%s&destinations=%s&key=%s",
                apiUrl, origin, destination, apiKey);
        return restTemplate.getForObject(url, String.class);
    }
    
    // Get traffic severity with the provided origin, destination, departure time, and mode of transport
    public String getTrafficSeverity(String origin, String destination, String departure_time, String mode) {
        // Construct the URL with the provided parameters
        String url = String.format("%s?origins=%s&destinations=%s&departure_time=%s&mode=%s&key=%s",
                apiUrl, origin, destination, departure_time, mode, apiKey);
        
        // Call the Google Maps API
        String response = restTemplate.getForObject(url, String.class);
        
        return inferTrafficSeverity(response);
    }
    

    // Inference of traffic severity based on the API response
    private String inferTrafficSeverity(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.getString("status").equals("OK")) {
                // Extract the relevant data from the response
                JSONObject element = jsonResponse.getJSONArray("rows")
                        .getJSONObject(0)
                        .getJSONArray("elements")
                        .getJSONObject(0);

                // Duration in normal conditions (in seconds)
                double durationValue = element.getJSONObject("duration").getDouble("value");
                // Duration with traffic (in seconds)
                double durationInTrafficValue = element.getJSONObject("duration_in_traffic").getDouble("value");
                // Distance without traffic (in meters)
                double distanceValue = element.getJSONObject("distance").getDouble("value");

                // Ensure duration_in_traffic is always greater than or equal to duration
                if (durationInTrafficValue < durationValue) {
                    double temp = durationInTrafficValue;
                    durationInTrafficValue = durationValue;
                    durationValue = temp;
                }

                // Calculate the percentage difference
                double diff = Math.abs((durationInTrafficValue - durationValue) / durationValue * 100);

                // Determine the traffic severity
                String trafficSeverity;
                if (diff < 10) {
                    trafficSeverity = "Light Traffic";
                } else if (diff >= 10 && diff <= 30) {
                    trafficSeverity = "Moderate Traffic";
                } else {
                    trafficSeverity = "Heavy Traffic";
                }

                // Convert durations to hours and minutes
                int durationHours = (int) (durationValue / 3600);
                int durationMinutes = (int) ((durationValue % 3600) / 60);

                int durationInTrafficHours = (int) (durationInTrafficValue / 3600);
                int durationInTrafficMinutes = (int) ((durationInTrafficValue % 3600) / 60);

                // Convert distance to kilometers
                double distanceInKm = distanceValue / 1000.0;

                // Build response JSON with all the necessary details
                JSONObject responseObj = new JSONObject();
                responseObj.put("origin", jsonResponse.getJSONArray("origin_addresses").getString(0));
                responseObj.put("destination", jsonResponse.getJSONArray("destination_addresses").getString(0));
                responseObj.put("distance", distanceInKm); // Distance in kilometers
                responseObj.put("duration_without_traffic", durationHours + " hrs " + durationMinutes + " min");
                responseObj.put("duration_with_traffic", durationInTrafficHours + " hrs " + durationInTrafficMinutes + " min");
                responseObj.put("traffic_severity", trafficSeverity);

                return responseObj.toString();
            } else {
                return "Error: " + jsonResponse.getString("status");
            }
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
