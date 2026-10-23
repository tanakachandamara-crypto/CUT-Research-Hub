package gui;

import java.awt.*;
import javax.swing.*;

/** Small, shared palette used by screens and dialogs. */
public final class Theme {
    public static final Color BLUE = new Color(19, 64, 107);
    public static final Color GOLD = new Color(242, 186, 61);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    public final Color background, surface, text, muted, border;
    public Theme(boolean dark) {
        background = dark ? new Color(19, 28, 42) : new Color(242, 245, 249);
        surface = dark ? new Color(30, 42, 59) : Color.WHITE;
        text = dark ? new Color(236, 241, 248) : new Color(28, 44, 62);
        muted = dark ? new Color(174, 191, 211) : new Color(91, 108, 127);
        border = dark ? new Color(63, 79, 99) : new Color(215, 224, 233);
    }
    public void apply(Component component) {
        component.setFont(BODY);
        component.setForeground(text);
        component.setBackground(component instanceof JTextField || component instanceof JTextArea
                || component instanceof JTable || component instanceof JList<?> || component instanceof JComboBox<?>
                ? surface : background);
        if (component instanceof javax.swing.text.JTextComponent) {
            ((javax.swing.text.JTextComponent) component).setCaretColor(text);
        }
        if (component instanceof Container) for (Component child : ((Container) component).getComponents()) apply(child);
    }
    public JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(BODY.deriveFont(Font.BOLD));
        button.setBackground(primary ? BLUE : surface);
        button.setForeground(primary ? Color.WHITE : this.text);
        button.setOpaque(true);
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? BLUE : border),
                BorderFactory.createEmptyBorder(11, 16, 11, 16)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
    public JLabel label(String text, int size, boolean bold) {
        JLabel label = new JLabel(text);
        label.setFont(BODY.deriveFont(bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(this.text);
        return label;
    }
}
