package com.ericsson.ei.logFilter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.springframework.context.annotation.PropertySource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@PropertySource("classpath:logback.xml")
public class LogFilter extends Filter<ILoggingEvent> {

public boolean filter(LocalDateTime start, LocalDateTime end, String logLine) {
        DateTimeFormatter logFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");
        LocalDateTime current=start;
        while(current.isBefore(end))
        {
            String time=current.format(logFormatter);
            if(logLine.contains(time))
            {
                return (true);
            }
            else
            {
                current=current.plus(1, ChronoUnit.MILLIS);
            }


        }
    return (false);
    }

    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        return null;
    }
}

