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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableDetail {

    public static final String schemaName = "hibernate";

    public String getTableName() {
        return tableName;
    }

    public List<String> getPk() {
        return pk;
    }

    public List<String> getFields() {
        return fields;
    }

    public Map<String, String> getFks() {
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

    public void setFks(Map<String, String> fks) {
        this.fks = fks;
    }

    private  List<String> pk;
    private  String tableName;
    private  List<String> fields;
    private  Map<String, String> fks;


    public List<String> getForeignKeyColumns() {

        return new ArrayList<>(fks.keySet());
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
