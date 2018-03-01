package com.GraphToSQL.Service;

import com.GraphToSQL.Domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by John on 2018-02-19.
 */
@Service
public class SQLSchemaCreator {

    public List<GraphToSQLTableDetail> createSchema(GraphDetail graphDetail) {

        List<GraphToSQLTableDetail> graphToSQLTableDetails = new ArrayList<>();
        Map<String, List<String>> foreignKeys = new HashMap<>();

        for(Map.Entry<MyRelationshipType, List<MyRelationship>> element: graphDetail.getAllMyRelationships().entrySet()) {
            decideWhichTableShouldHaveForeignKey(foreignKeys, element);
        }

        Map<String, Map<Long, MyNode>> allMyNodes = graphDetail.getAllMyNodes();
        for(Map.Entry<String, Map<Long, MyNode>> element: allMyNodes.entrySet()) {

            createTableDetail(graphToSQLTableDetails, foreignKeys, element);
        }

        for (Map.Entry<String, List<String>> element : foreignKeys.entrySet()) {

            handleJunctionTable(graphToSQLTableDetails, foreignKeys, element);
        }
        return graphToSQLTableDetails;


    }

    private void handleJunctionTable(List<GraphToSQLTableDetail> graphToSQLTableDetails, Map<String, List<String>> foreignKeys, Map.Entry<String, List<String>> element) {
        GraphToSQLTableDetail graphToSQLTableDetail = new GraphToSQLTableDetail();
        graphToSQLTableDetail.setTableName(element.getKey());
        List<String> pks = new ArrayList<>();
        pks.add(element.getKey());
        graphToSQLTableDetail.setPk(pks);
        graphToSQLTableDetail.setColumnsAndTypes(new HashMap<>());
        List<String> remove = foreignKeys.remove(element.getKey());
        //TODO change to optional
        if(remove == null) {
            graphToSQLTableDetail.setGraphFks(new ArrayList<>());
        } else {
            graphToSQLTableDetail.setGraphFks(remove);
        }
        graphToSQLTableDetails.add(graphToSQLTableDetail);
    }

    private void createTableDetail(List<GraphToSQLTableDetail> graphToSQLTableDetails, Map<String, List<String>> foreignKeys, Map.Entry<String, Map<Long, MyNode>> element) {
        Map<String, Object> columnAndType = new HashMap<>();
        for(MyNode node : element.getValue().values()) {
            columnAndType.putAll(node.getValues());
        }

        GraphToSQLTableDetail graphToSQLTableDetail = new GraphToSQLTableDetail();
        graphToSQLTableDetail.setTableName(element.getKey());
        //TODO in case of junction table there might be composite primary key
        List<String> pks = new ArrayList<>();
        pks.add(element.getKey());
        graphToSQLTableDetail.setPk(pks);
        graphToSQLTableDetail.setColumnsAndTypes(columnAndType);
        //TODO change to optional
        //TODO in case of composite primary key there might be composite foreign key
        List<String> remove = foreignKeys.remove(element.getKey());
        if(remove == null) {
            graphToSQLTableDetail.setGraphFks(new ArrayList<>());
        } else {
            graphToSQLTableDetail.setGraphFks(remove);
        }

        graphToSQLTableDetails.add(graphToSQLTableDetail);
    }

    private void decideWhichTableShouldHaveForeignKey(Map<String, List<String>> foreignKeys, Map.Entry<MyRelationshipType, List<MyRelationship>> element) {

        MyRelationshipType key = element.getKey();
        Set<Long> firstNodesIds = new HashSet<>();
        Set<Long> secondNodesIds = new HashSet<>();
        List<MyRelationship> value = element.getValue();
        for(MyRelationship myRelationship: value) {
            firstNodesIds.add(myRelationship.getFirstNode());
            secondNodesIds.add(myRelationship.getSecondNode());
        }

        if(firstNodesIds.size()<value.size() && secondNodesIds.size()<value.size()) {
            List<String> bothForeignKeys = new ArrayList<>();
            bothForeignKeys.add(key.getFirstNodeLabel());
            bothForeignKeys.add(key.getSecondNodeLabel());
            foreignKeys.computeIfAbsent(key.getFirstNodeLabel() +"_"+ key.getSecondNodeLabel(),
                    k -> new ArrayList<>()).addAll(bothForeignKeys);
            key.setFirstNodeForeignKey(true);
            key.setSecondNodeForeignKey(true);
        }
        else if(firstNodesIds.size()<value.size()) {
            foreignKeys.computeIfAbsent(key.getSecondNodeLabel(), k -> new ArrayList<>()).add(key.getFirstNodeLabel());
            key.setFirstNodeForeignKey(true);
        }
        else {
            foreignKeys.computeIfAbsent(key.getFirstNodeLabel(), k -> new ArrayList<>()).add(key.getSecondNodeLabel());
            key.setSecondNodeForeignKey(true);
        }
    }
}
