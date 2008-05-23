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

package at.wklieber;


import at.wklieber.tools.AccessXmlFile;
import at.wklieber.tools.Console;
import at.wklieber.tools.IAccessFile;

import java.io.InputStream;
import java.util.logging.Logger;

public class Settings {
    private static Logger log = Logger.getLogger(Settings.class.getName());
    private static Console console = Console.getReference();
    private IAccessFile accessConfigFile;
    public static final String DEFAULT_MPEG7_NAME = "scene.mpeg7.xml";

    public static enum OPERATOR {AND, OR};

    //----- preload all DIRS and FILE String with the relative paths
    private String BASE_DIR = "/at/wklieber/";
    private String DATA_DIR = BASE_DIR + "data/";
    private String LOG_DIR = "";

    private String LOG4J_CFG_FILE = "log4j.properties";

    //--------------------- GUI-Search ----------------------------


    private int thumpnailHeigh = 120;
    private int thumpnailWidth = 90;
    private String schemaLocation = "Mpeg7-2001.xsd";

    private static Settings settings = null;


    public static Settings getReference() {
        if (settings == null) {
            settings = new Settings();
        }

        return settings;
    }

    /**
     * there are only static methods. So no instance of this class is usefull.
     */
    private Settings() {
        init();
    }

    private void init() {
    }




    public String getBaseDir() {
        return BASE_DIR;
    }


    public String getDataDir() {
        return DATA_DIR;
    }

    public String getIconsDir() {
        return DATA_DIR + "icons/";
    }

    public String getImageDir() {
        return DATA_DIR + "sample_images/";
    }



    public String getLogDir() {
        return LOG_DIR;
    }



    public String getLog4jConfigFile() {
        if (LOG4J_CFG_FILE == null)
            return "";
        else
            return LOG4J_CFG_FILE;
    }



    public int getThumpnailHeigh() {
        return thumpnailHeigh;
    }

    public int getThumpnailWidth() {
        return thumpnailWidth;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public IAccessFile getConfigAccess() {
        try {
            if (accessConfigFile == null) {
                InputStream stream = this.getClass().getResourceAsStream("/at/wklieber/data/config.xml");
                accessConfigFile = new AccessXmlFile(stream);
            }
        } catch (Exception e) {
            log.severe(e.toString());
            accessConfigFile = null;
        }

        return accessConfigFile;
    }
} // end class
