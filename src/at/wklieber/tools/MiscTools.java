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
package at.wklieber.tools;




import java.awt.*;
import java.awt.image.ImageProducer;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

public class MiscTools
        //implements Constants
        {
    static Logger cat = Logger.getLogger(MiscTools.class.getName());

    private static Console console = Console.getReference();

    private MiscTools() {
    }

    // configure the log4j so it is ready to use
    // first try to load from System-parameter
    // second, try to load from a given property-file if possible
    // finally, configure hardcoded with default values
    public static void configureLog4j(String filename1, String configAccess1, String log4jPath1) {
        //console.echo("configuring log4J");
        //PropertyConfigurator.resetConfiguration();

        //configAccess1 should be i
        configAccess1 = null; // not used

        String filename = filename1;
        File file = null;
        String debugLevel = "ERROR";

        try {
            //--- first check the configuration file, wheter to override
            //      any other configuration-files with a default value
            //IAccessFile config = Settings.getConfigAccess();


                //---- next try to load from System-parameter
                String cfgFilename = System.getProperty("LOG_CONFIG");
                if (cfgFilename != null && cfgFilename.length() > 0) {
                    cfgFilename = FileTools.resolvePath(cfgFilename);
                    if (FileTools.existsFile(cfgFilename)) {
                        file = new File(cfgFilename);
                        //cat.fine("Log-cfg: " + cfgFilename);
                    }
                }

            //----- next, try to find a log4j property-file
            if (file == null) {
                String confFile = FileTools.resolvePath(filename);

                if (FileTools.existsFile(confFile)) {
                    //file = new File(jarFile);

//                    PropertyConfigurator.configure(new URL(FileTools.setUrlPrefix(confFile)));
                    //cat.fine("Log-cfg: " + cfgFile);
                } else {
                    //--- next, configure manually with default values
                    Properties p = new Properties();
                    p.setProperty("log4j.rootCategory", debugLevel + ", stdout");
                    p.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
                    p.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
                    p.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%-6r [%t] %-10p %l %x - %m%n");
//                    PropertyConfigurator.configure(p);

                    console.echo("Log4j: Static Debuglevel: \"" + debugLevel + "\"");
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
                System.out.println("Log4j: Using property file: " + file.toURL());
            }

        } catch (MalformedURLException e) {
            //Console.exitOnException(e);
            e.printStackTrace();
        }
        //console.echo("Configure end");
    } // end method

    public static void configureLog4j(String filename1) {
        configureLog4j(filename1, null, null);
    }

    public static void configureLog4j() {
        configureLog4j(null, null, null);
    }


    /**
     * print a list of all member variables of the given object
     */
    public static void toStringGenerator(Object className1) {
        try {
            //Class targetClass = Class.forName(className1);
            Class targetClass = className1.getClass();
            if (!targetClass.isPrimitive() && targetClass != String.class) {
                Field fields[] = targetClass.getDeclaredFields();
                Class cSuper = targetClass.getSuperclass(); // Retrieving the super class
                output("StringBuffer buffer = new StringBuffer(500);"); // Buffer Construction
                if (cSuper != null && cSuper != Object.class) {
                    output("buffer.append(super.toString());"); // Super class's toString()
                }
                for (int j = 0; j < fields.length; j++) {
                    output("buffer.append(\"" + fields[j].getName() + " = \");"); // Append Field name
                    if (fields[j].getType().isPrimitive() || fields[j].getType() == String.class) // Check for a primitive or string
                        output("buffer.append(this." + fields[j].getName() + ");"); // Append the primitive field value
                    else {
                        /* It is NOT a primitive field so this requires a check for the NULL value for the aggregated object */
                        output("if ( this." + fields[j].getName() + "!= null )");
                        output("buffer.append(this." + fields[j].getName() + ".toString());");
                        output("else buffer.append(\"value is null\"); ");
                    } // end of else
                } // end of for loop
                output("return  buffer.toString();");
            } // end if
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Class not found in the class path");
            //System.exit(0);
        }
    } // end method

    private static void output(String data) {
        System.out.println(data);
    }


    /**
     * print all Java environment variables (System.getProperties)
     */
    public static void printSystemVariables() {
        System.out.println("======= System Variables start ===========");
        Properties properties = System.getProperties();
        Enumeration enum1 = properties.propertyNames();
        while (enum1.hasMoreElements()) {
            String key = (String) enum1.nextElement();
            System.out.println(key + ": \"" + System.getProperty(key) + "\"");
        }
        System.out.println("======= System Variables end ===========");
    } // end method

    public static boolean isWindowsOperatingSystem() {
        boolean returnValue = false;
        String key = "os.name";
        String value = System.getProperty(key);
        if (value.indexOf("Win") > -1) {
            returnValue = true;
        }
        cat.fine("key: <" + key + ">, value: " + value + ", is Windows: " + returnValue);

        return returnValue;
    } // end method


    /**
     * wait for "second" seconds
     */
    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end method


    /**
     * returns an instance of the class given by the input name e.g "tools.MyTools"
     * Note: the given class must be in the classpath and it must have just a constructor
     * without inpputparameter e.g. net tools.MyTools() must be possible
     * If something is wrong (Class not found, or unable to create an instance) null is returned
     */
    public static Object loadClassByName(String className1) {
        Object returnValue = null;
        try {
            Class me = ClassLoader.getSystemClassLoader().loadClass(className1);
            returnValue = me.newInstance();
        } catch (ClassNotFoundException e) {
            cat.fine(e.toString());

        } catch (InstantiationException e) {
            cat.fine(e.toString());
        } catch (IllegalAccessException e) {
            cat.fine(e.toString());
        }


        return returnValue;
    }

    /**
     * read in a file from a web server. At the moment the result have to be a string: HTML, XML, ...
     * Not an image.
     *
     * @param urlString The url where to download the file e.g. "http://localhost/index.html"
     */
    public static String downloadTextFileFromHttp(String urlString, String default1) {
        String returnValue = default1;

        try {
            //ObjectInputStream is;
            URL url;

            //calling the server
            url = new URL(urlString);


            //--------
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //Set mime type for POST
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //Now Write Data
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            //Here is where you add the form variables
            String post = "";//"param1=" + URLEncoder.encode( paramVariable1 ) + "&param2=" + URLEncoder.encode( paramVariable2 );
            dos.writeBytes(post);
            dos.flush();
            dos.close();

            //Now Read Data
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuffer strBuff = new StringBuffer();
            String t = in.readLine();
            while (t != null) {
                //System.out.println(t);
                strBuff.append(t);
                t = in.readLine();
            }
            in.close();
            returnValue = strBuff.toString();

        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * download a binary file from the internet and save it on the targetDir
     * if the file cannot be downloaded, no target-file is written.
     */
    public static void saveFileFromHttp(String httpUri, String targetFile) {
        try {
            //ObjectInputStream is;
            URL url = new URL(httpUri);
            URI uri = new URI(httpUri);
            System.out.println("DownLoad: \"" + url + "\"");
            //File f = new File(uri);

            //FileInputStream fin =  new FileInputStream(f);
            //FileTools.saveToFile(targetFile, fin);

            Toolkit tk = Toolkit.getDefaultToolkit();
            Image img = tk.createImage((ImageProducer) url.getContent());
            //Java2dTools.getReference().showImage(img);
            byte[] dataBytes = Java2dTools.getReference().serializeImage(img, Java2dTools.TYPE_SERIALIZE_JPEG);
            FileTools.saveToFile(targetFile, dataBytes);



/*




            //--------
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //Set mime type for POST
            //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Type", "image/jpeg");

            //Now Write Data
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

            //Here is where you add the form variables
            String post = "";//"param1=" + URLEncoder.encode( paramVariable1 ) + "&param2=" + URLEncoder.encode( paramVariable2 );
            dos.writeBytes(post);
            dos.flush();
            dos.close();

            //conn.getC
            //Now Read Data
            InputStream inStream = conn.getInputStream();
            //BufferedReader in = new BufferedReader(new InputStreamReader(inStream));

           // Toolkit tk = Toolkit.getDefaultToolkit();
           //Image img = tk.createImage((ImageProducer) url.getContent());
           //Java2dTools.getReference().showImage(img);

            FileTools.saveToFile(targetFile, inStream);
*/

        } catch (Exception e) {
            cat.severe(e.toString());
            e.printStackTrace();
            System.exit(10);
        }

    }

    /**
     * read an inputStream and try to convert the chars to a string
     * Not an image.
     */
    public static String inputStreamToString(InputStream inputStream, String default1) {
        String returnValue = default1;

        try {
            //Now Read Data
            returnValue = inputStreamToString(new InputStreamReader(inputStream), default1);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return returnValue;
    }

    /**
     * read an inputStream and try to convert the chars to a string
     * Not an image.
     */
    public static String inputStreamToString(InputStreamReader inputStreamReader, String default1) {
        String returnValue = default1;

        try {
            //Now Read Data
            BufferedReader in = new BufferedReader(inputStreamReader);

            StringBuffer strBuff = new StringBuffer();
            String t = in.readLine();
            while (t != null) {
                //System.out.println(t);
                strBuff.append(t);
                t = in.readLine();
            }
            in.close();
            returnValue = strBuff.toString();

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        return returnValue;
    }
}  // end class