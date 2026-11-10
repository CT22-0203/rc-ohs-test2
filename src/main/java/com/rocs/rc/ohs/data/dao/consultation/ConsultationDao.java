package com.rocs.rc.ohs.data.dao.consultation;

import com.rocs.rc.ohs.app.model.consultation.Consultation;

import java.sql.Connection;
import java.util.List;

/**
 * Defines the data access operations for consultations.
 */
public interface ConsultationDao {

    /**
     * Saves a new consultation using the given connection, so the caller
     * can save it together with other records in one transaction.
     *
     * @param connection The connection to use. It is not closed here.
     * @param consultation The consultation to save.
     * @return The saved {@link Consultation} with its generated UUID.
     */
    Consultation add(Connection connection, Consultation consultation);

    /**
     * Retrieves the consultations of a patient, latest first.
     *
     * @param patientId The UUID of the patient.
     * @return A list of consultations of the patient.
     */
    List<Consultation> findByPatientId(String patientId);
}
