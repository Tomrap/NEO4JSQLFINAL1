package com.dao;

import com.Domain.GraphDetail;
import com.Domain.MyNode;
import com.Domain.MyRelationship;
import com.Domain.MyRelationshipType;
import org.neo4j.graphdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by John on 2018-02-18.
 */

@Lazy
@Repository
public class NEO4JReaderDao {

    @Autowired
    private GraphDatabaseService graphDatabaseService;

    public GraphDetail read() {


        Map<String, Map<Long, MyNode>> allMyNodes = new HashMap<>();
        Map<MyRelationshipType,List<MyRelationship>> allMyRelationships = new HashMap<>();
        GraphDetail graphDetail = new GraphDetail(allMyNodes,allMyRelationships);
        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            ResourceIterable<Node> allNodes = graphDatabaseService.getAllNodes();
            ResourceIterable<Relationship> allRelationships = graphDatabaseService.getAllRelationships();

            for(Node node : allNodes) {

                //TODO make sure that labels are always in the same order - yes do this
                Iterable<Label> labels = node.getLabels();

                String oneLabel = StreamSupport.stream(labels.spliterator(), false).map(Object::toString).collect(Collectors.joining(""));

                MyNode myNode = new MyNode(oneLabel,node.getAllProperties());

                allMyNodes.computeIfAbsent(oneLabel, k -> new HashMap<>()).put(node.getId(),myNode);

            }

            for(Relationship relationship: allRelationships) {

                Node startNode = relationship.getStartNode();
                Node endNode = relationship.getEndNode();

                String firstNodeLabel = StreamSupport.stream(startNode.getLabels().spliterator(), false).map(Object::toString).collect(Collectors.joining(""));
                String secondNodeLabel = StreamSupport.stream(endNode.getLabels().spliterator(), false).map(Object::toString).collect(Collectors.joining(""));

                MyRelationship myRelationship = new MyRelationship(startNode.getId(),
                       endNode.getId(),relationship.getAllProperties());

                allMyRelationships.computeIfAbsent(new MyRelationshipType(firstNodeLabel,secondNodeLabel), k-> new ArrayList<>()).add(myRelationship);
            }

            tx.success();
            return graphDetail;
        }


    }
}
