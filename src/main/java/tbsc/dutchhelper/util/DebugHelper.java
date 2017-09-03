package tbsc.dutchhelper.util;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Allow user to enable specific debug modes through parameters.
 * e.g. gridLines for showing the lines in the grid of the application,
 * log for showing debug log output
 *
 * -d will just enable everything
 *
 * Valid debug modes:
 * log: Enable debug log messages (that detail every little thing that the program does)
 * logCaller: Log messages will show which class logged it
 * gridLines: In the GUI, show the lines that make up the grid.
 *
 * Created on 09/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class DebugHelper {

    private static Log log = new Log(DebugHelper.class);

    /**
     * This is toggled to true only when "-d" is supplied as an argument.
     */
    private static boolean ALL_ACTIVE = false;

    /**
     * All *active* modes are listed here.
     * If a mode exists but isn't here, it's disabled and it's "actions" should not be done.
     */
    private static List<Modes> ACTIVE_MODES = new ArrayList<>();

    /**
     * Checks if the mode is currently active, or if all modes have been enabled using "-d".
     * @param mode Mode to check
     * @return Are all modes enabled, and if not, is this mode enabled specifically
     */
    public static boolean isModeActive(Modes mode) {
        return ALL_ACTIVE || ACTIVE_MODES.contains(mode);
    }

    /**
     * Meant to be used when parsing command line arguments.
     * Goes through the string, separates by commas, checks all outputs and enables relevant debug modes.
     * If it contains a nonexistent mode, it prints a warning to console.
     * @param arguments command line arguments containing debug modes to enable, separated by commas
     */
    public static void parseModes(String arguments) {
        // separates string by commas
        String[] modes = arguments.split(",");
        // converts elements to enum type, removes null values and set active modes list to this
        ACTIVE_MODES = Arrays.stream(modes)
                .map(Modes::fromString)
                .filter(Objects::nonNull)
                .peek(m -> log.i("Enabled debug mode: " + m.getTextForm()))
                .collect(Collectors.toList());
        Arrays.stream(modes).filter(m -> Modes.fromString(m) == null)
                .forEach(m -> log.w("Attempted to enable nonexistent debug mode " + m + ", ignoring"));
    }

    /**
     * Used when "-d" is an argument. Essentially, just ignores everything and enables all debug modes.
     */
    public static void enableAllModes() {
        ALL_ACTIVE = true;
        log.i("All debug modes activated");
    }

    /**
     * Enum of all possible modes.
     * Adding new modes only takes adding them here, and implementing their usage.
     * Documenting them in the top of this file is also recommended.
     */
    public enum Modes {

        LOG("log"), LOG_CALLER("logCaller"), GRID_LINES("gridLines");

        private String textForm;

        Modes(String textForm) {
            this.textForm = textForm;
        }

        public String getTextForm() {
            return textForm;
        }

        /**
         * Convert a string from of a mode (used in program arguments) to the corresponding enum type.
         * @param textForm String form of the mode
         * @return Enum object corresponding to the string form, or null if not valid
         */
        public static Modes fromString(@NotNull String textForm) {
            // filter stream of all modes to contain only modes whose text form is the same as the parameter
            // since the result should only be one mode, just get first element and return it
            List<Modes> modes = Arrays.stream(values())
                    .filter(m -> m.textForm.equalsIgnoreCase(textForm))
                    .collect(Collectors.toList());
            // prevent returning a non-existent element
            return modes.size() > 0 ? modes.get(0) : null;
        }

    }

}
