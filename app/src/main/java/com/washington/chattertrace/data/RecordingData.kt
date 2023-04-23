package com.washington.chattertrace.data

import java.time.LocalDate
import java.time.LocalTime

data class RecordingFolder(val id: Int, val date: LocalDate, val duration: LocalTime,
                           val numRecordings: Int, val isUploaded: Boolean, val recording: Recording)

data class Recording(val id: Int);

val dummyData: List<RecordingFolder> = mutableListOf(
    RecordingFolder(0,
        LocalDate.of(2023, 4, 20),
        LocalTime.of(0, 0, 0),
        4,
        true,
        Recording(0)
    ),
    RecordingFolder(1,
        LocalDate.of(2023, 4, 19),
        LocalTime.of(0, 0, 0),
        4,
        true,
        Recording(1)),
    RecordingFolder(2,
        LocalDate.of(2023, 4, 18),
        LocalTime.of(0, 0, 0),
        4,
        false,
        Recording(2)),
    RecordingFolder(3,
        LocalDate.of(2023, 4, 17),
        LocalTime.of(0, 0, 0),
        4,
        false,
        Recording(3)),
    RecordingFolder(4,
        LocalDate.of(2023, 4, 16),
        LocalTime.of(0, 0,0),
        4,
        false,
        Recording(4))
);