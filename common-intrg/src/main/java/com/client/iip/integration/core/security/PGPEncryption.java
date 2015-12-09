package com.client.iip.integration.core.security;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;

/**
 * @author vmuthiah
 *
 */
public class PGPEncryption {
	
	private String keyFilePath = null;
	
	public PGPEncryption() {
			Security.addProvider(new BouncyCastleProvider());
	}
	

	public void setKeyFilePath(String _keyFilePath){
		keyFilePath = _keyFilePath;
	}
	
	public PGPPublicKey readPublicKey(String filePath)
		    throws IOException, PGPException
		{
		
			InputStream in = new FileInputStream(filePath);
			
		    in = PGPUtil.getDecoderStream(in);

		    PGPPublicKeyRingCollection        pgpPub = new PGPPublicKeyRingCollection(in);

		    //
		    // Through the collection till and find a key suitable for encryption
		    // iterate through the key rings.
		    //
		    Iterator rIt = pgpPub.getKeyRings();

		    while (rIt.hasNext())
		    {
		        PGPPublicKeyRing    kRing = (PGPPublicKeyRing)rIt.next();    
		        Iterator kIt = kRing.getPublicKeys();

		        while (kIt.hasNext())
		        {
		            PGPPublicKey    k = (PGPPublicKey)kIt.next();

		            if (k.isEncryptionKey())
		            {
		                return k;
		            }
		        }
		    }

		    throw new IllegalArgumentException("Can't find encryption key in key ring.");
		}
	
	

		public String encryptFile(String inData)
		    throws IOException, NoSuchProviderException
		{
			String encryptedString = "";
		    try
		    {				
				PGPPublicKey pubKey = readPublicKey(this.keyFilePath);
		
		        PGPEncryptedDataGenerator   cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, false, new SecureRandom(), "BC");
		
		        cPk.addMethod(pubKey);
		
		        byte[]  bytes = inData.getBytes();
		        
		        OutputStream outReturn = new ByteArrayOutputStream();
		        
                OutputStream  cOut = cPk.open(outReturn, bytes.length);
                
                cOut.write(bytes);
        
                cOut.close();
        
                outReturn.close();		        
		
                encryptedString = outReturn.toString();
		    }
		    catch (PGPException e)
		    {
		        System.err.println(e);
		        if (e.getUnderlyingException() != null)
		        {
		            e.getUnderlyingException().printStackTrace();
		        }
		    }
			
			return encryptedString;
		}    
		
}
