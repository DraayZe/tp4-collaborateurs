package fr.efrei.java;

public class OperationRefuseeException extends RegleMetierException {

    public OperationRefuseeException(String raison) {
        super("Refusé : " + raison);
    }
}
