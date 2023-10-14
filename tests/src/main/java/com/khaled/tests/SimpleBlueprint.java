package com.khaled.tests;

import io.helidon.builder.api.Prototype;


/**
 *
 * @author khaled
 */
@Prototype.Blueprint
public interface SimpleBlueprint {
    String name();
    int age();
}
