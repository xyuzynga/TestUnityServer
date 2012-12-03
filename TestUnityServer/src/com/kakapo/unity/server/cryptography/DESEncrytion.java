package com.kakapo.unity.server.cryptography;

final class DESEncrytion {

    private String doConvertToHex(byte[] mSerial) {
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
    //<editor-fold defaultstate="collapsed" desc="commented Main">
    ////    public static void main(String[] argv) throws UnsupportedEncodingException {
    ////
    ////        System.out.println(new DESEncryptor().computeCheckSum("TestApp@drd.co.in"));
    ////        try {
    ////
    //////            KeyGenerator keygenerator = KeyGenerator.getInstance("DES");
    //////            SecretKey myDesKey = keygenerator.generateKey();
    ////            byte[] secretKey = new DESSecretKey().getKey();
    //////            byte[] secretKey = "password".getBytes();
    ////            SecretKey myDesKey = new SecretKeySpec(secretKey, "DES");
    ////
    ////            // Create the cipher
    ////            Cipher desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
    ////
    //////*****************************************************************************************************************************
    ////            // Initialize the cipher for encryption
    ////            desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
    ////
    ////            //sensitive information
    ////            byte[] text = "TestApp@drd.co.in".getBytes();
    ////
    ////            System.out.println("Text [Byte Format] : " + text);
    ////            System.out.println("Text : " + new String(text));
    ////
    ////            // Encrypt the text
    ////            byte[] textEncrypted = desCipher.doFinal(text);
    ////
    //////            System.out.println(new DESEncrytion().doConvertToHex(textEncrypted));
    ////
    ////            BASE64Encoder encoder = new BASE64Encoder();
    ////            String encryptedText = encoder.encode(textEncrypted);
    ////            System.out.println("Text Encryted : " + encryptedText);
    ////
    ////
    //////            System.out.println("Text Encryted : " + new String(textEncrypted));
    ////            //*******************************************************************************************************************************
    ////            // Initialize the same cipher for decryption
    ////            desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
    ////
    ////            // Decrypt the text
    ////            byte[] textDecrypted = desCipher.doFinal(textEncrypted);
    //////             byte[] textDecrypted = desCipher.doFinal("uS0urtjDwaN9rt+UJA2ylHQ21uQJ3cdK".getBytes());
    ////
    ////            System.out.println("Text Decryted : " + new String(textDecrypted));
    ////
    ////        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
    ////            e.printStackTrace();
    ////        }
    ////    }
    //</editor-fold>
}
