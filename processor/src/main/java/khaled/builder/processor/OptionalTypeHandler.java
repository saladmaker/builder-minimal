package khaled.builder.processor;

import java.io.IOException;
import java.io.Writer;
import static khaled.builder.processor.GenerationInfo.INDENTATION;

/**
 *
 * @author khaled
 */
public record OptionalTypeHandler(PropertyMethod property) implements TypeHandler {

    public static OptionalTypeHandler create(PropertyMethod property) {
        return new OptionalTypeHandler(property);
    }

    @Override
    public void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException {
        String propertyName = property().name();
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
        return property().type().typeArguments().get(0).className();
    }

}
