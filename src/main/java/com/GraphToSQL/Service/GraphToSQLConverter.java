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
public class GraphToSQLConverter {

    @Autowired
    @Lazy
    private GraphReaderDao graphReaderDao;

    public GraphDetail read() {

        return graphReaderDao.readGraph();
    }


    public Map<String, Map<Integer, TableRow>> convertGraphDetailsToTableRows(GraphDetail graphDetail, List<GraphToSQLTableDetail> schema) {
        Map<String, Map<Integer, TableRow>> allRows = new HashMap<>();
        graphDetail.assignSQLPrimaryKeysToNodes();
        Map<String, Map<Long, MyNode>> nodesWithoutAnyForeignKeys = getNodesWithoutAnyForeignKeys(schema,graphDetail.getAllMyNodes());

        for (Map.Entry<String, Map<Long, MyNode>> element : nodesWithoutAnyForeignKeys.entrySet()) {

            for (MyNode myNode : element.getValue().values()) {
                TableRow tableRow = new TableRow();
                tableRow.setMyNode(myNode);
                allRows.computeIfAbsent(element.getKey(), k -> new HashMap<>()).put(myNode.getSqlID(), tableRow);
            }
        }

        Map<String, Map<Long, MyNode>> allMyNodes = graphDetail.getAllMyNodes();
        Map<MyRelationshipType, List<MyRelationship>> allMyRelationships = graphDetail.getAllMyRelationships();
        for (Map.Entry<MyRelationshipType, List<MyRelationship>> element : allMyRelationships.entrySet()) {

            MyRelationshipType key = element.getKey();
            for (MyRelationship myRelationship : element.getValue()) {

                //TODO handle relationship properties later

                MyNode firstNode = allMyNodes.get(key.getFirstNodeLabel()).get(myRelationship.getFirstNode());
                MyNode secondNode = allMyNodes.get(key.getSecondNodeLabel()).get(myRelationship.getSecondNode());

                //3 scenarios
                //new table
                if (key.isFirstNodeForeignKey() && key.isSecondNodeForeignKey()) {
                    //TODO do sth about the names
                    TableRow tableRow;
                    if (allRows.get(key.getFirstNodeLabel() + "_" + key.getSecondNodeLabel()) != null) {
                        tableRow = allRows.get(key.getFirstNodeLabel() + "_" + key.getSecondNodeLabel()).get(firstNode.getSqlID() + secondNode.getSqlID());
                        if (tableRow == null) {
                            tableRow = getTableRow(key, firstNode, secondNode);
                        } else {
                            Map<String, Integer> foreignKeys = tableRow.getForeignKeys();
                            foreignKeys.put(key.getFirstNodeLabel(), firstNode.getSqlID());
                            foreignKeys.put(key.getSecondNodeLabel(), secondNode.getSqlID());
                        }
                    } else {
                        tableRow = getTableRow(key, firstNode, secondNode);
                    }
                    //TODO introduce composite primary key
                    allRows.computeIfAbsent(key.getFirstNodeLabel() + "_" + key.getSecondNodeLabel(), k -> new HashMap<>()).put(firstNode.getSqlID() + secondNode.getSqlID(), tableRow);
                } else if (key.isFirstNodeForeignKey()) {
                    calculateForeignKeys(allRows,key.getSecondNodeLabel(),key.getFirstNodeLabel(),secondNode,firstNode);
                } else {
                    calculateForeignKeys(allRows,key.getFirstNodeLabel(),key.getSecondNodeLabel(),firstNode,secondNode);
                }
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

    private void calculateForeignKeys(Map<String, Map<Integer, TableRow>> allRows, String firstNodeLabel, String secondNodeLabel, MyNode firstNode, MyNode secondNode  ) {
        TableRow tableRow;
        if (allRows.get(firstNodeLabel) != null) {
            tableRow = allRows.get(firstNodeLabel).get(firstNode.getSqlID());
            if (tableRow == null) {
                tableRow = helper(firstNode,secondNode,secondNodeLabel);
            } else {
                tableRow.getForeignKeys().put(secondNodeLabel, secondNode.getSqlID());
            }
        } else {
            tableRow = helper(firstNode,secondNode,secondNodeLabel);
        }
        allRows.computeIfAbsent(firstNodeLabel, k -> new HashMap<>()).put(firstNode.getSqlID(), tableRow);
    }

    private TableRow getTableRow(MyRelationshipType key, MyNode firstNode, MyNode secondNode) {
        TableRow tableRow;
        tableRow = new TableRow();
        Map<String, Integer> foreignKeys = new HashMap<>();
        foreignKeys.put(key.getFirstNodeLabel(), firstNode.getSqlID());
        foreignKeys.put(key.getSecondNodeLabel(), secondNode.getSqlID());
        tableRow.setForeignKeys(foreignKeys);
        return tableRow;
    }

    private TableRow helper(MyNode firstNode, MyNode secondNode, String label) {
        TableRow tableRow = new TableRow();
        tableRow.setMyNode(firstNode);
        Map<String, Integer> foreignKeys = new HashMap<>();
        foreignKeys.put(label, secondNode.getSqlID());
        tableRow.setForeignKeys(foreignKeys);
        return tableRow;
    }
}