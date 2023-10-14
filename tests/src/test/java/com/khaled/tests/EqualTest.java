package com.khaled.tests;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 *
 * @author khaled
 */
public class EqualTest {

    @Test
    void shouldBeEqual() {

        Simple prototype1 = Simple.builder()
                .name("someName")
                .age(15)
                .build();


        Simple prototype2 = Simple.builder()
                .name("someName")
                .age(15)
                .build();

        assertThat(prototype1, equalTo(prototype2));
        assertThat(prototype2, equalTo(prototype1));

        //hash code
        assertThat(prototype1.hashCode(), equalTo(prototype2.hashCode()));

    }

    @Test
    void shouldNotBeEqual() {
        Simple differentName1 = Simple.builder()
                .name("someName")
                .age(15)
                .build();
        Simple differentName2 = Simple.builder()
                .name("someName2")
                .age(15)
                .build();

        assertThat(differentName1, not(equalTo(differentName2)));
        assertThat(differentName2, not(equalTo(differentName1)));

        //different age
        Simple differentAge1 = Simple.builder()
                .name("someName")
                .age(20).build();
        
        Simple differentAge2 = Simple.builder()
                .name("someName")
                .age(15).build();

        assertThat(differentAge1, not(equalTo(differentAge2)));
        assertThat(differentAge2, not(equalTo(differentAge1)));
    }

}
