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
