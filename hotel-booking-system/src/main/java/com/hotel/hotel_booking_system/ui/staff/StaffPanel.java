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

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.model.Staff;
import com.hotel.hotel_booking_system.service.StaffService;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;


@Component
public class StaffPanel extends JPanel{
    private final StaffService staffService;
    private BaseTableModel<Staff> tableModel;

    public StaffPanel(StaffService staffService) {
        this.staffService = staffService;
        initUi();
    }

    private void initUi() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Staff");
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.NORTH);

        tableModel = new BaseTableModel<>(List.of("Username", "Full Name", "Role", "Status"), List.of(Staff::getUsername, Staff::getFullName, Staff::getRole, Staff::isActive));
        JTable staffTable = new JTable(tableModel);
        add(new JScrollPane(staffTable), BorderLayout.CENTER);

        refresh();

        addButton.addActionListener(e -> {
            StaffFormDialog.showAdd(staffService, this);
            refresh();
        });

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

    private void refresh() {
        tableModel.setRows(staffService.findAll());
    }
}
