package com.hotel.hotel_booking_system.ui.login;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.MainFrame;
import com.hotel.hotel_booking_system.service.AuthService;
import com.hotel.hotel_booking_system.ui.common.Dialog;
import com.hotel.hotel_booking_system.ui.staff.StaffPanel;

@Component
public class LoginFrame extends JFrame {
    private final JTextField textField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton button = new JButton("Login");

    private final AuthService authService;
    private final StaffPanel staffPanel;

    public LoginFrame(AuthService authService, StaffPanel staffPanel) {
        this.staffPanel = staffPanel;
        this.authService = authService;
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        // Row 0: username
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        form.add(textField, gbc);

        // Row 1: password
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        form.add(passwordField, gbc);

        // Row 2: button, aligned to the right under the fields
        gbc.gridx = 1;
        gbc.gridy = 2;
        form.add(button, gbc);

        outer.add(form, BorderLayout.CENTER);
        add(outer);
        pack();
        setLocationRelativeTo(null);

        getRootPane().setDefaultButton(button); // Enter key triggers Login
        button.addActionListener(e -> loginAction());
        passwordField.addActionListener(e -> loginAction());
    }

    private void loginAction() {
        String username = textField.getText();
        String rawPassword = new String(passwordField.getPassword());
        try {
            authService.login(username, rawPassword);
            dispose();
            MainFrame mainFrame = new MainFrame();
            mainFrame.addCard("Staff", staffPanel);
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        } catch (Exception e) {
            Dialog.showError(this, e.getMessage());
        }
    }
}