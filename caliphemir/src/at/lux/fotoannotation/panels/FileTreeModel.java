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
package at.lux.fotoannotation.panels;

import at.lux.fotoannotation.utils.ImageFileFilter;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Date: 12.01.2005
 * Time: 23:32:53
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FileTreeModel extends DefaultTreeModel {
    private FileFilter filter = new ImageFileFilter();
    private List<File> fileRoots = null;
    private HashMap<File, Boolean> fileRootIsLeaf = null;

    /**
     * Creates a tree in which any node can have children.
     */
    public FileTreeModel() {
        super(null);
        setRoot(new DefaultMutableTreeNode(new FileNodeObject(getFileRoot())));
    }

    private File getFileRoot() {
        File root = null;
        try {
            String tmpFile = new File(".").getCanonicalPath();
            if (tmpFile.startsWith("/")) {
                // its a unix system ... so take "/"
                root = new File("/");
            } else {
                // its windows and has a letter for the drive ...
                root = new File(tmpFile.charAt(0) + ":/");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    /**
     * Returns the child of <I>parent</I> at index <I>index</I> in the parent's
     * child array.  <I>parent</I> must be a node previously obtained from
     * this data source. This should not return null if <i>index</i>
     * is a valid index for <i>parent</i> (that is <i>index</i> >= 0 &&
     * <i>index</i> < getChildCount(<i>parent</i>)).
     *
     * @param parent a node in the tree, obtained from this data source
     * @return the child of <I>parent</I> at index <I>index</I>
     */
    public Object getChild(Object parent, int index) {
        SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(null);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
        if (node.getUserObject() instanceof String) {
            return null;
//            return new DefaultMutableTreeNode(new FileNodeObject(getFileRoots().get(index)));
        } else if (node.getUserObject() instanceof FileNodeObject) {
            File f = ((FileNodeObject) node.getUserObject()).getContent();
            if (f.isDirectory()) {
                return new DefaultMutableTreeNode(new FileNodeObject(f.listFiles(filter)[index]));
            } else {
                return null;
            }
        }
        System.setSecurityManager(sm);
        return null;
    }

    /**
     * Returns the number of children of <I>parent</I>.  Returns 0 if the node
     * is a leaf or if it has no children.  <I>parent</I> must be a node
     * previously obtained from this data source.
     *
     * @param parent a node in the tree, obtained from this data source
     * @return the number of children of the node <I>parent</I>
     */
    public int getChildCount(Object parent) {
        SecurityManager sm = System.getSecurityManager();
        System.setSecurityManager(null);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
        if (node.getUserObject() instanceof String) {
            return 0;
//            return getFileRoots().size();
        } else if (node.getUserObject() instanceof FileNodeObject) {
            File f = ((FileNodeObject) node.getUserObject()).getContent();
            if (f.isDirectory()) {
                return f.listFiles(filter).length;
            } else {
                return 0;
            }
        }
        System.setSecurityManager(sm);
        return 0;
    }

/*
    private List<File> getFileRoots() {
//        SecurityManager sm = System.getSecurityManager();
//        System.setSecurityManager(null);
        List<File> results = new LinkedList<File>();
        if (fileRoots == null) {
            File[] roots = File.listRoots();
            for (int i = 0; i < roots.length; i++) {
                results.add(roots[i]);
            }
            fileRoots = results;
        }
//        System.setSecurityManager(sm);
        return fileRoots;
    }
*/

/*
    private HashMap<File, Boolean> getRootIsLeaf() {
        List<File> roots = getFileRoots();
        if (fileRootIsLeaf == null) {
            fileRootIsLeaf = new HashMap<File, Boolean>(roots.size());
            for (Iterator<File> iterator = roots.iterator(); iterator.hasNext();) {
                File file = iterator.next();
                if (file.listFiles(filter) == null || file.listFiles(filter).length == 0)
                    fileRootIsLeaf.put(file, true);
                else
                    fileRootIsLeaf.put(file, false);
            }
        }
        return fileRootIsLeaf;
    }
*/

    /**
     * Returns whether the specified node is a leaf node.
     * The way the test is performed depends on the
     * <code>askAllowsChildren</code> setting.
     *
     * @param parent the node to check
     * @return true if the node is a leaf node
     * @see #asksAllowsChildren
     * @see javax.swing.tree.TreeModel#isLeaf
     */
    public boolean isLeaf(Object parent) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent;
        if (node.getUserObject() instanceof String) {
            return false;
        } else if (node.getUserObject() instanceof FileNodeObject) {
            File f = ((FileNodeObject) node.getUserObject()).getContent();
            if (f.equals(root)) {
                return false;
            } else return !f.isDirectory();
        }
        return true;
    }

    /**
     * Returns the index of child in parent.
     * If either the parent or child is <code>null</code>, returns -1.
     *
     * @param parent a note in the tree, obtained from this data source
     * @param child  the node we are interested in
     * @return the index of the child in the parent, or -1
     *         if either the parent or the child is <code>null</code>
     */
    public int getIndexOfChild(Object parent, Object child) {
        return super.getIndexOfChild(parent, child);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Tells how leaf nodes are determined.
     *
     * @return true if only nodes which do not allow children are
     *         leaf nodes, false if nodes which have no children
     *         (even if allowed) are leaf nodes
     * @see #asksAllowsChildren
     */
    public boolean asksAllowsChildren() {
        return super.asksAllowsChildren();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Sets the root file of the tree ...
     *
     * @param newRoot
     */
    public void setRoot(File newRoot) {
        setRoot(new DefaultMutableTreeNode(new FileNodeObject(newRoot)));
        reload();
    }
}
