package com.Domain;

import java.util.Map;

/**
 * Created by John on 2018-02-18.
 */
public class MyNode {

    private String primaryKeyName;
    private Map<String,Object> values;
    private int sqlID;

    public int getSqlID() {
        return sqlID;
    }

    public void setSqlID(int sqlID) {
        this.sqlID = sqlID;
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public MyNode(String primaryKeyName, Map<String, Object> values) {
        this.primaryKeyName = primaryKeyName;
        this.values = values;
    }

}
