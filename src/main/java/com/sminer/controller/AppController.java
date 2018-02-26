package com.sminer.controller;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sminer")
@EnableAutoConfiguration
public class AppController {

    @RequestMapping("/")
    String index() {
        return "index";
    }
}
