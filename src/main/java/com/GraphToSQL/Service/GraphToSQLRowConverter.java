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

    private String join = "_";

    @Autowired
    @Lazy
    private GraphReaderDao graphReaderDao;

    public GraphDetail read() {

        return graphReaderDao.readGraph();
    }

    public Map<String, Map<Integer, TableRow>> convertGraphDetailsToTableRows(GraphDetail graphDetail, List<GraphToSQLTableDetail> schema) {
        graphDetail.assignSQLPrimaryKeysToNodes();
        Map<String, Map<Long, MyNode>> nodesWithoutAnyForeignKeys = getNodesWithoutAnyForeignKeys(schema,graphDetail.getAllMyNodes());
        Map<String, Map<Integer, TableRow>> allRows = createRowsWithoutAnyForeignKeys(nodesWithoutAnyForeignKeys);

        Map<String, Map<Long, MyNode>> allMyNodes = graphDetail.getAllMyNodes();
        Map<MyRelationshipType, List<MyRelationship>> allMyRelationships = graphDetail.getAllMyRelationships();
        for (Map.Entry<MyRelationshipType, List<MyRelationship>> element : allMyRelationships.entrySet()) {

            createRowWithProperForeignKeys(allRows, allMyNodes, element);
        }
        return allRows;
    }

    private void createRowWithProperForeignKeys(Map<String, Map<Integer, TableRow>> allRows, Map<String, Map<Long, MyNode>> allMyNodes, Map.Entry<MyRelationshipType, List<MyRelationship>> element) {
        MyRelationshipType key = element.getKey();
        for (MyRelationship myRelationship : element.getValue()) {

            MyNode firstNode = allMyNodes.get(key.getFirstNodeLabel()).get(myRelationship.getFirstNode());
            MyNode secondNode = allMyNodes.get(key.getSecondNodeLabel()).get(myRelationship.getSecondNode());

            if (key.isFirstNodeForeignKey() && key.isSecondNodeForeignKey()) {
                createRowWithTwoForeignKeys(allRows, key, firstNode, secondNode,myRelationship);
            } else if (key.isFirstNodeForeignKey()) {
                createRowWithOneForeignKey(allRows,key.getSecondNodeLabel(),key.getFirstNodeLabel(),secondNode,firstNode,myRelationship,key);
            } else {
                createRowWithOneForeignKey(allRows,key.getFirstNodeLabel(),key.getSecondNodeLabel(),firstNode,secondNode,myRelationship,key);
            }
        }
    }

    private void createRowWithTwoForeignKeys(Map<String, Map<Integer, TableRow>> allRows, MyRelationshipType key, MyNode firstNode, MyNode secondNode, MyRelationship myRelationship) {

        TableRow tableRow;
        if (allRows.get(key.getFirstNodeLabel() + join + key.getSecondNodeLabel()) != null) {
            tableRow = allRows.get(key.getFirstNodeLabel() + join + key.getSecondNodeLabel()).get(firstNode.getSqlID() + secondNode.getSqlID());
            if (tableRow == null) {
                tableRow = assignForeignKeysInJunctionTable(key, firstNode, secondNode,myRelationship);
            } else {
                Map<String, Integer> foreignKeys = tableRow.getForeignKeys();
                foreignKeys.put(key.getLabel()+"_1", firstNode.getSqlID());
                foreignKeys.put(key.getLabel()+"_2", secondNode.getSqlID());
                Map<String, Object> relationshipProperties = tableRow.getRelationshipProperties();
                relationshipProperties.putAll(myRelationship.getValues());
                tableRow.setRelationshipProperties(relationshipProperties);
            }
        } else {
            tableRow = assignForeignKeysInJunctionTable(key, firstNode, secondNode,myRelationship);
        }
        allRows.computeIfAbsent(key.getFirstNodeLabel() + join + key.getSecondNodeLabel(), k -> new HashMap<>()).put(firstNode.getSqlID() + secondNode.getSqlID(), tableRow);
    }

    private TableRow assignForeignKeysInJunctionTable(MyRelationshipType key, MyNode firstNode, MyNode secondNode, MyRelationship myRelationship) {
        TableRow tableRow;
        tableRow = new TableRow();
        Map<String, Integer> foreignKeys = new HashMap<>();
        foreignKeys.put(key.getLabel()+"_1", firstNode.getSqlID());
        foreignKeys.put(key.getLabel()+"_2", secondNode.getSqlID());
        tableRow.setForeignKeys(foreignKeys);
        tableRow.setRelationshipProperties(myRelationship.getValues());
        return tableRow;
    }

    private void createRowWithOneForeignKey(Map<String, Map<Integer, TableRow>> allRows, String firstNodeLabel, String secondNodeLabel, MyNode firstNode, MyNode secondNode, MyRelationship myRelationship, MyRelationshipType key) {
        TableRow tableRow;
        if (allRows.get(firstNodeLabel) != null) {
            tableRow = allRows.get(firstNodeLabel).get(firstNode.getSqlID());
            if (tableRow == null) {
                tableRow = createRowWithGivenForeignKey(firstNode,secondNode,secondNodeLabel,myRelationship,key);
            } else {
                tableRow.getForeignKeys().put(key.getLabel(), secondNode.getSqlID());
                Map<String, Object> relationshipProperties = tableRow.getRelationshipProperties();
                relationshipProperties.putAll(myRelationship.getValues());
                tableRow.setRelationshipProperties(relationshipProperties);
            }
        } else {
            tableRow = createRowWithGivenForeignKey(firstNode,secondNode,secondNodeLabel,myRelationship,key);
        }
        allRows.computeIfAbsent(firstNodeLabel, k -> new HashMap<>()).put(firstNode.getSqlID(), tableRow);
    }

    private TableRow createRowWithGivenForeignKey(MyNode firstNode, MyNode secondNode, String foreignKeyName, MyRelationship myRelationship, MyRelationshipType key) {
        TableRow tableRow = new TableRow();
        tableRow.setMyNode(firstNode);
        Map<String, Integer> foreignKeys = new HashMap<>();
        foreignKeys.put(key.getLabel(), secondNode.getSqlID());
        tableRow.setForeignKeys(foreignKeys);
        tableRow.setRelationshipProperties(myRelationship.getValues());
        return tableRow;
    }

    private Map<String, Map<Integer, TableRow>> createRowsWithoutAnyForeignKeys(Map<String, Map<Long, MyNode>> nodesWithoutAnyForeignKeys) {

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

    private Map<String, Map<Long, MyNode>> getNodesWithoutAnyForeignKeys(List<GraphToSQLTableDetail> list, Map<String,Map<Long,MyNode>> allMyNodes) {

        Map<String, Map<Long, MyNode>> nodes = new HashMap<>();
        for(Map.Entry<String, Map<Long, MyNode>> element: allMyNodes.entrySet()) {
            Optional<GraphToSQLTableDetail> first = list.stream().filter(o -> o.getTableName().equals(element.getKey())).findFirst();
            GraphToSQLTableDetail SQLtoGraphTableDetail = first.get();
            if(SQLtoGraphTableDetail.getGraphFks().size() == 0) {
                nodes.put(element.getKey(),element.getValue());
            }
        }
        return nodes;
    }
}