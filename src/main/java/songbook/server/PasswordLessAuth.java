package songbook.server;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements an authentication that use a token that is used once and has been sent by mail
 *
 * Created by laurent on 02/02/2016.
 */
public class PasswordLessAuth {

    private SecureRandom random = new SecureRandom();

    private Map<String, String> tokens = new HashMap<>();

    public String newLogin(String address) {
        String token = new BigInteger(130, random).toString(32);
        tokens.put(address, token);
        return token;
    }

    public boolean validate(String address, String token) {
        String storedToken = tokens.get(address);
        if (storedToken != null && token.equals(storedToken)) {
            tokens.remove(address);
            return true;
        }
        return false;
    }

}
