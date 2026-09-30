package com.hotel.hotel_booking_system.ui.common;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Dialog {
    public Dialog() {

    }

    public static boolean showForm(Component parent, 
            String title, LinkedHashMap<String, JComponent> fields) {
        JPanel panel = new JPanel(new GridLayout(fields.size(), 2, 8, 8));
        for (var entry : fields.entrySet()) {
            panel.add(new JLabel(entry.getKey()));
            panel.add(entry.getValue());
        }
        int result = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION);
        return result == JOptionPane.OK_OPTION;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
