package com.ericsson.ei.waitlist;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ericsson.ei.handlers.MatchIdRulesHandler;
import com.ericsson.ei.jmespath.JmesPathInterface;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;
import com.ericsson.ei.rmqhandler.RmqHandler;
import com.ericsson.ei.rules.RulesHandler;
import com.ericsson.ei.rules.RulesObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@Component
public class WaitListWorker {

    @Autowired
    private WaitListStorageHandler waitListStorageHandler;

    @Autowired
    private RmqHandler rmqHandler;

    @Autowired
    private RulesHandler rulesHandler;

    @Autowired
    private JmesPathInterface jmesPathInterface;

    @Autowired
    private MatchIdRulesHandler matchIdRulesHandler;

//    @Value("${waitlist.initDelay}") private int a1;
    private String a1 = System.getProperty("rabbitmq.port");
    private final int A = Integer.parseInt(a1);

    static Logger log = (Logger) LoggerFactory.getLogger(WaitListWorker.class);

    @Scheduled(initialDelay = 100, fixedRate = 10000)
    public void run() {
        RulesObject rulesObject = null;
        ArrayList<String> documents = waitListStorageHandler.getWaitList();
        for (String document : documents) {
            DBObject dbObject = (DBObject) JSON.parse(document);
            String event = dbObject.get("Event").toString();
            rulesObject = rulesHandler.getRulesForEvent(event);
            String idRule = rulesObject.getIdentifyRules();
            JsonNode ids = jmesPathInterface.runRuleOnEvent(idRule, event);
            if (ids.isArray()) {
                for (final JsonNode idJsonObj : ids) {
                    ArrayList<String> objects = matchIdRulesHandler.fetchObjectsById(rulesObject, idJsonObj.textValue());
                    if (objects.size() > 0) {
                        rmqHandler.publishObjectToMessageBus(event);
                        waitListStorageHandler.dropDocumentFromWaitList(document);
                    }
                }
            }
        }
    }
}
