package com.rocs.rc.ohs.data.dao.prescription.impl;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.medicine.MedicineStock;
import com.rocs.rc.ohs.app.model.personnel.Personnel;
import com.rocs.rc.ohs.app.model.prescription.Prescription;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PrescriptionDaoImpl} class without a real database.
 */
class PrescriptionDaoImplTest {

    /** The ID of a consultation in the test data. */
    private static final String CONSULTATION_ID =
            "a451a504-a3d4-4721-bf8d-a18b8c199356";

    /** The ID given to a new prescription, as the facade would do. */
    private static final String NEW_ID =
            "9f4e6c1a-2b7d-4c58-8a31-5d0e7b9c2f14";

    /** The ID of the Amoxicillin 500mg stock in the test data. */
    private static final String STOCK_ID =
            "70aaf305-a7f5-41f1-bd0a-6ad73c042be9";

    /** The ID of a personnel in the test data. */
    private static final String PERSONNEL_ID =
            "64ffaffe-79b5-4690-bbd4-587507b6859c";

    /** The mocked connection helper. */
    private MockedStatic<ConnectionHelper> connectionHelper;

    /** The mocked connection. */
    private Connection connection;

    /** The mocked prepared statement. */
    private PreparedStatement statement;

    /** The mocked result set. */
    private ResultSet resultSet;

    /** The DAO under test. */
    private PrescriptionDaoImpl dao;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(statement.executeUpdate()).thenReturn(1);

        connectionHelper = mockStatic(ConnectionHelper.class);
        connectionHelper.when(ConnectionHelper::getConnection)
                .thenReturn(connection);

        dao = new PrescriptionDaoImpl();
    }

    @AfterEach
    void tearDown() {
        connectionHelper.close();
    }

    // The prescription is recorded under the consultation.
    @Test
    void savesThePrescriptionUnderTheConsultation()
            throws SQLException {
        Prescription saved = dao.add(newPrescription());

        assertEquals(NEW_ID, saved.getId());
        verify(statement).setString(1, NEW_ID);
        verify(statement).setString(2, CONSULTATION_ID);
        verify(statement).setString(3, STOCK_ID);
        verify(statement).setString(4, PERSONNEL_ID);
        verify(statement).setInt(5, 2);
        verify(statement).setString(6, "500mg");
        verify(statement).setString(7, "3 times a day");
        verify(statement).executeUpdate();
    }

    // A prescription without a consultation is rejected, not saved.
    @Test
    void rejectsAPrescriptionWithoutConsultation() {
        Prescription prescription = newPrescription();
        prescription.setConsultation(null);

        assertThrows(IllegalArgumentException.class,
                () -> dao.add(prescription));
    }

    // The saved prescription details are retrieved for the consultation.
    @Test
    void returnsTheSavedPrescriptionDetails() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("medicine_stock_id")).thenReturn(STOCK_ID);
        when(resultSet.getString("personnel_id")).thenReturn(PERSONNEL_ID);
        when(resultSet.getInt("quantity_to_dispense")).thenReturn(2);
        when(resultSet.getString("dose")).thenReturn("500mg");
        when(resultSet.getString("frequency")).thenReturn("3 times a day");
        when(resultSet.getString("medicine_name"))
                .thenReturn("Amoxicillin 500mg");

        List<Prescription> result =
                dao.findByConsultationId(CONSULTATION_ID);

        verify(statement).setString(1, CONSULTATION_ID);
        assertEquals(1, result.size());
        Prescription prescription = result.get(0);
        assertEquals(STOCK_ID, prescription.getMedicineStock().getId());
        assertEquals("Amoxicillin 500mg", prescription.getMedicineStock()
                .getMedicine().getMedicineName());
        assertEquals("500mg", prescription.getDose());
        assertEquals("3 times a day", prescription.getFrequency());
        assertEquals(2, prescription.getQuantityToDispense());
    }

    private Prescription newPrescription() {
        Consultation consultation = new Consultation();
        consultation.setId(CONSULTATION_ID);
        MedicineStock stock = new MedicineStock();
        stock.setId(STOCK_ID);
        Personnel personnel = new Personnel();
        personnel.setId(PERSONNEL_ID);
        Prescription prescription = new Prescription();
        prescription.setId(NEW_ID);
        prescription.setConsultation(consultation);
        prescription.setMedicineStock(stock);
        prescription.setPersonnel(personnel);
        prescription.setQuantityToDispense(2);
        prescription.setDose("500mg");
        prescription.setFrequency("3 times a day");
        return prescription;
    }
}
