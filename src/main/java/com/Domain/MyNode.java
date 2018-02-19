package com.Domain;

import java.util.Map;

/**
 * Created by John on 2018-02-18.
 */
public class MyNode {

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    private String primaryKeyName;

    public MyNode(String primaryKeyName, Map<String, Object> values) {
        this.primaryKeyName = primaryKeyName;
        this.values = values;
    }

    private Map<String,Object> values;
}
