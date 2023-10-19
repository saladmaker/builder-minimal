package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import static khaled.builder.processor.GenerationInfo.INDENTATION;

/**
 *
 * @author khaled
 */
public record CollectionTypeHandler(PropertyMethod property, Type collectionType) implements TypeHandler {
    
    enum Type {
        SET {
            @Override
            String initialization() {
                return "new LinkedHashSet<>(Set.of(%1$s))";
            }

        },
        LIST {
            @Override
            String initialization() {
                return "new ArrayList<>(List.of(%1$s))";
            }

        };

        abstract String initialization();
    }

    static CollectionTypeHandler create(PropertyMethod property) {
        if (property.type().isList()) {
            return new CollectionTypeHandler(property, Type.LIST);
        }
        if (property.type().isSet()) {
            return new CollectionTypeHandler(property, Type.SET);
        }
        throw new IllegalStateException("unkown collection type");
    }



    @Override
    public void generateBuilderMutators(Writer writer, String builderName, int indentationLevel) throws IOException {
        String name = property.name();
        generateAddCollection(writer, builderName, indentationLevel, name, true, false);
        generateAddCollection(writer, builderName, indentationLevel, "add" + capitalize(name), false, false);
        String sigular = property.singlar();
        if (null != sigular) {
            if (sigular.isBlank()) {
                generateAddCollection(writer, builderName, indentationLevel,  "add" + capitalize(singular(name)), false, true);
            } else {
                generateAddCollection(writer, builderName, indentationLevel, sigular, false, true);
            }
        }
    }
    private void generateAddCollection(Writer writer,String builderName, int indentationLevel, String methodName, boolean clear, boolean singular) throws IOException{
        String mutatorDeclarationPrefix = INDENTATION.repeat(indentationLevel) + "public ";
        String builderType = builderName;
        String propertyName = property.name();
        String paramName;
        String paramType;
        if (singular) {
            paramType = property.type().typeArguments().getFirst().className();
            paramName = singular(propertyName);

        } else {
            paramType = covary(property.type());
            paramName = propertyName;
        }
        if (null == paramType) {
            String message = """
                          paramType is null method name:%1$s, clear:%2$s, singular:%3$s
                          property method:%4$s
                          """.formatted(methodName, clear, singular, property);
            throw new IllegalStateException(message);
        }
        String mutatorDeclaration = mutatorDeclarationPrefix + builderType + " " + methodName + "(final "
                + paramType + " " + paramName + "){\n";

        writer.write(mutatorDeclaration);

        String requireNonNull = INDENTATION.repeat(indentationLevel + 1) + "Objects.requireNonNull(" + paramName + ");\n";
        writer.write(requireNonNull);

        if (clear) {
            String clearCollection = INDENTATION.repeat(indentationLevel + 1) + "this." + propertyName + ".clear();\n";
            writer.write(clearCollection);
        }
        if (singular) {
            String add = INDENTATION.repeat(indentationLevel + 1 ) + "this." + propertyName + ".add(" + paramName + ");\n";
            writer.write(add);

        } else {
            String addAll = INDENTATION.repeat(indentationLevel + 1) + "this." + propertyName + ".addAll(" + paramName + ");\n";
            writer.write(addAll);
        }
        String returSelf = INDENTATION.repeat(indentationLevel + 1) + "return self();\n";

        writer.write(returSelf);
        writer.write(INDENTATION.repeat(indentationLevel) + "}\n\n");
    }
    private String covary(TypeName type) {
        List<TypeName> arguments = type.typeArguments()
                .stream()
                .map(it -> TypeName.builder(it).wildcard(true).build())
                .collect(Collectors.toList());
        String name = type.className();
        if (!arguments.isEmpty()) {
            String paramFormat = "<%1$s>";
            String tokens = arguments.stream()
                    .map(it -> (it.wildcard() ? "? extends " : "") + it.className())
                    .collect(Collectors.joining(", "));
            String params = paramFormat.formatted(tokens);
            name = name + params;

        }
        return name;
    }
    private static String capitalize(String name) {
        if (!name.isBlank()) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;

    }

    private static String singular(String name) {
        if (name.length() > 1 && name.endsWith("s")) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }
}
