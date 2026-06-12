package com.example.badminton.service;

import com.example.badminton.dto.request.TimeSlotCreateRequest;
import com.example.badminton.dto.request.TimeSlotUpdateRequest;
import com.example.badminton.dto.response.TimeSlotResponse;
import com.example.badminton.entity.TimeSlot;
import com.example.badminton.exception.BadRequestException;
import com.example.badminton.exception.ResourceNotFoundException;
import com.example.badminton.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public List<TimeSlotResponse> getAllTimeSlots() {
        return timeSlotRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TimeSlotResponse getTimeSlotById(Long id) {
        TimeSlot ts = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
        return toResponse(ts);
    }

    @Transactional
    public TimeSlotResponse createTimeSlot(TimeSlotCreateRequest request) {
        validateTimeRange(request.getStartTime(), request.getEndTime());
        TimeSlot ts = TimeSlot.builder()
                .label(request.getLabel())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
        return toResponse(timeSlotRepository.save(ts));
    }

    @Transactional
    public TimeSlotResponse updateTimeSlot(Long id, TimeSlotUpdateRequest request) {
        TimeSlot ts = timeSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
        validateTimeRange(request.getStartTime(), request.getEndTime());
        ts.setLabel(request.getLabel());
        ts.setStartTime(request.getStartTime());
        ts.setEndTime(request.getEndTime());
        return toResponse(timeSlotRepository.save(ts));
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new ResourceNotFoundException("Time slot not found");
        }
        timeSlotRepository.deleteById(id);
    }

    private void validateTimeRange(java.time.LocalTime start, java.time.LocalTime end) {
        if (end.isBefore(start) || end.equals(start)) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    public TimeSlotResponse toResponse(TimeSlot ts) {
        return TimeSlotResponse.builder()
                .id(ts.getId())
                .label(ts.getLabel())
                .startTime(ts.getStartTime())
                .endTime(ts.getEndTime())
                .build();
    }
}
