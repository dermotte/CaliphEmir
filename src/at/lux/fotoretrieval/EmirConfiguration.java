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

import at.lux.retrieval.fdp.FDPParameters;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Date: 21.02.2005
 * Time: 23:11:20
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class EmirConfiguration {
    private static EmirConfiguration configuration = null;
    private Properties props = new Properties();

    private EmirConfiguration() {
        // nothing to do here ... its a singleton.
    }

    /**
     * Returns the current EmirConfiguration
     * @return the current instance of the EmirConfiguration
     */
    public static EmirConfiguration getInstance() {
        if (configuration == null) {
            configuration = new EmirConfiguration();
        }
        return configuration;
    }

    /**
     * Returns the current EmirConfiguration by importing alle values given by properties
     * @param properties Properties to import.
     * @return current EmirConfiguration by importing alle values given by properties.
     */
    public static EmirConfiguration getInstance(Properties properties) {
        if (configuration == null) {
            configuration = new EmirConfiguration();
        }
        if (properties == null) {
            configuration.setProperties(new Properties());
        } else {
            configuration.setProperties(properties);
        }
        return configuration;
    }

    public void setProperties(Properties properties) {
        setProperty("Retrieval.Result.Maximum", "30", properties);
        setProperty("Retrieval.Cbir.useDerby", "false", properties);

        setProperty("SpringEmbedder.Parameters.c1", "0", properties);
        setProperty("SpringEmbedder.Parameters.c2", "0", properties);
        setProperty("SpringEmbedder.Parameters.c3", "0", properties);
        setProperty("SpringEmbedder.Parameters.c4", "0", properties);
        setProperty("SpringEmbedder.Parameters.invisibleCenterNode", "false", properties);
        setProperty("SpringEmbedder.Parameters.normalizeMovementVector", "false", properties);
        setProperty("SpringEmbedder.Parameters.scaleDownSpace", "false", properties);

        setProperty("MdsVisPanel.FDP.StepWait", "17", properties);
        setProperty("MdsVisPanel.FDP.StopCondition", "0.015", properties);
        setProperty("MdsVisPanel.FDP.StartWait", "200", properties);
        setProperty("MdsVisPanel.ImageLoader.StepWait", "23", properties);
        setProperty("MdsVisPanel.ImageLoader.StartWait", "700", properties);
        setProperty("MdsVisPanel.ImageLoader.MaxImageSideLength", "50", properties);
        setProperty("MdsVisPanel.ImageLoader.ShowPleaseWait", "true", properties);

        setProperty("GraphConstructionPanel.EdgeOffset.x", "100", properties);
        setProperty("GraphConstructionPanel.EdgeOffset.y", "50", properties);
        setProperty("Algorithm.FDP.Parameters.r", "1", properties);
        setProperty("Algorithm.FDP.Parameters.w", "1", properties);
        setProperty("Algorithm.FDP.Parameters.d", "1", properties);
        setProperty("Algorithm.FDP.Parameters.gravity", "0.3", properties);
        setProperty("Algorithm.FDP.Parameters.minimumDistance", "0.0000001", properties);

        setProperty("Metric.Clustering", "0", properties);
        
        setProperty("Metric.FDP", "1", properties);

        setProperty("Clustering.Algorithm", "1", properties);

        //browser.linux=firefox {url}
        //browser.windows=cmd.exe /c start "" "{url}"

        //help.online=http://www.semanticmetadata.net/wiki/
        //help.homepage=http://www.semanticmetadata.net/
        setProperty("browser.linux", "firefox {url}", properties);
        setProperty("browser.windows", "cmd.exe /c start \"\" \"{url}\"", properties);
        setProperty("help.online", "http://www.semanticmetadata.net/wiki/", properties);
        setProperty("help.homepage", "http://www.semanticmetadata.net/", properties);
        setProperty("emir.demomode", "false", properties);
    }

    private void setProperty(String name, String defaultValue, Properties properties) {
        props.setProperty(name, properties.getProperty(name, defaultValue));
    }

    public Properties saveProperties(Properties propsWhichAreSaved) {
        Set keyEnumeration = props.keySet();
        for (Object aKeyEnumeration : keyEnumeration) {
            String key = (String) aKeyEnumeration;
            propsWhichAreSaved.put(key, props.get(key));
        }
        return propsWhichAreSaved;
    }

    public float getFloat(String key) {
        return new Float(props.getProperty(key));
    }

    public int getInt(String key) {
        return new Integer(props.getProperty(key));
    }

    public double getDouble(String key) {
        return new Double(props.getProperty(key));
    }

    /**
     * Creates and returns a parameters object for FDP algorithm.
     * @return the parameters for the FDP Algorithm.
     * @see at.lux.retrieval.fdp.FDPParameters
     */
    public FDPParameters getFDPParameters() {
        double r = getDouble("Algorithm.FDP.Parameters.r");
        double w = getDouble("Algorithm.FDP.Parameters.w");
        double d = getDouble("Algorithm.FDP.Parameters.d");
        double gravity = getDouble("Algorithm.FDP.Parameters.gravity");
        float minimumDistance = getFloat("Algorithm.FDP.Parameters.minimumDistance");
        FDPParameters params = new FDPParameters(d, gravity, minimumDistance, r, w);
        return params;
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(props.getProperty(key));
    }
}
