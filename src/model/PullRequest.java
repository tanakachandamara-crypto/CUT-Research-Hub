package model;

import java.io.Serializable;
import java.util.UUID;

public class PullRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private final String title;
    private final Student author;
    private final HubRepository target;

    private Paper proposedPaper;
    private String status;

    public PullRequest(String title, Student author, HubRepository target) {
        this.title = title;
        this.author = author;
        this.target = target;
    }

    public PullRequest(String title, Student author, HubRepository target, Paper paper) {
        this(title, author, target);
        proposedPaper = paper;
        status = "OPEN";
    }

    public Paper getProposedPaper() { return proposedPaper; }
    public String getStatus() { return status == null ? "OPEN" : status; }
    public boolean isOpen() { return "OPEN".equals(getStatus()); }
    public void close(boolean accepted) {
        if (!isOpen()) throw new IllegalStateException("This request has already been reviewed.");
        status = accepted ? "MERGED" : "REJECTED";
    }
    @Override public String toString() { return title + " — " + author.getName() + " [" + getStatus() + "]"; }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public Student getAuthor() { return author; }
    public HubRepository getTarget() { return target; }
}