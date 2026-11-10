package com.rocs.rc.ohs.data.dao.filter.impl;

import com.rocs.rc.ohs.app.model.medicalrecord.MedicalRecord;
import com.rocs.rc.ohs.app.model.person.Person;
import com.rocs.rc.ohs.data.connection.ConnectionHelper;
import com.rocs.rc.ohs.data.dao.filter.FilterDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the data access operations for filtering medical records.
 */
public class FilterDaoImpl implements FilterDao {

    /**
     * Retrieves the medical records that match the given criteria.
     * A null or blank criterion is ignored.
     *
     * @param patientName      The first or last name of the patient.
     * @param medicalCondition The medical condition of the patient.
     * @param allergy          The allergy of the patient.
     * @return The list of matching {@link MedicalRecord} records.
     */
    @Override
    public List<MedicalRecord> filterMedicalRecords(String patientName,
                                                    String medicalCondition,
                                                    String allergy) {

        List<MedicalRecord> records = new ArrayList<>();
        List<String> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "select m.id, m.medical_conditions, m.allergies, "
                        + "p.id as patient_id, p.first_name, "
                        + "p.middle_name, p.last_name "
                        + "from medical_record m "
                        + "join person p on p.id = m.patient_id "
                        + "where 1 = 1");

        if (hasValue(patientName)) {
            sql.append(" and (lower(p.first_name) like ?"
                    + " or lower(p.last_name) like ?)");
            params.add(toPattern(patientName));
            params.add(toPattern(patientName));
        }
        if (hasValue(medicalCondition)) {
            sql.append(" and lower(m.medical_conditions) like ?");
            params.add(toPattern(medicalCondition));
        }
        if (hasValue(allergy)) {
            sql.append(" and lower(m.allergies) like ?");
            params.add(toPattern(allergy));
        }
        sql.append(" order by p.last_name, p.first_name");

        try (Connection conn = ConnectionHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Person patient = new Person();
                    patient.setId(rs.getString("patient_id"));
                    patient.setFirstName(rs.getString("first_name"));
                    patient.setMiddleName(rs.getString("middle_name"));
                    patient.setLastName(rs.getString("last_name"));

                    MedicalRecord record = new MedicalRecord();
                    record.setId(rs.getString("id"));
                    record.setPatient(patient);
                    record.setMedicalConditions(
                            rs.getString("medical_conditions"));
                    record.setAllergies(rs.getString("allergies"));

                    records.add(record);
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "An exception was thrown while filtering medical records. "
                            + e.getMessage());
        }
        return records;
    }

    /**
     * Checks if the given criterion has a usable value.
     *
     * @param value The criterion to check.
     * @return true if the criterion is not null and not blank.
     */
    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Converts the given criterion to a case-insensitive "contains" pattern.
     *
     * @param value The criterion to convert.
     * @return The lowercase pattern wrapped in wildcards.
     */
    private String toPattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}
