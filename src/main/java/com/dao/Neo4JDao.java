
package com.dao;

import com.Domain.TableDetail;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
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


//    private static enum RelTypes implements RelationshipType {
//        KNOWS
//    }

    public void createNode(List<Map<String, Object>> rs, TableDetail tableDetail) throws SQLException {



    }

    public void createAttributes(ResultSet rs, TableDetail tableDetail) {

        List<String> fields = tableDetail.getFields();




    }

    public void createDb() throws IOException {

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
//            relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
//            relationship.setProperty("message", "brave Neo4j ");
//            // END SNIPPET: addData
//
//            // START SNIPPET: readData
//            System.out.print(firstNode.getProperty("message"));
//            System.out.print(relationship.getProperty("message"));
//            System.out.print(secondNode.getProperty("message"));
//            // END SNIPPET: readData
//
//            greeting = ((String) firstNode.getProperty("message"))
//                    + ((String) relationship.getProperty("message"))
//                    + ((String) secondNode.getProperty("message"));
//
//            // START SNIPPET: transaction
//            tx.success();
//        }
////         END SNIPPET: transaction
    }

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