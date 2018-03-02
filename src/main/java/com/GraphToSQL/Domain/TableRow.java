package com.GraphToSQL.Domain;

import com.GraphToSQL.Domain.MyNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 2018-02-25.
 */
public class TableRow {

    private MyNode myNode;
    private Map<String, Integer> foreignKeys = new HashMap<>();
    private Map<String, Object> relationshipProperties = new HashMap<>();

    public Map<String, Object> getRelationshipProperties() {
        return relationshipProperties;
    }

    public void setRelationshipProperties(Map<String, Object> relationshipProperties) {
        this.relationshipProperties = relationshipProperties;
    }

    public MyNode getMyNode() {
        return myNode;
    }

    public void setMyNode(MyNode myNode) {
        this.myNode = myNode;
    }

    public Map<String, Integer> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(Map<String, Integer> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
