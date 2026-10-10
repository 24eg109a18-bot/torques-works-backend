package com.garage.management.entity;

import java.util.EnumSet;
import java.util.Set;

/** Life-cycle of a service request. The allowed transitions are enforced in one place: next(). */
public enum ServiceStatus {
    PENDING, APPROVED, ASSIGNED, IN_PROGRESS, WAITING_FOR_PARTS, COMPLETED, CANCELLED;

    public Set<ServiceStatus> next() {
        return switch (this) {
            case PENDING -> EnumSet.of(APPROVED, CANCELLED);
            case APPROVED -> EnumSet.of(ASSIGNED, CANCELLED);
            case ASSIGNED -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(WAITING_FOR_PARTS, COMPLETED, CANCELLED);
            case WAITING_FOR_PARTS -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case COMPLETED, CANCELLED -> EnumSet.noneOf(ServiceStatus.class);
        };
    }

    public boolean canMoveTo(ServiceStatus target) {
        return next().contains(target);
    }
}
