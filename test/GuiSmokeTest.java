import core.Hub;
import gui.CUTResearchHubGUI;
import model.*;
import service.ResearchService;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Run on a desktop (or Xvfb). Exercises actual Swing controls with isolated test data. */
public class GuiSmokeTest {
    private static CUTResearchHubGUI frame;
    private static int checks;
    private static List<Component> descendants(Container root) {
        List<Component> result = new ArrayList<>();
        for (Component component : root.getComponents()) {
            result.add(component);
            if (component instanceof Container) result.addAll(descendants((Container) component));
        }
        return result;
    }
    private static JButton button(String text) {
        return descendants(frame).stream().filter(c -> c instanceof JButton && text.equals(((JButton) c).getText()))
                .map(c -> (JButton) c).findFirst().orElseThrow(() -> new AssertionError("Missing button: " + text));
    }
    private static <T> T first(Class<T> type) {
        return descendants(frame).stream().filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    private static void capture(Path directory, String name) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics(); frame.paint(graphics); graphics.dispose();
            try { ImageIO.write(image, "png", directory.resolve(name + ".png").toFile()); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        });
    }
    private static void waitForDashboard() throws Exception {
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            boolean[] found = {false};
            SwingUtilities.invokeAndWait(() -> found[0] = descendants(frame).stream().anyMatch(c -> c instanceof JButton && "Sign out".equals(((JButton) c).getText())));
            if (found[0]) return;
            Thread.sleep(50);
        }
        throw new AssertionError("Login did not reach dashboard");
    }
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("cut-gui-");
        Path screenshots = args.length > 0 ? Path.of(args[0]) : Files.createTempDirectory("cut-screens-");
        Files.createDirectories(screenshots);
        var prefs = java.util.prefs.Preferences.userNodeForPackage(CUTResearchHubGUI.class);
        boolean oldDark = prefs.getBoolean("darkMode", false); prefs.putBoolean("darkMode", false);
        try {
            Hub hub = new Hub(directory);
            hub.register("Workspace Admin", "admin@example.edu", "AdminPass42", 3);
            hub.register("Research Lecturer", "lecturer@example.edu", "Research42", 2);
            User researcher = hub.authenticate("lecturer@example.edu", "Research42");
            ResearchService service = new ResearchService(hub);
            HubRepository repo = service.createRepository(researcher, "Smart Irrigation", "Studying soil-moisture sensing and efficient water use.");
            service.addPaper(researcher, repo, "Sensor calibration study", "Compare readings from calibrated soil moisture sensors.");
            service.createRepository(researcher, "Campus Energy", "Exploring efficient energy systems.");
            hub.save();
            SwingUtilities.invokeAndWait(() -> {
                try { frame = new CUTResearchHubGUI(hub); }
                catch (Exception ex) { throw new RuntimeException(ex); }
            });
            capture(screenshots, "01-login");
            SwingUtilities.invokeAndWait(() -> button("New here? Create an account").doClick());
            capture(screenshots, "02-register");
            SwingUtilities.invokeAndWait(() -> {
                List<JTextField> fields = descendants(frame).stream().filter(c -> c instanceof JTextField).map(c -> (JTextField)c).toList();
                fields.get(0).setText("Student Contributor"); fields.get(1).setText("student@example.edu");
                fields.get(2).setText("Student42"); fields.get(3).setText("Student42");
                check(first(JComboBox.class).getItemCount() == 2, "No public admin role after setup");
                button("Create account").doClick();
            });
            waitForDashboard(); checks++;
            capture(screenshots, "03-overview");
            SwingUtilities.invokeAndWait(() -> {
                button("Repositories").doClick();
                JTable table = first(JTable.class);
                check(table.getRowCount() == 2, "Repository list opens");
                first(JTextField.class).setText("smart");
                check(table.getRowCount() == 1, "Search filters visible rows");
                check("Smart Irrigation".equals(table.getValueAt(0, 0)), "Search returns correct repository");
                table.setRowSelectionInterval(0, 0);
                button("Fork selected").doClick();
                check(hub.getRepos().stream().anyMatch(r -> r.getSourceRepositoryId() != null && r.getPapers().size() == 1), "Fork button copies papers");
            });
            capture(screenshots, "04-repositories");
            SwingUtilities.invokeAndWait(() -> button("Dark mode").doClick());
            capture(screenshots, "05-dark");
            SwingUtilities.invokeAndWait(() -> {
                check(button("Light mode") != null, "Theme label changes");
                for (String page : new String[]{"My repositories", "Pull requests", "Activity", "Profile", "Overview"}) button(page).doClick();
                checks++;
                button("Sign out").doClick();
                button("New here? Create an account").doClick();
                check(button("Create account") != null, "Register still opens after logout");
                button("Already registered? Sign in").doClick();
                List<JTextField> fields = descendants(frame).stream().filter(c -> c instanceof JTextField).map(c -> (JTextField)c).toList();
                fields.get(0).setText("lecturer@example.edu"); fields.get(1).setText("Research42");
                button("Sign in").doClick();
            });
            waitForDashboard(); checks++;
            SwingUtilities.invokeAndWait(() -> {
                button("Repositories").doClick();
                check(button("Create repository") != null, "Researcher create action visible");
                button("Light mode").doClick();
                frame.setSize(1000, 700); frame.validate();
            });
            capture(screenshots, "06-minimum-size");
            System.out.println("PASS: " + checks + " GUI checks; screenshots: " + screenshots);
        } finally {
            if (frame != null) SwingUtilities.invokeAndWait(() -> frame.dispose());
            prefs.putBoolean("darkMode", oldDark);
            try (var paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
