package khaled.builder.processor;

import java.io.IOException;
import java.io.Writer;
import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.GenerationInfo.CHECKER_SUFFIX;

/**
 *
 * @author khaled
 */
public record SimpleTypeHandler(PropertyMethod property) implements TypeHandler {

    private static final String CHECKER_FORMAT = """
                                                    %1$sprivate boolean %2$s%3$s = false;\n
                                                    """;

    private static final String VALIDATION_STATEMENT = """
                                                           %1$sif(!%2$s){
                                                           %3$s throw new IllegalStateException(\"%4$s\");\n
                                                           %1$s}
                                                           
                                                           """;

    @Override
    public void generateBuilderChecker(Writer writer, int indentationLevel) throws IOException {
        if (null == property.defaultValue()) {
            String declaration = CHECKER_FORMAT.formatted(
                    INDENTATION.repeat(indentationLevel),
                    property.name(),
                    CHECKER_SUFFIX);
            writer.write(declaration);
        }
    }

    @Override
    public void generateBuilderValidateStatement(Writer writer, int indentationLevel) throws IOException {
        if (null == property.defaultValue()) {
            String checkerName = property.name() + "Checker";
            String message = "property " + property.name() + " must be initialized before building";
            String statement = VALIDATION_STATEMENT.formatted(INDENTATION.repeat(indentationLevel),
                    checkerName,
                    INDENTATION.repeat(indentationLevel + 1),
                    message
            );
            writer.write(statement);
        }
        writer.write("");
    }

    @Override
    public void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException {
        String mutatorDeclarationPrefix = INDENTATION.repeat(indentationLevel) + "public ";
        String name = property.name();
        String builderType = builderName;
        String paramType = property.type().className();

        String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + name + "(final "
                + paramType + " " + name + "){\n";

        writer.write(mutatorDeclaration);

        String mutatorBody;
        if (property.type().primitive()) {

            mutatorBody = INDENTATION.repeat(indentationLevel + 1) + "this." + name + " = " + name + ";\n";

        } else {

            String requireNonNull = "Objects.requireNonNull(" + name + ");";
            mutatorBody = INDENTATION.repeat(indentationLevel + 1) + "this." + name + " = " + requireNonNull + "\n";

        }
        writer.write(mutatorBody);

        // set checker
        String checkerName = property.name() + "Checker";
        if (null == property.defaultValue()) {

            String setChecker = INDENTATION.repeat(3) + "this." + checkerName + " = true;\n";
            writer.write(setChecker);

        }

        String returSelf = INDENTATION.repeat(indentationLevel + 1) + "return self();\n";
        writer.write(returSelf);
        writer.write(INDENTATION.repeat(indentationLevel) + "}\n\n");

    }
}
