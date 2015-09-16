package com.company;

import java.io.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Account implements java.io.Serializable {
        private String node;
        private String host;
        private int port;
        private String chiffrement;
        private String authentification;
        private String username;
        private String password;

        private Boolean chang;

        public Account() {
                node = "Default";
                host = "";
                port = 21;
                chiffrement = "FTP Simple";
                authentification = "Normal";
                username = "";
                password = "";
                chang = false;
        }

        public void updateAccount(String Nhost,String Nport ,String Nuser,String Npass ) {
                host = Nhost;
                if (Nport.length() > 0) {
                        port = Integer.parseInt(Nport);
                }
                username = Nuser;
                password = Npass;
        }

        public Account(BufferedReader reader) {
                String line = null;

                try {
                        while( ( line = reader.readLine() ) != null  ) {
                                int startType = line.indexOf("<");
                                int endType = line.indexOf(">");

                                int startData = line.lastIndexOf("<");
                                int endData = line.lastIndexOf(">");

                                String type = line.substring(startType+1, endType);
                                String data = line.substring(startData+1,endData);

                                if ( affectField(type, data) ) {
                                        break;
                                }
                        }

                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private void saveField(String type,String data,PrintWriter writer) {
                writer.println("\t<" + type + ">" + "<" + data + ">");
        }

        private String cryptPassword(String password) {
                String crypt = password;
                /*
                        try {
                                MessageDigest md = MessageDigest.getInstance("MD5");
                                md.update(password.getBytes());
                                byte[] digest = md.digest();

                                StringBuffer sb = new StringBuffer();
                                for(byte b: digest) {
                                        sb.append(String.format("%02x", b & 0xff));
                                }

                                saveField("password",sb.toString(),writer);
                        } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                        }
                           */
                // demande d'un mot de passe pour pouvoir lire les info snesible de l'application
                // comme les mot de passe chiffré a partir de ce mot de passe enregistré en MD5
                // cihffré a partir du mot de passe en claire mais comparaison avec le md5
                        /*
                        try {
                                KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
                                rsa.initialize(2048);

                                KeyPair keypair = rsa.genKeyPair();

                                saveField("password",sb.toString(),writer);
                        } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                        }  */

                return crypt;
        }

        public static File openFile(String nameFile) {
                File file = new File(nameFile);
                // refaire class readWrite, decouper en sous classe
                try {
                        if (file.createNewFile()){
                                System.out.println("File is created!");
                        }else{
                                System.out.println("File already exists.");
                        }
                } catch (IOException e1) {
                        e1.printStackTrace();
                }
                return file;
        }

        public void saveAccount(String nameFile) {

                File file = openFile(nameFile);
                PrintWriter writer = null;
                try {
                        writer = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
                        writer.println("<start>");

                        saveField("node",node, writer);
                        saveField("host", host, writer);
                        saveField("port", "" + port, writer);
                        saveField("chiffrement",chiffrement,writer);
                        saveField("authentification",authentification,writer);
                        saveField("username",username,writer);
                        saveField("password",cryptPassword(password),writer);

                        writer.println("<end>");
                        writer.close();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        private Boolean affectField(String type,String data) {
                Boolean end = false;
                switch (type) {
                        case "node":
                                node = data;
                                break;
                        case "host":
                                host = data;
                                break;
                        case "port":
                                port = Integer.parseInt(data);
                                break;
                        case "chiffrement":
                                chiffrement = data;
                                break;
                        case "authentification":
                                authentification = data;
                                break;
                        case "username":
                                username = data;
                                break;
                        case "password":
                                password = data;
                                break;
                        case "end":
                                end = true;
                                break;
                        default:
                                System.out.println("Error type is not a user type");
                                break;
                }

                return end;
        }

        public void setNode(String name) {
                node = name;
        }

        public String getNode() {
                return node;
        }

        public void setHost(String Nhost) {
                host = Nhost;
        }

        public String getHost() {
                return host;
        }

        public void setPort(int Nport) {
                port = Nport;
        }

        public int getPort() {
                return port;
        }

        public void setChiffrement(String Nchiff) {
                chiffrement = Nchiff;
        }

        public String getChiffrement() {
                return chiffrement;
        }

        public void setAuthentification(String NAuthen) {
                authentification = NAuthen;
        }

        public String getAuthentification() {
                return authentification;
        }

        public void setUsername(String Nuser) {
                username = Nuser;
        }

        public String getUsername() {
                return username;
        }

        public void setPassword(String Npass) {
                password = Npass;
        }

        public String getPassword() {
                return password;
        }
}
