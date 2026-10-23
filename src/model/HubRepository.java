package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HubRepository implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private String name, description = "No description";
    private final User owner;
    private final List<Paper> papers = new ArrayList<>();

    private String sourceRepositoryId;

    public HubRepository(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public User getOwner() { return owner; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public List<Paper> getPapers() { return new ArrayList<>(papers); }

    public void addPaper(Paper p) {
        if (p != null) papers.add(p);
    }

    public String getSourceRepositoryId() { return sourceRepositoryId; }
    public void setSourceRepositoryId(String id) { sourceRepositoryId = id; }

    @Override
    public String toString() {
        return name + " — " + owner.getName();
    }
}