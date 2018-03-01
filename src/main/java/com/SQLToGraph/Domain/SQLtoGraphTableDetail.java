/**
 * Copyright (c) 2015 Michael Hunger
 *
 * This file is part of Relational to Neo4j Importer.
 *
 *  Relational to Neo4j Importer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Relational to Neo4j Importer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Relational to Neo4j Importer.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.SQLToGraph.Domain;

import com.main.TableDetail;

import java.util.*;

public class SQLtoGraphTableDetail extends TableDetail {

    private static final Map<String, SQLtoGraphTableDetail> TABLES = new LinkedHashMap<>();
    public static final String schemaName = "sakila";

    private  List<String> fields;
    private   Map<List<String>, String> fks;
    private int firstIndex;
    private HashMap<Integer,Integer> mappingMap;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Map<List<String>, String> getFks() {
        return fks;
    }

    public void setFks(Map<List<String>, String> fks) {
        this.fks = fks;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public HashMap<Integer, Integer> getMappingMap() {
        return mappingMap;
    }

    public void setMappingMap(HashMap<Integer, Integer> mappingMap) {
        this.mappingMap = mappingMap;
    }

    public static void addtoTables(SQLtoGraphTableDetail SQLtoGraphTableDetail) {
        TABLES.put(SQLtoGraphTableDetail.getTableName(), SQLtoGraphTableDetail);
    }

    public static SQLtoGraphTableDetail getTable(String tableName) {
        return TABLES.get(tableName);
    }

    public List<String> getForeignKeyColumns() {

        List<String> list = new ArrayList<>();

        for (Map.Entry<List<String>, String> entry : fks.entrySet()) {
            list.addAll(entry.getKey());
        }
        return list;
    }
}
