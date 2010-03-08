
package com.bradmcevoy.web;

public class Scratch {
    public static void main(String[] args) throws Exception {
        for( String c : args) { 
            Class clazz = Class.forName(c);
            System.out.println("hascode: " + c + " = "+ clazz.hashCode());
        }
    }
}
