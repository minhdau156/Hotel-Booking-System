package com.hotel.hotel_booking_system.ui.rooms;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.hotel.hotel_booking_system.model.RoomType;
import com.hotel.hotel_booking_system.service.RoomService;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;
import com.hotel.hotel_booking_system.ui.common.Dialog;

public class RoomTypePanel extends JPanel {

    private final RoomService roomService;
    private final BaseTableModel<RoomType> tableModel;
    private final JTable table;
    private final JButton deleteButton = new JButton("Delete");

    public RoomTypePanel(RoomService roomService) {
        this.roomService = roomService;
        setLayout(new BorderLayout());

        JButton addButton = new JButton("Add Room Type");
        deleteButton.setEnabled(false);
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(addButton);
        north.add(deleteButton);
        add(north, BorderLayout.NORTH);

        tableModel = new BaseTableModel<>(
                List.of("Name", "Base Price", "Capacity", "Description"),
                List.<Function<RoomType, Object>>of(
                        RoomType::getName,
                        RoomType::getBasePrice,
                        RoomType::getCapacity,
                        RoomType::getDescription));
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteButton.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        addButton.addActionListener(e -> {
            if (RoomTypeFormDialog.showCreate(this, roomService)) {
                refresh();
            }
        });

        deleteButton.addActionListener(e -> deleteSelected());

        // Double-click một dòng để sửa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        RoomType selected = tableModel.getRowAt(row);
                        if (RoomTypeFormDialog.showEdit(RoomTypePanel.this, roomService, selected)) {
                            refresh();
                        }
                    }
                }
            }
        });

        refresh(); // để bảng không rỗng ở lần hiện đầu tiên
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        RoomType selected = tableModel.getRowAt(row);
        if (!Dialog.showConfirm(this, "Delete room type " + selected.getName() + "?")) {
            return;
        }
        try {
            roomService.deleteRoomType(selected.getId());
            refresh();
        } catch (IllegalArgumentException ex) {
            Dialog.showError(this, ex.getMessage());
        }
    }

    public void refresh() {
        tableModel.setRows(roomService.findAllRoomTypes());
        deleteButton.setEnabled(table.getSelectedRow() >= 0);
    }
}