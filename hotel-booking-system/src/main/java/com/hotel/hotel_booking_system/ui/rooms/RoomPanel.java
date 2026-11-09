package com.hotel.hotel_booking_system.ui.rooms;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.hotel.hotel_booking_system.model.Room;
import com.hotel.hotel_booking_system.model.RoomStatus;
import com.hotel.hotel_booking_system.model.RoomType;
import com.hotel.hotel_booking_system.service.RoomService;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;
import com.hotel.hotel_booking_system.ui.common.Dialog;

public class RoomPanel extends JPanel {

    private final RoomService roomService;
    private final BaseTableModel<Room> tableModel;
    private final JTable table;
    private final JButton changeStatusButton = new JButton("Change Status");
    private final JButton deleteButton = new JButton("Delete");

    // roomTypeId -> tên loại phòng, nạp lại mỗi lần refresh()
    private Map<Long, String> typeNames = new HashMap<>();

    public RoomPanel(RoomService roomService) {
        this.roomService = roomService;
        setLayout(new BorderLayout());

        JButton addButton = new JButton("Add Room");
        changeStatusButton.setEnabled(false);
        deleteButton.setEnabled(false);
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(addButton);
        north.add(changeStatusButton);
        north.add(deleteButton);
        add(north, BorderLayout.NORTH);

        tableModel = new BaseTableModel<>(
                List.of("Room Number", "Room Type", "Status"),
                List.<Function<Room, Object>>of(
                        Room::getRoomNumber,
                        r -> typeNames.getOrDefault(r.getRoomTypeId(), "?"),
                        Room::getStatus));
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Các nút chỉ bật khi có dòng được chọn
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtons();
            }
        });

        addButton.addActionListener(e -> {
            if (RoomFormDialog.showCreate(this, roomService)) {
                refresh();
            }
        });

        changeStatusButton.addActionListener(e -> changeStatus());
        deleteButton.addActionListener(e -> deleteSelected());

        // Double-click một dòng để sửa
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        Room selected = tableModel.getRowAt(row);
                        if (RoomFormDialog.showEdit(RoomPanel.this, roomService, selected)) {
                            refresh();
                        }
                    }
                }
            }
        });

        refresh();
    }

    private void updateButtons() {
        boolean selected = table.getSelectedRow() >= 0;
        changeStatusButton.setEnabled(selected);
        deleteButton.setEnabled(selected);
    }

    private void changeStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        Room selected = tableModel.getRowAt(row);

        JComboBox<RoomStatus> statusCombo = new JComboBox<>(RoomStatus.values());
        statusCombo.setSelectedItem(selected.getStatus());

        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        fields.put("Status", statusCombo);

        if (!Dialog.showForm(this, "Change status - room " + selected.getRoomNumber(), fields)) {
            return;
        }

        Room updated = new Room(selected.getId(), selected.getRoomNumber(),
                selected.getRoomTypeId(), (RoomStatus) statusCombo.getSelectedItem());
        try {
            roomService.save(updated);
            refresh();
        } catch (IllegalArgumentException ex) {
            Dialog.showError(this, ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        Room selected = tableModel.getRowAt(row);
        if (!Dialog.showConfirm(this, "Delete room " + selected.getRoomNumber() + "?")) {
            return;
        }
        try {
            roomService.deleteRoom(selected.getId());
            refresh();
        } catch (IllegalArgumentException ex) {
            Dialog.showError(this, ex.getMessage());
        }
    }

    public void refresh() {
        Map<Long, String> names = new HashMap<>();
        for (RoomType t : roomService.findAllRoomTypes()) {
            names.put(t.getId(), t.getName());
        }
        typeNames = names; // gán TRƯỚC setRows để extractor thấy dữ liệu mới
        tableModel.setRows(roomService.findAll());
        updateButtons();
    }
}