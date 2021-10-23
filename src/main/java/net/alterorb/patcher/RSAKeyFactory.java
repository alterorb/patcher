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

    public static RSAKeyFactory create() throws NoSuchAlgorithmException {
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(KEY_SIZE);

        return new RSAKeyFactory(factory, keyGen);
    }

    public KeyPair generateKeyPair() {
        return generator.generateKeyPair();
    }

    public RSAPublicKeySpec publicKeySpecFrom(KeyPair keyPair) throws InvalidKeySpecException {
        return factory.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
    }

    public RSAPublicKeySpec publicKeySpecFrom(PublicKey publicKey) throws InvalidKeySpecException {
        return factory.getKeySpec(publicKey, RSAPublicKeySpec.class);
    }

    public RSAPublicKeySpec publicKeySpecFrom(InputStream inputStream) throws IOException, InvalidKeySpecException {
        var bytes = inputStream.readAllBytes();
        var encodedKeySpec = new X509EncodedKeySpec(bytes);

        var publicKey = factory.generatePublic(encodedKeySpec);

        return publicKeySpecFrom(publicKey);
    }
}
