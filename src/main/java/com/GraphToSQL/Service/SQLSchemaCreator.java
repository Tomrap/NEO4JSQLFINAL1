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

        for (Map.Entry<MyRelationshipType, List<MyRelationship>> relationshipGroup : graphDetail.getAllMyRelationships().entrySet()) {
            decideWhichTableShouldHaveForeignKey(foreignKeys, relationshipGroup, allRelationshipsProperties);
        }

        Map<String, Map<Long, MyNode>> allMyNodes = graphDetail.getAllMyNodes();
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
        graphToSQLTableDetail.setTableName(element.getKey());
        List<String> pks = new ArrayList<>();
        pks.add(element.getKey());
        graphToSQLTableDetail.setPk(pks);
        Map<String, Object> columnAndType = new HashMap<>();
        Map<String, Object> remove1 = allRelationshipsProperties.remove(element.getKey());
        if (remove1 != null) {
            columnAndType.putAll(remove1);
        }
        graphToSQLTableDetail.setColumnsAndTypes(columnAndType);
        List<Map.Entry<String, String>> remove = foreignKeys.remove(element.getKey());
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

    private void decideWhichTableShouldHaveForeignKey(Map<String, List<Map.Entry<String, String>>> foreignKeys, Map.Entry<MyRelationshipType, List<MyRelationship>> relationshipGroup, Map<String, Map<String, Object>> allRelationshipsProperties) {

        MyRelationshipType key = relationshipGroup.getKey();
        Set<Long> firstNodesIds = new HashSet<>();
        Set<Long> secondNodesIds = new HashSet<>();
        List<MyRelationship> value = relationshipGroup.getValue();

        Map<String, Object> relationshipProperties = new HashMap<>();
        for (MyRelationship myRelationship : value) {
            firstNodesIds.add(myRelationship.getFirstNode());
            secondNodesIds.add(myRelationship.getSecondNode());
            relationshipProperties.putAll(myRelationship.getValues());
        }

        if (firstNodesIds.size() < value.size() && secondNodesIds.size() < value.size()) {
            List<Map.Entry<String, String>> bothForeignKeys = new ArrayList<>();
            bothForeignKeys.add(new AbstractMap.SimpleEntry<>(key.getFirstNodeLabel(), key.getFirstNodeLabel()));
            bothForeignKeys.add(new AbstractMap.SimpleEntry<>(key.getSecondNodeLabel(), key.getSecondNodeLabel()));
            foreignKeys.computeIfAbsent(key.getLabel(),
                    k -> new ArrayList<>()).addAll(bothForeignKeys);
            allRelationshipsProperties.computeIfAbsent(key.getLabel(),
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
}
