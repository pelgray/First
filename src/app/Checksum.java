package app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 1 on 20.04.2017.
 */
public class Checksum {
    private MessageDigest md;

    public Checksum(){
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String get(String filename){
        final FileInputStream fis;
        try {
            fis = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        byte[] dataBytes = new byte[1024];
        int bytesRead;
        try {
            while((bytesRead = fis.read(dataBytes)) > 0) {
                md.update(dataBytes, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] mdBytes = md.digest();

        // Переводим контрольную сумму в виде массива байт в
        // шестнадцатеричное представление
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < mdBytes.length; i++) {
            sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
