package khaled.builder.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.GenerationInfo.PROTOTYPE_BUILDER;
import static khaled.builder.processor.GenerationInfo.COMMON_BUILDER;
import static khaled.builder.processor.GenerationInfo.CHECKER_SUFFIX;

/**
 *
 * @author khaled
 */
public class BuilderGenerator {

    private final GenerationInfo generationInfo;
    private final Writer writer;
    private final String builderName;
    private final String prototypeName;
    private final String implName;

    private final Map<String, String> propertyCheckers = new LinkedHashMap<>();

    BuilderGenerator(GenerationInfo generationInfo, Writer writer) {
        this.generationInfo = generationInfo;
        this.writer = writer;
        this.builderName = generationInfo.builderName();
        this.prototypeName = generationInfo.prototypeName();
        this.implName = generationInfo.implName();

    }

    void generateBuilder() throws IOException {

        generateDeclaration();
        generateCheckers();

        generateProperties();

        //mutators
        generateMutators();

        // accessor
        generateAccessors();

        generateValidate();

        generateBuilds();

        writer.write(INDENTATION + "}\n\n");
    }

    private void generateDeclaration() throws IOException {
        String builderDeclaration = INDENTATION + "class " + builderName + " implements "
                + PROTOTYPE_BUILDER + "<" + builderName + ", " + prototypeName + ">,\n" + INDENTATION.repeat(4)
                + COMMON_BUILDER + "<" + builderName + ", " + prototypeName + ">{\n";

        writer.write(builderDeclaration);

    }

    private void generateProperties() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//properties\n");
        String propertyDeclarationPrefix = INDENTATION.repeat(2) + "private ";
        for (var property : generationInfo.properties()) {
            String name = property.name();

            String type = property.type().className();

            String propertyDeclaration = propertyDeclarationPrefix + type + " " + name + " ;\n";

            writer.write(propertyDeclaration);

        }

    }

    private void generateCheckers() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//checkers\n");
        String propertyCheckerPrefix = INDENTATION.repeat(2) + "private boolean ";
        for (var property : generationInfo.properties()) {
            String name = property.name();

            String checker = property.name() + CHECKER_SUFFIX;

            if (null != propertyCheckers.put(name, checker)) {
                throw new IllegalStateException("duplicate property" + name);

            }

            String propertyCheckerDeclaration = propertyCheckerPrefix + checker + " = false;\n";
            writer.write(propertyCheckerDeclaration);
        }
    }

    private void generateMutators() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//mutators\n");
        String mutatorDeclarationPrefix = INDENTATION.repeat(2) + "public ";
        for (var property : generationInfo.properties()) {
            String name = property.name();
            String builderType = builderName;
            String paramType = property.type().className();

            String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + name + "(final "
                    + paramType + " " + name + "){\n";

            writer.write(mutatorDeclaration);

            String mutatorBody;
            if (property.type().primitive()) {

                mutatorBody = INDENTATION.repeat(3) + "this." + name + " = " + name + ";\n";

            } else {

                mutatorBody = INDENTATION.repeat(3) + "this." + name + " = Objects.requireNonNull(" + name + ");\n";

            }
            writer.write(mutatorBody);

            // set checker
            String checker = propertyCheckers.get(name);
            String setChecker = INDENTATION.repeat(3) + "this." + checker + " = true;\n";
            writer.write(setChecker);
            String returSelf = INDENTATION.repeat(3) + "return self();\n";
            writer.write(returSelf);
            writer.write(INDENTATION.repeat(2) + "}\n\n");
        }
    }

    private void generateAccessors() throws IOException {
        String accessorDeclarationPrefix = INDENTATION.repeat(2) + "public ";
        for (var property : generationInfo.properties()) {
            String type = property.type().className();
            String name = property.name();

            String accessorDeclaration = accessorDeclarationPrefix + type + " " + name + "(){\n";
            writer.write(accessorDeclaration);

            String accessorBody = INDENTATION.repeat(3) + "return this." + name + ";\n";
            writer.write(accessorBody);
            writer.write(INDENTATION.repeat(2) + "}\n\n");
        }
    }

    private void generateValidate() throws IOException {
        String validatorDeclarationPrefix = INDENTATION.repeat(2) + "private void validate(){\n\n";
        writer.write(validatorDeclarationPrefix);
        for (var property : generationInfo.properties()) {

            String name = property.name();
            String message = name + " must be set before building";
            String checker = propertyCheckers.get(name);
            String validationFormat = """
          %1$sif(!%2$s){%n
          %3$sthrow new IllegalStateException(\"%4$s\");%n
          %1$s}%n"""
                    .formatted(INDENTATION.repeat(3),
                            checker,
                            INDENTATION.repeat(4),
                            message);
            writer.write(validationFormat);
        }
        writer.write(INDENTATION.repeat(2) + "}\n\n");
    }

    private void generateBuilds() throws IOException {
        String buildPrototypeDeclarationPrefix = INDENTATION.repeat(2) + "@Override\n" + INDENTATION.repeat(2) + "public "
                + prototypeName + " buildPrototype(){\n";
        writer.write(buildPrototypeDeclarationPrefix);
        writer.write(INDENTATION.repeat(3) + "validate();\n");
        writer.write(INDENTATION.repeat(3) + "return new " + implName + "(this);\n");
        writer.write(INDENTATION.repeat(2) + "}\n");
        String buildDeclarationPrefix = INDENTATION.repeat(2) + "@Override\n" + INDENTATION.repeat(2) + "public " + prototypeName
                + " build(){\n";
        writer.write(buildDeclarationPrefix);
        writer.write(INDENTATION.repeat(3) + "return buildPrototype();\n");
        writer.write(INDENTATION.repeat(2) + "}\n");
    }

}
