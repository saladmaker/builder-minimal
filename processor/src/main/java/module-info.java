module builder.processor {
    requires java.compiler;
    requires io.helidon.common.processor;
    provides javax.annotation.processing.Processor with
        khaled.builder.processor.BuilderProcessor;
}
