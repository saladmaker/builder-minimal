
package com.khaled.tests;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author khaled
 */
@Prototype.Blueprint
public interface AllSupportBlueprint {
    @Option.Singular
    List<String> words();
    
    String name();
    
    int age();
    
    Optional<Integer> opt();
}
