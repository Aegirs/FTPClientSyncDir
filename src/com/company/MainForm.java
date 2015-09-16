package com.company;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * Created by thomasmazurkiewicz on 25/03/15.
 */
public class MainForm extends JFrame {
    private JTree Server;
    private JTree ClientTree;

    private JPanel rootPanel;

    private JButton addDir;
    private JTable tableErrors;
    private JTable tableSuccess;
    private JTable tableTransfers;
    private JLabel infoDataTransfers;
    private JLabel time;
    private JTextArea status;

    private PrintStream printStreamStatus;
    private String command = "";

    private SettingForm setting;
    private TreeDir treeDirServer;
    private TreeDir treeDirClient;

    private ReadWrite readWrite;
    private Vector<Byte> by = new Vector<>();


    private void initFrame() {

        setContentPane(rootPanel);
        pack();

        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
    }

    public MainForm() {
        super("Client FTP");

        // tests
        String pathTest = "/Users/thomasmazurkiewicz/Desktop/TestInte/";
        ScanFile sF = new ScanFile(pathTest+"f1",pathTest+"f2");
        System.out.println(sF.integriteFile());

        printStreamStatus = new PrintStream( new OutputStream() {
            @Override
            public void write( final int b ) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        by.add((byte)b);
                        command += (char)b;

                        if ( (char)b == '\n' ) {
                            commandStatus();
                        }
                    }
                });
            }
        } );

        menu();
        initFrame();

        addDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DefaultMutableTreeNode nodeSelected = treeDirServer.getNodeSelected();
                readWrite = new ReadWrite(treeDirServer, treeDirClient);
                Boolean isNotFound = readWrite.searchClientPath(Server, nodeSelected);

                if (isNotFound) {
                    readWrite.createClientPath(ClientTree);
                } else {
                    treeDirClient.changRoot(readWrite.getClientPath());
                }
            }
        });

        // déploiement du sous arbre d’un nœud sélectionné
        Server.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) Server.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                treeDirServer.updateTree(node);
            }
        });

        // déploiement du sous arbre d’un nœud sélectionné
        ClientTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) ClientTree.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                treeDirClient.updateTree(node);
            }
        });
    }

    private void actionCloseSetting() {
        if (setting.isLog()) {
            DefaultMutableTreeNode racine = new DefaultMutableTreeNode("/");
            Server.setModel(new DefaultTreeModel(racine));

            treeDirServer = new TreeDir(setting.getClient(),"Server",Server);
            treeDirServer.updateTree(racine);
            treeDirClient = new TreeDir(setting.getClient(),"Client",ClientTree);
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        DefaultMutableTreeNode racine = new DefaultMutableTreeNode("/");
        DefaultMutableTreeNode racineClient = new DefaultMutableTreeNode("/");

        Server = new JTree(racine);
        ClientTree = new JTree(racineClient);

        tableTransfers = TabbedTransfers.initTable();
        tableSuccess = TabbedTransfers.initTable();
        tableErrors =  TabbedTransfers.initTable();
    }

    private void menu() {
        JMenuItem menuItem;
        JMenuBar menuBar;
        JMenu menu;

        setting = new SettingForm(printStreamStatus);

        //Create the menu bar.
        menuBar = new JMenuBar();
        //Build the first menu.
        menu = new JMenu("Server");
        menu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menu);

        menuItem = new JMenuItem("Settings");
        menuItem.setMnemonic(KeyEvent.VK_B);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setting.shaw();
                setting.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        actionCloseSetting();
                        setting.saveTreeAccount();
                    }
                });
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Reference");
        menuItem.setMnemonic(KeyEvent.VK_B);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReferenceForm ref = new ReferenceForm();
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Synchronize");
        menuItem.setMnemonic(KeyEvent.VK_B);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Synchronize");
                System.out.println("ServerPath: " + readWrite.getPathServer());
                System.out.println("ClientPath: " + readWrite.getClientPath());

                TabbedTransfers tabbedTransfers = new TabbedTransfers(setting.getClient(),tableTransfers,tableSuccess,tableErrors,infoDataTransfers,time);
                tabbedTransfers.updateTableTransfers(readWrite.getPathServer(), readWrite.getClientPath());
                tabbedTransfers.start();
                //tabbedTransfers.uploadFile(readWrite.getClientPath());
            }
        });
        menuBar.add(menuItem);

        this.setJMenuBar(menuBar);
    }

    public void commandStatus() {

        byte[] res = new byte[by.size()];

        for(int i= 0 ; i < by.size();i++ ) {
            res[i] = by.get(i);
        }
        by.removeAllElements();

        try {
            command = new String(res,"UTF-8");
            System.out.println(command);

            int endCommand = command.lastIndexOf(" ");
            if ( endCommand > 0 ) {
                String subCommand = command.substring(0,endCommand);

                if ( subCommand.equals("PASS") ) {
                    command = "PASS ******\n";
                }
            }

            status.append(command);
            command = "";

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
