package com.dao;

import com.Domain.TableDetail;
import org.apache.commons.compress.utils.IOUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexCreator;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Repository
public class Neo4JDao {

    private GraphDatabaseService graphDb;

    @Autowired
    public Neo4JDao(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
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

        try (Transaction tx = graphDb.beginTx()) {

            List<String> fields = tableDetail.getFields();

            for (Map row : rs) {

                Node currentNode = graphDb.createNode();
                currentNode.addLabel(tableDetail::getTableName);

                for (String field : fields) {
                    Object object = row.get(field);
                    if(object != null) {
                        currentNode.setProperty(field, convertValue(object));
                    }
                }
                tx.success();
            }
        }
    }

    public void createIndices(TableDetail tableDetail) {

        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();

            IndexCreator indexCreator = schema.indexFor(Label.label(tableDetail.getTableName()));

            if(tableDetail.getPk().size() == 0) {
                System.out.println("sgg");
            }
            for (String pk : tableDetail.getPk()) {
                indexCreator = indexCreator.on(pk);
            }
            indexDefinition = indexCreator.create();
            tx.success();
        }
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
        }
    }

    //todo use JCypher

    private String getPrimaryNodeQuery(TableDetail tableDetail, Map<String, Object> row) {

        StringBuilder primaryNodeQuery = new StringBuilder("MATCH (n) WHERE n:" + tableDetail.getTableName());

        for (String pk : tableDetail.getPk()) {
            primaryNodeQuery.append(" AND n.").append(pk).append("=").append(row.get(pk));
        }
        primaryNodeQuery.append(" RETURN n");

        return primaryNodeQuery.toString();
    }

    //todo use JCypher

    private String getForeignNodeQuery(String foreignTable, Map.Entry<List<String>, String> entry, Map<String, Object> row) {

        TableDetail foreignTableDetail = TableDetail.getTable(foreignTable);

        StringBuilder foreignKeyQuery = new StringBuilder("MATCH (n) WHERE n:" + foreignTable);

        Iterator<String> primaryKeys = foreignTableDetail.getPk().iterator();
        Iterator<String> values = entry.getKey().iterator();

        while (primaryKeys.hasNext() && values.hasNext()) {
            foreignKeyQuery.append(" AND n.").append(primaryKeys.next()).append("=").append(row.get(values.next()));
        }
        foreignKeyQuery.append(" RETURN n");

        return foreignKeyQuery.toString();
    }

    public void createRelationships(TableDetail tableDetail, List<Map<String, Object>> rs) throws IOException, SQLException {

        try (Transaction tx = graphDb.beginTx()) {

            if (tableDetail.getFks().size() > 0) {

//                if (tableDetail.hasExactlyTwoForeignKeys()) {
//                    for (Map<String, Object> row : rs) {
//                        handleExactlyTwoForeignKeys(tableDetail, row);
//                    }
//                } else{
                    for (Map<String, Object> row : rs) {
                        handleMoreThanTwoForeignKeys(tableDetail,row);
                    }
//                }
                tx.success();
            }
        }
    }

    private void handleMoreThanTwoForeignKeys(TableDetail tableDetail, Map<String, Object> row) {
        Optional<Object> primaryNode = graphDb.execute(getPrimaryNodeQuery(tableDetail, row)).columnAs("n").stream().findFirst();

        for (Map.Entry<List<String>, String> entry : tableDetail.getFks().entrySet()) {

            final String foreignTable = entry.getValue();

            Optional<Object> foreignNode = graphDb.execute(getForeignNodeQuery(foreignTable, entry, row)).columnAs("n").stream().findFirst();

            if (primaryNode.isPresent() && foreignNode.isPresent()) {

                Node actualPrimaryNode = (Node) primaryNode.get();
                Node actualForeignNode = (Node) foreignNode.get();
                actualPrimaryNode.createRelationshipTo(actualForeignNode, () -> foreignTable);
            }
        }
    }

    private void handleExactlyTwoForeignKeys(TableDetail tableDetail, Map<String, Object> row) throws IOException, SQLException {

        List<String> fields = tableDetail.getFields();

        Iterator<Map.Entry<List<String>, String>> iterator = tableDetail.getFks().entrySet().iterator();

        Map.Entry<List<String>, String> next1 = iterator.next();
        Map.Entry<List<String>, String> next2 = iterator.next();

        String foreignTable1 = next1.getValue();
        String foreignTable2 = next2.getValue();

        Optional<Object> foreignNode1 = graphDb.execute(getForeignNodeQuery(foreignTable1, next1, row)).columnAs("n").stream().findFirst();
        Optional<Object> foreignNode2 = graphDb.execute(getForeignNodeQuery(foreignTable2, next2, row)).columnAs("n").stream().findFirst();

        if (foreignNode1.isPresent() && foreignNode2.isPresent()) {

            Node actualPrimaryNode = (Node) foreignNode1.get();
            Node actualForeignNode = (Node) foreignNode2.get();
            Relationship relationshipTo = actualPrimaryNode.createRelationshipTo(actualForeignNode, () -> foreignTable2);

            for (String field : fields) {
                if(!tableDetail.isPartOfPk(field)) {
                    Object object = row.get(field);
                    relationshipTo.setProperty(field, convertValue(object));
                }
            }
        }
    }

    public void deletePrimaryKeys() {

        try (Transaction tx = graphDb.beginTx()) {
            for (Node node : graphDb.getAllNodes()) {
                TableDetail tableDetail = TableDetail.getTable(node.getLabels().iterator().next().toString());

                tableDetail.getPk().forEach(node::removeProperty);
                Iterator<List<String>> iterator = tableDetail.getFks().keySet().iterator();
                while(iterator.hasNext()) {
                    iterator.next().forEach(node::removeProperty);
                }
            }
            tx.success();
        }
    }
}