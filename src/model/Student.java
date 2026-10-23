package model;

public class Student extends User {
    private static final long serialVersionUID = 1L;

    public Student(String n, String e, String p) {
        super(n, e, p);
    }

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public boolean canCreateRepo() {
        return false;
    }

    @Override
    public boolean canMerge(PullRequest pr) {
        return false;
    }

    @Override
    public int getMaxRepos() {
        return 5;
    }
}