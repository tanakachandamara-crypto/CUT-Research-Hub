package gui;

import core.Hub;
import model.*;
import service.ResearchService;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/** Swing views and event handling. Domain rules are delegated to ResearchService. */
public class CUTResearchHubGUI extends JFrame {
    private final Hub hub;
    private final ResearchService service;
    private final Preferences preferences = Preferences.userNodeForPackage(CUTResearchHubGUI.class);
    private boolean darkMode = preferences.getBoolean("darkMode", false);
    private Theme theme = new Theme(darkMode);
    private User currentUser;
    private String page = "Overview";
    private JPanel content;
    private JLabel status;
    private boolean dirty;
    private boolean authenticating;

    public CUTResearchHubGUI() throws IOException { this(new Hub()); }
    public CUTResearchHubGUI(Hub hub) throws IOException {
        this.hub = hub;
        hub.load(); // A damaged file stops startup instead of being overwritten with empty data.
        service = new ResearchService(hub);
        setTitle("CUT Research Hub");
        setSize(1160, 780);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                if (!authenticating && saveChanges()) dispose();
            }
        });
        showAuth(false);
        setVisible(true);
    }

    private JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(theme.background);
        return panel;
    }
    private JButton button(String text, boolean primary, Runnable action) {
        JButton button = theme.button(text, primary);
        button.addActionListener(event -> action.run());
        return button;
    }
    private JPanel row(Component... components) {
        JPanel row = panel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        for (Component component : components) row.add(component);
        return row;
    }
    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check", JOptionPane.WARNING_MESSAGE);
    }
    private void change(Runnable action, String message) {
        try {
            action.run();
            dirty = true;
            boolean saved = saveChanges();
            showDashboard();
            status.setText(saved ? message + "  •  Saved locally" : "Changes are in memory. Use Save before closing.");
        } catch (IllegalArgumentException | IllegalStateException ex) { error(ex); }
    }
    private boolean saveChanges() {
        if (!dirty) return true;
        try { hub.save(); dirty = false; return true; }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save your changes. They remain in this session.\n"
                    + "Check folder permissions or available space, then choose Save.\n\n" + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private void toggleTheme() {
        darkMode = !darkMode;
        preferences.putBoolean("darkMode", darkMode);
        theme = new Theme(darkMode);
        showDashboard();
    }

    private void showAuth(boolean register) {
        if (authenticating) return;
        currentUser = null;
        JPanel root = panel(new BorderLayout());
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setBackground(Theme.BLUE);
        brand.setPreferredSize(new Dimension(370, 0));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        JLabel mark = new JLabel("CUT / RESEARCH HUB");
        mark.setForeground(Theme.GOLD); mark.setFont(Theme.BODY.deriveFont(Font.BOLD, 17));
        JLabel headline = new JLabel("<html>Ideas grow<br>through<br>collaboration.</html>");
        headline.setForeground(Color.WHITE); headline.setFont(Theme.BODY.deriveFont(Font.BOLD, 37));
        JLabel caption = new JLabel("<html>Share research. Build on ideas.<br>Make your contribution count.</html>");
        caption.setForeground(new Color(208, 222, 237)); caption.setFont(Theme.BODY.deriveFont(15f));
        brandText.add(mark); brandText.add(Box.createVerticalStrut(35)); brandText.add(headline);
        brandText.add(Box.createVerticalStrut(25)); brandText.add(caption); brand.add(brandText);
        root.add(brand, BorderLayout.WEST);
        JPanel center = panel(new GridBagLayout());
        JPanel form = panel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        g.insets = new Insets(7, 0, 7, 0);
        form.setPreferredSize(new Dimension(440, register ? 605 : 430));
        form.add(theme.label(register ? "Create your account" : "Welcome back", 28, true), g);
        g.gridy++;
        JLabel subtitle = theme.label(register ? "Start contributing to the CUT research community." : "Sign in to your research workspace.", 14, false);
        subtitle.setForeground(theme.muted); form.add(subtitle, g);
        JTextField name = new JTextField(25);
        JTextField email = new JTextField(25);
        JPasswordField password = new JPasswordField(25);
        JPasswordField confirm = new JPasswordField(25);
        JComboBox<String> role = new JComboBox<>(hub.getAllUsers().isEmpty()
                ? new String[]{"Student", "Researcher", "Admin (first-account setup)"} : new String[]{"Student", "Researcher"});
        if (register) authField(form, g, "Full name", name);
        authField(form, g, "Email address", email);
        authField(form, g, register ? "Password (at least 8 characters)" : "Password", password);
        if (register) { authField(form, g, "Confirm password", confirm); authField(form, g, "Your role", role); }
        JLabel feedback = theme.label(" ", 13, false);
        feedback.setForeground(darkMode ? new Color(255, 160, 150) : new Color(172, 38, 38));
        JButton submit = theme.button(register ? "Create account" : "Sign in", true);
        submit.addActionListener(event -> {
            String supplied = new String(password.getPassword());
            if (register && !supplied.equals(new String(confirm.getPassword()))) {
                feedback.setText("Your passwords do not match."); return;
            }
            String suppliedName = name.getText();
            String suppliedEmail = email.getText();
            int suppliedRole = role.getSelectedIndex() + 1;
            authenticating = true;
            submit.setEnabled(false); feedback.setText("Please wait...");
            new SwingWorker<User, Void>() {
                @Override protected User doInBackground() {
                    if (register) hub.register(suppliedName, suppliedEmail, supplied, suppliedRole);
                    User user = hub.authenticate(suppliedEmail, supplied);
                    if (user == null) throw new IllegalArgumentException("Incorrect email or password.");
                    return user;
                }
                @Override protected void done() {
                    authenticating = false;
                    submit.setEnabled(true);
                    try {
                        currentUser = get(); dirty = true; saveChanges();
                        password.setText(""); confirm.setText(""); page = "Overview"; showDashboard();
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                        feedback.setText(cause.getMessage());
                    }
                }
            }.execute();
        });
        g.gridy++; form.add(feedback, g);
        g.gridy++; form.add(submit, g);
        g.gridy++; form.add(button(register ? "Already registered? Sign in" : "New here? Create an account", false,
                () -> showAuth(!register)), g);
        center.add(form); root.add(center, BorderLayout.CENTER);
        setContentPane(root); getRootPane().setDefaultButton(submit);
        revalidate(); repaint();
    }
    private void authField(JPanel form, GridBagConstraints g, String label, JComponent input) {
        g.gridy++; g.insets = new Insets(6, 0, 2, 0); form.add(theme.label(label, 13, true), g);
        g.gridy++; g.insets = new Insets(2, 0, 5, 0);
        theme.apply(input); input.setPreferredSize(new Dimension(400, 34));
        form.add(input, g);
    }

    private void showDashboard() {
        JPanel root = panel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(Theme.BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel logo = new JLabel("CUT / RESEARCH HUB"); logo.setForeground(Color.WHITE);
        logo.setFont(Theme.BODY.deriveFont(Font.BOLD, 19)); header.add(logo, BorderLayout.WEST);
        JPanel account = row(); account.setOpaque(false);
        JLabel user = new JLabel(currentUser.getName() + "  /  " + currentUser.getRole());
        user.setForeground(Color.WHITE); user.setFont(Theme.BODY);
        account.add(user);
        account.add(button(darkMode ? "Light mode" : "Dark mode", false, this::toggleTheme));
        account.add(button("Sign out", false, () -> { if (saveChanges()) showAuth(false); }));
        header.add(account, BorderLayout.EAST); root.add(header, BorderLayout.NORTH);
        JPanel sidebar = panel(new GridBagLayout()); sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 16, 20, 16));
        GridBagConstraints g = new GridBagConstraints(); g.gridx = 0; g.gridy = 0; g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1; g.insets = new Insets(5, 0, 5, 0);
        String[] pages = currentUser instanceof Admin
                ? new String[]{"Overview", "Repositories", "My repositories", "Pull requests", "Activity", "Users", "Profile"}
                : new String[]{"Overview", "Repositories", "My repositories", "Pull requests", "Activity", "Profile"};
        for (String name : pages) {
            JButton nav = button(name, name.equals(page), () -> { page = name; showDashboard(); });
            nav.setHorizontalAlignment(SwingConstants.LEFT);
            if (name.equals(page)) { nav.setBackground(Theme.GOLD); nav.setForeground(Theme.BLUE); }
            sidebar.add(nav, g); g.gridy++;
        }
        g.weighty = 1; sidebar.add(Box.createVerticalGlue(), g); g.gridy++; g.weighty = 0;
        sidebar.add(button("Save", false, () -> {
            if (saveChanges()) status.setText("All changes saved locally.");
        }), g);
        if (currentUser instanceof Admin) {
            g.gridy++;
            sidebar.add(button("Create backup", false, () -> {
                try { var path = hub.backup(); dirty = false; showText("Backup created", path.toAbsolutePath().toString()); }
                catch (IOException ex) { error(ex); }
            }), g);
        }
        root.add(sidebar, BorderLayout.WEST);
        content = panel(new BorderLayout(0, 20)); content.setBorder(BorderFactory.createEmptyBorder(28, 20, 18, 28));
        content.add(theme.label(page, 29, true), BorderLayout.NORTH); root.add(content, BorderLayout.CENTER);
        status = theme.label(dirty ? "Unsaved changes. Choose Save to retry." : "Local workspace  •  Changes save automatically", 12, false);
        status.setForeground(theme.muted); status.setBorder(BorderFactory.createEmptyBorder(10, 24, 12, 24));
        root.add(status, BorderLayout.SOUTH);
        switch (page) {
            case "Repositories" -> repositories(false);
            case "My repositories" -> repositories(true);
            case "Pull requests" -> requests();
            case "Activity" -> activity();
            case "Users" -> users();
            case "Profile" -> profile();
            default -> overview();
        }
        setContentPane(root); getRootPane().setDefaultButton(null); revalidate(); repaint();
    }

    private void overview() {
        JPanel body = panel(new BorderLayout(0, 24));
        JPanel cards = panel(new GridLayout(1, 3, 16, 0));
        long own = hub.getRepos().stream().filter(r -> r.getOwner().equals(currentUser)).count();
        long pending = service.visibleRequests(currentUser).stream().filter(PullRequest::isOpen).count();
        cards.add(metric("Available repositories", hub.getRepos().size()));
        cards.add(metric("Your repositories", own)); cards.add(metric("Open pull requests", pending));
        body.add(cards, BorderLayout.NORTH);
        JPanel welcome = panel(new BorderLayout(0, 16));
        welcome.add(theme.label("Your next contribution starts here", 22, true), BorderLayout.NORTH);
        String instructions = currentUser instanceof Student
                ? "1. Explore repositories and open a paper to read it.\n\n2. Fork a researcher repository into your own workspace.\n\n3. Add a new paper to your fork. This records a commit.\n\n4. Submit that paper as a pull request for the owner to review."
                : "1. Create a repository for your research topic.\n\n2. Add papers with a title and research content.\n\n3. Review contributions from students and merge accepted papers.\n\n4. Follow the activity history to see how the work develops.";
        welcome.add(textArea(instructions, false), BorderLayout.CENTER);
        welcome.add(row(button("Explore repositories", true, () -> { page = "Repositories"; showDashboard(); }),
                button(currentUser.canCreateRepo() ? "Create repository" : "My workspace", false, () -> {
                    if (currentUser.canCreateRepo()) createRepository(); else { page = "My repositories"; showDashboard(); }
                })), BorderLayout.SOUTH);
        body.add(welcome, BorderLayout.CENTER); content.add(body, BorderLayout.CENTER);
    }
    private JPanel metric(String label, long value) {
        JPanel card = new JPanel(new BorderLayout(0, 12)); card.setBackground(theme.surface);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(theme.border),
                BorderFactory.createEmptyBorder(22, 20, 22, 20)));
        card.add(theme.label(Long.toString(value), 34, true), BorderLayout.CENTER);
        card.add(theme.label(label, 13, false), BorderLayout.SOUTH); return card;
    }
    private JTable table(String[] columns, Object[][] rows) {
        DefaultTableModel model = new DefaultTableModel(rows, columns) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model); table.setFont(Theme.BODY); table.setRowHeight(42);
        table.setBackground(theme.surface); table.setForeground(theme.text);
        table.setGridColor(theme.border); table.setShowVerticalLines(false);
        table.setSelectionBackground(Theme.BLUE); table.setSelectionForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(Theme.BODY.deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(theme.surface); table.getTableHeader().setForeground(theme.text);
        return table;
    }
    private JScrollPane scroll(Component child) {
        JScrollPane scroll = new JScrollPane(child); scroll.getViewport().setBackground(theme.surface);
        scroll.setBorder(BorderFactory.createLineBorder(theme.border)); return scroll;
    }
    private int selected(JTable table) {
        if (table.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.", "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
            return -1;
        }
        return table.convertRowIndexToModel(table.getSelectedRow());
    }
    private void repositories(boolean mine) {
        List<HubRepository> repos = hub.getRepos().stream().filter(r -> !mine || r.getOwner().equals(currentUser)).toList();
        Object[][] rows = repos.stream().map(r -> new Object[]{r.getName(), r.getOwner().getName(), r.getPapers().size(),
                r.getSourceRepositoryId() == null ? "Original" : "Fork"}).toArray(Object[][]::new);
        JTable table = table(new String[]{"Repository", "Owner", "Papers", "Type"}, rows);
        JPanel body = panel(new BorderLayout(0, 12));
        JTextField search = new JTextField(22); theme.apply(search);
        search.setToolTipText("Search by repository, owner, or type");
        search.getDocument().addDocumentListener(new DocumentListener() {
            private void filter() {
                String query = search.getText().trim().toLowerCase(Locale.ROOT);
                @SuppressWarnings("unchecked") var sorter = (javax.swing.table.TableRowSorter<DefaultTableModel>) table.getRowSorter();
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        for (int i = 0; i < entry.getValueCount(); i++) if (entry.getStringValue(i).toLowerCase(Locale.ROOT).contains(query)) return true;
                        return false;
                    }
                });
            }
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });
        body.add(row(theme.label("Search", 14, true), search), BorderLayout.NORTH);
        body.add(scroll(table), BorderLayout.CENTER);
        Runnable open = () -> { int i = selected(table); if (i >= 0) repositoryDetails(repos.get(i)); };
        table.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) open.run(); } });
        JPanel controls = row(button("Open repository", true, open));
        if (currentUser.canCreateRepo()) controls.add(button("Create repository", false, this::createRepository));
        if (currentUser instanceof Student) controls.add(button("Fork selected", false, () -> {
            int i = selected(table); if (i >= 0) change(() -> service.fork(currentUser, repos.get(i)), "Repository forked with its papers.");
        }));
        if (repos.isEmpty()) controls.add(theme.label(mine ? "Your workspace is empty." : "No repositories yet.", 13, false));
        body.add(controls, BorderLayout.SOUTH); content.add(body, BorderLayout.CENTER);
    }
    private void createRepository() {
        JTextField name = new JTextField(30); JTextArea description = textArea("", true);
        JPanel form = form("Repository name", name, "Description", scroll(description));
        if (confirm(form, "Create repository")) change(() -> service.createRepository(currentUser, name.getText(), description.getText()), "Repository created.");
    }
    private JTextArea textArea(String text, boolean editable) {
        JTextArea area = new JTextArea(text, 8, 40); theme.apply(area);
        area.setEditable(editable); area.setLineWrap(true); area.setWrapStyleWord(true);
        area.setMargin(new Insets(16, 16, 16, 16)); area.setCaretPosition(0); return area;
    }
    private JPanel form(String firstLabel, JComponent first, String secondLabel, JComponent second) {
        JPanel panel = panel(new BorderLayout(0, 10));
        JPanel top = panel(new BorderLayout(0, 7)); top.add(theme.label(firstLabel, 14, true), BorderLayout.NORTH);
        theme.apply(first); first.setPreferredSize(new Dimension(480, 34)); top.add(first, BorderLayout.CENTER);
        JPanel bottom = panel(new BorderLayout(0, 7)); bottom.add(theme.label(secondLabel, 14, true), BorderLayout.NORTH);
        bottom.add(second, BorderLayout.CENTER); panel.add(top, BorderLayout.NORTH); panel.add(bottom, BorderLayout.CENTER);
        return panel;
    }
    private boolean confirm(Component form, String title) {
        return JOptionPane.showConfirmDialog(this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }
    private void repositoryDetails(HubRepository repo) {
        JDialog dialog = new JDialog(this, repo.getName(), true);
        JPanel body = panel(new BorderLayout(0, 15)); body.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        body.add(theme.label(repo.getName() + " / " + repo.getOwner().getName(), 22, true), BorderLayout.NORTH);
        List<Paper> papers = repo.getPapers();
        JTable table = table(new String[]{"Paper", "Added"}, papers.stream().map(p -> new Object[]{p.getTitle(), p.getUploadDate().toLocalDate()}).toArray(Object[][]::new));
        JPanel center = panel(new BorderLayout(0, 10));
        JTextArea description = textArea(repo.getDescription(), false); description.setRows(2);
        center.add(scroll(description), BorderLayout.NORTH); center.add(scroll(table), BorderLayout.CENTER);
        body.add(center, BorderLayout.CENTER);
        JPanel actions = row(button("Read paper", true, () -> { int i = selected(table); if (i >= 0) showText(papers.get(i).getTitle(), papers.get(i).getContent()); }));
        if (repo.getOwner().equals(currentUser) || currentUser instanceof Admin) actions.add(button("Add paper", false, () -> {
            JTextField title = new JTextField(30); JTextArea text = textArea("", true);
            if (confirm(form("Paper title", title, "Research content / summary", scroll(text)), "Add paper")) {
                try {
                    service.addPaper(currentUser, repo, title.getText(), text.getText()); dirty = true; saveChanges();
                    dialog.dispose(); showDashboard(); repositoryDetails(repo);
                } catch (IllegalArgumentException ex) { error(ex); }
            }
        }));
        if (currentUser instanceof Student && repo.getOwner().equals(currentUser)) actions.add(button("Submit paper", false, () -> {
            int i = selected(table); if (i < 0) return;
            String title = JOptionPane.showInputDialog(dialog, "Pull request title", "Contribute: " + papers.get(i).getTitle());
            if (title == null) return;
            try {
                service.openRequest(currentUser, repo, papers.get(i), title); dirty = true; saveChanges(); dialog.dispose();
                page = "Pull requests"; showDashboard();
            } catch (IllegalArgumentException ex) { error(ex); }
        }));
        if (papers.isEmpty()) actions.add(theme.label("No papers added yet.", 13, false));
        body.add(actions, BorderLayout.SOUTH); dialog.setContentPane(body); dialog.setSize(830, 560);
        dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }
    private void requests() {
        List<PullRequest> requests = service.visibleRequests(currentUser);
        JTable table = table(new String[]{"Title", "Author", "Target", "Status"}, requests.stream()
                .map(pr -> new Object[]{pr.getTitle(), pr.getAuthor().getName(), pr.getTarget().getName(), pr.getStatus()}).toArray(Object[][]::new));
        JPanel body = panel(new BorderLayout(0, 15)); body.add(scroll(table), BorderLayout.CENTER);
        JPanel actions = row(button("Read contribution", true, () -> {
            int i = selected(table); if (i < 0) return;
            PullRequest request = requests.get(i); Paper paper = request.getProposedPaper();
            showText(request.getTitle(), paper == null ? "This request was created in the original version and contains only a title. There is no attached paper."
                    : paper.getTitle() + "\n\n" + paper.getContent());
        }));
        if (!(currentUser instanceof Student)) actions.add(button("Review selected", false, () -> {
            int i = selected(table); if (i < 0) return;
            PullRequest request = requests.get(i);
            if (!request.isOpen()) { error(new IllegalArgumentException("This request has already been reviewed.")); return; }
            String details = "Review: " + request.getTitle() + "\nTarget: " + request.getTarget().getName();
            if (request.getProposedPaper() == null) details += "\nLegacy title-only request: no paper will be added.";
            int answer = JOptionPane.showOptionDialog(this, details, "Review contribution", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, new String[]{"Merge", "Reject", "Cancel"}, "Cancel");
            if (answer == 0 || answer == 1) change(() -> service.review(currentUser, request, answer == 0), answer == 0 ? "Request merged." : "Request rejected.");
        }));
        if (requests.isEmpty()) actions.add(theme.label("No contributions to show yet.", 13, false));
        body.add(actions, BorderLayout.SOUTH); content.add(body, BorderLayout.CENTER);
    }
    private void activity() {
        StringBuilder text = new StringBuilder();
        List<model.Action> actions = hub.getHistory();
        for (int i = actions.size() - 1; i >= 0; i--) text.append(actions.get(i)).append("\n\n");
        if (text.length() == 0) text.append("No activity yet. Create a repository or make your first contribution.");
        content.add(scroll(textArea(text.toString(), false)), BorderLayout.CENTER);
    }
    private void users() {
        JTable table = table(new String[]{"Name", "Email", "Role"}, hub.getAllUsers().stream()
                .map(u -> new Object[]{u.getName(), u.getEmail(), u.getRole()}).toArray(Object[][]::new));
        content.add(scroll(table), BorderLayout.CENTER);
    }
    private void profile() {
        long owned = hub.getRepos().stream().filter(r -> r.getOwner().equals(currentUser)).count();
        long commits = hub.getHistory().stream().filter(a -> a instanceof Commit && ((Commit) a).getAuthor().equals(currentUser)).count();
        String quota = currentUser.getMaxRepos() == Integer.MAX_VALUE ? "Unlimited" : Integer.toString(currentUser.getMaxRepos());
        content.add(scroll(textArea("Name: " + currentUser.getName() + "\n\nEmail: " + currentUser.getEmail()
                + "\n\nRole: " + currentUser.getRole() + "\n\nRepositories: " + owned + " / " + quota
                + "\n\nRecorded commits: " + commits, false)), BorderLayout.CENTER);
    }
    private void showText(String title, String text) {
        JScrollPane pane = scroll(textArea(text, false)); pane.setPreferredSize(new Dimension(650, 380));
        JOptionPane.showMessageDialog(this, pane, title, JOptionPane.PLAIN_MESSAGE);
    }
}
