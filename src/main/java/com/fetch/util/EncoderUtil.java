package com.fetch.util;

import com.fetch.util.exception.ParsingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.security.Key;

import java.util.Base64;

@Component
public class EncoderUtil {

    @Value("${encoder.salt}")
    private String SALT;

    public String maskValue(String value) throws ParsingException {

        try {
            Key key = new SecretKeySpec(SALT.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedValueBytes = cipher.doFinal(value.getBytes());

            return Base64.getEncoder().encodeToString(encryptedValueBytes);
        }
        catch (Exception e ){
            throw new ParsingException(e.getMessage());
        }

    }

}
