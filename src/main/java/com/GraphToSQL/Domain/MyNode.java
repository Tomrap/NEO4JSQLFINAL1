package com.GraphToSQL.Domain;

import java.util.Map;


public class MyNode {

    private Map<String,Object> values;
    private int sqlID;

    public int getSqlID() {
        return sqlID;
    }

    public void setSqlID(int sqlID) {
        this.sqlID = sqlID;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public MyNode(Map<String, Object> values) {
        this.values = values;
    }

}
