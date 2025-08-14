package app.bola.taskforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final long startTime = System.currentTimeMillis();

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();

        health.put("status", "UP");
        health.put("version", "1.0.0");
        
	long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        health.put("uptimeMillis", uptimeMillis);

        String environment = System.getenv().getOrDefault("SPRING_PROFILES_ACTIVE", "development");
        health.put("environment", environment);

        health.put("database", "connected");

        return health;
    }
}
