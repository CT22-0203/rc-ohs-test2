package com.rocs.rc.ohs.data.dao.medicalrecord.impl;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;
import com.rocs.rc.ohs.app.model.person.Person;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.medicalrecord.MedicalRecordDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements the data access operations for medical records.
 */
public class MedicalRecordDaoImpl implements MedicalRecordDao {

    /** The columns selected for a medical record. */
    private static final String COLUMNS =
            "select id, patient_id, consultation_id, medical_conditions, "
                    + "allergies from medical_record ";

    /** The SQL statement that selects a medical record by ID. */
    private static final String SELECT_BY_ID_SQL =
            COLUMNS + "where id = ?";

    /** The SQL statement that selects the medical records of a patient. */
    private static final String SELECT_BY_PATIENT_SQL =
            COLUMNS + "where patient_id = ?";

    /** The SQL statement that inserts a medical record. */
    private static final String INSERT_SQL =
            "insert into medical_record (id, patient_id, consultation_id, "
                    + "medical_conditions, allergies) values (?, ?, ?, ?, ?)";

    /**
     * Retrieves a medical record by ID.
     *
     * @param id the ID of the medical record.
     * @return the medical record if found, otherwise null.
     */
    @Override
    public MedicalRecord findById(String id) {

        MedicalRecord medicalRecord = null;
        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    medicalRecord = toMedicalRecord(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println(
                    "An exception was thrown while finding a medical record. "
                            + e.getMessage());
        }
        return medicalRecord;
    }

    /**
     * Retrieves all medical records of a patient.
     *
     * @param patientId the ID of the patient.
     * @return the medical records of the patient, or an empty list if
     *         the patient has none.
     */
    @Override
    public List<MedicalRecord> findByPatientId(String patientId) {

        List<MedicalRecord> medicalRecords = new ArrayList<>();
        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(SELECT_BY_PATIENT_SQL)) {

            stmt.setString(1, patientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicalRecords.add(toMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println(
                    "An exception was thrown while finding medical records. "
                            + e.getMessage());
        }
        return medicalRecords;
    }

    /**
     * Adds a medical record. A unique ID is generated when the medical
     * record does not have one yet.
     *
     * @param medicalRecord the medical record to add.
     * @return true if successful, otherwise false.
     */
    @Override
    public boolean addMedicalRecord(MedicalRecord medicalRecord) {

        if (medicalRecord == null || medicalRecord.getPatient() == null
                || medicalRecord.getPatient().getId() == null) {
            return false;
        }

        if (medicalRecord.getId() == null) {
            medicalRecord.setId(UUID.randomUUID().toString());
        }

        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            int index = 1;

            stmt.setString(index, medicalRecord.getId());
            index++;

            stmt.setString(index, medicalRecord.getPatient().getId());
            index++;

            if (medicalRecord.getConsultation() == null) {
                stmt.setNull(index, Types.VARCHAR);
            } else {
                stmt.setString(index,
                        medicalRecord.getConsultation().getId());
            }
            index++;

            stmt.setString(index, medicalRecord.getMedicalConditions());
            index++;

            stmt.setString(index, medicalRecord.getAllergies());

            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            System.out.println(
                    "An exception was thrown while adding a medical record. "
                            + e.getMessage());
            return false;
        }
    }

    /**
     * Maps the current row of the result set to a medical record.
     *
     * @param rs the result set positioned at a medical record row.
     * @return the mapped medical record.
     * @throws SQLException if a column cannot be read.
     */
    private MedicalRecord toMedicalRecord(ResultSet rs) throws SQLException {

        MedicalRecord medicalRecord = new MedicalRecord();
        medicalRecord.setId(rs.getString("id"));

        Person patient = new Person();
        patient.setId(rs.getString("patient_id"));
        medicalRecord.setPatient(patient);

        String consultationId = rs.getString("consultation_id");
        if (consultationId != null) {
            Consultation consultation = new Consultation();
            consultation.setId(consultationId);
            medicalRecord.setConsultation(consultation);
        }

        medicalRecord.setMedicalConditions(
                rs.getString("medical_conditions"));
        medicalRecord.setAllergies(rs.getString("allergies"));
        return medicalRecord;
    }
}
