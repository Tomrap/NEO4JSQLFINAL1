
package com.dao;

import com.Domain.TableDetail;
import org.neo4j.graphdb.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class Neo4JDao {

    private GraphDatabaseService graphDb;

    @Autowired
    public Neo4JDao(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }


    private static enum RelTypes implements RelationshipType {
        KNOWS
    }

    public void createNodes(List<Map<String, Object>> rs, final TableDetail tableDetail) throws SQLException {


        try (Transaction tx = graphDb.beginTx()) {

            List<String> fields = tableDetail.getFields();

            for (Map row : rs) {

                Node currentNode = graphDb.createNode();
                currentNode.addLabel(new Label() {
                    @Override
                    public String name() {
                        return tableDetail.getTableName();
                    }
                });

                for (String field : fields) {
                    Object object = row.get(field);

                    currentNode.setProperty(field, object);
                }

            }
            tx.success();
        }
    }

//    public void createRelationships(List<Map<String, Object>> rs, TableDetail tableDetail) {
//
//
//        try (Transaction tx = graphDb.beginTx()) {
//
//            graphDb.getNodeById()
//
//            tx.success();
//        }
//    }

//    public void createDb() throws IOException {
//
//
//        Node firstNode;
//        Node secondNode;
////         START SNIPPET: transaction
//        try (Transaction tx = graphDb.beginTx()) {
//            // Database operations go here
//            // END SNIPPET: transaction
//            // START SNIPPET: addData
//            firstNode = graphDb.createNode();
//            firstNode.setProperty("message", "Hello, ");
//            secondNode = graphDb.createNode();
//            secondNode.setProperty("message", "World!");
//
//            Relationship relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
//            relationship.setProperty("message", "brave Neo4j ");
//            // END SNIPPET: addData
//
//            // START SNIPPET: readData
//            System.out.print(firstNode.getProperty("message"));
//            System.out.print(relationship.getProperty("message"));
//            System.out.print(secondNode.getProperty("message"));
//            // END SNIPPET: readData
//
//            String greeting = ((String) firstNode.getProperty("message"))
//                    + ((String) relationship.getProperty("message"))
//                    + ((String) secondNode.getProperty("message"));
//
//            // START SNIPPET: transaction
//            tx.success();
//        }
////         END SNIPPET: transaction
//    }

//    void removeData() {
//        try (Transaction tx = graphDb.beginTx()) {
//            // START SNIPPET: removingData
//            // let's remove the data
//            firstNode.getSingleRelationship(RelTypes.KNOWS, Direction.OUTGOING).delete();
//            firstNode.delete();
//            secondNode.delete();
//            // END SNIPPET: removingData
//
//            tx.success();
//        }
//    }
//
//    void shutDown() {
//        System.out.println();
//        System.out.println("Shutting down database ...");
//        // START SNIPPET: shutdownServer
//        graphDb.shutdown();
//        // END SNIPPET: shutdownServer
//    }
}