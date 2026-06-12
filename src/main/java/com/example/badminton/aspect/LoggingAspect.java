package com.example.badminton.aspect;

import com.example.badminton.service.BookingService;
import com.example.badminton.service.UserService;
import com.example.badminton.service.AuthService;
import com.example.badminton.service.CourtService;
import com.example.badminton.service.TimeSlotService;
import com.example.badminton.service.CloudStorageService;
import com.example.badminton.service.CourtImageService;
import com.example.badminton.service.FileService;
import com.example.badminton.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.badminton.service.*.*(..))")
    public void serviceMethods() {
    }

    @Around("serviceMethods()")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        String username = resolveUsername();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[SERVICE - SUCCESS] Method: {} | User: {} | Duration: {}ms",
                    methodName, username, duration);
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("[SERVICE - FAILED] Method: {} | User: {} | Duration: {}ms | Error: {}",
                    methodName, username, duration, ex.getMessage());
            throw ex;
        }
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
