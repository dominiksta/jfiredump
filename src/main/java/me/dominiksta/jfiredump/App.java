package me.dominiksta.jfiredump;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

/**
 * Hello world!
 */
public class App {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    static final String USAGE_TEXT = "jfiredump [options] [file]\nAvailable options:";

    public static void main(String[] args) {

        // ----------------------------------------------------------------------
        // global logging setup
        // ----------------------------------------------------------------------

        LogManager manager = LogManager.getLogManager();
        try {
            manager.readConfiguration(
                App.class.getClassLoader().getResourceAsStream("logging.properties")
            );
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }

        Handler handler = new ConsoleHandler();

        logger.setLevel(Level.INFO);
        handler.setLevel(Level.INFO);

        logger.addHandler(handler);

        // ----------------------------------------------------------------------
        // cli arguments
        // ----------------------------------------------------------------------

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();

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
        Option outFile = new Option(
            "o", "outfile", true, "specify output file (default: out.sql)"
        );
        options.addOption(outFile);

        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);

            // print help and exit
            if (line.hasOption(help)) {
                formatter.printHelp(USAGE_TEXT, options);
                System.exit(0);
            }
            // check positional file option
            if (line.getArgs().length != 1) {
                System.err.println("Missing positional argument [file]");
                formatter.printHelp(USAGE_TEXT, options);
                System.exit(1);
            }

            // set verbose logging
            if (line.hasOption(verbose)) {
                logger.setLevel(Level.FINE);
                handler.setLevel(Level.FINE);
            }

            // set very verbose logging
            if (line.hasOption(veryVerbose)) {
                logger.setLevel(Level.ALL);
                handler.setLevel(Level.ALL);
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

            DBConnection con = new DBConnection(
                line.getOptionValue(host, "localhost"),
                Integer.parseInt(line.getOptionValue(port, "3050")),
                line.getArgs()[0],
                line.getOptionValue(user, "SYSDBA"),
                line.getOptionValue(password, "masterkey")
            );

            try {
                String file = line.getOptionValue(outFile, "out.sql");
                BufferedWriter outFileWriter = new BufferedWriter(new FileWriter(file));
                App.logger.info("Opened file for output: " + file);

                DBExporter exporter = new DBExporterInsertStatements(con, outFileWriter);
                exporter.export("LICENSE");

                try {
                    con.close();    
                } catch (SQLException e) {
                    App.logger.severe("Could not close database connection!");
                    e.printStackTrace();
                }

                try {
                    outFileWriter.close();
                } catch (IOException e) {
                    App.logger.severe("Could not close outfile!");
                    e.printStackTrace();
                }

            } catch(IOException e) {
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
