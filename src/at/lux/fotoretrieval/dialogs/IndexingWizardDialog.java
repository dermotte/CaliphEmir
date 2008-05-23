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
package at.lux.fotoretrieval.dialogs;

import at.lux.fotoretrieval.RetrievalFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Date: 02.02.2005
 * Time: 22:20:22
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class IndexingWizardDialog extends JDialog {
    private IndexingWizard iw;
    private RetrievalFrame parent;

    /**
     * Creates a non-modal dialog without a title with the
     * specified <code>Dialog</code> as its owner.
     * <p/>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     *
     * @param owner the non-null <code>Dialog</code> from which the dialog is displayed
     * @throws java.awt.HeadlessException if GraphicsEnvironment.isHeadless()
     *                                    returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @see javax.swing.JComponent#getDefaultLocale
     */
    public IndexingWizardDialog(RetrievalFrame owner) throws HeadlessException {
        super(owner, "Indexing wizard", true);
        parent = owner;
        iw = new IndexingWizard(this);
        this.getContentPane().add(iw, BorderLayout.CENTER);
        pack();
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((ss.width-getWidth())>>1, (ss.height-getHeight())>>1);
        setVisible(true);
    }

    public void startIndexing(String path) {
        RetrievalFrame.BASE_DIRECTORY = path;
        parent.createIndex();
        setVisible(false);
    }
}
