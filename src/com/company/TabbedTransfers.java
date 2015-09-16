package com.company;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Vector;

/**
 * Created by thomasmazurkiewicz on 29/03/15.
 */
@SuppressWarnings("serial")
class ProgressRenderer extends JProgressBar implements TableCellRenderer
{
    public ProgressRenderer()
    {
        setValue(0); // initialisation
        setBorderPainted(true);
    }

    public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus, int row, int col) {
        setValue((Integer)value);
        if (isSelected)  setBorder(BorderFactory.createLineBorder(Color.BLUE,2));
        else setBorder(null);
        return this;
    }
}

class ThreadProgressBar extends Thread
{
    private String path;
    private long sizeRef;
    private JTable transfers;
    private Boolean isRunning = true;

    private JLabel timeTot;
    private JLabel labelSizeRest;

    private long speedMoy = 0;
    private long sizeRest;

    public ThreadProgressBar(JTable Ntransfers,JLabel NtimeTot,JLabel NlabelSize,
                                    long NsizeRest,String Npath,long sizeFile) {
        path = Npath;
        sizeRef = sizeFile;
        transfers = Ntransfers;
        timeTot = NtimeTot;

        labelSizeRest = NlabelSize;
        sizeRest = NsizeRest;
    }

    public void run() {
        File file = new File(path);

        long start = java.lang.System.currentTimeMillis();
        long speed = 0;

        while ( isRunning && (file.length() != sizeRef) ) {
            int perCent = perCentSize(sizeRef, file.length());
            updatePerCent(perCent,0);

            file = new File(path);

            long current = java.lang.System.currentTimeMillis() - start;
            if ( current > 0 ) {
                speed = (1000*file.length())/current;
            }

            String speedString = TabbedTransfers.convertSize(speed) + "/s";
            updateSpeed(speedString, 0);

            long sizeRestCurrent = sizeRest - file.length();
            if ( speed > 0 ) {
                long currentTime = sizeRestCurrent/speed;
                String timeString = String.format("%02d:%02d:%02d",(currentTime/60/60)%60,(currentTime/60)%60,currentTime%60);
                timeTot.setText("Time: " + timeString );
            }

            labelSizeRest.setText("Taille: " + TabbedTransfers.convertSize(sizeRestCurrent));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if ( file.length() == sizeRef ) {
            updatePerCent(100,0);
        }
    }

    public void kill() {
        isRunning = false;
    }

    private void updatePerCent(int value,int numRow) {
        transfers.getModel().setValueAt(value, numRow,5);
    }

    private void updateSpeed(String value,int numRow) {
        transfers.getModel().setValueAt(value, numRow,3);
    }

    private int perCentSize(long sizeRef,long size) {
        return (int)(100*size/sizeRef);
    }
}

public class TabbedTransfers extends Thread {
    private FTPClient ftpClient;
    private JTable transfers;
    private JTable success;
    private JTable errors;
    private JLabel sizeAllData;
    private JLabel allTime;

    private Vector<Vector> listPath;
    long allSize = 0;

    public TabbedTransfers(FTPClient NftpClient,JTable Ntransfers,JTable Nsuccess,
                                JTable Nerrors,JLabel Nlabel,JLabel Ntime) {
        ftpClient = NftpClient;

        transfers = Ntransfers;
        success = Nsuccess;
        errors = Nerrors;

        sizeAllData = Nlabel;
        allTime = Ntime;
    }

    public Vector<Vector> getListPath() {
        return listPath;
    }

    public static JTable initTable() {
        Object[] title = {"File Serve","Direction","File Client","Speed","Size","Status"};
        Object[][] dataTransfers = null;

        DefaultTableModel model = new DefaultTableModel(dataTransfers,title);

        JTable table = new JTable(model);

        table.getColumn("Status").setCellRenderer(new ProgressRenderer());

        return table;
    }

    public void run() {
        // function download datas + updatePerCent
        // upload done
        // verif if exists in client and server test oldest version ..

        // create two list on to download on server and other to download on client
        downloadListDatas(listPath);

    }

    public static String convertSize(long size) {
        String res;
        int cmpt = 0;

        double taille = size;

        while ( size > 1000 ) {
            cmpt++;
            size/= 1000;
            taille /= 1000.0;
        }
        res = String.format("%.2f ", taille);

        switch(cmpt)  {
            case 0 :
                res += "o";
                break;
            case 1 :
                res += "ko";
                break;
            case 2 :
                res += "Mo";
                break;
            case 3 :
                res += "Go";
                break;
            case 4 :
                res += "To";
                break;
            default :
                res += "TGrosse ;)";
        }
        return res;
    }

