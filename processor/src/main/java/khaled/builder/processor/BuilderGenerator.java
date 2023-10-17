package khaled.builder.processor;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.GenerationInfo.PROTOTYPE_BUILDER;
import static khaled.builder.processor.GenerationInfo.COMMON_BUILDER;

/**
 *
 * @author khaled
 */
public final class BuilderGenerator implements Generator{

    private final Writer writer;
    private final String builderName;
    private final String prototypeName;
    private final String implName;
    private final Set<PropertyMethod> properties;

    BuilderGenerator(GenerationInfo generationInfo, Writer writer) {
        this.writer = writer;
        this.builderName = generationInfo.builderName();
        this.prototypeName = generationInfo.prototypeName();
        this.implName = generationInfo.implName();
        this.properties = generationInfo.properties();

    }

    @Override
    public void generate() throws IOException {

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
        for (var property : properties) {
            property.typeHandler().generateBuilderProperty(writer, 2);
        }

    }

    private void generateCheckers() throws IOException {
        int indentationLevel = 2;
        writer.write("\n\n" + INDENTATION.repeat(indentationLevel) + "//checkers\n");
        for (var property : properties) {
            property.typeHandler().generateBuilderChecker(writer, 2);
        }
    }

    private void generateMutators() throws IOException {
        var indentationLevel = 2;
        writer.write("\n\n" + INDENTATION.repeat(indentationLevel) + "//mutators\n");
        for (var property : properties) {
            property.typeHandler().generateBuilderMutators(writer, builderName, indentationLevel);
        }
    }

    private void generateAccessors() throws IOException {
        for (var property : properties) {
            property.typeHandler().generateAccessors(writer, 2, this);
        }
    }

    private void generateValidate() throws IOException {
        String validatorDeclarationPrefix = INDENTATION.repeat(2) + "private void validate(){\n\n";
        writer.write(validatorDeclarationPrefix);
        for (var property: properties) {
            property.typeHandler().generateBuilderValidateStatement(writer, 3);
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
