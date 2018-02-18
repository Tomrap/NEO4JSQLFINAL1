package com.dao;

import com.Domain.TableDetail;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Lazy
@Repository
public class Neo4JCreationDao {

    private HashFunction hf = Hashing.murmur3_128();

    @Autowired
    private BatchInserter batchInserter;

    private static final Logger logger = Logger.getLogger(Neo4JCreationDao.class);


    public Object convertValue(Object value) throws SQLException, IOException {
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        if (value instanceof Blob) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            IOUtils.copy(((Blob) value).getBinaryStream(), bo);
            return bo.toByteArray();
        }
        return value;
    }

    public void createNodes(final TableDetail tableDetail, List<Map<String, Object>> rs) throws SQLException, IOException {

        logger.info("Start generating nodes for " + tableDetail.getTableName());

        HashMap<Integer,Integer> mappingMap= new HashMap<>();

        List<String> fields = tableDetail.getFields();

        int currentIndex = tableDetail.getFirstIndex();

        for (Map row : rs) {

            handleOneRowForNode(tableDetail, mappingMap, fields, currentIndex, row);
            currentIndex++;
        }

        tableDetail.setMappingMap(mappingMap);

        logger.info("Finished generating nodes for " + tableDetail.getTableName());
    }

    private void handleOneRowForNode(TableDetail tableDetail, HashMap<Integer, Integer> mappingMap, List<String> fields, int currentIndex, Map row) throws SQLException, IOException {

        Map<String, Object> map = new HashMap<>();
        for (String field : fields) {
            Object object = row.get(field);
            if(object != null) {
                map.put(field,convertValue(object));
            }
        }

        Hasher hasher = hf.newHasher();
        for(String pk : tableDetail.getPk()) {
            hasher = hasher.putInt((Integer) row.get(pk));
        }

        mappingMap.put(hasher.hash().asInt(), currentIndex-tableDetail.getFirstIndex());

        batchInserter.createNode(currentIndex, map, tableDetail::getTableName);
    }


    public void createRelationships(TableDetail tableDetail, List<Map<String, Object>> rs) throws IOException, SQLException {

        logger.info("Start generating relationships for " + tableDetail.getTableName());

            int currentValue = 0;

            if (tableDetail.getFks().size() > 0) {

                for (Map<String, Object> row : rs) {

                    handleOneRowForRelationship(tableDetail, currentValue, row);
                    currentValue++;
                }
            }
        logger.info("Finished generating relationships for " + tableDetail.getTableName());
    }

    private void handleOneRowForRelationship(TableDetail tableDetail, int currentValue, Map<String, Object> row) {

        int primaryNodeIndex = tableDetail.getFirstIndex() + currentValue;

        outerloop:
        for (Map.Entry<List<String>, String> entry : tableDetail.getFks().entrySet()) {

            final String foreignTable = entry.getValue();
            TableDetail foreignTableDetail = TableDetail.getTable(foreignTable);
            Hasher hasherForeign = hf.newHasher();
            for (String string : entry.getKey()) {

                Integer value = (Integer)row.get(string);
                if(value == null) {
                    continue outerloop;
                }
                hasherForeign = hasherForeign.putInt(value);
            }
            int foreignNodeIndex = foreignTableDetail.getFirstIndex() + foreignTableDetail.getMappingMap().get(hasherForeign.hash().asInt());
            batchInserter.createRelationship(primaryNodeIndex,foreignNodeIndex, foreignTable.concat("::").concat(tableDetail.getTableName())::toUpperCase,null);
        }
    }

}