package com.ericsson.ei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class App {

    public static void main(String[] args) {

        List<String> logLevels = new ArrayList<>();
        Collections.addAll(logLevels, "ALL", "DEBUG", "ERROR", "FATAL", "INFO", "TRACE", "WARN");

        if(args != null && args.length > 0 && logLevels.contains(args[0])) {
            System.setProperty("logging.level.root", args[0]);
            System.setProperty("logging.level.org.springframework.web", args[0]);
            System.setProperty("logging.level.com.ericsson.ei", args[0]);
        } else {
            System.setProperty("logging.level.root", "OFF");
            System.setProperty("logging.level.org.springframework.web", "OFF");
            System.setProperty("logging.level.com.ericsson.ei", "OFF");
        }

        SpringApplication.run(App.class, args);
    }

}
