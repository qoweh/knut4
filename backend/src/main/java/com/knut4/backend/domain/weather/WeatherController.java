package com.knut4.backend.domain.weather;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.Map;

@RestController
public class WeatherController {

    @GetMapping("/api/public/weather")
    public Map<String,String> weather(@RequestParam(required = false) Double lat,
                                      @RequestParam(required = false) Double lon) {
        // Very naive placeholder logic: infer a pseudo-weather string by time bucket.
        int hour = LocalTime.now().getHour();
        String w;
        if (hour >= 6 && hour < 11) w = "맑음"; // morning clear
        else if (hour < 16) w = "따뜻"; // warm
        else if (hour < 20) w = "선선"; // cool evening
        else w = "밤"; // night
        return Map.of("weather", w);
    }
}