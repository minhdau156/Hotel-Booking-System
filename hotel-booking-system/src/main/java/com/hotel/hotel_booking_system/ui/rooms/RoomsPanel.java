package com.hotel.hotel_booking_system.ui.rooms;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.service.RoomService;

@Component
public class RoomsPanel extends JPanel {

    public RoomsPanel(RoomService roomService) {
        setLayout(new BorderLayout());

        RoomPanel roomPanel = new RoomPanel(roomService);
        RoomTypePanel roomTypePanel = new RoomTypePanel(roomService);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rooms", roomPanel);
        tabs.addTab("Room Types", roomTypePanel);
        // Đổi tab thì nạp lại để thấy tên loại phòng mới sửa
        tabs.addChangeListener(e -> {
            roomPanel.refresh();
            roomTypePanel.refresh();
        });

        add(tabs, BorderLayout.CENTER);
    }
}