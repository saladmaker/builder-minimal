package com.khaled.tests;

import io.helidon.builder.api.Prototype;
import java.util.Optional;

/**
 *
 * @author khaled
 */

@Prototype.Blueprint
public interface OptionalSupportBlueprint {
    Optional<String> name();
}
