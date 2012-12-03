package com.kakapo.unity.server.cryptography;

import com.kakapo.unity.server.cryptography.misc.BASE64Encoder;
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

            // Create the cipher 
            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            // Initialize the cipher for encryption
            desCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Encrypt the text
            byte[] textEncrypted = desCipher.doFinal(loginId);

            BASE64Encoder encoder = new BASE64Encoder();
            serverCheckSum = encoder.encode(textEncrypted);
//            System.out.println("Text Encryted : " + serverCheckSum);

            textEncrypted = null;
            secretKey = null;
            desCipher = null;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            //Logg INVALID DES KEY
            Logger.getLogger(DESEncryptor.class.getName()).log(Level.SEVERE, "Error while computing CheckSum! ", e);
            serverCheckSum = "CHECKSUM IS INVALID";
        }
        loginId = null;
        return serverCheckSum;
//        return clientLoginId;
    }
}
