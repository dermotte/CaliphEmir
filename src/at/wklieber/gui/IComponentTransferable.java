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




import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.logging.Logger;

class IComponentTransferable
        implements Transferable, ClipboardOwner {
    static Logger cat = Logger.getLogger(IComponentTransferable.class.getName());

    public static DataFlavor iComponentFlavor = null;
    public static DataFlavor localIComponentFlavor = null; // no longer used
    public static DataFlavor imageFlavor = null;

    IComponent iComponent = null;

    public IComponentTransferable(IComponent iComponent1) {
        // make a copy of the currend object
        iComponent = (IComponent) iComponent1.clone();
    }

    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] returnValue = new DataFlavor[1];
        returnValue[0] = localIComponentFlavor;
        //returnValue[0] = imageFlavor;
        return returnValue;
    }

    public void lostOwnership(Clipboard clipboard, Transferable transferable) {
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor == null || localIComponentFlavor == null) {
            return false;
        }
        return (flavor.equals(localIComponentFlavor));

        /*
        if (flavor == null || imageFlavor == null) {
            return false;
        }

        return (flavor.equals(imageFlavor));
        */
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (iComponent == null) {
            cat.severe("!!! IComponent is null");
            return null;
        }

        /*if (iComponent.getImageIcon() == null) {
           cat.severe("!!! imageIcon is null");
           return null;
        }

        return iComponent.getImageIcon();*/

        return iComponent;
        //IImageComponent imageComponent = (IImageComponent) iComponent;
        //return imageComponent.getImageIcon().getImage();
    }

    static {
        try {
            iComponentFlavor =
                    new DataFlavor(IComponent.class,
                            "IMB image Component flavor");

            localIComponentFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                    "; class=" + IComponent.class.getName(),
                            "Local IMB IComponent flavor", IComponent.class.getClassLoader());


            //imageFlavor = new DataFlavor(DataFlavor.imageFlavor, "Local IMB IComponent flavor");
            imageFlavor = new DataFlavor(Class.forName("java.awt.Image"), "Image");


        } catch (Exception e) {
            cat.severe(e.toString());
            e.printStackTrace();
        }
    }

}

