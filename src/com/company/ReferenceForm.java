package com.company;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

/**
 * Created by thomasmazurkiewicz on 29/03/15.
 */
public class ReferenceForm extends JFrame{
    private JPanel referencePanel;
    private JButton deleteAllButton;
    private JTable referenceTable;
    private ReadWrite readWrite;

    private void initFrame() {
        pack();
        setContentPane(referencePanel);

        setSize(400, 400);
        setVisible(true);
    }

    public ReferenceForm() {
        super("Reference");
        initFrame();

        deleteAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readWrite.clearContent();
            }
        });

        referenceTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == e.UPDATE) {
                    System.out.println(e.getColumn());
                    System.out.println(e.getFirstRow());
                    System.out.println(e.getLastRow());
                }
            }
        });

        referenceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String selectedData = null;

                int[] selectedRow = referenceTable.getSelectedRows();
                int[] selectedColumns = referenceTable.getSelectedColumns();

                for (int i = 0; i < selectedRow.length; i++) {
                    for (int j = 0; j < selectedColumns.length; j++) {
                        selectedData = (String) referenceTable.getValueAt(selectedRow[i], selectedColumns[j]);
                    }
                }
                System.out.println("Selected: " + selectedData);
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        readWrite = new ReadWrite(null,null);
        readWrite.openFile();
        Vector< Vector > list = readWrite.monitorReference();
        Vector<String> title = new Vector<String>();
        title.addElement("Path Server");
        title.addElement("Path Client");

        referenceTable = new JTable(list,title);

    }
}
