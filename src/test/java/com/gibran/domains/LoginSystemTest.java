package com.gibran.domains;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.gibran.domains.LoginSystem;
import com.gibran.exceptions.ExpiredTokenException;
import com.gibran.exceptions.InexistentTokenException;
import com.gibran.exceptions.NullTokenException;


public class LoginSystemTest {
	LoginSystem login;
	final long VALIDITY_IN_MINUTES = 10;
	LocalDateTime currentTimePlus11Minutes;
	LocalDateTime currentTimePlus5Minutes;
	
	@Before
	public void setUp() {
		login = new LoginSystem(VALIDITY_IN_MINUTES, Executors.newCachedThreadPool());
		currentTimePlus11Minutes = LocalDateTime.now().plusMinutes(12);
		currentTimePlus5Minutes = LocalDateTime.now().plusMinutes(5);
	}

	@Test
	public void shouldReturnAValidTokenForUpTo10Min () throws DateTimeException, NullTokenException, InexistentTokenException, ExpiredTokenException {
		Integer expectedPlayer = 1;
		String newToken = login.findOrCreateToken(expectedPlayer, LocalDateTime.now());
		Integer player = login.getTokenLogin(newToken, currentTimePlus5Minutes);
		assertEquals(expectedPlayer, player);
	}
	
	@Test (expected=ExpiredTokenException.class)
	public void shouldRejectValidationForTokenEmittedAfter10Min() throws DateTimeException, NullTokenException, InexistentTokenException, ExpiredTokenException {
		Integer expectedPlayer = 1;
		String newToken = login.findOrCreateToken(expectedPlayer, LocalDateTime.now());
		expectedPlayer = login.getTokenLogin(newToken, currentTimePlus11Minutes);
	}
	
	@Test 
	public void shouldReturnSameTokenForSameLoginFor10Min() throws DateTimeException, NullTokenException, InexistentTokenException, ExpiredTokenException, InterruptedException {
		Integer expectedPlayer = 1;
		String expectedToken = login.findOrCreateToken(expectedPlayer, LocalDateTime.now());
		Thread.sleep(2000);
		String newToken = login.findOrCreateToken(expectedPlayer, currentTimePlus5Minutes);
		assertEquals(expectedToken, newToken);
	}
	
	@Test 
	public void shouldReturnNewTokenForSameLoginAfter10Min() throws DateTimeException, NullTokenException, InexistentTokenException, ExpiredTokenException, InterruptedException {
		Integer expectedPlayer = 1;
		String expiredToken = login.findOrCreateToken(expectedPlayer, LocalDateTime.now());
		Thread.sleep(2000);
		String newToken = login.findOrCreateToken(expectedPlayer, currentTimePlus11Minutes);
		assertThat (expiredToken, not(equalTo(newToken)));
	}
	
	@Test (expected=NullTokenException.class)
	public void shouldRejectValidationForNullToken() throws DateTimeException, NullTokenException, InexistentTokenException, ExpiredTokenException {
		String newToken = null;
		Integer O = login.getTokenLogin(newToken, currentTimePlus11Minutes);
	}
	
	@Test (expected=InexistentTokenException.class)
	public void shouldRejectValidationForInexistentToken() throws DateTimeException, NullTokenException, InexistentTokenException, ExpiredTokenException {
		String newToken = "inexistentToken";
		Integer player = login.getTokenLogin(newToken, currentTimePlus11Minutes);		
	}
}
