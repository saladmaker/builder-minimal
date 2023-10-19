package com.khaled.tests;

import io.helidon.builder.api.Option;
import io.helidon.builder.api.Prototype;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author khaled
 */
@Prototype.Blueprint
public interface SupportAllDefaultBlueprint {
    static final List<Integer> INTS = Arrays.asList(1,2,3,4,5);
    static final List<Byte> BYTES = Arrays.asList((byte)1,(byte)2,(byte)3,(byte)4,(byte)5);
    static final List<Double> DOUBLES = Arrays.asList(1d,2d,3d,4d,6d);
    static final List<Float> FLOATS = Arrays.asList((float)12,(float)434);
    static final List<String>  STRINGS =  Arrays.asList("khaled", "abderrahim");
    
    @Option.DefaultInt({1,2,3,4,5})
    List<Integer> ints();
    
    
    @Option.DefaultInt({1,2,3,4,5})
    List<Byte> bytes();
    
    @Option.DefaultLong({2332332342L,324234243})
    List<Long> longs();
    
    @Option.Default({"khaled", "abderrahim"})
    List<String> strings();
    
    @Option.DefaultDouble({1,2,3,4,6})
    List<Double> doubles();
    
    @Option.DefaultDouble({12,434})
    List<Float> floats();
    
}
