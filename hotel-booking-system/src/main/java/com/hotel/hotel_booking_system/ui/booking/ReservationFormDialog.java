package com.hotel.hotel_booking_system.ui.booking;

import java.awt.Component;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hotel.hotel_booking_system.model.Guest;
import com.hotel.hotel_booking_system.model.Room;
import com.hotel.hotel_booking_system.model.RoomType;
import com.hotel.hotel_booking_system.service.CurrentUserContext;
import com.hotel.hotel_booking_system.service.GuestService;
import com.hotel.hotel_booking_system.service.ReservationService;
import com.hotel.hotel_booking_system.service.RoomService;
import com.hotel.hotel_booking_system.ui.common.Dialog;
import com.hotel.hotel_booking_system.ui.guests.GuestFormDialog;

public final class ReservationFormDialog {
    private ReservationFormDialog() {
    }

    public static boolean showCreate(
            Component parent,
            Room room,
            LocalDate checkIn,
            LocalDate checkOut,
            RoomService roomService,
            GuestService guestService,
            ReservationService reservationService,
            CurrentUserContext currentUserContext) {
        List<Guest> guests = guestService.findAll();
        JComboBox<Guest> guestCombo = new JComboBox<>(
                guests.toArray(new Guest[0]));
        Set<Long> existingGuestIds = new HashSet<>();
        for (Guest guest : guests) {
            existingGuestIds.add(guest.getId());
        }

        JButton newGuestButton = new JButton("New Guest");
        JPanel guestPicker = new JPanel();
        guestPicker.add(guestCombo);
        guestPicker.add(newGuestButton);
        newGuestButton.addActionListener(event -> {
            if (GuestFormDialog.showCreate(parent, guestService)) {
                List<Guest> updatedGuests = guestService.findAll();
                guestCombo.removeAllItems();
                Guest newlyAdded = null;
                for (Guest guest : updatedGuests) {
                    guestCombo.addItem(guest);
                    if (!existingGuestIds.contains(guest.getId())) {
                        newlyAdded = guest;
                    }
                }
                if (newlyAdded != null) {
                    guestCombo.setSelectedItem(newlyAdded);
                }
            }
        });

        RoomType roomType = roomService.findAllRoomTypes().stream()
                .filter(type -> type.getId().equals(room.getRoomTypeId()))
                .findFirst()
                .orElse(null);
        String roomDescription = room.getRoomNumber()
                + (roomType == null ? "" : " - " + roomType.getName());

        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        fields.put("Room", new JLabel(roomDescription));
        fields.put("Check-in", new JLabel(checkIn.toString()));
        fields.put("Check-out", new JLabel(checkOut.toString()));
        fields.put("Guest", guestPicker);
        if (guests.isEmpty()) {
            newGuestButton.doClick();
        }
        if (guestCombo.getItemCount() == 0) {
            Dialog.showError(parent, "Create a guest before making a booking.");
            return false;
        }
        if (!Dialog.showForm(parent, "Create Reservation", fields)) {
            return false;
        }

        Guest selectedGuest = (Guest) guestCombo.getSelectedItem();
        if (selectedGuest == null) {
            Dialog.showError(parent, "Select a guest for this reservation.");
            return false;
        }
        var staff = currentUserContext.getCurrentStaff();
        if (staff.isEmpty() || staff.get().getId() == null) {
            Dialog.showError(parent, "Please log in before making a booking.");
            return false;
        }
        try {
            reservationService.create(
                    room.getId(),
                    selectedGuest.getId(),
                    staff.get().getId(),
                    checkIn,
                    checkOut);
            return true;
        } catch (IllegalArgumentException exception) {
            Dialog.showError(parent, exception.getMessage());
            return false;
        }
    }
}
