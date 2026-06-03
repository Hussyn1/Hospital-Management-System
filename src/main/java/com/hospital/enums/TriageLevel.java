package com.hospital.enums;

public enum TriageLevel {
    CRITICAL(1),
    HIGH(2),
    MEDIUM(3),
    LOW(4);

    private final int priority;

    TriageLevel(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
