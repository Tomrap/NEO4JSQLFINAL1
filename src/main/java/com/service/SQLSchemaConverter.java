package com.service;

import com.Domain.TableDetail;
import com.dao.SQLImportDao;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.constraint;

/**
 * Created by John on 2018-02-23.
 */
@Service
public class SQLSchemaConverter {

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
        return null;
    }

    public void createSQLSchema(List<TableDetail> tableDetails) throws SQLException {


        DSLContext create = DSL.using(SQLImportDao.getJdbcTemplate().getDataSource().getConnection(), SQLDialect.MYSQL);

        String sqlSchema = create.createSchema(TableDetail.schemaName).getSQL();

        for(TableDetail tableDetail : tableDetails) {

            CreateTableAsStep<Record> table = create.createTable(tableDetail.getTableName());

            //TODO currently name of the primary key is the same as the name of the table and this is used later!
            //copy pk
            for(String element:tableDetail.getPk()) {
                table.column(element, SQLDataType.INTEGER);
            }

            //copy fields
            for(Map.Entry<String, Object> element : tableDetail.getColumnsAndTypes().entrySet()) {

                table.column(element.getKey(), convertValue(element.getValue()));

            }

            //copy fks
            for(String element:tableDetail.getGraphFks()) {
                table.column(element, SQLDataType.INTEGER);
            }

            //setUpConstraints
            for(String element:tableDetail.getPk()) {
                ((CreateTableColumnStep) table).constraints(constraint(element.toUpperCase()).primaryKey(element));
            }

            for(String element:tableDetail.getGraphFks()) {
                ((CreateTableColumnStep) table).constraints(constraint(element.toUpperCase()).foreignKey(element).references(element,element));
            }

            String sql = ((CreateTableColumnStep) table).getSQL();
            System.out.println(sql);

        }


    }

}