    public void updateTableTransfers(String pathServer,String pathClient) {

        Vector<Vector> datas = listFileServerToClient(pathServer, pathClient);
        Vector<Vector> datas2 = listFileClientToServer(pathServer, pathClient);
        DefaultTableModel model = (DefaultTableModel) transfers.getModel();

       /* System.out.println("\nServerToClient");
        for(Vector elt : datas) {
            System.out.println("\nPath Server: " + elt.get(0));
            System.out.println("Path Client: " + elt.get(1));
            System.out.println("Size file: " + elt.get(2));
        }

        System.out.println("\nClientToServer");
        for(Vector elt : datas2) {
            System.out.println("\nPath Server: " + elt.get(0));
            System.out.println("Path Client: " + elt.get(1));
            System.out.println("Size file: " + elt.get(2));
        }*/

        for(Vector<Object> data: datas) {
            Vector<Object> line = new Vector<Object>();

            line.addElement(data.get(0));
            line.addElement("-->");
            line.addElement(data.get(1));
            line.addElement("");
            line.addElement(convertSize((long) data.get(2)));
            line.addElement(0);

            allSize += (long) data.get(2);
            model.addRow(line);
        }

     /*   for(Vector<Object> data: datas2) {
            Vector<Object> line = new Vector<Object>();

            line.addElement(data.get(0));
            line.addElement("<--");
            line.addElement(data.get(1));
            line.addElement("");
            line.addElement(convertSize((long) data.get(2)));
            line.addElement(0);

            allSize += (long) data.get(2);
            model.addRow(line);
        }*/

        sizeAllData.setText("Total: " + convertSize(allSize));
        allTime.setText("Time: --:--:--");

        listPath = datas;
    }

    private String extractPathTargetName(String pathFile) {
        int end = pathFile.lastIndexOf("/");
        return pathFile.substring(end+1);
    }

