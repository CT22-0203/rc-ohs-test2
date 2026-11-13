package com.rocs.rc.ohs.app.facade.consultation.impl;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.enums.recordtype.RecordType;
import com.rocs.rc.ohs.app.model.medicine.MedicineStock;
import com.rocs.rc.ohs.app.model.person.Person;
import com.rocs.rc.ohs.app.model.personnel.Personnel;
import com.rocs.rc.ohs.app.model.prescription.Prescription;
import com.rocs.rc.ohs.data.dao.consultation.ConsultationDao;
import com.rocs.rc.ohs.data.dao.prescription.PrescriptionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ConsultationFacadeImpl} class without a real database.
 */
class ConsultationFacadeImplTest {

    /** The ID of Jowan Carl Quirona, a patient in the test data. */
    private static final String JOWAN_ID =
            "f04cac48-889d-4a62-9a2b-a274406ece20";

    /** The ID of a personnel in the test data. */
    private static final String STAFF_ID =
            "64ffaffe-79b5-4690-bbd4-587507b6859c";

    /** The ID of the Amoxicillin 500mg stock in the test data. */
    private static final String STOCK_ID =
            "70aaf305-a7f5-41f1-bd0a-6ad73c042be9";

    /** The ID of a consultation in the test data. */
    private static final String CONSULTATION_ID =
            "a451a504-a3d4-4721-bf8d-a18b8c199356";

    /** The date of the consultation in the test cases. */
    private static final LocalDateTime DATE =
            LocalDateTime.of(2026, 10, 20, 9, 30);

    /** The diagnosis of Jowan in the test cases. */
    private static final String DIAGNOSIS = "Bacterial throat infection";

    /** The treatment of Jowan in the test cases. */
    private static final String TREATMENT = "Antibiotic course and rest";

    /** The mocked consultation DAO. */
    private ConsultationDao consultationDao;

    /** The mocked prescription DAO. */
    private PrescriptionDao prescriptionDao;

    /** The facade under test. */
    private ConsultationFacadeImpl facade;

    @BeforeEach
    void setUp() {
        consultationDao = mock(ConsultationDao.class);
        prescriptionDao = mock(PrescriptionDao.class);

        facade = new ConsultationFacadeImpl(consultationDao,
                prescriptionDao);
    }

    // The consultation and its prescription are saved.
    @Test
    void savesTheConsultationWithItsPrescriptions() {
        Consultation consultation =
                newConsultation(null, DIAGNOSIS, TREATMENT);
        Prescription prescription =
                newPrescription("500mg", "3 times a day", 2);

        Consultation saved = facade.saveConsultation(consultation,
                List.of(prescription));

        verify(consultationDao).add(consultation);
        verify(prescriptionDao).add(prescription);
        assertNotNull(saved.getId());
        assertEquals(JOWAN_ID, saved.getPatient().getId());
        assertEquals(STAFF_ID, saved.getStaff().getId());
        assertEquals(DATE, saved.getConsultationDate());
        assertEquals(DIAGNOSIS, saved.getDiagnosis());
        assertEquals(TREATMENT, saved.getTreatment());

        assertNotNull(prescription.getId());
        assertEquals(saved, prescription.getConsultation());
        assertEquals(STAFF_ID, prescription.getPersonnel().getId());
        assertEquals(STOCK_ID, prescription.getMedicineStock().getId());
        assertEquals("500mg", prescription.getDose());
        assertEquals("3 times a day", prescription.getFrequency());
        assertEquals(2, prescription.getQuantityToDispense());
    }

    // The saved consultations of the patient are retrieved.
    @Test
    void returnsTheSavedConsultationsOfThePatient() {
        Consultation consultation =
                newConsultation(CONSULTATION_ID, DIAGNOSIS, TREATMENT);
        Prescription prescription =
                newPrescription("500mg", "3 times a day", 2);
        when(consultationDao.findByPatientId(JOWAN_ID))
                .thenReturn(List.of(consultation));
        when(prescriptionDao.findByConsultationId(CONSULTATION_ID))
                .thenReturn(List.of(prescription));

        List<Consultation> consultations =
                facade.getConsultationsByPatient(JOWAN_ID);
        List<Prescription> prescriptions =
                facade.getPrescriptionsByConsultation(CONSULTATION_ID);

        assertEquals(1, consultations.size());
        assertEquals(CONSULTATION_ID, consultations.get(0).getId());
        assertEquals(DIAGNOSIS, consultations.get(0).getDiagnosis());
        assertEquals(TREATMENT, consultations.get(0).getTreatment());
        assertEquals(1, prescriptions.size());
        assertEquals("500mg", prescriptions.get(0).getDose());
        assertEquals(2, prescriptions.get(0).getQuantityToDispense());
    }

    private Consultation newConsultation(String id, String diagnosis,
                                         String treatment) {
        Person patient = new Person();
        patient.setId(JOWAN_ID);
        Personnel staff = new Personnel();
        staff.setId(STAFF_ID);
        Consultation consultation = new Consultation();
        consultation.setId(id);
        consultation.setPatient(patient);
        consultation.setStaff(staff);
        consultation.setRecordType(RecordType.CHECK_UP);
        consultation.setConsultationDate(DATE);
        consultation.setDiagnosis(diagnosis);
        consultation.setTreatment(treatment);
        return consultation;
    }

    private Prescription newPrescription(String dose, String frequency,
                                         int quantity) {
        MedicineStock stock = new MedicineStock();
        stock.setId(STOCK_ID);
        Prescription prescription = new Prescription();
        prescription.setMedicineStock(stock);
        prescription.setDose(dose);
        prescription.setFrequency(frequency);
        prescription.setQuantityToDispense(quantity);
        return prescription;
    }
}
