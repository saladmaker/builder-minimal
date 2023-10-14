package com.khaled.tests;

import io.helidon.builder.api.Prototype;


@Prototype.Blueprint
public interface AsyncConfigBlueprint {
    String value();
    boolean booleanValue();
    byte byteValue();
    char charValue();
    double doubleValue();
    float floatValue();
    int intValue();
    short shortValue();
    
    
    default int number() {
    	return 6;
    }
    class Inner{
    	
    }
}
