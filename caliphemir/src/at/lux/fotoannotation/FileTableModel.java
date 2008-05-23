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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at)
 * http://www.juggle.at, http://caliph-emir.sourceforge.net
 */
package at.lux.fotoannotation;

import at.lux.fotoannotation.utils.ImageFileFilter;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.Arrays;
import java.util.Vector;

public class FileTableModel extends AbstractTableModel {
    private File directory;
    private int rowCount = 0;
    private ImageFileFilter iff = new ImageFileFilter();
    private int countRoots;
    // buffern der roots aus Performancegruenden ...
    private File[] roots;
    private String[] rootNames;
    private String[] currentFiles = null;
    boolean currentIsDirectory = false;

    public FileTableModel(File directory) {
        roots = File.listRoots();
        this.directory = directory;
        countRoots = roots.length;
        rootNames = new String[countRoots];
        for (int i = 0; i < rootNames.length; i++) {
            rootNames[i] = roots[i].toString();
        }
        currentIsDirectory = directory.isDirectory();
        if (currentIsDirectory) {
            currentFiles = getNames(directory);
            rowCount = currentFiles.length + 1 + countRoots;
        }
    }

    public int getColumnCount() {
        return 1;
    }

    public int getRowCount() {
        return rowCount;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex > 0)
            return null;
        else if (rowIndex > File.listRoots().length)
            return currentFiles[rowIndex - 1 - countRoots];
        else if (rowIndex > 0)
            return "[" + rootNames[rowIndex - 1] + "]";
        else
            return "..";

    }

    public void setNewDirectory(File f) {
        this.directory = f;
        currentIsDirectory = directory.isDirectory();
        if (currentIsDirectory) {
            currentFiles = getNames(directory);
            rowCount = currentFiles.length + 1 + countRoots;
        } else {
            rowCount = 1 + countRoots;
        }
    }

    /**
     * Returns "Files"
     */
    public String getColumnName(int column) {
        //return super.getColumnName(column);
        return "Files";
    }

    /**
     * Sort it like this: directories first, files after that ...
     *
     * @param f parent directory
     * @return array of Strings
     */
    private String[] getNames(File f) {
        File[] current = directory.listFiles(iff);
        Vector files = new Vector();
        Vector directories = new Vector();

        for (int i = 0; i < current.length; i++) {
            if (current[i].isDirectory())
                directories.add(current[i].getName());
            else
                files.add(current[i].getName());
        }
        String[] dirNames = (String[]) directories.toArray(new String[directories.size()]);
        String[] fileNames = (String[]) files.toArray(new String[files.size()]);
        Arrays.sort(dirNames);
        Arrays.sort(fileNames);
        String names[] = new String[dirNames.length + fileNames.length];
        int position = 0;
        for (int i = 0; i < dirNames.length; i++) {
            names[position] = dirNames[i];
            position++;
        }
        for (int i = 0; i < fileNames.length; i++) {
            names[position] = fileNames[i];
            position++;
        }
        return names;
    }
}
