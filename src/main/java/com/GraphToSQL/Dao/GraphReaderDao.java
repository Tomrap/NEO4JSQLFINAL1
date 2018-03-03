package com.GraphToSQL.Dao;

import com.GraphToSQL.Domain.GraphDetail;
import com.GraphToSQL.Domain.MyNode;
import com.GraphToSQL.Domain.MyRelationship;
import com.GraphToSQL.Domain.MyRelationshipType;
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
public class GraphReaderDao {

    @Autowired
    private GraphDatabaseService graphDatabaseService;

    private Map<String, Map<Long, MyNode>> readNodes(ResourceIterable<Node> allNodes) {
        Map<String, Map<Long, MyNode>> allMyNodes = new HashMap<>();

        for(Node node : allNodes) {

            // important assumption - all nodes have proper labels as the nodes are grouped depending on their labels
            //TODO make sure that labels are always in the same order - yes do this
            Iterable<Label> labels = node.getLabels();

            String oneLabel = StreamSupport.stream(labels.spliterator(), false).map(Object::toString).collect(Collectors.joining(""));

            MyNode myNode = new MyNode(node.getAllProperties());

            allMyNodes.computeIfAbsent(oneLabel, k -> new HashMap<>()).put(node.getId(),myNode);

        }
        return allMyNodes;
    }

    private Map<MyRelationshipType,List<MyRelationship>> readRelationships(ResourceIterable<Relationship> allRelationships) {
        Map<MyRelationshipType,List<MyRelationship>> allMyRelationships = new HashMap<>();

        for(Relationship relationship: allRelationships) {

            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();

            String firstNodeLabel = StreamSupport.stream(startNode.getLabels().spliterator(), false).map(Object::toString).collect(Collectors.joining(""));
            String secondNodeLabel = StreamSupport.stream(endNode.getLabels().spliterator(), false).map(Object::toString).collect(Collectors.joining(""));

            String label = relationship.getType().name();

            MyRelationship myRelationship = new MyRelationship(startNode.getId(),
                    endNode.getId(),relationship.getAllProperties());

            allMyRelationships.computeIfAbsent(new MyRelationshipType(label,firstNodeLabel,secondNodeLabel), k-> new ArrayList<>()).add(myRelationship);
        }

        return allMyRelationships;
    }

    public GraphDetail readGraph() {

        try ( Transaction tx = graphDatabaseService.beginTx() )
        {
            Map<String, Map<Long, MyNode>> nodes = readNodes(graphDatabaseService.getAllNodes());
            Map<MyRelationshipType, List<MyRelationship>> relationships = readRelationships(graphDatabaseService.getAllRelationships());
            tx.success();
            return new GraphDetail(nodes,relationships);
        }
    }
}
