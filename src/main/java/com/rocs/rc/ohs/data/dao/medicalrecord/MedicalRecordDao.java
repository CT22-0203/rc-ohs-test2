package com.rocs.rc.ohs.data.dao.medicalrecord;

import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;

public interface MedicalRecordDao {
    /**
     * Updates modifiable fields of an existing medical record.
     *
     * @param medicalRecord the record containing the updated values.
     * @return true if the record was updated false if no record
     * with the given ID exists.
     */
    boolean updateMedicalRecord(
            MedicalRecord medicalRecord);
}
