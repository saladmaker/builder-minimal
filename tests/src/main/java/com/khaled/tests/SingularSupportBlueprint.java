package com.khaled.tests;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;
import java.util.List;
import java.util.Set;

/**
 *
 * @author khaled
 */
@Prototype.Blueprint
public interface SingularSupportBlueprint {
    
    @Option.Singular
    List<String> word();
    
    @Option.Singular
    Set<Integer> numbers();
    
    @Option.Singular
    List<Integer> s();
    
    @Option.Singular
    Set<Long> l();
    
    List<String> simple();
}
