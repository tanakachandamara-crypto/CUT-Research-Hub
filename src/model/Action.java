package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Action implements Serializable {
    private static final long serialVersionUID = 1L;
    private final LocalDateTime when = LocalDateTime.now();
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public abstract String describe();

    @Override
    public String toString() {
        return "[" + DTF.format(when) + "] " + describe();
    }
}