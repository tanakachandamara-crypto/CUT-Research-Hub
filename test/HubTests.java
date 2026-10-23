import core.Hub;
import model.*;
import service.ResearchService;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Dependency-free regression tests. Run with test.bat or ./test.sh. */
public class HubTests {
    private static int checks;
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        checks++;
    }
    private static void rejected(Runnable action, String name) {
        try { action.run(); throw new AssertionError(name); }
        catch (IllegalArgumentException | IllegalStateException expected) { checks++; }
    }
    public static void main(String[] args) throws Exception {
        Path folder = Files.createTempDirectory("cut-hub-tests-");
        try {
            Hub hub = new Hub(folder);
            hub.register("Admin", "admin@example.edu", "AdminPass42", 3);
            rejected(() -> hub.register("Another admin", "other@example.edu", "Password42", 3), "Public admin registration blocked");
            rejected(() -> hub.register("Bad", "bad-email", "Password42", 1), "Email validation");
            rejected(() -> hub.register("Short", "short@example.edu", "short", 1), "Password validation");
            hub.register("Researcher", " Research@Example.edu ", "Research42", 2);
            hub.register("Student", "student@example.edu", "Student42", 1);
            hub.register("Other researcher", "other@example.edu", "Research42", 2);
            User admin = hub.authenticate("ADMIN@example.edu", "AdminPass42");
            User researcher = hub.authenticate(" research@example.edu ", "Research42");
            User student = hub.authenticate("student@example.edu", "Student42");
            User other = hub.authenticate("other@example.edu", "Research42");
            check(researcher != null && admin != null, "Email normalization");
            check(hub.authenticate("student@example.edu", "wrong") == null, "Wrong password denied");
            rejected(() -> hub.register("Duplicate", "RESEARCH@example.edu", "Password42", 1), "Duplicate email blocked");
            ResearchService service = new ResearchService(hub);
            HubRepository original = service.createRepository(researcher, "Soil (moisture)", "Sensor-based irrigation research");
            Paper initial = service.addPaper(researcher, original, "Initial study", "Initial research findings.");
            rejected(() -> service.createRepository(student, "Unauthorized", ""), "Student cannot create originals");
            rejected(() -> service.createRepository(researcher, "soil (MOISTURE)", ""), "Duplicate repository name blocked");
            rejected(() -> service.addPaper(student, original, "Intrusion", "Text"), "Nonowner write blocked");
            HubRepository fork = service.fork(student, original);
            check(fork.getPapers().size() == 1 && fork.getPapers().get(0).getId().equals(initial.getId()), "Fork copies papers");
            check(original.getId().equals(fork.getSourceRepositoryId()), "Fork retains source link");
            rejected(() -> service.openRequest(student, fork, initial, "Existing"), "Already present paper blocked");
            Paper contribution = service.addPaper(student, fork, "New results", "Results from the field.");
            check(original.getPapers().size() == 1 && fork.getPapers().size() == 2, "Fork list independent");
            check(hub.getHistory().stream().anyMatch(a -> a instanceof Commit), "Student paper records a commit");
            PullRequest request = service.openRequest(student, fork, contribution, "Please review new results");
            rejected(() -> service.openRequest(student, fork, contribution, "Duplicate"), "Duplicate open request blocked");
            rejected(() -> service.review(other, request, true), "Unrelated researcher cannot review");
            service.review(researcher, request, true);
            check("MERGED".equals(request.getStatus()) && !request.isOpen(), "Merged status stored");
            check(original.getPapers().size() == 2, "Merge copies proposed paper");
            rejected(() -> service.review(researcher, request, true), "Double merge prevented");
            check(original.getPapers().size() == 2, "No duplicate merge paper");
            Paper rejectedPaper = service.addPaper(student, fork, "Unfinished", "Needs more work");
            PullRequest rejectedRequest = service.openRequest(student, fork, rejectedPaper, "Review draft");
            service.review(admin, rejectedRequest, false);
            check("REJECTED".equals(rejectedRequest.getStatus()) && original.getPapers().size() == 2, "Reject leaves target unchanged");
            for (int i = 0; i < 4; i++) service.fork(student, original);
            rejected(() -> service.fork(student, original), "Student quota enforced");
            for (int i = 0; i < 19; i++) service.createRepository(researcher, "Study " + i, "");
            rejected(() -> service.createRepository(researcher, "Over limit", ""), "Researcher quota enforced");
            hub.save();
            Path backup = hub.backup();
            check(Files.exists(backup) && !backup.equals(folder.resolve("hub.ser")), "Backup is separate file");
            Hub loaded = new Hub(folder); loaded.load(); loaded.load();
            check(loaded.getAllUsers().size() == 4 && loaded.getPullRequests().size() == 2, "Repeated load does not duplicate data");
            check(loaded.getRepo(original.getId()).getPapers().size() == 2, "Merged papers persist");
            check(loaded.getPullRequests().stream().noneMatch(PullRequest::isOpen), "Closed status persists");
            check(loaded.authenticate("student@example.edu", "Student42") != null, "Password survives reload");
            // Verify old hash values still authenticate, then are upgraded on save.
            var passwordField = User.class.getDeclaredField("passwordHash"); passwordField.setAccessible(true);
            passwordField.set(student, Integer.toHexString("Legacy42".hashCode()));
            check(student.checkPwd("Legacy42"), "Legacy password accepted");
            check(((String) passwordField.get(student)).startsWith("pbkdf2:"), "Legacy hash upgraded");
            check(!student.checkPwd("wrong"), "Upgraded password rejects wrong input");
            PullRequest legacy = new PullRequest("Old title-only request", (Student) student, original);
            hub.addPullRequest(legacy); service.review(admin, legacy, true);
            check(!legacy.isOpen() && original.getPapers().size() == 2, "Legacy request closes without invented content");
            byte[] corrupt = {1, 2, 3, 4}; Files.write(folder.resolve("hub.ser"), corrupt);
            try { loaded.load(); throw new AssertionError("Corruption should fail"); }
            catch (IOException expected) { checks++; }
            check(loaded.getRepos().size() == 25, "Failed load preserves live state");
            check(Arrays.equals(Files.readAllBytes(folder.resolve("hub.ser")), corrupt), "Failed load does not overwrite file");
            if (args.length > 0) {
                Path oldFolder = Files.createDirectories(folder.resolve("original"));
                Files.copy(Path.of(args[0]), oldFolder.resolve("hub.ser"));
                Hub old = new Hub(oldFolder); old.load(); old.save();
                check(old.getRepos().isEmpty() && old.getAllUsers().isEmpty(), "Supplied original empty data is compatible");
            }
            System.out.println("PASS: " + checks + " regression checks");
        } finally {
            try (var paths = Files.walk(folder)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
