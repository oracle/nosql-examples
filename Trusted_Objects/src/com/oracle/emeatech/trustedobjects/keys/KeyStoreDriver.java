package com.oracle.emeatech.trustedobjects.keys;

import com.oracle.emeatech.trustedobjects.TrustedObjectException;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Provides KeyStore Services for Trusted Objects
 *
 * @author ewan
 */
public class KeyStoreDriver
{

    private char[] keyStorePassword;
    private char[] privateKeyPassword;
    private File keyStoreFile;
    private KeyStore keyStore;

    private KeyStoreDriver(String fileName, String keyStorePassword, String privateKeyPassword)
    {
        this.keyStorePassword = keyStorePassword.toCharArray();
        this.privateKeyPassword = privateKeyPassword.toCharArray();
        this.keyStoreFile = new File(fileName);
    }

    private KeyStoreDriver(File file, String keyStorePassword, String privateKeyPassword)
    {
        this.keyStorePassword = keyStorePassword.toCharArray();
        this.privateKeyPassword = privateKeyPassword.toCharArray();
        this.keyStoreFile = file;
    }

    //open the KeyStore
    public void open() throws TrustedObjectException
    {
        try (FileInputStream fis = new FileInputStream(this.getKeyStoreFile()))
        {
            this.getKeyStore().load(fis, this.getKeyStorePassword());
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    //read a Public Key from the KeyStore
    public PublicKey getPublicKey(String alias) throws TrustedObjectException
    {
        try
        {
            return this.getKeyStore().getCertificate(alias).getPublicKey();
        } catch (KeyStoreException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    public PrivateKey getPrivateKey(String alias) throws TrustedObjectException
    {
        try
        {
            return (PrivateKey) this.getKeyStore().getKey(alias, this.getPrivateKeyPassword());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException x)
        {
            throw new TrustedObjectException(x);
        }

    }

    //generate a new symmetric SecretKey
    public static SecretKey makeNewSecretKey(KeySize aKeySize, String algorithm)
    {
        //note: a SecretKeySpec is a SecretKey
        return new SecretKeySpec(aKeySize.getByteArray(), algorithm);
    }

    //write a Key to the KeyStore
    public void storeSecretKey(SecretKey key, String alias) throws TrustedObjectException
    {
        KeyStore.SecretKeyEntry ske = new KeyStore.SecretKeyEntry(key);
        try
        {
            this.getKeyStore().setEntry(alias, ske, new KeyStore.PasswordProtection(this.getKeyStorePassword()));
        } catch (KeyStoreException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    //get a secret key from the KeyStore
    public SecretKey getSecretKey(String alias) throws TrustedObjectException
    {
        try {
        return (SecretKey) this.getKeyStore().getKey(alias, this.getKeyStorePassword());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    //save the KeyStore
    public void save() throws TrustedObjectException
    {
        try (FileOutputStream fos = new FileOutputStream(this.getKeyStoreFile()))
        {
            this.getKeyStore().store(fos, this.getKeyStorePassword());
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException x)
        {
            throw new TrustedObjectException(x);
        }
    }

    /**
     * @return the keyStoreFile
     */
    private File getKeyStoreFile()
    {
        return keyStoreFile;
    }

    /**
     * @return the _KeyStore
     */
    private KeyStore getKeyStore() throws KeyStoreException
    {
        if (this.keyStore == null)
        {
            this.keyStore = KeyStore.getInstance("JCEKS");
        }
        return this.keyStore;
    }

    private static KeyStoreDriver openKeyStoreDriver(KeyStoreDriver ksd) throws TrustedObjectException
    {
        ksd.open();
        return ksd;
    }

    public static KeyStoreDriver getInstance(String fileName, String keyStorePassword, String privateKeyPassword) throws TrustedObjectException
    {
        return openKeyStoreDriver(new KeyStoreDriver(fileName, keyStorePassword, privateKeyPassword));
    }

    public static KeyStoreDriver getInstance(File file, String keyStorePassword, String privateKeyPassword) throws TrustedObjectException
    {
        return openKeyStoreDriver(new KeyStoreDriver(file, keyStorePassword, privateKeyPassword));
    }

    /**
     * @return the keyStorePassword
     */
    public char[] getKeyStorePassword()
    {
        return keyStorePassword;
    }

    /**
     * @return the privateKeyPassword
     */
    public char[] getPrivateKeyPassword()
    {
        return privateKeyPassword;
    }
}
