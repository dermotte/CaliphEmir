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

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all input setting needed to parse file-hierarchies:
 * This includes list for start paths, filename-pattern and exclude lists.
 * - startDirectoryList: The start directories where to start loading. The directory is parsed recursive
 * - fileFilterList:     the Filefilters to use: e.g "*.doc", "*.txt"
 * - excludeFilterList:  patterns for files and directories to exclude: e.g. "*.bak", "CVS/", "c:/temp/bigfile.tmp"
 * - logic to get the regex pattern from the filters for usage within java.lang.String.matches(regex)
 * NOTE: Directory-pattern ends with a "/" or "\". Complicated pattern like "*tree*.tmp" are not cmpletly tested yet
 * <p/>
 * User: wklieber
 * Date: 29.05.2004
 * Time: 18:25:08
 */
public class FileParserSettings {
    List<String> startDirectoryList;
    List<String> fileFilterList;
    List<String> excludeFilterList;
    private String regexFilterDir;  //regex pattern for directories only
    private String regexIncludeFilterFile; //regex filters for filenames only
    private String regexExcludeFilterFile;

    public FileParserSettings(List<String> a_startDirectoryList,
                              List<String> a_fileFilterList,
                              List<String> a_excludeFilterList) {
        init(a_startDirectoryList, a_fileFilterList, a_excludeFilterList);
    }

    public FileParserSettings(String a_directory, String a_fileFilter) {
        List<String> dirs = new ArrayList<String>();
        List<String> includes = new ArrayList<String>();
        List<String> excludes = new ArrayList<String>();
        dirs.add(a_directory);
        includes.add(a_fileFilter);

        init(dirs, includes, excludes);
    }

    public FileParserSettings(String a_directory,
                              List<String> a_fileFilterList,
                              List<String> a_excludeFilterList) {
        List<String> dirs = new ArrayList<String>();
        dirs.add(a_directory);

        init(dirs, a_fileFilterList, a_excludeFilterList);
    }

    private void init(List<String> a_startDirectoryList,
                      List<String> a_fileFilterList,
                      List<String> a_excludeFilterList) {

        if (a_startDirectoryList == null) {
            throw new UnsupportedOperationException("Directory path not specified (is null)");
        }
        if (a_startDirectoryList.size() == 0) {
            throw new UnsupportedOperationException("Directory path not specified (list is empty)");
        }
        if (a_startDirectoryList.size() > 1) {
            throw new UnsupportedOperationException("(Not implemented) At the moment just one start directory is allowed.");
        }


        if (a_fileFilterList == null) {
            throw new UnsupportedOperationException("file filter list(*.hmtl, ...) not specified (is null)");
        }
        if (a_fileFilterList.size() == 0) {
            throw new UnsupportedOperationException("file filter list(*.hmtl, ...) not specified (list is empty)");
        }

        startDirectoryList = adaptFilterList(a_startDirectoryList);
        excludeFilterList = adaptFilterList(a_excludeFilterList);
        fileFilterList = adaptFilterList(a_fileFilterList);

        //chech that all startdirectories exists and are absolute
        List<String> newStartList = new ArrayList<String>(startDirectoryList.size());
        for (String dir : startDirectoryList) {
            String absDir;
            absDir = FileTools.removeFileUrlPrefix(dir);
            absDir = FileTools.resolvePath(absDir, true);
            if (!FileTools.exists(absDir)) {
                throw new UnsupportedOperationException("Start directory \"" + dir + "\" does not exist.");
            }
            newStartList.add(absDir);
        }
        startDirectoryList = newStartList;

        regexIncludeFilterFile = "";
        regexExcludeFilterFile = "";
        regexFilterDir = "";

        // add filname include pattern
        for (String s : fileFilterList) {
            makeFilenameFilterRegex(s, false);
        }

        //add filters for exclude. can be either filnames or directories
        for (String s : excludeFilterList) {

            if (s.endsWith("/")) {
                // filter for dirs
                makeDirectoryFilterRegex(s);
            } else {
                makeFilenameFilterRegex(s, true);
            }
        }
    }

    private void makeDirectoryFilterRegex(String s) {
        String regex = s;
        if (regexFilterDir.length() > 0) {
            regexFilterDir += "|";
        }
        regex = regex.replace(".", "\\.");
        regex = regex.replace("*", ".*");
        if (regex.startsWith("/")) {
            regex = "^" + regex; // match at the beginning of the line
        } else {
            regex = ".*" + regex + "$"; //match at the end of the line
        }
        regexFilterDir += regex;
    }

    /**
     * make regex pattern for filnames that can be used for the java-regex implementation:
     * e.g. "*.jpg" will return ".*\.jpg"
     *
     * @param s
     */
    private void makeFilenameFilterRegex(String s, boolean doExclude) {
        String returnValue = s;

        if (returnValue.equals("*.*")) returnValue = "*";
        returnValue = returnValue.replace(".", "\\.");
        returnValue = returnValue.replace("*", ".*");
        if (doExclude) {
            if (regexExcludeFilterFile.length() > 0) {
                regexExcludeFilterFile += "|";
            }
            regexExcludeFilterFile += returnValue;
        } else {
            if (regexIncludeFilterFile.length() > 0) {
                regexIncludeFilterFile += "|";
            }
            regexIncludeFilterFile += returnValue;
        }
    }

    /**
     * return a normalized list: all back-slashes will be removed by slashes
     *
     * @param a_filterList
     * @return
     */
    private List<String> adaptFilterList(List<String> a_filterList) {
        List<String> returnValue = new ArrayList<String>();
        if (a_filterList != null) {
            for (String s : a_filterList) {
                if (s != null) {
                    s = s.replace('\\', '/');
                    returnValue.add(s);
                }
            }
        }
        return returnValue;
    }

    public List<String> getStartDirectoryList() {
        return startDirectoryList;
    }

    public List<String> getFileFilterList() {
        return fileFilterList;
    }

    public List<String> getExcludeFilterList() {
        return excludeFilterList;
    }

    /**
     * @return all regex pattern for filnames to include
     */
    public String getIncludeFileFilterRegex() {
        return regexIncludeFilterFile;
    }

    /**
     * @return all regex pattern for filnames to exclude
     */
    public String getExcludeFileFilterRegex() {
        return regexExcludeFilterFile;
    }

    /**
     * @return all regex pattern for dirs to exclude
     */
    public String getIncludeDirFilterRegex() {
        return regexFilterDir;
    }
}
