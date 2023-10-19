package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import static khaled.builder.processor.GenerationInfo.PROTOTYPE;
import static khaled.builder.processor.GenerationInfo.INDENTATION;

public final class PrototypeGenerator implements Generator{

    private final String superTypeName;

    private final String prototypeName;

    private final String packageName;

    private final Writer writer;

    private final BuilderGenerator builderGenerator;
    private final ImplementationGenerator implGenerator;

    private final String builderName;
    private final Set<TypeHandler> typeHandlers;

    public PrototypeGenerator(GenerationInfo generationInfo, Writer writer) {
        this.prototypeName = generationInfo.prototypeName();
        this.packageName = generationInfo.packageName();
        this.superTypeName = generationInfo.superTypeName();
        this.builderName = generationInfo.builderName();
        this.writer = writer;
        this.builderGenerator = new BuilderGenerator(generationInfo, writer);
        this.implGenerator = new ImplementationGenerator(generationInfo, writer);
        this.typeHandlers = generationInfo.typeHandlers();
    }

    @Override
    public void generate() throws IOException {
        this.writer.write("package " + packageName + ";\n\n");

        this.writer.write("import " + PROTOTYPE + ";\n");

        this.writer.write("import java.util.Objects;\n\n\n");

        importCollectionAndOptional();

        this.writer.write("public interface " + prototypeName + " extends " + superTypeName + "{\n\n\n");

        generateBuilderMethod();

        builderGenerator.generate();
        implGenerator.generate();
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

    private void importCollectionAndOptional() throws IOException {
        //import List
        boolean importList = typeHandlers.stream()
                .map(TypeHandler::type)
                .anyMatch(TypeName::isList);
                
        if(importList){
            importLists();
        }
        boolean importSet = typeHandlers.stream()
                .map(TypeHandler::type)
                .anyMatch(TypeName::isSet);
        if(importSet){
            importSets();
        }
        boolean importOptional = typeHandlers.stream()
                .map(TypeHandler::type)
                .anyMatch(TypeName::isOptional);
        if(importOptional){
            importOptional();
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
    private void importOptional() throws IOException {
        writer.write("""
                     import java.util.Optional;\n
                     """);
    }
}
