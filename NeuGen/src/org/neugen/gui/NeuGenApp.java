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

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.neugen.utils.NeuGenLogger;

/**
 * The main class of the application.
 * 
 * @author Alexander Wanner
 * @author Sergei Wolf
 */
public final class NeuGenApp extends SingleFrameApplication {

    private final static Logger logger = Logger.getLogger(NeuGenApp.class.getName());
    public static boolean antiAliasing = true;

    /** At startup create and show the main frame of the application. */
    @Override
    protected void startup() {
        //UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
        //Options.setDefaultIconSize(new Dimension(18, 18));
        String lafName =
                LookUtils.IS_OS_WINDOWS
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();

        try {
            UIManager.setLookAndFeel(lafName);
            logger.info("Look and feel name: " + lafName);
        } catch (Exception e) {
            logger.error("Can't set look & feel:" + e, e);
        }

        /*
        PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
        try {
        UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (Exception e) {
        }
         *
         */
        NeuGenView ngView = new NeuGenView(this);
        ngView.getFrame().setLocationRelativeTo(null);

        NeuGenView.setInstance(ngView);
        show(ngView);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    @Override
    protected void shutdown() {
        logger.info("End logging:*******************************************************\n\n");
        // The default shutdown saves session window state.
//        super.shutdown();
        // Now perform any other shutdown tasks you need.
//        System.exit(0);
    }

    /** A convenient static getter for the application instance. */
    public static NeuGenApp getApplication() {
        return Application.getInstance(NeuGenApp.class);
    }

    public static void handleLicenseKey() throws Exception {
        // License key
        File lkf = new File(".lk");
        boolean lkFound = false;
        //generated code: KeyGenerator.getInternCodedKey..(newPass)
        String encodedCode = "T0zBdfTmwauvn/7+QaOuaaSJgenjJ1+/";
        String decodedLK = KeyGenerator.getInternDecodedKey(encodedCode);

        if (lkf.exists()) {
            try {
                BufferedReader lkReader = new BufferedReader(new FileReader(lkf));
                String licenseString = lkReader.readLine();
                licenseString = KeyGenerator.getExternDecodedKey(licenseString);
                if (licenseString != null && licenseString.equals(decodedLK)) {
                    lkFound = true;
                }
            } catch (FileNotFoundException e) {
                logger.error(e, e);
            } catch (IOException e) {
                logger.error(e, e);
            }
        }

        if (!lkFound) {
            try {
                String licenseKey = "";
                NGKeyDialog keyDialog = new NGKeyDialog(getApplication().getMainFrame(), true);
                keyDialog.setLocationByPlatform(true);
                do {
                    keyDialog.setVisible(true);
                    if (NGKeyDialog.RET_OK == keyDialog.getReturnStatus()) {
                        char[] key = keyDialog.getKeyField().getPassword();
                        for (char c : key) {
                            licenseKey += c;
                        }
                        if (!licenseKey.equals(decodedLK)) {
                            keyDialog.getKeyField().setText("");
                            String messagePrefix = licenseKey + " Invalid license key. Please try again.. ";
                            JOptionPane.showMessageDialog(getApplication().getMainFrame(),
                                    messagePrefix,
                                    "Error Message",
                                    JOptionPane.ERROR_MESSAGE);
                            licenseKey = "";
                        }
                    } else {
                        System.exit(0);
                    }
                } while (!licenseKey.equals(decodedLK));
                FileWriter fw = new FileWriter(".lk");
                String codeToWrite = KeyGenerator.getExternCodedKey(decodedLK);
                fw.write(codeToWrite);
                fw.close();
            } catch (IOException e) {
                logger.error(e, e);
            }
        }
    }

    /** Main method launching the application.*/
    public static void main(File logProperties, final String[] args) {
        NeuGenLogger.initLogger(logProperties);

//        try {
//            handleLicenseKey();
//        } catch (Exception ex) {
//            logger.error(ex);
//        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    launch(NeuGenApp.class, args);
                }
            });

    }
    
    /** Main method launching the application.*/
    public static void main(final String[] args) {
        NeuGenLogger.initLogger();

//        try {
//            handleLicenseKey();
//        } catch (Exception ex) {
//            logger.error(ex);
//        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    launch(NeuGenApp.class, args);
                }
            });

    }
}
