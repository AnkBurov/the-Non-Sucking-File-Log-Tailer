package org.ankburov.logtailer;

/**
 * LogTailerListener interface. Implement this interface to create listener object
 */
public interface LogTailerListener {
    /**
     * This method is called if the tailed file is not found.
     */
    void fileNotFound();

    /**
     * This method handles the response from tailer
     */
    void update(String line);

    /**
     * This method handles any exception, except IOException (file not found)
     */
    void handleException(Exception exception);

    /**
     * This method is called if tailed file was deleted and tailer is termination
     */
    void handleRemovedFile();
}
