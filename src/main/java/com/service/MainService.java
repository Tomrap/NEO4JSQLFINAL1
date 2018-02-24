package com.service;

import com.Domain.GraphDetail;
import com.Domain.TableDetail;
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
    private GraphReader graphReader;

    @Autowired
    private SQLSchemaReader SQLSchemaReader;

    @Autowired
    private SQLSchemaCreator sqlSchemaCreator;

    @Autowired
    private SQLSchemaConverter sqlSchemaConverter;

    public void convertSQLtoNEO4J() throws SQLException, SchemaCrawlerException, IOException {

        List<TableDetail> tables = SQLSchemaReader.extractSchema();
        List<List<Map<String, Object>>> allData = sqlService.readAllTables(tables);
        graphGenerator.generate(allData,tables);
    }

    public void convertNEO4JtoSQL() throws SQLException {

        GraphDetail graphDetail = graphReader.read();
        List<TableDetail> schema = sqlSchemaCreator.createSchema(graphDetail);
        sqlSchemaConverter.createSQLSchema(schema);


    }

    public void schemaCreation() {

        System.out.println("Yes");
    }

}
