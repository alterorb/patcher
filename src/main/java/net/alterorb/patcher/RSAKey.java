package net.alterorb.patcher;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public record RSAKey(KeyFactory factory, KeyPairGenerator generator) {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    public static RSAKey create() throws NoSuchAlgorithmException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        return new RSAKey(factory, keyGen);
    }

    public KeyPair generate() {
        return generator.generateKeyPair();
    }

    public RSAPublicKeySpec loadPublicKeySpec(InputStream inputStream) throws IOException, InvalidKeySpecException {
        var bytes = inputStream.readAllBytes();
        var encodedKeySpec = new X509EncodedKeySpec(bytes);

        var publicKey = factory.generatePublic(encodedKeySpec);

        return factory.getKeySpec(publicKey, RSAPublicKeySpec.class);
    }
}
