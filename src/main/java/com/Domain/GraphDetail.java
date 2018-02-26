package com.Domain;

import java.util.*;

/**
 * Created by John on 2018-02-18.
 */
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

    public Map<MyRelationshipType,List<MyRelationship>> getRelationshipsForGivenLabel(String label) {

        Map<MyRelationshipType,List<MyRelationship>> relationshipsForGivenLabel = new HashMap<>();
        for(Map.Entry<MyRelationshipType, List<MyRelationship>> element : allMyRelationships.entrySet()) {
            if(element.getKey().isInARelationshipp(label)) {
                relationshipsForGivenLabel.put(element.getKey(),element.getValue());
            }
        }
        return relationshipsForGivenLabel;
    }

    public Map<String, Map<Long, MyNode>> nodesWithoutAnyForeignKeys(List<TableDetail> list) {

        Map<String, Map<Long, MyNode>> nodes = new HashMap<>();
        for(Map.Entry<String, Map<Long, MyNode>> element: allMyNodes.entrySet()) {
            Optional<TableDetail> first = list.stream().filter(o -> o.getTableName().equals(element.getKey())).findFirst();
            TableDetail tableDetail = first.get();
            if(tableDetail.getGraphFks().size() == 0) {
                nodes.put(element.getKey(),element.getValue());
            }
        }
        return nodes;
    }

    public void assignPrimaryKeysToNodes() {
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
