package songbook.server;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Created by laurent on 02/02/2016.
 */
public class PasswordLessAuthTest {
    @Test public void testFakeLogin() {
        PasswordLessAuth auth = new PasswordLessAuth();
        String token = auth.newLogin("llg@mail.com");
        System.out.println(token);
        assertTrue(auth.validate("llg@mail.com", token));
        assertFalse(auth.validate("llg@mail.com", token));
    }
}
