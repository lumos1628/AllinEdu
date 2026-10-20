package com.edufast.application.dto;

import com.edufast.domain.model.SessionEdit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SessionUpdateRequest(
        @NotBlank @Size(max = 240) String title,
        String purpose,
        Integer durationMinutes,
        String evidence,
        @NotNull @Size(max = 40) List<@Valid Activity> activities) {

    public record Activity(Long id, @NotBlank String moment, @Size(max = 180) String title,
                           @NotBlank String instructions, Integer minutes) {
    }

    public SessionEdit toDomain() {
        return new SessionEdit(title.trim(), blankToNull(purpose), durationMinutes, blankToNull(evidence),
                activities.stream()
                        .map(a -> new SessionEdit.ActivityEdit(a.id(), a.moment(), blankToNull(a.title()),
                                a.instructions().trim(), a.minutes()))
                        .toList());
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
