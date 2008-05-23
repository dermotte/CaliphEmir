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

import at.wklieber.tools.Console;



import java.util.Date;

public class DescriptionData
        implements DataInterface {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(DescriptionData.class.getName());
    private static Console console = Console.getReference();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description = "";
    private String creatorFirstName = "";
    private String creatorLastName = "";
    private String creationTool = "";
    private String creationPlace = "";
    private Date creationTime = null;

    public int getId() {
        return DESCRIPTION_DATA;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public String getCreationTime(String default1) {
        String returnValue = default1;
        if (creationTime != null) {
            returnValue = creationTime.toString();
        }
        return returnValue;
    }

    public DescriptionData() {
        init();
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    private void init() {

    }

    public String getCreatorFirstName() {
        return creatorFirstName;
    }

    public void setCreatorFirstName(String creatorFirstName) {
        this.creatorFirstName = creatorFirstName;
    }

    public String getCreatorLastName() {
        return creatorLastName;
    }

    public void setCreatorLastName(String creatorLastName) {
        this.creatorLastName = creatorLastName;
    }

    public String getCreationTool() {
        return creationTool;
    }

    public void setCreationTool(String creationTool) {
        this.creationTool = creationTool;
    }


    public String getCreationPlace() {
        return creationPlace;
    }

    public void setCreationPlace(String creationPlace) {
        this.creationPlace = creationPlace;
    }


    public String toString() {
        String returnValue = "General Description: \"" + getDescription() + "\", ";
        returnValue += " Creator Firstname: \"" + getCreatorFirstName() + "\", ";
        returnValue += " Creator Lastname: \"" + getCreatorLastName() + "\", ";
        returnValue += " Creator Time: \"" + getCreationTime() + "\", ";
        returnValue += " Creator Place: \"" + getCreationPlace() + "\", ";
        returnValue += " Creator Tools: \"" + getCreationTool() + "\"";

        return returnValue;
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public static void main(String[] argv) {
        DescriptionData m_DescriptionData = new DescriptionData();
    }
}
