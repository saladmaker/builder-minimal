package com.khaled.tests;


import org.junit.jupiter.api.Test;
import static  org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author khaled
 */
public class NullHostilityTest {

    @Test
    void should_throw_when_reference_value_set_to_null() {

        assertThrows(NullPointerException.class, ()->{
            Simple.builder()
                .name(null);
        });
        
    }
}
