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
package at.lux.fotoannotation.dialogs;

import at.lux.fotoannotation.AgentComboBoxModel;
import at.lux.fotoannotation.mpeg7.Mpeg7DescriptionMetadata;
import org.jdom.Element;
import org.jdom.Namespace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class NewAgentDialog extends JDialog implements ActionListener, NewDescriptorDialogInterface {
    private AgentComboBoxModel model;
    Element agent = null;
    private AgentPanel agentPanel;

    public NewAgentDialog(AgentComboBoxModel model) {
        this.model = model;
        init();
    }

    public NewAgentDialog(AgentComboBoxModel model, Frame owner) {
        super(owner, true);
        this.model = model;
        init();
    }

    public NewAgentDialog(Frame owner) {
        super(owner, true);
        model = null;
        init();
    }

    private void init() {
        this.setTitle("Create new person object");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton ok = new JButton("OK");
        ok.addActionListener(this);
        ok.setActionCommand("ok");
        ok.setMnemonic('o');
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        cancel.setActionCommand("cancel");
        buttonPanel.add(ok);
        buttonPanel.add(cancel);

        agentPanel = new AgentPanel();
        this.getContentPane().add(agentPanel, BorderLayout.CENTER);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ok")) {
            boolean hide = false;
            if (model != null) {
                hide = addAgent();
            } else {
                hide = createDocument();
            }
            if (hide) setVisible(false);
        } else if (e.getActionCommand().equals("cancel")) {
            setVisible(false);
        }
    }

    private boolean addAgent() {
        boolean agentadded = false;
        String fname = agentPanel.getFamilyName();
        String gname = agentPanel.getGivenName();
        String org = agentPanel.getOrganization();
        String email = agentPanel.getEmail();
        String phone = agentPanel.getPhone();
        String fax = agentPanel.getFax();
        String url = agentPanel.getUrl();
        String nick = agentPanel.getNickname();
        String address = agentPanel.getAddress();
        String description = agentPanel.getDescription();
        ArrayList addressLines = null;
        if (address.length() > 0) {
            addressLines = new ArrayList();
            StringTokenizer t = new StringTokenizer(address, "\n", false);
            while (t.hasMoreElements()) {
                String s = t.nextToken();
                addressLines.add(s.trim());
            }
        }
        if (org.length() < 1) org = null;
        if (phone.length() < 1) phone = null;
        if (fax.length() < 1) fax = null;
        if (url.length() < 1) url = null;
        if (email.length() < 1) email = null;
        if (nick.length() < 1) nick = null;

        System.out.println("[at.lux.fotoannotation.NewAgentDialog] Agents name: " + gname + " " + fname);
        if (fname.length() > 1 && gname.length() > 1) {
            System.out.println("[at.lux.fotoannotation.NewAgentDialog] Generating MPEG-7");
            Mpeg7DescriptionMetadata m = new Mpeg7DescriptionMetadata("1.0", gname, fname, org, addressLines, phone, fax, email, url, null, null, null);
            Element e = m.getDescriptionMetadata();
            Element tmpAgent = (Element) (e.getChild("Creator", e.getNamespace()).getChild("Agent", e.getNamespace())).detach();
            if (nick != null) {
                tmpAgent.getChild("Name", tmpAgent.getNamespace()).addContent(new Element("GivenName", tmpAgent.getNamespace()).addContent(nick));
            }
            model.addAgent(tmpAgent);
            agentadded= true;
        } else {
            JOptionPane.showMessageDialog(this, "At least given name and family name are needed!");
        }
        return agentadded;

    }

    private boolean createDocument() {
        boolean agentadded = false;
        String fname = agentPanel.getFamilyName();
        String gname = agentPanel.getGivenName();
        String org = agentPanel.getOrganization();
        String email = agentPanel.getEmail();
        String phone = agentPanel.getPhone();
        String fax = agentPanel.getFax();
        String url = agentPanel.getUrl();
        String nick = agentPanel.getNickname();
        String address = agentPanel.getAddress();
        String description = agentPanel.getDescription();
        ArrayList addressLines = null;
        if (address.length() > 0) {
            addressLines = new ArrayList();
            StringTokenizer t = new StringTokenizer(address, "\n", false);
            while (t.hasMoreElements()) {
                String s = t.nextToken();
                addressLines.add(s.trim());
            }
        }
        if (org.length() < 1) org = null;
        if (phone.length() < 1) phone = null;
        if (fax.length() < 1) fax = null;
        if (url.length() < 1) url = null;
        if (email.length() < 1) email = null;
        if (nick.length() < 1) nick = null;

        System.out.println("[at.lux.fotoannotation.NewAgentDialog] Agents name: " + gname + " " + fname);
        if (fname.length() > 1 && gname.length() > 1) {
            System.out.println("[at.lux.fotoannotation.NewAgentDialog] Generating MPEG-7");
            Mpeg7DescriptionMetadata m = new Mpeg7DescriptionMetadata("1.0", gname, fname, org, addressLines, phone, fax, email, url, null, null, null);
            Element e = m.getDescriptionMetadata();
            Element tmpagent = (Element) (e.getChild("Creator", e.getNamespace()).getChild("Agent", e.getNamespace())).detach();
            if (nick != null) {
                tmpagent.getChild("Name", tmpagent.getNamespace()).addContent(new Element("GivenName", tmpagent.getNamespace()).addContent(nick));
            }
            Namespace mpeg7, xsi;
            mpeg7 = Namespace.getNamespace("", "urn:mpeg:mpeg7:schema:2001");
            xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            agent = new Element("SemanticBase", mpeg7).setAttribute("type", "AgentObjectType", xsi);
            Element label = new Element("Label", mpeg7).addContent(new Element("Name", mpeg7).addContent(gname + " " + fname));
            Element definition = null;
            if (description.length() > 0) {
                definition = new Element("Definition", mpeg7).addContent(new Element("FreeTextAnnotation", mpeg7).addContent(description));
            }
            agent.addContent(label);
            if (definition != null) {
                agent.addContent(definition);
            }
            agent.addContent(tmpagent);
            agentadded = true;
        } else {
            JOptionPane.showMessageDialog(this, "At least given name and family name are needed!");
        }
        return agentadded;
    }

    public Element createXML() {
        return agent;
    }
}

