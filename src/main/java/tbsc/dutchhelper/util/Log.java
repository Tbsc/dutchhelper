package tbsc.dutchhelper.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Copied directly from DutchHelper-v1, with changes to work with the debug modes system.
 * Logging requires having an instance of this class to enable logging the caller, if enabled through debug.
 *
 * Created on 07/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class Log {

    enum LogLevel {
        INFO(" [INFO] "),
        WARN(" [WARN] "),
        ERROR("[ERROR] "),
        FATAL("[FATAL] "),
        DEBUG("[DEBUG] "),
        NONE("");

        String levelprint;

        LogLevel(String levelprint) {
            this.levelprint = levelprint;
        }
    }

    private final String className;

    public Log(String className) {
        this.className = className;
    }

    public Log(Class clazz) {
        this.className = clazz.getSimpleName();
    }

    public Log(Object objClass) {
        this.className = objClass.getClass().getSimpleName();
    }

    /**
     * I stands for info.
     */
    public void i(Object print, Object... format) {
        log(LogLevel.INFO, print, format);
    }

    /**
     * W stands for warn(ing).
     */
    public void w(Object print, Object... format) {
        log(LogLevel.WARN, print, format);
    }

    /**
     * E stands for error.
     */
    public void e(Object print, Object... format) {
        log(LogLevel.ERROR, print, format);
    }

    /**
     * F stands for fatal.
     */
    public void f(Object print, Object... format) {
        log(LogLevel.FATAL, print, format);
    }

    /**
     * D stands for debug.
     * Only prints when debug mode is on, either by adding "-d" or "--debug=log" to the arguments
     */
    public void d(Object print, Object... format) {
        if (DebugHelper.isModeActive(DebugHelper.Modes.LOG)) {
            log(LogLevel.DEBUG, print, format);
        }
    }

    public void print(Object print, Object... format) {
        log(LogLevel.NONE, print, format);
    }

    private void log(LogLevel loglevel, Object print, Object... format) {
        System.out.println(
                loglevel.levelprint // [loglevel]
                + "[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd-HH:mm:ss")) + "] " // [time]
                + (DebugHelper.isModeActive(DebugHelper.Modes.LOG_CALLER) ? "[" + className + "] " : "") // only if active
                + String.format(print.toString(), format)); // log message
    }

}
