package com.Domain;

import java.util.Map;

/**
 * Created by John on 2018-02-18.
 */
public class MyNode {

    private String primaryKeyName;

    public MyNode(String primaryKeyName, Map<String, Object> values) {
        this.primaryKeyName = primaryKeyName;
        this.values = values;
    }

    private Map<String,Object> values;
}
