package com.GraphToSQL.Domain;

import java.util.List;
import java.util.Map;


public class GraphDetail {

    private Map<String, Map<Long, MyNode>> allMyNodes;
    private Map<MyRelationshipType,List<MyRelationship>> allMyRelationships;

    public GraphDetail(Map<String, Map<Long, MyNode>> allMyNodes, Map<MyRelationshipType, List<MyRelationship>> allMyRelationships) {
        this.allMyNodes = allMyNodes;
        this.allMyRelationships = allMyRelationships;
    }

    public Map<String, Map<Long, MyNode>> getAllMyNodes() {
        return allMyNodes;
    }

    public Map<MyRelationshipType, List<MyRelationship>> getAllMyRelationships() {
        return allMyRelationships;
    }

    public void assignSQLPrimaryKeysToNodes() {
        int count;
        for (Map.Entry<String, Map<Long, MyNode>> element : allMyNodes.entrySet()) {
            count = 1;
            for (MyNode node : element.getValue().values()) {
                node.setSqlID(count);
                count++;
            }
        }
    }

}
