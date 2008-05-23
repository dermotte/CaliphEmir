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
package at.wklieber.tools;

import at.wklieber.Settings;
import at.wklieber.gui.GenericListener;


import org.jdom.Attribute;
import org.jdom.Element;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * used to extract the Accelerator from a string.
 * e.g. "&File" -> "File", "F"
 */
class MenuEntryStruct {
    private String text = "";
    private char mnemonic = 0;

    public MenuEntryStruct(String property1) {
        init(property1, null);
    }

    public MenuEntryStruct(String property1, String accelerator1) {
        init(property1, accelerator1);
    }

    private void init(String property1, String accelerator1) {
        text = property1;


        if (accelerator1 != null && accelerator1.length() > 0) {
            mnemonic = accelerator1.charAt(0);
        } else { // try to extract from property name
            int shortCut = text.indexOf("&");
            mnemonic = 0;
            if ((shortCut > -1) && (text.length() > shortCut)) {
                String prefix = "";
                if (shortCut > 0) {
                    prefix = text.substring(0, shortCut);
                }
                mnemonic = text.charAt(shortCut + 1);
                String postfix = text.substring(shortCut + 1);

                text = prefix + postfix;
            }
        }
    }

    public String getText() {
        return text;
    }

    public char getMnemonic() {
        return mnemonic;
    }

    public String toString() {
        return "Text: <" + text + ">, Mnemonic: <" + (int) mnemonic + ">";
    }
}  // end class

//----------------------------------------------------------------------------------

public class MenuTools {
    static Logger cat = Logger.getLogger(MenuTools.class.getName());
    private static Console console = Console.getReference();

    //private static MenuTools java2dTools = null;

    public static final int TYPE_MENUBAR = 0;
    public static final int TYPE_POPUP = 1;


    private final int ITEM_PLAIN = 0;// Item types
    private final int ITEM_CHECK = 1;
    private final int ITEM_RADIO = 2;

    JMenuBar menuBar = null;
    JPopupMenu popupMenu = null;
    Container toolBarContainer = null;
    Map menuEntries = null;  // contains all menu main entries: File, Edit, Search, ....
    Map toolbarEntries = null; // a set of toolbars, according to the menu main entries
    String imageDir = null;  // the base dir, where all images are located

    private Object parentClass = null;


