package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static khaled.builder.processor.GenerationInfo.INDENTATION;
import static khaled.builder.processor.GenerationInfo.PROTOTYPE_BUILDER;
import static khaled.builder.processor.GenerationInfo.COMMON_BUILDER;
import static khaled.builder.processor.GenerationInfo.CHECKER_SUFFIX;

/**
 *
 * @author khaled
 */
public class BuilderGenerator {

    private final Writer writer;
    private final String builderName;
    private final String prototypeName;
    private final String implName;
    private final Set<PropertyMethod> properties;

    private final Map<String, String> propertyCheckers = new LinkedHashMap<>();

    BuilderGenerator(GenerationInfo generationInfo, Writer writer) {
        this.writer = writer;
        this.builderName = generationInfo.builderName();
        this.prototypeName = generationInfo.prototypeName();
        this.implName = generationInfo.implName();
        this.properties = generationInfo.properties();

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
        for (var property : properties) {
            TypeName type = property.type();

            String name = property.name();

            String className = type.classNameWithTypes();

            String literalValue = "";
            String finalModifier = "";
            final String defaultValue = property.defaultValue();
            if (null != defaultValue || property.collectionBased()) {
                if (property.collectionBased()) {
                    finalModifier = "final ";
                }
                literalValue = generateLiteralDefaultValue(defaultValue, type.boxed());
                literalValue = " = " + literalValue;
            }
            String propertyDeclaration = propertyDeclarationPrefix + finalModifier + className + " " + name;

            propertyDeclaration = propertyDeclaration + literalValue + ";\n";
            writer.write(propertyDeclaration);

        }

    }

    private String generateLiteralDefaultValue(String defaultValue, TypeName type) {
        String name = type.className();

        /*
        using the compiler and the javac jvm process as means of validation
            ValueOf will validate the String
            single quotation with validate the char
         */
        return switch (name) {
            case null -> throw new IllegalStateException("null default value");

            case "String" -> "\"" + defaultValue + "\"";

            case "Boolean" -> Boolean.valueOf(defaultValue).toString();

            case "Byte" -> Byte.valueOf(defaultValue).toString();

            case "Character" -> "\'" + defaultValue + "\'";

            case "Double" -> Double.valueOf(defaultValue).toString();

            case "Float" -> Float.valueOf(defaultValue).toString();

            case "Integer" -> Integer.valueOf(defaultValue).toString();

            case "Long" -> Long.valueOf(defaultValue).toString();

            case "Short" -> Short.valueOf(defaultValue).toString();

            case "List" -> "new ArrayList<>()";

            case "Set" -> "new LinkedHashSet<>()";    

            default -> throw new IllegalStateException("unkown type defaulted");
        };
    }

    private void generateCheckers() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//checkers\n");
        String propertyCheckerPrefix = INDENTATION.repeat(2) + "private boolean ";
        for (var property : properties) {
            if (null == property.defaultValue() && !property.collectionBased()) {
                String name = property.name();

                String checker = property.name() + CHECKER_SUFFIX;

                if (null != propertyCheckers.put(name, checker)) {
                    throw new IllegalStateException("duplicate property" + name);

                }

                String propertyCheckerDeclaration = propertyCheckerPrefix + checker + " = false;\n";
                writer.write(propertyCheckerDeclaration);

            }
        }
    }

    private void generateMutators() throws IOException {
        writer.write("\n\n" + INDENTATION.repeat(2) + "//mutators\n");
        String mutatorDeclarationPrefix = INDENTATION.repeat(2) + "public ";
        for (var property : properties) {
            String name = property.name();
            String builderType = builderName;
            String paramType = covary(property.type());

            String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + name + "(final "
                    + paramType + " " + name + "){\n";

            writer.write(mutatorDeclaration);

            String mutatorBody;
            if (property.type().primitive()) {

                mutatorBody = INDENTATION.repeat(3) + "this." + name + " = " + name + ";\n";

            } else {
                String requireNonNull = "Objects.requireNonNull(" + name + ");";
                if (property.collectionBased()) {
                    mutatorBody = INDENTATION.repeat(3) + requireNonNull + "\n"
                            + INDENTATION.repeat(3) + "this." + name + ".clear();\n"
                            + INDENTATION.repeat(3) + "this." + name + ".addAll(" + name + ");\n";

                } else {

                
                    mutatorBody = INDENTATION.repeat(3) + "this." + name + " = " + requireNonNull + "\n";
                }

            }
            writer.write(mutatorBody);

            // set checker
            String checker = propertyCheckers.get(name);
            if (null != checker) {
                String setChecker = INDENTATION.repeat(3) + "this." + checker + " = true;\n";
                writer.write(setChecker);
            }
            String returSelf = INDENTATION.repeat(3) + "return self();\n";
            writer.write(returSelf);
            writer.write(INDENTATION.repeat(2) + "}\n\n");
        }
    }

    private String covary(TypeName type) {
        List<TypeName> arguments = type.typeArguments()
                .stream()
                .map(it -> TypeName.builder(it).wildcard(true).build())
                .collect(Collectors.toList());
        String name = type.className();
        if(!arguments.isEmpty()){
            String paramFormat = "<%1$s>";
            String tokens = arguments.stream()
                    .map(it-> (it.wildcard()? "? extends " : "") + it.className())
                    .collect(Collectors.joining(", "));
            String params = paramFormat.formatted(tokens);
            name = name + params;
            
        }
        return name;
    }

    private void generateAccessors() throws IOException {
        String accessorDeclarationPrefix = INDENTATION.repeat(2) + "public ";
        for (var property : properties) {
            String type = property.type().classNameWithTypes();
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
        for (var propertyName : propertyCheckers.keySet()) {

            String message = propertyName + " must be set before building";
            String checker = propertyCheckers.get(propertyName);
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
