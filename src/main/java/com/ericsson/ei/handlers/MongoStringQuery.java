package com.ericsson.ei.handlers;

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
