package com.rocs.rc.ohs.app.model.prescription;

import com.rocs.rc.ohs.app.model.consultation.Consultation;
import com.rocs.rc.ohs.app.model.medicine.MedicineStock;
import com.rocs.rc.ohs.app.model.personnel.Personnel;

/**
 * Represents a medicine prescribed during a consultation.
 */
public class Prescription {

    /** The unique ID of the prescription. */
    private String id;

    /** The consultation associated with the prescription. */
    private Consultation consultation;

    /** The medicine stock associated with the prescription. */
    private MedicineStock medicineStock;

    /** The personnel who prescribed the medicine. */
    private Personnel personnel;

    /** The quantity of medicine to dispense. */
    private int quantityToDispense;

    /** The prescribed dose. */
    private String dose;

    /** How often the medicine should be taken. */
    private String frequency;

    /** Additional instructions for the patient. */
    private String instructions;

    /**
     * Retrieves the unique ID of the prescription.
     *
     * @return the unique ID of the prescription.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the prescription.
     *
     * @param newId the unique ID of the prescription.
     */
    public void setId(String newId) {
        this.id = newId;
    }

    /**
     * Retrieves the consultation associated with the prescription.
     *
     * @return the consultation associated with the prescription.
     */
    public Consultation getConsultation() {
        return consultation;
    }

    /**
     * Sets the consultation associated with the prescription.
     *
     * @param newConsultation the consultation associated with the prescription.
     */
    public void setConsultation(Consultation newConsultation) {
        this.consultation = newConsultation;
    }

    /**
     * Retrieves the medicine stock associated with the prescription.
     *
     * @return the medicine stock associated with the prescription.
     */
    public MedicineStock getMedicineStock() {
        return medicineStock;
    }

    /**
     * Sets the medicine stock associated with the prescription.
     *
     * @param newMedicineStock the medicine stock associated with the
     *                         prescription.
     */
    public void setMedicineStock(MedicineStock newMedicineStock) {
        this.medicineStock = newMedicineStock;
    }

    /**
     * Retrieves the personnel who prescribed the medicine.
     *
     * @return the personnel who prescribed the medicine.
     */
    public Personnel getPersonnel() {
        return personnel;
    }

    /**
     * Sets the personnel who prescribed the medicine.
     *
     * @param newPersonnel the personnel who prescribed the medicine.
     */
    public void setPersonnel(Personnel newPersonnel) {
        this.personnel = newPersonnel;
    }

    /**
     * Retrieves the quantity of medicine to dispense.
     *
     * @return the quantity of medicine to dispense.
     */
    public int getQuantityToDispense() {
        return quantityToDispense;
    }

    /**
     * Sets the quantity of medicine to dispense.
     *
     * @param newQuantityToDispense the quantity of medicine to dispense.
     */
    public void setQuantityToDispense(int newQuantityToDispense) {
        this.quantityToDispense = newQuantityToDispense;
    }

    /**
     * Retrieves the prescribed dose.
     *
     * @return the prescribed dose.
     */
    public String getDose() {
        return dose;
    }

    /**
     * Sets the prescribed dose.
     *
     * @param newDose the prescribed dose.
     */
    public void setDose(String newDose) {
        this.dose = newDose;
    }

    /**
     * Retrieves how often the medicine should be taken.
     *
     * @return how often the medicine should be taken.
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Sets how often the medicine should be taken.
     *
     * @param newFrequency how often the medicine should be taken.
     */
    public void setFrequency(String newFrequency) {
        this.frequency = newFrequency;
    }

    /**
     * Retrieves the additional instructions for the patient.
     *
     * @return the additional instructions for the patient.
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Sets the additional instructions for the patient.
     *
     * @param newInstructions the additional instructions for the patient.
     */
    public void setInstructions(String newInstructions) {
        this.instructions = newInstructions;
    }
}