    /**
     * create a button with actiononlistener
     */
    public static JButton createImageButton(String iconFileName,
                                            String toolTip,
                                            Object actionListenerTargetClass,
                                            String actionListenerName) {
        JButton returnValue = null;

        try {
            ImageIcon icon = null;
            String iconDir = Settings.getReference().getIconsDir();
            icon = new ImageIcon(iconDir + iconFileName, toolTip);
            returnValue = createImageButton(icon, toolTip, actionListenerTargetClass, actionListenerName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    public static JButton createImageButton(Icon icon,
                                            String toolTip,
                                            Object actionListenerTargetClass,
                                            String actionListenerName) {
        JButton returnValue = null;

        try {
            ActionListener action = createActionListener(actionListenerName, actionListenerTargetClass);

            JButton jButton = new JButton(icon);
            jButton.setToolTipText(toolTip);
            jButton.addActionListener(action);
            returnValue = jButton;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }


    /**
     * constructor for managing a menuBar List
     */
    public MenuTools(Object parentClass1, JMenuBar menuBar1, Container toolBarContainer1,
                     String imageLocation1) {
        init(parentClass1, menuBar1, toolBarContainer1, imageLocation1);
    } // end constructor


    /**
     * constructor for managing a popupMenu
     */
    public MenuTools(String imageLocation1) {
        init(null, null, null, imageLocation1);
    } // end constructor


    private void init(Object parentClass1, JMenuBar menuBar1, Container toolBarContainer1,
                      String imageLocation1) {
        parentClass = parentClass1;
        menuBar = menuBar1;
        toolBarContainer = toolBarContainer1;

        imageDir = "/at/wklieber/data/icons";
        //imageDir = FileTools.setUrlPrefix(imageDir);
        imageDir = FileTools.setFinalDirectorySlash(imageLocation1);


        popupMenu = new JPopupMenu();

        menuEntries = new HashMap();
        toolbarEntries = new HashMap();
    }


    /**
     * reads the complete structure from the IAccess configuratin
     */

    private String getAttributeValue(Element element1, String attrName1) {
        String returnValue = "";
        if (element1 == null || attrName1 == null || attrName1.length() == 0) {
            return returnValue;
        }
        try {
            Attribute attributeElement = element1.getAttribute(attrName1);
            if (attributeElement != null) {
                returnValue = attributeElement.getValue();
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }
        return returnValue;
    }


    String nameWithAccelerator(String entryName, String entryAccelerator) {
        String returnValue = entryName;

        if (entryName == null || entryAccelerator == null || entryAccelerator.length() == 0) {
            return returnValue;
        }

        try {
            int start = entryName.indexOf(entryAccelerator);
            if (start >= 0) {
                String prefix = "";
                if (start > 0) {
                    prefix = entryName.substring(0, start);
                }

                String postfix = entryName.substring(start);
                returnValue = prefix + "&" + postfix;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * read menu settings from the xul file and add the entries to a menubar or pop-up menu
     *
     * @param dialogName1 the id in the xul file that specifies the menu to read in
     * @param type        specifies where the entries are added: menubar or popup menu
     */
    public void readMenuFromConfigFile(String dialogName1, int type) {
        try {
            IAccessFile config = Settings.getReference().getConfigAccess();
            String xmlRoot = "imbConfig/userInterface/menu/xul";

            java.util.List menuElements = config.getProperties(xmlRoot);
            if (menuElements == null || menuElements.size() < 1) {
                return;
            }

            // get xul tag
            Element xmlRootElement = (Element) menuElements.get(0);
            //cat.fine(xmlRootElement.getName());

            // get the actionlistener names


            java.util.List commandElements = xmlRootElement.getChildren("Command");
            Map actionListnerList = new HashMap(commandElements.size());
            for (Iterator commandIterator = commandElements.iterator(); commandIterator.hasNext();) {
                Element e = (Element) commandIterator.next();
                actionListnerList.put(getAttributeValue(e, "id"), getAttributeValue(e, "oncommand"));
            }
            //cat.fine(CollectionTools.printCollectionContent(actionListnerList));


            String xpath = "//xul/menubar[@id=\"" + dialogName1 + "\"]";
            java.util.List menuBarElements = XmlTools.xpathQuery(xmlRootElement, xpath, true);
            if (menuBarElements == null || menuBarElements.size() < 1) {
                return;
            }

            //get the dialogName element "menubar"
            Element dialogElement = (Element) menuBarElements.get(0);
            //cat.fine(XmlTools.documentToString(dialogElement));

            // now get the entries for the menubar: e.g. file, edit, ...
            java.util.List menuList = XmlTools.xpathQuery(dialogElement, "//menubar/menu", true);
            for (Iterator menuIterator = menuList.iterator(); menuIterator.hasNext();) {
                Element itemElement = (Element) menuIterator.next();
                String menuName = getAttributeValue(itemElement, "label");
                String menuAccelerator = getAttributeValue(itemElement, "accesskey");
                menuName = nameWithAccelerator(menuName, menuAccelerator);

                Element child = itemElement.getChild("menupopup");
                if (child == null) {
                    continue;
                }

                java.util.List entryList = child.getChildren("menuitem");
                //cat.fine("--->" + entryList.size());
                for (Iterator entryIterator = entryList.iterator(); entryIterator.hasNext();) {
                    Element entryElement = (Element) entryIterator.next();
                    //entryElement.detach();

                    String entryName = getAttributeValue(entryElement, "label");
                    String entryAccelerator = getAttributeValue(entryElement, "accesskey");
                    String infoText = getAttributeValue(entryElement, "statustext");
                    String iconName = getAttributeValue(entryElement, "image");
                    String commandId = getAttributeValue(entryElement, "command");

                    String actionListenerName = (String) actionListnerList.get(commandId);
                    entryName = nameWithAccelerator(entryName, entryAccelerator);

                    //cat.fine("Menu: " + entryName + ", " + actionListenerName);


                    switch (type) {
                        case TYPE_MENUBAR:
                            {
                                addMenuEntry(menuName, entryName, infoText, iconName, actionListenerName);
                                break;
                            }
                        case TYPE_POPUP:
                            {
                                cat.severe("not implenented: not added to popup");
                                break;
                            }
                        default:
                            {
                                cat.severe("unsupported id <" + type + ">");
                                break;
                            }
                    }
                } // end for menu entries
            } // end for menu bar eintries
        } catch (Exception e) {
            cat.severe(e.toString());
        }
    } // end method


    /**
     * generates a MenuItem that can be inserted in  JMenu of JPopupMenu
     *
     * @param iType the type of the item : ITEM_RADIO, ITEM_CHECK of ITEM_PLAIN
     */
    public JMenuItem createMenuItem(int iType, String sText,
                                    ImageIcon image, char acceleratorKey,
                                    String sToolTip) {
// Create the item
        JMenuItem menuItem;

        switch (iType) {
            case ITEM_RADIO:
                menuItem = new JRadioButtonMenuItem();
                break;

            case ITEM_CHECK:
                menuItem = new JCheckBoxMenuItem();
                break;

            default:
                menuItem = new JMenuItem();
                break;
        }



// Add the optional icon
        if ((image == null) || (image.getIconHeight() <= 0)) {
            String name = imageDir + "transparent.gif";
            //name = FileTools.removeFileUrlPrefix(name);
            //URL iconUrl = FileTools.getFileURL(name, null);
            //image = new ImageIcon(iconUrl);
            URL imageUrl = this.getClass().getResource(name);
            image = new ImageIcon(imageUrl);
            //assert (image.getIconHeight() > 0);
        }

        if ((image != null) && (image.getIconHeight() > 0))
            menuItem.setIcon(image);

// Add the item text
        menuItem.setText(sText);


// Add the accelerator key
        if (acceleratorKey > 0)
            menuItem.setMnemonic(acceleratorKey);

        // Add the optional tool tip text
        if (sToolTip != null)
            menuItem.setToolTipText(sToolTip);

        // Add an action handler to this menu item
        //menuItem.addActionListener(this);

        //menu.add(menuItem);

        return menuItem;
    }  // end method

    public JMenuItem createMenuItem(String sText, char acceleratorKey) {
        return createMenuItem(ITEM_PLAIN, sText, null, acceleratorKey, "");
    }

    /**
     * add a component to the menuebar (without actionlistener and menuentry)
     */
    public void addToolBarEntry(JComponent component1) {
        JToolBar toolBar = new JToolBar(component1.getName());
        toolBar.add(component1);

        //toolbarEntries.put(component1.getName(), toolBar);
        //mainFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
        toolBarContainer.add(toolBar);
        toolBar.validate();
        toolBar.repaint();
    }

    /**
     * add a menuItem to menuBar
     */
    public void addMenuEntry(String category1, String entry1, String description1,
                             String iconName1, ActionListener actionListener1) {

        if (menuEntries == null)
            menuEntries = new HashMap();

        if (toolbarEntries == null)
            toolbarEntries = new HashMap();


        String iconFileName1 = imageDir + iconName1;
        //cat.fine("orginal input: <" + iconFileName1 + ">");
        //iconFileName1 = FileTools.removeFileUrlPrefix(iconFileName1);
        //URL iconUrl = FileTools.getFileURL(iconFileName1, null);

        URL iconUrl = this.getClass().getResource(iconFileName1);

        //String testStr = iconUrl2.toExternalForm();
        /*URL iconUrl3 = null;
        try {
           iconUrl3 = new URL(testStr);
        } catch (MalformedURLException e) {
           e.printStackTrace();
        }*/


        ImageIcon image1;
        if (iconUrl == null) {
            image1 = new ImageIcon(iconFileName1);
            cat.fine("url is null. pass name as string");
            //cat.fine("USING   FILE: \"" + iconFileName1 + "\"");
        } else {
            image1 = new ImageIcon(iconUrl);
            //cat.fine("USING   URL: \"" + iconUrl.toExternalForm() + "\"");
            //cat.fine("WORK    URL: \"" + iconUrl.toExternalForm() + "\"");
        }

        /*if (iconUrl2 != null )
        cat.fine("WORKING URL: \"" + iconUrl2.toExternalForm() + "\"");
        else
          cat.severe("url2 is null.");*/

        /*cat.fine("getImage is null: " + (image1.getImage() == null) +
                     ", toSTring: <" + image1.toString() + ">");
  */

        // ---- add the entry to the menu
        MenuEntryStruct categoryData = new MenuEntryStruct(category1);
        MenuEntryStruct entryData = new MenuEntryStruct(entry1);

        //cat.fine(categoryData.toString());
        //cat.fine(entryData.toString());

        JMenu menu;
        if (menuEntries.containsKey(categoryData.getText())) {
            menu = (JMenu) menuEntries.get(categoryData.getText());
        } else {
            menu = new JMenu(categoryData.getText());
            if (categoryData.getMnemonic() != 0) {
                menu.setMnemonic(categoryData.getMnemonic());
            }
            menuEntries.put(categoryData.getText(), menu);
            menuBar.add(menu);
        }


        /*JMenuItem menuItem = createMenuItem(java2dTools.ITEM_PLAIN, entryData.getText(),
                image1, entryData.getMnemonic(), description1);
        */
        JMenuItem menuItem = createMenuItem(ITEM_PLAIN, entryData.getText(),
                image1, entryData.getMnemonic(), "");
        menuItem.addActionListener(actionListener1);
        menu.add(menuItem);

        // -- add the entry to the tool bar
        // add only, if the image exists


        //cat.fine("GO: " + image1.getIconHeight());
        if (image1.getIconHeight() > 0) {
            JToolBar toolBar;
            if (toolbarEntries.containsKey(categoryData.getText())) {
                toolBar = (JToolBar) toolbarEntries.get(categoryData.getText());
            } else {
                toolBar = new JToolBar(categoryData.getText());

                toolbarEntries.put(categoryData.getText(), toolBar);
                //mainFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
                toolBarContainer.add(toolBar);
            }


            JButton jButton1 = new JButton();
            toolBar.add(jButton1);

            jButton1.setIcon(image1);
            jButton1.setToolTipText(description1);
            jButton1.addActionListener(actionListener1);
        }

    } // end method

    /**
     * create a own actionlistener from the name.
     */
    public void addMenuEntry(String category1, String entry1, String description1,
                             String iconName1, String actionListenerName1) {

        ActionListener actionListener = null;
        try {
            actionListener = createActionListener(actionListenerName1, parentClass);
        } catch (Exception e) {
            cat.severe("Unable to create actionListener for \"" +
                    parentClass.getClass().getName() + "." + actionListenerName1 + "\"\n" +
                    "Menubar: <" + category1 + ">, Item: <" + entry1 + ">, Icon: <" + iconName1 + ">");

        }

        //ActionListener actionListener = getActionListener(actionListenerName1);
        addMenuEntry(category1, entry1, description1, iconName1, actionListener);


    }

    private static ActionListener createActionListener(String actionListenerName1, Object targetClass) {
        ActionListener actionListener;
        actionListener = (ActionListener)
                (GenericListener.create(ActionListener.class, "actionPerformed",
                        targetClass, actionListenerName1));
        return actionListener;
    }

    //------------- methods for using a popup menu

    // display the poputmenu
    public void showPopupMenu(Component parentComponent1, int x, int y) {
        if (popupMenu != null) {
            popupMenu.show(parentComponent1, x, y);
        }
    }


    public void addPopupMenuEntry(String entry1, String description1,
                                  String iconName1, String actionListenerName1) {
        ActionListener actionListener = (ActionListener)
                (GenericListener.create(ActionListener.class, "actionPerformed",
                        parentClass, actionListenerName1));

        addPopupMenuEntry(entry1, description1, iconName1, actionListener);
    }

    public void addPopupMenuEntry(String entry1, String description1,
                                  String iconName1, ActionListener actionListener1) {

        String iconFileName1 = imageDir + iconName1;
        ImageIcon image1 = new ImageIcon(iconFileName1);

        // ---- parse the entry to the menu
        MenuEntryStruct entryData = new MenuEntryStruct(entry1);

        //cat.fine(categoryData.toString());
        //cat.fine(entryData.toString());


        JMenuItem menuItem = createMenuItem(ITEM_PLAIN, entryData.getText(),
                image1, entryData.getMnemonic(), description1);
        menuItem.addActionListener(actionListener1);
        popupMenu.add(menuItem);
    } // end method

    public void removeAllPopupEntries() {
        if (popupMenu != null) {
            popupMenu.removeAll();
        }
    }

    public void setParentClass(Object parentClass) {
        this.parentClass = parentClass;
    }


    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }
} // end class