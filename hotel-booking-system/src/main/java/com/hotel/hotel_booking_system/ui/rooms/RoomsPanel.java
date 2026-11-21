package com.hotel.hotel_booking_system.ui.rooms;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.service.RoomService;
import com.hotel.hotel_booking_system.ui.common.Refreshable;

@Component
public class RoomsPanel extends JPanel implements Refreshable {

    private final RoomPanel roomPanel;
    private final RoomTypePanel roomTypePanel;

    public RoomsPanel(RoomService roomService) {
        setLayout(new BorderLayout());

        roomPanel = new RoomPanel(roomService);
        roomTypePanel = new RoomTypePanel(roomService);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rooms", roomPanel);
        tabs.addTab("Room Types", roomTypePanel);
        // Đổi tab thì nạp lại để thấy tên loại phòng mới sửa
        tabs.addChangeListener(e -> refresh());

        add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refresh() {
        roomPanel.refresh();
        roomTypePanel.refresh();
    }
}