package com.ericsson.ei.config.scope.register;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.SimpleThreadScope;

/**
 * This class extends the "request" bean scope to thread level
 * This "thread" scope is implemented for EventHandler and RuleHandler
 * The spring provided "request" bean scope will work only request level, threads are not access the request bean scope without request
 * Using the SimpleThreadScope custom scope(scope name "thread") extend the request scope to threads.
 *
 */
public class ThreadScopeConfig implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope("thread", new SimpleThreadScope());
    }
}
