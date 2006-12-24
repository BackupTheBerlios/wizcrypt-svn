package org.wiztools.crypt;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class has static methods to create <code>CipherKey</code> objects.
 */
public final class CipherKeyGen{

    private static final String ALGO = ResourceBundle.getBundle("wizcrypt").getString("algorithm");
    
    private static byte[] passHash(String password) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte[] raw = md.digest();
        return raw;
    }
    
    private static CipherKey getCipherKey(String keyStr, int mode) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException{
        byte[] passKeyHash = null;
        Cipher cipher = null;

        SecretKey key = new SecretKeySpec(keyStr.getBytes(), ALGO);
        cipher = Cipher.getInstance(ALGO);
        cipher.init(mode, key);
        passKeyHash = CipherKeyGen.passHash(keyStr);
        
        CipherKey ce = new CipherKey(cipher, passKeyHash);
        
        return ce;
    }
    
    /**
     * This is the public API used for getting the <code>CipherKey</code> object
     * used in <code>WizCrypt</code> public APIs.
     *
     * @param keyStr The password used for creating the cipher.
     * @throws NoSuchAlgorithmException When the Algorithm used by WizCrypt (RC4) is not found in JVM. This is highly unlikely, because SUN JVM has this.
     * @throws InvalidKeyException The key should be of specific size. If this size limits are not met, <code>InvalidKeyException</code> exception is thrown.
     * @throws NoSuchPaddingException This exception is thrown when a particular padding mechanism is requested but is not available in the environment. This also is also highly unlikely to happen.
     * @return Returns the initialized <code>CipherKey</code> object for encryption.
     * @see WizCrypt#encrypt(InputStream is, OutputStream os, CipherKey ce)
     * @see CipherKey
     */
    public static CipherKey getCipherKeyForEncrypt(String keyStr) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException{
        return getCipherKey(keyStr, Cipher.ENCRYPT_MODE);
    }
    
    /**
     * This is the public API used for getting the <code>CipherKey</code> object
     * used in <code>WizCrypt</code> public APIs.
     *
     * @param keyStr The password used for creating the cipher.
     * @throws NoSuchAlgorithmException When the Algorithm used by WizCrypt (RC4) is not found in JVM. This is highly unlikely, because SUN JVM has this.
     * @throws InvalidKeyException The key should be of specific size. If this size limits are not met, <code>InvalidKeyException</code> exception is thrown.
     * @throws NoSuchPaddingException This exception is thrown when a particular padding mechanism is requested but is not available in the environment. This also is also highly unlikely to happen.
     * @return Returns the initialized <code>CipherKey</code> object for decryption.
     * @see WizCrypt#decrypt(InputStream is, OutputStream os, CipherKey ce)
     * @see CipherKey
     */
    public static CipherKey getCipherKeyForDecrypt(String keyStr) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException{
        return getCipherKey(keyStr, Cipher.DECRYPT_MODE);
    }
}