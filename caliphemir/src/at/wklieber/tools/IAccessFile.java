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

import java.util.List;

/**
 * @author Werner Klieber
 * @version 1.0
 */

public interface IAccessFile {
    public void open(String file)
            throws Exception;

    public void close()
            throws Exception;

    //some file-type don't support section (e.g [general]) like batch-files
    //in this case, the section-string is ignored
    public boolean isSectionSupported();

    // retrieve one property, even if more with the same key exists
    public String getProperty(String section1, String key1, String defaultKey1);

    public int getProperty(String section1, String key1, int defaultKey1);

    public boolean getProperty(String section1, String key1, boolean defaultKey1);

    /**
     * return all matching entries
     * section must be unique
     */
    public String[] getProperties(String section1, String key1, String[] defaultKey1);

    /**
     * make your own xpath-query
     *
     * @return a list of all matching jdom-elements
     */
    public List getProperties(String xpathQuery1);

    // set the proptery with the name key1
    public void setProperty(String section1, String key1, String value1)
            throws Exception;

    // set a list of properties with the name key1
    // any other existing properties with the same location are deleted
    public void setProperties(String section1, String key1, List value1)
            throws Exception;
}