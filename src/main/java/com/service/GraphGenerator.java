package com.service;

import com.Domain.TableDetail;
import com.dao.Neo4JCreationDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-10-23.
 */
@Service
public class GraphGenerator {

    private static final Logger logger = Logger.getLogger(GraphGenerator.class);

    @Autowired @Lazy
    private Neo4JCreationDao neo4JCreationDao;

    public void generate(List<List<Map<String, Object>>> allData, List<TableDetail> tableDetailList) throws SQLException, IOException {

        logger.info("Start generating graph database");

        generateNodes(tableDetailList,allData);
        generateRelationships(tableDetailList,allData);
    }

    private void generateNodes(List<TableDetail> tableDetailList, List<List<Map<String, Object>>> allData) throws SQLException, IOException {

        logger.info("Start generating nodes");

        Iterator<TableDetail> it1 = tableDetailList.iterator();
        Iterator<List<Map<String, Object>>> it2;
        it2 = allData.iterator();
        int firstIndex = 0;

        while (it1.hasNext() && it2.hasNext()) {

            TableDetail tableDetail = it1.next();
            List<Map<String, Object>> row = it2.next();

            tableDetail.setFirstIndex(firstIndex);
            firstIndex += row.size();

            neo4JCreationDao.createNodes(tableDetail, row);
        }

        logger.info("Finished generating nodes");
    }

    private void generateRelationships(List<TableDetail> tableDetailList, List<List<Map<String, Object>>> allData) throws SQLException, IOException {

        logger.info("Start generating relationships for database");

        Iterator<TableDetail> it1 = tableDetailList.iterator();
        Iterator<List<Map<String, Object>>> it2;
        it2 = allData.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            TableDetail tableDetail = it1.next();
            List<Map<String, Object>> row = it2.next();
            neo4JCreationDao.createRelationships(tableDetail, row);
        }
        logger.info("Finished generating relationships for database");
    }
}
