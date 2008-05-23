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



import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.logging.Logger;

/**
 * Description:
 * Implementation fo the FileFilter, FilenameFilter interfaces for the java.if.File classe.
 * Use this class when you want
 * a list of e.g. all *.java files in a directory.
 * This class is used in java.io.File.list(new DirFilter(...))
 * Note: This is a implementation using the java regex implementation.
 * ATTENTION: not complete tested/implemented. not all patterns may work. Use on own risk (:
 *
 * @author Werner Klieber
 * @version 1.0
 */
public class DirFilter implements FileFilter, FilenameFilter {
    static Logger log = Logger.getLogger(DirFilter.class.getName());

    private String includeFileFilter; // all patterns for file to include.e.g."*.*"
    private String excludeFileFilter; // all file-patterns to exclude. e.g. "*.bak"
    private String excludeDirFilter;  // all dirs to exclude. e.g "cvs/"

    public DirFilter(FileParserSettings a_fileSettings) {
        includeFileFilter = a_fileSettings.getIncludeFileFilterRegex();
        excludeFileFilter = a_fileSettings.getExcludeFileFilterRegex();
        excludeDirFilter = a_fileSettings.getIncludeDirFilterRegex();
    } // end method


    /**
     * Tests if a specified file should be included in a file list.
     * This method is called when using the filenameFilter interface
     *
     * @param dir  the directory in which the file was found.
     * @param name the name of the file.
     * @return <code>true</code> if and only if the name should be
     *         included in the file list; <code>false</code> otherwise.
     */
    public boolean accept(File dir, String name) {
        return checkFileName(name);
    }

    /**
     * check one single filename like "document.txt"
     *
     * @param name
     * @return return true if the filename should be accepted
     */
    private boolean checkFileName(String name) {
        boolean returnValue;
        returnValue = name.matches(includeFileFilter);

        if ((returnValue) && (excludeFileFilter.length() > 0)) {
            returnValue = !name.matches(excludeFileFilter);
        }

        log.fine(returnValue + "(" + includeFileFilter + "): \"" + name + "\"");
        return returnValue;
    }


    /**
     * Tests whether or not the specified abstract pathname should be
     * included in a pathname list.
     * This method implements the FileFilter-Interface
     *
     * @param a_pathname The abstract pathname to be tested
     * @return <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    public boolean accept(File a_pathname) {
        boolean returnValue = false;

        if (a_pathname.isFile()) {
            returnValue = checkFileName(a_pathname.getName());
        } else {
            String f = a_pathname.toString() + "/";
            if (excludeDirFilter.length() == 0) {
                returnValue = true; // by default all directories are included
            } else {
                f = f.replace('\\', '/');
                returnValue = !f.matches(excludeDirFilter);
            }
            log.fine("DIR " + returnValue + " (" + excludeDirFilter + "): \"" + f + "\"");
        }

        return returnValue;
    } // end method
} // end class