/*
 *
 *
 *
 * @author Mathias Lux, mathias@juggle.at
 * Date: 27.08.2002
 * Time: 09:35:38
 */
package at.knowcenter.caliph.objectcatalog.semanticscreator;

import java.util.Vector;

public interface BeeDataExchange {
    public static String[] COUNTRIES_CODE_3_LETTERS = {"arg", "bel", "bra", "chn", "crc", "den", "ger", "ecu", "eng", "fra", "irl", "ita", "jpn", "cmr", "cro", "mex", "nga", "par", "pol", "por", "kor", "rus", "ksa", "swe", "sen", "svn", "esp", "rsa", "tun", "tur", "uru", "usa"};
    // public static String[] ISO_COUNTRIES_CODE_3_LETTERS = {"arg", "bel", "bra", "chn", "crc", "den", "ger", "ecu", "eng", "fra", "irl", "ita", "jpn", "cmr", "cro", "mex", "nga", "par", "pol", "por", "kor", "rus", "ksa", "swe", "sen", "svn", "esp", "rsa", "tun", "tur", "uru", "usa"};
    public static String[] ISO_COUNTRIES_CODE_2_LETTERS = {"ar", "be", "br", "cn", "cr", "dk", "de", "ec", "en", "fr", "ie", "it", "jp", "cm", "hr", "mx", "ng", "py", "pl", "pt", "kr", "ru", "sa", "se", "sn", "si", "es", "za", "tn", "tr", "uy", "us"};

    public abstract Vector getAgents();

    public abstract void addAgents(Vector v);

    public abstract Vector getEvents();

    public abstract void addEvents(Vector v);

    public abstract Vector getVenues();

    public abstract void addVenues(Vector v);

    public abstract Vector getPossibleObjects();

    public abstract String[] getRelations();
}
