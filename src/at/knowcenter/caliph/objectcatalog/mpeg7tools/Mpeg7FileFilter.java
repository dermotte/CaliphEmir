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
 * (c) 2002-2005 by Mathias Lux (mathias@juggle.at) and the Know-Center Graz
 * Inffeldgasse 21a, 8010 Graz, Austria
 * http://www.know-center.at
 */
package at.knowcenter.caliph.objectcatalog.mpeg7tools;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 29.08.2002
 *         Time: 16:04:06
 */
public class Mpeg7FileFilter extends FileFilter {
    /**
     * Whether the given file is accepted by this filter.
     */
    public boolean accept(File f) {
        boolean accepted = false;
        if (f.getName().endsWith(".mp7.xml"))
            accepted = true;
        if (f.getName().endsWith(".mp7"))
            accepted = true;
        if (f.isDirectory())
            accepted = true;
        return accepted;
    }

    /**
     * The description of this filter. For example: "JPG and GIF Images"
     */
    public String getDescription() {
        return "MPEG-7 Files (*.mp7, *.mp7.xml)";
    }
}
