package com.hotel.hotel_booking_system.ui.rooms;

import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.hotel.hotel_booking_system.model.Room;
import com.hotel.hotel_booking_system.model.RoomStatus;
import com.hotel.hotel_booking_system.model.RoomType;
import com.hotel.hotel_booking_system.service.RoomService;
import com.hotel.hotel_booking_system.ui.common.Dialog;

public final class RoomFormDialog {

    private RoomFormDialog() {
    }

    /** @return true nếu đã lưu thành công */
    public static boolean showCreate(Component parent, RoomService service) {
        return show(parent, service, null);
    }

    public static boolean showEdit(Component parent, RoomService service, Room existing) {
        return show(parent, service, existing);
    }

    private static boolean show(Component parent, RoomService service, Room existing) {
        boolean editing = existing != null;

        List<RoomType> types = service.findAllRoomTypes();
        if (types.isEmpty()) {
            Dialog.showError(parent, "Create a room type first.");
            return false;
        }

        JTextField numberField = new JTextField(editing ? existing.getRoomNumber() : "", 10);

        // Combo hiển thị tên nhờ RoomType.toString()
        JComboBox<RoomType> typeCombo = new JComboBox<>(types.toArray(new RoomType[0]));
        if (editing) {
            for (RoomType t : types) {
                if (t.getId().equals(existing.getRoomTypeId())) {
                    typeCombo.setSelectedItem(t);
                    break;
                }
            }
        }

        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        fields.put("Room number", numberField);
        fields.put("Room type", typeCombo);

        if (!Dialog.showForm(parent, editing ? "Edit room" : "Add room", fields)) {
            return false;
        }

        try {
            RoomType selectedType = (RoomType) typeCombo.getSelectedItem();
            Room room = new Room(
                    editing ? existing.getId() : null,
                    numberField.getText(),
                    selectedType.getId(),
                    editing ? existing.getStatus() : RoomStatus.AVAILABLE);

            service.save(room);
            return true;
        } catch (IllegalArgumentException e) {
            Dialog.showError(parent, e.getMessage());
            return false;
        }
    }
}