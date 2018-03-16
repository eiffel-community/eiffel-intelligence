package com.ericsson.ei.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Component
@CrossOrigin
@Api(value = "Get template", description = "REST end-points for get template")
public class SubscriptionTemplateControllerImpl implements SubscriptionTemplateController {

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(SubscriptionTemplateControllerImpl.class);

    @Override
    @CrossOrigin
    @ApiOperation(value = "")
    public void getSubscriptionJsonTemplate(HttpServletResponse response) {
        try {
            InputStream is = getClass().getResourceAsStream("/subscriptionsTemplate.json");
            IOUtils.copy(is, response.getOutputStream());
            response.getOutputStream().flush();
        } catch (NullPointerException e) {
            LOGGER.error("ERROR: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Error :- " + e.getMessage());
        }

    }
}
