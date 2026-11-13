package com.rocs.rc.ohs.data.dao.consultation;

import com.rocs.rc.ohs.app.model.consultation.Consultation;

import java.util.List;

/**
 * Defines the data access operations for consultations.
 */
public interface ConsultationDao {

    /**
     * Saves a new consultation so the clinic staff can find it later in
     * the patient's medical records.
     *
     * @param consultation The consultation to save. Its ID must be set.
     * @return The saved {@link Consultation}.
     * @throws IllegalArgumentException If a required value is null.
     */
    Consultation add(Consultation consultation)
            throws IllegalArgumentException;

    /**
     * Retrieves the consultations of a patient, latest first, so they
     * can be shown in the patient's medical records.
     *
     * @param patientId The UUID of the patient.
     * @return A list of consultations of the patient.
     * @throws IllegalArgumentException If the patient ID is null.
     */
    List<Consultation> findByPatientId(String patientId)
            throws IllegalArgumentException;
}
