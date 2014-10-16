package net.quined.textual_filters;

// Lightly adapted source code from Guillaume Polet's solution
// for the following Stack Overflow question:
// https://stackoverflow.com/questions/15385114/jtable-color-row-and-cell-dynamically
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class StatsGui {

    private String search;

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    private class TableSearchRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setBackground(null);
            Component tableCellRendererComponent =
              super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 3) {
              String s = table.getValueAt(row, column - 1).toString();
              Color c = Color.decode(s);
              setBackground(c);
            }
            if (column == 0
                && getSearch() != null
                && getSearch().length() > 0
                && value.toString().contains(getSearch())) {
                setBackground(Color.RED);
            }
            return tableCellRendererComponent;
        }
    }

    protected void initUI(Word[] words) {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Word");
        model.addColumn("Frequencey");
        model.addColumn("Color Value");
        model.addColumn("Color");
        for (int i = 0; i < words.length; i++) {
            Vector<Object> row = new Vector<Object>();
            row.add(words[i].text);
            row.add(Integer.toString(words[i].frequency));
            String rgb = Integer.toHexString(words[i].color.getRGB());
            rgb = rgb.substring(2, rgb.length());
            row.add("0x"+rgb);
            model.addRow(row);
        }
        table = new JTable(model);
        TableSearchRenderer renderer = new TableSearchRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        textField = new JTextField(30);
        textField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSearch();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSearch();
            }
        });
        JFrame frame = new JFrame(StatsGui.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        frame.add(scrollpane, BorderLayout.CENTER);
        frame.add(textField, BorderLayout.NORTH);
        frame.setSize(500, 800);
        frame.setVisible(true);
    }

    protected void updateSearch() {
        setSearch(textField.getText());
        table.repaint();
    }

    private JTable table;
    private JTextField textField;
}
