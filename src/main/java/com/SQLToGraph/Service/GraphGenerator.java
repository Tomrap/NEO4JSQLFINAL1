package com.SQLToGraph.Service;

import com.SQLToGraph.Dao.GraphCreationDao;
import com.SQLToGraph.Domain.SQLtoGraphTableDetail;
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

    @Autowired
    @Lazy
    private GraphCreationDao graphCreationDao;

    public void generate(List<List<Map<String, Object>>> allData, List<SQLtoGraphTableDetail> sQLtoGraphTableDetailList) throws SQLException, IOException, ClassNotFoundException {

        logger.info("Start generating graph database");

        generateNodes(sQLtoGraphTableDetailList, allData);
        generateRelationships(sQLtoGraphTableDetailList, allData);
    }

    private void generateNodes(List<SQLtoGraphTableDetail> sQLtoGraphTableDetailList, List<List<Map<String, Object>>> allData) throws SQLException, IOException, ClassNotFoundException {

        logger.info("Start generating nodes");

        Iterator<SQLtoGraphTableDetail> it1 = sQLtoGraphTableDetailList.iterator();
        Iterator<List<Map<String, Object>>> it2;
        it2 = allData.iterator();
        int firstIndex = 0;

        while (it1.hasNext() && it2.hasNext()) {

            SQLtoGraphTableDetail sQLtoGraphTableDetail = it1.next();
            List<Map<String, Object>> row = it2.next();
            if (!sQLtoGraphTableDetail.isJunctionTable()) {
                sQLtoGraphTableDetail.setFirstIndex(firstIndex);
                firstIndex += row.size();
                graphCreationDao.createNodes(sQLtoGraphTableDetail, row);
            }

        }
        logger.info("Finished generating nodes");
    }

    private void generateRelationships(List<SQLtoGraphTableDetail> sQLtoGraphTableDetailList, List<List<Map<String, Object>>> allData) throws SQLException, IOException, ClassNotFoundException {

        logger.info("Start generating relationships for database");

        Iterator<SQLtoGraphTableDetail> it1 = sQLtoGraphTableDetailList.iterator();
        Iterator<List<Map<String, Object>>> it2;
        it2 = allData.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            SQLtoGraphTableDetail sQLtoGraphTableDetail = it1.next();
            List<Map<String, Object>> row = it2.next();
            graphCreationDao.createRelationships(sQLtoGraphTableDetail, row);
        }
        logger.info("Finished generating relationships for database");
    }
}
