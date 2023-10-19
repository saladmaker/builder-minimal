package com.khaled.tests;

import io.helidon.builder.api.Prototype;
import io.helidon.builder.api.Option;

/**
 *
 * @author khaled
 */
@Prototype.Blueprint
public interface DefaultSupportBlueprint {
    static final String DEFAULT_NAME = "khaled";
    static final byte DEFAULT_BIT = 2;
    
    @Option.Default(DEFAULT_NAME)
    String name();
    
    int age();
    
    Double doubleValue();
    
    @Option.DefaultInt(DEFAULT_BIT)
    byte bit();
    
    
}
