package vfrolenko.pr1.controller;

import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemController {

    @GetMapping
    public Map<String, Object> getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("os", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        info.put("cpu_cores", runtime.availableProcessors());
        info.put("ram_used_mb", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        info.put("ram_max_mb", runtime.maxMemory() / (1024 * 1024));

        return info;
    }
}