package model;

public class Review extends Action {
    private static final long serialVersionUID = 1L;
    private final User reviewer;
    private final PullRequest pr;
    private final boolean accepted;

    public Review(User reviewer, PullRequest pr, boolean accepted) {
        this.reviewer = reviewer;
        this.pr = pr;
        this.accepted = accepted;
    }

    public boolean isAccepted() { return accepted; }

    @Override
    public String describe() {
        String status = accepted ? "accepted" : "rejected";
        return reviewer.getName() + " " + status + " PR '" + pr.getTitle() + "'";
    }
}