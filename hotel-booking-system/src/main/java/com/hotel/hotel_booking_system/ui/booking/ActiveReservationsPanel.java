package com.hotel.hotel_booking_system.ui.booking;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.model.ReservationStatus;
import com.hotel.hotel_booking_system.model.ReservationView;
import com.hotel.hotel_booking_system.service.ReservationService;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;
import com.hotel.hotel_booking_system.ui.common.Dialog;

@Component
public class ActiveReservationsPanel extends JPanel {
    private final ReservationService reservationService;
    private final BaseTableModel<ReservationView> tableModel =
            new BaseTableModel<>(
                    List.of("Room Number", "Guest Name", "Check-in",
                            "Check-out", "Status"),
                    List.of(
                            ReservationView::getRoomNumber,
                            ReservationView::getGuestName,
                            ReservationView::getCheckIn,
                            ReservationView::getCheckOut,
                            ReservationView::getStatus));
    private final JTable table = new JTable(tableModel);
    private final JButton checkInButton = new JButton("Check In");
    private final JButton checkOutButton = new JButton("Check Out");
    private final JButton cancelButton = new JButton("Cancel");

    public ActiveReservationsPanel(ReservationService reservationService) {
        this.reservationService = reservationService;
        setLayout(new BorderLayout());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(checkInButton);
        actions.add(checkOutButton);
        actions.add(cancelButton);
        add(actions, BorderLayout.SOUTH);

        checkInButton.setEnabled(false);
        checkOutButton.setEnabled(false);
        cancelButton.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                updateActions();
            }
        });
        checkInButton.addActionListener(event ->
                performAction(reservationService::checkIn));
        checkOutButton.addActionListener(event ->
                performAction(reservationService::checkOut));
        cancelButton.addActionListener(event ->
                performAction(reservationService::cancel));
        refresh();
    }

    public void refresh() {
        tableModel.setRows(reservationService.findActiveWithDetails());
        updateActions();
    }

    private void updateActions() {
        int selectedRow = table.getSelectedRow();
        ReservationStatus status = selectedRow < 0
                ? null
                : tableModel.getRowAt(
                        table.convertRowIndexToModel(selectedRow)).getStatus();
        checkInButton.setEnabled(status == ReservationStatus.BOOKED);
        checkOutButton.setEnabled(status == ReservationStatus.CHECKED_IN);
        cancelButton.setEnabled(status == ReservationStatus.BOOKED
                || status == ReservationStatus.CHECKED_IN);
    }

    private void performAction(ReservationAction action) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        long reservationId = tableModel.getRowAt(
                table.convertRowIndexToModel(selectedRow)).getId();
        try {
            action.run(reservationId);
            refresh();
        } catch (IllegalArgumentException exception) {
            Dialog.showError(this, exception.getMessage());
            refresh();
        }
    }

    @FunctionalInterface
    private interface ReservationAction {
        void run(long reservationId);
    }
}
