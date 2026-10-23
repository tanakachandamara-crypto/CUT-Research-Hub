package model;

public class Commit extends Action {
    private static final long serialVersionUID = 1L;
    private final Student author;
    private final String message;

    public Commit(Student author, String msg) {
        this.author = author;
        this.message = msg;
    }

    public Student getAuthor() { return author; }

    @Override
    public String describe() {
        return "" + author.getName() + " committed: " + message;
    }
}