package org.wiztools.wizcrypt;

import javax.crypto.NoSuchPaddingException;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.ResourceBundle;

/**
 * Class to do the decryption using the WizCrypt naming convention (*.wiz).
 */
public class Decrypt implements IProcess{
    
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.wiztools.wizcrypt.wizcryptmsg");
    
    private CipherKey ce;
    
    public void init(final String keyStr) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException{
        ce = CipherKeyGen.getCipherKeyForDecrypt(keyStr);
    }
    
    public void process(final File file, final boolean forceOverwrite) 
            throws FileNotFoundException,
                DestinationFileExistsException,
                PasswordMismatchException,
                IOException{
        FileInputStream fis = null;
        FileOutputStream fos = null;
        boolean canDelete = false;
        try{
            String path = file.getCanonicalPath();
            if(!path.endsWith(".wiz")){
                throw new FileNotFoundException(rb.getString("err.file.not.end.wiz")+path);
            }
            String newPath = path.replaceFirst(".wiz$", "");
            File outFile = new File(newPath);
            if(!forceOverwrite && outFile.exists()){
                throw new DestinationFileExistsException(
                        rb.getString("err.destination.file.exists")+
                        outFile.getCanonicalPath());
            }
            fis = new FileInputStream(file);
            fos = new FileOutputStream(outFile);
            WizCrypt.decrypt(fis, fos, ce);
            canDelete = true;
        } finally{
            // fos & fis will be closed by WizCrypt.decrypt() API
            if(canDelete){
                file.delete();
            }
        }
    }
}
