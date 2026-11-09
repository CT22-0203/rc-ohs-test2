package com.rocs.rc.ohs.data.dao.filter.impl;


import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the filtering of medical records in {@link FilterDaoImpl}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FilterDaoImplTest {

    /** The mocked database connection. */
    @Mock
    private Connection connection;

    /** The mocked prepared statement. */
    @Mock
    private PreparedStatement statement;

    /** The mocked result set. */
    @Mock
    private ResultSet resultSet;

    /** The mocked static connection helper. */
    private MockedStatic<ConnectionHelper> connectionHelper;

    /** The data access object under test. */
    private FilterDaoImpl filterDao;

    /**
     * Prepares the mocks before each test.
     *
     * @throws SQLException if a mock cannot be prepared.
     */
    @BeforeEach
    void setUp() throws SQLException {
        connectionHelper = mockStatic(ConnectionHelper.class);
        connectionHelper.when(ConnectionHelper::getConnection)
                .thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        filterDao = new FilterDaoImpl();
    }

    /**
     * Releases the static mock after each test.
     */
    @AfterEach
    void tearDown() {
        connectionHelper.close();
    }

    /**
     * Returns the SQL that was sent to the database.
     *
     * @return the captured SQL.
     * @throws SQLException if the SQL cannot be captured.
     */
    private String capturedSql() throws SQLException {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(connection).prepareStatement(captor.capture());
        return captor.getValue();
    }

    /**
     * No criteria is given, so no filter is added and nothing is bound.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsWithNoCriteriaAppliesNoFilter()
            throws SQLException {
        filterDao.filterMedicalRecords(null, null, null);

        assertFalse(capturedSql().contains("like ?"));
        verify(statement, never()).setString(anyInt(), anyString());
    }

    /**
     * Blank criteria are ignored just like null criteria.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsIgnoresBlankCriteria() throws SQLException {
        filterDao.filterMedicalRecords("", "   ", "");

        assertFalse(capturedSql().contains("like ?"));
        verify(statement, never()).setString(anyInt(), anyString());
    }

    /**
     * The patient name matches the first name or the last name.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsByPatientNameChecksFirstAndLastName()
            throws SQLException {
        filterDao.filterMedicalRecords("jowan", null, null);

        String sql = capturedSql();
        assertTrue(sql.contains("lower(p.first_name) like ?"));
        assertTrue(sql.contains("lower(p.last_name) like ?"));
        verify(statement).setString(1, "%jowan%");
        verify(statement).setString(2, "%jowan%");
    }

    /**
     * The medical condition is matched against the medical conditions column.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsByMedicalConditionOnly() throws SQLException {
        filterDao.filterMedicalRecords(null, "asthma", null);

        assertTrue(capturedSql()
                .contains("lower(m.medical_conditions) like ?"));
        verify(statement).setString(1, "%asthma%");
    }

    /**
     * The allergy is matched against the allergies column.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsByAllergyOnly() throws SQLException {
        filterDao.filterMedicalRecords(null, null, "penicillin");

        assertTrue(capturedSql().contains("lower(m.allergies) like ?"));
        verify(statement).setString(1, "%penicillin%");
    }

    /**
     * Multiple criteria are all applied and bound in the right order.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsWithAllCriteriaAppliesAllFilters()
            throws SQLException {
        filterDao.filterMedicalRecords("jowan", "asthma", "penicillin");

        String sql = capturedSql();
        assertTrue(sql.contains("lower(p.first_name) like ?"));
        assertTrue(sql.contains("lower(m.medical_conditions) like ?"));
        assertTrue(sql.contains("lower(m.allergies) like ?"));
        verify(statement).setString(1, "%jowan%");
        verify(statement).setString(2, "%jowan%");
        verify(statement).setString(3, "%asthma%");
        verify(statement).setString(4, "%penicillin%");
    }

    /**
     * Criteria are trimmed and converted to lowercase before searching.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsTrimsAndLowercasesCriteria()
            throws SQLException {
        filterDao.filterMedicalRecords(null, "  AsThMa  ", null);

        verify(statement).setString(1, "%asthma%");
    }

    /**
     * A filter with no matching rows returns an empty list.
     */
    @Test
    void filterMedicalRecordsWithNoMatchReturnsEmptyList() {
        List<MedicalRecord> records =
                filterDao.filterMedicalRecords("nobody", null, null);

        assertTrue(records.isEmpty());
    }

    /**
     * A matching row is mapped to a medical record with its patient.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsMapsRowToMedicalRecord() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("id")).thenReturn("record-1");
        when(resultSet.getString("patient_id")).thenReturn("patient-1");
        when(resultSet.getString("first_name")).thenReturn("Jowan");
        when(resultSet.getString("middle_name")).thenReturn("Carl");
        when(resultSet.getString("last_name")).thenReturn("Quirona");
        when(resultSet.getString("medical_conditions")).thenReturn("Asthma");
        when(resultSet.getString("allergies")).thenReturn(null);

        List<MedicalRecord> records =
                filterDao.filterMedicalRecords("jowan", null, null);

        assertEquals(1, records.size());
        MedicalRecord record = records.get(0);
        assertEquals("record-1", record.getId());
        assertEquals("patient-1", record.getPatient().getId());
        assertEquals("Jowan", record.getPatient().getFirstName());
        assertEquals("Carl", record.getPatient().getMiddleName());
        assertEquals("Quirona", record.getPatient().getLastName());
        assertEquals("Asthma", record.getMedicalConditions());
        assertEquals(null, record.getAllergies());
    }

    /**
     * Multiple matching rows are all returned.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsReturnsEveryMatchingRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        List<MedicalRecord> records =
                filterDao.filterMedicalRecords(null, null, null);

        assertEquals(2, records.size());
    }

    /**
     * A database error returns an empty list instead of throwing.
     *
     * @throws SQLException if a mock fails.
     */
    @Test
    void filterMedicalRecordsReturnsEmptyListOnSqlException()
            throws SQLException {
        when(connection.prepareStatement(anyString()))
                .thenThrow(new SQLException("boom"));

        List<MedicalRecord> records =
                filterDao.filterMedicalRecords("jowan", null, null);

        assertTrue(records.isEmpty());
    }
}
