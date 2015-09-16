package com.company;

import java.io.*;

/**
 * Created by thomasmazurkiewicz on 17/04/15.
 */
public class ScanFile {
    private File reference;
    private File test;
    private int perCentTest;
    private int perCentMaxTest;

    public ScanFile(String ref, String Ntest) {
        reference = new File(ref);
        test = new File(Ntest);

        perCentTest = 20;
        perCentMaxTest = 50;
    }

    private long random(long s,long e) {
        return (long)(Math.random() *( e - s ) + s);
    }

    private long lengthCorresp(byte[] ref,byte[] test) {
        int i = 0;
        while ( (i < ref.length) && (ref[i] == test[i]) ) {
            i++;
        }

        return i;
    }

    private byte[] getByteFromFile(File file,long start,int size) {
        byte[] byteExtract = null;
        RandomAccessFile reader = null;

        try {
            reader = new RandomAccessFile(file,"r");
            try {
                byteExtract = new byte[size];
                reader.seek(start);
                reader.read(byteExtract);
                //System.out.println(byteExtract);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return byteExtract;
    }

    public int integriteFile() {
        int perCent = 0;
        long currentSizeTest = 0;

        long maxByte = Math.min(reference.length(), test.length());

        long refSizeTest = ( perCentTest * maxByte ) / 100;
        long maxLength = ( perCentMaxTest * maxByte ) / 100;

        if ( maxLength > (1 << 16) ) {
            maxLength = (1 << 16);
        }

        System.out.println("RefSizeTest: " + refSizeTest);
        System.out.println("Max length: " + maxLength);

        while ( currentSizeTest < refSizeTest ) {
            long x = random(0,maxByte-1);
            long y = random(x,maxByte-1);

            long nbElt = (y - x + 1);

            if ( nbElt < maxLength ) {
                byte[] byteRef = getByteFromFile(reference,x,(int)nbElt);
                byte[] byteTest = getByteFromFile(test,x,(int)nbElt);

                System.out.println("NbElt: " + nbElt + " x: " + x + "  y: " + y);

                perCent += lengthCorresp(byteRef, byteTest);
                currentSizeTest += nbElt;
            }
        }

        if ( currentSizeTest > 0 ) {
            perCent *= 100;
            perCent /= currentSizeTest;
        }

        return perCent;
    }

    public void setPerCentTest(int perCent) {
        perCentTest = perCent;
    }


}
