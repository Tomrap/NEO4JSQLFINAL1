package com.service;

import com.Domain.TableDetail;
import com.dao.Dao;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import schemacrawler.schema.Database;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.InclusionRule;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevel;
import schemacrawler.utility.SchemaCrawlerUtility;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2017-10-20.
 */
@Service
public class RDBReader {

    @Autowired
    private Dao dao;

    public  List<TableDetail> extractTables() throws SQLException, SchemaCrawlerException {

        final String schemaName = "sakila";

        ArrayList<TableDetail> tableList = new ArrayList<TableDetail>();

        final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
        options.setSchemaInfoLevel(SchemaInfoLevel.standard());

        options.setSchemaInclusionRule(new InclusionRule() {
            @Override
            public boolean include(String s) {
                return schemaName.equals(s);
            }
        });

        final Database database = SchemaCrawlerUtility.getDatabase(dao.getJdbcTemplate().getDataSource().getConnection(),options);

        List<Table> tables = (List<Table>) database.getTables();

        System.out.println(tables);

        return tableList;
    }
}
