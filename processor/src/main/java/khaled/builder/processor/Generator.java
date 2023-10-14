package khaled.builder.processor;

import java.io.IOException;
import java.io.Writer;

import static khaled.builder.processor.GenerationInfo.PROTOTYPE;
import static khaled.builder.processor.GenerationInfo.INDENTATION;

public class Generator {

    private final String superTypeName;

    private final String prototypeName;

    private final String packageName;

    private final Writer writer;

    private final BuilderGenerator builderGenerator;
    private final ImplementationGenerator implGenerator;

    private final String builderName;

    public Generator(GenerationInfo generationInfo, Writer writer) {
        this.prototypeName = generationInfo.prototypeName();
        this.packageName = generationInfo.packageName();
        this.superTypeName = generationInfo.superTypeName();
        this.builderName = generationInfo.builderName();
        this.writer = writer;
        this.builderGenerator = new BuilderGenerator(generationInfo, writer);
        this.implGenerator = new ImplementationGenerator(generationInfo, writer);
    }

    void generate() throws IOException {
        this.writer.write("package " + packageName + ";\n\n");

        this.writer.write("import " + PROTOTYPE + ";\n");

        this.writer.write("import java.util.Objects;\n\n\n");

        this.writer.write("public interface " + prototypeName + " extends " + superTypeName + "{\n\n\n");

        generateBuilderMethod();

        builderGenerator.generateBuilder();
        implGenerator.generateImplementation();
        this.writer.write("}");
    }

    private void generateBuilderMethod() throws IOException {
        String builderMethodDeclaration = INDENTATION + "public static " + builderName + " builder(){\n";
        writer.write(builderMethodDeclaration);
        
        String builderMethodBody = INDENTATION.repeat(2)
                + "return new "
                + builderName
                + "();\n"
                + INDENTATION
                + "}\n";
        writer.write(builderMethodBody);

    }

}
