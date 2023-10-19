package khaled.builder.processor;

import io.helidon.common.types.Annotation;
import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypedElementInfo;
import java.util.List;

public record PropertyMethod(String name, TypeName type, String defaultValue, String singlar, TypedElementInfo typedInfo) {

    static final String OPTION_DEFAULT = "io.helidon.builder.api.Option.Default";
    static final TypeName OPTION_DEFAULT_TYPE = TypeName.create(OPTION_DEFAULT);
    
    static final String OPTION_SINGULAR = "io.helidon.builder.api.Option.Singular";
    static final TypeName OPTION_SINGULAR_TYPE = TypeName.create(OPTION_SINGULAR);

    public boolean collectionBased() {
        return type.isList() || type.isSet();
    }
    
    public TypeHandler typeHandler(){
        if(collectionBased()){
            return CollectionTypeHandler.create(this);
        }if(type.isOptional()){
            return OptionalTypeHandler.create(this);
        }else{
            return new SimpleTypeHandler(this);
        }
    }
    
    public static PropertyMethod create(TypedElementInfo tei) {

        TypeName returnType = tei.typeName();

        String name = tei.elementName();

        String defaultValue = tei.findAnnotation(OPTION_DEFAULT_TYPE)
                .map(PropertyMethod::extractDefaultValue)
                .orElse(null);

        String singular = tei.findAnnotation(OPTION_SINGULAR_TYPE)
                .map(PropertyMethod::extractSingularValue)
                .orElse(null);

        return new PropertyMethod(name, returnType, defaultValue, singular, tei);
    }
    
    private static String extractDefaultValue(final Annotation annotation) {
        return annotation.stringValues()
                .stream()
                .flatMap(List::stream)
                .findFirst()
                .orElse(null);
    }

    private static String extractSingularValue(final Annotation annotation) {
        return annotation.stringValue()
                .orElse(null);
    }
}
