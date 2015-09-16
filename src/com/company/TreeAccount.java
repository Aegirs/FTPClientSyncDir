package com.company;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.*;
import java.util.*;

/**
 * Created by thomasmazurkiewicz on 01/04/15.
 */
public class TreeAccount {
    private JTree tree;
    private int nbChild;
    private List<Account> listAccount = null;

    public TreeAccount(JTree Ntree) {
        tree = Ntree;
        nbChild = 0;

        listAccount = new LinkedList<Account>();
    }

    public void initTree() {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        File file = Account.openFile("Account.txt");

        nbChild = 0;

        BufferedReader read  = null;
        try {
            read = new BufferedReader(new FileReader(file));
            try {
                while( read.readLine() != null ) {
                    Account tmp = new Account(read);
                    addChild(tmp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        tree.expandPath(new TreePath(model.getRoot()));
    }

    public void addChild(Account child) {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        model.insertNodeInto(new DefaultMutableTreeNode(child.getNode()), (MutableTreeNode) model.getRoot(), nbChild);
        model.reload();

        listAccount.add(nbChild, child);
        nbChild++;
    }

    public void removeChild(int index) {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        root.remove(index);
        model.reload();

        listAccount.remove(index);
        nbChild--;
    }

    public void updateAccount(int index,Account newAccount) {
        listAccount.set(index,newAccount);
    }

    public List<?> getListAccount() {
        return listAccount;
    }

    public void saveTree(String nameFile) {
        File f = new File(nameFile);
        f.delete();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        for( Account account : listAccount) {
            account.saveAccount(nameFile);
        }
    }

}

