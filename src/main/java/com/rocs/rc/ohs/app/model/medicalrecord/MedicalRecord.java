package com.rocs.rc.ohs.app.model.medicalrecord;

/**
 * Represents a medical record of a patient.
 */
public class MedicalRecord {

    /** The unique ID of the medical record. */
    private Long id;

    /** The ID of the patient (person). */
    private Long patientId;

    /** The ID of the consultation. */
    private Long consultationId;

    /** The medical conditions of the patient. */
    private String medicalConditions;

    /** The allergies of the patient. */
    private String allergies;

    /**
     * Retrieves the unique ID of the medical record.
     *
     * @return the unique ID of the medical record.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the medical record.
     *
     * @param newId the unique ID of the medical record.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the ID of the patient (person).
     *
     * @return the ID of the patient (person).
     */
    public Long getPatientId() {
        return patientId;
    }

    /**
     * Sets the ID of the patient (person).
     *
     * @param newPatientId the ID of the patient (person).
     */
    public void setPatientId(Long newPatientId) {
        this.patientId = newPatientId;
    }

    /**
     * Retrieves the ID of the consultation.
     *
     * @return the ID of the consultation.
     */
    public Long getConsultationId() {
        return consultationId;
    }

    /**
     * Sets the ID of the consultation.
     *
     * @param newConsultationId the ID of the consultation.
     */
    public void setConsultationId(Long newConsultationId) {
        this.consultationId = newConsultationId;
    }

    /**
     * Retrieves the medical conditions of the patient.
     *
     * @return the medical conditions of the patient.
     */
    public String getMedicalConditions() {
        return medicalConditions;
    }

    /**
     * Sets the medical conditions of the patient.
     *
     * @param newMedicalConditions the medical conditions of the patient.
     */
    public void setMedicalConditions(String newMedicalConditions) {
        this.medicalConditions = newMedicalConditions;
    }

    /**
     * Retrieves the allergies of the patient.
     *
     * @return the allergies of the patient.
     */
    public String getAllergies() {
        return allergies;
    }

    /**
     * Sets the allergies of the patient.
     *
     * @param newAllergies the allergies of the patient.
     */
    public void setAllergies(String newAllergies) {
        this.allergies = newAllergies;
    }
}
