package com.kakapo.unity.server.cryptography;

/*
 * DESCryptoServiceProvider() des = new DESCryptoServiceProvider();  
 * des.Key = UTF8Encoding.UTF8.GetBytes(new DESSecretKey().getKey()); 
 * des.Mode = CipherMode.ECB; 
 */
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

final class DESEncrytion {

    private String doConvertSerial(byte[] mSerial) {
        StringBuilder tBuilder = new StringBuilder();
        for (int i = 0; i < mSerial.length; i++) {
            byte b = mSerial[i];
            if (b < 0) {
                tBuilder.append(Integer.toHexString(b).toUpperCase().substring(Integer.toHexString(b).length() - 2, Integer.toHexString(b).length()));
            } else if (b < 16) {
                tBuilder.append('0').append(Integer.toHexString(b).toUpperCase());
            } else {
                tBuilder.append(Integer.toHexString(b).toUpperCase());
            }
        }
        return tBuilder.toString();
    }

    public static void main(String[] argv) throws UnsupportedEncodingException {

//        System.out.println("*"+new DESEncryptor().computeCheckSum("TestApp@drd.co.in" )+"*"); 
        try {

//            KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
//            SecretKey myDesKey = keygenerator.generateKey();
            byte[] secretKey = new DESSecretKey().getKey();
            SecretKey myDesKey = new SecretKeySpec(secretKey, "DES");

            Cipher desCipher;

            // Create the cipher 
//            desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            desCipher = Cipher.getInstance("DES");
            
//*****************************************************************************************************************************
//            // Initialize the cipher for encryption
//            desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
//
//            //sensitive information
//            byte[] text = "TestApp@drd.co.in".getBytes();
//
//            System.out.println("Text [Byte Format] : " + text);
//            System.out.println("Text : " + new String(text));
//
//            // Encrypt the text
//            byte[] textEncrypted = desCipher.doFinal(text);
//
////            System.out.println(new DESEncrytion().doConvertSerial(textEncrypted));
//            
//            System.out.print("Text Encryted : " + new String(textEncrypted));
 //*******************************************************************************************************************************           
            // Initialize the same cipher for decryption
            desCipher.init(Cipher.DECRYPT_MODE, myDesKey);

            // Decrypt the text
//            byte[] textDecrypted = desCipher.doFinal(textEncrypted);
             byte[] textDecrypted = desCipher.doFinal("u/eqW4BRO68=".getBytes());
             
            System.out.println("Text Decryted : " + new String(textDecrypted));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
    }
}
