package com.hotel.hotel_booking_system.ui.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

public class BaseTableModel<T> extends AbstractTableModel {

    private final List<String> columnNames;
    private final List<Function<T, Object>> columnExtractors;
    private List<T> rows = new ArrayList<>();

    public BaseTableModel(List<String> columnNames, List<Function<T, Object>> columnExtractors) {
        if (columnNames.size() != columnExtractors.size()) {
            throw new IllegalArgumentException("columnNames and columnExtractors must have the same length");
        }
        this.columnNames = List.copyOf(columnNames);
        this.columnExtractors = List.copyOf(columnExtractors);
    }

    public void setRows(List<T> newRows) {
        this.rows = new ArrayList<>(newRows);
        fireTableDataChanged();
    }

    public T getRowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return columnExtractors.get(columnIndex).apply(rows.get(rowIndex));
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);   
    }
    
}
