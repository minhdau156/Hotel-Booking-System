package com.hotel.hotel_booking_system.ui.booking;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;

import org.springframework.stereotype.Component;

import com.hotel.hotel_booking_system.model.Room;
import com.hotel.hotel_booking_system.model.RoomType;
import com.hotel.hotel_booking_system.service.CurrentUserContext;
import com.hotel.hotel_booking_system.service.GuestService;
import com.hotel.hotel_booking_system.service.ReservationService;
import com.hotel.hotel_booking_system.service.RoomService;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;
import com.hotel.hotel_booking_system.ui.common.Dialog;

@Component
public class BookingPanel extends JPanel {
    private final RoomService roomService;
    private final GuestService guestService;
    private final ReservationService reservationService;
    private final CurrentUserContext currentUserContext;
    private final JSpinner checkInSpinner = dateSpinner(LocalDate.now());
    private final JSpinner checkOutSpinner =
            dateSpinner(LocalDate.now().plusDays(1));
    private final JButton bookButton = new JButton("Book Selected Room");
    private final BaseTableModel<Room> tableModel;
    private final JTable roomTable;
    private Map<Long, RoomType> roomTypesById = Map.of();

    public BookingPanel(
            RoomService roomService,
            GuestService guestService,
            ReservationService reservationService,
            CurrentUserContext currentUserContext) {
        this.roomService = roomService;
        this.guestService = guestService;
        this.reservationService = reservationService;
        this.currentUserContext = currentUserContext;

        setLayout(new BorderLayout(8, 8));
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton searchButton = new JButton("Search");
        checkInSpinner.setName("checkIn");
        checkOutSpinner.setName("checkOut");
        searchBar.add(new JLabel("Check-in:"));
        searchBar.add(checkInSpinner);
        searchBar.add(new JLabel("Check-out:"));
        searchBar.add(checkOutSpinner);
        searchBar.add(searchButton);
        add(searchBar, BorderLayout.NORTH);

        roomTypesById = roomService.findAllRoomTypes().stream()
                .collect(Collectors.toMap(
                        RoomType::getId,
                        Function.identity()));
        tableModel = new BaseTableModel<>(
                List.of("Room Number", "Type", "Capacity"),
                List.of(
                        Room::getRoomNumber,
                        room -> {
                            RoomType type = roomTypesById.get(
                                    room.getRoomTypeId());
                            return type == null ? "Unknown" : type.getName();
                        },
                        room -> {
                            RoomType type = roomTypesById.get(
                                    room.getRoomTypeId());
                            return type == null ? "" : type.getCapacity();
                        }));
        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setAutoCreateRowSorter(true);
        add(new JScrollPane(roomTable), BorderLayout.CENTER);

        bookButton.setEnabled(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(bookButton);
        add(actions, BorderLayout.SOUTH);

        searchButton.addActionListener(event -> search());
        checkInSpinner.addChangeListener(event -> search());
        checkOutSpinner.addChangeListener(event -> search());
        roomTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                bookButton.setEnabled(roomTable.getSelectedRow() >= 0);
            }
        });
        bookButton.addActionListener(event -> bookSelectedRoom());
        search();
    }

    public void refresh() {
        roomTypesById = roomService.findAllRoomTypes().stream()
                .collect(Collectors.toMap(
                        RoomType::getId,
                        Function.identity()));
        search();
    }

    private void search() {
        LocalDate checkIn = getDate(checkInSpinner);
        LocalDate checkOut = getDate(checkOutSpinner);
        if (!checkOut.isAfter(checkIn)) {
            tableModel.setRows(List.of());
            bookButton.setEnabled(false);
            return;
        }
        try {
            tableModel.setRows(
                    roomService.findAvailable(checkIn, checkOut));
            bookButton.setEnabled(false);
        } catch (IllegalArgumentException exception) {
            Dialog.showError(this, exception.getMessage());
        }
    }

    private void bookSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }
        Room room = tableModel.getRowAt(
                roomTable.convertRowIndexToModel(selectedRow));
        if (ReservationFormDialog.showCreate(
                this,
                room,
                getDate(checkInSpinner),
                getDate(checkOutSpinner),
                roomService,
                guestService,
                reservationService,
                currentUserContext)) {
            search();
        }
    }

    private static JSpinner dateSpinner(LocalDate date) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setValue(Date.from(date.atStartOfDay(ZoneId.systemDefault())
                .toInstant()));
        return spinner;
    }

    private static LocalDate getDate(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
