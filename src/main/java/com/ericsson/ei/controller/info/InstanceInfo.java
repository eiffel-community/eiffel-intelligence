package com.ericsson.ei.controller.info;

import com.ericsson.ei.controller.model.ParseInstanceInfoEI;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InstanceInfo {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(InstanceInfo.class);

    @Autowired
    private ParseInstanceInfoEI istanceInfo;

    @RequestMapping(value = "/information", produces = MediaType.APPLICATION_JSON_VALUE)
    public String parseInfo() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(istanceInfo);
        } catch (Exception e) {
            LOGGER.error("Serialization is failed " + e.getMessage());
        }
        return null;
    }
}
