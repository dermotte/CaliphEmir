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
package at.lux.fotoretrieval;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class FileOperations {

    public static String[] getAllDescriptions(File directory, boolean descendIntoSubDirectories) throws IOException {
        Vector v = new Vector();
        File[] f = directory.listFiles();
        for (int i = 0; i < f.length; i++) {
            File file = f[i];
            if (file != null && file.getName().endsWith(".mp7.xml")) {
                v.add(file.getCanonicalPath());
            }

            if (descendIntoSubDirectories && file.isDirectory()) {
                String[] tmp = getAllDescriptions(file, true);
                if (tmp != null) {
                    for (int j = 0; j < tmp.length; j++) {
                        v.add(tmp[j]);
                    }
                }
            }
        }
//        for (int i = 0; i < f.length; i++) {
//            System.out.println(f[i].toString());
//        }
        if (v.size() > 0)
            return (String[]) v.toArray(new String[1]);
        else
            return null;
    }
}
