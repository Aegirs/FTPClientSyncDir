package com.company;

import apple.laf.JRSUIUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by thomasmazurkiewicz on 28/03/15.
 */
public class ReadWrite {
    private String clientPath;
    private Boolean notFound;
    private TreeDir treeRead;
    private TreeDir treeWrite;

    private String pathReference = "./reference.txt";
    private String pathServer = "";
    File file;

    public ReadWrite(TreeDir treeR,TreeDir treeW) {
        treeRead = treeR;
        treeWrite = treeW;

        clientPath = "";
        notFound = true;

        openFile();
    }

    public String getClientPath() {
        return clientPath;
    }

    public Boolean isNotFound() {
        return notFound;
    }

    public String getPathServer() {
        return pathServer;
    }

    // create file if not existss
    public void openFile() {
        file = new File(pathReference);

        try {
            if (file.createNewFile()){
                System.out.println("File is created!");
            }else{
                System.out.println("File already exists.");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //verification du path dans le fichier de ref
    // si il est dedans alors on open le dossier client dans le graph
    public Boolean searchClientPath(JTree tree,DefaultMutableTreeNode lastNode) {
        BufferedReader read = null;
        pathServer = treeRead.getNodePath(tree,lastNode);

        try {
            read = new BufferedReader(new FileReader(file));
            String line = null;

            try {
                while( ( line = read.readLine() ) != null ) {
                    int sep = line.indexOf(";");
                    String pathServerRef = line.substring(0, sep);
                    String pathClient = line.substring(sep+1);

                    if (pathServerRef.equals(pathServer)) {
                        clientPath = pathClient;
                        notFound = false;
                        break;
                    }
                }
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return notFound;
    }

    public void createClientPath(JTree ClientTree) {
        FileChooser chooser = new FileChooser();
        chooser.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.out.println("Close");
                try {
                    clientPath = chooser.getFileChoose();

                    if ( clientPath != null ) {
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
                        writer.println(pathServer + ";" + clientPath);
                        writer.close();
                        treeWrite.changRoot(clientPath);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public Vector<Vector> monitorReference() {
        BufferedReader read = null;

        Vector<Vector> paths = new Vector<Vector>();
        try {
            read = new BufferedReader(new FileReader(file));
            String line = null;

            try {
                while( ( line = read.readLine() ) != null ) {
                    int sep = line.indexOf(";");
                    String pathServerRef = line.substring(0, sep);
                    String pathClient = line.substring(sep+1);

                    Vector<String> link = new Vector<String>();

                    link.addElement(pathServerRef);
                    link.addElement(pathClient);

                    paths.add(link);
                }
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return paths;
    }

    public void clearContent() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            writer.print("");
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
