package com.khaled.tests;

import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.is;

/**
 *
 * @author khaled
 */
public class AsyncConfigTest {

    public AsyncConfigTest() {
    }

    @Test
    public void testBuild() {

        AsyncConfig async = AsyncConfig.builder()
                .booleanValue(true)
                .charValue('d')
                .floatValue(3.3f)
                .byteValue((byte) 4)
                .shortValue((short) 32)
                .intValue(3).value("khaled").doubleValue(323.23d).build();

        assertThat(async.booleanValue(), is(true));
        assertThat(async.shortValue(), is((short) 32));
        assertThat(async.intValue(), is(3));

    }

    @Test
    void testToString() {
        AsyncConfig async = AsyncConfig.builder()
                .booleanValue(true)
                .charValue('d')
                .floatValue(3.3f)
                .byteValue((byte) 4)
                .shortValue((short) 32)
                .intValue(3).value("khaled").doubleValue(323.23d).build();

        var toString = async.toString();
        assertThat(toString, containsString("booleanValue=true"));
        assertThat(toString, containsString("shortValue=32"));
        assertThat(toString, containsString("value=khaled"));
        assertThat(toString, containsString("doubleValue=323.23"));
        assertThat(toString, not(containsString("shortValue=4")));

    }
}
