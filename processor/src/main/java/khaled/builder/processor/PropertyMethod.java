package khaled.builder.processor;

import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypedElementInfo;

public record PropertyMethod(String name, TypeName type) {

    public static PropertyMethod create(TypedElementInfo tei) {

        TypeName returnType = tei.typeName();

        String name = tei.elementName();

        return new PropertyMethod(name, returnType);
    }
}
