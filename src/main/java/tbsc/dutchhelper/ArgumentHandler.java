package tbsc.dutchhelper;

import org.apache.commons.cli.*;
import tbsc.dutchhelper.util.DebugHelper;
import tbsc.dutchhelper.util.Log;

import java.io.File;

/**
 * Uses Apache's commons-cli to handle arguments.
 * I've decided to not use JavaFX's argument handler because it doesn't allow me to have short arguments with a value.
 * I'm not sure if I'm just doing it wrong, but
 *
 * Created on 12/08/2017
 * @author tbsc
 * @since 2.0.0
 */
public class ArgumentHandler {

    private static Log log = new Log(ArgumentHandler.class);

    public static void handle(String[] args) {
        Options options = new Options();
        options.addOption("d", "enable all debug modes");
        options.addOption(Option.builder()
                .longOpt("debug")
                .desc("enable specific debug modes")
                .hasArg(true)
                .build());
        // options.addOption("debug", true, "enable specific debug modes");
        options.addOption("p", "database-path", true, "change path to database");
        options.addOption("h", "help", false, "print this message");

        CommandLineParser parser = new DefaultParser();

        try {
            internalHandle(parser.parse(options, args), options);
        } catch (ParseException e) {
            log.e("Argument parsing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * After parsing, actually handles arguments.
     * @param cmd Parsed arguments
     */
    private static void internalHandle(CommandLine cmd, Options options) {
        if (cmd.hasOption("h")) {
            HelpFormatter help = new HelpFormatter();
            help.printHelp("dutchhelper", options);
            System.exit(0);
        }

        if (cmd.hasOption("debug")) {
            DebugHelper.parseModes(cmd.getOptionValue("debug"));
        }

        if (cmd.hasOption("d")) {
            DebugHelper.enableAllModes();
        }

        if (cmd.hasOption("p")) {
            handleCustomDatabasePath(cmd.getOptionValue("p"));
        }
    }

    private static void handleCustomDatabasePath(String path) {
        if (!new File(path).exists()) {
            log.e("Invalid database path (%s), directory not found.", path);
            System.exit(1);
        }
        Constants.DATABASE_PATH = path;
        log.i("Database path set to %s", path);
    }

}
