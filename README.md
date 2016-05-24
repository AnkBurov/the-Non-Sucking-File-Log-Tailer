# The Non-Sucking File Log Tailer #
[![](https://jitpack.io/v/AnkBurov/the-Non-Sucking-File-Log-Tailer.svg)](https://jitpack.io/#AnkBurov/the-Non-Sucking-File-Log-Tailer)
* Log tailer that doesn't suck. As simple as it sounds. Main advantage before Apache Tailer (from org.apache.commons.io.input) is that non-sucking tailer doesn't lock file from deletion, even when tailer is reading the file (unlike Apache Tailer that prevents file from deletion if Apache tailer is in the middle of the file). Using this tailer, you can easily write a program, that will start a new thread of this tailer for each new file and tailing log files won't be locked from deletion. If tailed file was deleted, the tailer is also closes its work.  



## Instructions ##
0. Open jitpack link above and add repository and dependency in your build file.
1. Create LogTailer object using one of the constructors:
* LogTailer tailer = new LogTailer(File file)
* LogTailer tailer = new LogTailer()
2. If you created LogTailer with "default" constructor, don't forget to set tailing file using setFile setter:
* tailer.setFile(File file)
3. Add one or more listeners (implementations of LogTailerListener):
* tailer.addListener(LogTailerListener listener) as an inner class or a separate class.
4. Run it as a new Thread:
* new Thread(tailer).start()  Mark thread as a daemon thread, if you need.