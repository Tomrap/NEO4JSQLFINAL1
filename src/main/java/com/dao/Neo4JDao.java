package com.dao;

import com.Domain.TableDetail;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

@Repository
public class Neo4JDao {

    private HashFunction hf = Hashing.murmur3_128();
    private BatchInserter batchInserter;

    private static final Logger logger = Logger.getLogger(Neo4JDao.class);

    @Autowired
    public Neo4JDao(BatchInserter batchInserter) throws IOException {
        this.batchInserter = batchInserter;
    }

    public Object convertValue(Object value) throws SQLException, IOException {
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        if (value instanceof Blob) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            IOUtils.copy(((Blob) value).getBinaryStream(), bo);
            return bo.toByteArray();
        }
        return value;
    }

    public void createNodes(final TableDetail tableDetail, List<Map<String, Object>> rs) throws SQLException, IOException {

        logger.info("Start generating nodes for " + tableDetail.getTableName());



//        try (Transaction tx = graphDb.beginTx()) {
//
//            List<String> fields = tableDetail.getFields();
//
//            for (Map row : rs) {
//
//                Node currentNode = graphDb.createNode();
//                currentNode.addLabel(tableDetail::getTableName);
//
//                for (String field : fields) {
//                    Object object = row.get(field);
//                    if(object != null) {
//                        currentNode.setProperty(field, convertValue(object));
//                    }
//                }
//                tx.success();
//            }
//        }

        HashMap<Integer,Integer> mapping= new HashMap<>();


        List<String> fields = tableDetail.getFields();

        long currentIndex = tableDetail.getFirstIndex();

        for (Map row : rs) {

            Map<String, Object> map = new HashMap<>();
            for (String field : fields) {
                Object object = row.get(field);
                if(object != null) {
                    map.put(field,convertValue(object));
                }
            }



//
//            List<Integer> list = new ArrayList<>();
//
//
            Hasher hasher = hf.newHasher();
            for(String pk : tableDetail.getPk()) {
                hasher = hasher.putInt((Integer) row.get(pk));
            }
//
            mapping.put(hasher.hash().asInt(), (int) (currentIndex-tableDetail.getFirstIndex()));
//            int hash = Objects.hash(tableDetail.getTableName(), list);
//
//            int hashcode = hash & Integer.MAX_VALUE;

//            logger.info(hashcode);
            batchInserter.createNode(currentIndex, map, tableDetail::getTableName);
//            logger.info("done");
            currentIndex++;
        }

        tableDetail.setMapping(mapping);

        logger.info("Finished generating nodes for " + tableDetail.getTableName());
    }

//    public void createIndices(TableDetail tableDetail) {
//
//        logger.info("Start generating indexes for " + tableDetail.getTableName());
//
//        IndexDefinition indexDefinition;
//        try (Transaction tx = graphDb.beginTx()) {
//            Schema schema = graphDb.schema();
//
//            IndexCreator indexCreator = schema.indexFor(Label.label(tableDetail.getTableName()));
//
//            if(tableDetail.getPk().size() == 0) {
//                System.out.println("sgg");
//            }
//            for (String pk : tableDetail.getPk()) {
//                indexCreator = indexCreator.on(pk);
//            }
//            indexDefinition = indexCreator.create();
//            tx.success();
//        }
//        try (Transaction tx = graphDb.beginTx()) {
//            Schema schema = graphDb.schema();
//            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
//        }
//
//        logger.info("Finished generating indexes for " + tableDetail.getTableName());
//    }

    //todo use JCypher

