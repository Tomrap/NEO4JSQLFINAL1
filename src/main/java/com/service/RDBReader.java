package com.service;

import com.Domain.TableDetail;
import com.dao.RelationalDao;
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
    private RelationalDao relationalDao;

    public List<TableDetail> extractTables() throws SQLException, SchemaCrawlerException {

        final String schemaName = TableDetail.schemaName;

        ArrayList<TableDetail> tableList = new ArrayList<TableDetail>();

        final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        options.setSchemaInfoLevel(SchemaInfoLevel.standard());

        options.setSchemaInclusionRule((InclusionRule) schemaName::equals);

        final Database database = SchemaCrawlerUtility.getDatabase(relationalDao.getJdbcTemplate().getDataSource().getConnection(), options);

        for (final Table table : database.getTables()) {

            TableDetail tableDetail = new TableDetail();

            String tableName = table.getName();
            tableDetail.setTableName(tableName);
            tableDetail.setPk(getPrimaryKeys(table));
            tableDetail.setFks(getForeignKeys(table));
            tableDetail.setFields(getColumns(table,tableDetail));

            tableList.add(tableDetail);

            TableDetail.addtoTables(tableDetail);

        }
        return tableList;
    }


    private List<String> getColumns(Table table, TableDetail tableDetail) {

        List<Column> columns = table.getColumns();
        Collection<String> fields = new ArrayList<>(columns.size());

        //all
        for (final Column column : columns) {
            String columnName = column.getName();
            fields.add(columnName);
        }
        //remove foreign keys
        fields.removeAll(tableDetail.getForeignKeyColumns());

        return new ArrayList<>(fields);
    }

    private Map<List<String>, String> getForeignKeys(Table table) {
        Collection<ForeignKey> foreignKeys = table.getForeignKeys();
        Map<List<String>, String> fks = new LinkedHashMap<>(10);

        if (foreignKeys != null) {

            for (ForeignKey foreignKey : foreignKeys) {

                List<ForeignKeyColumnReference> columnReferences = foreignKey.getColumnReferences();
                if (columnReferences.isEmpty()) continue;
                ForeignKeyColumnReference firstReference = columnReferences.get(0);
                String otherTableName = firstReference.getPrimaryKeyColumn().getParent().getName();
                List<String> keys = new ArrayList<>(3);
                for (ForeignKeyColumnReference reference : columnReferences) {
                    Table otherTable = reference.getPrimaryKeyColumn().getParent();
                    Table thisTable = reference.getForeignKeyColumn().getParent();
                    if (otherTable.equals(table) && !thisTable.equals(table)) continue;
                    keys.add(reference.getForeignKeyColumn().getName());
                }
                if (!keys.isEmpty()) fks.put(keys, otherTableName);
                //if (otherTable.equals(table) && !thisTable.equals(table))  it means that we have primary key not foreign key because referenced table is our table,if you look at MySQL you will see that info about every foreign key is
                //stored twice, once in actual table that contains column for it and once in a table that has it as primary key, obviously we only want to extract this information once, we do this from the table that actually have it as column
                //if (otherTable.equals(table) && thisTable.equals(table)) it means we have self referential foreign key
            }
        }
        return fks;
    }

    private List<String> getPrimaryKeys(Table table) {
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
