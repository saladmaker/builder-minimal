package com.khaled.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author khaled
 */
public class SupportAllDefaultTest {
    @Test
    void should_build(){
        var prototype = SupportAllDefault.builder().build();
        assertThat(prototype, notNullValue());                
    }
    @Test
    void should_set_default(){
        var prototype = SupportAllDefault.builder().build();

        
        assertThat(prototype.ints(), is(equalTo(SupportAllDefaultBlueprint.INTS)));
        assertThat(prototype.bytes(), is(equalTo(SupportAllDefaultBlueprint.BYTES)));
        assertThat(prototype.strings(), is(equalTo(SupportAllDefaultBlueprint.STRINGS)));
        assertThat(prototype.doubles(), is(equalTo(SupportAllDefaultBlueprint.DOUBLES)));
        
    }
}
