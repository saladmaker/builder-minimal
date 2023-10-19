package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import static khaled.builder.processor.GenerationInfo.INDENTATION;

/**
 *
 * @author khaled
 */
public record OptionalTypeHandler(String name, TypeName type) implements TypeHandler {


    @Override
    public void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException {
        String propertyName = name();
        String declaration = INDENTATION.repeat(indentationLevel) + "public " + builderName + " " + propertyName
                + "(final "
                + mutatorType()
                + " " + propertyName + "){\n";
        writer.write(declaration);
        String requireNonNull = " = Objects.requireNonNull(%1$s);\n".formatted(propertyName);
        String mutatorBody = INDENTATION.repeat(indentationLevel + 1) + "this." + propertyName + requireNonNull;
        writer.write(mutatorBody);
        String returSelf = INDENTATION.repeat(indentationLevel + 1) + "return self();\n";
        writer.write(returSelf);
        writer.write(INDENTATION.repeat(indentationLevel) + "}\n");
    }

    String mutatorType() {
        return type().typeArguments().getFirst().className();
    }

}
