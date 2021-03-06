package me.dominiksta.jfiredump;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** This class is the main entrypoint for jfiredump */
public class App {

    public static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static Handler loggingHandler;

    /**
     * This will be displayed when the cli arguments are invalid or --help is
     * provided
     */
    static final String USAGE_TEXT = "jfiredump [<OPTIONS>] <FILE> {<TABLE>|!!all!!}" +
        System.lineSeparator() + "Available options:";

    /** Global logging setup */
    public static void initLoggin() {
        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(
                App.class.getClassLoader().getResourceAsStream("logging.properties")
            );
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }

        loggingHandler = new ConsoleHandler();

        logger.setLevel(Level.INFO);
        loggingHandler.setLevel(Level.INFO);

        logger.addHandler(loggingHandler);
    }

    /**
     * Parse cli arguments and start exporter based on cli arguments.
     */
    public static void main(String[] args) {

        App.initLoggin();

        // ----------------------------------------------------------------------
        // cli arguments
        // ----------------------------------------------------------------------

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(90);

        Option help = new Option(null, "help", false, "print this message");
        options.addOption(help);
        Option host = new Option(
            "h", "host", true, "specify database host (default: localhost)"
        );
        options.addOption(host);
        Option port = new Option(
            null, "port", true, "specify database port (default: 3050)"
        );
        options.addOption(port);
        Option user = new Option(
            "u", "user", true, "specify database user (default: SYSDBA)"
        );
        options.addOption(user);
        Option password = new Option(
            "p", "password", true, "specify database password (default: masterkey)"
        );
        options.addOption(password);
        Option verbose = new Option(
            "v", "verbose", false, "verbose logging output for debugging"
        );
        options.addOption(verbose);
        Option veryVerbose = new Option(
            "vv", "very-verbose", false, "very verbose logging output for debugging"
        );
        options.addOption(veryVerbose);
        Option outLocation = new Option(
            "o", "out-location", true, "specify output location (default for single" +
            " tables: '<datetime><table>.sql', default for all tables: " +
            "'<datetime> jfiredump')"
        );
        options.addOption(outLocation);
        Option encoding = new Option(
            "e", "encoding", true, "specify database encoding (firebird encoding" +
            " names, see https://github.com/FirebirdSQL/jaybird/wiki/Character-encodings)"
        );
        options.addOption(encoding);
        Option runFile = new Option(
            "r", "run-file", true, "run an existing .sql-File (only allows INSERT" +
            " statements). When using this option, the positional <TABLE> argument is" +
            " ignored. Will only commit when all statements were processed without" +
            " errors."
        );
        options.addOption(runFile);
        Option lineEndings = new Option("l", "line-endings", true, "either LF or CRLF");
        options.addOption(lineEndings);

        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);

            // print help and exit
            if (line.hasOption(help)) {
                formatter.printHelp(USAGE_TEXT, options);
                System.exit(0);
            }
            // check positional options
            if ((line.getArgs().length < 2 && line.getOptionValue(runFile) == null)
                || line.getArgs().length < 1) {
                System.err.println("Missing positional argument");
                formatter.printHelp(USAGE_TEXT, options);
                System.exit(1);
            }

            // set verbose logging
            if (line.hasOption(verbose)) {
                logger.setLevel(Level.FINE);
                loggingHandler.setLevel(Level.FINE);
            }

            // set very verbose logging
            if (line.hasOption(veryVerbose)) {
                logger.setLevel(Level.ALL);
                loggingHandler.setLevel(Level.ALL);
            }

            // typecheck port option
            if (line.hasOption(port)) {
                try {
                    Integer.parseInt(line.getOptionValue(port));
                } catch(NumberFormatException e) {
                    logger.severe("Invalid port: " + line.getOptionValue(port));
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            // ----------------------------------------------------------------------
            // run program based on cli arguments
            // ----------------------------------------------------------------------

            DBConnection con = new DBConnection(
                line.getOptionValue(host, "localhost"),
                Integer.parseInt(line.getOptionValue(port, "3050")),
                line.getArgs()[0],
                line.getOptionValue(user, "SYSDBA"),
                line.getOptionValue(password, "masterkey"),
                line.getOptionValue(encoding)
            );

            if (line.getOptionValue(runFile) == null) {
                // export to file
                // ------------------------------------------------------------
                DBExporterInsertStatements exporter =
                    new DBExporterInsertStatements(con);
                switch(line.getOptionValue(lineEndings, "auto")) {
                    case "LF":
                        exporter.setNewline("\n");
                        break;
                    case "CRLF":
                        exporter.setNewline("\r\n");
                        break;
                    case "auto":
                        // leave default
                        break;
                    default:
                        String msg = "Invalid specified line endings: " +
                            line.getOptionValue(lineEndings);
                        App.logger.severe(msg);
                        throw new RuntimeException(msg);
                }
                if (line.getArgs()[1].equals("!!all!!")) {
                    exporter.exportAllTables(line.getOptionValue(outLocation));
                } else {
                    exporter.exportTable(
                        line.getArgs()[1], line.getOptionValue(outLocation)
                    );
                }
            } else {
                // run existing file
                // ------------------------------------------------------------
                con.runFile(line.getOptionValue(runFile));
            }


            try {
                con.close();
            } catch (SQLException e) {
                App.logger.severe("Could not close database connection!");
                e.printStackTrace();
            }
        }
        catch (ParseException exp) {
            System.err.println("Parsing command line failed. Reason: " + exp.getMessage());
            formatter.printHelp(USAGE_TEXT, options);
            System.exit(1);
        }
    }
}
