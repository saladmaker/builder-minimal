package com.khaled.tests;

import java.util.List;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author khaled
 */
public class SingularTest {

    @Test
    void should_build() {
        SingularSupport cs1 = SingularSupport.builder()
                .word(List.of("sf", "33"))
                .addWord(List.of("dfd", "dfsd", "dfsdf"))
                .addWord("dfsdf")
                .numbers(Set.of(1, 2, 3))
                .addNumbers(Set.of(3, 4, 5))
                .addNumber(33)
                .s(List.of(1, 3))
                .addS(List.of(2, 4, 5, 6))
                .addS(45)
                .l(Set.of(5L, 6L, 8L))
                .addL(Set.of(2L, 3L, 4L))
                .addL(343L)
                .simple(List.of("sdf", "sfsfd"))
                .addSimple(List.of("sfsdf", "dsfsdfs"))
                .build();
        SingularSupport cs2 = SingularSupport.builder()
                .word(List.of("sf", "33"))
                .addWord(List.of("dfd", "dfsd", "dfsdf"))
                .addWord("dfsdf")
                .numbers(Set.of(1, 2, 3))
                .addNumbers(Set.of(3, 4, 5))
                .addNumber(33)
                .s(List.of(1, 3))
                .addS(List.of(2, 4, 5, 6))
                .addS(45)
                .l(Set.of(5L, 6L, 8L))
                .addL(Set.of(2L, 3L, 4L))
                .addL(343L)
                .simple(List.of("sdf", "sfsfd"))
                .addSimple(List.of("sfsdf", "dsfsdfs"))
                .build();
        assertThat(cs1, is(notNullValue()));
        assertThat(cs1, equalTo(cs2));
        assertThat(cs2, equalTo(cs1));
        assertThat(cs1.hashCode(), equalTo(cs2.hashCode()));

    }
}
