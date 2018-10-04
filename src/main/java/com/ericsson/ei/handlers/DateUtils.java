package com.ericsson.ei.handlers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DateUtils {
	/**
	   * This method creates the date object with 
	   * the current date for appending in the document
	   * object before inserting it into mongoDB.
	   */
	public static Date getDate() throws ParseException {
		Logger log = (Logger) LoggerFactory.getLogger(ObjectHandler.class);
		Date date = new Date();
		try {			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String time = dateFormat.format(date);
			date = dateFormat.parse(time);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return date;
	}
}
