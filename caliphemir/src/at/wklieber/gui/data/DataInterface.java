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
package at.wklieber.gui.data;

/**
 * used by the mpeg 7 xml tool to identify the descriptor
 */
public interface DataInterface {
    public static final int DESCRIPTION_DATA = 0;
    public static final int CAMERA_MOTION_DATA = 1;
    public static final int ICOMPONENT_DATA = 2;  // set icomponent directly: shape, rectangle
    public static final int TEXT_QUERY_DATA = 3;
    public static final int SEMANTIC_DATA = 4;  // MPEG7 semantic descriptor

    public int getId();
}
