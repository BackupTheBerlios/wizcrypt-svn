package org.wiztools.crypt;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ResourceBundle;
import java.util.Arrays;

public class Decrypt implements IProcess{
    private final String ALGO = ResourceBundle.getBundle("wizcrypt").getString("algorithm");
    
    private static final ResourceBundle rb = ResourceBundle.getBundle("wizcryptmsg");
    
    private Cipher cipher;
    private byte[] passKeyHash;
    
    public void init(String keyStr) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException{
        SecretKey key = new SecretKeySpec(keyStr.getBytes(), ALGO);
        cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key);
        passKeyHash = PassHash.passHash(keyStr);
    }
    
    public void process(File file) throws IOException, FileNotFoundException, PasswordMismatchException{
        FileInputStream fis = null;
        CipherOutputStream cos = null;
        boolean canDelete = false;
        try{
            String path = file.getCanonicalPath();
            if(!path.endsWith(".wiz")){
                throw new FileNotFoundException(rb.getString("err.file.not.end.wiz")+path);
            }
            String newPath = path.replaceFirst(".wiz$", "");
            File outFile = new File(newPath);
            fis = new FileInputStream(file);
            cos = new CipherOutputStream(
                    new FileOutputStream(outFile), cipher);
            // read 16 bytes from fis
            byte[] filePassKeyHash = new byte[16];
            fis.read(filePassKeyHash, 0, 16);
            if(!Arrays.equals(passKeyHash, filePassKeyHash)){
                throw new PasswordMismatchException(rb.getString("err.pwd.not.match")+path);
            }
            
            int i = -1;
            while((i=fis.read()) != -1){
                cos.write((char)i);
            }
            canDelete = true;
        } finally{
            try{
                if(cos != null){
                    cos.close();
                }
            } catch(IOException ioe){
                System.err.println(ioe.getMessage());
            }
            try{
                if(fis != null){
                    fis.close();
                }
            } catch(IOException ioe){
                System.err.println(ioe.getMessage());
            }
            if(canDelete){
                file.delete();
            }
        }
    }
}
