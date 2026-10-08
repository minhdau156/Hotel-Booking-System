package com.hotel.hotel_booking_system.ui.staff;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import java.awt.Component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;

import com.hotel.hotel_booking_system.model.Role;

import com.hotel.hotel_booking_system.model.Staff;
import com.hotel.hotel_booking_system.service.StaffService;
import com.hotel.hotel_booking_system.ui.common.Dialog;

public final class StaffFormDialog {
    private static final JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
    private static final JTextField userNameField = new JTextField(20);
    private static final JTextField fullNameField = new JTextField(20);
    private static final JPasswordField passwordField = new JPasswordField(20);
    private static final JCheckBox activeCheckBox = new JCheckBox("Active");

    public StaffFormDialog() {
        
    }

    private boolean show(Staff existing, Component parent) {
        boolean isEdit = existing != null;
        userNameField.setText(existing == null ? "" : existing.getUsername());
        fullNameField.setText(existing == null ? "" : existing.getFullName());
        roleCombo.setSelectedItem(existing == null ? null : existing.getRole());
        activeCheckBox.setSelected(existing == null ? true : existing.isActive());
        
        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        if (isEdit) {
            fields.put("Username", userNameField);
            fields.put("Full name", fullNameField);
            fields.put("Role", roleCombo);
            fields.put("Status", activeCheckBox);
        }
        else {
            fields.put("Username", userNameField);
            fields.put("Password", passwordField);
            fields.put("Full name", fullNameField);
            fields.put("Role", roleCombo);
        }
        return Dialog.showForm(parent, isEdit ? "Edit staff" : "Add staff", fields);
    }

    public static void showAdd(StaffService staffService, Component parent) {
        boolean isOk = new StaffFormDialog().show(null, parent);
        if (!isOk) {
            return;
        }

        try {
            Staff staff = new Staff();
            staff.setUsername(userNameField.getText());
            staff.setFullName(fullNameField.getText());
            staff.setRole((Role) roleCombo.getSelectedItem());
            
            staffService.create(staff, new String(passwordField.getPassword()));
            
        }
        catch (Exception e) {
            Dialog.showError(parent, e.getMessage());
            
        }
    }


    public static void showEdit(StaffService staffService, Staff existing, Component parent) {
        boolean isOk = new StaffFormDialog().show(existing, parent);
        if (!isOk) {
            return;
        }
        try {
            Staff staff = existing;
            staff.setUsername(userNameField.getText());
            staff.setFullName(fullNameField.getText());
            staff.setRole((Role) roleCombo.getSelectedItem());
            staffService.update(existing, userNameField.getText(), fullNameField.getText(), (Role) roleCombo.getSelectedItem(), activeCheckBox.isSelected());
        }
        catch (Exception e) {
            Dialog.showError(parent, e.getMessage());
        }
    }
}
