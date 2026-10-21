package com.edufast.application.dto;

import com.edufast.domain.model.LearningUnit;
import com.edufast.domain.model.ScheduleBlock;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record WeekResponse(
        Long sectionId,
        LocalDate weekStart,
        LocalDate today,
        List<Block> blocks,
        Unit unit) {

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("H:mm");

    public record Block(Long slotId, int dayOfWeek, LocalDate date, String start, String end, String area,
                        boolean mine, Session session) {

        public static Block from(ScheduleBlock b, LocalDate weekStart) {
            var s = b.session();
            return new Block(b.slotId(), b.dayOfWeek(), weekStart.plusDays(b.dayOfWeek() - 1L),
                    b.start().format(HOUR), b.end().format(HOUR), b.area(), b.taughtByTeacher(),
                    s == null ? null : new Session(s.id(), s.unitId(), s.title(), "PUBLICADA".equals(s.status()),
                            s.activities()));
        }
    }

    /** La sesión asignada al bloque en esa fecha. activities = 0 significa "sin secuencia". */
    public record Session(Long id, Long unitId, String title, boolean approved, int activities) {
    }

    public record Unit(Long id, String title, String situation, LocalDate startDate, LocalDate endDate,
                       int approvedSessions, int totalSessions) {

        public static Unit from(LearningUnit u) {
            return new Unit(u.id(), u.title(), u.situation(), u.startDate(), u.endDate(),
                    u.approvedSessions(), u.totalSessions());
        }
    }
}
