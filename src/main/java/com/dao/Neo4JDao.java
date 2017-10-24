
package com.dao;

import com.Domain.TableDetail;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
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
        STH
    }

    public void createNodes(List<Map<String, Object>> rs, final TableDetail tableDetail) throws SQLException {


        try (Transaction tx = graphDb.beginTx()) {

            Index<Node> index = this.graphDb.index().forNodes(tableDetail.getTableName());

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


                //todo if pk is composite, create composite index, need to use cypher
                index.add(currentNode, tableDetail.getPk().get(0), row.get(tableDetail.getPk().get(0)));

            }
            tx.success();
        }
    }

    public void createRelationships(List<Map<String, Object>> rs, TableDetail tableDetail) {


        try (Transaction tx = graphDb.beginTx()) {

            if (tableDetail.getFks().size() > 0) {

                for (Map row : rs) {

                    ///todo check if it is composite primary key and if it is get composite index
                    Node primaryNode;
                    Index<Node> primayIndex = graphDb.index().forNodes(tableDetail.getTableName());
                    primaryNode = primayIndex.get(tableDetail.getPk().get(0),row.get(tableDetail.getPk().get(0))).getSingle();

                    for (Map.Entry<String, String> entry : tableDetail.getFks().entrySet()) {

                        Index<Node> foreingIndex = graphDb.index().forNodes(entry.getValue());
                        TableDetail foreignTableDetail = TableDetail.getTable(entry.getValue());

                        ///todo check if it is composite primary key and if it is get get composite index
                        String foreignKeyColumnNameAsPrimary = foreignTableDetail.getPk().get(0);
                        Node foreignNode = foreingIndex.get(foreignKeyColumnNameAsPrimary,row.get(entry.getKey())).getSingle();

                        Relationship relationship = primaryNode.createRelationshipTo(foreignNode, RelTypes.STH);
                    }

                }

            }

            tx.success();
        }
    }

    public void deletePrimaryKeysAndIndexes() {

        try (Transaction tx = graphDb.beginTx()) {

            for (Node node : graphDb.getAllNodes()) {
                //todo presume that node has only one label
                TableDetail tableDetail = TableDetail.getTable(node.getLabels().iterator().next().toString());

                //todo consider composite key
                String primareKeyColumnName = tableDetail.getPk().get(0);
                node.removeProperty(primareKeyColumnName);
            }

            for (String name : graphDb.index().nodeIndexNames()) {
                graphDb.index().forNodes(name).delete();
            }
            tx.success();
        }
    }

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