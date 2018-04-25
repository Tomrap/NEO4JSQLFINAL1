package com.GraphToSQL.Service;

import com.GraphToSQL.Domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by John on 2018-02-19.
 */
@Service
public class SQLSchemaCreator {

    public List<GraphToSQLTableDetail> createSchema(GraphDetail graphDetail) {

        List<GraphToSQLTableDetail> graphToSQLTableDetails = new ArrayList<>();
        Map<String, List<Map.Entry<String, String>>> foreignKeys = new ConcurrentHashMap<>();
        Map<String, Map<String, Object>> allRelationshipsProperties = new HashMap<>();

        Map<String, Map<Long, MyNode>> allMyNodes = graphDetail.getAllMyNodes();
        for (Map.Entry<MyRelationshipType, List<MyRelationship>> relationshipGroup : graphDetail.getAllMyRelationships().entrySet()) {
            decideWhichTableShouldHaveForeignKey(foreignKeys, relationshipGroup, allRelationshipsProperties , allMyNodes);
        }

        for (Map.Entry<String, Map<Long, MyNode>> nodeGroup : allMyNodes.entrySet()) {

            createTableDetail(graphToSQLTableDetails, foreignKeys, nodeGroup, allRelationshipsProperties);
        }

        for (Map.Entry<String, List<Map.Entry<String, String>>> element : foreignKeys.entrySet()) {

            handleJunctionTable(graphToSQLTableDetails, foreignKeys, element, allRelationshipsProperties);
        }
        return graphToSQLTableDetails;
    }

    private void handleJunctionTable(List<GraphToSQLTableDetail> graphToSQLTableDetails, Map<String, List<Map.Entry<String, String>>> foreignKeys, Map.Entry<String, List<Map.Entry<String, String>>> element, Map<String, Map<String, Object>> allRelationshipsProperties) {
        GraphToSQLTableDetail graphToSQLTableDetail = new GraphToSQLTableDetail();
        String tableName = element.getKey();
        graphToSQLTableDetail.setTableName(tableName);
        List<String> pks = new ArrayList<>();
        pks.add(tableName);
        graphToSQLTableDetail.setPk(pks);
        Map<String, Object> columnAndType = new HashMap<>();
        Map<String, Object> remove1 = allRelationshipsProperties.remove(tableName);
        if (remove1 != null) {
            columnAndType.putAll(remove1);
        }
        graphToSQLTableDetail.setColumnsAndTypes(columnAndType);
        List<Map.Entry<String, String>> remove = foreignKeys.remove(tableName);
        if (remove == null) {
            graphToSQLTableDetail.setGraphFks(new ArrayList<>());
        } else {
            graphToSQLTableDetail.setGraphFks(remove);
        }
        graphToSQLTableDetails.add(graphToSQLTableDetail);
    }

    private void createTableDetail(List<GraphToSQLTableDetail> graphToSQLTableDetails, Map<String, List<Map.Entry<String, String>>> foreignKeys, Map.Entry<String, Map<Long, MyNode>> nodeGroup, Map<String, Map<String, Object>> allRelationshipsProperties) {
        // in NEO4J nodes with the same label can have different properties, i need to collect all to create all the columns
        // important assumption - all properties with the same name within the same node group have the same type - it is not enforced by NEO4J
        GraphToSQLTableDetail graphToSQLTableDetail = new GraphToSQLTableDetail();
        Map<String, Object> columnAndType = new HashMap<>();
        for (MyNode node : nodeGroup.getValue().values()) {
            columnAndType.putAll(node.getValues());
        }

        Map<String, Object> remove1 = allRelationshipsProperties.remove(nodeGroup.getKey());
        if (remove1 != null) {
            columnAndType.putAll(remove1);
        }

        graphToSQLTableDetail.setTableName(nodeGroup.getKey());
        List<String> pks = new ArrayList<>();
        pks.add(nodeGroup.getKey());
        graphToSQLTableDetail.setPk(pks);
        graphToSQLTableDetail.setColumnsAndTypes(columnAndType);
        List<Map.Entry<String, String>> remove = foreignKeys.remove(nodeGroup.getKey());
        if (remove == null) {
            graphToSQLTableDetail.setGraphFks(new ArrayList<>());
        } else {
            graphToSQLTableDetail.setGraphFks(remove);
        }

        graphToSQLTableDetails.add(graphToSQLTableDetail);
    }

