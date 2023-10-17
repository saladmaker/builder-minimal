package com.khaled.tests;

import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author khaled
 */
public class OptionaSupportTest {

    @Test
    void should_build() {
        OptionalSupport o = OptionalSupport.builder().build();
        assertThat(o, is(notNullValue()));
        assertThat(o.name(), is(equalTo(Optional.empty())));
        
        
        
    }
    
    @Test
    void should_not_build() {
        
        assertThrows(NullPointerException.class, ()->{
            OptionalSupport.builder().name(null).build();
        });
        
    }
    @Test
    void should_be_equal() {
        OptionalSupport o = OptionalSupport.builder().build();
        OptionalSupport o2 = OptionalSupport.builder().build();
        
        assertThat(o, equalTo(o2));
        assertThat(o2, equalTo(o));
        assertThat(o2.hashCode(), equalTo(o.hashCode()));
        
        
        OptionalSupport a1 = OptionalSupport.builder().name("dsfs").build();
        OptionalSupport a2 = OptionalSupport.builder().name("dsfs").build();
        
        assertThat(a1, equalTo(a2));
        assertThat(a2, equalTo(a1));
        assertThat(a2.hashCode(), equalTo(a1.hashCode()));
    }
    @Test
    void should_not_be_equal() {
        OptionalSupport o = OptionalSupport.builder().name("ssfsd").build();
        assertThat(o, is(notNullValue()));
        
        
        OptionalSupport o2 = OptionalSupport.builder().name("ssfs").build();
        assertThat(o, not(equalTo(o2)));
        assertThat(02, not(equalTo(o)));
    }
}
