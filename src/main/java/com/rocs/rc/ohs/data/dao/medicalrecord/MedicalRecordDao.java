package com.rocs.rc.ohs.data.dao.medicalrecord;

import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;

import java.util.List;

/**
 * Defines the data access operations for medical records.
 */
public interface MedicalRecordDao {

    /**
     * Retrieves a medical record by ID.
     *
     * @param id the ID of the medical record.
     * @return the medical record if found, otherwise null.
     */
    MedicalRecord findById(String id);

    /**
     * Retrieves all medical records of a patient.
     *
     * @param patientId the ID of the patient.
     * @return the medical records of the patient, or an empty list if
     *         the patient has none.
     */
    List<MedicalRecord> findByPatientId(String patientId);

    /**
     * Adds a medical record.
     *
     * @param medicalRecord the medical record to add.
     * @return true if successful, otherwise false.
     */
    boolean addMedicalRecord(MedicalRecord medicalRecord);
}
