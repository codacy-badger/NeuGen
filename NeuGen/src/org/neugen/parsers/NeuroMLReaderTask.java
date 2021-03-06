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
package org.neugen.parsers;

import org.neugen.utils.Utils;
import org.neugen.gui.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.neugen.datastructures.Net;
import org.neugen.datastructures.neuron.Neuron;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Sergei Wolf
 */
public final class NeuroMLReaderTask extends Task<Void, Void> {

    private final static Logger logger = Logger.getLogger(NeuroMLReaderTask.class.getName());
    private final File file;
    public int fileLength = 0;
    public NeuGenView ngView;

    public NeuroMLReaderTask(Application app, File file) {
        super(app);
        this.file = file;
        fileLength = (int) file.length();
        ngView = NeuGenView.getInstance();
    }

    public void setMyProgress(int value, int min, int max) {
        setProgress(value, min, max);
    }

    @Override
    protected Void doInBackground() throws IOException {
        try {
            FileInputStream instream = null;
            InputSource is = null;
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            MorphMLReader mmlBuilder = new MorphMLReader(this);
            xmlReader.setContentHandler(mmlBuilder);
            instream = new FileInputStream(file);
            is = new InputSource(instream);
            ngView.outPrintln("reading neuroML file: " + file.getPath() + "\n");
            xmlReader.parse(is);
            Net net = mmlBuilder.getNet();
            for (Neuron neuron : net.getNeuronList()) {
                neuron.infoNeuron();
            }
            net.setTotalNumOfSegments();
            ngView.setNet(net);
            /*
            ngView.outPrintln("Total number of segments(net): " + net.getTotalNumberOfSegments() + "\n");
            ngView.outPrintln("Total number of segments(reader): " + mmlBuilder.getTotalNumSegs());
            ngView.outPrintln("Total number of sections(reader): " + mmlBuilder.getTotalNumSecs());*/
        } catch (Exception e) {
            logger.error(e, e);
        }
        return null;
    }

    @Override
    protected void succeeded(Void result) {
        ngView.setNetExist(true);
        ngView.enableButtons();
        System.gc();
        ngView.outPrintln(Utils.getMemoryStatus());
        ngView.visualizeData().run();
    }
}
