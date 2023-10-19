package com.khaled.tests;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 *
 * @author khaled
 */
public class CollectionSupportTest {

    @Test
    void should_build() {
        var c = CollectionSupport.builder()
                .list(List.of("a", "b", "test"))
                .set(Set.of(1, 2, 5, 4))
                .build();
        var c2 = CollectionSupport.builder()
                .build();
        assertThat(c, notNullValue());
        assertThat(c2, notNullValue());
    }

    @Test
    void should_be_equal() {
        CollectionSupport prototype1 = CollectionSupport.builder()
                .list(List.of("a", "b", "test"))
                .set(Set.of(1, 2, 5, 4))
                .build();

        CollectionSupport prototype2 = CollectionSupport.builder()
                .list(List.of("a", "b", "test"))
                .set(Set.of(5, 4, 2, 1))
                .build();
        assertThat(prototype1, equalTo(prototype2));
        assertThat(prototype2, equalTo(prototype1));
        assertThat(prototype1.hashCode(), equalTo(prototype2.hashCode()));
    }

    @Test
    void should_not_be_equal() {
        CollectionSupport prototype1 = CollectionSupport.builder()
                .list(List.of("a", "b", "test"))
                .set(Set.of(1, 2, 5, 4))
                .build();

        CollectionSupport prototype2 = CollectionSupport.builder()
                .list(List.of("b", "a", "test"))
                .set(Set.of(5, 4, 2, 1))
                .build();
        assertThat(prototype1, not(equalTo(prototype2)));
        assertThat(prototype2, not(equalTo(prototype1)));
    }
}
