package com.Domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2018-02-18.
 */
public class GraphDetail {

    public GraphDetail(Map<String, List<MyNode>> allMyNodes, Map<MyRelationshipType, List<MyRelationship>> allMyRelationships) {
        this.allMyNodes = allMyNodes;
        this.allMyRelationships = allMyRelationships;
    }

    public Map<String, List<MyNode>> getAllMyNodes() {
        return allMyNodes;
    }

    public Map<MyRelationshipType, List<MyRelationship>> getAllMyRelationships() {
        return allMyRelationships;
    }

    private Map<String,List<MyNode>> allMyNodes;
    private Map<MyRelationshipType,List<MyRelationship>> allMyRelationships;

    public Map<MyRelationshipType,List<MyRelationship>> getRelationshipsForGivenLabel(String label) {

        Map<MyRelationshipType,List<MyRelationship>> relationshipsForGivenLabel = new HashMap<>();
        for(Map.Entry<MyRelationshipType, List<MyRelationship>> element : allMyRelationships.entrySet()) {
            if(element.getKey().isInARelationshipp(label)) {
                relationshipsForGivenLabel.put(element.getKey(),element.getValue());
            }
        }
        return relationshipsForGivenLabel;
    }

}
