package com.GraphToSQL.Service;

import com.GraphToSQL.Domain.GraphToSQLTableDetail;
import com.GraphToSQL.Domain.MyNode;
import com.GraphToSQL.Domain.TableRow;
import com.SQLToGraph.Dao.SQLImportDao;
import com.config.SpringConfig;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.*;

/**
 * Created by John on 2018-02-23.
 */
@Service
@PropertySource("classpath:config.properties")
public class SQLRowToSQLConverter {

    @Value("${GraphToSql.schemaName}")
    private String schemaName;

    @Autowired
    private SQLImportDao SQLImportDao;

    private DataType inferType(Object value) {

        if (value instanceof Boolean) {
            return SQLDataType.BIT;
        }
        if (value instanceof Integer || value instanceof Byte || value instanceof Short) {
            return SQLDataType.INTEGER;
        }
        if (value instanceof String) {
            return SQLDataType.VARCHAR.length(200);
        }
        if (value instanceof Float || value instanceof Double) {
            return SQLDataType.DECIMAL(10, 5);
        }
        if (value instanceof BigDecimal || value instanceof Long) return SQLDataType.BIGINT;

        return SQLDataType.BLOB;
    }

    private Object convertValue(Object value) throws IOException, SQLException {

        //TODO convert to BLOB
        if (value instanceof Object[]) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            oos.close();
            bos.close();
            byte[] data = bos.toByteArray();
            return new javax.sql.rowset.serial.SerialBlob(data);
        }
        return value;
    }

    private String escape(String value) {
        //TODO wont work in case of already escaped value (starts with '`')
        return "`" + value + "`";
    }

    private String addID(String value) {
        return value + "_ID";
    }

    public void createAndInsertSQLRows(Map<String, Map<Integer, TableRow>> allRows) throws SQLException, IOException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        connection.setCatalog(schemaName);
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
        Settings settings = create.settings();
        settings.setDebugInfoOnStackTrace(false);
        settings.setExecuteLogging(false);

        List<Object> rowValues;
        Collection<Field<Object>> columnNames;
        List<InsertValuesStepN<Record>> inserts;

        for (Map.Entry<String, Map<Integer, TableRow>> element : allRows.entrySet()) {

            inserts = new ArrayList<>();

            for (Map.Entry<Integer, TableRow> row : element.getValue().entrySet()) {

                rowValues = new ArrayList<>();
                columnNames = new ArrayList<>();

                columnNames.add(field(escape(addID(element.getKey()))));
                MyNode myNode = row.getValue().getMyNode();

                if (myNode == null) {
                    rowValues.add(row.getValue().getSQLid());
                } else {
                    rowValues.add(myNode.getSqlID());
                    for (Map.Entry<String, Object> myNodeValue : myNode.getValues().entrySet()) {
                        columnNames.add(field(escape(myNodeValue.getKey())));
                        rowValues.add(convertValue(myNodeValue.getValue()));
                    }
                }
                for (Map.Entry<String, Object> relationshipProperty : row.getValue().getRelationshipProperties().entrySet()) {
                    columnNames.add(field(escape(relationshipProperty.getKey())));
                    rowValues.add(convertValue(relationshipProperty.getValue()));
                }

                for (Map.Entry<String, Integer> foreignKey : row.getValue().getForeignKeys().entrySet()) {
                    columnNames.add(field(escape(foreignKey.getKey())));
                    rowValues.add(foreignKey.getValue());
                }

                inserts.add(create.insertInto(table(escape(element.getKey())), columnNames).values(rowValues));
            }
            create.batch(inserts).execute();
        }
    }

    public void createSQLTables(List<GraphToSQLTableDetail> graphToSQLTableDetails) throws SQLException, IOException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        String schema = schemaName;
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);
        create.createSchema(schema).execute();
        connection.setCatalog(schema);

        for (GraphToSQLTableDetail graphToSQLTableDetail : graphToSQLTableDetails) {

            CreateTableAsStep<Record> table = create.createTable(graphToSQLTableDetail.getTableName());

            for (String element : graphToSQLTableDetail.getPk()) {
                table.column(addID(element), SQLDataType.INTEGER.nullable(false));
            }

            for (Map.Entry<String, Object> element : graphToSQLTableDetail.getColumnsAndTypes().entrySet()) {
                table.column(element.getKey(), inferType(element.getValue()));
            }

            for (Map.Entry<String, String> element : graphToSQLTableDetail.getGraphFks()) {
                table.column(element.getValue() + "_" + element.getKey(), SQLDataType.INTEGER).getSQL();
            }

            for (String element : graphToSQLTableDetail.getPk()) {
                ((CreateTableColumnStep) table).constraints(constraint(addID(element)).primaryKey(addID(element)));
            }

            ((CreateTableColumnStep) table).execute();
        }
    }

    public void createForeignKeysConstraints(List<GraphToSQLTableDetail> graphToSQLTableDetails) throws SQLException {

        Connection connection = SQLImportDao.getJdbcTemplate().getDataSource().getConnection();
        connection.setCatalog(schemaName);
        DSLContext create = DSL.using(connection, SQLDialect.MYSQL);

        for (GraphToSQLTableDetail graphToSQLTableDetail : graphToSQLTableDetails) {

            for (Map.Entry<String, String> element : graphToSQLTableDetail.getGraphFks()) {
                create.alterTable(graphToSQLTableDetail.getTableName()).add(constraint(graphToSQLTableDetail.getTableName()+"_"+element.getValue() + "_" + element.getKey()).foreignKey(element.getValue() + "_" + element.getKey())
                        .references(element.getKey(), addID(element.getKey()))).execute();
            }
        }
    }
}
