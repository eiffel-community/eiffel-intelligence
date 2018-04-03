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
package com.ericsson.ei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class App extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(App.class);
    }

    public static void main(String[] args) {

        List<String> logLevels = new ArrayList<>();
        Collections.addAll(logLevels, "ALL", "DEBUG", "ERROR", "FATAL", "INFO", "TRACE", "WARN");

        if (args != null && args.length > 0 && logLevels.contains(args[0])) {
            System.setProperty("logging.level.root", args[0]);
            System.setProperty("logging.level.org.springframework.web", args[0]);
            System.setProperty("logging.level.com.ericsson.ei", args[0]);
        } else {
            System.setProperty("logging.level.root", "OFF");
            System.setProperty("logging.level.org.springframework.web", "OFF");
            System.setProperty("logging.level.com.ericsson.ei", "OFF");
        }

        ConfigurableBeanFactory beanFactory = new DefaultListableBeanFactory();
        Scope threadScope = new SimpleThreadScope();
        beanFactory.registerScope("thread", threadScope);

        SpringApplication.run(App.class, args);
    }

}
