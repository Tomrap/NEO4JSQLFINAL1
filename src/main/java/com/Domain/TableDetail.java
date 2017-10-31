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
package com.Domain;

import java.util.*;

public class TableDetail {

    private static final Map<String, TableDetail> TABLES = new LinkedHashMap<>();
    public static final String schemaName = "blabla";

    public String getTableName() {
        return tableName;
    }

    public List<String> getPk() {
        return pk;
    }

    public List<String> getFields() {
        return fields;
    }

    public Map<List<String>, String> getFks() {
        return fks;
    }

    public void setPk(List<String> pk) {
        this.pk = pk;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public void setFks(Map<List<String>, String> fks) {
        this.fks = fks;
    }

    private  List<String> pk;
    private  String tableName;
    private  List<String> fields;
    private   Map<List<String>, String> fks;

    public static void addtoTables(TableDetail tableDetail) {
        TABLES.put(tableDetail.getTableName(),tableDetail);
    }

    public static TableDetail getTable(String tableName) {
        return TABLES.get(tableName);
    }

    public List<String> getForeignKeyColumns() {

        List<String> list = new ArrayList<>();

        for (Map.Entry<List<String>, String> entry : fks.entrySet()) {
            list.addAll(entry.getKey());
        }
        return list;
    }

    public boolean hasExactlyTwoForeignKeys() {

        return fks.size() == 2;
    }

    public boolean isPartOfPk(String name) {

        boolean result = false;
        for (String pk : getPk()) {
            if (pk.equals(name)) {
                result = true;
            }
        }
        return result;
    }


    @Override
    public String toString() {
        return "TableInfo{" +
                ", tableName='" + tableName + '\'' +
                ", pk='" + pk + '\'' +
                ", fields=" + fields +
                ", fks=" + fks +
                '}';
    }
}
