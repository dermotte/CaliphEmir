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
package at.wklieber.gui;

import at.wklieber.gui.data.DescriptionData;
import at.wklieber.tools.Console;
import at.wklieber.tools.Mpeg7DateFormat;



import javax.swing.*;
import java.util.Date;

public class DescriptionFrame {
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(DescriptionFrame.class.getName());
    private static Console console = Console.getReference();

    public static String DIALOG_TITLE = "Enter Description Metadata";
    public static String DEFAULT_CREATION_TOOL = "IMB search specification";

    private JFrame parentFrame = null;

    private DescriptionData data = null;
    private DataInputDialog inputDialog = null;


    public DescriptionFrame(JFrame parentFrame1, DescriptionData data1) {
        init(parentFrame1, data1);
    }

    private void init(JFrame parentFrame1, DescriptionData data1) {
        parentFrame = parentFrame1;

        data = data1;
        validateData();

        inputDialog = new DataInputDialog(parentFrame, null, "Creation Metadata");

        readDataFields();
        inputDialog.showDialog();
        writeDataFields();
    }

    /**
     * ensure the data is not null
     */
    private void validateData() {
        if (data == null) {
            data = new DescriptionData();
        }
    }

    /**
     * read the data from the datastructure and write it to the dialogboxes
     */
    private void readDataFields() {
        Date currentDate = new Date(System.currentTimeMillis());
        if (data.getCreationTime() == null)
            data.setCreationTime(currentDate);

        inputDialog.addInputData("Creator First Name", "Enter the first name of the content creator", data.getCreatorFirstName());
        inputDialog.addInputData("Creator Second Name", "Enter the second name of the content creator", data.getCreatorLastName());
        inputDialog.addInputData("General Description", "Enter some general description", data.getDescription());
        inputDialog.addInputData("Creation Time", "Enter the Time of the content generation", Mpeg7DateFormat.date2Timepoint(data.getCreationTime()));
        inputDialog.addInputData("Creation Place", "Enter the place of the content generation ", data.getCreationPlace());
        inputDialog.addInputData("Creation Tool", "Enter the name of the creation tool", data.getCreationTool());

    }

    private void writeDataFields() {
        try {
            data.setCreatorFirstName(inputDialog.getInputData("Creator First Name", data.getCreatorFirstName()));
            data.setCreatorLastName(inputDialog.getInputData("Creator Second Name", data.getCreatorLastName()));
            data.setDescription(inputDialog.getInputData("General Description", data.getDescription()));
            data.setCreationPlace(inputDialog.getInputData("Creation Place", data.getCreationPlace()));
            data.setCreationTool(inputDialog.getInputData("Creation Tool", data.getCreationTool()));
            data.setCreationTime(Mpeg7DateFormat.format(inputDialog.getInputData("Creation Time",
                                    Mpeg7DateFormat.date2Timepoint(data.getCreationTime())), null)); // Mpeg7DateFormat.format(jTextCreationTime.getText(), null));

        } catch (Exception e) {
            cat.severe(e.toString());
        }
    }

    public String toString() {
        return "nothing to report";
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }
}
