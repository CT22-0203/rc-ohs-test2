package com.rocs.rc.ohs.data.dao.login.impl;

import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoginDaoImpl} class.
 */
class LoginDaoImplTest {

    /** DAO instance being tested. */
    private final LoginDaoImpl dao = new LoginDaoImpl();


    /**
     * Creates a mocked prepared statement for the update password query
     * and links it to the given connection.
     *
     * @param connection the connection that should return the mocked statement
     * @return the mocked prepared statement
     * @throws SQLException if stubbing the connection fails
     */
    private PreparedStatement mockUpdatePassword(Connection connection)
            throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(
                startsWith("update login set password")))
                .thenReturn(statement);
        return statement;
    }

    /**
     * Mocks the static ConnectionHelper so that it returns the given
     * connection.
     *
     * @param connection the connection that ConnectionHelper should return
     * @return the static mock, which must be closed at the end of the test
     */
    private MockedStatic<ConnectionHelper> mockConnectionHelper(
            Connection connection) {
        MockedStatic<ConnectionHelper> helper =
                mockStatic(ConnectionHelper.class);
        helper.when(ConnectionHelper::getConnection)
                .thenReturn(connection);
        return helper;
    }

    //Empty email.
    @Test
    void updatePasswordReturnsFalseWithoutConnectingWhenBlank() {
        Connection connection = mock(Connection.class);

        try (MockedStatic<ConnectionHelper> helper =
                     mockConnectionHelper(connection)) {
            assertFalse(dao.updatePassword(null,
                    "staff", "NewStaff@2026"));
            assertFalse(dao.updatePassword(" ", "staff",
                    "NewStaff@2026"));

            helper.verify(ConnectionHelper::getConnection, never());
        }
    }

    //Identity verified (details match), so the password is updated.
    @Test
    void updatePasswordReturnsTrueWhenDetailsMatch() throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement updatePassword = mockUpdatePassword(connection);
        when(updatePassword.executeUpdate()).thenReturn(1);

        try (MockedStatic<ConnectionHelper> helper =
                     mockConnectionHelper(connection)) {
            assertTrue(dao.updatePassword("john.doe@example.com",
                    "staff", "NewStaff@2026"));
        }
        verify(updatePassword).setString(1, "NewStaff@2026");
        verify(updatePassword).setString(2, "staff");
        verify(updatePassword).setString(3, "john.doe@example.com");
    }

    //Identity not verified, so nothing is updated.
    @Test
    void updatePasswordReturnsFalseWhenDetailsDoNotMatch()
            throws SQLException {
        Connection connection = mock(Connection.class);
        PreparedStatement updatePassword = mockUpdatePassword(connection);
        when(updatePassword.executeUpdate()).thenReturn(0);

        try (MockedStatic<ConnectionHelper> helper =
                     mockConnectionHelper(connection)) {
            assertFalse(dao.updatePassword(
                    "john.doe@example.com", "staff",
                    "NewStaff@2026"));
        }
    }
}
