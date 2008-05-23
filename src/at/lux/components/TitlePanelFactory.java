package at.lux.components;

import javax.swing.*;

/**
 */
public class TitlePanelFactory {
    public static JPanel getTitlePanel(String label) {
        SpringLayout layout = new SpringLayout();
        JPanel result = new JPanel(layout);
        JLabel jlabel = new JLabel(label);
        layout.putConstraint(SpringLayout.WEST, jlabel, 5, SpringLayout.WEST, result);
        layout.putConstraint(SpringLayout.NORTH, jlabel, 5, SpringLayout.NORTH, result);
        result.add(jlabel);
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        layout.putConstraint(SpringLayout.EAST, separator, -5, SpringLayout.EAST, result);
        layout.putConstraint(SpringLayout.WEST, separator, 5, SpringLayout.EAST, jlabel);
        layout.putConstraint(SpringLayout.NORTH, separator, 15, SpringLayout.NORTH, result);
        result.add(separator);
        return result;
    }
}
