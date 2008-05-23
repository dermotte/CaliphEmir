package at.lux.imageanalysis.db;
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
 * Date: 27.10.2005
 * Time: 21:41:45
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SQLGeneratorFactory {
    public enum Database {
        MySQL, Derby, HSQLDB, Oracle, Informix}

    ;

    /**
     * Creates and returns an appropriate SQLGenerator Object.
     *
     * @param database defines the type of the desired generator.
     * @return the desired SQLGenerator.
     */
    public static SQLGenerator getSQLGenerator(Database database) {
        if (database == Database.Derby) {
            return DerbySQLGenerator.getInstance();
        } else {
            throw new UnsupportedOperationException("Not implemented yet!");
        }
    }
}
