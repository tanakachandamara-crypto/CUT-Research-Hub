package model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String n, String e, String p) {
        super(n, e, p);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public boolean canCreateRepo() {
        return true;
    }

    @Override
    public boolean canMerge(PullRequest pr) {
        return true;
    }

    @Override
    public int getMaxRepos() {
        return Integer.MAX_VALUE;
    }
}