package com.hotel.hotel_booking_system.ui.guests;

import java.awt.Component;
import java.util.LinkedHashMap;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.hotel.hotel_booking_system.model.Guest;
import com.hotel.hotel_booking_system.service.GuestService;
import com.hotel.hotel_booking_system.ui.common.Dialog;

public class GuestFormDialog{

    public static boolean showCreate(Component parent, GuestService guestService){
        return show(parent, guestService, new Guest());
    }

    public static boolean showEdit(Component parent, GuestService guestService, Guest guest){
        return show(parent, guestService, guest);
    }

    private static boolean show(Component parent, GuestService guestService, Guest guest){

        JTextField nameField = new JTextField(guest.getFullName() == null ? "" : guest.getFullName());

        JTextField phoneField = new JTextField(guest.getPhone() == null ? "" : guest.getPhone());

        JTextField emailField = new JTextField(guest.getEmail() == null ? "" : guest.getEmail());

        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        fields.put("Name", nameField);
        fields.put("Phone", phoneField);
        fields.put("Email", emailField);

        String title = guest.getId() > 0 ? "Edit Guest" : "Add Guest";

        boolean confirmed = Dialog.showForm(parent, title, fields);

        if (!confirmed){
            return false;
        }

        guest.setFullName(nameField.getText().trim());
        guest.setPhone(phoneField.getText().trim());
        guest.setEmail(emailField.getText().trim());

        try{
            guestService.save(guest);
            return true;
        } catch(IllegalArgumentException e){
            Dialog.showError(parent, e.getMessage());
            return false;
        }
    }
}