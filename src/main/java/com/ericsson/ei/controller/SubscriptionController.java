package com.ericsson.ei.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

     @RequestMapping("/listSubscriptions")
    public String[] listSubscriptions() {
        return new String[1];

    }
}
