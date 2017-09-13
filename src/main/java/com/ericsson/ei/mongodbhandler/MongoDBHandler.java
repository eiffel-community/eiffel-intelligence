package com.ericsson.ei.mongodbhandler;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

@Component
public class MongoDBHandler {
    static Logger log = (Logger) LoggerFactory.getLogger(MongoDBHandler.class);

    MongoClient mongoClient;

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Value("${mongodb.host}") private String host;
    @Value("${mongodb.port}") private int port;

    //TODO establish connection automatically when Spring instantiate this
    // based on connection data in properties file
    @PostConstruct public void init() {
        createConnection(host, port);
    }

    //Establishing the connection to mongodb and creating a collection
    public  void createConnection(String host, int port){
        mongoClient = new MongoClient(host , port);
    }

    //Insert data into collection
    public  boolean insertDocument(String dataBaseName, String collectionName, String input){
        try {
            DB db = mongoClient.getDB(dataBaseName);
            DBCollection table = db.getCollection(collectionName);
            DBObject dbObjectInput = (DBObject) JSON.parse(input);
            WriteResult result = table.insert(dbObjectInput);
            if (result.wasAcknowledged()) {
                System.out.println("Inserted successfully");
                return result.wasAcknowledged();
            }
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        return false;
    }

    //Retrieve entire data from  collection
    public ArrayList<String> getAllDocuments(String dataBaseName, String collectionName){
        ArrayList<String> result = new ArrayList<>();
        try{
            DB db = mongoClient.getDB(dataBaseName);
            DBCollection table = db.getCollection(collectionName);
            DBCursor cursor = table.find();
            if (cursor.count()!=0){
                int i = 1;
                while (cursor.hasNext()) {
                    DBObject document = cursor.next();
                    String documentStr = document.toString();
                    log.info("Got Document: "+i);
                    log.info(documentStr);
                    result.add(documentStr);
                    i++;
                }
            }
            else{
                log.info("No documents found in database: " + dataBaseName + "and collection: " + collectionName);
            }
        }catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return result;
    }

    //Retrieve data from the collection based on condition
    public  ArrayList<String> find(String dataBaseName, String collectionName, String condition){
        ArrayList<String> result = new ArrayList<>();
        try{
            DB db = mongoClient.getDB(dataBaseName);
            DBCollection table = db.getCollection(collectionName);
            DBObject dbObjectCondition = (DBObject)JSON.parse(condition);
            DBCursor conditionalCursor = table.find(dbObjectCondition);
            if (conditionalCursor.count()!=0){
                while(conditionalCursor.hasNext()) {
                    DBObject object = conditionalCursor.next();
                    String documentStr = object.toString();
                    log.info(documentStr);
                    result.add(documentStr);
                }
            }
            else{
                log.info("No documents found with given condition: " + condition);
                log.info("in database: " + dataBaseName + " and collection: " + collectionName);
            }
        }catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return result;
    }

    //update the document in collection
    public  boolean updateDocument(String dataBaseName, String collectionName, String input, String updateInput ){
        try{
            DB db = mongoClient.getDB(dataBaseName);
            DBCollection table = db.getCollection(collectionName);
            DBObject dbObjectInput = (DBObject)JSON.parse(input);
            DBObject dbObjectUpdateInput = (DBObject)JSON.parse(updateInput);
            WriteResult result = table.update(dbObjectInput , dbObjectUpdateInput);
            return result.isUpdateOfExisting();
        }catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return false;
    }

    public  DBObject findAndModify(String dataBaseName, String collectionName, String input, String updateInput){
        try{
            DB db = mongoClient.getDB(dataBaseName);
            DBCollection table = db.getCollection(collectionName);
            DBObject dbObjectInput = (DBObject)JSON.parse(input);
            DBObject dbObjectUpdateInput = (DBObject)JSON.parse(updateInput);
            DBObject result = table.findAndModify(dbObjectInput , dbObjectUpdateInput);
            if (result != null){return result;}
        }catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }

    //drop the document in collection
    public  boolean dropDocument(String dataBaseName, String collectionName,String condition){
        try{
            DB db = mongoClient.getDB(dataBaseName);
            DBCollection table = db.getCollection(collectionName);
            DBObject dbObjectCondition = (DBObject)JSON.parse(condition);
            WriteResult result = table.remove(dbObjectCondition);
            if(result.getN()>0){
                return true;
            }
            else{
                log.info("No documents found to delete");
                return false;
            }
        }catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return false;
    }

    public void createTTLIndex(String dataBaseName, String collectionName,String fieldName,int ttlValue){
        DB db = mongoClient.getDB(dataBaseName);
        BasicDBObject ttlField=new BasicDBObject(fieldName,1);
        BasicDBObject ttlTime=new BasicDBObject("expireAfterSeconds",ttlValue);
        db.getCollection(collectionName).createIndex(ttlField,ttlTime);
    }

}
