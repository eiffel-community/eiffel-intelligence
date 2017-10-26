package com.ericsson.ei.jsonmerge;

import org.springframework.stereotype.Component;

import com.ericsson.ei.rules.RulesObject;

@Component
public class DownstreamMergeHandler extends MergeHandler {

    protected String getMergeRules(RulesObject rules) {
        return rules.getDownstreamMergeRules();
    }
}
