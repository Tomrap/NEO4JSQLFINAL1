package com.GraphToSQL.Domain;

import java.util.Map;

/**
 * Created by John on 2018-02-18.
 */
public class MyRelationship {

    public MyRelationship(long firstNode, long secondNode, Map<String, Object> values) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
        this.values = values;
    }

    public long getFirstNode() {
        return firstNode;
    }

    public long getSecondNode() {
        return secondNode;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    private long firstNode;
    private long secondNode;
    private Map<String,Object> values;
}
