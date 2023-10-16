package khaled.builder.processor;

import io.helidon.common.processor.TypeInfoFactory;
import io.helidon.common.types.TypeInfo;
import io.helidon.common.types.TypeName;
import io.helidon.common.types.TypeNames;
import io.helidon.common.types.TypedElementInfo;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class BuilderProcessor extends AbstractProcessor {

    private static final System.Logger LOGGER = System.getLogger(BuilderProcessor.class.getName());
    private static final String BLUEPRINT_TYPE = "io.helidon.builder.api.Prototype.Blueprint";

    private TypeElement blueprintElementType;
    private Elements elementUtil;
    private Messager messager;
    private Filer filer;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(BLUEPRINT_TYPE);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtil = processingEnv.getElementUtils();
        this.blueprintElementType = elementUtil.getTypeElement(BLUEPRINT_TYPE);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(blueprintElementType);
        Set<? extends TypeElement> blueprints = validateBlueprintsTypes(elements);
        for (var blueprint : blueprints) {
            process(blueprint);
        }
        return true;
    }

    private void process(TypeElement blueprint) {
        TypeInfo typeInfo = (TypeInfo) TypeInfoFactory.create(processingEnv, blueprint, new TypedElementInfoPredicate())
                .orElseThrow(
                        () -> new IllegalStateException("cannot create type info for blueprint: " + blueprint));
        if (validateBlueprintName(typeInfo.typeName())) {
            typeInfo.elementInfo()
                    .stream()
                    .filter(Predicate.not(this::validatePropertyMethod))
                    .findAny()
                    .ifPresent(it -> System.out.println("error" + String.valueOf(it)));
            GenerationInfo generationInfo = GenerationInfo.create(typeInfo);
            String sourceName = TypeName.builder(typeInfo.typeName())
                    .className(generationInfo.prototypeName()).build().resolvedName();
            generate(generationInfo, sourceName, blueprint);
        }
    }

    private Set<? extends TypeElement> validateBlueprintsTypes(Set<? extends Element> blueprints) {
        Set<TypeElement> result = new LinkedHashSet<>();
        for (Element blueprint : blueprints) {
            if (blueprint.getKind() != ElementKind.INTERFACE) {
                messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        BLUEPRINT_TYPE + " can only be defined on an interface",
                        blueprint);
            }
            result.add((TypeElement) blueprint);
        }
        return result;
    }

    private boolean validateBlueprintName(TypeName blueprintTypeName) {
        if (!blueprintTypeName.className().endsWith("Blueprint")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    BLUEPRINT_TYPE + " blueprint must have suffix 'Blueprint': "
                    + blueprintTypeName.resolvedName());
            return false;
        }
        return true;
    }

    private boolean validatePropertyMethod(TypedElementInfo tei) {
        TypeName returnType = tei.typeName();
        boolean result = validateType(returnType);
        if (!result) {
            String methodName = tei.elementName();
            String typeName = tei.enclosingType().map(TypeName::resolvedName).orElse("");
            String declaration = typeName + "::" + methodName;
            messager.printMessage(Diagnostic.Kind.ERROR,
                    BLUEPRINT_TYPE + " invalid return type " + declaration + "return type: " + returnType
                            .resolvedName());
            return false;
        }
        return true;
    }

    private boolean validateType(TypeName type) {
        if(type.isList() || type.isSet()){
            return validateSimpleType(type.typeArguments().get(0));
        }
        return validateSimpleType(type);
    }

    private boolean validateSimpleType(TypeName type) {
        return TypeNames.STRING.equals(type)
                || TypeNames.BOXED_BOOLEAN.equals(type)
                || TypeNames.BOXED_BYTE.equals(type)
                || TypeNames.BOXED_CHAR.equals(type)
                || TypeNames.BOXED_DOUBLE.equals(type)
                || TypeNames.BOXED_FLOAT.equals(type)
                || TypeNames.BOXED_INT.equals(type)
                || TypeNames.BOXED_LONG.equals(type)
                || TypeNames.BOXED_SHORT.equals(type)
                || (type.primitive() && !type.array());
    }

    private void generate(GenerationInfo generationInfo, String sourceName, TypeElement blueprint) {
        try {

            JavaFileObject sourceFile = this.filer.createSourceFile(sourceName, blueprint);

            try (Writer writer = sourceFile.openWriter()) {

                Generator generator = new Generator(generationInfo, writer);
                generator.generate();
            }

        } catch (IOException ex) {
            LOGGER.log(System.Logger.Level.ERROR, ex.getMessage());
        }
    }
}
