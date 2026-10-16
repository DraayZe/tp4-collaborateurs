package fr.efrei.java;

/** Opération correcte dans la forme mais interdite par une règle métier. */
public class OperationRefuseeException extends RegleMetierException {

    public OperationRefuseeException(String raison) {
        super("Refusé : " + raison);
    }
}
