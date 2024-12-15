package org.dstadler.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.dstadler.commons.testing.ThreadTestHelper;
import org.junit.jupiter.api.Test;

public class SimpleAuthenticatorTest {

	private static final int NUMBER_OF_THREADS = 20;
	private static final int NUMBER_OF_TESTS = 10000;

	@Test
	public void testSimpleAuthenticator() {
		SimpleAuthenticator auth = new SimpleAuthenticator("user", "password");
		assertNotNull(auth.getPasswordAuthentication());
	}

    @Test
    public void testMultipleThreads() throws Throwable {

        ThreadTestHelper helper =
            new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

        helper.executeTest(new ThreadTestHelper.TestRunnable() {
            @Override
            public void doEnd(int threadnum) {
                // do stuff at the end ...
            }

            @Override
            public void run(int threadnum, int iter) {
            	if(iter == 0) {
            		SimpleAuthenticator auth = new SimpleAuthenticator("user-" + threadnum, "password-" + threadnum);
            		Authenticator.setDefault(auth);
            	}

            	PasswordAuthentication authenticator = Authenticator.requestPasswordAuthentication("", null, 0, "", "", "");
            	assertEquals("user-" + threadnum, authenticator.getUserName());
            	//assertEquals("password-" + threadnum, authenticator.getPassword());
            }
        });
    }
}
