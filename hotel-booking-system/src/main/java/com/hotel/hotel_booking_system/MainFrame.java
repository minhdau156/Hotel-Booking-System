package com.hotel.hotel_booking_system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import java.awt.*;

import org.springframework.stereotype.Component;

/**
 * MainFrame
 */
@Component 
public class MainFrame extends JFrame{

    public static final String ROOMS   = "Rooms";
    public static final String GUESTS  = "Guests";
    public static final String BOOKING = "Booking";
    public static final String STAFF   = "Staff";
    public static final String BILLING = "Billing";
    public static final String REPORTS = "Reports";

    private static final Color NORMAL_BG   = new Color(238, 238, 238);
    private static final Color SELECTED_BG = new Color(52, 120, 200);

    private static final List<String> MODULES =
            List.of(ROOMS, GUESTS, BOOKING, STAFF, BILLING, REPORTS);
    
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final Map<String, JPanel> cards = new HashMap<>();
    private final Map<String, JButton> sidebarButtons = new HashMap<>();

    public MainFrame() {
        initUi();
    }

    private void initUi() {
        
        setTitle("Hotel Booking System");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);

        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        for (String name : MODULES) {
            addCard(name, placeholder(name));
        }
        showCard(ROOMS);
        
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        Dimension buttonSize = new Dimension(140, 36);
        for (String name : MODULES) {
            JButton button = new JButton(name);
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);   // stop the look-and-feel painting over our color
            button.setOpaque(true);
            button.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize); 
            button.addActionListener(e -> showCard(name));
            sidebar.add(button);

            sidebarButtons.put(name, button);
        }

        return sidebar;
    }

    private void showCard(String name) {
        if (!cards.containsKey(name)) {
            throw new IllegalArgumentException("No card registered: " + name);
        }
        cardLayout.show(cardPanel, name);

        sidebarButtons.forEach((moduleName, button) -> {
            boolean active = moduleName.equals(name);
            button.setBackground(active ? SELECTED_BG : NORMAL_BG);
            button.setForeground(active ? Color.WHITE : Color.BLACK);
        });

    }

    public void addCard(String name, JPanel panel) {
        JPanel old = cards.put(name, panel);
        if (old != null) {
            cardPanel.remove(old);
        }
        cardPanel.add(panel, name);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private JPanel placeholder(String name) {
        JPanel p = new JPanel(new GridBagLayout());   // centers the label
        p.add(new JLabel(name + " (coming soon)"));
        return p;
    }
}
