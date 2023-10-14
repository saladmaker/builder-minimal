package com.khaled.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author khaled
 */
public class ValidateTest {
    @Test
    void should_throw_when_property_not_set() {

        assertThrows(IllegalStateException.class, ()->{
            Simple.builder()
                .name("").build();
        });
        
        assertThrows(IllegalStateException.class, ()->{
            Simple.builder()
                .age(3).build();
        });
    }
}
