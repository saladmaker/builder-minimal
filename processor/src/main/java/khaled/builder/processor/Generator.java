package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

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
    private final Set<PropertyMethod> properties;

    public Generator(GenerationInfo generationInfo, Writer writer) {
        this.prototypeName = generationInfo.prototypeName();
        this.packageName = generationInfo.packageName();
        this.superTypeName = generationInfo.superTypeName();
        this.builderName = generationInfo.builderName();
        this.writer = writer;
        this.builderGenerator = new BuilderGenerator(generationInfo, writer);
        this.implGenerator = new ImplementationGenerator(generationInfo, writer);
        this.properties = generationInfo.properties();
    }

    void generate() throws IOException {
        this.writer.write("package " + packageName + ";\n\n");

        this.writer.write("import " + PROTOTYPE + ";\n");

        this.writer.write("import java.util.Objects;\n\n\n");

        importCollection();

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

    private void importCollection() throws IOException {
        //import List
        boolean importList = properties.stream()
                .map(PropertyMethod::type)
                .anyMatch(TypeName::isList);
                
        if(importList){
            importLists();
        }
        boolean importSet = properties.stream()
                .map(PropertyMethod::type)
                .anyMatch(TypeName::isSet);
        if(importSet){
            importSets();
        }
        
        writer.write("\n\n");

    }

    private void importLists() throws IOException {
        writer.write("""
                     import java.util.List;
                     import java.util.ArrayList;                     
                     """);
    }
    private void importSets() throws IOException {
        writer.write("""
                     import java.util.Set;
                     import java.util.LinkedHashSet;                     
                     """);
    }
}
