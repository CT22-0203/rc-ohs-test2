package com.rocs.rc.ohs.data.dao.medicalrecord.impl;

import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;
import com.rocs.rc.ohs.data.dao.medicalrecord.MedicalRecordDao;
import com.rocs.rc.ohs.data.utils.QueryConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * JDBC implementation of {@link MedicalRecordDao}.
 */
public class MedicalRecordDaoImpl implements MedicalRecordDao {

    /** The database connection. */
    private  final Connection connection;

    /**
     * Create a DAO that uses the given database connection.
     *
     * @param newConnection connection.
     */
    public MedicalRecordDaoImpl(Connection newConnection) {
        this.connection = newConnection;
    }

    /** {@inheritDoc} */
    @Override
    public boolean updateMedicalRecord(
            MedicalRecord medicalRecord) {
        try (PreparedStatement statement = connection
                .prepareStatement(QueryConstants.
                        UPDATE_MEDICAL_RECORD_STATEMENT)) {
            statement.setString(1, medicalRecord.getMedicalConditions());
            statement.setString(2, medicalRecord.getAllergies());
            statement.setString(3, medicalRecord.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            String id = medicalRecord.getId();
            throw new RuntimeException(
                    "Failed to update medical record: " + id, e);
        }
    }
}
