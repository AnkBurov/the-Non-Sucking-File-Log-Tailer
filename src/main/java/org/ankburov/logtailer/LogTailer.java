package org.ankburov.logtailer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The Non-Sucking File Log Tailer
 * <p>
 * Log tailer that doesn't suck. As simple as it sounds. Main advantage before Apache Tailer (from org.apache.commons.io.input)
 * is that non-sucking tailer doesn't lock file from deletion, even when tailer is reading the file (unlike Apache Tailer that
 * prevents file from deletion if Apache tailer is in the middle of the file). Using this tailer, you can easily write a program,
 * that will start a new thread of this tailer for each new file and tailing log files won't be locked from deletion.
 * If tailed file was deleted, the tailer is also closes its work.
 * <p>
 * Instructions
 * <p>
 * 1. Create LogTailer object using one of the constructors:
 * LogTailer tailer = new LogTailer(File file)
 * LogTailer tailer = new LogTailer()
 * 2. If you created LogTailer with "default" constructor, don't forget to set tailing file using setFile setter:
 * tailer.setFile(File file)
 * 3. Add one or more listeners (implementations of LogTailerListener):
 * tailer.addListener(LogTailerListener listener) as an inner class or a separate class.
 * 4. Run it as a new Thread:
 * new Thread(tailer).start()  Mark thread as a daemon thread, if you need.
 */
public class LogTailer implements Runnable {
    Set<LogTailerListener> listeners = new HashSet<LogTailerListener>();
    private File file;
    private long startTime;
    private long timeoutUntilInterrupt;
    private int sleepTimer;
    private boolean isStopped;
    private boolean isChanged;

    public LogTailer() {
        this.timeoutUntilInterrupt = 0;
        this.sleepTimer = 1000;
    }

    /**
     * Constructor method
     * timeoutUntilInterrupt - number of Hours before tailer thread will be terminated. 0 - unlimited. Default is 0.
     * sleepTimer - delay between each trying to read the completed log file. Measurers in Milliseconds. Default is 1000 ms.
     *
     * @param file - File class with listened file.
     */
    public LogTailer(File file) {
        this.timeoutUntilInterrupt = 0;
        this.sleepTimer = 1000;
        this.file = file;
    }

    /**
     * Runnable method fo log tailer
     */
    public void run() {
        /*start of thread*/
        startTime = System.currentTimeMillis();
        isStopped = false;
        isChanged = false;
        String line;
        InputStream is = null;
        try {
            is = Files.newInputStream(file.toPath(), StandardOpenOption.DSYNC);
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            BufferedReader lineReader = new BufferedReader(reader);
            // if tailer is not stopped, process all files
            while (!isStopped && file.exists()) {
                line = lineReader.readLine();
                if (line != null) {
                    setChanged();
                } else {
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleepTimer);
                        // check if life timer exceeded available live of object
                        if (timeoutUntilInterrupt != 0 && (System.currentTimeMillis() - startTime > timeoutUntilInterrupt)) {
                            setStopped(true);
                        }
                    } catch (InterruptedException e) {
                        notifyListenersAboutException(e);
                    }
                }
                notifyListeners(line);
            }
            throw new RuntimeException("File has been removed");
        } catch (NullPointerException e) {
            notifyFileNotFound();
        } catch (IOException e) {
            // file has not been found
            notifyFileNotFound();
        } catch (RuntimeException e) {
            // file has been deleted during tailing
            notifyListenersAboutRemovedFile();
        } catch (Exception e) {
            // any other exceptions
            notifyListenersAboutException(e);
        } finally {
            // close stream
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                notifyListenersAboutException(e);
            }
        }
    }

    /**
     * Setter alternative to constructor
     *
     * @param file - File class with listened file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Set custom number of hours.
     *
     * @param timeoutUntilInterrupt - number of Hours before tailer thread will be terminated. 0 - unlimited
     */
    public void setTimeoutUntilInterrupt(long timeoutUntilInterrupt) {
        this.timeoutUntilInterrupt = timeoutUntilInterrupt * 3600000;
    }

    /**
     * Set custom number of milliseconds
     *
     * @param sleepTimer - delay between each trying to read the completed log file. Measurers in Milliseconds
     */
    public void setSleepTimer(int sleepTimer) {
        this.sleepTimer = sleepTimer;
    }

    /**
     * set isChanged in true
     */
    public void setChanged() {
        isChanged = true;
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }

    /**
     * Method adds listener to listeners set
     *
     * @param listener - class implementing LogTailerListener
     * @return result of operation
     */
    public boolean addListener(LogTailerListener listener) {
        return listeners.add(listener);
    }

    /**
     * Method removes listener from listeners set
     *
     * @param listener - class implementing LogTailerListener
     * @return result of operation
     */
    public boolean removeListener(LogTailerListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Method notifies listeners about new string in tailing file
     *
     * @param line - new string in tailing file
     */
    public void notifyListeners(String line) {
        if (isChanged) {
            for (LogTailerListener listener : listeners) {
                listener.update(line);
            }
            isChanged = false;
        }
    }

    /**
     * Method notifies listeners about happened exception
     *
     * @param exception - happened exception
     */
    public void notifyListenersAboutException(Exception exception) {
        for (LogTailerListener listener : listeners) {
            listener.handleException(exception);
        }
    }

    /**
     * Method notifies listeners about removed file
     */
    public void notifyListenersAboutRemovedFile() {
        for (LogTailerListener listener : listeners) {
            listener.handleRemovedFile();
        }
    }

    /**
     * Method notifies listeners that file has not been found
     */
    public void notifyFileNotFound() {
        for (LogTailerListener listener : listeners) {
            listener.fileNotFound();
        }
    }
}
