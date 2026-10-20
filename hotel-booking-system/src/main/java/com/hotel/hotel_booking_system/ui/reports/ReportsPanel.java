
package com.hotel.hotel_booking_system.ui.reports;

import com.hotel.hotel_booking_system.service.ReportService;
import com.hotel.hotel_booking_system.service.ReportService.OccupancyRow;
import com.hotel.hotel_booking_system.service.ReportService.RevenueRow;
import com.hotel.hotel_booking_system.ui.common.BaseTableModel;
import com.hotel.hotel_booking_system.ui.common.Dialog;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ReportsPanel extends JPanel {

    private final ReportService reportService;

    private final BaseTableModel<OccupancyRow> occupancyModel =
        new BaseTableModel<>(
            List.of(
                "Room Type",
                "Nights Booked",
                "Occupancy Rate"
            ),
            List.of(
                row -> row.roomTypeName(),
                row -> row.nightsBooked(),
                row -> String.format(
                    "%.2f%%",
                    row.occupancyRate()
                )
            )
        );

    private final BaseTableModel<RevenueRow> revenueModel =
        new BaseTableModel<>(
            List.of("Period", "Total Revenue"),
            List.of(
                row -> row.period(),
                row -> row.totalRevenue()
            )
        );

    private final JTable occupancyTable =
        new JTable(occupancyModel);

    private final JTable revenueTable =
        new JTable(revenueModel);

    private final JSpinner occupancyFrom = createDateSpinner();
    private final JSpinner occupancyTo = createDateSpinner();
    private final JSpinner revenueFrom = createDateSpinner();
    private final JSpinner revenueTo = createDateSpinner();

    public ReportsPanel(ReportService reportService) {
        this.reportService = reportService;

        setLayout(new BorderLayout(8, 8));
        setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab(
            "Occupancy",
            createOccupancyPanel()
        );

        tabs.addTab(
            "Revenue",
            createRevenuePanel()
        );

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createOccupancyPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JButton runButton = new JButton("Run");

        JPanel filter = createFilterBar(
            occupancyFrom,
            occupancyTo,
            runButton
        );

        occupancyTable.setFillsViewportHeight(true);

        JScrollPane scrollPane =
            new JScrollPane(occupancyTable);

        panel.add(filter, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        runButton.addActionListener(event -> {
            try {
                LocalDate from = readDate(occupancyFrom);
                LocalDate to = readDate(occupancyTo);

                if (from.isAfter(to)) {
                    Dialog.showError(
                        this,
                        "Ngày bắt đầu không được sau ngày kết thúc."
                    );
                    return;
                }

                List<OccupancyRow> rows =
                    reportService.occupancyReport(from, to);

                occupancyModel.setRows(rows);

            } catch (RuntimeException ex) {
                showError(ex);
            }
        });

        return panel;
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JButton runButton = new JButton("Run");

        JPanel filter = createFilterBar(
            revenueFrom,
            revenueTo,
            runButton
        );

        revenueTable.setFillsViewportHeight(true);

        JScrollPane scrollPane =
            new JScrollPane(revenueTable);

        panel.add(filter, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        runButton.addActionListener(event -> {
            try {
                LocalDate from = readDate(revenueFrom);
                LocalDate to = readDate(revenueTo);

                if (from.isAfter(to)) {
                    Dialog.showError(
                        this,
                        "Ngày bắt đầu không được sau ngày kết thúc."
                    );
                    return;
                }

                List<RevenueRow> rows =
                    reportService.revenueByPeriod(from, to);

                revenueModel.setRows(rows);

            } catch (RuntimeException ex) {
                showError(ex);
            }
        });

        return panel;
    }

    private JPanel createFilterBar(
            JSpinner fromSpinner,
            JSpinner toSpinner,
            JButton runButton) {

        JPanel panel =
            new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("From:"));
        panel.add(fromSpinner);

        panel.add(new JLabel("To:"));
        panel.add(toSpinner);

        panel.add(runButton);

        return panel;
    }

    private static JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(
            new Date(),
            null,
            null,
            java.util.Calendar.DAY_OF_MONTH
        );

        JSpinner spinner = new JSpinner(model);

        spinner.setEditor(
            new JSpinner.DateEditor(
                spinner,
                "dd/MM/yyyy"
            )
        );

        return spinner;
    }

    private static LocalDate readDate(JSpinner spinner) {
        Date date = (Date) spinner.getValue();

        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }

    private void showError(RuntimeException ex) {
        Dialog.showError(
            this,
            ex.getMessage() == null
                ? "Không thể tải báo cáo."
                : ex.getMessage()
        );
    }
}
