
package com.dao;

import com.Domain.TableDetail;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.IndexCreator;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class Neo4JDao {

    private GraphDatabaseService graphDb;

    @Autowired
    public Neo4JDao(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public void createNodes(final TableDetail tableDetail, List<Map<String, Object>> rs) throws SQLException {

        try (Transaction tx = graphDb.beginTx()) {

            List<String> fields = tableDetail.getFields();

            for (Map row : rs) {

                Node currentNode = graphDb.createNode();
                currentNode.addLabel(tableDetail::getTableName);

                for (String field : fields) {
                    Object object = row.get(field);
                    currentNode.setProperty(field, object);
                }
                tx.success();
            }
        }
    }

    public void createIndices(TableDetail tableDetail) {

        IndexDefinition indexDefinition;
        try ( Transaction tx = graphDb.beginTx() )
        {
            Schema schema = graphDb.schema();

            IndexCreator indexCreator = schema.indexFor( Label.label( tableDetail.getTableName() ) );

            for(String pk: tableDetail.getPk()) {
                indexCreator = indexCreator.on(pk);
            }
            indexDefinition = indexCreator.create();
            tx.success();
        }
        try ( Transaction tx = graphDb.beginTx() )
        {
            Schema schema = graphDb.schema();
            schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
        }
    }

    public void createRelationships(TableDetail tableDetail, List<Map<String, Object>> rs) {


        try (Transaction tx = graphDb.beginTx()) {

            if (tableDetail.getFks().size() > 0) {

                for (Map row : rs) {

                    StringBuilder primaryNodeQuery = new StringBuilder("MATCH (n) WHERE n:" + tableDetail.getTableName());

                    for(String pk: tableDetail.getPk()) {
                        primaryNodeQuery.append(" AND n." + pk + "=" + row.get(pk));
                    }
                    primaryNodeQuery.append(" RETURN n");

                    Optional<Object> primaryNode  = graphDb.execute(primaryNodeQuery.toString()).columnAs("n").stream().findFirst();

                    for (Map.Entry<List<String>, String> entry : tableDetail.getFks().entrySet()) {

                        final String foreignTable = entry.getValue();
                        TableDetail foreignTableDetail = TableDetail.getTable(foreignTable);

                        StringBuilder foreignKeyQuery = new StringBuilder("MATCH (n) WHERE n:" + foreignTable);

                        Iterator<String> primaryKeys = foreignTableDetail.getPk().iterator();
                        Iterator<String> values = entry.getKey().iterator();

                        while (primaryKeys.hasNext() && values.hasNext()) {
                            foreignKeyQuery.append(" AND n." + primaryKeys.next() + "=" + row.get(values.next()));
                        }
                        foreignKeyQuery.append(" RETURN n");

                        Optional<Object> foreignNode  = graphDb.execute(foreignKeyQuery.toString()).columnAs("n").stream().findFirst();

                        if(primaryNode.isPresent() && foreignNode.isPresent()) {

                            Node actualPrimaryNode = (Node) primaryNode.get();
                            Node actualForeignNode = (Node) foreignNode.get();
                            Relationship relationshipTo = actualPrimaryNode.createRelationshipTo(actualForeignNode, () -> foreignTable);
                            for (String prop: entry.getKey()) {
                                relationshipTo.setProperty(prop, row.get(prop));
                            }
                        }
                    }
                }
                tx.success();
            }
        }
    }

    public void deletePrimaryKeysAndIndexes() {


        try (Transaction tx = graphDb.beginTx()) {
            for (Node node : graphDb.getAllNodes()) {
                TableDetail tableDetail = TableDetail.getTable(node.getLabels().iterator().next().toString());

                tableDetail.getPk().forEach(node::removeProperty);
            }

            for (String name : graphDb.index().nodeIndexNames()) {
                graphDb.index().forNodes(name).delete();
            }
            tx.success();
        }
    }
}