package com.hotel.hotel_booking_system.ui.staff;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.model.Staff;
import com.hotel.hotel_booking_system.service.StaffService;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;
import com.hotel.hotel_booking_system.ui.common.Dialog;


@Component
public class StaffPanel extends JPanel{
    private final StaffService staffService;
    private BaseTableModel<Staff> tableModel;
    private JTable staffTable;
    private final JButton deleteButton = new JButton("Delete");

    public StaffPanel(StaffService staffService) {
        this.staffService = staffService;
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Staff");
        deleteButton.setEnabled(false);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.NORTH);

        tableModel = new BaseTableModel<>(List.of("Username", "Full Name", "Role", "Status"), List.of(Staff::getUsername, Staff::getFullName, Staff::getRole, Staff::isActive));
        staffTable = new JTable(tableModel);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(staffTable), BorderLayout.CENTER);

        refresh();

        addButton.addActionListener(e -> {
            StaffFormDialog.showAdd(staffService, this);
            refresh();
        });

        staffTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteButton.setEnabled(staffTable.getSelectedRow() >= 0);
            }
        });

        deleteButton.addActionListener(e -> deleteSelected());

        staffTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Staff selected = tableModel.getRowAt(staffTable.getSelectedRow());
                    StaffFormDialog.showEdit(staffService, selected, StaffPanel.this);
                    refresh();
                }
            }
        });
    }

    private void deleteSelected() {
        int row = staffTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        Staff selected = tableModel.getRowAt(row);
        if (!Dialog.showConfirm(this, "Delete staff " + selected.getUsername() + "?")) {
            return;
        }
        try {
            staffService.delete(selected.getId());
            refresh();
        } catch (IllegalArgumentException ex) {
            Dialog.showError(this, ex.getMessage());
        }
    }

    private void refresh() {
        tableModel.setRows(staffService.findAll());
        deleteButton.setEnabled(staffTable.getSelectedRow() >= 0);
    }
}
