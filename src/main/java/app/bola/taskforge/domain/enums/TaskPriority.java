package app.bola.taskforge.domain.enums;

public enum TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public boolean isEscalatedFrom(TaskPriority other) {
        return this.ordinal() > other.ordinal();
    }
}
