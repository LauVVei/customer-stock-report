package com.example.report.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {"/", "/customer-report/**"})
    public String index() {
        return "forward:/index.html";
    }
}
