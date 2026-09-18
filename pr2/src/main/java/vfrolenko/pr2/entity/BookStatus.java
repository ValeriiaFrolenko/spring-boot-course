package vfrolenko.pr2.entity;

import java.util.Set;
import java.util.Map;

public enum BookStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    private static final Map<BookStatus, Set<BookStatus>> ALLOWED_TRANSITIONS = Map.of(
            DRAFT,      Set.of(PUBLISHED),
            PUBLISHED,  Set.of(ARCHIVED),
            ARCHIVED,   Set.of()
    );

    public boolean canTransitionTo(BookStatus target) {
        return ALLOWED_TRANSITIONS.get(this).contains(target);
    }
}