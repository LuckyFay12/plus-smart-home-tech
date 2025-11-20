package ru.yandex.practicum.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("execution(* ru.yandex.practicum..*ServiceImpl.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        String fullMethodName = className + "." + methodName;

        if (log.isDebugEnabled()) {
            log.debug("▶️ SERVICE START: {}() with args: {}",
                    fullMethodName, Arrays.toString(joinPoint.getArgs()));
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();

            // INFO только для медленных методов (>100ms)
            if (stopWatch.getTotalTimeMillis() > 100) {
                log.warn("🟡 SERVICE SLOW: {}() - time: {}ms",
                        fullMethodName, stopWatch.getTotalTimeMillis());
            } else if (log.isDebugEnabled()) {
                log.debug("✅ SERVICE SUCCESS: {}() - time: {}ms",
                        fullMethodName, stopWatch.getTotalTimeMillis());
            }

            return result;

        } catch (Exception e) {
            stopWatch.stop();
            log.error("❌ SERVICE ERROR: {}() - time: {}ms - error: {}",
                    fullMethodName, stopWatch.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
}