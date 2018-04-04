package com.ericsson.ei.config.scope.register;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Register the SimpleThreadScope 
 *
 */
@Configuration
public class SpringBeanScopeRegisterConfig {
 
    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return new CustomScopeRegisteringBeanFactoryPostProcessor();
    }
 
}