//    private String getPrimaryNodeQuery(TableDetail tableDetail, Map<String, Object> row) {
//
//        StringBuilder primaryNodeQuery = new StringBuilder("MATCH (n) WHERE n:" + tableDetail.getTableName());
//
//        for (String pk : tableDetail.getPk()) {
//            primaryNodeQuery.append(" AND n.").append(pk).append("=").append(row.get(pk));
//        }
//        primaryNodeQuery.append(" RETURN n");
//
//        return primaryNodeQuery.toString();
//    }
//
//    //todo use JCypher
//
//    private String getForeignNodeQuery(String foreignTable, Map.Entry<List<String>, String> entry, Map<String, Object> row) {
//
//        TableDetail foreignTableDetail = TableDetail.getTable(foreignTable);
//
//        StringBuilder foreignKeyQuery = new StringBuilder("MATCH (n) WHERE n:" + foreignTable);
//
//        Iterator<String> primaryKeys = foreignTableDetail.getPk().iterator();
//        Iterator<String> values = entry.getKey().iterator();
//
//        while (primaryKeys.hasNext() && values.hasNext()) {
//            foreignKeyQuery.append(" AND n.").append(primaryKeys.next()).append("=").append(row.get(values.next()));
//        }
//        foreignKeyQuery.append(" RETURN n");
//
//        return foreignKeyQuery.toString();
//    }

    public void createRelationships(TableDetail tableDetail, List<Map<String, Object>> rs) throws IOException, SQLException {

        logger.info("Start generating relationships for " + tableDetail.getTableName());

//        try (Transaction tx = graphDb.beginTx()) {
//
//            if (tableDetail.getFks().size() > 0) {
//
////                if (tableDetail.hasExactlyTwoForeignKeys()) {
////                    for (Map<String, Object> row : rs) {
////                        handleExactlyTwoForeignKeys(tableDetail, row);
////                    }
////                } else{
//                    for (Map<String, Object> row : rs) {
//                        handleMoreThanTwoForeignKeys(tableDetail,row);
//                    }
////                }
//                tx.success();
//            }
//        }

            int currentValue = 0;

            if (tableDetail.getFks().size() > 0) {

                for (Map<String, Object> row : rs) {

//                    Optional<Object> primaryNode = graphDb.execute(getPrimaryNodeQuery(tableDetail, row)).columnAs("n").stream().findFirst();

//                    String toHashPrimary = tableDetail.getTableName() + (Integer)row.get(tableDetail.getPk().get(0));


//                    Hasher hasher = hf.newHasher();
//                    for(String pk : tableDetail.getPk()) {
//
//                        hasher = hasher.putInt((Integer) row.get(pk));
//
//                    }
//
//
//                    int primaryNodeIndex = (int) (tableDetail.getFirstIndex() + tableDetail.getMapping().get(hasher.hash().asInt()));

                    int primaryNodeIndex = (int) (tableDetail.getFirstIndex() + currentValue);
                    currentValue++;



//                    long primaryNodeId = tableDetail.getFirstIndex() + (Integer)row.get(tableDetail.getPk().get(0)) - 1;

                    outerloop:
                    for (Map.Entry<List<String>, String> entry : tableDetail.getFks().entrySet()) {

                        final String foreignTable = entry.getValue();

                        TableDetail foreignTableDetail = TableDetail.getTable(foreignTable);

                        Hasher hasherForeign = hf.newHasher();
                        for (String string : entry.getKey()) {

                            Integer value = (Integer)row.get(string);
                            if(value == null) {
                                continue outerloop;
                            }
                            hasherForeign = hasherForeign.putInt(value);



//                            long foreignNodeId = foreignFirstIndex + value - 1;

//                            String toHashForeign = foreignTableDetail.getTableName() + value;

//                            if(!batchInserter.nodeExists(foreignNodeId) || !batchInserter.nodeExists(primaryNodeId)) {
//                                System.out.println("here");
//                            }
//                            logger.info(primaryNodeId);
//
                        }

                        int foreignNodeIndex = (int) (foreignTableDetail.getFirstIndex() + foreignTableDetail.getMapping().get(hasherForeign.hash().asInt()));

                        batchInserter.createRelationship(primaryNodeIndex,foreignNodeIndex, foreignTable::toUpperCase,null);
//                        Optional<Object> foreignNode = graphDb.execute(getForeignNodeQuery(foreignTable, entry, row)).columnAs("n").stream().findFirst();


//                actualPrimaryNode.createRelationshipTo(actualForeignNode, () -> foreignTable);

                    }

                }


            }


        logger.info("Finished generating relationships for " + tableDetail.getTableName());
    }

//    private void handleMoreThanTwoForeignKeys(TableDetail tableDetail, Map<String, Object> row) {
//        Optional<Object> primaryNode = graphDb.execute(getPrimaryNodeQuery(tableDetail, row)).columnAs("n").stream().findFirst();
//
//        for (Map.Entry<List<String>, String> entry : tableDetail.getFks().entrySet()) {
//
//            final String foreignTable = entry.getValue();
//
//            Optional<Object> foreignNode = graphDb.execute(getForeignNodeQuery(foreignTable, entry, row)).columnAs("n").stream().findFirst();
//
//            if (primaryNode.isPresent() && foreignNode.isPresent()) {
//
//                Node actualPrimaryNode = (Node) primaryNode.get();
//                Node actualForeignNode = (Node) foreignNode.get();
//
////                batchInserter.createRelationship(actualPrimaryNode.getId(),actualForeignNode.getId(),() -> foreignTable,null);
////                actualPrimaryNode.createRelationshipTo(actualForeignNode, () -> foreignTable);
//            }
//        }
//    }
//
//    private void handleExactlyTwoForeignKeys(TableDetail tableDetail, Map<String, Object> row) throws IOException, SQLException {
//
//        List<String> fields = tableDetail.getFields();
//
//        Iterator<Map.Entry<List<String>, String>> iterator = tableDetail.getFks().entrySet().iterator();
//
//        Map.Entry<List<String>, String> next1 = iterator.next();
//        Map.Entry<List<String>, String> next2 = iterator.next();
//
//        String foreignTable1 = next1.getValue();
//        String foreignTable2 = next2.getValue();
//
//        Optional<Object> foreignNode1 = graphDb.execute(getForeignNodeQuery(foreignTable1, next1, row)).columnAs("n").stream().findFirst();
//        Optional<Object> foreignNode2 = graphDb.execute(getForeignNodeQuery(foreignTable2, next2, row)).columnAs("n").stream().findFirst();
//
//        if (foreignNode1.isPresent() && foreignNode2.isPresent()) {
//
//            Node actualPrimaryNode = (Node) foreignNode1.get();
//            Node actualForeignNode = (Node) foreignNode2.get();
//            Relationship relationshipTo = actualPrimaryNode.createRelationshipTo(actualForeignNode, () -> foreignTable2);
//
//            for (String field : fields) {
//                if(!tableDetail.isPartOfPk(field)) {
//                    Object object = row.get(field);
//                    relationshipTo.setProperty(field, convertValue(object));
//                }
//            }
//        }
//    }



    public void shutDown() {
        batchInserter.shutdown();
    }
}