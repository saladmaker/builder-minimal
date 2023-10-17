package khaled.builder.processor;

import java.io.IOException;

/**
 *
 * @author khaled
 */
public sealed interface Generator permits PrototypeGenerator, BuilderGenerator, ImplementationGenerator{
    void generate() throws IOException;
}
