package com.rocs.rc.ohs.app.model.medicalrecord;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.person.Person;

/**
 * Represents the medical record of a patient.
 */
public class MedicalRecord {

    /** The unique ID of the medical record. */
    private String id;

    /** The patient associated with the medical record. */
    private Person patient;

    /** The consultation associated with the medical record. */
    private Consultation consultation;

    /** The medical conditions of the patient. */
    private String medicalConditions;

    /** The allergies of the patient. */
    private String allergies;

    /**
     * Retrieves the unique ID of the medical record.
     *
     * @return the unique ID of the medical record.
     */
    public String getId() {
        return id;
    }


    /**
     * Sets the unique ID of the medical record.
     *
     * @param newId the unique ID of the medical record.
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Retrieves the patient associated with the medical record.
     *
     * @return the patient associated with the medical record.
     */
    public Person getPatient() {
        return patient;
    }

    /**
     * Sets the patient associated with the medical record.
     *
     * @param newPatient the patient associated with the medical record.
     */
    public void setPatient(Person newPatient) {
        this.patient = newPatient;
    }

    /**
     * Retrieves the consultation associated with the medical record.
     *
     * @return the consultation associated with the medical record.
     */
    public Consultation getConsultation() {
        return consultation;
    }

    /**
     * Sets the consultation associated with the medical record.
     *
     * @param newConsultation the consultation associated with the
     *                        medical record.
     */
    public void setConsultation(Consultation newConsultation) {
        this.consultation = newConsultation;
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
