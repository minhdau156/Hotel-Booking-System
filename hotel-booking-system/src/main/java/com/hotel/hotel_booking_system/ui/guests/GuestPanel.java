package com.hotel.hotel_booking_system.ui.guests;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.hotel.hotel_booking_system.model.Guest;
import com.hotel.hotel_booking_system.service.GuestService;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;

public class GuestPanel extends JPanel{

    private final GuestService guestService;
    private final BaseTableModel<Guest> tableModel;
    private final JTable table;
    private final JTextField searchField = new JTextField(20);

    public GuestPanel(GuestService guestService){

        this.guestService = guestService;

        setLayout(new BorderLayout());

        JPanel searchBar = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        JButton searchBtn = new JButton("Search");
        JButton addBtn = new JButton("Add Guest");

        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(addBtn);

        add(searchBar, BorderLayout.NORTH);

        tableModel = new BaseTableModel<>(
                List.of("Name", "Phone", "Email"),
                List.of(
                        Guest::getFullName,
                        Guest::getPhone,
                        Guest::getEmail
                )
        );

        table = new JTable(tableModel);

        add(new JScrollPane(table), BorderLayout.CENTER);

        searchBtn.addActionListener(e->refresh());

        searchField.addActionListener(e->refresh());

        addBtn.addActionListener(e->{
            if (GuestFormDialog.showCreate(this, guestService)){
                refresh();
            }
        });

        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0){

                    int selectedRow = table.getSelectedRow();
                    Guest selected = tableModel.getRowAt(table.convertRowIndexToModel(selectedRow));

                    if (GuestFormDialog.showEdit(GuestPanel.this, guestService, selected)){
                        refresh();
                    }
                }
            }
        });

        refresh();
    }

    public void refresh(){
        String query = searchField.getText();

        List<Guest> guests = query == null || query.isBlank() ? guestService.findAll() : guestService.search(query.trim());

        tableModel.setRows(guests);
    }
}
