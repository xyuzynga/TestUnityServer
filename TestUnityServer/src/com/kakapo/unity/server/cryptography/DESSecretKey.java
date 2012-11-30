package com.kakapo.unity.server.cryptography;

final class DESSecretKey {

    private final byte[] key = {114, 54, 110, 48, 115, 56, 109, 51};

    DESSecretKey() {
    }

//    public static void main(String[] args) {
//        try {
//            DESSecretKey sk = new DESSecretKey();
//            
//            ByteBuffer byteBuffer;
//            byteBuffer = ByteBuffer.wrap(getKey());
//
//            Charset charset = Charset.forName("UTF-8");
//            CharsetDecoder decoder = charset.newDecoder();
//
//            String data = decoder.decode(byteBuffer).toString();
//            System.out.print(data);
//
//        } catch (CharacterCodingException ex) {
//            Logger.getLogger(DESSecretKey.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    byte[] getKey() {
        return key;
    }
}
