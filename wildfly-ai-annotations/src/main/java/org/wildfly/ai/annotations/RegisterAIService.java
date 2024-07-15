package org.wildfly.ai.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.util.Nonbinding;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
public @interface RegisterAIService {

    Class<? extends Annotation> scope() default RequestScoped.class;

    Class<?>[] tools() default {};

    int chatMemoryMaxMessages() default 10;

    @Nonbinding
    String chatLanguageModelName() default "";

    @Nonbinding
    String embeddingModelName() default "";

    @Nonbinding
    String embeddingStoreName() default "";

    @Nonbinding
    String contentRetrieverName() default "";
}
