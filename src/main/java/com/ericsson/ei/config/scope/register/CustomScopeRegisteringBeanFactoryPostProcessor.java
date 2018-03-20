package com.ericsson.ei.config.scope.register;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.SimpleThreadScope;

/**
 * This class for the extend the request bean scope to thread level
 * This scope is implemented to EventHandler and RuleHandler
 * These two classes are not share to each request and rabbitmq consumer.
 * The present request bean scope will work only request level, threads are not access the request bean scope beans
 * Using the SimpleThreadScope custom scope extend the request scope to threads.
 *
 */
public class CustomScopeRegisteringBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope("thread", new SimpleThreadScope());
    }
}
