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


import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


class ResourceClass {
    public String getBaseDir() {
        URL base = this.getClass().getResource("/");
        String returnValue = base.toExternalForm();
        return returnValue;
    }
}

public class FileTools {
    //static Logger log = Logger.getLogger(FileTools.class);
    public static String LINE_BREAK = "\n";

    public static final String SEPARATOR = File.separator;
    private static int BUFFER_SIZE = 2048;

    private static boolean noDebug = false;
    private static FileTools fileTools = new FileTools();
    private static String currentWorkingDir = null;

    /*static {
        byte[] b = {Character.LINE_SEPARATOR};
        LINE_BREAK = new String(b);
    }*/

    public static void setNoDebug(boolean noDebug1) {
        noDebug = noDebug1;
    }

    /**
     * do not instanciate this class
     */
    private FileTools() {

    }

    /**
     * if the input string doesn't end with an directory-seperator
     * one is added
     */
    public static String setFinalDirectorySlash(String dir) {
        if (!dir.endsWith("/") && !dir.endsWith("\\"))
            dir += SEPARATOR;

        return dir;
    }


    /**
     * replace invalid characters filename that are not allowed in
     * filenames. Note: ":" is replaced. so don't use it with absolute paths
     * like "c:\test". The same is with "\", "/"
     */
    public static String makeValidFilename(String filename1) {
        String ret = filename1;
        ret = ret.replace(':', '_');
        ret = ret.replace('\\', '_');
        ret = ret.replace('/', '_');
        ret = ret.replace(';', '_');
        ret = ret.replace('*', '_');
        ret = ret.replace('?', '_');
        ret = ret.replace('\"', '_');
        ret = ret.replace('>', '_');
        ret = ret.replace('<', '_');
        ret = ret.replace('|', '_');

        return ret;
    }

    /**
     * true if the give file exists
     */
    public static boolean existsFile(String filename1) {
        boolean returnValue = false;
        if (filename1 == null) return returnValue;

        try {
            String name = removeFileUrlPrefix(filename1);

            File f = new File(name);
            returnValue = (f.exists() && f.isFile());

            if (returnValue) {
                return returnValue;  //-- exit point -------------
            }
        } catch (Exception e) {
            //log.info("Exception: " + e.getMessage());
            //e.printStackTrace();
            returnValue = false;
        }

        // file does not work with files within a jar file
        // so invoke the resource Loader to find the file within a jar-file
        try {
            returnValue = false;
            int startPos = filename1.indexOf("!");
            if (startPos > -1) {

                filename1 = filename1.substring(startPos + 2); // without leading "!/"
                //log.fine("LOOK for: " + filename1);

                ClassLoader loader = fileTools.getClass().getClassLoader();
                URL url = loader.getResource(filename1);
                returnValue = (url != null);
            } // end if is located in a jar-file
        } catch (Exception e) {
            //log.severe(e);
            //e.printStackTrace();
            returnValue = false;
        }

        return returnValue;
    }

    /**
     * true if the give file or dir exists
     */
    public static boolean exists(String filename1) {
        boolean returnValue = false;
        if (filename1 == null) return returnValue;

        try {
            File f = new File(filename1);
            returnValue = f.exists();
        } catch (Exception e) {
            //log.info("Exception: " + e.getMessage());
            //e.printStackTrace();
            returnValue = false;
        }

        return returnValue;
    }

    /**
     * if the input string ends with an directory-seperator it is removed
     */
    public static String removeFinalDirectorySlash(String dir1) {
        String ret = dir1;
        if (ret.endsWith("/") || ret.endsWith("\\")) {
            int len = ret.length();
            ret = ret.substring(0, len - 1);
        }

        return ret;
    }

    /**
     * create the directory "dir1". Recursive Directory generation is supported.
     * e.g. Directory "c:\temp" exists. dir1 = "C:\temp\dir1\dir2\dir3\" is allowed.
     * if the String does not end with an directory-separator it is assumed that it
     * is a file. e.g c:\temp\dira\dirb\test creates c:\temp\dira\dirb\
     */
    public static void makeDirectory(String dir1) {
        if ((dir1 == null) || (dir1.trim().length() == 0)) {
            return;
        }
        dir1 = removeFileUrlPrefix(dir1);
        String directory = resolvePath(dir1);
        if (!directory.endsWith("\\") && !directory.endsWith("/")) {
            StringTokenizer tokens = new StringTokenizer(directory, "/\\", false);
            directory = "";
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                if (tokens.hasMoreTokens()) {
                    if (directory.length() > 0) {
                        directory += "/";
                    }
                    directory += token;
                }
            } // end while
        }  // end if last entry is a file

