package com.service;

import com.Domain.TableDetail;
import com.dao.Dao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import schemacrawler.schema.*;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by John on 2017-10-20.
 */
@Service
public class RDBReader {

    @Autowired
    private Dao dao;

    public List<TableDetail> extractTables() throws SQLException, SchemaCrawlerException {

        final String schemaName = TableDetail.schemaName;

        ArrayList<TableDetail> tableList = new ArrayList<TableDetail>();

        final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        options.setSchemaInfoLevel(SchemaInfoLevel.standard());

        options.setSchemaInclusionRule(new InclusionRule() {
            @Override
            public boolean include(String s) {
                return schemaName.equals(s);
            }
        });

        final Database database = SchemaCrawlerUtility.getDatabase(dao.getJdbcTemplate().getDataSource().getConnection(), options);


        for (final Table table : database.getTables()) {
            String tableName = table.getName();

            List<Column> columns = table.getColumns();

            List<String> fields = new ArrayList<>(columns.size());

            for (final Column column : columns) {
//                    System.out.println("     o--> " + column + " pk: "+ column.isPartOfPrimaryKey() + " fk: " + column.isPartOfForeignKey());
                String columnName = column.getName();
                fields.add(columnName);
            }

            TableDetail tableDetail = new TableDetail(tableName, getPriamryKeys(table), fields, getForeignKeys(table));
            tableList.add(tableDetail);
        }
        return tableList;
    }

    private Map<String, String> getForeignKeys(Table table) {
        Collection<ForeignKey> foreignKeys = table.getForeignKeys();
        Map<String, String> fks = fks = new LinkedHashMap<>(10);

        if (foreignKeys != null) {

            for (ForeignKey foreignKey : foreignKeys) {
                List<ForeignKeyColumnReference> columnReferences = foreignKey.getColumnReferences();
                if (columnReferences.isEmpty()) continue;
                ForeignKeyColumnReference reference = columnReferences.get(0);
                String otherTableName = reference.getPrimaryKeyColumn().getParent().getName();
                Table otherTable = reference.getPrimaryKeyColumn().getParent();
                Table thisTable = reference.getForeignKeyColumn().getParent();
                if (otherTable.equals(table) && !thisTable.equals(table)) continue;
                //if (otherTable.equals(table) && !thisTable.equals(table))  it means that we have primary key not foreign key because referenced table is our table,if you look at MySQL you will see that info about every foreign key is
                //stored twice, once in actual table that contains column for it and once in a table that has it as primary key, obviously we only want to extract this information once, we do this from the table that actually have it as column
                //if (otherTable.equals(table) && thisTable.equals(table)) it means we have self referential foreign key
                fks.put(reference.getForeignKeyColumn().getName(), otherTableName);
            }
        }
        return fks;
    }

    private List<String> getPriamryKeys(Table table) {
        List<String> pks = new ArrayList<>();

        if (table.getPrimaryKey() != null) {
            List<IndexColumn> pkColumns = table.getPrimaryKey().getColumns();

            for (IndexColumn pkColumn : pkColumns) {
                String pkName = pkColumn.getName();
                pks.add(pkName);
            }
        }

        return pks;
    }
}
