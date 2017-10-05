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

    @Value("${waitlist.initialDelayResend:}")  private int initialDelayResend;
    @Value("${waitlist.fixedRateResend:}") private int fixedRateResend;

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

    static Logger log = (Logger) LoggerFactory.getLogger(WaitListWorker.class);

    @Scheduled(initialDelay = 10, fixedRate = 10)
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