        File fileDescr = new File(directory);
        try {
            if (fileDescr.exists()) {
                if (fileDescr.isFile()) {
                    //log.severe("Unable to create directory \"" + directory + "\" because it is a file.");
                }
                // now it is a directory that already exists
                return;
            }

            if (!fileDescr.mkdirs()) {
                //log.warn("Unable to create directory \"" + directory + "\".");
            }
        } catch (Exception e) {
            //log.severe(e);
        }
    } // end method

    public static void deleteDir(String a_dir) {
        File f = new File(a_dir);

        if (f.exists() && f.isDirectory()) {
            f.delete();
        } else {
            // log.severe("Unable do delete file: \"" + a_dir + "\"");
        }
    }

    /**
     * if the path "relPath" is a relative path (not begins with a seperator or x:)
     * then the abs_path information is added,
     * so the out put is absPath + relPath
     */
    public static String relative2absolutePath(String relPath, String absPath, boolean isFile) {
        if (relPath == null)
            return relPath;

        if (absPath == null) {
            absPath = "";
        }
        boolean doublePoint = false;
        if ((relPath.length() >= 2) && (relPath.charAt(1) == ':'))
            doublePoint = true;
        //System.out.println("char: <" + relPath.charAt(1) + ">, bool: " + doublePoint);

        String ret = relPath;
        if (!isFile)
            relPath = setFinalDirectorySlash(relPath);

        if (!doublePoint && !relPath.startsWith("\\") && !relPath.startsWith("/")) {
            if (relPath.startsWith("./")) {
                relPath = relPath.substring(2);
            }
            ret = setFinalDirectorySlash(absPath) + relPath;
        }

        return ret;
    }

    public static String relative2absolutePath(String relPath, String absPath) {
        return relative2absolutePath(relPath, absPath, false);
    }


    /**
     * make an absolute path depending on the working directory
     * if the last entry specifies a directory that exists, a separator is added
     * if isDirectory1=true, a separator is always added
     * addapt the /, \ to /
     * example: ..\..\dummy.txt -> c:\test\dummy.txt
     * folder\dummy.txt -> c:\folder\dummy.txt
     *
     * @param filename1    Filename to check
     * @param isDirectory1 if true, at the last entry in the path is interpeted
     *                     as Directory (a slash will be added).
     */
    public static String resolvePath(String filename1, boolean isDirectory1) {
        String ret = filename1.trim();
        try {
            if (ret == null || ret.length() == 0) {
                return "";
            }

            // if UNIX: convert X:\something -> /something
            if ((File.separatorChar == '/') && (ret.length() >= 2) && (ret.charAt(1) == ':')) {
                ret = ret.substring(2);
            }

            if (!hasUrlPrefix(ret)) {
                File f = new File(ret);

                // cannonicalPAth do not work with uri
                // file://c:/temp -> d:\workingdir\file://c:/temp
                ret = f.getCanonicalPath();

                if (isDirectory1 || f.isDirectory()) {
                    ret = setFinalDirectorySlash(ret);
                }
            }

            ret = ret.replace('\\', '/');

            // if it is a file, try to locate it. Even if its in an jar-archive
            if (!isDirectory1 && !existsFile(ret)) {
                // try to load using the classloader
                ret = filename1;
                /*int baseDirLength = getWorkingDirectory().length();
                log.fine("Working directory: \"" + getWorkingDirectory() + "\"");
                // make the absolute ret relative, so the getResourse can be used
                if ((baseDirLength > 0) && (ret.length() > baseDirLength) && (ret.startsWith(getWorkingDirectory()))) {
                   ret = "/" + ret.substring(baseDirLength);
                }*/

                //log.fine("Looking with getResource for <" + ret + ">");
                URL fileUrl = fileTools.getClass().getResource(ret);
                //System.out.println("URL is null :<" + (fileUrl == null) + ">, File: " + ret);
                if (fileUrl != null) {
                    //log.fine("URL tostring:<" + fileUrl.toString() + ">, exist: " + existsFile(fileUrl.toString()));
                    //log.fine("URL file    :<" + fileUrl.getFile() + ">, exist: " + existsFile(fileUrl.getFile()));
                    //log.fine("URL ref     :<" + fileUrl.getRef() + ">, exist: " + existsFile(fileUrl.getRef()));
                    ret = fileUrl.toString();
                }
            }

        } catch (Exception e) {
            //Console.exitOnException(e);
            e.printStackTrace();
            System.err.println(e);
        }
        return ret;
    }

    public static String resolvePath(String filename1) {
        //default is no directory.
        // but if the filename ends with a slash, it is interpreded as path
        if (filename1 == null) {
            filename1 = "";
        }

        String filename = filename1.trim();
        boolean isPath = ((filename.endsWith("/")) || (filename.endsWith("\\")));

        return resolvePath(filename1, isPath);
    }

    /**
     * input is the string representation of a file, output is the java url class of this.
     * Note: if the filenName is relative, the class.getResource is involved to retrieve the
     * location
     */
    public static URL getFileURL(String fileName1, URL default1) {
        URL returnValue = default1;
        if (fileName1 == null) return returnValue;

        // first check, wheter it is already a valid url
        try {
            returnValue = new URL(setUrlPrefix(fileName1));
            return returnValue;
        } catch (MalformedURLException e) {
            //log.info(e);
            returnValue = default1;
        }


        try {
            String urlString = resolvePath(fileName1);

            returnValue = new URL(setUrlPrefix(urlString));
        } catch (MalformedURLException e) {
            //log.severe(e);
            returnValue = default1;
        }

        return returnValue;
    }

    /**
     * returns the absolute path of the currend working directory
     *
     * @return
     */
    public static String getWorkingDirectory() {
        if (currentWorkingDir == null) {
            File f = new File("");
            currentWorkingDir = setFinalDirectorySlash(f.getAbsolutePath());
        }

        return currentWorkingDir;
    }

    /**
     * set a new working directory
     */
    public static void setWorkingDirectory(String workingDir) {
        if (workingDir != null) {
            File f = new File(workingDir);
            currentWorkingDir = setFinalDirectorySlash(f.getAbsolutePath());
        } else {
            currentWorkingDir = null;
        }
    }

    /**
     * returns the absolute path of the temp directory
     *
     * @return
     */
    public static String getTempDirectory() {
        return setFinalDirectorySlash(System.getProperty("java.io.tmpdir"));
    }


    /**
     * returns the absolute path of the user home directory
     *
     * @return
     */
    public static String getUserHomeDirectory() {
        return setFinalDirectorySlash(System.getProperty("user.home"));
    }

    /**
     * returns the absolute path of the base dir where the class is located (classpath).
     *
     * @return
     */
    public static String getResourceBaseDirectory() {
        ResourceClass res = new ResourceClass();
        String returnValue = res.getBaseDir();
        return returnValue;
    }

    /**
     * search for all occurrencies of a String and replace it with an other String
     * input:
     * str1: source-String that will be processed
     * old1: string, that is search in str1
     * new1: old1 will be replaced by new1
     */
    public static String replace(String str1, String old1, String new1) {
        String str = str1;
        String oldStr = old1;
        String newStr = new1;

        if ((str == null) || (oldStr == null) || (newStr == null) ||
                (str.length() == 0) || (oldStr.length() == 0)) {
            return str1;
        }

        String result = "";

        int i = 0;
        while (i <= (str.length() - oldStr.length())) {
            String sub = str.substring(i, i + oldStr.length()); // part of the text-string
            if (sub.equals(oldStr)) {
                result += newStr;
                i += oldStr.length();
            } else {
                result += str.substring(i, ++i);
            }
        }  // end for
        // add the rest on the end if not already replaced;
        if (i <= str.length()) {
            result += str.substring(i, str.length());
        }


        return result;
    }  // end method


    /**
     * Converts "\temp\sample.txt" -> "file:///temp/sample.txt"
     */
    public static String setUrlPrefix(java.lang.String fileName1) {
        String returnValue = fileName1;
        if (fileName1 == null)
            return "file:/";

        if ((!fileName1.startsWith("file:")) &&
                (!fileName1.startsWith("http:")) &&
                (!fileName1.startsWith("jar:")) &&
                (!fileName1.startsWith("rmi:")) &&
                (!fileName1.startsWith("https:")) &&
                (!fileName1.startsWith("ftp:"))) {
            File file = new File(fileName1);
            returnValue = "file:/" + file.getAbsolutePath();
        }

        return returnValue;
    }

    /**
     * remove "file:" url prefix for file-urls. It is mainly used to get a valid path for
     * new File(myPath);
     */
    public static String removeFileUrlPrefix(java.lang.String fileName1) {
        String returnValue = fileName1;

        try {
            if (fileName1 == null) {
                return "";
            }


            //URI uri = new URI(fileName1);
            //returnValue = uri.getPath();  //get the filename and decode octed strings

            if (fileName1.startsWith("file:")) {

                // somethimes a file uri string is not encoded int the right way
                fileName1 = replace(fileName1, "[", "%5B");
                fileName1 = replace(fileName1, "]", "%5D");
                fileName1 = replace(fileName1, " ", "%20");

                String path = new URI(fileName1).getPath(); // path contains spaces, ...
                //String path = new URI(fileName1).toString();  //toString contains %20 ...
                // on a dos filename remove the leading slash. "/c:/temp" -> "c:/temp"
                if ((path.length() >= 2) && (path.charAt(2) == ':')) {
                    returnValue = path.substring(1);
                } else {
                    returnValue = path;
                }
            }

            /*
            if (fileName1.startsWith("file:///")) {
                returnValue = convertString(fileName1, 8);
            } else
            if (fileName1.startsWith("file://")) {
                returnValue = convertString(fileName1, 7);
            } else
            if (fileName1.startsWith("file:/")) {
                returnValue = convertString(fileName1, 6);
            } else
            if (fileName1.startsWith("file:")) {
                returnValue = convertString(fileName1, 5);
            }
            */
        } catch (Exception e) {
            System.out.println("file: " + fileName1);
            e.printStackTrace();
        }

        return returnValue;
    }

    private static String convertString(String url, int startPos) {
        String returnValue = url.substring(startPos);
        returnValue = returnValue.replaceAll("%20", " ");

        return returnValue;
    }

    /**
     * remove url prefix"jar:file:/"
     * example: "jar:file:/D:/tools"  -> "D:/tools"
     */
    public static String removeUrlPrefixes(java.lang.String fileName1) {
        String returnValue = fileName1;
        if (fileName1 == null)
            return "";

        //log.fine(" U input: " + returnValue);
        if (fileName1.startsWith("jar:file:")) {
            returnValue = fileName1.substring(9);
        }

        //log.fine(" U <jar:file:> : " + returnValue);

        // on windows systems remove the remaining "/x:"
        if (fileName1.charAt(2) == ':') {
            returnValue = fileName1.substring(3);
        }

        //log.fine(" U </X:> : " + returnValue);

        return returnValue;
    }


    public static boolean hasUrlPrefix(java.lang.String fileName1) {
        if (fileName1 == null)
            return false;

        String filenName = fileName1;
        return (setUrlPrefix(fileName1).equalsIgnoreCase(filenName));
    }

    /**
     * Read the content of a File and store it in a String
     */
    public static String readFromFile(String filename1, String default1, String l_locale) {
        String ret = "";
        if (l_locale == null || l_locale.equals("")) l_locale = "UTF-8";

        //log.fine("try reading: " + filename1);
        try {
            //BufferedReader inputStream = new BufferedReader(new FileReader(filename1));
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(filename1), l_locale));

            StringBuffer strBuff = new StringBuffer();
            while (inputStream.ready()) {
                String input = inputStream.readLine();
                if ((input != null)) {
                    input = input.trim();
                    strBuff.append(input + FileTools.LINE_BREAK);
                } // end if
            } // end while

            ret = strBuff.toString();

            if ((ret == null) || (ret.length() == 0)) {
                ret = default1;
            }
        } catch (Exception e) {
            //log.fine(e);
            ret = default1;
        }

        return ret;
    } // end method

    public static String readFromFile(String filename1, String default1) {
        return readFromFile(filename1, default1, null);
    }

    public static String readFromFileUtf8(String filename1, String default1) {
        return readFromFile(filename1, default1, "UTF-8");
    }

    public static String readFromStream(InputStream a_stream) {
        String returnValue = null;

        try {
            char buf[] = new char[BUFFER_SIZE];
            InputStreamReader reader = new InputStreamReader(a_stream); //TODO: , encoding);
            StringBuffer strBuff = new StringBuffer();
            while (reader.ready()) {
                String input;

                int availableBytes = reader.read(buf);
                input = new String(buf, 0, availableBytes);
                if ((input != null)) {
                    input = input.trim();
                    strBuff.append(input + FileTools.LINE_BREAK);
                } // end if
            }

            returnValue = strBuff.toString();
        } catch (IOException e) {
            //log.severe(e);
            e.printStackTrace();
        }

        return returnValue;
    }


    /**
     * read key-value pairs form a file. A line in the file looks like "key=value"
     *
     * @param fileName
     * @return
     */
    public static Map readMapFromFile(String fileName) {
        Map m = new HashMap();
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
            while (inputStream.ready()) {
                //get next key2-value pair from file
                String input = inputStream.readLine();
                if ((input != null) && (input.length() > 0)) {
                    input = input.trim();
                    String key = input.substring(0, input.indexOf("="));
                    String value = input.substring(input.indexOf("=") + 1, input.length());
                    m.put(key, value);
                }
            }
            inputStream.close();
            //Log.notice("Map loaded from file <" + fileName + ">");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return m;
    }


    /**
     * reads every line from fileName and assignes each line to a HashSet. The
     * created HashSEt is returned
     *
     * @param fileName
     * @param l_locale a locale character setting (e.g. UTF-8). If ommitted UTF-8 is assumed
     * @return
     */
    public static Set readSetFromFile(String fileName,
                                      String l_locale) throws IOException {
        Set m = new HashSet();
        if (l_locale == null || l_locale.equals("")) l_locale = "UTF-8";

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), l_locale));
        String l_line;
        while ((l_line = inputStream.readLine()) != null) {
            m.add(l_line.trim());
        }
        inputStream.close();
        //Log.notice("Map loaded from file <" + fileName + ">");
        return m;
    }

    /**
     * reads every line from fileName. Each line is splitted using string.split(l_string_split_filter)
     * and assignes each token to a HashSet. Null or empty strings are not assigned if l_keep_empty is false The
     * created HashSEt is returned
     *
     * @param fileName
     * @param l_locale a locale character setting (e.g. UTF-8). If ommitted UTF-8 is assumed
     * @return
     */
    public static Set readSetFromFileAndBreakUpLines(String fileName,
                                                     String l_locale,
                                                     String l_string_split_filter,
                                                     boolean l_keep_empty) throws IOException {
        Set m = new HashSet();
        if (l_locale == null || l_locale.equals("")) l_locale = "UTF-8";

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), l_locale));
        String l_line;
        while ((l_line = inputStream.readLine()) != null) {
            String[] tokens = l_line.trim().split(l_string_split_filter);
            for (int i = 0; i < tokens.length; i++) {
                if (l_keep_empty || !tokens[i].equals(""))
                    m.add(tokens[i]);


            }
        }
        inputStream.close();
        //Log.notice("Map loaded from file <" + fileName + ">");
        return m;
    }

    /**
     * read single line of a file and store each line in a separate list-entry
     *
     * @param fileName
     * @return
     */
    public static List readLinesFromFile(String fileName) {
        List returnValue = new ArrayList();
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
            while (inputStream.ready()) {
                //get next key2-value pair from file
                String input = inputStream.readLine();
                if ((input != null) && (input.length() > 0)) {
                    input = input.trim();

                    returnValue.add(input);
                }
            }
            inputStream.close();
            //Log.notice("Map loaded from file <" + fileName + ">");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public static List<String> readLinesFromStream(InputStream a_stream) {
        List<String> returnValue = new ArrayList<String>();

        try {
            char buf[] = new char[BUFFER_SIZE];
            InputStreamReader inr = new InputStreamReader(a_stream); //TODO: , encoding);
            BufferedReader reader = new BufferedReader(inr);
            StringBuffer strBuff = new StringBuffer();
            while (reader.ready()) {
                String input;

                input = reader.readLine();
                if ((input != null)) {
                    input = input.trim();
                    returnValue.add(input);
                } // end if
            }
        } catch (IOException e) {
            //log.severe(e);
            e.printStackTrace();
        }

        return returnValue;
    }


    // simple method that saves the given string to the given filename
    // any existing files are replaced
    public static void saveToFile(String fname, String data) {
        try {
            makeDirectory(fname);
            FileOutputStream fout = new FileOutputStream(fname);
            OutputStreamWriter stream_out = new OutputStreamWriter(fout);
            stream_out.write(data);
            stream_out.flush();
            stream_out.close();
        } catch (Exception e) {
            //log.severe(e);
        }
    }

    // simple method that saves the given string to the given filename
    // any existing files are replaced
    public static void saveToFile(String fname, String data, boolean append) {
        try {
            if (!append) {
                saveToFile(fname, data);
            } else {
                if (!existsFile(fname)) {
                    saveToFile(fname, data);
                } else {
                    FileOutputStream fout = new FileOutputStream(fname, true);
                    OutputStreamWriter stream_out = new OutputStreamWriter(fout);
                    stream_out.write("\n");
                    stream_out.write(data);
                    stream_out.flush();
                    stream_out.close();
                }
            }
        } catch (Exception e) {
            //log.severe(e);
        }
    }

    /**
     * simple method that saves the given byteArray to the given filename
     * any existing files are replaced
     */
    public static void saveToFile(String fname, byte[] data) {
        try {
            makeDirectory(fname);
            FileOutputStream fout = new FileOutputStream(fname);
            DataOutputStream streamOut = new DataOutputStream(fout);
            //ByteArrayOutputStream byteOut = new ByteArrayOutputStream(fout);

            streamOut.write(data);
            streamOut.flush();
            streamOut.close();
        } catch (Exception e) {
            //log.severe(e);
        }
    }

    /**
     * simple method that saves the given inputStrream to the given filename
     * any existing files are replaced
     */
    public static void saveToFile(String fname, InputStream inputStream) {
        try {
            makeDirectory(fname);
            if (inputStream != null) {
                FileOutputStream fout = new FileOutputStream(fname);
                DataOutputStream streamOut = new DataOutputStream(fout);
                //ByteArrayOutputStream byteOut = new ByteArrayOutputStream(fout);
                BufferedInputStream bin = new BufferedInputStream(inputStream);

                byte buf[] = new byte[BUFFER_SIZE];
                int availableBytes;

                while ((availableBytes = bin.read(buf)) > -1) {
                    streamOut.write(buf, 0, availableBytes);
                }
                bin.close();
                inputStream.close();
                streamOut.flush();
                streamOut.close();
            }
        } catch (Exception e) {
            //-------- silent catch -------------------
            //log.severe(e);
        }
    }

    // simple method that saves the given string to the given filename
    // any existing files are replaced
    public static void saveToFileUtf8(String fname, String data) {
        try {
            makeDirectory(fname);

            String filename = removeFileUrlPrefix(fname);
            File file = new File(filename);
            //File file = new File(new URI(setUrlPrefix(fname)));
            FileOutputStream fout = new FileOutputStream(file);
            OutputStreamWriter stream_out = new OutputStreamWriter(fout, "UTF-8");
            stream_out.write(data);
            stream_out.flush();
            stream_out.close();
            //log.fine("wrote in UTF-8: " + fname);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // checks whether "line1" contains the suppstring "varName1" (without a preceeding #
    // if not, null is returned otherwise  "value1" is returned
    public static String checkLine(String line1, String varName1, String value1) {
        String ret = null;

        line1.trim();
        if (line1.startsWith("#") || line1.startsWith(";") ||
                line1.startsWith("REM") || line1.startsWith("rem"))
            return ret;

        // create a substring without ending commends
        StringTokenizer enumerator = new StringTokenizer(line1, "#", false);
        String data = "";
        if (enumerator.hasMoreElements())
            data = (String) enumerator.nextElement();

        data.trim();
        //System.out.println("checking: <" + data + "> with <" + varName1 + ">");
        if (data.startsWith(varName1)) {
            ret = value1;
            //System.out.println("Line replaced with <" + ret + ">");
        }

        return ret;
    }

    // read all lines from the input-file and sets the corresponding entry
    // handles #13 and #10 right (DOS/UNIX files)
    public static void setFileEntry(String Filename1, String a_section, String[] varName1, String[] value1)
            throws java.io.IOException {

        String currentSection = "";

        File orgFile = new File(Filename1);

        File tempFile = File.createTempFile("ini", ".tmp", orgFile.getParentFile());
        //System.out.println("Tempfile: " + tempFile.getAbsolutePath());

        // write all names to a list. Entries are removed, when they are updated
        List updatedVars = new Vector(value1.length);
        for (int i = 0; i < value1.length; i++)
            updatedVars.add(value1[i]);

        FileInputStream fis = new FileInputStream(orgFile);
        //DataInputStream fin = new DataInputStream(fis);
        InputStreamReader fin = new InputStreamReader(fis, "UTF-8");
        BufferedReader in = new BufferedReader(fin);

        FileOutputStream fos = new FileOutputStream(tempFile);
        //DataOutputStream fout = new DataOutputStream(fos);
        OutputStreamWriter fout = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter out = new BufferedWriter(fout);

        String line = "";
        //while (fin.available() > 0) {
        while (line != null) {
            // read a line
            line = in.readLine();

            //-------------------- now we have an input-line

            //update the line if needed
            if (line.startsWith("[")) {
                // on a new section we write new variables at the end of the sectoin
                if (currentSection.equalsIgnoreCase(a_section)) {
                    while (!updatedVars.isEmpty()) {
                        String str = (String) updatedVars.remove(0);
                        out.write(str + "\n");
                    }
                }

                currentSection = line.substring(1, line.length() - 1);
            } else {
                if (currentSection.equalsIgnoreCase(a_section)) {
                    for (int i = 0; i < varName1.length; i++) {
                        String newLine = checkLine(line, varName1[i], value1[i]);
                        if (newLine != null) {
                            // write the line with the new value
                            line = newLine;
                            if (updatedVars.contains(value1[i]))
                                updatedVars.remove(value1[i]);
                        }
                    } // end for
                }
            }

            // write the line to the outputstream
            out.write(line + "\n");

        } // end while fin reading


        // write new Variables at the end of the file
        if (!updatedVars.isEmpty() && !currentSection.equalsIgnoreCase(a_section)) {
            fout.write("\n");

            String str = "[" + a_section + "]";
            fout.write(str + "\n");
        }

        while (!updatedVars.isEmpty()) {
            String str = (String) updatedVars.remove(0);
            fout.write(str + "\n");
        }

        out.close();
        fout.close();
        fos.close();

        in.close();
        fin.close();
        fis.close();
        fin = null;
        fis = null;

        // delete the original file and rename the tempfile
        replaceFile(Filename1, tempFile.getAbsolutePath(), true);
    }  // end method

    public static void setFileEntry(String Filename1, String a_section, String a_varName, String a_value)
            throws java.io.IOException {

        String[] sectionArray = new String[1];
        String[] varArray = new String[1];
        String[] valueArray = new String[1];

        sectionArray[0] = a_section;
        varArray[0] = a_varName;
        valueArray[0] = a_value;
        setFileEntry(Filename1, a_section, varArray, valueArray);
    }

    // delete originalFila and rename newFile to originalFile-name
    public static void replaceFile(String originalFile1, String newFile1,
                                   boolean makeBackup1) {
//      try {
        File orgFile = new File(originalFile1);
        File newFile = new File(newFile1);
        //File tempFile = new File(orgFile.getName() + ".bak");
        File tempFile = new File(originalFile1 + ".bak");

        if (!newFile.exists()) {
            System.err.println("File " + newFile.toString() + " does not exitst");
            return;
        }

        // save the original file to .bak
        if (orgFile.exists()) {
            // prepare the tempFile (delete if exists)
            if (makeBackup1) {
                if (tempFile.exists()) {
                    if (!tempFile.delete()) {
                        System.err.println("could not delete file " + tempFile.toString());
                        return;
                    }
                }

                if (!orgFile.renameTo(tempFile)) {
                    System.err.println("\ncould not rename file " + orgFile.toString() +
                            " to " + tempFile.toString());
                    return;
                }
            } else {
                if (!orgFile.delete()) {
                    System.err.println("could not delete file " + orgFile.toString());
                    return;
                }
            } // end if makeBackup
        } // end if orgFile exists

        if (!newFile.renameTo(orgFile)) {
            System.err.println("could not rename file " + newFile.toString() +
                    " to " + orgFile.toString());
            return;
        }
//      } catch {

//      }
    }

    /**
     * delete a single File called fileName.
     * Note: placeholders like *.java are allowed
     */
    public static void deleteFile(String fileName1) {
        if ((fileName1 == null) || (fileName1.trim().length() == 0)) {
            return;
        }

        try {
            File orgFile = new File(fileName1);

            if (orgFile.exists()) {
                orgFile.delete();
            }
        } catch (Exception e) {
            //log.severe(e);
            e.printStackTrace();
        }
    } // end method


    /**
     * @param path1 The pathname to parse
     * @return a list with all path names. e.g. "/temp/dummy/joe.jpg" -> {"temp" ,"dummy", "joe"}
     */
    public static String[] parsePathName(String path1) {
        String[] returnValue = new String[0];

        StringTokenizer tokens = new StringTokenizer(path1, "/\\", false);

        returnValue = new String[tokens.countTokens()];
        int counter = 0;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            returnValue[counter] = token;
            counter++;
        } // end while


        return returnValue;
    }

    /**
     * retrieve a list of all subdirs in the path
     *
     * @param a_fileSettings containing the startdirectoy ind filter patterns
     * @return a list of all mathing directoryies - no files
     */
    public static String[] subDirList(FileParserSettings a_fileSettings) {
        String path1 = a_fileSettings.getStartDirectoryList().get(0);
        //log.fine("path: <" + path1 + ">");
        String[] returnValue = new String[0];

        if (path1 == null) {
            return returnValue;
        }

        try {
            File path = new File(FileTools.removeFileUrlPrefix(path1));
            File[] list;

            //list = path.listFiles();
            list = path.listFiles((FileFilter) new DirFilter(a_fileSettings));
            //list = path.listFiles((FileFilter) null);
            if (list == null) {
                list = new File[0];
            }

            List resultList = new Vector();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isDirectory()) {
                    resultList.add(list[i].getName());
                }
            }

            returnValue = (String[]) resultList.toArray(new String[resultList.size()]);
        } catch (Exception e) {
            //log.severe(e);
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * replace invalid characters filename that are not allowed in
     * filenames. Note: ":" is replaced. so don't use it with absolute paths
     * like "c:\test". The same is with "\", "/"
     */
    public static String checkFilename(String filename1) {
        String ret = filename1;
        ret = ret.replace(':', '_');
        ret = ret.replace('\\', '_');
        ret = ret.replace('/', '_');
        ret = ret.replace(';', '_');
        ret = ret.replace('*', '_');
        ret = ret.replace('?', '_');
        ret = ret.replace('\"', '_');
        ret = ret.replace('>', '_');
        ret = ret.replace('<', '_');
        ret = ret.replace('|', '_');

        return ret;
    }

    /**
     * retrieve the directory of the file. unchanged if it is a directory, otherwise the
     * directory wihtout the filename
     */
    public static String getFilePath(String file1) {
        String returnValue = file1;

        if (file1 == null) {
            return "";
        }

        /*File path = new File(file1);

        if (path.isFile()) {
           returnValue = path.getPath();
        }

        returnValue = resolvePath(returnValue, true);*/

        if (returnValue.endsWith("/") || returnValue.endsWith("\\")) {
            return returnValue;
        }

        StringTokenizer tokens = new StringTokenizer(file1, "/\\", true);
        String dummy = "";
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (tokens.hasMoreTokens()) {
                dummy += token;
            }
        }
        returnValue = dummy;

        return returnValue;
    }

    /**
     * retrieve the name of the file without the directory prefix.
     * if it is a directory or null then "" is returned;
     */
    public static String getFilename(String fileWithPath1) {
        String returnValue = "";

        if (fileWithPath1 == null || fileWithPath1.length() < 1) {
            return returnValue;
        }

        try {
            File file;
            file = new File(fileWithPath1);

            //if (file.isFile()) {
            //returnValue = file.getName();
            String path = getFilePath(fileWithPath1);
            returnValue = fileWithPath1.substring(path.length());
            //}
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        return returnValue;
    }


    /**
     * input: path+filter, e.g. C:/temp/*.bak
     * ouput: list with all matching files e.g. c:/temp/test.bak, ....  - no directories
     * Note: does not go recusive iinto subfolders
     */
    public static String[] fileList(FileParserSettings a_fileSettings) {
        //log.fine("path: <" + path1 + ">");
        //log.fine("filter: <" + fileFilter1 + ">");

        File path = new File(FileTools.removeFileUrlPrefix(a_fileSettings.getStartDirectoryList().get(0)));
        String[] list;

        list = path.list(new DirFilter(a_fileSettings));
        if (list == null) {
            list = new String[0];
        }
        //Arrays.sort(list, new AlphabeticComparator());
        //for(int i = 0; i < list.length; i++)

        return list;
    }


    /** return a list of all Files of in the subdirectory
     *  todo: recursive parsing is not cmpletly implemented
     * @param fileSettings containing startdirs, filefilter, excludelist
     * @param walkRecusive if true, subdirectories are scanned, too
     * @return
     */
    /*public static List fileList(FileParserSettings fileSettings, boolean walkRecusive) {
        //log.fine("path: <" + path1 + ">");
        //log.fine("filter: <" + fileFilter1 + ">");
        List returnValue = new Vector(10, 100);

        try {
            List pathList = new Vector(0, 10);
            for (String path : fileSettings.getFileFilterList()) {
                pathList.add(setFinalDirectorySlash(path));
            }

            if (walkRecusive) {
                String[] dirList = subDirList(path);
                for (int i = 0; i < dirList.length; i++) {
                    String s = dirList[i];
                    pathList.add(path1 + s);
                }
            }

            for (int i = 0; i < pathList.size(); i++) {
                String s = (String) pathList.get(i);
                s = setFinalDirectorySlash(s);
                String[] fList = fileList(s, fileFilter1);
                for (int j = 0; j < fList.length; j++) {
                    String file = fList[j];
                    returnValue.add(s + file);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return returnValue;
    }*/


    /**
     * Delete a set of files. E.g "c:\temp\*.bak"
     */
    public static void deleteFiles(String path1, String fileFilter1) {
        if ((path1 == null) || (path1.trim().length() == 0)) {
            //log.fine("path is NULL. Nothing will be deleted");
            return;
        }

        if (fileFilter1 == null) {
            //log.fine("fileFilter is NULL. Nothing will be deleted");
            return;
        }


        String[] fileList = fileList(new FileParserSettings(path1, fileFilter1));
        //log.fine(fileList.length + " files found to delete");
        for (int i = 0; i < fileList.length; i++) {
            //log.fine("delete: \"" + fileList[i] + "\"");
            deleteFile(setFinalDirectorySlash(path1) + fileList[i]);
        }
    } // end method

    /**
     * true if fileName is a directory
     */
    public static boolean isDirectory(String fileName1) {
        boolean ret = false;

        try {
            File orgFile = new File(fileName1);

            ret = orgFile.isDirectory();
        } catch (Exception e) {
            //log.severe(e);
            e.printStackTrace();
        }

        return ret;
    } // end method


    public static void copyFile(String source1, String dest1)
            throws java.io.FileNotFoundException,
            java.io.IOException {
        BufferedInputStream fis = new BufferedInputStream(new FileInputStream(source1));
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(dest1));

        byte buf[] = new byte[BUFFER_SIZE];
        int availableBytes;

        while ((availableBytes = fis.read(buf)) > -1) {
            fos.write(buf, 0, availableBytes);
        }

        fis.close();
        fos.flush();
        fos.close();
    }

    /**
     * copies the file from an inputstream to a file
     *
     * @param source1
     * @param dest1
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void copyStream(InputStream source1, String dest1)
            throws java.io.FileNotFoundException,
            java.io.IOException {
        BufferedInputStream fis = new BufferedInputStream(source1);
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(dest1));

        byte buf[] = new byte[BUFFER_SIZE];
        int availableBytes;

        while ((availableBytes = fis.read(buf)) > -1) {
            fos.write(buf, 0, availableBytes);
        }

        fis.close();
        fos.flush();
        fos.close();
    }

    /**
     * copies the file from an inputstream to a String
     */
    public static String copyStreamToString(InputStream a_stream, String defaultValue) {
        String returnValue = defaultValue;

        try {
            InputStreamReader in = new InputStreamReader(a_stream, "UTF-8");

            char buf[] = new char[BUFFER_SIZE];
            int availableBytes;

            StringBuffer strBuf = new StringBuffer();
            while ((availableBytes = in.read(buf)) > -1) {
                in.read(buf, 0, availableBytes);

                //String str = new String(buf, 0, availableBytes);
                strBuf.append(buf);
            }
            returnValue = strBuf.toString();
        } catch (Exception e) {
            //log.severe(e);
            e.printStackTrace();
        }
        return returnValue;
    }


    // copies a file from the jarfile to a tempdirectory and returns the path
    public static String extractFromJarFile(String jarFile1, String filename1)
            throws java.io.IOException {
        String filename = filename1;
        JarFile file = new JarFile(jarFile1);
        ZipEntry theEntry = new ZipEntry(filename);
        InputStream in = file.getInputStream(theEntry);
        if (in == null) {
            if (!noDebug)
            //log.fine("no entry: " + filename);
                filename = filename.replace('/', '\\');
            if (!noDebug)
            //log.fine("try with: " + filename);
                theEntry = new ZipEntry(filename);
            in = file.getInputStream(theEntry);
        }

        String tempDir = System.getProperty("user.dir");
        filename = filename.replace('\\', '/');
        File f = new File(filename);
        String name = f.getName();
        //log.fine("Property-Name: <" + name + ">");
        String destination = setFinalDirectorySlash(tempDir) + name;

        if (!noDebug) {
            //log.fine("Jar: " + jarFile1 + ", " + FileTools.existsFile(jarFile1));
            //log.fine("name: " + filename);
            //log.fine(in.toString());
            //log.fine("" + in.available());
            //log.fine(destination);
        }

        if (in != null) {
            copyStream(in, destination);
            in.close();
            in = null;
        } else {
            if (!noDebug) {
                //log.severe("unable to locate entry \"" + filename + "\" in archive \"" +
                //        jarFile1 + "\"");
            }

            File newFile = new File(destination);
            newFile.createNewFile();
        }

        return destination;
    }


    /**
     * open a Filedialog box, select a single file and return the name *
     */
    private static String showFileDialog(String directory1, String[] extension1,
                                         String[] description1, boolean addAll1,
                                         boolean isOpenDialog, String defaultValue1) {
        String returnValue = defaultValue1;

        JFileChooser chooser = new JFileChooser();

        if (!addAll1) {
            chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());
        }

        ExampleFileFilter filter = null;

        /*
        if (addAll1) {
           filter = new ExampleFileFilter();
           filter.addExtension("*");
           filter.setDescription("All Files");
           chooser.setFileFilter(filter);
        }
        */


        for (int i = 0; i < extension1.length; i++) {
            filter = new ExampleFileFilter();
            filter.addExtension(extension1[i]);
            filter.setDescription(description1[i]);
            chooser.setFileFilter(filter);
        }

        chooser.setCurrentDirectory(new File(directory1));

        int actionPerformed;
        if (isOpenDialog) {
            actionPerformed = chooser.showOpenDialog(null);
        } else {
            actionPerformed = chooser.showSaveDialog(null);
        }
        //log.fine("action: " + actionPerformed + ", APP: " + JFileChooser.APPROVE_OPTION);


        if (actionPerformed == JFileChooser.APPROVE_OPTION) {
            File filename = chooser.getSelectedFile();
            try {
                returnValue = filename.getCanonicalPath();
            } catch (Exception e) {
                //log.severe(e);
            }

            //System.out.println("File: " + returnValue);
        }

        //log.fine("File: " + returnValue);
        return returnValue;
    }

    /**
     * open a Filedialog box, select a single file and return the name *
     */
    public static String showOpenDialog(String directory1, String[] extension1,
                                        String[] description1, boolean addAll1,
                                        String defaultValue1) {
        return showFileDialog(directory1, extension1, description1, addAll1, true, defaultValue1);
    }


    /**
     * open a Filedialog box, select a single file and return the name *
     */
    public static String showOpenDialog(String directory1, String extension1,
                                        String description1, boolean addAll1,
                                        String defaultValue1) {
        String[] extension = new String[1];
        String[] description = new String[1];

        extension[0] = extension1;
        description[0] = description1;

        return showOpenDialog(directory1, extension, description, addAll1, defaultValue1);
    }

    /**
     * open a Save file Filedialog box, select a single file and return the name *
     */
    public static String showSaveDialog(String directory1, String[] extension1,
                                        String[] description1, boolean addAll1,
                                        String defaultValue1) {
        return showFileDialog(directory1, extension1, description1, addAll1, false, defaultValue1);
    }

    /**
     * open a Filedialog box, select a single file and return the name *
     */
    public static String showSaveDialog(String directory1, String extension1,
                                        String description1, boolean addAll1,
                                        String defaultValue1) {
        String[] extension = new String[1];
        String[] description = new String[1];

        extension[0] = extension1;
        description[0] = description1;

        return showSaveDialog(directory1, extension, description, addAll1, defaultValue1);
    }


    public static void removeOddLinesFromFile(String input1, String output1) {
        try {
            if (!existsFile(input1)) {
                return;
            }

            File file = new File(output1);

            BufferedReader fis = new BufferedReader(new FileReader(input1));
            //BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(target));
            BufferedWriter fos = new BufferedWriter(new FileWriter(output1));

            String line;
            boolean doInsert = true;
            while ((line = fis.readLine()) != null) {
                line = line.trim();

                if (doInsert) {
                    fos.write(line);
                    fos.newLine();
                }

                doInsert = !doInsert;
            }

            fis.close();
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end method

    // throw exeptions
    public static InputStream openFile(String a_fileName) throws FileNotFoundException {
        InputStream returnValue = null;

        if (a_fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        if (!existsFile(a_fileName)) {
            throw new UnsupportedOperationException("file \"" + a_fileName + "\" does'nt exist");
        }

        String fileName = a_fileName;
        if ((fileName != null) && (existsFile(a_fileName))) {
            FileInputStream fis = new FileInputStream(fileName);
            returnValue = new DataInputStream(fis);
        }


        return returnValue;
    }

    /**
     * open the given file an returns the DataInputStream. Does not throw but display exceptions
     */
    public static InputStream openFile(String a_fileName, InputStream defaultInputStream) {
        InputStream returnValue = defaultInputStream;

        try {
            returnValue = openFile(a_fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }


    /**
     * returns the suffix if a file. E.g. "test.txt" will return "TXT"
     *
     * @return returns the suffix of a file in upper case, e.g. "TXT", "PDF", ...
     */
    public static String getFileSuffix(String a_filename) {
        String returnValue = "";

        if (a_filename != null) {
            StringTokenizer tokens = new StringTokenizer(a_filename, ".", false);
            String lastToken = returnValue;
            while (tokens.hasMoreTokens()) {
                lastToken = tokens.nextToken();
            }
            returnValue = lastToken;
        }

        returnValue = returnValue.toUpperCase();
        return returnValue;
    }

    /**
     * returns the prefix if a file. E.g. "test.txt" will return "test"
     *
     * @return returns the prefix of a file in upper case, e.g. "filename"
     */
    public static String getFilePrefix(String a_filename) {
        String returnValue = "";

        String suffix = getFileSuffix(a_filename);
        returnValue = a_filename.substring(0, a_filename.length() - suffix.length() - 1);

        return returnValue;
    }

    /**
     * return the domain for this uri.
     * For web addresses this is the main domain. e.g. "http://www.google.at"
     *
     * @param a_uri
     * @return the domain for this uri.
     */
    public static String getDomain(String a_uri) {
        String returnValue = null;
        try {
            URI uri = new URI(a_uri);
            String host = uri.getHost();
            int port = uri.getPort();
            String scheme = uri.getScheme();
            String authority = uri.getAuthority(); // e.g. c: for files
            String userInfo = uri.getUserInfo();
            String query = null;
            String fragement = null;
            URI root = new URI(scheme, authority, "/", query, fragement);
            returnValue = root.toString();
        } catch (URISyntaxException e) {
            //log.severe(e);
            e.printStackTrace();
        }

        return returnValue;
    }
}
