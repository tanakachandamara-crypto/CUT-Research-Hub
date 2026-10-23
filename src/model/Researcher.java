package model;

public class Researcher extends User {
    private static final long serialVersionUID = 1L;

    public Researcher(String n, String e, String p) {
        super(n, e, p);
    }

    @Override
    public String getRole() {
        return "Researcher";
    }

    @Override
    public boolean canCreateRepo() {
        return true;
    }

    @Override
    public boolean canMerge(PullRequest pr) {
        return pr != null && pr.getTarget().getOwner().equals(this);
    }

    @Override
    public int getMaxRepos() {
        return 20;
    }
}