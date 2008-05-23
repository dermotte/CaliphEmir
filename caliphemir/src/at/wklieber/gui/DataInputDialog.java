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

import at.wklieber.tools.Console;



import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

class InputDataSet {
    public int type = -1;

    public String name = null;
    public String value = null;

    public JTextField jTextField = null;
    public JComboBox jComboBox = null;
}

public class DataInputDialog
        implements DocumentListener
//       extends Container
{
    private static java.util.logging.Logger cat = java.util.logging.Logger.getLogger(DataInputDialog.class.getName());
    private static Console console = Console.getReference();

    public static final int SMALL_LENGTH = 6;
    public static final int MEDIUM_LENGTH = 15;

    private static final int INPUT_TYPE_TEXTFIELD = 0;
    private static final int INPUT_TYPE_COMBOBOX = 1;


    private String dialogTitle = "Data Input Frame";

    private JFrame parentFrame = null;
    private JPanel textPanel = null;
    private JPanel textPanelIterator = null;
    private Container contentPane = null;

    private java.util.Map dataMap = null;
    private java.util.Map oldDataMap = null;

    protected EventListenerList changeListeners = new EventListenerList();

    JDialog myDialog = null;

    public DataInputDialog(JFrame parentFrame1, Container drawContainer1, String title1) {
        init(parentFrame1, drawContainer1, title1);
    }

    public DataInputDialog(JFrame parentFrame1) {
        init(parentFrame1, null, dialogTitle);
    }

    private void init(JFrame parentFrame1, Container drawContainer1, String title1) {
        parentFrame = parentFrame1;
        contentPane = drawContainer1;
        dialogTitle = title1;


        if (contentPane == null) {
            contentPane = new Container();
        }

        contentPane.setLayout(new BorderLayout());


        textPanel = new JPanel();
        LayoutManager mgr = new BorderLayout();

        textPanel.setLayout(mgr);
        contentPane.add(textPanel, BorderLayout.CENTER);

        contentPane.setVisible(true);

        dataMap = new HashMap();

        if (dialogTitle != null && dialogTitle.length() > 0) {
            JLabel title = new JLabel(dialogTitle);
            title.setMinimumSize(new Dimension(200, 20));
            title.setBorder(BorderFactory.createLoweredBevelBorder());
            contentPane.add(title, BorderLayout.NORTH);
        }

        contentPane.validate();
    }

    public Container getContentPane() {
        return contentPane;
    }

    /**
     * Set as new entry for a Textfield
     */
    public void addInputData(String name1, String description1, String initValue, int textFieldLength1) {
        String[] values = new String[1];
        values[0] = initValue;
        addNewEntry(name1, description1, values, textFieldLength1, INPUT_TYPE_TEXTFIELD, -1);
    }

    private void addNewEntry(String name1, String description1, String[] initValues1,
                             int textFieldLength1, int type1, int startPos1) {
        Date currentDate = new Date(System.currentTimeMillis());

        if (textPanelIterator == null) {
            textPanelIterator = textPanel;
        }


        JLabel label = new JLabel(name1 + ": ");
        label.setToolTipText(description1);
        //label.setMaximumSize(new Dimension(100, MAX_HIGH));
        //label.setMinimumSize(new Dimension(100, MIN_HIGH));

        Component inputField = null;
        if (type1 == INPUT_TYPE_TEXTFIELD) {
            String initValue = initValues1[0];

            JTextField textField = new JTextField(initValue);
            textField.setToolTipText(description1);
            textField.setColumns(textFieldLength1);
            textField.getDocument().addDocumentListener(this);
            inputField = textField;
        } else if (type1 == INPUT_TYPE_COMBOBOX) {
            java.util.Vector entry = new Vector();
            for (int i = 0; i < initValues1.length; i++) {
                entry.add(initValues1[i]);
                //cat.fine("Como add: " + dirs[i]);
            }

            JComboBox comboBox = new JComboBox(entry);
            comboBox.setToolTipText(description1);
            comboBox.setSelectedIndex(startPos1);

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    String selected = (String) cb.getSelectedItem();
                    int index = (int) cb.getSelectedIndex();
                    actionComboElementSelected(selected, index);
                }
            };
            comboBox.addActionListener(listener);

            inputField = comboBox;
        }

        JPanel dummyPanel = new JPanel();
        dummyPanel.setLayout(new BorderLayout());
        textPanelIterator.add(dummyPanel, BorderLayout.CENTER);

        JPanel entryPanel = new JPanel();
        //entryPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        entryPanel.setLayout(new GridLayout(0, 2));
        entryPanel.add(label);
        entryPanel.add(inputField);
        dummyPanel.add(entryPanel, BorderLayout.NORTH);

        //textPanel.add(label);
        //textPanel.add(textField);

        //textPanel.revalidate();
        textPanelIterator = dummyPanel;

        //cat.fine("textPanel added: " + name1);

        InputDataSet data = new InputDataSet();
        data.type = type1;
        data.name = name1;

        if (type1 == INPUT_TYPE_TEXTFIELD) {
            data.jTextField = (JTextField) inputField;
            data.value = initValues1[0];
        } else if (type1 == INPUT_TYPE_COMBOBOX) {
            data.jComboBox = (JComboBox) inputField;

            data.value = initValues1[startPos1];
        }

        dataMap.put(name1, data);

        oldDataMap = new HashMap();
        for (Iterator it = dataMap.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            InputDataSet d = (InputDataSet) dataMap.get(key);
            InputDataSet copy = new InputDataSet();
            copy.type = d.type;
            copy.name = d.name;
            copy.value = d.value;

            oldDataMap.put(key, copy);
        }


        contentPane.validate();
    }

    /**
     * Set as new entry for a Textfield
     */
    public void addInputData(String name1, String description1, String initValue1) {
        addInputData(name1, description1, initValue1, MEDIUM_LENGTH);
    }

    /**
     * Set as new entry for Pull Down Box
     */
    public void addInputData(String name1, String description1, String[] values1, String startName1) {
        int currendIndex = 0;
        for (int i = 0; i < values1.length; i++) {
            if (values1[i].equalsIgnoreCase(startName1)) {
                currendIndex = i;
            }
        }

        addNewEntry(name1, description1, values1, MEDIUM_LENGTH, INPUT_TYPE_COMBOBOX, currendIndex);
    }


    public void removeAllInputData() {
        textPanel.removeAll();
        textPanelIterator = null;
        dataMap = new HashMap();
    }


    public String getInputData(String name1, String defaultValue1) {
        String returnValue = defaultValue1;
        try {
            if (dataMap.containsKey(name1)) {
                InputDataSet data = (InputDataSet) dataMap.get(name1);
                returnValue = data.value;
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
         * Returns true if the input String is "yes" or "true"
         * Returns false otherwise
         *
         * @param name1
         * @param defaultValue1
         * @return
         */
    public boolean getInputData(String name1, boolean defaultValue1) {
        boolean returnValue = defaultValue1;
        try {
            if (dataMap.containsKey(name1)) {
                InputDataSet data = (InputDataSet) dataMap.get(name1);


                if (data.value.equalsIgnoreCase("true") || data.value.equalsIgnoreCase("yes") ||
                            data.value.equalsIgnoreCase("1")) {
                    returnValue = true;
                } else {
                    returnValue = false;
                }
            }
        } catch (Exception e) {
            cat.severe(e.toString());
        }

        return returnValue;
    }

    /**
     * show the content of the container in an own Dialog with ok and cancel button
     */
    public void showDialog() {
        myDialog = new JDialog(parentFrame, dialogTitle, true);
        Container dialogContentPane = myDialog.getContentPane();
        dialogContentPane.setLayout(new BorderLayout());
        myDialog.setSize(500, 400);
        //myDialog.setTitle(dialogTitle);
        //      contentPane.setLayout(new BorderLayout());
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        myDialog.setLocation((d.width - myDialog.getSize().width) / 2, (d.height - myDialog.getSize().height) / 2);

        //contentPane = new Container();
        //contentPane.setLayout(new BorderLayout());
        //contentPane.add(new JButton("OK"), BorderLayout.SOUTH);
        dialogContentPane.add(contentPane, BorderLayout.CENTER);

        Container controllButtons = new Container();
        controllButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        dialogContentPane.add(controllButtons, BorderLayout.SOUTH);

        ActionListener okAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionOk(e);
            }
        };

        ActionListener cancelAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionCancel(e);
            }
        };
        JButton okButton = new JButton("OK");
        okButton.addActionListener(okAction);
        controllButtons.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(cancelAction);
        controllButtons.add(cancelButton);

        myDialog.setVisible(true);
    }

    private void updateData() {
        Component[] components = textPanel.getComponents();
        for (Iterator it = dataMap.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            InputDataSet data = (InputDataSet) dataMap.get(name);

            if (data.type == INPUT_TYPE_TEXTFIELD) {
                data.value = data.jTextField.getText();
            } else if (data.type == INPUT_TYPE_COMBOBOX) {
                data.value = (String) data.jComboBox.getSelectedItem();
            }
            //cat.fine("update: " + name + ", " + data.value);

        } // end for
    }

    public void actionOk(ActionEvent e) {
        updateData();
        myDialog.setVisible(false);
    }

    public void actionCancel(ActionEvent e) {
        dataMap = oldDataMap;
        myDialog.setVisible(false);
    }


    public String toString() {
        return "nothing to report";
    }

    protected void finalize() throws Throwable {
        super.finalize();
    }

    //--------------------- stuff to inform changelistern classes
    public void setContentPane(Container contentPane) {
        this.contentPane = contentPane;
    }

    public void insertUpdate(DocumentEvent evt) {
        //cat.fine("ACTION, insertUpdate: " + evt.toString());
        fireChange();
    }

    public void removeUpdate(DocumentEvent evt) {
        //cat.fine("ACTION, removeUpdate: " + evt.toString());
        fireChange();
    }

    public void changedUpdate(DocumentEvent evt) {
        //cat.fine("ACTION, changedUpdate: " + evt.toString());
        fireChange();
    }

    // Listener notification support
    public void addChangeListener(ChangeListener x) {
        changeListeners.add(ChangeListener.class, x);

        // bring it up to date with current state
        x.stateChanged(new ChangeEvent(this));
    }

    public void removeChangeListener(ChangeListener x) {
        changeListeners.remove(ChangeListener.class, x);
    }

    // inform all listeners that something has changed
    protected void fireChange() {
        ChangeEvent c = new ChangeEvent(this);
        updateData();
        // Get the listener list
        Object[] listeners = changeListeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                ChangeListener cl = (ChangeListener) listeners[i + 1];
                cl.stateChanged(c);
            }
        }
    }

    private void actionComboElementSelected(String selected, int index) {
        //cat.fine("Selected: " + index);
        //drawPanel.removeAll();
        if (index < 0) {
            return;
        }


        fireChange();
    }  // end method


}
