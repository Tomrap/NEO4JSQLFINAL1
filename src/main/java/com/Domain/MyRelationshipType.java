package com.Domain;

/**
 * Created by John on 2018-02-18.
 */
public class MyRelationshipType {


    //code for cyclic graphs
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        MyRelationshipType that = (MyRelationshipType) o;
//
//        return firstNode.equals(that.firstNode) && secondNode.equals(that.secondNode)
//                || firstNode.equals(that.secondNode) && secondNode.equals(that.firstNode);
//
//    }
//
//    @Override
//    public int hashCode() {
//        int result = firstNode.hashCode();
//        result = 31 * (result + secondNode.hashCode());
//        return result;
//    }

    public MyRelationshipType(String firstNode, String secondNode) {
        this.firstNode = firstNode;
        this.secondNode = secondNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyRelationshipType that = (MyRelationshipType) o;

        if (!firstNode.equals(that.firstNode)) return false;
        return secondNode.equals(that.secondNode);

    }

    @Override
    public int hashCode() {
        int result = firstNode.hashCode();
        result = 31 * result + secondNode.hashCode();
        return result;
    }

    public String getFirstNode() {
        return firstNode;
    }

    public String getSecondNode() {
        return secondNode;
    }

    private String firstNode;
    private String secondNode;

    public boolean isInARelationshipp(String label) {
        return firstNode.equals(label) || secondNode.equals(label);
    }
}