    // init table transfers
    public Vector<Vector> listFileServerToClient(String pathServer,String pathClient) {
        Vector<Vector> listFile = new Vector<Vector>();

        try {
            FTPFile[] fileServer = ftpClient.listFiles(pathServer);

            /*if (fileServer.hasPermission(FTPFile.USER_ACCESS,FTPFile.WRITE_PERMISSION))  {
                System.out.println("Permission OK");
            }*/

            if ( fileServer != null) {
                if ( fileServer.length > 1 ) {
                    listFileInServerDir(pathServer, listFile, pathClient);
                }
                else {
                    String pathFileServer = fileServer[0].getName();
                    pathClient += "/" + extractPathTargetName(pathFileServer);

                    Vector<Object> serverToClient = new Vector<Object>();
                    serverToClient.addElement(pathServer);
                    serverToClient.addElement(pathClient);
                    serverToClient.addElement(fileServer[0].getSize());

                    listFile.add(serverToClient);
                }
            }
            else {
                System.out.println("TabbedTransfers:Error cannot create fileServer");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("TabbedTransfers:All  Path Download");

        return listFile;
    }

    private void listFileInServerDir(String directory,Vector<Vector> listFile,String pathClient) {
        String nameDir = extractPathTargetName(directory);
        String pathDirClient = pathClient + "/" + nameDir;

        try {
            FTPFile[] files = ftpClient.listFiles(directory);
            for(FTPFile fileServe : files) {
                String pathNewFile = pathDirClient + "/" + fileServe.getName();
                String pathServerFile = directory + "/" + fileServe.getName();

                if ( fileServe.isDirectory() ) {
                    listFileInServerDir(pathServerFile, listFile, pathDirClient);
                }
                else {
                    Vector<Object> serverToClient = new Vector<Object>();
                    serverToClient.addElement(pathServerFile);
                    serverToClient.addElement(pathNewFile);
                    serverToClient.addElement(fileServe.getSize());

                    listFile.add(serverToClient);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Vector<Vector> listFileClientToServer(String pathServer,String pathClient) {
        Vector<Vector> listFile = new Vector<Vector>();
        File fileClient = new File(pathClient);

        if ( fileClient.isDirectory() ) {
            listFileInClientDir(fileClient,listFile,pathServer);
        }else {
            String pathFileClient = fileClient.getName();
            pathServer += "/" + extractPathTargetName(pathFileClient);

            Vector<Object> serverToClient = new Vector<Object>();
            serverToClient.addElement(pathServer);
            serverToClient.addElement(pathFileClient);
            serverToClient.addElement(fileClient.length());

            listFile.add(serverToClient);
        }

        System.out.println("TabbedTransfers:All  Path Download");

        return listFile;
    }

    private void listFileInClientDir(File directory,Vector<Vector> listFile,String pathServer) {
        String nameDirClient = extractPathTargetName(directory.getName());
        String newPathServer = pathServer + "/" + nameDirClient;

        for (File fileClient : directory.listFiles()) {

            if (fileClient.isDirectory()) {
                listFileInClientDir(fileClient, listFile, newPathServer);
            } else {

                //testAddFile(fileClient);

                Vector<Object> serverToClient = new Vector<Object>();
                serverToClient.addElement(newPathServer + "/" + fileClient.getName());
                serverToClient.addElement(fileClient.getPath());
                serverToClient.addElement(fileClient.length());

                listFile.add(serverToClient);
            }
        }

    }

    private void createLostDir(String clientPath) {
        int index = clientPath.lastIndexOf('/');
        String parentDir = clientPath.substring(0,index);

        File client = new File(parentDir);
        Vector<String> lostDir = new Vector<String>();

        while( !client.exists() ) {
            index = parentDir.lastIndexOf('/');
            lostDir.addElement(parentDir.substring(index));

            parentDir = parentDir.substring(0,index);
            client = new File(parentDir);
        }

        int n = lostDir.size();
        for(int i = 0; i < n; i++) {
            String dir = lostDir.get(n-i-1);
            parentDir += dir;
            File newDir = new File(parentDir);
            newDir.mkdir();
        }
    }
    // verif if dir or file exists etc and create remove ect...
    // first test dir
    //second test file and found algorithm to resolve problem
    public void downloadListDatas(Vector<Vector> datas) {
        long currentSize = allSize;

        for(Vector<Object> data : datas) {
            String serverPath = (String)data.get(0);
            String clientPath = (String)data.get(1);

            System.out.println("createLiso");
            createLostDir(clientPath);
            System.out.println("end");

            //download file
            ThreadProgressBar threadProgressBar = new ThreadProgressBar(transfers, allTime,sizeAllData,currentSize,clientPath,(long)data.get(2));
            System.out.println("deb");
            Boolean isTranfers = downloadFile(serverPath, clientPath, threadProgressBar);
            System.out.println("end");
            // update table
            DefaultTableModel modelTransfers = (DefaultTableModel) transfers.getModel();
            Vector<Object> rowData = (Vector<Object>) modelTransfers.getDataVector().elementAt(0);
            modelTransfers.removeRow(0);

            DefaultTableModel modelIsSucess = null;
            if ( isTranfers ) {
                modelIsSucess = (DefaultTableModel) success.getModel();
            }
            else {
                modelIsSucess = (DefaultTableModel) errors.getModel();
            }
            modelIsSucess.addRow(rowData);

            currentSize -= (long)data.get(2);
            sizeAllData.setText("Total: " + convertSize(currentSize));
        }
    }

    // path client is directory path in Client + name Server file.
    private boolean downloadFile(String pathFileServer,String pathClient,ThreadProgressBar threadProgressBar) {
        OutputStream outputClient;
        Boolean isTransfers = true;

        try {
            outputClient = new FileOutputStream(pathClient);
            //get the file from the remote system
            try {
                threadProgressBar.start();
                ftpClient.retrieveFile(pathFileServer, outputClient);

                //close output stream
                outputClient.close();
            } catch (Exception e) {
                e.printStackTrace();
                isTransfers = false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            isTransfers = false;
        }

        threadProgressBar.kill();

        return isTransfers;
    }


    public void uploadFile(String path) {
        File firstLocalFile = new File(path);

        String firstRemoteFile = "test";
        System.out.println("TabbedTransfers: Start uploading first file");

        boolean done = false;
        try {
            InputStream inputStream = new FileInputStream(firstLocalFile);
            // storeFile download on server file
            done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (done) {
            System.out.println("TabbedTransfers: The first file is uploaded successfully.");
        }
    }

    private Boolean testAddFile(File f) {
        Boolean res = false;

        System.out.println("Caract File");
        BasicFileAttributes attr = null;
        try {
            attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            System.out.println("creationTime: " + attr.creationTime());
            System.out.println("lastAccessTime: " + attr.lastAccessTime());
            System.out.println("lastModifiedTime: " + attr.lastModifiedTime());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }
}

/*
* //get output stream
                    OutputStream output;
                    output = new FileOutputStream(localDirectory + "/" + file.getName());
                    //get the file from the remote system
                    ftp.retrieveFile(file.getName(), output);
                    //close output stream
                    output.close();

                    //delete the file
                    ftp.deleteFile(file.getName());
* */


// APPROACH #1: uploads first file using an InputStream
           /* File firstLocalFile = new File("D:/Test/Projects.zip");

            String firstRemoteFile = "Projects.zip";
            InputStream inputStream = new FileInputStream(firstLocalFile);

            System.out.println("Start uploading first file");
            boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
            inputStream.close();
            if (done) {
                System.out.println("The first file is uploaded successfully.");
            }

            // APPROACH #2: uploads second file using an OutputStream
            File secondLocalFile = new File("E:/Test/Report.doc");
            String secondRemoteFile = "test/Report.doc";
            inputStream = new FileInputStream(secondLocalFile);

            System.out.println("Start uploading second file");
            OutputStream outputStream = ftpClient.storeFileStream(secondRemoteFile);
            byte[] bytesIn = new byte[4096];
            int read = 0;

            while ((read = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, read);
            }
            inputStream.close();
            outputStream.close();

            boolean completed = ftpClient.completePendingCommand();
            if (completed) {
                System.out.println("The second file is uploaded successfully.");
            }*/
