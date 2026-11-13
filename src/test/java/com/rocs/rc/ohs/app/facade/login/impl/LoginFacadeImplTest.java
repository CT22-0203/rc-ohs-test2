package com.rocs.rc.ohs.app.facade.login.impl;

import com.rocs.rc.ohs.app.model.login.Login;
import com.rocs.rc.ohs.data.dao.login.LoginDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoginFacadeImpl}.
 */
class LoginFacadeImplTest {

    /**
     * The email address used in the tests.
     */
    private static final String EMAIL = "john.doe@example.com";

    /**
     * The username used in the tests.
     */
    private static final String USERNAME = "staff";

    /**
     * The new password used in the tests.
     */
    private static final String NEW_PASSWORD = "NewPass123!";

    /**
     * The mocked data access object of the login accounts.
     */
    private LoginDao loginDao;

    /**
     * The facade being tested.
     */
    private LoginFacadeImpl loginFacade;

    /**
     * Creates the facade with a mocked DAO before each test.
     */
    @BeforeEach
    void setUp() {
        loginDao = Mockito.mock(LoginDao.class);
        loginFacade = new LoginFacadeImpl(loginDao);
    }

    /**
     * Login returns the account found by the DAO,
     * or null when the DAO finds nothing.
     */
    @Test
    void loginReturnsAccountFromDao() {
        Login account = Mockito.mock(Login.class);
        when(loginDao.login(USERNAME)).thenReturn(account);
        when(loginDao.login("unknown")).thenReturn(null);

        assertSame(account, loginFacade.login(USERNAME, "anyPassword"));
        assertNull(loginFacade.login("unknown", "anyPassword"));
    }

    /**
     * Forgot password passes the details to the DAO
     * and returns whatever the DAO reports.
     */
    @Test
    void forgotPasswordReturnsDaoResult() throws Exception {
        when(loginDao.updatePassword(EMAIL, USERNAME,
                NEW_PASSWORD))
                .thenReturn(true);
        assertTrue(loginFacade.forgotPassword(EMAIL, USERNAME,
                NEW_PASSWORD));

        when(loginDao.updatePassword(EMAIL, "other",
                NEW_PASSWORD))
                .thenReturn(false);
        assertFalse(loginFacade.forgotPassword(EMAIL, "other",
                NEW_PASSWORD));
    }

    /**
     * Forgot password returns false for every missing value
     * (null, empty, blank) and never reaches the DAO.
     */
    @Test
    void forgotPasswordReturnsFalseForMissingValues() {
        String[] missing = {null, "", "   "};

        for (String value : missing) {
            assertFalse(loginFacade.forgotPassword(value, USERNAME,
                    NEW_PASSWORD));
            assertFalse(loginFacade.forgotPassword(EMAIL, value,
                    NEW_PASSWORD));
            assertFalse(loginFacade.forgotPassword(EMAIL, USERNAME,
                    value));
        }

        verifyNoInteractions(loginDao);
    }
}

