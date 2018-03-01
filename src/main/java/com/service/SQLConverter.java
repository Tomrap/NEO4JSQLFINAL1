package com.service;

import com.Domain.MyNode;
import com.Domain.TableDetail;
import com.Domain.TableRow;
import com.dao.SQLImportDao;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.jooq.impl.DSL.*;

/**
 * Created by John on 2018-02-23.
 */
@Service
public class SQLConverter{


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



    public void createSQLRows(Map<String, Map<Integer, TableRow>> allRows) throws SQLException, IOException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        connection.setCatalog(TableDetail.schemaName);
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

                columnNames.add(field(element.getKey()+"_ID"));
                rowValues.add(myNode.getSqlID());

                for(Map.Entry<String, Object> myNodeValue: myNode.getValues().entrySet()) {

                    columnNames.add(field(myNodeValue.getKey()));
                    rowValues.add(myNodeValue.getValue());
                }

                for(Map.Entry<String, Integer> foreignKey : row.getValue().getForeignKeys().entrySet()) {
                    columnNames.add(field(foreignKey.getKey()+"_ID"));
                    rowValues.add(foreignKey.getValue());
                }

                inserts.add(create.insertInto(table(element.getKey()), columnNames).values(rowValues));
            }
            create.batch(inserts).execute();
        }

    }

    public void createSQLSchema(List<TableDetail> tableDetails) throws SQLException, IOException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        String schema = TableDetail.schemaName;
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
        create.createSchema(schema).execute();
        connection.setCatalog(TableDetail.schemaName);

        for(TableDetail tableDetail : tableDetails) {

            CreateTableAsStep<Record> table = create.createTable(tableDetail.getTableName());

            //TODO currently name of the primary key is the same as the name of the table and this is used later!
            //copy pk
            for (String element : tableDetail.getPk()) {
                table.column(element+"_ID", SQLDataType.INTEGER.nullable(false));
            }

            //copy fields
            for (Map.Entry<String, Object> element : tableDetail.getColumnsAndTypes().entrySet()) {
                table.column(element.getKey(), inferType(element.getValue()));
            }


            for(String element:tableDetail.getGraphFks()) {
                table.column(element+"_ID",  SQLDataType.INTEGER).getSQL();
            }

            //set up Pk Constraint
            for (String element : tableDetail.getPk()) {
                ((CreateTableColumnStep) table).constraints(constraint(element+"_ID").primaryKey(element+"_ID"));
            }

            ((CreateTableColumnStep) table).execute();
        }
    }

    public void createFOreignKeysConstraints(List<TableDetail> tableDetails) throws SQLException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        connection.setCatalog(TableDetail.schemaName);
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);


        for (TableDetail tableDetail : tableDetails) {

            //copy fks
            for (String element : tableDetail.getGraphFks()) {
                create.alterTable(tableDetail.getTableName()).add(constraint(tableDetail.getTableName() + "_" + element).foreignKey(element + "_ID").references(element, element + "_ID")).execute();
            }
        }
    }
}
