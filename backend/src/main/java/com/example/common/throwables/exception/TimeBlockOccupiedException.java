package com.example.common.throwables.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeBlockOccupiedException extends RuntimeException {

    public TimeBlockOccupiedException(LocalDate date, LocalTime timeStart, LocalTime timeEnd) {
        super("A timeblock between " + timeStart + " and " + timeEnd + " on " + date + " is already occupied. Please shoose different time or date.");
    }
}
