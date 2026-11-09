package com.hotel.hotel_booking_system.ui.rooms;

import java.awt.Component;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.hotel.hotel_booking_system.model.RoomType;
import com.hotel.hotel_booking_system.service.RoomService;
import com.hotel.hotel_booking_system.ui.common.Dialog;

public final class RoomTypeFormDialog {

    private RoomTypeFormDialog() {
    }

    /** @return true nếu đã lưu thành công */
    public static boolean showCreate(Component parent, RoomService service) {
        return show(parent, service, null);
    }

    public static boolean showEdit(Component parent, RoomService service, RoomType existing) {
        return show(parent, service, existing);
    }

    private static boolean show(Component parent, RoomService service, RoomType existing) {
        boolean editing = existing != null;

        JTextField nameField = new JTextField(editing ? existing.getName() : "", 20);
        JTextField priceField = new JTextField(editing ? existing.getBasePrice().toPlainString() : "");
        JTextField capacityField = new JTextField(editing ? String.valueOf(existing.getCapacity()) : "");
        JTextField descriptionField = new JTextField(
                editing && existing.getDescription() != null ? existing.getDescription() : "");

        LinkedHashMap<String, JComponent> fields = new LinkedHashMap<>();
        fields.put("Name", nameField);
        fields.put("Base price", priceField);
        fields.put("Capacity", capacityField);
        fields.put("Description", descriptionField);

        if (!Dialog.showForm(parent, editing ? "Edit room type" : "Add room type", fields)) {
            return false; // bấm Cancel
        }

        try {
            // Tạo object mới để không làm bẩn dữ liệu trong bảng nếu lưu lỗi
            RoomType rt = new RoomType();
            rt.setId(editing ? existing.getId() : null);
            rt.setName(nameField.getText());
            rt.setBasePrice(new BigDecimal(priceField.getText().trim()));
            rt.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            rt.setDescription(descriptionField.getText().trim());

            service.saveRoomType(rt);
            return true;
        } catch (NumberFormatException e) {
            // Phải catch TRƯỚC IllegalArgumentException vì nó là lớp con của IllegalArgumentException
            Dialog.showError(parent, "Base price and capacity must be valid numbers.");
        } catch (IllegalArgumentException e) {
            Dialog.showError(parent, e.getMessage());
        }
        return false;
    }
}