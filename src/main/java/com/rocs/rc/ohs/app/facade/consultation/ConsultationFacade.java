package com.rocs.rc.ohs.app.facade.consultation;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.prescription.Prescription;

import java.util.List;

/**
 * Defines the consultation operations of the clinic system.
 */
public interface ConsultationFacade {

    /**
     * Saves a consultation together with its prescriptions. Everything is
     * checked first, so nothing is saved when a required value is missing.
     *
     * @param consultation The consultation to save.
     * @param prescriptions The prescriptions of the consultation.
     * @return The saved {@link Consultation} with its generated ID, or
     *         null if a required value is missing or it could not be
     *         saved.
     */
    Consultation saveConsultation(Consultation consultation,
                                  List<Prescription> prescriptions);

    /**
     * Retrieves the saved consultations of a patient, latest first.
     *
     * @param patientId The UUID of the patient.
     * @return A list of consultations of the patient, or an empty list
     *         if they could not be read.
     * @throws IllegalArgumentException If the patient ID is missing.
     */
    List<Consultation> getConsultationsByPatient(String patientId)
            throws IllegalArgumentException;

    /**
     * Retrieves the prescriptions of a saved consultation.
     *
     * @param consultationId The UUID of the consultation.
     * @return A list of prescriptions of the consultation, or an empty
     *         list if they could not be read.
     * @throws IllegalArgumentException If the consultation ID is missing.
     */
    List<Prescription> getPrescriptionsByConsultation(
            String consultationId) throws IllegalArgumentException;
}
