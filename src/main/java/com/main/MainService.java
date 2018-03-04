package com.main;

import com.GraphToSQL.Domain.GraphDetail;
import com.GraphToSQL.Domain.GraphToSQLTableDetail;
import com.SQLToGraph.Domain.SQLtoGraphTableDetail;
import com.GraphToSQL.Domain.TableRow;
import com.GraphToSQL.Service.GraphToSQLRowConverter;
import com.GraphToSQL.Service.SQLRowToSQLConverter;
import com.GraphToSQL.Service.SQLSchemaCreator;
import com.SQLToGraph.Service.GraphGenerator;
import com.SQLToGraph.Service.SQLService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import schemacrawler.schemacrawler.SchemaCrawlerException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-10-20.
 */
@Service
public class MainService {

    private static final Logger logger = Logger.getLogger(MainService.class);

    @Autowired
    private GraphGenerator graphGenerator;

    @Autowired
    private SQLService sqlService;

    @Autowired
    private GraphToSQLRowConverter graphToSQLRowConverter;

    @Autowired
    private com.SQLToGraph.Service.SQLSchemaReader SQLSchemaReader;

    @Autowired
    private SQLSchemaCreator sqlSchemaCreator;

    @Autowired
    private SQLRowToSQLConverter SQLRowToSqlConverter;

    public void convertSQLtoNEO4J() throws SQLException, SchemaCrawlerException, IOException {

        List<SQLtoGraphTableDetail> tables = SQLSchemaReader.extractSchema();
        List<List<Map<String, Object>>> allData = sqlService.readAllTables(tables);
        graphGenerator.generate(allData,tables);
    }

    public void convertNEO4JtoSQL() throws SQLException, IOException {

        GraphDetail graphDetail = graphToSQLRowConverter.read();
        List<GraphToSQLTableDetail> schema = sqlSchemaCreator.createSchema(graphDetail);
        SQLRowToSqlConverter.createSQLTables(schema);
        Map<String, Map<Integer, TableRow>> allRows = graphToSQLRowConverter.convertGraphDetailsToTableRows(graphDetail);
        SQLRowToSqlConverter.createAndInsertSQLRows(allRows);
        SQLRowToSqlConverter.createForeignKeysConstraints(schema);

    }

}
