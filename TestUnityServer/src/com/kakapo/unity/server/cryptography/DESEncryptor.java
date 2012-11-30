/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.server.cryptography;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author felix.vincent
 */
public final class DESEncryptor {

    private String serverCheckSum = "";
    private final byte[] password;

    public DESEncryptor() {
        this.password = new DESSecretKey().getKey();
    }

    public String computeCheckSum(String clientLoginId) {
        byte[] loginId = clientLoginId.getBytes();
        try {
            SecretKey secretKey = new SecretKeySpec(password, "DES");
            Cipher desCipher = Cipher.getInstance("DES");
            desCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] textEncrypted = desCipher.doFinal(loginId);

            serverCheckSum = new String(textEncrypted, "UTF-8");
            
            textEncrypted = null;
            secretKey = null;
            desCipher = null;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DESEncryptor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            //Logg INVALID DES KEY
            Logger.getLogger(DESEncryptor.class.getName()).log(Level.SEVERE, "Error while computing CheckSum! ", e);
            serverCheckSum = "CHECKSUM IS INVALID";
        }
        loginId = null;
//        return serverCheckSum;
        return clientLoginId;
    }
}
