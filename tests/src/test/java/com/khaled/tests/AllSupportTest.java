package com.khaled.tests;

import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author khaled
 */
public class AllSupportTest {
    @Test
    void should_build() {
        AllSupport o = AllSupport.builder()
                .name("khaled")
                .age(34)
                .build();

        assertThat(o, is(notNullValue()));
        assertThat(o.opt(), is(equalTo(Optional.empty())));
        
        AllSupport.builder()
                .words(List.of("df", "sdfsfd"))
                .addWord("dsf")
                .name("khaled")
                .opt(23)
                .age(34)
                .build();
        
    }
    
    @Test
    void should_not_build() {
        
        assertThrows(IllegalStateException.class, ()->{
            AllSupport o = AllSupport.builder()
                .age(32)
                .build();
        });
        
    }
    @Test
    void should_be_equal() {
        AllSupport o = AllSupport.builder()
                .words(List.of("df", "sdfsfd"))
                .addWord("dsf")
                .name("khaled")
                .opt(23)
                .age(34)
                .build();
        AllSupport o2 = AllSupport.builder()
                .words(List.of("df", "sdfsfd"))
                .addWord("dsf")
                .name("khaled")
                .opt(23)
                .age(34)
                .build();
        
        assertThat(o, equalTo(o2));
        assertThat(o2, equalTo(o));
        assertThat(o2.hashCode(), equalTo(o.hashCode()));
        
        
        
        AllSupport a1 = AllSupport.builder()
                .words(List.of("df", "sdfsfd"))
                .addWord("dsf")
                .name("khaled")
                .opt(23)
                .age(34)
                .build();
        AllSupport a2 = AllSupport.builder()
                .words(List.of("df", "sdfsfd"))
                .addWord("dsf")
                .name("khaled")
                .opt(24)
                .age(34)
                .build();
        
        assertThat(a1, not(equalTo(a2)));
        assertThat(a2, not(equalTo(a1)));
    }
}
