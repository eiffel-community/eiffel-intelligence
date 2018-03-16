package com.ericsson.ei.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/download/subscriptiontemplate")
public interface SubscriptionTemplateController {

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    void getSubscriptionJsonTemplate(HttpServletResponse response);
}
