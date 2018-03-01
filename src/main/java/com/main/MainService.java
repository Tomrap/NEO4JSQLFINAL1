package com.main;

import com.GraphToSQL.Domain.GraphDetail;
import com.GraphToSQL.Domain.GraphToSQLTableDetail;
import com.SQLToGraph.Domain.SQLtoGraphTableDetail;
import com.GraphToSQL.Domain.TableRow;
import com.GraphToSQL.Service.GraphToSQLConverter;
import com.GraphToSQL.Service.SQLConverter;
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
    private GraphToSQLConverter graphToSQLConverter;

    @Autowired
    private com.SQLToGraph.Service.SQLSchemaReader SQLSchemaReader;

    @Autowired
    private SQLSchemaCreator sqlSchemaCreator;

    @Autowired
    private SQLConverter sqlConverter;

    public void convertSQLtoNEO4J() throws SQLException, SchemaCrawlerException, IOException {

        List<SQLtoGraphTableDetail> tables = SQLSchemaReader.extractSchema();
        List<List<Map<String, Object>>> allData = sqlService.readAllTables(tables);
        graphGenerator.generate(allData,tables);
    }

    public void convertNEO4JtoSQL() throws SQLException, IOException {

        GraphDetail graphDetail = graphToSQLConverter.read();
        List<GraphToSQLTableDetail> schema = sqlSchemaCreator.createSchema(graphDetail);
        sqlConverter.createSQLSchema(schema);
        Map<String, Map<Integer, TableRow>> allRows = graphToSQLConverter.convertGraphDetailsToTableRows(graphDetail, schema);
        sqlConverter.createSQLRows(allRows);
        sqlConverter.createFOreignKeysConstraints(schema);
//        sqlConverter.executeAnddestroyScripts();

    }

    public void schemaCreation() {

        System.out.println("Yes");
    }

}
