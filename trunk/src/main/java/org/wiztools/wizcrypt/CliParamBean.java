/*
 * CliParamBean.java
 *
 * Created on 14 February 2007, 22:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.wiztools.wizcrypt;

/**
 *
 * @author subhash
 */
public class CliParamBean {
    
    private boolean forceOverwrite = false;
    private boolean keepSource = false;
    private boolean isOldFormat = false;
    private boolean recurseIntoDir = false;
    private boolean verbose = false;
    
    /** Creates a new instance of CliParamBean */
    public CliParamBean() {
    }
    
    public void setForceOverwrite(final boolean forceOverwrite){
        this.forceOverwrite = forceOverwrite;
    }
    
    public boolean getForceOverwrite(){
        return forceOverwrite;
    }
    
    public void setKeepSource(final boolean keepSource){
        this.keepSource = keepSource;
    }
    
    public boolean getKeepSource(){
        return keepSource;
    }
    
    public void setIsOldFormat(final boolean isOldFormat){
        this.isOldFormat = isOldFormat;
    }
    
    public boolean getIsOldFormat(){
        return isOldFormat;
    }
    
    public void setRecurseIntoDir(final boolean recurseIntoDir){
        this.recurseIntoDir = recurseIntoDir;
    }
    
    public boolean getRecurseIntoDir(){
        return recurseIntoDir;
    }
    
    public void setVerbose(final boolean verbose){
        this.verbose = verbose;
    }
    
    public boolean getVerbose(){
        return verbose;
    }
}
