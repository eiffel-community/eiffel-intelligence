/*
   Copyright 2019 Ericsson AB.
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
*/package com.ericsson.ei.mongo;

public class MongoStringQuery implements MongoQuery {

    private String query;

    /**
     * Creates a MongoQuery with the given string as query
     * @param query
     */
    public MongoStringQuery(String query) {
        this.query = query;
    }

    /**
     * Returns the query String
     * @return the Query String
     */
    @Override
    public String getQueryString() {
        return query;
    }

    /**
     * See {@link #getQueryString()}
     */
    public String toString() {
        return getQueryString();
    }
    
    /*
     * TODO: emalinn - this class should be changed so it works like below
     * 
     *  MongoQueryBuilder.buildAnd(first, second);
     *  MongoQueryBuilder.buildQuery(string)
     */
}
