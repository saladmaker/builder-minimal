package com.khaled.tests;

import io.helidon.builder.api.Prototype;
import java.util.List;
import java.util.Set;

/**
 *
 * @author khaled
 */
@Prototype.Blueprint
public interface CollectionSupportBlueprint {
    List<String> list();
    
    Set<Integer> set();
    
}
