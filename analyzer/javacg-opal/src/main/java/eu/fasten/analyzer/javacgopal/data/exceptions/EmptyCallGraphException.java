package eu.fasten.analyzer.javacgopal.data.exceptions;

/**
 * Exception that is thrown whenever any generated call graph is empty.
 */
public class EmptyCallGraphException extends Exception {
    /**
     * Default constructor without a message.
     */
    public EmptyCallGraphException() {
        super();
    }
}
