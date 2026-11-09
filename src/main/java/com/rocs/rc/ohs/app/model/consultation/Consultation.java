package com.rocs.rc.ohs.app.model.consultation;

import com.rocs.rc.ohs.app.model.enums.recordtype.RecordType;
import com.rocs.rc.ohs.app.model.person.Person;
import com.rocs.rc.ohs.app.model.personnel.Personnel;
import java.time.LocalDateTime;

/**
 * Represents a consultation of a patient in the clinic.
 */
public class Consultation {

    /** The unique ID of the consultation. */
    private String id;

    /** The patient associated with the consultation. */
    private Person patient;

    /** The personnel who recorded the consultation. */
    private Personnel staff;

    /** The record type of the consultation. */
    private RecordType recordType;

    /** The date and time of the consultation. */
    private LocalDateTime consultationDate;

    /** The complaint of the patient. */
    private String complaint;

    /** The diagnosis of the consultation. */
    private String diagnosis;

    /** The treatment given to the patient. */
    private String treatment;

    /** The action taken during the consultation. */
    private String actionTaken;

    /** The additional remarks about the consultation. */
    private String remarks;

    /**
     * Retrieves the unique ID of the consultation.
     *
     * @return the unique ID of the consultation.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the consultation.
     *
     * @param newId the unique ID of the consultation.
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Retrieves the patient associated with the consultation.
     *
     * @return the patient associated with the consultation.
     */
    public Person getPatient() {
        return patient;
    }

    /**
     * Sets the patient associated with the consultation.
     *
     * @param newPatient the patient associated with the consultation.
     */
    public void setPatient(Person newPatient) {
        this.patient = newPatient;
    }

    /**
     * Retrieves the personnel who recorded the consultation.
     *
     * @return the personnel who recorded the consultation.
     */
    public Personnel getStaff() {
        return staff;
    }

    /**
     * Sets the personnel who recorded the consultation.
     *
     * @param newStaff the personnel who recorded the consultation.
     */
    public void setStaff(Personnel newStaff) {
        this.staff = newStaff;
    }

    /**
     * Retrieves the record type of the consultation.
     *
     * @return the record type of the consultation.
     */
    public RecordType getRecordType() {
        return recordType;
    }

    /**
     * Sets the record type of the consultation.
     *
     * @param newRecordType the record type of the consultation.
     */
    public void setRecordType(RecordType newRecordType) {
        this.recordType = newRecordType;
    }

    /**
     * Retrieves the date and time of the consultation.
     *
     * @return the date and time of the consultation.
     */
    public LocalDateTime getConsultationDate() {
        return consultationDate;
    }

    /**
     * Sets the date and time of the consultation.
     *
     * @param newConsultationDate the date and time of the consultation.
     */
    public void setConsultationDate(LocalDateTime newConsultationDate) {
        this.consultationDate = newConsultationDate;
    }

    /**
     * Retrieves the complaint of the patient.
     *
     * @return the complaint of the patient.
     */
    public String getComplaint() {
        return complaint;
    }

    /**
     * Sets the complaint of the patient.
     *
     * @param newComplaint the complaint of the patient.
     */
    public void setComplaint(String newComplaint) {
        this.complaint = newComplaint;
    }

    /**
     * Retrieves the diagnosis of the consultation.
     *
     * @return the diagnosis of the consultation.
     */
    public String getDiagnosis() {
        return diagnosis;
    }

    /**
     * Sets the diagnosis of the consultation.
     *
     * @param newDiagnosis the diagnosis of the consultation.
     */
    public void setDiagnosis(String newDiagnosis) {
        this.diagnosis = newDiagnosis;
    }

    /**
     * Retrieves the treatment given to the patient.
     *
     * @return the treatment given to the patient.
     */
    public String getTreatment() {
        return treatment;
    }

    /**
     * Sets the treatment given to the patient.
     *
     * @param newTreatment the treatment given to the patient.
     */
    public void setTreatment(String newTreatment) {
        this.treatment = newTreatment;
    }

    /**
     * Retrieves the action taken during the consultation.
     *
     * @return the action taken during the consultation.
     */
    public String getActionTaken() {
        return actionTaken;
    }

    /**
     * Sets the action taken during the consultation.
     *
     * @param newActionTaken the action taken during the consultation.
     */
    public void setActionTaken(String newActionTaken) {
        this.actionTaken = newActionTaken;
    }

    /**
     * Retrieves the additional remarks about the consultation.
     *
     * @return the additional remarks about the consultation.
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Sets the additional remarks about the consultation.
     *
     * @param newRemarks the additional remarks about the consultation.
     */
    public void setRemarks(String newRemarks) {
        this.remarks = newRemarks;
    }
}
