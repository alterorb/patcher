package net.alterorb.patcher;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public record RSAKeyFactory(KeyFactory factory, KeyPairGenerator generator) {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    public static RSAKeyFactory create() {
        try {
            KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(KEY_SIZE);

            return new RSAKeyFactory(factory, keyGen);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("RSA algorithm is not available on this platform", e);
        }
    }

    public KeyPair generateKeyPair() {
        return generator.generateKeyPair();
    }

    public RSAPublicKeySpec publicKeySpecFrom(KeyPair keyPair) {
        return publicKeySpecFrom(keyPair.getPublic());
    }

    public RSAPublicKeySpec publicKeySpecFrom(PublicKey publicKey) {
        try {
            return factory.getKeySpec(publicKey, RSAPublicKeySpec.class);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid key spec", e);
        }
    }

    public RSAPublicKeySpec publicKeySpecFrom(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            var encodedKeySpec = new X509EncodedKeySpec(bytes);

            var publicKey = factory.generatePublic(encodedKeySpec);

            return publicKeySpecFrom(publicKey);
        } catch (IOException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to load pubkey", e);
        }
    }
}
