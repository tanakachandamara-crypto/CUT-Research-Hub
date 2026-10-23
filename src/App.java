import gui.CUTResearchHubGUI;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                new CUTResearchHubGUI();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                        "CUT Research Hub could not start.\nYour existing data has not been overwritten.\n\n"
                        + ex.getMessage() + "\n\nCheck hub_data/hub.ser or restore a backup.",
                        "Unable to start", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
    }
}
