package com.rocs.rc.ohs.app.model.consultation;

import java.time.LocalDateTime;

/**
 * Represents a consultation of a patient in the clinic.
 */
public class Consultation {

    /** The unique ID of the consultation. */
    private Long id;

    /** The ID of the patient (person) consulted. */
    private Long patientId;

    /** The ID of the personnel who recorded it. */
    private Long staffId;

    /** The record type, Check-up or Follow-up. */
    private String recordType;

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
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID of the consultation.
     *
     * @param newId the unique ID of the consultation.
     */
    public void setId(Long newId) {
        this.id = newId;
    }

    /**
     * Retrieves the ID of the patient (person) consulted.
     *
     * @return the ID of the patient (person) consulted.
     */
    public Long getPatientId() {
        return patientId;
    }

    /**
     * Sets the ID of the patient (person) consulted.
     *
     * @param newPatientId the ID of the patient (person) consulted.
     */
    public void setPatientId(Long newPatientId) {
        this.patientId = newPatientId;
    }

    /**
     * Retrieves the ID of the personnel who recorded it.
     *
     * @return the ID of the personnel who recorded it.
     */
    public Long getStaffId() {
        return staffId;
    }

    /**
     * Sets the ID of the personnel who recorded it.
     *
     * @param newStaffId the ID of the personnel who recorded it.
     */
    public void setStaffId(Long newStaffId) {
        this.staffId = newStaffId;
    }

    /**
     * Retrieves the record type, Check-up or Follow-up.
     *
     * @return the record type, Check-up or Follow-up.
     */
    public String getRecordType() {
        return recordType;
    }

    /**
     * Sets the record type, Check-up or Follow-up.
     *
     * @param newRecordType the record type, Check-up or Follow-up.
     */
    public void setRecordType(String newRecordType) {
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
