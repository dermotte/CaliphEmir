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
package at.lux.fotoretrieval.panels;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class QualityConstraintPanel extends JPanel {
    private JSlider quality;
    private String[] modes = {"Minimum", "Maximum", "Exact match"};
    private JComboBox mode;
    String[] qual = {"Unsatisfactory", "Poor", "Fair", "Good", "Excellent"};
    private JLabel qualText;

    public QualityConstraintPanel() {
        super(new BorderLayout());
        init();
    }

    private void init() {
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Quality Constraints"));
        JPanel labelPane = new JPanel(new GridLayout(0, 1, 5, 5));
        JPanel inputPane = new JPanel(new GridLayout(0, 1, 5, 5));
        quality = new JSlider(JSlider.HORIZONTAL, 1, 5, 1);
        quality.setPreferredSize(new Dimension(128, 32));
        quality.setSnapToTicks(true);
        quality.setMinorTickSpacing(1);
        quality.setMajorTickSpacing(2);
        quality.setPaintTicks(true);
        quality.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setQualityText();
            }
        });
        mode = new JComboBox(modes);

        labelPane.add(new JLabel("Quality: "));
        labelPane.add(new JLabel("Mode: "));

        JPanel _sliderAndText = new JPanel(new BorderLayout());
        qualText = new JLabel(qual[0], JLabel.CENTER);
        _sliderAndText.add(quality, BorderLayout.WEST);
        _sliderAndText.add(qualText, BorderLayout.CENTER);

        inputPane.add(_sliderAndText);
        inputPane.add(mode);

        JPanel componentPanel = new JPanel(new BorderLayout());
        componentPanel.add(labelPane, BorderLayout.WEST);
        componentPanel.add(inputPane, BorderLayout.CENTER);
        this.add(componentPanel, BorderLayout.NORTH);
    }

    public int getQuality() {
        return quality.getValue();
    }

    public int getMode() {
        return mode.getSelectedIndex();
    }

    private void setQualityText() {
        qualText.setText(qual[quality.getValue() - 1]);
    }
}
