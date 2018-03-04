package com.GraphToSQL.Service;

import com.GraphToSQL.Dao.GraphReaderDao;
import com.GraphToSQL.Domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by John on 2018-02-18.
 */
@Service
public class GraphToSQLRowConverter {

    @Autowired
    @Lazy
    private GraphReaderDao graphReaderDao;

    public GraphDetail read() {

        return graphReaderDao.readGraph();
    }

    public Map<String, Map<Integer, TableRow>> convertGraphDetailsToTableRows(GraphDetail graphDetail) {
        graphDetail.assignSQLPrimaryKeysToNodes();
        Map<String, Map<Integer, TableRow>> allRows = initializeAllOriginalTables(graphDetail.getAllMyNodes());

        Map<String, Map<Long, MyNode>> allMyNodes = graphDetail.getAllMyNodes();
        Map<MyRelationshipType, List<MyRelationship>> allMyRelationships = graphDetail.getAllMyRelationships();
        for (Map.Entry<MyRelationshipType, List<MyRelationship>> element : allMyRelationships.entrySet()) {

            createRowWithProperForeignKeys(allRows, allMyNodes, element);
        }
        assignSQLPrimaryKeysToRemainingTables(allRows);
        return allRows;
    }

    private void createRowWithProperForeignKeys(Map<String, Map<Integer, TableRow>> allRows, Map<String, Map<Long, MyNode>> allMyNodes, Map.Entry<MyRelationshipType, List<MyRelationship>> element) {
        MyRelationshipType key = element.getKey();
        if (key.isFirstNodeForeignKey() && key.isSecondNodeForeignKey()) {
            for (MyRelationship myRelationship : element.getValue()) {
                MyNode firstNode = allMyNodes.get(key.getFirstNodeLabel()).get(myRelationship.getFirstNode());
                MyNode secondNode = allMyNodes.get(key.getSecondNodeLabel()).get(myRelationship.getSecondNode());
                TableRow tableRow;
                tableRow =  assignForeignKeysInJunctionTable(key, firstNode, secondNode,myRelationship);
                allRows.computeIfAbsent(key.getLabel(), k -> new HashMap<>()).put(tableRow.hashCode(), tableRow);
            }
        }
        else if (key.isFirstNodeForeignKey()) {
            for (MyRelationship myRelationship : element.getValue()) {
                MyNode firstNode = allMyNodes.get(key.getFirstNodeLabel()).get(myRelationship.getFirstNode());
                MyNode secondNode = allMyNodes.get(key.getSecondNodeLabel()).get(myRelationship.getSecondNode());
                createRowWithOneForeignKey(allRows,key.getSecondNodeLabel(), secondNode,firstNode,myRelationship,key);
            }
        } else {
            for (MyRelationship myRelationship : element.getValue()) {
                MyNode firstNode = allMyNodes.get(key.getFirstNodeLabel()).get(myRelationship.getFirstNode());
                MyNode secondNode = allMyNodes.get(key.getSecondNodeLabel()).get(myRelationship.getSecondNode());
                createRowWithOneForeignKey(allRows,key.getFirstNodeLabel(), firstNode,secondNode,myRelationship,key);
            }
        }

    }

    private void assignSQLPrimaryKeysToRemainingTables(Map<String, Map<Integer, TableRow>> allRows) {
        int count;
        for (Map.Entry<String, Map<Integer, TableRow>> element : allRows.entrySet()) {
            count = 1;
            for (TableRow tableRow : element.getValue().values()) {
                tableRow.setSQLID(count);
                count++;
            }
        }
    }

    private TableRow assignForeignKeysInJunctionTable(MyRelationshipType key, MyNode firstNode, MyNode secondNode, MyRelationship myRelationship) {
        TableRow tableRow;
        tableRow = new TableRow();
        Map<String, Integer> foreignKeys = new HashMap<>();
        foreignKeys.put(key.getFirstNodeLabel(), firstNode.getSqlID());
        foreignKeys.put(key.getSecondNodeLabel(), secondNode.getSqlID());
        tableRow.setForeignKeys(foreignKeys);
        tableRow.setRelationshipProperties(myRelationship.getValues());
        return tableRow;
    }

    private void createRowWithOneForeignKey(Map<String, Map<Integer, TableRow>> allRows, String firstNodeLabel, MyNode firstNode, MyNode secondNode, MyRelationship myRelationship, MyRelationshipType key) {
        TableRow tableRow = allRows.get(firstNodeLabel).get(firstNode.getSqlID());
        tableRow.getForeignKeys().put(key.getLabel(), secondNode.getSqlID());
        Map<String, Object> relationshipProperties = tableRow.getRelationshipProperties();
        relationshipProperties.putAll(myRelationship.getValues());
        tableRow.setRelationshipProperties(relationshipProperties);
        allRows.computeIfAbsent(firstNodeLabel, k -> new HashMap<>()).put(firstNode.getSqlID(), tableRow);
    }

    private Map<String, Map<Integer, TableRow>> initializeAllOriginalTables(Map<String, Map<Long, MyNode>> nodesWithoutAnyForeignKeys) {

        Map<String, Map<Integer, TableRow>> allRows = new HashMap<>();
        for (Map.Entry<String, Map<Long, MyNode>> element : nodesWithoutAnyForeignKeys.entrySet()) {
            for (MyNode myNode : element.getValue().values()) {
                TableRow tableRow = new TableRow();
                tableRow.setMyNode(myNode);
                allRows.computeIfAbsent(element.getKey(), k -> new HashMap<>()).put(myNode.getSqlID(), tableRow);
            }
        }
        return allRows;
    }
}