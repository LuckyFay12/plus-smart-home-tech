package ru.yandex.practicum.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("@annotation(logExecution)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint, LogExecution logExecution) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        // Логируем вход в метод
        logAtLevel(logExecution.level(),
                "Entering method: {}.{}() with arguments: {}",
                className, methodName,
                logExecution.logParameters() ? Arrays.toString(joinPoint.getArgs()) : "[hidden]"
        );

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();

            // Логируем успешное выполнение
            logAtLevel(logExecution.level(),
                    "Exiting method: {}.{}() with result: {} - execution time: {} ms",
                    className, methodName,
                    logExecution.logResult() ? result : "[hidden]",
                    stopWatch.getTotalTimeMillis()
            );

            return result;

        } catch (Exception e) {
            stopWatch.stop();

            log.error("Exception in method: {}.{}() - {} - execution time: {} ms",
                    className, methodName, e.getMessage(), stopWatch.getTotalTimeMillis(), e);
            throw e;
        }
    }

    private void logAtLevel(LogExecution.LogLevel level, String message, Object... args) {
        switch (level) {
            case DEBUG -> log.debug(message, args);
            case INFO -> log.info(message, args);
            case WARN -> log.warn(message, args);
            case ERROR -> log.error(message, args);
        }
    }
}