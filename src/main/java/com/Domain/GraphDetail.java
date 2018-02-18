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

    private Map<String,List<MyNode>> allMyNodes;
    private Map<MyRelationshipType,List<MyRelationship>> allMyRelationships = new HashMap<>();

}
