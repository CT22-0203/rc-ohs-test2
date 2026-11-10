package com.rocs.rc.ohs.data.dao.consultation.impl;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.enums.recordtype.RecordType;
import com.rocs.rc.ohs.app.model.person.Person;
import com.rocs.rc.ohs.app.model.personnel.Personnel;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.consultation.ConsultationDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements the data access operations for consultations.
 */
public class ConsultationDaoImpl implements ConsultationDao {

    /** The SQL statement that saves a consultation. */
    private static final String INSERT_SQL =
            "INSERT INTO consultation (id, patient_id, staff_id, "
                    + "record_type, consultation_date, complaint, diagnosis, "
                    + "treatment, action_taken, remarks) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /** The SQL statement that retrieves the consultations of a patient. */
    private static final String FIND_BY_PATIENT_SQL =
            "SELECT id, staff_id, record_type, consultation_date, "
                    + "complaint, diagnosis, treatment, action_taken, remarks "
                    + "FROM consultation WHERE patient_id = ? "
                    + "ORDER BY consultation_date DESC";

    /**
     * Saves a new consultation using the given connection.
     *
     * @param connection The connection to use. It is not closed here.
     * @param consultation The consultation to save.
     * @return The saved {@link Consultation} with its generated UUID.
     */
    @Override
    public Consultation add(Connection connection,
                            Consultation consultation) {
        String id = UUID.randomUUID().toString();
        try (PreparedStatement statement =
                     connection.prepareStatement(INSERT_SQL)) {
            Timestamp date =
                    Timestamp.valueOf(consultation.getConsultationDate());
            statement.setString(1, id);
            statement.setString(2, consultation.getPatient().getId());
            statement.setString(3, consultation.getStaff().getId());
            statement.setString(4, consultation.getRecordType().getValue());
            statement.setTimestamp(5, date);
            statement.setString(6, consultation.getComplaint());
            statement.setString(7, consultation.getDiagnosis());
            statement.setString(8, consultation.getTreatment());
            statement.setString(9, consultation.getActionTaken());
            statement.setString(10, consultation.getRemarks());
            statement.executeUpdate();
            consultation.setId(id);
            return consultation;
        } catch (SQLException ex) {
            throw new RuntimeException("Error saving the consultation.", ex);
        }
    }

    /**
     * Retrieves the consultations of a patient, latest first.
     *
     * @param patientId The UUID of the patient.
     * @return A list of consultations of the patient.
     */
    @Override
    public List<Consultation> findByPatientId(String patientId) {
        List<Consultation> consultations = new ArrayList<>();
        try (Connection connection = ConnectionHelper.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(FIND_BY_PATIENT_SQL)) {
            statement.setString(1, patientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    consultations.add(toConsultation(resultSet, patientId));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(
                    "Error retrieving the consultations.", ex);
        }
        return consultations;
    }

    /**
     * Builds a consultation from the current row of the result set.
     *
     * @param resultSet The result set positioned on a consultation row.
     * @param patientId The UUID of the patient.
     * @return The {@link Consultation} built from the row.
     * @throws SQLException If a column cannot be read.
     */
    private Consultation toConsultation(ResultSet resultSet, String patientId)
            throws SQLException {
        Person patient = new Person();
        patient.setId(patientId);
        Personnel staff = new Personnel();
        staff.setId(resultSet.getString("staff_id"));
        Consultation consultation = new Consultation();
        consultation.setId(resultSet.getString("id"));
        consultation.setPatient(patient);
        consultation.setStaff(staff);
        consultation.setRecordType(
                toRecordType(resultSet.getString("record_type")));
        consultation.setConsultationDate(
                resultSet.getTimestamp("consultation_date").toLocalDateTime());
        consultation.setComplaint(resultSet.getString("complaint"));
        consultation.setDiagnosis(resultSet.getString("diagnosis"));
        consultation.setTreatment(resultSet.getString("treatment"));
        consultation.setActionTaken(resultSet.getString("action_taken"));
        consultation.setRemarks(resultSet.getString("remarks"));
        return consultation;
    }

    /**
     * Converts a database value to its record type.
     *
     * @param value The database value of the record type.
     * @return The matching {@link RecordType}.
     */
    private RecordType toRecordType(String value) {
        for (RecordType type : RecordType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalStateException("Unknown record type: " + value);
    }
}
