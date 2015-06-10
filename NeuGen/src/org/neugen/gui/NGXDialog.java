/* 
 * Copyright (c) 2005–2012 Goethe Center for Scientific Computing - Simulation and Modelling (G-CSC Frankfurt)
 * Copyright (c) 2012-2015 Goethe Center for Scientific Computing - Computational Neuroscience (G-CSC Frankfurt)
 * 
 * This file is part of NeuGen.
 *
 * NeuGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * see: http://opensource.org/licenses/LGPL-3.0
 *      file://path/to/NeuGen/LICENSE
 *
 * NeuGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * This version of NeuGen includes copyright notice and attribution requirements.
 * According to the LGPL this information must be displayed even if you modify
 * the source code of NeuGen. The copyright statement/attribution may not be removed.
 *
 * Attribution Requirements:
 *
 * If you create derived work you must do the following regarding copyright
 * notice and author attribution.
 *
 * Add an additional notice, stating that you modified NeuGen. In addition
 * you must cite the publications listed below. A suitable notice might read
 * "NeuGen source code modified by YourName 2012".
 * 
 * Note, that these requirements are in full accordance with the LGPL v3
 * (see 7. Additional Terms, b).
 *
 * Publications:
 *
 * S. Wolf, S. Grein, G. Queisser. NeuGen 2.0 -
 * Employing NeuGen 2.0 to automatically generate realistic
 * morphologies of hippocapal neurons and neural networks in 3D.
 * Neuroinformatics, 2013, 11(2), pp. 137-148, doi: 10.1007/s12021-012-9170-1
 *
 *
 * J. P. Eberhard, A. Wanner, G. Wittum. NeuGen -
 * A tool for the generation of realistic morphology 
 * of cortical neurons and neural networks in 3D.
 * Neurocomputing, 70(1-3), pp. 327-343, doi: 10.1016/j.neucom.2006.01.028
 *
 */

package org.neugen.gui;

import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import org.neugen.datastructures.xml.XMLNode;
import org.neugen.datastructures.xml.XMLObject;

/**
 *
 * @author stephan
 */
public class NGXDialog extends javax.swing.JDialog {
    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;
    private double density;
    
    private int returnStatus = RET_CANCEL;

