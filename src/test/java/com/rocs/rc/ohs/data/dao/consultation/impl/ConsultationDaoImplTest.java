package com.rocs.rc.ohs.data.dao.consultation.impl;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.enums.recordtype.RecordType;
import com.rocs.rc.ohs.app.model.person.Person;
import com.rocs.rc.ohs.app.model.personnel.Personnel;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConsultationDaoImpl} class without a real database.
 */
class ConsultationDaoImplTest {

    /** The ID of Jowan Carl Quirona, a patient in the test data. */
    private static final String JOWAN_ID =
            "f04cac48-889d-4a62-9a2b-a274406ece20";

    /** The ID of Antonio Ferrer, a patient in the test data. */
    private static final String ANTONIO_ID =
            "6e00132d-76c0-4c86-82c2-53ba0211aaf0";

    /** The ID of a personnel in the test data. */
    private static final String STAFF_ID =
            "64ffaffe-79b5-4690-bbd4-587507b6859c";

    /** The ID of a consultation in the test data. */
    private static final String CONSULTATION_ID =
            "a451a504-a3d4-4721-bf8d-a18b8c199356";

    /** The date of the consultation in the test cases. */
    private static final LocalDateTime DATE =
            LocalDateTime.of(2026, 10, 20, 9, 30);

    /** The diagnosis of Jowan in the test cases. */
    private static final String DIAGNOSIS = "Bacterial throat infection";

    /** The treatment of Jowan in the test cases. */
    private static final String TREATMENT = "Antibiotic course and rest";

    /** The mocked connection helper. */
    private MockedStatic<ConnectionHelper> connectionHelper;

    /** The mocked connection. */
    private Connection connection;

    /** The mocked prepared statement. */
    private PreparedStatement statement;

    /** The mocked result set. */
    private ResultSet resultSet;

    /** The DAO under test. */
    private ConsultationDaoImpl dao;

    /**
     * Mocks the connection so that no real database is used.
     *
     * @throws SQLException If the mocks cannot be set up.
     */
    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        connectionHelper = mockStatic(ConnectionHelper.class);
        connectionHelper.when(ConnectionHelper::getConnection)
                .thenReturn(connection);

        dao = new ConsultationDaoImpl();
    }

    /**
     * Releases the mocked connection helper.
     */
    @AfterEach
    void tearDown() {
        connectionHelper.close();
    }

    /**
     * The treatment plan is saved as part of the consultation.
     *
     * @throws SQLException If a mock call fails.
     */
    @Test
    void savesTheTreatmentPlan() throws SQLException {
        Consultation consultation = newConsultation(ANTONIO_ID,
                "Mild dehydration", "Drink plenty of water and rest");

        dao.add(connection, consultation);

        verify(statement).setString(2, ANTONIO_ID);
        verify(statement).setString(7, "Mild dehydration");
        verify(statement).setString(8, "Drink plenty of water and rest");
        verify(statement).executeUpdate();
    }

    /**
     * The consultation is saved with the patient, date, diagnosis
     * and treatment plan.
     *
     * @throws SQLException If a mock call fails.
     */
    @Test
    void savesTheConsultationWithAllDetails() throws SQLException {
        Consultation saved = dao.add(connection,
                newConsultation(JOWAN_ID, DIAGNOSIS, TREATMENT));

        assertNotNull(saved.getId());
        assertEquals(36, saved.getId().length());
        verify(statement).setString(1, saved.getId());
        verify(statement).setString(2, JOWAN_ID);
        verify(statement).setString(3, STAFF_ID);
        verify(statement).setString(4, RecordType.CHECK_UP.getValue());
        verify(statement).setTimestamp(5, Timestamp.valueOf(DATE));
        verify(statement).setString(7, DIAGNOSIS);
        verify(statement).setString(8, TREATMENT);
        verify(statement).executeUpdate();
    }

    /**
     * The saved consultation details are retrieved for the patient.
     *
     * @throws SQLException If a mock call fails.
     */
    @Test
    void returnsTheSavedConsultationDetails() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("id")).thenReturn(CONSULTATION_ID);
        when(resultSet.getString("staff_id")).thenReturn(STAFF_ID);
        when(resultSet.getString("record_type"))
                .thenReturn(RecordType.CHECK_UP.getValue());
        when(resultSet.getTimestamp("consultation_date"))
                .thenReturn(Timestamp.valueOf(DATE));
        when(resultSet.getString("diagnosis")).thenReturn(DIAGNOSIS);
        when(resultSet.getString("treatment")).thenReturn(TREATMENT);

        List<Consultation> result = dao.findByPatientId(JOWAN_ID);

        verify(statement).setString(1, JOWAN_ID);
        assertEquals(1, result.size());
        Consultation consultation = result.get(0);
        assertEquals(CONSULTATION_ID, consultation.getId());
        assertEquals(JOWAN_ID, consultation.getPatient().getId());
        assertEquals(DATE, consultation.getConsultationDate());
        assertEquals(DIAGNOSIS, consultation.getDiagnosis());
        assertEquals(TREATMENT, consultation.getTreatment());
    }

    /**
     * Creates a consultation with the given patient, diagnosis and
     * treatment.
     *
     * @param patientId The ID of the patient.
     * @param diagnosis The diagnosis.
     * @param treatment The treatment plan.
     * @return The new consultation.
     */
    private Consultation newConsultation(String patientId, String diagnosis,
                                         String treatment) {
        Person patient = new Person();
        patient.setId(patientId);
        Personnel staff = new Personnel();
        staff.setId(STAFF_ID);
        Consultation consultation = new Consultation();
        consultation.setPatient(patient);
        consultation.setStaff(staff);
        consultation.setRecordType(RecordType.CHECK_UP);
        consultation.setConsultationDate(DATE);
        consultation.setDiagnosis(diagnosis);
        consultation.setTreatment(treatment);
        return consultation;
    }
}
