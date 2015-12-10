package com.client.iip.integration.core.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

/**
 * @author vmuthiah
 * 
 */
public class PGPEncryption {

	private static final Log logger = LogFactory.getLog(PGPEncryption.class);
	private String keyFilePath = null;

	public PGPEncryption() {
		Security.addProvider(new BouncyCastleProvider());
	}

	public void setKeyFilePath(String _keyFilePath) {
		keyFilePath = _keyFilePath;
	}

    private PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
            throws PGPException, NoSuchProviderException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

        if (pgpSecKey == null) {
            return null;
        }

        return pgpSecKey.extractPrivateKey(pass, "BC");
    }

    /**
     * decrypt the byte[] 
     * 
     * @param encrypted The message to be decrypted.
     * @param passPhrase Pass phrase (key)
     * 
     * @return Clear text as a byte array. 
     * @exception IOException
     * @exception PGPException
     * @exception NoSuchProviderException
     */
    public byte[] decrypt(byte[] encrypted, InputStream keyIn, char[] password)
            throws IOException, PGPException, NoSuchProviderException {
    	
    	logger.info("Inside Decrypt");
    	
        InputStream in = new ByteArrayInputStream(encrypted);

        in = PGPUtil.getDecoderStream(in);

        PGPObjectFactory pgpF = new PGPObjectFactory(in);
        PGPEncryptedDataList enc = null;
        Object o = pgpF.nextObject();

        if (o instanceof PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        // find the secret key
        Iterator it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
                PGPUtil.getDecoderStream(keyIn));

        while (sKey == null && it.hasNext()) {
            pbe = (PGPPublicKeyEncryptedData) it.next();

            sKey = findSecretKey(pgpSec, pbe.getKeyID(), password);
        }

        if (sKey == null) {
            throw new IllegalArgumentException("Secret key for message not found.");
        }
        
        InputStream clear = pbe.getDataStream(sKey, "BC");
        
        PGPObjectFactory pgpFact = new PGPObjectFactory(clear);

        Object message = pgpFact.nextObject();
        
        PGPLiteralData ld = (PGPLiteralData) message;

        InputStream unc = ld.getInputStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int ch;

        while ((ch = unc.read()) >= 0) {
            out.write(ch);
        }

        byte[] returnBytes = out.toByteArray();
        out.close();
        in.close();
    	logger.info("Decrypted");
        return returnBytes;
    }

    /**
     * Simple PGP encryptor between byte[].
     * 
     * @param dataToEncrypt The test to be encrypted
     * @param passPhrase
     *            The pass phrase (key). This method assumes that the key is a
     *            simple pass phrase, and does not yet support RSA or more
     *            sophisiticated keying.
     * @param fileName
     *            File name. This is used in the Literal Data Packet (tag 11)
     *            which is really inly important if the data is to be related to
     *            a file to be recovered later. Because this routine does not
     *            know the source of the information, the caller can set
     *            something here for file name use that will be carried. If this
     *            routine is being used to encrypt SOAP MIME bodies, for
     *            example, use the file name from the MIME type, if applicable.
     *            Or anything else appropriate.
     * 
     * @param armor
     * 
     * @return encrypted data.
     * @exception IOException
     * @exception PGPException
     * @exception NoSuchProviderException
     */
    public byte[] encrypt(byte[] dataToEncrypt, PGPPublicKey encKey,
            String fileName,boolean withIntegrityCheck, boolean armor)
            throws IOException, PGPException, NoSuchProviderException {
    	logger.info("Inside Encrypt");
        if (fileName == null) {
            fileName = PGPLiteralData.CONSOLE;
        }

        ByteArrayOutputStream encOut = new ByteArrayOutputStream();

        OutputStream out = encOut;
        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(bOut, PGPLiteralData.BINARY, fileName, dataToEncrypt.length, new Date());
        pOut.write(dataToEncrypt);
        lData.close();

        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(
                PGPEncryptedData.CAST5, withIntegrityCheck, new SecureRandom(),
                "BC");

        cPk.addMethod(encKey);

        byte[] bytes = bOut.toByteArray();

        OutputStream cOut = cPk.open(out, bytes.length);
        cOut.write(bytes);

        cOut.close();

        out.close();

    	logger.info("Encrypted");
        return encOut.toByteArray();
    }

    /**
     * Read the public key from the Key file
     * @param in
     * @return PGPPublicKey
     * @throws IOException
     * @throws PGPException
     */
    private PGPPublicKey readPublicKey(InputStream keyFileString)
            throws IOException, PGPException {
    	keyFileString = PGPUtil.getDecoderStream(keyFileString);

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(keyFileString);

        //Loop through the collection till we find a key
        Iterator rIt = pgpPub.getKeyRings();

        while (rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();

            while (kIt.hasNext()) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();

                if (k.isEncryptionKey()) {
                    return k;
                }
            }
        }

        throw new IllegalArgumentException("Cannot find encryption key in key ring.");
    }
    
    /**
     * Encrypt the given data in byte[]
     * @param dataToEncrypt
     * @return encrypted byte[]
     * @throws Exception
     */
    public byte[] encrypt(String dataToEncrypt) throws Exception {
    	FileInputStream pubKey = new FileInputStream(keyFilePath);
    	return encrypt(dataToEncrypt.getBytes(), readPublicKey(pubKey), null, true, true);
    }

    /**
     * Decrypt the given data in byte[]
     * @param encryptedData data to be decrypted
     * @param passphrase password for the private key file
     * @return decrypted byte[]
     * @throws Exception
     */
    public byte[] decrypt(String encryptedData, String passphrase) throws Exception {
        FileInputStream secKey = new FileInputStream(keyFilePath);
        return decrypt(encryptedData.getBytes(), secKey, passphrase.toCharArray());
    }
    
    
	public static void main(String[] a) {
		PGPEncryption pgpEnc = new PGPEncryption();
		pgpEnc.setKeyFilePath("c:/cwsj/iip/fremont/FremontPublic.asc");
        try {
			byte[] encryptedData = pgpEnc.encrypt("Hello world");

			//Key file for decrypting
			pgpEnc.setKeyFilePath("c:/cwsj/iip/fremont/FremontPriv.asc");
			byte[] decryptedData = pgpEnc.decrypt(new String(encryptedData), "fremont");
			System.out.println("Decrypted data is = " + new String(decryptedData));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}    
}
