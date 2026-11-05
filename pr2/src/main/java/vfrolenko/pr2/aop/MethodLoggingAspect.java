package vfrolenko.pr2.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class MethodLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(MethodLoggingAspect.class);

    @Around("@annotation(logMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint, LogMethodCall logMethodCall) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        String operation = logMethodCall.value().isBlank() ? signature : logMethodCall.value();

        log.info("-> [{}] args={}", operation, Arrays.toString(joinPoint.getArgs()));

        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.info("<- [{}] completed in {} ms, result={}", operation, durationMs, result);
            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            log.error("!! [{}] failed in {} ms: {}", operation, durationMs, ex.getMessage());
            throw ex;
        }
    }
}