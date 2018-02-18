package com.Domain;

/**
 * Created by John on 2018-02-18.
 */
public class MyRelationshipType {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyRelationshipType that = (MyRelationshipType) o;

        if(firstNode.equals(that.firstNode) && secondNode.equals(that.secondNode)) return true;
        if(firstNode.equals(that.secondNode) && secondNode.equals(that.firstNode)) return true;
        return false;

    }

    @Override
    public int hashCode() {
        int result = firstNode.hashCode();
        result = 31 * (result + secondNode.hashCode());
        return result;
    }

    public MyRelationshipType(String firstNode, String secondNode) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
    }

    private String firstNode;
    private String secondNode;

}
