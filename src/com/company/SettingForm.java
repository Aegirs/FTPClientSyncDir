package com.company;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;

import javax.net.ssl.*;
import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by thomasmazurkiewicz on 25/03/15.
 */
public class SettingForm extends JFrame {
    private JPanel settingPanel;

    private JTextField hostField;
    private JTextField portField;

    private JComboBox choiceChiffrement;

    private JTextField username;
    private JPasswordField password;

    private JLabel connectLabel;
    private JLabel accountLabel;

    private JButton connectButton;
    private JComboBox choiceAuthen;
    private JTree treeServer;
    private TreeAccount treeAccount;
    private JButton addServerButton;
    private JButton removeServerButton;
    private JButton saveServer;

    private FTPClient ftpClient;
    private FTPSClient ftpsClient;
    private Account currentAccount = null;
    private int indexCurrentNode;

    private boolean accountConnected;
    private String typeConnection = "FTP Simple";
    private PrintStream printStreamStatus;

    public SettingForm(PrintStream status) {
        super("Settings");

        treeAccount = new TreeAccount(treeServer);
        treeAccount.initTree();
        printStreamStatus = status;
        initCurrentAccount();

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                updateCurrentAccount();
                disconnectFTP();

                connectLabel.setText("");
                accountLabel.setText("");

                switch (currentAccount.getChiffrement()) {//check for a match
                    case "FTP Simple":
                        connectServerFTP();
                        break;
                    case "FTPS SSL/TLS explicite":
                        connectServerFTPS(true);
                        break;
                    case "FTPS SSL/TLS implicite":
                        connectServerFTPS(false);
                        break;
                    default:
                        System.out.println("Error typeConnection doesn't exist: " + typeConnection);
                        break;
                }
            }
        });

        choiceChiffrement.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                typeConnection = (String) choiceChiffrement.getSelectedItem();//get the selected item
                currentAccount.setChiffrement(typeConnection);
            }
        });

        choiceAuthen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String choice = (String) choiceAuthen.getSelectedItem();//get the selected item
                currentAccount.setAuthentification(choice);

                switch (choice) {//check for a match
                    case "Normal":
                        setNormalAuthen();
                        break;
                    case "Anonymous":
                        setAnonymousAuthen();
                        break;
                    default:
                        System.out.println("Error choice doesn't exist : " + choice);
                        break;
                }
            }
        });

        addServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treeAccount.addChild(new Account());
            }
        });

        removeServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                treeAccount.removeChild(indexCurrentNode);
                initCurrentAccount();
            }
        });

        saveServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCurrentAccount();
            }
        });

        treeServer.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeServer.getLastSelectedPathComponent();
                if (node == null) {
                    return;
                }
                TreeNode root = (TreeNode) treeServer.getModel().getRoot();

                //save previous account without select validate button
                updateCurrentAccount();

                //set new account
                indexCurrentNode = root.getIndex(node);
                currentAccount = (Account) treeAccount.getListAccount().get(indexCurrentNode);

                setAllField(currentAccount);
            }
        });

        treeServer.getModel().addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());

                try {
                    int index = e.getChildIndices()[0];
                    node = (DefaultMutableTreeNode) (node.getChildAt(index));

                    currentAccount.setNode(node.toString());
                    treeAccount.updateAccount(indexCurrentNode, currentAccount);
                } catch (NullPointerException exc) {}
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {

            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {

            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {

            }
        });
    }

    private void disconnectFTP() {
        if ( (getClient() != null) && getClient().isConnected() ) {
            try {
                getClient().logout();
                getClient().disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void initCurrentAccount() {
        if ( /*mode normal  currentAccount != null *//* mode test */ treeAccount.getListAccount().size() > 0 ) {
            currentAccount = (Account) treeAccount.getListAccount().get(0);
            indexCurrentNode = 0;
            setAllField(currentAccount);
        }
        else {
            // all field not enable
            userNotSelected();
        }
    }

    private void userNotSelected() {
        hostField.enable(false);
        portField.enable(false);

        username.enable(false);
        password.enable(false);

        choiceChiffrement.enable(false);
        choiceAuthen.enable(false);
    }

    public void shaw() {
        pack();
        setContentPane(settingPanel);

        getRootPane().setDefaultButton(connectButton);
        setSize(getPreferredSize());
        setResizable(false);
        setVisible(true);
    }

    public void saveTreeAccount() {
        treeAccount.saveTree("Account.txt");
    }

    private void setAllField(Account account) {
        hostField.setText(account.getHost());
        portField.setText("" + account.getPort());

        username.setText(account.getUsername());
        password.setText(account.getPassword());

        choiceChiffrement.setSelectedItem(account.getChiffrement());
        choiceAuthen.setSelectedItem(account.getChiffrement());
    }

    private void updateCurrentAccount() {
        String host = hostField.getText();
        String portS = portField.getText();

        String user = username.getText();
        String pass = password.getText();

        currentAccount.updateAccount(host, portS, user, pass);
        treeAccount.updateAccount(indexCurrentNode, currentAccount);
    }

    private void setNormalAuthen() {
        username.setText("");
        password.setText("");

        username.setEnabled(true);
        password.setEnabled(true);

        currentAccount.setAuthentification("Normal");
    }

    private void setAnonymousAuthen() {
        username.setText("anonymous");
        password.setText("anonymous");

        username.setEnabled(false);
        password.setEnabled(false);

        currentAccount.setAuthentification("Anonymous");
    }

    public Boolean isLog() {
        return accountConnected;
    }

    public FTPClient getClient() {
        int firstPart = currentAccount.getChiffrement().lastIndexOf(" ");
        String chiffrement = currentAccount.getChiffrement().substring(0,firstPart);

        if ( chiffrement.equals("FTPS SSL/TLS") ) {
            return ftpsClient;
        }
        return ftpClient;
    }

    private void labelSuccess(JLabel label,Boolean state) {
        if (state) {
            label.setForeground(Color.green);
            label.setText("Connected");
        }
        else {
            label.setForeground(Color.red);
            label.setText("Not Connected");
        }
    }

    private void connectServerFTP() {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding("UTF8");
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(printStreamStatus)));

        try {
            ftpClient.connect(currentAccount.getHost(), currentAccount.getPort());

            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = ftpClient.getReplyCode();
            labelSuccess(connectLabel, ftpClient.isConnected());

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftpsClient.disconnect();
                System.err.println("FTP server refused connection.");
            }
            else {

                accountConnected = ftpClient.login(currentAccount.getUsername(), currentAccount.getPassword());
                labelSuccess(accountLabel, accountConnected);

                if ( accountConnected ) {
                    ftpClient.enterLocalPassiveMode();
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                }
            }
        } catch (IOException ex) {
            labelSuccess(connectLabel, ftpClient.isConnected());

            if (ftpClient.isConnected())
            {
                try
                {
                    ftpClient.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server FTP.");
            ex.printStackTrace();
        }
    }

    private void connectServerFTPS(boolean mode) {
        String protocol = "SSL";    // SSL/TLS
        boolean  binaryTransfer = true;

        TrustManager tm = new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                System.out.println("getAcceptedIssuers------");
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                // TODO Auto-generated method stub
                System.out.println("checkClientTrusted------");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
                // TODO Auto-generated method stub
                System.out.println("checkServerTrusted------");
            }
        };


        System.out.println("Connection FTPS start.");
        ftpsClient = new FTPSClient(protocol);
        ftpsClient.setControlEncoding("UTF8");
        ftpsClient.setAuthValue("TLS");
        ftpsClient.setTrustManager(tm);
        ftpsClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(printStreamStatus)));

        // if ( mode ) explicite else implicite

        try
        {
            ftpsClient.connect(currentAccount.getHost(),currentAccount.getPort());
            System.out.println("Connected to " + currentAccount.getHost() + ".");

            // ftpsClient.enterLocalPassiveMode();
            // ftpsClient.enterLocalActiveMode();

            // success.
            int reply = ftpsClient.getReplyCode();
            labelSuccess(connectLabel, ftpsClient.isConnected());

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftpsClient.disconnect();
                System.err.println("FTPS server refused connection.");
            }
            else {
                // Login
                accountConnected = ftpsClient.login(currentAccount.getUsername(), currentAccount.getPassword());
                if ( accountConnected ) {

                    // Set protection buffer size
                    ftpsClient.execPBSZ(0);
                    // Set data channel protection to private
                    ftpsClient.execPROT("P");

                    if (binaryTransfer) ftpsClient.setFileType(FTP.BINARY_FILE_TYPE);

                    // Enter local passive mode
                    ftpsClient.enterLocalPassiveMode();

                    System.out.println("Remote system is " + ftpsClient.getSystemName());

                }

                labelSuccess(accountLabel,accountConnected);
            }
        }
        catch (IOException e)
        {
            if (ftpsClient.isConnected())
            {
                try
                {
                    ftpsClient.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server FTPS.");
            e.printStackTrace();
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        DefaultMutableTreeNode racine = new DefaultMutableTreeNode("Server");
        treeServer = new JTree(racine);
        TreeModel model = treeServer.getModel();

        treeServer.expandPath(new TreePath(model.getRoot()));
    }
}