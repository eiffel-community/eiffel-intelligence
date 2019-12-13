/*
   Copyright 2019 Ericsson AB.
   For a full list of individual contributors, please see the commit history.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.ericsson.ei.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import com.ericsson.ei.mongo.MongoUri;

public class ConfigurationLogger implements ApplicationListener<ApplicationPreparedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLogger.class);

    private ConfigurableEnvironment environment;

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        environment = event.getApplicationContext().getEnvironment();
        logConfiguration();
    }

    private void logConfiguration() {
        List<MapPropertySource> propertySources = findApplicationProperties();
        for (MapPropertySource propertySource : propertySources) {
            String propertiesLogMessage = createPropertiesLogMessage(propertySource);
            LOGGER.debug(propertiesLogMessage);
        }
    }

    private List<MapPropertySource> findApplicationProperties() {
        List<MapPropertySource> propertySources = new ArrayList<>();
        for (PropertySource<?> propertySource : ((ConfigurableEnvironment) environment).getPropertySources()) {
            if (propertySource.getName().contains("applicationConfig")) {
                propertySources.add((MapPropertySource) propertySource);
            }
        }
        return propertySources;
    }

    private String createPropertiesLogMessage(MapPropertySource propertySource) {
        String[] propertyNames = propertySource.getPropertyNames();
        Arrays.sort(propertyNames);
        StringBuilder propertiesLogMessage = new StringBuilder();
        propertiesLogMessage.append(String.format("\n##### %s #####\n", propertySource.getName()));
        for (String propertyName : propertyNames) {
            String propertyValue = getPropertyValueAndCheckForSpecialCases(propertyName);
            propertiesLogMessage.append(String.format("%s: %s\n", propertyName, propertyValue));
        }
        return propertiesLogMessage.toString();
    }

    private String getPropertyValueAndCheckForSpecialCases(String propertyName) {
        switch (propertyName) {
        case "spring.data.mongodb.uri":
            final String unsafeUri = environment.getProperty("spring.data.mongodb.uri");
            final String safeUri = MongoUri.getUriWithHiddenPassword(unsafeUri);
            return safeUri;
        default:
            String propertyValue = environment.getProperty(propertyName);
            return propertyValue;
        }
    }
}
