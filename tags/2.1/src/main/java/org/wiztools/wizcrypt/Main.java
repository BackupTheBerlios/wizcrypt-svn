package org.wiztools.wizcrypt;

import java.io.Console;
import java.io.InputStream;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.security.InvalidKeyException;
import java.security.GeneralSecurityException;

import java.io.IOException;
import java.io.File;

import java.util.ResourceBundle;

/**
 * The class which does the commandline parsing the calls the public APIs to encrypt/decrypt.
 */
public class Main{
    
    private static final ResourceBundle rb = ResourceBundle.getBundle("org.wiztools.wizcrypt.wizcryptmsg");
    
    // Error booleans
    // Note: Even when just one of the input file
    // triggers any of these Exceptions, the flag
    // is set.
    private static boolean IO_EXCEPTION = false;
    private static boolean INVALID_PWD = false;
    private static boolean PWD_MISMATCH = false;
    private static boolean SECURITY_EXCEPTION = false;
    private static boolean PARSE_EXCEPTION = false;
    private static boolean CONSOLE_NOT_AVBL_EXCEPTION = false;
    private static boolean DEST_FILE_EXISTS = false;
    
    // Error codes
    private static final int C_IO_EXCEPTION = 1;
    private static final int C_INVALID_PWD = 2;
    private static final int C_PWD_MISMATCH = 3;
    private static final int C_CONSOLE_NOT_AVBL_EXCEPTION = 7;
    private static final int C_DEST_FILE_EXISTS = 8;
    // Both of the above
    private static final int C_MULTIPLE_EXCEPTION = 4;
    private static final int C_SECURITY_EXCEPTION = 5;
    private static final int C_PARSE_EXCEPTION = 6;
    
    private Options generateOptions(){
        Options options = new Options();
        Option option = OptionBuilder.withLongOpt("password")
                .hasOptionalArg()
                .isRequired(false)
                .withDescription(rb.getString("msg.password"))
                .create('p');
        options.addOption(option);
        option = OptionBuilder.withLongOpt("encrypt")
                .isRequired(false)
                .withDescription(rb.getString("msg.encrypt"))
                .create('e');
        options.addOption(option);
        option = OptionBuilder.withLongOpt("decrypt")
                .isRequired(false)
                .withDescription(rb.getString("msg.decrypt"))
                .create('d');
        options.addOption(option);
        option = OptionBuilder.withLongOpt("help")
                .isRequired(false)
                .withDescription(rb.getString("msg.help"))
                .create('h');
        options.addOption(option);
        option = OptionBuilder.withLongOpt("version")
                .isRequired(false)
                .withDescription(rb.getString("msg.version"))
                .create('v');
        options.addOption(option);
        option = OptionBuilder.withLongOpt("force-overwrite")
                .isRequired(false)
                .withDescription(rb.getString("msg.force.overwrite"))
                .create('f');
        options.addOption(option);
        return options;
    }
    
    private void printCommandLineHelp(Options options){
        HelpFormatter hf = new HelpFormatter();
        String cmdLine = rb.getString("display.cmdline");
        String descriptor = rb.getString("display.detail");
        String moreHelp = rb.getString("display.footer");
        hf.printHelp(cmdLine, descriptor, options, moreHelp);
    }
    
    private void printVersionInfo(){
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/wiztools/wizcrypt/VERSION");
        try{
            int i = -1;
            while((i=is.read())!=-1){
                System.out.print((char)i);
            }
            is.close();
            System.out.println();
        } catch(IOException ioe) {
            assert true: "VERSION file not found in Jar!";
        }
    }
    
    private char[] getConsolePassword(String msg) throws PasswordMismatchException,
                    ConsoleNotAvailableException{
        Console cons = System.console();
        if(cons == null){
            throw new ConsoleNotAvailableException();
        }
        char[] passwd;
        passwd = cons.readPassword("%s ", msg);
        if(passwd == null){
            throw new PasswordMismatchException(rb.getString("err.no.pwd"));
        }

        return passwd;
    }
    
    private char[] getConsolePassword() throws PasswordMismatchException,
                    ConsoleNotAvailableException{
        return getConsolePassword(rb.getString("msg.interactive.password"));
    }
    
