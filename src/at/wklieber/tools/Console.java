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

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Console {

    // when false: the default value is used as default and the user can input
    //             an other value
    //       true: default vaule is used without user interaction


    public static int TEXT_MODE = 1;
    public static int GRAFIC_MODE = 2;

    private static int DEFAULT_MODE = TEXT_MODE;
    //private static Color DEFAULT_COLOR = Color.BLACK;
    private static boolean DEFAULT_PRINT_TIME = true;
    private static String DEFAULT_TITLE = "Message Window";


    private static Console console = null;

    public static Console getReference() {
        if (console == null) {
            console = new Console(DEFAULT_MODE);
        }

        return console;
    }



    // ----------------------------- static stuff end ----------------------------------


    private int mode_ = DEFAULT_MODE;
    private JFrame mainFrame = null;
    private JTextPane textField = null;
    private Color textColor = null;
    private Color errorColor = null;

    private Console(int mode1) {
        init(mode1);

    }

    private void init(int mode1) {
        mode_ = mode1;

        if (mainFrame != null) {
            mainFrame.dispose();
        }

        if (mode_ == GRAFIC_MODE) {
            mainFrame = new JFrame();

            mainFrame.setDefaultCloseOperation(3);
            mainFrame.setTitle(DEFAULT_TITLE);

            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            mainFrame.setSize((int) d.getWidth() / 3, (int) d.getHeight() / 3);


            textColor = Color.BLACK;
            errorColor = Color.RED;

            textField = new JTextPane();
            JScrollPane scrollPane = new JScrollPane(textField);

            mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            mainFrame.setLocation(0, 0);
            mainFrame.setVisible(true);

            mainFrame.validate();
            mainFrame.setVisible(true);

        }
    }


    public void switchToMode(int newMode1) {
        if (newMode1 != mode_) {
            init(newMode1);
        }
    }


    public String readString(String infoText1, String defText1) {
        String returnValue = defText1;
        if (mode_ == TEXT_MODE) {
            returnValue = readStringFromText(infoText1, defText1);
        } else {
            console.echo("not Implemented");
        }

        return returnValue;
    }

    // reads a string from the input-console
    private String readStringFromText(String text, String defText) {
        String ret = "";
        try {
            System.out.print(text + "[" + defText + "]: ");

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            ret = in.readLine();
            in = null;
            if (ret.length() == 0)
                ret = defText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }  // end method

    public void echo(String text1) {
        if (mode_ == TEXT_MODE) {
            System.out.println(text1);
        } else {
            echo(text1, textColor);
        }
    }

    public void echo(String text1, Color color1) {
        if (mode_ == TEXT_MODE) {
            System.out.println(text1);
        } else {
            //String oldText = textField.getText();
            //textField.setText(oldText + text1 + "\n");
            String initStyles = "bold";
            Style s = textField.addStyle("bold", null);
            StyleConstants.setBold(s, false);
            StyleConstants.setForeground(s, color1);
            Document doc = textField.getDocument();

            try {
                doc.insertString(doc.getLength(), text1 + "\n", textField.getStyle(initStyles));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    public void error(String text1) {
        if (mode_ == TEXT_MODE) {
            console.echo("ERROR: " + text1);
        } else {
            echo(text1, errorColor);
        }
    }

    public void line() {
        if (mode_ == TEXT_MODE) {
            console.echo("-------------------------------------------------------");
        } else {
            console.echo("not Implemented");
        }
    }


    public void exitOnException(Exception e) {
        if (mode_ == TEXT_MODE) {
            e.printStackTrace();
        } else {
            console.echo("not Implemented");
        }
        System.exit(1);
    }

    public void exitOnError(String text) {
        if (mode_ == TEXT_MODE) {
            echo("ERROR: " + text);
        } else {
            console.echo("not Implemented");
        }
        System.exit(1);
    }

    /*//stop watch methods
    public void watchReset() {
        watch.start();
    }

    public void watchStop() {
        watch.stop();
    }

    public void watchPrint() {
        if (DEFAULT_PRINT_TIME) {
            watch.printTimes();
        }
    }

    public void watchAddTime(String text1) {
        watch.storeTime(text1);
    }*/

} // end class