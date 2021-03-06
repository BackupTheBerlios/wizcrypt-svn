/*
 * MainTest.java
 * JUnit based test
 *
 * Created on March 2, 2006, 5:32 PM
 */

package org.wiztools.wizcrypt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.io.IOException;
import java.io.File;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wiztools.wizcrypt.exception.DestinationFileExistsException;
import org.wiztools.wizcrypt.exception.FileCorruptException;
import org.wiztools.wizcrypt.exception.PasswordMismatchException;
import org.wiztools.wizcrypt.impl.Decrypt07;
import org.wiztools.wizcrypt.impl.Encrypt07;

/**
 *
 * @author subhash
 */
public class MainTest {
    
    private static Logger LOG;
    
    final String path = System.getProperty("java.io.tmpdir")+
            File.separator + "wiztest.txt";
    
    final String password = "testpwd";
    
    final String wrongPassword = "wrongpwd";
    
    // Please provide content with NO new lines (\n):
    final String content = "This is a dummy text!";
    
    File fin = new File(path);
    File fout = new File(path+".wiz");
    
    ParamBean cpb = new ParamBean();
    
    private void createFile() throws IOException{
        // Write dummy content into the file
        
        PrintWriter pw = new PrintWriter(new FileWriter(fin));
        pw.println(content);
        pw.close();
        
    }
    
    @Before
    public void setUp() throws Exception {
        System.out.println("setUp()");
        try{
            LogManager.getLogManager().readConfiguration(
                    Main.class.getClassLoader()
                    .getResourceAsStream("org/wiztools/wizcrypt/logging.properties"));
            LOG = Logger.getLogger(MainTest.class.getName());
        } catch(IOException ioe){
            ioe.printStackTrace();
            assert true: "Logger configuration load failed!";
        }
        
        cpb.setForceOverwrite(true);
        cpb.setKeepSource(true);
        
        fin.deleteOnExit();
        
        createFile();
        
        System.out.println("setUp() End");
    }
    
    @After
    public void tearDown() throws Exception {
        System.out.println("tearDown()");
        
        try{
            // Read content to verify
            BufferedReader br = new BufferedReader(new FileReader(fin));
            String str = br.readLine();
            br.close();

            System.out.println("Checking for equality");
            Assert.assertEquals(content, str);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("tearDown() End");
    }
    
    /**
     * Test of main method, of class org.wiztools.crypt.Main.
     */
    @Test
    public void testMain() throws Exception {
        setUp();
        System.out.println("main");
        
        // Encryption
        
        IProcess e = Encrypt07.getInstance();
        
        try {
            WizCryptBean wcb = new WizCryptBean();
            wcb.setPassword(password.toCharArray());

            System.out.println("before processing...");
            cpb.setKeepSource(true);
            e.process(fin, wcb, cpb);
            System.out.println("processing over...");
        } catch(DestinationFileExistsException dfe){
            dfe.printStackTrace();
            LOG.severe(dfe.getMessage());
            Assert.fail(dfe.getMessage());
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
            LOG.severe(ex.getMessage());
            Assert.fail(ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            LOG.severe(ex.getMessage());
            Assert.fail(ex.getMessage());
        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();
            LOG.severe(ex.getMessage());
            Assert.fail(ex.getMessage());
        }
        
        // Decryption
        
        IProcess d = Decrypt07.getInstance();
        
        // test for wrong password

        WizCryptBean wcb = new WizCryptBean();
        wcb.setPassword(wrongPassword.toCharArray());
        try {
            d.process(fout, wcb, cpb);
            System.out.println("My message!");
            Assert.fail("Cannot process for wrong supplied password!!!");
        } catch(DestinationFileExistsException dfe){
            dfe.printStackTrace();
            LOG.severe(dfe.getMessage());
            Assert.fail(dfe.getMessage());
        } catch (PasswordMismatchException ex) {
            System.out.println("Should visit here!");
            // should be visited here;
            // create deleted file:
            createFile();
        } catch(FileCorruptException ffe){
            ffe.printStackTrace();
            LOG.severe(ffe.getMessage());
            Assert.fail(ffe.getMessage());
        }
    }
    
    @Test
    public void fileExistenceTest() throws Exception{
        IProcess d = Decrypt07.getInstance();
        try {
            WizCryptBean wcb = new WizCryptBean();
            wcb.setPassword(password.toCharArray());
            
            // Try processing file not ending with .wiz
            try{
                d.process(new File(path+".xxx"), wcb, cpb);
                Assert.fail("Cannot process for file not ending with .wiz");
            } catch(FileNotFoundException fnfe){
                // should be visited here
            } catch(DestinationFileExistsException dfe){
                dfe.printStackTrace();
                LOG.severe(dfe.getMessage());
                Assert.fail(dfe.getMessage());
            } catch(PasswordMismatchException ex){
                ex.printStackTrace();
                LOG.severe(ex.getMessage());
                Assert.fail(ex.getMessage());
            } catch(FileCorruptException ffe){
                ffe.printStackTrace();
                LOG.severe(ffe.getMessage());
                Assert.fail(ffe.getMessage());
            }
            
            try{
                d.process(fout, wcb, cpb);
            } catch (PasswordMismatchException ex) {
                ex.printStackTrace();
                LOG.severe(ex.getMessage());
                Assert.fail(ex.getMessage());
            } catch(FileCorruptException ffe){
                ffe.printStackTrace();
                LOG.severe(ffe.getMessage());
                Assert.fail(ffe.getMessage());
            }
        } catch(DestinationFileExistsException dfe){
            dfe.printStackTrace();
            LOG.severe(dfe.getMessage());
            Assert.fail(dfe.getMessage());
        }
        tearDown();
    }
}
