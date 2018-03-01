package com.SQLToGraph.Service;

import com.SQLToGraph.Domain.SQLtoGraphTableDetail;
import com.SQLToGraph.Dao.SQLImportDao;
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
public class SQLSchemaReader {

    @Autowired
    private SQLImportDao SQLImportDao;

    public List<SQLtoGraphTableDetail> extractSchema() throws SQLException, SchemaCrawlerException {

        final String schemaName = SQLtoGraphTableDetail.schemaName;

        ArrayList<SQLtoGraphTableDetail> tableList = new ArrayList<SQLtoGraphTableDetail>();

        final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        options.setSchemaInfoLevel(SchemaInfoLevel.standard());

        options.setSchemaInclusionRule((InclusionRule) schemaName::equals);

        final Database database = SchemaCrawlerUtility.getDatabase(SQLImportDao.getJdbcTemplate().getDataSource().getConnection(), options);

        for (final Table table : database.getTables()) {

            if(table.getTableType().equals(TableType.TABLE)) {
                SQLtoGraphTableDetail SQLtoGraphTableDetail = new SQLtoGraphTableDetail();

                String tableName = table.getName();
                SQLtoGraphTableDetail.setTableName(tableName);
                SQLtoGraphTableDetail.setPk(getPrimaryKeys(table));
                SQLtoGraphTableDetail.setFks(getForeignKeys(table));
                SQLtoGraphTableDetail.setFields(getColumns(table, SQLtoGraphTableDetail));

                tableList.add(SQLtoGraphTableDetail);

                SQLtoGraphTableDetail.addtoTables(SQLtoGraphTableDetail);
            }
        }
        return tableList;
    }

    private List<String> getColumns(Table table, SQLtoGraphTableDetail SQLtoGraphTableDetail) {

        List<Column> columns = table.getColumns();
        Collection<String> fields = new ArrayList<>(columns.size());

        for (final Column column : columns) {
            String columnName = column.getName();
            fields.add(columnName);
        }

        fields.removeAll(SQLtoGraphTableDetail.getForeignKeyColumns());
        fields.removeAll(SQLtoGraphTableDetail.getPk());

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
