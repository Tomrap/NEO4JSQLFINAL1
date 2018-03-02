package com.GraphToSQL.Service;

import com.GraphToSQL.Domain.GraphToSQLTableDetail;
import com.GraphToSQL.Domain.MyNode;
import com.GraphToSQL.Domain.TableRow;
import com.SQLToGraph.Dao.SQLImportDao;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.jooq.impl.DSL.*;

/**
 * Created by John on 2018-02-23.
 */
@Service
public class SQLRowToSQLConverter {

    private String id = "_ID";

    @Autowired
    private SQLImportDao SQLImportDao;

    private DataType inferType(Object value) {

        if(value instanceof Boolean) {
           return SQLDataType.BIT;
        }
        if(value instanceof Integer || value instanceof Byte || value instanceof Short ) {
            return SQLDataType.INTEGER;
        }
        if(value instanceof String) {
            return SQLDataType.VARCHAR.length(130);
        }
        if(value instanceof Float || value instanceof Double) {
            return SQLDataType.DECIMAL(10,5);
        }
        if (value instanceof BigDecimal || value instanceof Long) return SQLDataType.BIGINT;

        return SQLDataType.BLOB;
    }

    public void createAndInsertSQLRows(Map<String, Map<Integer, TableRow>> allRows) throws SQLException, IOException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        connection.setCatalog(GraphToSQLTableDetail.schemaName);
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
        Settings settings = create.settings();
        settings.setDebugInfoOnStackTrace(false);
        settings.setExecuteLogging(false);

        List<Object> rowValues;
        Collection<Field<Object>> columnNames;
        List<InsertValuesStepN<Record>> inserts;

        for(Map.Entry<String, Map<Integer, TableRow>> element: allRows.entrySet()) {

            inserts = new ArrayList<>();

            for(Map.Entry<Integer, TableRow> row: element.getValue().entrySet()) {

                rowValues = new ArrayList<>();
                columnNames = new ArrayList<>();

                MyNode myNode = row.getValue().getMyNode();

                columnNames.add(field(element.getKey()+id));
                rowValues.add(myNode.getSqlID());

                for(Map.Entry<String, Object> myNodeValue: myNode.getValues().entrySet()) {

                    columnNames.add(field(myNodeValue.getKey()));
                    rowValues.add(myNodeValue.getValue());
                }

                for(Map.Entry<String, Object> relationshipProperty: row.getValue().getRelationshipProperties().entrySet()) {
                    columnNames.add(field(relationshipProperty.getKey()));
                    rowValues.add(relationshipProperty.getValue());
                }

                for(Map.Entry<String, Integer> foreignKey : row.getValue().getForeignKeys().entrySet()) {
                    columnNames.add(field(foreignKey.getKey()+id));
                    rowValues.add(foreignKey.getValue());
                }

                inserts.add(create.insertInto(table(element.getKey()), columnNames).values(rowValues));
            }
            create.batch(inserts).execute();
        }
    }

    public void createSQLTables(List<GraphToSQLTableDetail> graphToSQLTableDetails) throws SQLException, IOException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        String schema = GraphToSQLTableDetail.schemaName;
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
        create.createSchema(schema).execute();
        connection.setCatalog(schema);

        for(GraphToSQLTableDetail graphToSQLTableDetail : graphToSQLTableDetails) {

            CreateTableAsStep<Record> table = create.createTable(graphToSQLTableDetail.getTableName());

            for (String element : graphToSQLTableDetail.getPk()) {
                table.column(element+id, SQLDataType.INTEGER.nullable(false));
            }

            for (Map.Entry<String, Object> element : graphToSQLTableDetail.getColumnsAndTypes().entrySet()) {
                table.column(element.getKey(), inferType(element.getValue()));
            }

            for(String element: graphToSQLTableDetail.getGraphFks()) {
                table.column(element+id,  SQLDataType.INTEGER).getSQL();
            }

            for (String element : graphToSQLTableDetail.getPk()) {
                ((CreateTableColumnStep) table).constraints(constraint(element+id).primaryKey(element+id));
            }

            ((CreateTableColumnStep) table).execute();
        }
    }

    public void createForeignKeysConstraints(List<GraphToSQLTableDetail> graphToSQLTableDetails) throws SQLException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        connection.setCatalog(GraphToSQLTableDetail.schemaName);
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);

        for (GraphToSQLTableDetail graphToSQLTableDetail : graphToSQLTableDetails) {

            for (String element : graphToSQLTableDetail.getGraphFks()) {
                create.alterTable(graphToSQLTableDetail.getTableName()).add(constraint(graphToSQLTableDetail.getTableName() + "_" + element).foreignKey(element + id)
                        .references(element, element + id)).execute();
            }
        }
    }
}
