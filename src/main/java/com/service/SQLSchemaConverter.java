package com.service;

import com.Domain.TableDetail;
import com.dao.SQLImportDao;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.tools.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.jooq.impl.DSL.constraint;

/**
 * Created by John on 2018-02-23.
 */
@Service
public class SQLSchemaConverter implements ApplicationContextAware {

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
            return SQLDataType.VARCHAR;
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

    public void createSQLSchema(List<TableDetail> tableDetails) throws SQLException, IOException {


        DSLContext create = DSL.using(SQLImportDao.getJdbcTemplate().getDataSource().getConnection(), SQLDialect.MYSQL);

        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());

        String schema = TableDetail.schemaName;

        stringJoiner.add(create.createSchema(schema).getSQL());

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

            //set up Pk Constraint
            for (String element : tableDetail.getPk()) {
                ((CreateTableColumnStep) table).constraints(constraint(element+"_ID").primaryKey(element+"_ID"));
            }

            stringJoiner.add(((CreateTableColumnStep) table).getSQL());
        }

        for(TableDetail tableDetail : tableDetails) {
            //copy fks
            for(String element:tableDetail.getGraphFks()) {
                stringJoiner.add(create.alterTable(tableDetail.getTableName()).add(element+"_ID",  SQLDataType.INTEGER).getSQL());
                stringJoiner.add(create.alterTable(tableDetail.getTableName()).add(constraint(tableDetail.getTableName()+"_"+element).foreignKey(element+"_ID").references(element,element+"_ID")).getSQL());
            }
        }

        String rootPath = System.getProperty("user.dir");
        File schemaSQL = new File(StringUtils.join(rootPath, "/src/" , "schema.sql"));
        schemaSQL.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(schemaSQL));
        writer.write(stringJoiner.toString());
        writer.close();

        applicationContext.getBean("dataSourceInitializer");

        schemaSQL.delete();
    }

}