    /** Creates new form NGXDialog */
    public NGXDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.neugen.gui.NeuGenApp.class).getContext().getResourceMap(NGXDialog.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
        jTextField2.setName("jTextField2"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(28, 28, 28)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jButton1)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jLabel2)
                                .add(jLabel1)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(36, 36, 36)
                                .add(jButton2))
                            .add(layout.createSequentialGroup()
                                .add(28, 28, 28)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(jTextField2)
                                    .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)))))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(jLabel3)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 343, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(78, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(34, Short.MAX_VALUE)
                .add(jLabel3)
                .add(1, 1, 1)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(28, 28, 28)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(57, 57, 57)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton2))
                .add(49, 49, 49))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
   doClose(RET_CANCEL);  
}//GEN-LAST:event_jButton2ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    doClose(RET_OK);
}//GEN-LAST:event_jButton1ActionPerformed

 private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
 
 public float getNpartsDensity() {
     
     System.err.println(jTextField1.getText());
     if (jTextField1.getText().isEmpty()) {
         this.density = 0.025f;
         return 0.025f; /// default value
     } else {
        this.density = Float.parseFloat(jTextField1.getText());
        return Float.parseFloat(jTextField1.getText());
     }
 }
 
 
  public void correct_params(XMLObject paramRoot) {
    
           
            XMLObject obj = paramRoot; 
            //if ("net".equals(entry.getKey())) {
               Enumeration<XMLNode> childs = obj.children();
            
               while (childs.hasMoreElements()) {
                 XMLNode node = childs.nextElement();
                 System.err.println("Node: " + node.toString());
                 if ("neuron".equals(node.toString())) {
                    Enumeration<XMLNode> childs2 = node.children();
                    while (childs2.hasMoreElements()) {
                        XMLNode node2 = childs2.nextElement();
                        if ("axon".equals(node2.toString())) {
                            Enumeration<XMLNode> childs3 = node2.children();
                            while (childs3.hasMoreElements()) {
                                XMLNode child4 = childs3.nextElement();
                                System.err.println("axon child: " + child4.toString());
                                if ("gen_0".equals(child4.toString())) {
                                    Enumeration<XMLNode> childs5 = child4.children();
                                    
                                    while (childs5.hasMoreElements()) {
                                        XMLNode child6 = childs5.nextElement();
                            
                                        if ("nparts_density".equals(child6.getKey())) {
                                            System.err.println("child6 (before): " + child6.toString());
                                            System.err.println("child6's key (before): " + child6.getKey());
                                            child6.setValue(density);
                                        
                                        
                                            System.err.println("child6 (after): " + child6.toString());
                                            System.err.println("child6's key (after): " + child6.getKey());
                                        }
                                        
                                        if  ("siblings".equals(child6.getKey())) {
                                            correct_siblings(child6);
                                        }
                                        
                                    }
                                }
                            }
                        
                            
                            
                            } else if ("dendrite".equals(node2.toString())) {
                               
                             Enumeration<XMLNode> childs3 = node2.children();
                             while (childs3.hasMoreElements()) {
                                XMLNode child4 = childs3.nextElement();
                                System.err.println("axon child: " + child4.toString());
                                if ("gen_0".equals(child4.toString())) {
                                    Enumeration<XMLNode> childs5 = child4.children();
                                    
                                    while (childs5.hasMoreElements()) {
                                        XMLNode child6 = childs5.nextElement();
                            
                                        if ("nparts_density".equals(child6.getKey())) {
                                            System.err.println("child6 (before): " + child6.toString());
                                            System.err.println("child6's key (before): " + child6.getKey());
                                            child6.setValue(density);
                                        
                                        
                                            System.err.println("child6 (after): " + child6.toString());
                                            System.err.println("child6's key (after): " + child6.getKey());
                                        }
                                        
                                        if  ("siblings".equals(child6.getKey())) {
                                            correct_siblings(child6);
                                        }
                                        
                                    }
                                }
                            }
                                
                            
                            } else {
                            
                            }
                    }
                 }
               }
         }
 
 
 
 public void correct_params() {
      for (Entry<String, XMLObject> entry : NeuGenView.getInstance().getParamTrees().entrySet()) {
            System.err.println("Key:" + entry.getKey());
            XMLObject obj = entry.getValue();
            //if ("net".equals(entry.getKey())) {
               Enumeration<XMLNode> childs = obj.children();
            
               while (childs.hasMoreElements()) {
                 XMLNode node = childs.nextElement();
                 System.err.println("Node: " + node.toString());
                 if ("neuron".equals(node.toString())) {
                    Enumeration<XMLNode> childs2 = node.children();
                    while (childs2.hasMoreElements()) {
                        XMLNode node2 = childs2.nextElement();
                        if ("axon".equals(node2.toString())) {
                            Enumeration<XMLNode> childs3 = node2.children();
                            while (childs3.hasMoreElements()) {
                                XMLNode child4 = childs3.nextElement();
                                System.err.println("axon child: " + child4.toString());
                                if ("gen_0".equals(child4.toString())) {
                                    Enumeration<XMLNode> childs5 = child4.children();
                                    
                                    while (childs5.hasMoreElements()) {
                                        XMLNode child6 = childs5.nextElement();
                            
                                        if ("nparts_density".equals(child6.getKey())) {
                                            System.err.println("child6 (before): " + child6.toString());
                                            System.err.println("child6's key (before): " + child6.getKey());
                                            child6.setValue(density);
                                        
                                        
                                            System.err.println("child6 (after): " + child6.toString());
                                            System.err.println("child6's key (after): " + child6.getKey());
                                        }
                                        
                                        if  ("siblings".equals(child6.getKey())) {
                                            correct_siblings(child6);
                                        }
                                        
                                    }
                                }
                            }
                        
                            
                            
                            } else if ("dendrite".equals(node2.toString())) {
                               
                             Enumeration<XMLNode> childs3 = node2.children();
                             while (childs3.hasMoreElements()) {
                                XMLNode child4 = childs3.nextElement();
                                System.err.println("axon child: " + child4.toString());
                                if ("gen_0".equals(child4.toString())) {
                                    Enumeration<XMLNode> childs5 = child4.children();
                                    
                                    while (childs5.hasMoreElements()) {
                                        XMLNode child6 = childs5.nextElement();
                            
                                        if ("nparts_density".equals(child6.getKey())) {
                                            System.err.println("child6 (before): " + child6.toString());
                                            System.err.println("child6's key (before): " + child6.getKey());
                                            child6.setValue(density);
                                        
                                        
                                            System.err.println("child6 (after): " + child6.toString());
                                            System.err.println("child6's key (after): " + child6.getKey());
                                        }
                                        
                                        if  ("siblings".equals(child6.getKey())) {
                                            correct_siblings(child6);
                                        }
                                        
                                    }
                                }
                            }
                                
                            
                            } else {
                            
                            }
                    }
                 }
               }
         }
 }
 
 
 
 
 private void correct_siblings(XMLNode child) {
     Enumeration<XMLNode> childs = child.children(); // only one child called siblings
     XMLNode child2 = childs.nextElement();
     Enumeration<XMLNode> childs3 = child2.children();
     
     while (childs3.hasMoreElements()) {
      XMLNode child4 = childs3.nextElement();
        
     
      if ("nparts_density".equals(child4.getKey())) {
          child4.setValue(density);
          
      }
      
      if  ("siblings".equals(child4.getKey())) {                                 
         correct_siblings(child4);
      }
    }
 }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NGXDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NGXDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NGXDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NGXDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                NGXDialog dialog = new NGXDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
