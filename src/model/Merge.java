package model;

public class Merge extends Action {
    private static final long serialVersionUID = 1L;
    private final User merger;
    private final PullRequest pr;

    public Merge(User merger, PullRequest pr) {
        this.merger = merger;
        this.pr = pr;
    }

    @Override
    public String describe() {
        return "" + merger.getName() + " merged PR '" + pr.getTitle() + "'";
    }
}