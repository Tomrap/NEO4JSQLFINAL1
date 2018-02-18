package com.Domain;

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

    private long firstNode;
    private long secondNode;
    private Map<String,Object> values;
}
