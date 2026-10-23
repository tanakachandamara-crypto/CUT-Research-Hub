package model;

/** A simple history entry for actions other than commits and reviews. */
public class Activity extends Action {
    private static final long serialVersionUID = 1L;
    private final String message;
    public Activity(String message) { this.message = message; }
    @Override public String describe() { return message; }
}
