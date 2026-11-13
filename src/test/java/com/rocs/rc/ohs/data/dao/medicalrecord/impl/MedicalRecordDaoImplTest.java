package com.rocs.rc.ohs.data.dao.medicalrecord.impl;

import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test the {@link MedicalRecordDaoImpl}.
 */
@ExtendWith(MockitoExtension.class)
public class MedicalRecordDaoImplTest {

    /** mock connection. */
    @Mock
    private Connection connection;

    /** mock statement. */
    @Mock
    private PreparedStatement statement;

    /** The DAO under test. */
    private MedicalRecordDaoImpl medicalRecordDao;

    /**
     * Prepares the DAO and the mocked statement before each test.
     *
     * @throws SQLException if the statement cannot be prepared.
     */
    @BeforeEach
    void setUp()
            throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        medicalRecordDao = new MedicalRecordDaoImpl(connection);
    }

    /**
     * Builds medical record with only its ID and medical condition sets.
     *
     * @param id the ID of the medical record.
     * @param conditions the medical conditions of the patient.
     * @return the medical record.
     */
    private MedicalRecord buildRecord(String id, String conditions) {
        MedicalRecord record = new MedicalRecord();
        record.setId(id);
        record.setMedicalConditions(conditions);
        return record;
    }

    /**
     * TESTCASE1: Should return false if no ID exists.
     *
     * @throws SQLException if the sql query update fails
     */
    @Test
    void updateMedicalRecordWhenIdReturnsNothingReturnsFalse()
            throws SQLException {
        String allisonId = "ea67a306-d343-4847-b204-c92f2d4cc90";
        String jowanConditions = "Hypertension";

        when(statement.executeUpdate()).thenReturn(0);

        boolean result = medicalRecordDao.updateMedicalRecord(
                buildRecord(allisonId, jowanConditions));

        assertFalse(result);
    }

    /**
     * TC2: Returns false if name has no match.
     *
     * @throws SQLException if database connection has error
     */
    @Test
    void updateMedicalRecordWhenNameReturnsNothingReturnsFalse()
            throws SQLException {
        String allisonId = "ea67a306-d343-4847-b204-c92f2d4cc9e0";
        String jowanConditions = "Hypertension";
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = medicalRecordDao.updateMedicalRecord(
                buildRecord(allisonId, jowanConditions));

        assertFalse(result);
    }

    /**
     * TC3: Returns true when update is successful for single ID.
     *
     * @throws SQLException if the update has failed
     */
    @Test
    void updateMedicalRecordWhenIdReturnsSingleResultReturnsTrue()
            throws SQLException {
        String jowanId = "4e49eb71-3481-476b-b6ba-5662a48df1e5";
        String jowanConditions = "Hypertension";
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = medicalRecordDao.updateMedicalRecord(
                buildRecord(jowanId, jowanConditions));

        assertTrue(result);
    }

    /**
     * TC4: Check if update returns true using  name parameter.
     *
     * @throws SQLException if sql params are wrong.
     */
    @Test
    void updateMedicalRecordWhenNameReturnsSingleResultReturnsTrue()
            throws SQLException {
        String antonioId = "669ce09d-4086-4497-a76e-8cb9afe2b635";
        String antonioConditions = "Asthma";
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = medicalRecordDao.updateMedicalRecord(
                buildRecord(antonioId, antonioConditions));

        assertTrue(result);
    }

    /**
     * TC7: Update should work even if chosen from multiple search results.
     *
     * @throws SQLException if table update cannot process data.
     */
    @Test
    void updateMedicalRecordWhenSelectedFromMultipleResultsReturnsTrue()
            throws SQLException {
        String jowanId = "4e49b71-3481-476b-b6ba-5662a48df1e5";
        String jowanConditions = "Hypertension";
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = medicalRecordDao.updateMedicalRecord(
                buildRecord(jowanId, jowanConditions));

        assertTrue(result);
    }

    /**
     * TC9: Verify that corrects values are actually passed to the statement.
     *
     * @throws SQLException if verification drops.
     */
    @Test
    void updateMedicalRecordWhenUpdatedSendsNewValuesToDatabase()
            throws SQLException {
        String jowanId = "4e49eb71-3481-476b-b6ba-5662a48df1e5";
        String jowanConditions = "Hypertension";
        when(statement.executeUpdate()).thenReturn(1);

        medicalRecordDao.updateMedicalRecord(
                buildRecord(jowanId, jowanConditions));

        verify(statement).setString(1, jowanConditions);
        verify(statement).setString(2, null);
        verify(statement).setString(3, jowanId);
        verify(statement).executeUpdate();
    }
}
