package vfrolenko.pr1.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping
    public String getName() {
        return "Valeriia Frolenko";
    }
}