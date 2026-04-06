/*
   Copyright 2017 Ericsson AB.
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

import com.ericsson.ei.controller.model.ParseInstanceInfoEI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class SwaggerConfig {

    private static final String CONTACT_NAME = "Eiffel Intelligence Maintainers";
    private static final String CONTACT_URL = "https://github.com/eiffel-community/eiffel-intelligence";
    private static final String CONTACT_EMAIL = "eiffel-community@googlegroups.com";

    @Autowired
    ParseInstanceInfoEI parseInstanceInfoEI;

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("eiffel-intelligence")
                .packagesToScan("com.ericsson.ei.controller")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eiffel Intelligence REST API")
                        .description("A real time data aggregation and analysis solution for Eiffel events.")
                        .version(parseInstanceInfoEI.getVersion())
                        .contact(new Contact()
                                .name(CONTACT_NAME)
                                .url(CONTACT_URL)
                                .email(CONTACT_EMAIL))
                        .license(new License()
                                .name("Apache License Version 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
