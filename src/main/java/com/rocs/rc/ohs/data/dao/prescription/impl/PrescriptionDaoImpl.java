package com.rocs.rc.ohs.data.dao.prescription.impl;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.medicine.Medicine;
import com.rocs.rc.ohs.app.model.medicine.MedicineStock;
import com.rocs.rc.ohs.app.model.personnel.Personnel;
import com.rocs.rc.ohs.app.model.prescription.Prescription;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.prescription.PrescriptionDao;
import com.rocs.rc.ohs.data.utils.QueryConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link PrescriptionDao}.
 */
public class PrescriptionDaoImpl implements PrescriptionDao {

    /**
     * Saves a new prescription.
     *
     * @param prescription The prescription to save. Its ID must be set.
     * @return The saved {@link Prescription}.
     * @throws IllegalArgumentException If a required value is null.
     */
    @Override
    public Prescription add(Prescription prescription)
            throws IllegalArgumentException {
        if (prescription == null
                || prescription.getConsultation() == null
                || prescription.getMedicineStock() == null
                || prescription.getPersonnel() == null) {
            throw new IllegalArgumentException(
                    "Consultation, medicine stock and staff are required.");
        }
        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     QueryConstants.ADD_PRESCRIPTION_STATEMENT)) {
            statement.setString(1, prescription.getId());
            statement.setString(2, prescription.getConsultation().getId());
            statement.setString(3, prescription.getMedicineStock().getId());
            statement.setString(4, prescription.getPersonnel().getId());
            statement.setInt(5, prescription.getQuantityToDispense());
            statement.setString(6, prescription.getDose());
            statement.setString(7, prescription.getFrequency());
            statement.setString(8, prescription.getInstructions());
            int rows = statement.executeUpdate();
            if (rows != 1) {
                System.out.println("No prescription row was saved.");
            }
        } catch (SQLException ex) {
            System.out.println(
                    "Could not save prescription: " + ex.getMessage());
        }
        return prescription;
    }

    /**
     * Retrieves all prescriptions issued in a consultation, with the name
     * and unit of each medicine.
     *
     * @param consultationId The UUID of the consultation.
     * @return A list of prescriptions of the consultation.
     * @throws IllegalArgumentException If the consultation ID is null.
     */
    @Override
    public List<Prescription> findByConsultationId(String consultationId)
            throws IllegalArgumentException {
        if (consultationId == null) {
            throw new IllegalArgumentException(
                    "Consultation ID is required.");
        }
        List<Prescription> prescriptions = new ArrayList<>();
        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     QueryConstants
                             .GET_PRESCRIPTIONS_BY_CONSULTATION_STATEMENT)) {
            statement.setString(1, consultationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prescriptions.add(
                            toPrescription(resultSet, consultationId));
                }
            }
        } catch (SQLException ex) {
            System.out.println(
                    "Could not read prescriptions: " + ex.getMessage());
        }
        return prescriptions;
    }

    /**
     * Maps one prescription row to a {@link Prescription}.
     *
     * @param resultSet The query result holding the prescription.
     * @param consultationId The UUID of the consultation.
     * @return The {@link Prescription} built from the row.
     * @throws SQLException If a column cannot be read.
     */
    private Prescription toPrescription(ResultSet resultSet,
                                        String consultationId)
            throws SQLException {
        Consultation consultation = new Consultation();
        consultation.setId(consultationId);
        Medicine medicine = new Medicine();
        medicine.setMedicineName(resultSet.getString("medicine_name"));
        medicine.setUnit(resultSet.getString("unit"));
        MedicineStock medicineStock = new MedicineStock();
        medicineStock.setId(resultSet.getString("medicine_stock_id"));
        medicineStock.setMedicine(medicine);
        Personnel personnel = new Personnel();
        personnel.setId(resultSet.getString("personnel_id"));
        Prescription prescription = new Prescription();
        prescription.setId(resultSet.getString("id"));
        prescription.setConsultation(consultation);
        prescription.setMedicineStock(medicineStock);
        prescription.setPersonnel(personnel);
        prescription.setQuantityToDispense(
                resultSet.getInt("quantity_to_dispense"));
        prescription.setDose(resultSet.getString("dose"));
        prescription.setFrequency(resultSet.getString("frequency"));
        prescription.setInstructions(resultSet.getString("instructions"));
        return prescription;
    }
}
