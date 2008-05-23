package at.lux.fotoannotation;

import javax.swing.*;
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
 * Date: 20.11.2005
 * Time: 18:34:10
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class IconCache {
    private static IconCache instance = null;

    private ImageIcon agentIcon, placeIcon, timeIcon, objectIcon, eventIcon, helpIcon, removeIcon;
    private ImageIcon embedIcon, dbIcon, addIcon, saveAsIcon, saveIcon, clusterIcon, mdsIcon;

    private IconCache() {
        agentIcon = new ImageIcon(AnnotationFrame.class.getResource("data/PlainPeople.gif"));
        eventIcon = new ImageIcon(AnnotationFrame.class.getResource("data/event.gif"));
        placeIcon = new ImageIcon(AnnotationFrame.class.getResource("data/place.gif"));
        timeIcon = new ImageIcon(AnnotationFrame.class.getResource("data/time.gif"));
        helpIcon = new ImageIcon(AnnotationFrame.class.getResource("data/linkto_help.png"));
        objectIcon = new ImageIcon(AnnotationFrame.class.getResource("data/cube.png"));
        dbIcon = new ImageIcon(AnnotationFrame.class.getResource("data/db.png"));
        removeIcon = new ImageIcon(AnnotationFrame.class.getResource("data/delete_obj.gif"));
        embedIcon = new ImageIcon(AnnotationFrame.class.getResource("data/elements_obj.gif"));
        addIcon = new ImageIcon(AnnotationFrame.class.getResource("data/addgreen.png"));
        saveAsIcon = new ImageIcon(AnnotationFrame.class.getResource("data/saveas_edit.gif"));
        saveIcon = new  ImageIcon(AnnotationFrame.class.getResource("data/save_edit.gif"));
        clusterIcon = new  ImageIcon(AnnotationFrame.class.getResource("data/cluster.png"));
        mdsIcon = new  ImageIcon(AnnotationFrame.class.getResource("data/mds.png"));
    }

    public static IconCache getInstance() {
        if (instance == null) instance = new IconCache();
        return instance;
    }

    public ImageIcon getAgentIcon() {
        return agentIcon;
    }

    public ImageIcon getPlaceIcon() {
        return placeIcon;
    }

    public ImageIcon getTimeIcon() {
        return timeIcon;
    }

    public ImageIcon getObjectIcon() {
        return objectIcon;
    }

    public ImageIcon getEventIcon() {
        return eventIcon;
    }

    public ImageIcon getHelpIcon() {
        return helpIcon;
    }

    public ImageIcon getRemoveIcon() {
        return removeIcon;
    }

    public ImageIcon getEmbedIcon() {
        return embedIcon;
    }

    public ImageIcon getDbIcon() {
        return dbIcon;
    }

    public ImageIcon getAddIcon() {
        return addIcon;
    }

    public ImageIcon getSaveAsIcon() {
        return saveAsIcon;
    }

    public ImageIcon getSaveIcon() {
        return saveIcon;
    }

    public ImageIcon getClusterIcon() {
        return clusterIcon;
    }

    public ImageIcon getMdsIcon() {
        return mdsIcon;
    }
}
