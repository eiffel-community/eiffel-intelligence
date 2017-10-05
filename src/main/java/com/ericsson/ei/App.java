package com.ericsson.ei;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class App {

    public static void main(String[] args) {

        List<String> logLevels = new ArrayList<>();

        if(true) {

        } else {
            System.setProperty("logging.level.root", "OFF");
            System.setProperty("logging.level.org.springframework.web", "OFF");
            System.setProperty("logging.level.com.ericsson.ei", "OFF");
        }

        SpringApplication.run(App.class, args);
    }

}
