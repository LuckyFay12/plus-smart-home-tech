package ru.yandex.practicum.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {
    String value() default "";

    boolean logParameters() default true;

    boolean logResult() default true;

    LogLevel level() default LogLevel.INFO;

    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}