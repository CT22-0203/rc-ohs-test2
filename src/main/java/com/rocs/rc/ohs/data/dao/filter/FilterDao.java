package com.rocs.rc.ohs.data.dao.filter;

import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;

import java.util.List;

/**
 * Defines the data access operations for filtering medical records.
 */
public interface FilterDao {

    /**
     * Retrieves the medical records that match the given criteria.
     * A null or blank criterion is ignored.
     *
     * @param patientName      The first or last name of the patient.
     * @param medicalCondition The medical condition of the patient.
     * @param allergy          The allergy of the patient.
     * @return The list of matching {@link MedicalRecord} records.
     */
    List<MedicalRecord> filterMedicalRecords(String patientName,
                                             String medicalCondition,
                                             String allergy);
}
