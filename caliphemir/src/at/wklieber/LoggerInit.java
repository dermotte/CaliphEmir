/*
 * This file is part of Caliph & Emir.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2005 by Werner Klieber (werner@klieber.info)
 * http://caliph-emir.sourceforge.net
 */
package at.wklieber;

import at.wklieber.tools.FileTools;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * <p/>
 * <p/>
 * <strong>Verbal Class Description:</strong><br/>
 * Is used to initialize the log4j logger.
 * Configuration is search by following levels, until a valid config-info is found:
 * 1. search for environment  variable "LOG_CONFIG"
 * 2. search for a given (method-attibute) config-filename
 * 3. use a given (method-attibute) default log-level.
 * 4. use "ERROR" as default if everything else fails 
 * </p>
 * <p/>
 * <p/>
 * <strong>Instantiation:</strong><br/>
 * </p>
 * <p/>
 * <strong>Comments:</strong><br/>
 * </p>
 *
 * @author Werner Klieber
 *         Date:  24.09.2004
 */
public class LoggerInit {
    /* use the next static constructor in your main class to initializationDone the logger
    static {
        // intialize log4j
        configureLog4j("log4j.property", null);
    }
    */

    /**
     * configure the log4j so it is ready to use
     * first it try to load from the System-parameter "LOG_CONFIG"
     * second, it try to load the properties from the given filenename
     * finally, it configures  hardcoded with a given logname
     */

    private static boolean isAlreadyInitializedPA = false;

    //todo: enable some further debug levels
    public static void configureLog4j(String filename1, String a_logLevel) {
        if (isAlreadyInitializedPA) {
            return; //-----------------------------------> exit point
        }

        //System.out.println("configuring log4J");
        //PropertyConfigurator.resetConfiguration();

        String filename = filename1;
        if (filename != null && filename.length() > 0) {
            String logfilePath = FileTools.getWorkingDirectory(); // work dir is classes
            if (logfilePath.endsWith("classes/") || logfilePath.endsWith("classes\\")) {
                logfilePath = logfilePath.substring(0, logfilePath.length() - 8);
            }

            filename = FileTools.relative2absolutePath(filename, logfilePath, true);
        }

        File file = null;
        String debugLevel = "ERROR";
        if (a_logLevel != null && (a_logLevel.equalsIgnoreCase("DEBUG") ||
                a_logLevel.equalsIgnoreCase("INFO") ||
                a_logLevel.equalsIgnoreCase("WARN") ||
                a_logLevel.equalsIgnoreCase("ERROR") ||
                a_logLevel.equalsIgnoreCase("FATAL"))) {

            debugLevel = a_logLevel;
            //filename = "";
            //System.out.println("Using Loglevel from configuration-File: \"" + debugLevel + "\"");
        }

        try {

            //---- try to load from System-parameter
            String cfgFilename = System.getProperty("LOG_CONFIG");
            if (cfgFilename != null && cfgFilename.length() > 0) {
                cfgFilename = FileTools.resolvePath(cfgFilename);
                if (FileTools.existsFile(cfgFilename)) {
                    file = new File(cfgFilename);
                    //log.fine("Log-cfg: " + cfgFilename);
                }
            }

            //----- next, try to find a log4j property-file
            if (file == null) {
                String confFile = FileTools.resolvePath(filename);

                if (FileTools.existsFile(confFile)) {
//                    System.out.println("using configfile: " + confFile);
//                    PropertyConfigurator.configure(new URL(FileTools.setUrlPrefix(confFile)));
                } else {
//                    System.out.println("Warning: Configuration file \"" + confFile + "\" not found. " +
//                            "Setting configuration level by default to " + debugLevel);
                    //--- next, configure manually with default values
                    Properties p = new Properties();
//                    p.setProperty("log4j.rootCategory", debugLevel + ", stdout");
//                    p.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
//                    p.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
//                    p.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%-6r [%t] %-10p %l %x - %m%n");
//                    PropertyConfigurator.configure(p);

//                    System.out.println("Static Log4j initialized, Message-level: \"" + debugLevel + "\"");
                } // end if
            } // end if load from System-parameter

        } catch (Exception e) {
            //Console.exitOnException(e);
            e.printStackTrace();
        }

        // open and use a log4j-properties file if one exists
        try {
            if (file != null) {
//                PropertyConfigurator.configure(file.toURL());
                //DOMConfigurator.configure(f.toURL());
//                System.out.println("Using Log4j-Property file: " + file.toURL());
            }

        } catch (Exception e) {
            //Console.exitOnException(e);
            e.printStackTrace();
        }

        isAlreadyInitializedPA = true;
    } // end method

    public static void configureLog4jFromFile(String a_filename) {
        configureLog4j(a_filename, null);
    }

    public static void configureLog4jHardcoded(String a_logLevel) {
        configureLog4j(null, a_logLevel);
    }

}
