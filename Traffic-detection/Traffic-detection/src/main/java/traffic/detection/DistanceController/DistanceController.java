package traffic.detection.DistanceController;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import traffic.detection.DistanceService.DistanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.RestController;

@Controller
public class DistanceController {
	@Autowired
	private final DistanceService distanceService;

    public DistanceController(DistanceService distanceService) {
        this.distanceService = distanceService;
    }
    
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
    
    @GetMapping("/distance")
    public String getDistance(@RequestParam String origin, @RequestParam String destination) {
        return distanceService.getDistance(origin, destination);
    }
    
    @PostMapping("/trafficSeverity")
    public String getTrafficSeverity(@RequestParam String origin,
						            @RequestParam String destination,
						            @RequestParam String departure_time,
						           
						            Model model) {

    	String trafficDataJson = distanceService.getTrafficSeverity(origin, destination, departure_time);

        // Parse JSON response
        JSONObject trafficData = new JSONObject(trafficDataJson);

        // Add parsed data to model
        model.addAttribute("origin", trafficData.getString("origin"));
        model.addAttribute("destination", trafficData.getString("destination"));
        model.addAttribute("distance", trafficData.getDouble("distance"));
        model.addAttribute("duration_without_traffic", trafficData.getString("duration_without_traffic"));
        model.addAttribute("duration_with_traffic", trafficData.getString("duration_with_traffic"));
        model.addAttribute("traffic_severity", trafficData.getString("traffic_severity"));

        return "result";
    }
    
    @GetMapping("/map")
    public String getMapPage() {
        return "form"; // Serves form.html from resources/templates
    }
}