    private void decideWhichTableShouldHaveForeignKey(Map<String, List<Map.Entry<String, String>>> foreignKeys, Map.Entry<MyRelationshipType, List<MyRelationship>> relationshipGroup, Map<String, Map<String, Object>> allRelationshipsProperties, Map<String, Map<Long, MyNode>> allMyNodes) {

        MyRelationshipType key = relationshipGroup.getKey();
        Set<Long> firstNodesIds = new HashSet<>();
        Set<Long> secondNodesIds = new HashSet<>();
        List<MyRelationship> value = relationshipGroup.getValue();

        Map<String, Object> relationshipProperties = new HashMap<>();
        countNodesInRelationships(firstNodesIds, secondNodesIds, value, relationshipProperties,allMyNodes,key);

        if ((firstNodesIds.size() < value.size() && secondNodesIds.size() < value.size()) || relationshipProperties.size()>0 ) {
            List<Map.Entry<String, String>> bothForeignKeys = new ArrayList<>();
            if(key.getFirstNodeLabel().equals(key.getSecondNodeLabel())) {
                bothForeignKeys.add(new AbstractMap.SimpleEntry<>(key.getFirstNodeLabel(), "1_" + key.getLabel()));
                bothForeignKeys.add(new AbstractMap.SimpleEntry<>(key.getSecondNodeLabel(), "2_" + key.getLabel()));
            } else {
                bothForeignKeys.add(new AbstractMap.SimpleEntry<>(key.getFirstNodeLabel(), key.getLabel()));
                bothForeignKeys.add(new AbstractMap.SimpleEntry<>(key.getSecondNodeLabel(), key.getLabel()));
            }
            String tableName = key.getFirstNodeLabel() + "_" +key.getLabel() +"_" +key.getSecondNodeLabel();
            foreignKeys.computeIfAbsent(tableName,
                    k -> new ArrayList<>()).addAll(bothForeignKeys);
            allRelationshipsProperties.computeIfAbsent(tableName,
                    k -> new HashMap<>()).putAll(relationshipProperties);
            key.setFirstNodeForeignKey(true);
            key.setSecondNodeForeignKey(true);
        } else if (firstNodesIds.size() < value.size()) {
            foreignKeys.computeIfAbsent(key.getSecondNodeLabel(), k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(key.getFirstNodeLabel(), key.getLabel()));
            allRelationshipsProperties.computeIfAbsent(key.getSecondNodeLabel(), k -> new HashMap<>()).putAll(relationshipProperties);
            key.setFirstNodeForeignKey(true);
        } else {
            foreignKeys.computeIfAbsent(key.getFirstNodeLabel(), k -> new ArrayList<>()).add(new AbstractMap.SimpleEntry<>(key.getSecondNodeLabel(), key.getLabel()));
            allRelationshipsProperties.computeIfAbsent(key.getFirstNodeLabel(), k -> new HashMap<>()).putAll(relationshipProperties);
            key.setSecondNodeForeignKey(true);
        }
    }

    private void countNodesInRelationships(Set<Long> firstNodesIds, Set<Long> secondNodesIds, List<MyRelationship> value, Map<String, Object> relationshipProperties, Map<String, Map<Long, MyNode>> allMyNodes, MyRelationshipType key) {
        for (MyRelationship myRelationship : value) {
            if(allMyNodes.get(key.getFirstNodeLabel()).containsKey(myRelationship.getFirstNode())) {
                myRelationship.setDirectionSameAsInType(true);
                firstNodesIds.add(myRelationship.getFirstNode());
                secondNodesIds.add(myRelationship.getSecondNode());
            } else {
                myRelationship.setDirectionSameAsInType(false);
                firstNodesIds.add(myRelationship.getSecondNode());
                secondNodesIds.add(myRelationship.getFirstNode());
            }
            relationshipProperties.putAll(myRelationship.getValues());
        }
    }
}
