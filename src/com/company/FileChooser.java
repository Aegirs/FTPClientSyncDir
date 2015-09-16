package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Created by thomasmazurkiewicz on 28/03/15.
 */
public class FileChooser extends JFrame {
    private JPanel panel;
    private JFileChooser fileChooser;

    private String path;

    private void initFrame() {

        setContentPane(panel);
        pack();

        setSize(600, 400);

        setVisible(true);
    }

    public FileChooser() {
        super("FileChooser");
        initFrame();

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser theFileChooser = (JFileChooser) e.getSource();
                String command = e.getActionCommand();

                if (command.equals(JFileChooser.APPROVE_SELECTION)) {
                    File selectedFile = theFileChooser.getSelectedFile();
                    path = selectedFile.getPath();
                    eventCloseWindow();

                } else if (command.equals(JFileChooser.CANCEL_SELECTION)) {
                    System.out.println(JFileChooser.CANCEL_SELECTION);
                    dispose();
                }
            }
        });
    }

    private void eventCloseWindow() {
        WindowEvent closingEvent = new WindowEvent(FileChooser.this,
                WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(closingEvent);
    }

    public String getFileChoose() {
        return path;
    }

}
