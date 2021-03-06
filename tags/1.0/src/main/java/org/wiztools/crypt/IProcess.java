package org.wiztools.crypt;

import javax.crypto.NoSuchPaddingException;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public interface IProcess{
	public void init(String password) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException;
	public void process(File file) throws IOException, FileNotFoundException, PasswordMismatchException;
}
