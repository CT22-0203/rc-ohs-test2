package com.rocs.rc.ohs.app.facade.consultation.impl;

import com.rocs.rc.ohs.app.facade.consultation.ConsultationFacade;
import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.prescription.Prescription;
import com.rocs.rc.ohs.data.dao.consultation.ConsultationDao;
import com.rocs.rc.ohs.data.dao.consultation.impl.ConsultationDaoImpl;
import com.rocs.rc.ohs.data.dao.prescription.PrescriptionDao;
import com.rocs.rc.ohs.data.dao.prescription.impl.PrescriptionDaoImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implements the consultation operations of the clinic system.
 */
public class ConsultationFacadeImpl implements ConsultationFacade {

    /** The data access object of the consultations. */
    private final ConsultationDao consultationDao;

    /** The data access object of the prescriptions. */
    private final PrescriptionDao prescriptionDao;

    /**
     * Creates a facade that uses the default data access objects.
     */
    public ConsultationFacadeImpl() {
        this(new ConsultationDaoImpl(), new PrescriptionDaoImpl());
    }

    /**
     * Creates a facade that uses the given data access objects.
     *
     * @param newConsultationDao The DAO of the consultations.
     * @param newPrescriptionDao The DAO of the prescriptions.
     */
    public ConsultationFacadeImpl(ConsultationDao newConsultationDao,
                                  PrescriptionDao newPrescriptionDao) {
        this.consultationDao = newConsultationDao;
        this.prescriptionDao = newPrescriptionDao;
    }

    /**
     * Saves a consultation together with its prescriptions. Everything is
     * checked first, so nothing is saved when a required value is missing.
     *
     * @param consultation The consultation to save.
     * @param prescriptions The prescriptions of the consultation.
     * @return The saved {@link Consultation} with its generated ID, or
     *         null if a required value is missing, or it could not be
     *         saved.
     */
    @Override
    public Consultation saveConsultation(Consultation consultation,
                                         List<Prescription> prescriptions) {
        List<Prescription> items = prescriptions == null
                ? new ArrayList<>() : prescriptions;
        Consultation saved = null;
        try {
            validateConsultation(consultation);
            for (Prescription prescription : items) {
                validatePrescription(prescription);
            }

            consultation.setId(newId());
            consultationDao.add(consultation);
            for (Prescription prescription : items) {
                prescription.setId(newId());
                prescription.setConsultation(consultation);
                prescription.setPersonnel(consultation.getStaff());
                prescriptionDao.add(prescription);
            }
            saved = consultation;
        } catch (IllegalArgumentException ex) {
            System.out.println("Save consultation failed: " + ex.getMessage());
        }
        return saved;
    }

    /**
     * Retrieves the saved consultations of a patient, latest first.
     *
     * @param patientId The UUID of the patient.
     * @return A list of consultations of the patient, or an empty list
     *         if they could not be read.
     * @throws IllegalArgumentException If the patient ID is missing.
     */
    @Override
    public List<Consultation> getConsultationsByPatient(String patientId)
            throws IllegalArgumentException {
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalArgumentException("The patient ID is required.");
        }
        List<Consultation> consultations = new ArrayList<>();
        try {
            consultations = consultationDao.findByPatientId(patientId);
        } catch (IllegalArgumentException ex) {
            System.out.println(
                    "Read consultations failed: " + ex.getMessage());
        }
        return consultations;
    }

    /**
     * Retrieves the prescriptions of a saved consultation.
     *
     * @param consultationId The UUID of the consultation.
     * @return A list of prescriptions of the consultation, or an empty
     *         list if they could not be read.
     * @throws IllegalArgumentException If the consultation ID is missing.
     */
    @Override
    public List<Prescription> getPrescriptionsByConsultation(
            String consultationId) throws IllegalArgumentException {
        if (consultationId == null || consultationId.isBlank()) {
            throw new IllegalArgumentException(
                    "The consultation ID is required.");
        }
        List<Prescription> prescriptions = new ArrayList<>();
        try {
            prescriptions =
                    prescriptionDao.findByConsultationId(consultationId);
        } catch (IllegalArgumentException ex) {
            System.out.println(
                    "Read prescriptions failed: " + ex.getMessage());
        }
        return prescriptions;
    }

    /**
     * Checks that a consultation has every value the database requires.
     *
     * @param consultation The consultation to check.
     * @throws IllegalArgumentException If the consultation is incomplete.
     */
    private void validateConsultation(Consultation consultation)
            throws IllegalArgumentException {
        if (consultation == null) {
            throw new IllegalArgumentException(
                    "Consultation cannot be null.");
        }

        if (consultation.getPatient() == null
                || consultation.getPatient().getId() == null) {
            throw new IllegalArgumentException("Patient cannot be null.");
        }

        if (consultation.getStaff() == null
                || consultation.getStaff().getId() == null) {
            throw new IllegalArgumentException("Staff cannot be null.");
        }

        if (consultation.getRecordType() == null) {
            throw new IllegalArgumentException(
                    "Record type cannot be null.");
        }

        if (consultation.getConsultationDate() == null) {
            throw new IllegalArgumentException(
                    "Consultation date cannot be null.");
        }

        if (isBlank(consultation.getDiagnosis())) {
            throw new IllegalArgumentException("Diagnosis is required.");
        }

        if (isBlank(consultation.getTreatment())) {
            throw new IllegalArgumentException(
                    "Treatment plan is required.");
        }
    }

    /**
     * Checks that a prescription has every value the database requires.
     *
     * @param prescription The prescription to check.
     * @throws IllegalArgumentException If the prescription is incomplete.
     */
    private void validatePrescription(Prescription prescription)
            throws IllegalArgumentException {
        if (prescription == null) {
            throw new IllegalArgumentException(
                    "Prescription cannot be null.");
        }

        if (prescription.getMedicineStock() == null
                || prescription.getMedicineStock().getId() == null) {
            throw new IllegalArgumentException(
                    "Medicine stock cannot be null.");
        }

        if (isBlank(prescription.getDose())
                || isBlank(prescription.getFrequency())) {
            throw new IllegalArgumentException(
                    "Dose and frequency are required.");
        }

        if (prescription.getQuantityToDispense() <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be above zero.");
        }
    }

    /**
     * Tells whether a text is missing or empty.
     *
     * @param text The text to check.
     * @return True if the text is null or blank.
     */
    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    /**
     * Generates a new UUID for a record.
     *
     * @return The new UUID as text.
     */
    private String newId() {
        return UUID.randomUUID().toString();
    }
}
