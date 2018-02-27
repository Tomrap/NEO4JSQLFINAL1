package com.service;

import com.Domain.MyNode;
import com.Domain.TableDetail;
import com.Domain.TableRow;
import com.dao.SQLImportDao;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;

import static org.jooq.impl.DSL.*;

/**
 * Created by John on 2018-02-23.
 */
@Service
public class SQLConverter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Autowired
    private SQLImportDao SQLImportDao;

    private DataType convertValue(Object value) {

        if(value instanceof Integer) {
            return SQLDataType.INTEGER;
        }
        if(value instanceof String) {
            return SQLDataType.VARCHAR.length(130);
        }
        if (value instanceof Date) {
            return SQLDataType.DATE;
        }
        if (value instanceof BigDecimal || value instanceof Long) return SQLDataType.BIGINT;
        if (value instanceof Blob) {
            return SQLDataType.BLOB;
        }
        //TODO make sure it never returns null
        return SQLDataType.BLOB;
    }

    public void createSQLRows(Map<String, Map<Integer, TableRow>> allRows) throws SQLException, IOException {
        DSLContext create = DSL.using(SQLImportDao.getJdbcTemplate().getDataSource().getConnection(), SQLDialect.MYSQL);
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        String schema = TableDetail.schemaName;

        List<Object> rowValues = new ArrayList<>();
        Collection<Field<Object>> columnNames = new ArrayList<>();
        //TODO
        stringJoiner.add("USE `" + schema + "`");

        for(Map.Entry<String, Map<Integer, TableRow>> element: allRows.entrySet()) {

            for(Map.Entry<Integer, TableRow> row: element.getValue().entrySet()) {

                rowValues = new ArrayList<>();
                columnNames = new ArrayList<>();

                MyNode myNode = row.getValue().getMyNode();

                columnNames.add(field(myNode.getPrimaryKeyName()+"_ID"));
                rowValues.add(myNode.getSqlID());

                for(Map.Entry<String, Object> myNodeValue: myNode.getValues().entrySet()) {

                    columnNames.add(field(myNodeValue.getKey()));
                    rowValues.add(myNodeValue.getValue());
                }

                for(Map.Entry<String, Integer> foreignKey : row.getValue().getForeignKeys().entrySet()) {
                    columnNames.add(field(foreignKey.getKey()+"_ID"));
                    rowValues.add(foreignKey.getValue());
                }


                stringJoiner.add(create.insertInto(table(element.getKey()), columnNames).values(rowValues).getSQL(true));
            }

        }

//        System.out.println(stringJoiner.toString());

        String rootPath = System.getProperty("user.dir");
        File dataSQL = new File(StringUtils.join(rootPath, "/src/" , "data.sql"));
        dataSQL.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(dataSQL));
        writer.write(stringJoiner.toString());
        writer.close();

    }

    public void createSQLSchema(List<TableDetail> tableDetails) throws SQLException, IOException {


        DSLContext create = DSL.using(SQLImportDao.getJdbcTemplate().getDataSource().getConnection(), SQLDialect.MYSQL);

        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());

        String schema = TableDetail.schemaName;

        stringJoiner.add(create.createSchema(schema).getSQL());

        //TODO
        stringJoiner.add("USE `" + schema + "`");

        for(TableDetail tableDetail : tableDetails) {

            CreateTableAsStep<Record> table = create.createTable(tableDetail.getTableName());

            //TODO currently name of the primary key is the same as the name of the table and this is used later!
            //copy pk
            for (String element : tableDetail.getPk()) {
                table.column(element+"_ID", SQLDataType.INTEGER.nullable(false));
            }

            //copy fields
            for (Map.Entry<String, Object> element : tableDetail.getColumnsAndTypes().entrySet()) {
                table.column(element.getKey(), convertValue(element.getValue()));
            }


            for(String element:tableDetail.getGraphFks()) {
                table.column(element+"_ID",  SQLDataType.INTEGER).getSQL();
            }

            //set up Pk Constraint
            for (String element : tableDetail.getPk()) {
                ((CreateTableColumnStep) table).constraints(constraint(element+"_ID").primaryKey(element+"_ID"));
            }

            stringJoiner.add(((CreateTableColumnStep) table).getSQL());
        }

        String rootPath = System.getProperty("user.dir");
        File schemaSQL = new File(StringUtils.join(rootPath, "/src/" , "schema.sql"));
        schemaSQL.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(schemaSQL));
        writer.write(stringJoiner.toString());
        writer.close();




        stringJoiner = new StringJoiner(System.lineSeparator());
        stringJoiner.add("USE `" + schema + "`");

        for(TableDetail tableDetail : tableDetails) {
            //copy fks
            for(String element:tableDetail.getGraphFks()) {
                stringJoiner.add(create.alterTable(tableDetail.getTableName()).add(constraint(tableDetail.getTableName()+"_"+element).foreignKey(element+"_ID").references(element,element+"_ID")).getSQL());
            }
        }

        rootPath = System.getProperty("user.dir");
        File alterSchema = new File(StringUtils.join(rootPath, "/src/" , "alterSchema.sql"));
        alterSchema.createNewFile();
        writer = new BufferedWriter(new FileWriter(alterSchema));
        writer.write(stringJoiner.toString());
        writer.close();

    }

    public void executeAnddestroyScripts() {


        applicationContext.getBean("dataSourceInitializer");

        String rootPath = System.getProperty("user.dir");
        File schemaSQL = new File(StringUtils.join(rootPath, "/src/" , "schema.sql"));
        File alterSchema = new File(StringUtils.join(rootPath, "/src/" , "alterSchema.sql"));
        File dataSQL = new File(StringUtils.join(rootPath, "/src/" , "data.sql"));

        schemaSQL.delete();
        alterSchema.delete();
        dataSQL.delete();

    }


}
