package core;

import model.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Stores the application data. The four serialized objects retain the original format. */
public class Hub {
    private final Path dataDirectory;
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, HubRepository> repos = new LinkedHashMap<>();
    private final List<model.Action> history = new ArrayList<>();
    private final List<PullRequest> pullRequests = new ArrayList<>();

    public Hub() { this(Paths.get("hub_data")); }
    public Hub(Path dataDirectory) { this.dataDirectory = dataDirectory; }
    private String emailKey(String email) { return email == null ? "" : email.trim().toLowerCase(Locale.ROOT); }

    public User authenticate(String email, String password) {
        User user = users.get(emailKey(email));
        return user != null && user.checkPwd(password) ? user : null;
    }
    public boolean userExists(String email) { return users.containsKey(emailKey(email)); }
    public void register(String name, String email, String password, int role) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Enter your name.");
        String key = emailKey(email);
        if (!key.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) throw new IllegalArgumentException("Enter a valid email address.");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("Use at least 8 characters for your password.");
        if (userExists(key)) throw new IllegalArgumentException("This email is already registered.");
        if (role < 1 || role > 3) throw new IllegalArgumentException("Select a valid role.");
        if (role == 3 && !users.isEmpty()) throw new IllegalArgumentException("Admin is available only during first-account setup.");
        User user = switch (role) {
            case 1 -> new Student(name.trim(), key, password);
            case 2 -> new Researcher(name.trim(), key, password);
            default -> new Admin(name.trim(), key, password);
        };
        users.put(key, user);
    }
    public List<User> getAllUsers() { return new ArrayList<>(users.values()); }
    public List<HubRepository> getRepos() {
        List<HubRepository> result = new ArrayList<>(repos.values());
        result.sort(Comparator.comparing(HubRepository::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }
    public HubRepository getRepo(String id) { return repos.get(id); }
    public void addRepo(HubRepository repo) { repos.put(repo.getId(), repo); }
    public List<PullRequest> getPullRequests() { return new ArrayList<>(pullRequests); }
    public void addPullRequest(PullRequest request) { pullRequests.add(request); }
    public List<model.Action> getHistory() { return new ArrayList<>(history); }
    public void addAction(model.Action action) { history.add(action); }

    public void save() throws IOException {
        Files.createDirectories(dataDirectory);
        Path temporary = Files.createTempFile(dataDirectory, "hub-", ".tmp");
        try {
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporary))) {
                out.writeObject(users);
                out.writeObject(repos);
                out.writeObject(pullRequests);
                out.writeObject(history);
            }
            Path destination = dataDirectory.resolve("hub.ser");
            try {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }

    public Path backup() throws IOException {
        save();
        Path folder = dataDirectory.resolve("backups");
        Files.createDirectories(folder);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        Path copy = Files.createTempFile(folder, "hub-" + stamp + "-", ".ser");
        Files.copy(dataDirectory.resolve("hub.ser"), copy, StandardCopyOption.REPLACE_EXISTING);
        return copy;
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        Path file = dataDirectory.resolve("hub.ser");
        if (!Files.exists(file)) return;
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            // Read everything before replacing live state; failed loads leave it untouched.
            Map<String, User> loadedUsers = (Map<String, User>) in.readObject();
            Map<String, HubRepository> loadedRepos = (Map<String, HubRepository>) in.readObject();
            List<PullRequest> loadedRequests = (List<PullRequest>) in.readObject();
            List<model.Action> loadedHistory = (List<model.Action>) in.readObject();
            users.clear(); users.putAll(loadedUsers);
            repos.clear(); repos.putAll(loadedRepos);
            pullRequests.clear(); pullRequests.addAll(loadedRequests);
            history.clear(); history.addAll(loadedHistory);
        } catch (ClassNotFoundException | ClassCastException | NullPointerException ex) {
            throw new IOException("The saved data could not be read. Keep the file for recovery.", ex);
        }
    }
}
