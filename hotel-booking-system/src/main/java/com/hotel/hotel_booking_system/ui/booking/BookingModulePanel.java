package com.hotel.hotel_booking_system.ui.booking;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.springframework.stereotype.Component;

@Component
public class BookingModulePanel extends JPanel {
    private final BookingPanel bookingPanel;
    private final ActiveReservationsPanel activeReservationsPanel;

    public BookingModulePanel(
            BookingPanel bookingPanel,
            ActiveReservationsPanel activeReservationsPanel) {
        this.bookingPanel = bookingPanel;
        this.activeReservationsPanel = activeReservationsPanel;
        setLayout(new BorderLayout());
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Search and Book", bookingPanel);
        tabs.addTab("Active Reservations", activeReservationsPanel);
        tabs.addChangeListener(event -> refresh());
        add(tabs, BorderLayout.CENTER);
    }

    public void refresh() {
        bookingPanel.refresh();
        activeReservationsPanel.refresh();
    }
}
