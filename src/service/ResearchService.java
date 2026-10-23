package service;

import core.Hub;
import model.*;
import java.util.List;

/** Business rules live here, keeping Swing event handlers small and testable. */
public class ResearchService {
    private final Hub hub;
    public ResearchService(Hub hub) { this.hub = hub; }

    private String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(field + " is required.");
        return value.trim();
    }
    private void member(User user) {
        if (user == null || !hub.getAllUsers().contains(user)) throw new IllegalArgumentException("Please sign in again.");
    }
    private void stored(HubRepository repo) {
        if (repo == null || hub.getRepo(repo.getId()) != repo) throw new IllegalArgumentException("Select a repository.");
    }
    private void quota(User owner) {
        long count = hub.getRepos().stream().filter(r -> r.getOwner().equals(owner)).count();
        if (count >= owner.getMaxRepos()) throw new IllegalArgumentException("Your repository limit is " + owner.getMaxRepos() + ".");
    }
    private void uniqueName(User owner, String name) {
        boolean exists = hub.getRepos().stream().anyMatch(r -> r.getOwner().equals(owner) && r.getName().equalsIgnoreCase(name));
        if (exists) throw new IllegalArgumentException("You already own a repository with that name.");
    }
    public HubRepository createRepository(User owner, String name, String description) {
        member(owner);
        if (!owner.canCreateRepo()) throw new IllegalArgumentException("Students contribute by forking an existing repository.");
        name = required(name, "Repository name");
        quota(owner); uniqueName(owner, name);
        HubRepository repo = new HubRepository(name, owner);
        repo.setDescription(description == null || description.isBlank() ? "No description" : description.trim());
        hub.addRepo(repo);
        hub.addAction(new Activity(owner.getName() + " created repository '" + name + "'."));
        return repo;
    }
    public HubRepository fork(User user, HubRepository original) {
        member(user); stored(original);
        if (!(user instanceof Student)) throw new IllegalArgumentException("Forking is a student action.");
        if (original.getOwner().equals(user)) throw new IllegalArgumentException("Select another user's repository to fork.");
        quota(user);
        String base = original.getName() + "-fork";
        String name = base;
        int suffix = 2;
        while (hasName(user, name)) name = base + "-" + suffix++;
        HubRepository fork = new HubRepository(name, user);
        fork.setSourceRepositoryId(original.getId());
        fork.setDescription(original.getDescription());
        // Paper is immutable through its public API; the snapshot can safely share paper IDs.
        for (Paper paper : original.getPapers()) fork.addPaper(paper);
        hub.addRepo(fork);
        hub.addAction(new Activity(user.getName() + " forked '" + original.getName() + "'."));
        return fork;
    }
    private boolean hasName(User user, String name) {
        return hub.getRepos().stream().anyMatch(r -> r.getOwner().equals(user) && r.getName().equalsIgnoreCase(name));
    }
    public Paper addPaper(User user, HubRepository repo, String title, String content) {
        member(user); stored(repo);
        if (!repo.getOwner().equals(user) && !(user instanceof Admin)) throw new IllegalArgumentException("Only the owner or an admin can add papers here.");
        Paper paper = new Paper(required(title, "Paper title"), required(content, "Paper content"));
        repo.addPaper(paper);
        if (user instanceof Student) hub.addAction(new Commit((Student) user, "Added '" + paper.getTitle() + "' to " + repo.getName()));
        else hub.addAction(new Activity(user.getName() + " added '" + paper.getTitle() + "' to " + repo.getName()));
        return paper;
    }
    public PullRequest openRequest(User user, HubRepository fork, Paper paper, String title) {
        member(user); stored(fork);
        if (!(user instanceof Student) || !fork.getOwner().equals(user)) throw new IllegalArgumentException("Select a paper from your own fork.");
        HubRepository target = hub.getRepo(fork.getSourceRepositoryId());
        if (target == null || target.getOwner() instanceof Student) throw new IllegalArgumentException("Fork a researcher or admin repository to submit a contribution.");
        if (!fork.getPapers().contains(paper)) throw new IllegalArgumentException("Select a paper from this fork.");
        if (containsPaper(target, paper)) throw new IllegalArgumentException("This paper is already in the original repository. Add a new paper first.");
        boolean pending = hub.getPullRequests().stream().anyMatch(pr -> pr.isOpen()
                && pr.getTarget().getId().equals(target.getId()) && pr.getProposedPaper() != null
                && pr.getProposedPaper().getId().equals(paper.getId()));
        if (pending) throw new IllegalArgumentException("This paper already has an open pull request.");
        PullRequest request = new PullRequest(required(title, "Request title"), (Student) user, target, paper);
        hub.addPullRequest(request);
        hub.addAction(new Activity(user.getName() + " opened pull request '" + request.getTitle() + "'."));
        return request;
    }
    private boolean containsPaper(HubRepository repo, Paper paper) {
        return repo.getPapers().stream().anyMatch(p -> p.getId().equals(paper.getId()));
    }
    public void review(User reviewer, PullRequest request, boolean accepted) {
        member(reviewer);
        if (request == null || !hub.getPullRequests().contains(request)) throw new IllegalArgumentException("Select a pull request.");
        if (!reviewer.canMerge(request)) throw new IllegalArgumentException("Only the target owner or an admin can review this request.");
        if (!request.isOpen()) throw new IllegalArgumentException("This request has already been reviewed.");
        // Old title-only requests remain reviewable but contain no paper to copy.
        Paper paper = request.getProposedPaper();
        if (accepted && paper != null && !containsPaper(request.getTarget(), paper)) request.getTarget().addPaper(paper);
        request.close(accepted);
        hub.addAction(new Review(reviewer, request, accepted));
        if (accepted) hub.addAction(new Merge(reviewer, request));
    }
    public List<PullRequest> visibleRequests(User user) {
        return hub.getPullRequests().stream().filter(pr -> pr.getAuthor().equals(user) || user.canMerge(pr)).toList();
    }
}