    private char[] getConsolePasswordVerify() throws PasswordMismatchException,
                    ConsoleNotAvailableException{
        char[] passwd = getConsolePassword(rb.getString("msg.interactive.password"));
        char[] passwd_retype = getConsolePassword(rb.getString("msg.interactive.password.again"));
        if(!Arrays.equals(passwd, passwd_retype)){
            throw new PasswordMismatchException(rb.getString("err.interactive.pwd.not.match"));
        }
        return passwd;
    }
    
    public Main(String[] arg){
        Options options = generateOptions();
        try{
            boolean encrypt = false;
            boolean decrypt = false;
            boolean forceOverwrite = false;
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, arg);
            if(cmd.hasOption('h')){
                printCommandLineHelp(options);
                return;
            }
            if(cmd.hasOption('v')){
                printVersionInfo();
                return;
            }
            if(cmd.hasOption('f')){
                forceOverwrite = true;
            }
            if(cmd.hasOption('e')){
                encrypt = true;
            }
            if(cmd.hasOption('d')){
                decrypt = true;
            }
            if(encrypt && decrypt){
                throw new ParseException(rb.getString("err.both.selected"));
            }
            if(!encrypt && !decrypt){
                throw new ParseException(rb.getString("err.none.selected"));
            }
            char[] pwd = null;
            if(cmd.hasOption('p')){
                String pwdStr = cmd.getOptionValue('p');
                if(pwdStr == null){
                    pwd = encrypt ? getConsolePasswordVerify() : getConsolePassword();
                }
                else{
                    pwd = pwdStr.toCharArray();
                }
                
            } else{
                pwd = encrypt ? getConsolePasswordVerify() : getConsolePassword();
            }
            IProcess iprocess = null;
            if(encrypt){
                iprocess = new Encrypt();
            } else if(decrypt){
                iprocess = new Decrypt();
            }
            iprocess.init(new String(pwd));
            String[] args = cmd.getArgs();
            if(args.length == 0){
                throw new ParseException(rb.getString("err.no.file"));
            }
            for(int i=0;i<args.length;i++){
                File f = new File(args[i]);
                try{
                    iprocess.process(f, forceOverwrite);
                } catch(DestinationFileExistsException dfe){
                    DEST_FILE_EXISTS = true;
                    System.err.println(dfe.getMessage());
                } catch(PasswordMismatchException pme){
                    PWD_MISMATCH = true;
                    System.err.println(pme.getMessage());
                } catch(IOException ioe){
                    IO_EXCEPTION = true;
                    System.err.println(ioe.getMessage());
                }
            }
        } catch(ParseException pe){
            PARSE_EXCEPTION = true;
            System.err.println(pe.getMessage());
            printCommandLineHelp(options);
        } catch(ConsoleNotAvailableException cna){
            CONSOLE_NOT_AVBL_EXCEPTION = true;
            System.err.println(rb.getString("err.console.not.avbl"));
        } catch(PasswordMismatchException pme){
            INVALID_PWD = true;
            System.err.println(pme.getMessage());
        } catch(InvalidKeyException ike){
            INVALID_PWD = true;
            System.err.println(rb.getString("err.invalid.pwd"));
        } catch(GeneralSecurityException gse){
            SECURITY_EXCEPTION = true;
            System.err.println(gse.getMessage());
        }
    }
    
    public static void main(String[] arg){
        new Main(arg);
        
        // Set program exit value
        int exitVal = 0;
        if(PARSE_EXCEPTION){
            exitVal = C_PARSE_EXCEPTION;
        } else if(CONSOLE_NOT_AVBL_EXCEPTION){
            exitVal = C_CONSOLE_NOT_AVBL_EXCEPTION;
        } else if(INVALID_PWD){
            exitVal = C_INVALID_PWD;
        } else if(SECURITY_EXCEPTION){
            exitVal = C_SECURITY_EXCEPTION;
        } else{
            int count = 0; // count the number of exceptions
            if(DEST_FILE_EXISTS){
                exitVal = C_DEST_FILE_EXISTS;
                count++;
            }
            if(IO_EXCEPTION){
                exitVal = C_IO_EXCEPTION;
                count++;
            }
            if(PWD_MISMATCH){
                exitVal = C_PWD_MISMATCH;
                count++;
            }
            if(count > 1){
                exitVal = C_MULTIPLE_EXCEPTION;
            }
        }
        System.exit(exitVal);
    }

}
