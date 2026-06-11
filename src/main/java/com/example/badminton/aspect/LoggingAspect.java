package com.example.badminton.aspect;

import com.example.badminton.dto.request.BookingRequest;
import com.example.badminton.dto.response.BookingResponse;
import com.example.badminton.repository.CourtRepository;
import com.example.badminton.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final Logger auditLogger = LoggerFactory.getLogger(LoggingAspect.class);

    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Pointcut("execution(* com.example.badminton.service.BookingService.createBooking(..))")
    public void bookingCreationPointcut() {
    }

    @AfterReturning(pointcut = "bookingCreationPointcut()", returning = "result")
    public void logSuccessfulBooking(JoinPoint joinPoint, BookingResponse result) {
        String customerName = resolveCustomerName(joinPoint);
        auditLogger.info("[AUDIT - SUCCESS] Khách hàng {} đặt thành công Sân số {} vào ngày {}, Khung giờ {}.",
                customerName,
                result.getCourtId(),
                result.getBookingDate(),
                result.getTimeSlotLabel());
    }

    @AfterThrowing(pointcut = "bookingCreationPointcut()", throwing = "exception")
    public void logFailedBooking(JoinPoint joinPoint, Exception exception) {
        String customerName = resolveCustomerName(joinPoint);
        BookingRequest request = extractBookingRequest(joinPoint);

        String courtLabel = request != null
                ? courtRepository.findById(request.getCourtId())
                        .map(court -> String.valueOf(court.getId()))
                        .orElse("N/A")
                : "N/A";

        String timeSlotLabel = request != null
                ? timeSlotRepository.findById(request.getTimeSlotId())
                        .map(slot -> slot.getLabel())
                        .orElse("N/A")
                : "N/A";

        auditLogger.warn("[AUDIT - FAILED] Khách hàng {} cố gắng đặt Sân số {} nhưng thất bại do {}.",
                customerName,
                courtLabel,
                exception.getMessage());
    }

    private String resolveCustomerName(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof String username) {
            return username;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }

        return "Unknown";
    }

    private BookingRequest extractBookingRequest(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1 && args[1] instanceof BookingRequest request) {
            return request;
        }
        return null;
    }
}
