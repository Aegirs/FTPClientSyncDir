package com.company;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import javax.swing.tree.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by thomasmazurkiewicz on 27/03/15.
 */
public class TreeDir {

    private FTPClient client;
    private DefaultMutableTreeNode nodeSelected;
    private String type;
    private JTree tree;

    public TreeDir(FTPClient Nclient,String Ntype,JTree Ntree) {
        tree = Ntree;
        client = Nclient;
        type = Ntype;
    }

    public void setNodeSelected(DefaultMutableTreeNode NnodeSelected) {
        nodeSelected = NnodeSelected;
    }

    public DefaultMutableTreeNode getNodeSelected() {
        return nodeSelected;
    }

    public static String getNodePath(JTree tree,DefaultMutableTreeNode Racine) {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        // construction du chemin depuis la racine
        TreeNode[] paths = Racine.getPath();
        String path = "";
        String tmp = paths[0].toString();
        if ( tmp != null ) {
           path = tmp.equals("/") ? "" : paths[0].toString();
        }

        for (int i = 1; i < paths.length; ++i) {
            if (paths[i].toString() != null) {
                path += "/" + paths[i];
            }
        }

        return path;
    }

    private int addChild(MutableTreeNode Racine,String path) {
        int nbChild = 0;
        if ( type.equals("Client") ) {
            nbChild = addChildClient(Racine,path);
        }
        else if (type.equals("Server") ) {
            nbChild = addChildServer(Racine,path);
        }
        else {
            System.out.println("Error: This type doesn't exist");
        }

        return nbChild;
    }

    public void updateTree(DefaultMutableTreeNode node) {
        nodeSelected = node;
        String path = getNodePath(tree,nodeSelected);
        int nbChild = nodeSelected.getChildCount();

        System.out.println(type);
        System.out.println("Path : " + path);

        // add child to parent
        if ( nbChild == 0 ) {
            nbChild = addChild(nodeSelected, path + "/");
        }

        // set child on each child
        for(int i = 0; i < nbChild; i++ ) {
            TreeNode child = nodeSelected.getChildAt(i);
            String childPath = path + "/" + child.toString() + "/";

            if ( child.getChildCount() == 0 ) {
                addChild((MutableTreeNode) child,childPath);
            }
        }

        if ( nbChild > 0 ) {
            tree.expandPath(new TreePath(nodeSelected.getPath()));
        }
    }

    private int addChildServer(MutableTreeNode Racine,String path) {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        FTPFile[] files = new FTPFile[0];

        try {
            files = client.listFiles(path);
            for(int i=0;i < files.length;i++) {
                String nameFile = files[i].getName();
                model.insertNodeInto(new DefaultMutableTreeNode(nameFile), Racine, i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return files.length;
    }

    private int addChildClient(MutableTreeNode Racine,String path) {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();

        File f = new File(path);
        int i = 0;

        Path dir = f.toPath();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file: stream) {
                model.insertNodeInto(new DefaultMutableTreeNode(file.getFileName()), Racine, i);
                i++;
            }
            model.reload();
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }

        return i;
    }

    public void changRoot(String path) {
        // afficher contenu du dossier Client dans la fenetre
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        root.removeAllChildren();
        model.reload();

        root.setUserObject(path);
        model.nodeChanged(root);

        updateTree(root);
    }
}
