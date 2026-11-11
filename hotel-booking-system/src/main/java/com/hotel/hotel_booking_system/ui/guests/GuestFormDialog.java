package com.hotel.hotel_booking_system.ui.guests;

import com.hotel.hotel_booking_system.model.Guest;
import com.hotel.hotel_booking_system.service.GuestService;
import com.hotel.hotel_booking_system.ui.common.Dialog;
import java.awt.Component;
import java.util.LinkedHashMap;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class GuestFormDialog {

    public static boolean showCreate(Component parent, GuestService guestService) {
        return show(parent, guestService, new Guest());
    }

    public static boolean showEdit(Component parent, GuestService guestService, Guest existing) {
        return show(parent, guestService, existing);
    }

    private static boolean show(Component parent, GuestService guestService, Guest guest) {

        // Work on a copy so a failed validation does not change the object shown in the table
        Guest working = new Guest(guest.getId(), guest.getFullName(), guest.getPhone(),
                guest.getEmail(), guest.getIdNumber());

        JTextField nameField = new JTextField(working.getFullName() == null ? "" : working.getFullName());
        JTextField phoneField = new JTextField(working.getPhone() == null ? "" : working.getPhone());
        JTextField emailField = new JTextField(working.getEmail() == null ? "" : working.getEmail());

        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        fields.put("Full name", nameField);
        fields.put("Phone", phoneField);
        fields.put("Email", emailField);

        String title = working.getId() > 0 ? "Edit Guest" : "Add Guest";

        boolean confirmed = Dialog.showForm(parent, title, fields);

        if (!confirmed) {
            return false;
        }

        working.setFullName(nameField.getText().trim());
        working.setPhone(phoneField.getText().trim());
        working.setEmail(emailField.getText().trim());

        try {
            guestService.save(working);
            return true;
        } catch (IllegalArgumentException e) {
            Dialog.showError(parent, e.getMessage());
            return false;
        }
    }
}