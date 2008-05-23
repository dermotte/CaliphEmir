package at.lux.imageanalysis;
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

/**
 * This file is part of Caliph & Emir
 * Date: 01.11.2005
 * Time: 21:32:50
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface VisualDescriptor {
    /**
     * List available types:
     */
    enum Type {ColorLayout, ScalableColor, EdgeHistogram, DominantColor};

    /**
     * Compares one descriptor to another.
     *
     * @param descriptor
     * @return the distance from [0,infinite) or -1 if descriptor type does not match
     */
    float getDistance(VisualDescriptor descriptor);

    /**
     * Creates a String representation from the descriptor.
     * @return the descriptor as String.
     */
    public String getStringRepresentation();

    /**
     * Sets the descriptor value from a String.
     * @param descriptor the descriptor as String.
     */
    public void setStringRepresentation(String descriptor);
}
