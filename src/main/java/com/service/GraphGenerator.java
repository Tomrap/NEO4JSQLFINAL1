package com.service;

import com.Domain.TableDetail;
import com.dao.Neo4JDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by John on 2017-10-23.
 */
@Service
public class GraphGenerator {


    @Autowired
    private Neo4JDao neo4JDao;

    public void generate(List<List<Map<String, Object>>> allData, List<TableDetail> tableDetailList) throws SQLException {

        generateNodesAndIndices(tableDetailList,allData);
        generateRelationships(tableDetailList,allData);
        neo4JDao.deletePrimaryKeysAndIndexes();
    }

    public void generateNodesAndIndices(List<TableDetail> tableDetailList, List<List<Map<String, Object>>> allData) throws SQLException {

        //todo junction table

        Iterator<TableDetail> it1 = tableDetailList.iterator();
        Iterator<List<Map<String, Object>>> it2;
        it2 = allData.iterator();

        while (it1.hasNext() && it2.hasNext()) {

            TableDetail tableDetail = it1.next();
            List<Map<String, Object>> row = it2.next();
            neo4JDao.createNodes(tableDetail, row);
            neo4JDao.createIndices(tableDetail);
        }
    }

    public void generateRelationships(List<TableDetail> tableDetailList, List<List<Map<String, Object>>> allData) throws SQLException {

        Iterator<TableDetail> it1 = tableDetailList.iterator();
        Iterator<List<Map<String, Object>>> it2;
        it2 = allData.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            TableDetail tableDetail = it1.next();
            List<Map<String, Object>> row = it2.next();
            neo4JDao.createRelationships(tableDetail, row);
        }
    }
}
