package com.SQLToGraph.Dao;

import com.SQLToGraph.Domain.SQLtoGraphTableDetail;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.apache.log4j.Logger;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Lazy
@Repository
public class GraphCreationDao {

    private HashFunction hf = Hashing.murmur3_128();

    @Autowired
    private BatchInserter batchInserter;

    private static final Logger logger = Logger.getLogger(GraphCreationDao.class);


    public Object convertValue(Object value) throws SQLException, IOException, ClassNotFoundException {
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
        if (value instanceof Blob) {

            //TODO convert to BLOB
            int blobLength = (int) ((Blob) value).length();
            byte[] blobAsBytes = ((Blob) value).getBytes(1, blobLength);
            ((Blob) value).free();
            ByteArrayInputStream bis = new ByteArrayInputStream(blobAsBytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object object = ois.readObject();
            ois.close();
            return object;

        }
        return value;
    }

    public void createNodes(final SQLtoGraphTableDetail sQLtoGraphTableDetail, List<Map<String, Object>> rs) throws SQLException, IOException, ClassNotFoundException {

        logger.info("Start generating nodes for " + sQLtoGraphTableDetail.getTableName());

        HashMap<Integer,Integer> mappingMap= new HashMap<>();

        List<String> fields = sQLtoGraphTableDetail.getFields();

        int currentIndex = sQLtoGraphTableDetail.getFirstIndex();

        for (Map row : rs) {

            handleOneRowForNode(sQLtoGraphTableDetail, mappingMap, fields, currentIndex, row);
            currentIndex++;
        }

        sQLtoGraphTableDetail.setMappingMap(mappingMap);

        logger.info("Finished generating nodes for " + sQLtoGraphTableDetail.getTableName());
    }

    private void handleOneRowForNode(SQLtoGraphTableDetail sQLtoGraphTableDetail, HashMap<Integer, Integer> mappingMap, List<String> fields, int currentIndex, Map row) throws SQLException, IOException, ClassNotFoundException {

        Map<String, Object> map = new HashMap<>();
        for (String field : fields) {
            Object object = row.get(field);
            if(object != null) {
                map.put(field,convertValue(object));
            }
        }

        Hasher hasher = hf.newHasher();
        for(String pk : sQLtoGraphTableDetail.getPk()) {
            hasher = hasher.putInt((Integer) row.get(pk));
        }

        mappingMap.put(hasher.hash().asInt(), currentIndex- sQLtoGraphTableDetail.getFirstIndex());

        batchInserter.createNode(currentIndex, map, sQLtoGraphTableDetail::getTableName);
    }


    public void createRelationships(SQLtoGraphTableDetail sQLtoGraphTableDetail, List<Map<String, Object>> rs) throws IOException, SQLException, ClassNotFoundException {

        logger.info("Start generating relationships for " + sQLtoGraphTableDetail.getTableName());

        if(sQLtoGraphTableDetail.isJunctionTable()) {
            for (Map<String, Object> row : rs) {
                List<String> fields = sQLtoGraphTableDetail.getFields();
                handleOneRowForRelationshipInJunctionTable(sQLtoGraphTableDetail, row, fields);
            }
        } else {
            int currentValue = 0;
            if (sQLtoGraphTableDetail.getFks().size() > 0) {

                for (Map<String, Object> row : rs) {
                    handleOneRowForRelationship(sQLtoGraphTableDetail, currentValue, row);
                    currentValue++;
                }
            }
        }
        logger.info("Finished generating relationships for " + sQLtoGraphTableDetail.getTableName());
    }

    private void handleOneRowForRelationshipInJunctionTable(SQLtoGraphTableDetail sQLtoGraphTableDetail, Map<String, Object> row, List<String> fields) throws IOException, SQLException, ClassNotFoundException {

        int[] indexes = new int[2];
        int count = 0;

        for (Map.Entry<List<String>, String> entry : sQLtoGraphTableDetail.getFks().entrySet()) {

            final String foreignTable = entry.getValue();
            SQLtoGraphTableDetail foreignSQLtoGraphTableDetail = sQLtoGraphTableDetail.getTable(foreignTable);
            Hasher hasherForeign = hf.newHasher();
            for (String string : entry.getKey()) {
                Integer value = (Integer)row.get(string);
                hasherForeign = hasherForeign.putInt(value);
            }
            indexes[count] = foreignSQLtoGraphTableDetail.getMappingMap().get(hasherForeign.hash().asInt()) + foreignSQLtoGraphTableDetail.getFirstIndex();
            count++;
        }
        Map<String, Object> map = new HashMap<>();
        for (String field : fields) {
            Object object = row.get(field);
            if(object != null) {
                map.put(field,convertValue(object));
            }
        }

        batchInserter.createRelationship(indexes[1],indexes[0], sQLtoGraphTableDetail.getTableName()::toUpperCase,map);
    }

    private void handleOneRowForRelationship(SQLtoGraphTableDetail sQLtoGraphTableDetail, int currentValue, Map<String, Object> row) {

        int primaryNodeIndex = sQLtoGraphTableDetail.getFirstIndex() + currentValue;

        outerloop:
        for (Map.Entry<List<String>, String> entry : sQLtoGraphTableDetail.getFks().entrySet()) {

            final String foreignTable = entry.getValue();
            SQLtoGraphTableDetail foreignSQLtoGraphTableDetail = sQLtoGraphTableDetail.getTable(foreignTable);
            Hasher hasherForeign = hf.newHasher();
            for (String string : entry.getKey()) {

                Integer value = (Integer)row.get(string);
                if(value == null) {
                    continue outerloop;
                }
                hasherForeign = hasherForeign.putInt(value);
            }
            int foreignNodeIndex = foreignSQLtoGraphTableDetail.getFirstIndex() + foreignSQLtoGraphTableDetail.getMappingMap().get(hasherForeign.hash().asInt());
            batchInserter.createRelationship(primaryNodeIndex,foreignNodeIndex,
                    sQLtoGraphTableDetail.getTableName().concat("::").concat(foreignTable.concat(entry.getKey().stream().map(x -> x).collect(Collectors.joining())))::toUpperCase,null);
        }
    }

}