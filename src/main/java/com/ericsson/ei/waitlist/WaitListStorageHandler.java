package com.ericsson.ei.waitlist;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

@Component
public class WaitListStorageHandler {

    @Value("${waitlist.collection.name}") private String collectionName;
    @Value("${database.name}") private String databaseName;
    @Value("${waitlist.collection.ttlValue}") private int ttlValue;
    static Logger log = (Logger) LoggerFactory.getLogger(WaitListStorageHandler.class);
    
    @Autowired
    private MongoDBHandler mongoDbHandler;

    public void setMongoDbHandler(MongoDBHandler mongoDbHandler) {
        this.mongoDbHandler = mongoDbHandler;
    }

    @Autowired
    private JmesPathInterface jmesPathInterface;

    public void setJmesPathInterface(JmesPathInterface jmesPathInterface) {
        this.jmesPathInterface = jmesPathInterface;
    }

    public void addEventToWaitList(String event, RulesObject rulesObject) throws Exception {
        String input = addProprtiesToEvent(event, rulesObject);
        boolean result=mongoDbHandler.insertDocument(databaseName,collectionName, input);
        if (result == false) {
            throw new Exception("failed to insert the document into database");
        }
    }

    private String addProprtiesToEvent(String event, RulesObject rulesObject) {
        String time = null;
        Date date=null;
        String idRule = rulesObject.getIdRule();
        JsonNode id = jmesPathInterface.runRuleOnEvent(idRule, event);
        String condition = "{Event:" + JSON.parse(event).toString()+"}";
        ArrayList<String> documents = mongoDbHandler.find(databaseName, collectionName, condition);
        if (documents.size() == 0){
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            date = new Date();
            time = dateFormat.format(date); 
            try {
                date=dateFormat.parse(time);
            } catch (ParseException e) {
                log.info(e.getMessage(),e);
            }
            
        }
        BasicDBObject document = new BasicDBObject();
        document.put("_id", id.textValue());
        document.put("Time",  date);
        document.put("Event", JSON.parse(event));
        mongoDbHandler.createTTLIndex(databaseName, collectionName, "Time",ttlValue);
        return document.toString();
    }

    public ArrayList<String> getWaitList() {
        ArrayList<String> documents = mongoDbHandler.getAllDocuments(databaseName,collectionName);
        return documents;
    }
    
}