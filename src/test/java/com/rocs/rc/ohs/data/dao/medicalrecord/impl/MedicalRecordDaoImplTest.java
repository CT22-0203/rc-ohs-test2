package com.rocs.rc.ohs.data.dao.medicalrecord.impl;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;
import com.rocs.rc.ohs.app.model.person.Person;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/**
 * Tests the {@link MedicalRecordDaoImpl} class.
 */
class MedicalRecordDaoImplTest {

    /** The ID of the medical record used in tests. */
    private static final String RECORD_ID =
            "4e49eb71-3481-476b-b6ba-5662a48df1e5";

    /** The ID of the patient used in tests. */
    private static final String PATIENT_ID =
            "ea67a306-d343-4847-b204-c92f2d4cc9e0";

    /** The ID of the consultation used in tests. */
    private static final String CONSULTATION_ID =
            "a451a504-a3d4-4721-bf8d-a18b8c199356";

    /** Static connection helper used in tests. */
    private MockedStatic<ConnectionHelper> connectionHelper;

    /** Prepared statement for finding medical records. */
    private PreparedStatement findRecords;

    /** Prepared statement for adding a medical record. */
    private PreparedStatement insertRecord;

    /** Result set containing medical record information. */
    private ResultSet recordResult;

    /** DAO instance being tested. */
    private MedicalRecordDaoImpl dao;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = mock(Connection.class);
        findRecords = mock(PreparedStatement.class);
        insertRecord = mock(PreparedStatement.class);
        recordResult = mock(ResultSet.class);

        when(connection.prepareStatement(startsWith("select")))
                .thenReturn(findRecords);
        when(connection.prepareStatement(startsWith("insert")))
                .thenReturn(insertRecord);
        when(findRecords.executeQuery()).thenReturn(recordResult);

        connectionHelper = mockStatic(ConnectionHelper.class);
        connectionHelper.when(ConnectionHelper::getConnection)
                .thenReturn(connection);

        dao = new MedicalRecordDaoImpl();
    }

    @AfterEach
    void tearDown() {
        connectionHelper.close();
    }

    @Test
    void addMedicalRecordStoresRecordsWithAndWithoutConsultation()
            throws SQLException {
        when(insertRecord.executeUpdate()).thenReturn(1, 1);

        MedicalRecord existingRecord =
                newMedicalRecord(RECORD_ID, CONSULTATION_ID);

        assertTrue(dao.addMedicalRecord(existingRecord));

        verify(insertRecord).setString(1, RECORD_ID);
        verify(insertRecord).setString(2, PATIENT_ID);
        verify(insertRecord).setString(3, CONSULTATION_ID);
        verify(insertRecord).setString(4, "Asthma");
        verify(insertRecord).setString(5, "Peanuts");

        MedicalRecord newRecord = newMedicalRecord(null, null);

        assertTrue(dao.addMedicalRecord(newRecord));
        assertNotNull(newRecord.getId());

        verify(insertRecord).setString(1, newRecord.getId());
        verify(insertRecord).setNull(3, Types.VARCHAR);
    }

    @Test
    void addMedicalRecordRejectsInvalidOrUnsuccessfulRecords()
            throws SQLException {
        MedicalRecord noPatient = newMedicalRecord(RECORD_ID, null);
        noPatient.setPatient(null);

        MedicalRecord noPatientId = newMedicalRecord(RECORD_ID, null);
        noPatientId.getPatient().setId(null);

        assertFalse(dao.addMedicalRecord(null));
        assertFalse(dao.addMedicalRecord(noPatient));
        assertFalse(dao.addMedicalRecord(noPatientId));

        connectionHelper.verify(ConnectionHelper::getConnection, never());

        when(insertRecord.executeUpdate()).thenReturn(0);
        assertFalse(dao.addMedicalRecord(
                newMedicalRecord(RECORD_ID, CONSULTATION_ID)));

        doThrow(new SQLException("failed"))
                .when(insertRecord).executeUpdate();

        assertFalse(dao.addMedicalRecord(
                newMedicalRecord(RECORD_ID, CONSULTATION_ID)));
    }

    @Test
    void findByIdHandlesFoundMissingAndFailedQueries()
            throws SQLException {
        when(recordResult.next()).thenReturn(true, true, false);
        stubRow(CONSULTATION_ID);
        when(recordResult.getString("consultation_id"))
                .thenReturn(CONSULTATION_ID, null);

        MedicalRecord withConsultation = dao.findById(RECORD_ID);

        assertNotNull(withConsultation);
        assertEquals(RECORD_ID, withConsultation.getId());
        assertEquals(PATIENT_ID, withConsultation.getPatient().getId());
        assertEquals(CONSULTATION_ID,
                withConsultation.getConsultation().getId());
        assertEquals("Asthma", withConsultation.getMedicalConditions());
        assertEquals("Peanuts", withConsultation.getAllergies());

        verify(findRecords).setString(1, RECORD_ID);

        MedicalRecord withoutConsultation = dao.findById(RECORD_ID);

        assertNotNull(withoutConsultation);
        assertNull(withoutConsultation.getConsultation());

        assertNull(dao.findById("no-such-id"));

        doThrow(new SQLException("failed"))
                .when(findRecords).executeQuery();

        assertNull(dao.findById(RECORD_ID));
    }

    @Test
    void findByPatientIdReturnsRecordsOrAnEmptyList()
            throws SQLException {
        when(recordResult.next()).thenReturn(true, true, false, false);
        stubRow(CONSULTATION_ID);

        List<MedicalRecord> records = dao.findByPatientId(PATIENT_ID);

        assertEquals(2, records.size());
        assertEquals(PATIENT_ID, records.get(0).getPatient().getId());
        assertEquals(PATIENT_ID, records.get(1).getPatient().getId());

        verify(findRecords).setString(1, PATIENT_ID);

        assertTrue(dao.findByPatientId(PATIENT_ID).isEmpty());

        doThrow(new SQLException("failed"))
                .when(findRecords).executeQuery();

        assertTrue(dao.findByPatientId(PATIENT_ID).isEmpty());
    }

    private MedicalRecord newMedicalRecord(
            String id, String consultationId) {
        Person patient = new Person();
        patient.setId(PATIENT_ID);

        MedicalRecord record = new MedicalRecord();
        record.setId(id);
        record.setPatient(patient);

        if (consultationId != null) {
            Consultation consultation = new Consultation();
            consultation.setId(consultationId);
            record.setConsultation(consultation);
        }

        record.setMedicalConditions("Asthma");
        record.setAllergies("Peanuts");

        return record;
    }

    private void stubRow(String consultationId) throws SQLException {
        when(recordResult.getString("id")).thenReturn(RECORD_ID);
        when(recordResult.getString("patient_id")).thenReturn(PATIENT_ID);
        when(recordResult.getString("consultation_id"))
                .thenReturn(consultationId);
        when(recordResult.getString("medical_conditions"))
                .thenReturn("Asthma");
        when(recordResult.getString("allergies")).thenReturn("Peanuts");
    }
}
