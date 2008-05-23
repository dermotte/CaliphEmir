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

import at.knowcenter.caliph.objectcatalog.semanticscreator.SemanticObjectRepresentation;
import at.knowcenter.caliph.objectcatalog.semanticscreator.SemanticRelationRepresentation;

import javax.swing.*;
import java.awt.*;

public class VisSemanticSearchPanel extends JPanel {
    SemanticObjectRepresentation o1, o2, o3;
    SemanticRelationRepresentation r1, r2;

    public VisSemanticSearchPanel() {
        o1 = new SemanticObjectRepresentation(new Point(10, 10), "Object I");
        o2 = new SemanticObjectRepresentation(new Point(10, 10), "Object II");
        o3 = new SemanticObjectRepresentation(new Point(10, 10), "Object III");
        r1 = new SemanticRelationRepresentation(o1, o2, "Relation I");
        r2 = new SemanticRelationRepresentation(o2, o3, "Relation II");
        this.setMinimumSize(new Dimension(SemanticObjectRepresentation.WIDTH * 3 + 150, SemanticObjectRepresentation.HEIGHT + 20));
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        int width, height;
        width = this.getWidth();
        height = this.getHeight();
        g2.clearRect(0, 0, width, height);
        g2.setColor(new Color(152, 181, 255));
        g2.fillRect(2, 2, this.getWidth() - 4, this.getHeight() - 4);
        int y = (height - SemanticObjectRepresentation.HEIGHT) / 2;
        o1.setP(new Point(10, y));
        o2.setP(new Point((width - SemanticObjectRepresentation.WIDTH) / 2, y));
        o3.setP(new Point(width - SemanticObjectRepresentation.WIDTH - 10, y));

        o1.drawObject(g2);

        // only draw following objects if they've got names :)
        if (o2.getLabel().indexOf('?') == -1) o2.drawObject(g2);
        if (o3.getLabel().indexOf('?') == -1) o3.drawObject(g2);

        if (o2.getLabel().indexOf('?') == -1 && r1.getLabel().indexOf('?') == -1) r1.drawRelation(g2);
        if (o3.getLabel().indexOf('?') == -1 && r2.getLabel().indexOf('?') == -1) r2.drawRelation(g2);
    }

    public void changeRelation(boolean inverse1, boolean inverse2) {
        if (!inverse1) {
            r1 = new SemanticRelationRepresentation(o1, o2, "Relation I");
        } else {
            r1 = new SemanticRelationRepresentation(o2, o1, "Relation I");
        }
        if (!inverse2) {
            r2 = new SemanticRelationRepresentation(o2, o3, "Relation II");
        } else {
            r2 = new SemanticRelationRepresentation(o3, o2, "Relation II");
        }
    }

    public void setLabels(String lo1, String lo2, String lo3, String lr1, String lr2) {
        o1.setLabel(lo1);
        o2.setLabel(lo2);
        o3.setLabel(lo3);
        r1.setLabel(lr1);
        r2.setLabel(lr2);
    }
}
