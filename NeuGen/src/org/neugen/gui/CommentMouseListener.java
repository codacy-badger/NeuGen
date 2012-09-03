/*
 * File: NewCommentMouseListener.java
 * Created on 30.07.2009, 11:37:23
 *
 */
package org.neugen.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Properties;

import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.apache.log4j.Logger;
import org.neugen.datastructures.Pair;
import org.neugen.datastructures.xml.XMLNode;
import org.neugen.parsers.SimplifiedInheritance;

/**
 * @author Sergei Wolf
 */
public final class CommentMouseListener extends MouseAdapter {

    private static final Logger logger = Logger.getLogger(CommentMouseListener.class.toString());
    //tree view for parameters, mouseListener
    private JEditorPane htmlPane;
    private List<Properties> properList;
    private Properties defaultComment;
    private String propKey;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public CommentMouseListener(JEditorPane htmlPane, List<Properties> prop, JPopupMenu menu, Properties com) {
        this.htmlPane = htmlPane;
        this.properList = prop;
        this.defaultComment = com;
    }

    public String getPropKey() {
        return propKey;
    }

    public void setPropKey(String key) {
        changes.firePropertyChange("key", propKey, key);
        propKey = key;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getComponent() instanceof JTree) {
            //logger.info("mouse pressed!");
            JTree t = (JTree) (e.getComponent());
            //returns the path in the selection
            TreePath path = t.getSelectionPath();
            if (path != null) {
                XMLNode node = (XMLNode) (path.getLastPathComponent());
                //System.out.println(node.getHandler().getClass().toString() + " " + node.getKey());
                //System.out.println(node.getPathForProperties());
                String key = node.getPathForProperties();
                //logger.info("key: " + key);
                for (int i = 0; i < properList.size(); i++) {
                    Properties properties = properList.get(i);
                    if (properties.containsKey(key)) {
                        String value = properties.getProperty(key);
                        //logger.info("key: " + key);
                        //logger.info("value: " + value);
                        //System.out.println("size of prop: " + properties.size());
                        if (value.equals("")) {
                            if (node.isInherited()) {
                                SimplifiedInheritance simpInh = new SimplifiedInheritance();
                                Pair<XMLNode, Integer> heritage = simpInh.getHeritage(2, node);
                                XMLNode heritageNode = heritage.first;
                                String heritageKey = heritageNode.getPathForProperties();
                                value = properties.getProperty(heritageKey);

                                if (value.equals("")) {
                                    while (heritage != null) {
                                        heritage = simpInh.getHeritage(2, heritageNode);
                                        if(heritage == null) {
                                            break;
                                        }
                                        heritageNode = heritage.first;
                                        heritageKey = heritageNode.getPathForProperties();
                                        value = properties.getProperty(heritageKey);
                                        if(!value.equals("")) {
                                            break;
                                        }
                                    }
                                }
                                //logger.info("Der Konote ist vererbt und erbt ein Kommentar, der Pfad ist: " + propKey);
                                //System.out.println("key: " + key);
                                //System.out.println(value);
                            }
                        }

                        if (value.equals("")) {
                            String propKeyLoc = node.getKey();
                            String tmpVal = this.defaultComment.getProperty(propKeyLoc);
                            if (tmpVal != null) {
                                value = tmpVal;
                            }
                        }

                        if (value.equals("")) {
                            //logger.info("Der Konote ist vererbt aber findet kein Kommentar");
                            value = "no comment exists for: " + node.getKey();
                        }

                        this.setPropKey(key);
                        value = value.replaceAll(System.getProperty("line.separator"), "<br />");
                        value = value.replaceAll("\n", "<br />");
                        htmlPane.setText(value);
                        //htmlPane.setText(value.replaceAll("\n", "<br />"));
                        //htmlPane.setText(value.replaceAll("\r", "<br />"));
                        //htmlPane.setText(value.replaceAll(lineSep, "<br />"));
                        break;
                    } else {
                        if (i == properList.size()) {
                            logger.info("key not found!");
                        }
                        //logger.info("key not found in: " + i + ". of " + properList.size() + " properties: " + key);
                    }
                }
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }
}
