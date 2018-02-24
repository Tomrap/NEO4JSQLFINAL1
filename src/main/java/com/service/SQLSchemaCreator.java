package com.service;

import com.Domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by John on 2018-02-19.
 */
@Service
public class SQLSchemaCreator {

    public List<TableDetail> createSchema(GraphDetail graphDetail) {

        List<TableDetail> tableDetails = new ArrayList<>();


        //find fks
        Map<String, List<String>> foreigKeys = new HashMap<>();

        for(Map.Entry<MyRelationshipType, List<MyRelationship>> element: graphDetail.getAllMyRelationships().entrySet()) {

            //for given entry decide where the foreignkey goes (1,2 or new table)


            Set<Long> firstNodesIds = new HashSet<>();
            Set<Long> secondNodesIds = new HashSet<>();
            List<MyRelationship> value = element.getValue();
            for(MyRelationship myRelationship: value) {
                firstNodesIds.add(myRelationship.getFirstNode());
                secondNodesIds.add(myRelationship.getSecondNode());
            }

            //3 scenarios

            if(firstNodesIds.size()<value.size() && secondNodesIds.size()<value.size()) {
                List<String> bothForeignKeys = new ArrayList<>();
                bothForeignKeys.add(element.getKey().getFirstNode());
                bothForeignKeys.add(element.getKey().getSecondNode());
                foreigKeys.computeIfAbsent(element.getKey().getFirstNode() +"_"+ element.getKey().getSecondNode(),
                        k -> new ArrayList<>()).addAll(bothForeignKeys);
            }
            else if(firstNodesIds.size()<value.size()) {
                foreigKeys.computeIfAbsent(element.getKey().getSecondNode(),k -> new ArrayList<>()).add(element.getKey().getFirstNode());
            }
            else {
                foreigKeys.computeIfAbsent(element.getKey().getFirstNode(),k -> new ArrayList<>()).add(element.getKey().getSecondNode());
            }

        }

        Map<String, List<MyNode>> allMyNodes = graphDetail.getAllMyNodes();
        for(Map.Entry<String, List<MyNode>> element: allMyNodes.entrySet()) {
            MyNode exampleNode = element.getValue().get(0);
            TableDetail tableDetail = new TableDetail();
            tableDetail.setTableName(element.getKey());
            //TODO in case of junction table there might be composite primary key
            List<String> pks = new ArrayList<>();
            pks.add(exampleNode.getPrimaryKeyName());
            tableDetail.setPk(pks);
            tableDetail.setColumnsAndTypes(exampleNode.getValues());
            //TODO change to optional
            //TODO in case of composite primary key there might be composite foreign key
            List<String> remove = foreigKeys.remove(element.getKey());
            if(remove == null) {
                tableDetail.setGraphFks(new ArrayList<>());
            } else {
                tableDetail.setGraphFks(remove);
            }



            tableDetails.add(tableDetail);
        }

        for (Map.Entry<String, List<String>> element : foreigKeys.entrySet()) {
            TableDetail tableDetail = new TableDetail();
            tableDetail.setTableName(element.getKey());
            List<String> pks = new ArrayList<>();
            pks.add(element.getKey());
            tableDetail.setPk(pks);
            tableDetail.setColumnsAndTypes(new HashMap<>());
            List<String> remove = foreigKeys.remove(element.getKey());
            //TODO change to optional
            if(remove == null) {
                tableDetail.setGraphFks(new ArrayList<>());
            } else {
                tableDetail.setGraphFks(remove);
            }
            tableDetails.add(tableDetail);
        }
        return tableDetails;


    }
}
