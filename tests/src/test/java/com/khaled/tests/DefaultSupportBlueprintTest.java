package com.khaled.tests;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 *
 * @author khaled
 */
public class DefaultSupportBlueprintTest {
    @Test
    void should_set_default(){
        DefaultSupport defaultSupport = DefaultSupport.builder()
                .doubleValue(54d)
                .age(32)
                .build();
        
        assertThat(defaultSupport.name(), is(DefaultSupportBlueprint.DEFAULT_NAME));
        assertThat(defaultSupport.bit(), is(Byte.valueOf(DefaultSupportBlueprint.DEFAULT_BIT)));
        assertThat(defaultSupport.bool(), is(equalTo(Boolean.FALSE)));
        
    }
}
