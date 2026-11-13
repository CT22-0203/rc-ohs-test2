package com.rocs.rc.ohs.data.dao.prescription;

import com.rocs.rc.ohs.app.model.prescription.Prescription;

import java.util.List;

/**
 * Defines the data access operations for prescriptions.
 */
public interface PrescriptionDao {

    /**
     * Saves a new prescription of a consultation.
     *
     * @param prescription The prescription to save. Its ID must be set.
     * @return The saved {@link Prescription}.
     * @throws IllegalArgumentException If a required value is null.
     */
    Prescription add(Prescription prescription)
            throws IllegalArgumentException;

    /**
     * Retrieves all prescriptions issued in a consultation, with the name
     * and unit of each medicine, so they can be shown in the patient's
     * medical records.
     *
     * @param consultationId The UUID of the consultation.
     * @return A list of prescriptions of the consultation.
     * @throws IllegalArgumentException If the consultation ID is null.
     */
    List<Prescription> findByConsultationId(String consultationId)
            throws IllegalArgumentException;
}
