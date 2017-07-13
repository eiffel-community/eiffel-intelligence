/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     * 
 * No part of this software may be reproduced in any form without the  *   
 * written permission of the copyright owner.                          *             
 *                                                                     *
 ***********************************************************************/

package com.ericsson.ei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ericsson.ei.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(metaData());
    }
    @SuppressWarnings("deprecation")
    private ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                "Subscription REST API",
                "Subscription REST API to store and retrive the subscription.",
                "1.0",
                "Terms of service","",
               "Apache License Version 2.0",
               "https://www.apache.org/licenses/LICENSE-2.0");
        return apiInfo;
    }
}