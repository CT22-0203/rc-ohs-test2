package com.rocs.rc.ohs.data.dao.forgotpassword.impl;

import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.startsWith;

/**
 * Tests the {@link ForgotPasswordDaoImpl} class.
 */
class ForgotPasswordDaoImplTest {

    /** Static connection helper used in tests. */
    private MockedStatic<ConnectionHelper> connectionHelper;

    /** Prepared statement for finding an account. */
    private PreparedStatement findAccount;

    /** Prepared statement for updating the password. */
    private PreparedStatement updatePassword;

    /** Result set containing account information. */
    private ResultSet accountResult;

    /** DAO instance being tested. */
    private ForgotPasswordDaoImpl dao;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = mock(Connection.class);
        findAccount = mock(PreparedStatement.class);
        updatePassword = mock(PreparedStatement.class);
        accountResult = mock(ResultSet.class);

        when(connection.prepareStatement(startsWith("select")))
                .thenReturn(findAccount);
        when(connection.prepareStatement(startsWith("update login")))
                .thenReturn(updatePassword);
        when(findAccount.executeQuery()).thenReturn(accountResult);

        connectionHelper = mockStatic(ConnectionHelper.class);
        connectionHelper.when(ConnectionHelper::getConnection)
                .thenReturn(connection);

        dao = new ForgotPasswordDaoImpl();
    }

    @AfterEach
    void tearDown() {
        connectionHelper.close();
    }

    // AC 2: registered Staff ID or email, so the request can proceed
    @Test
    void isRegisteredReturnsTrueWhenAccountExists() throws SQLException {
        when(accountResult.next()).thenReturn(true);

        assertTrue(dao.isRegistered(" EMP-001 "));
        verify(findAccount).setString(1, "EMP-001");
        verify(findAccount).setString(2, "EMP-001");
    }

    // AC 3: not registered, so the password cannot be reset
    @Test
    void isRegisteredReturnsFalseWhenNotRegistered() throws SQLException {
        when(accountResult.next()).thenReturn(false);

        assertFalse(dao.isRegistered("EMP-999"));
    }

    // AC 4: empty Staff ID or email
    @Test
    void isRegisteredReturnsFalseWithoutConnectingWhenBlank() {
        assertFalse(dao.isRegistered(null));
        assertFalse(dao.isRegistered("   "));

        connectionHelper.verify(ConnectionHelper::getConnection, never());
    }

    // AC 5: identity verified (details match), so the password is updated
    @Test
    void updatePasswordReturnsTrueWhenDetailsMatch() throws SQLException {
        when(updatePassword.executeUpdate()).thenReturn(1);

        assertTrue(dao.updatePassword("EMP-001", "ana.corpuz", "NewPass123"));
        verify(updatePassword).setString(1, "NewPass123");
        verify(updatePassword).setString(2, "ana.corpuz");
    }

    // AC 5: identity not verified, so nothing is updated
    @Test
    void updatePasswordReturnsFalseWhenDetailsDoNotMatch()
            throws SQLException {
        when(updatePassword.executeUpdate()).thenReturn(0);

        assertFalse(dao.updatePassword(
                "EMP-001", "someone.else", "NewPass123"));
    }
}
