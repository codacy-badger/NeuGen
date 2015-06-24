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
/// package's name
package org.neugen.backend;

/// imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.neugen.datastructures.Net;
import org.neugen.datastructures.Region;
import org.neugen.datastructures.xml.XMLNode;
import org.neugen.datastructures.xml.XMLObject;
import org.neugen.gui.NeuGenConstants;
import org.neugen.gui.NeuGenLib;
import org.neugen.parsers.DefaultInheritance;
import org.neugen.parsers.NGX.NGXWriter;
import org.neugen.parsers.NeuGenConfigStreamer;
import org.neugen.utils.NeuGenLogger;
import org.neugen.utils.Utils;

/**
 * @brief provide some backend functionality
 * @author stephanmg <stephan@syntaktischer-zucker.de>
 *
 * @note maybe a configuration builder (SettingsBuilder) is a good choice
 */
public final class NGBackend {

	/// public static members
	public static final Logger logger = Logger.getLogger(NGBackend.class.getName());

	/// private static members
	private static final String ENCODING = "UTF-8";
	private static final NeuGenLib ngLib = new NeuGenLib();
	private static final double DIST_SYNAPSE = 1.0;
	private static final double N_PARTS_DENSITY = 0.01;

	/**
	 * @brief default ctor
	 */
	public NGBackend() {
		NeuGenConstants.WITH_GUI = false;
		NeuGenLogger.initLogger();
	}

	/**
	 * @brief enhanced ctor
	 * @param with_gui
	 */
	public NGBackend(boolean with_gui) {
		NeuGenConstants.WITH_GUI = with_gui;
	}

	/**
	 * @brief executes just the project
	 *
	 * @param projectType
	 */
	public void execute(String projectType) {
		ngLib.run(projectType);
	}

	/**
	 * @brief loads the initial parameters from project directory
	 *
	 * @param file
	 * @param root
	 * @return
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	private XMLObject loadParam(File file) {
		XMLObject root = null;
		try {
			NeuGenConfigStreamer stream = new NeuGenConfigStreamer(null);
			root = stream.streamIn(file);
			DefaultInheritance inhProzess = new DefaultInheritance();
			root = inhProzess.process(root);
		} catch (IOException ioe) {
			logger.fatal("Error when opening input file parameter: " + ioe.toString());
			ioe.printStackTrace();
		}
		return root;
	}

	/**
	 * @brief get's the project property
	 *
	 * @param projectPath
	 * @param projectType
	 */
	private Properties getProjectProp(String projectPath, String projectType) {
		Properties prop = new Properties();
		String filePath = projectPath + System.getProperty("file.separator") + NeuGenConstants.NEUGEN_PROJECT_FILE;
		File projectInfoFile = new File(filePath);
		try {
			InputStream is = new FileInputStream(projectInfoFile);
			try {
				prop.loadFromXML(is);
			} catch (IOException ex) {
				logger.error(ex);
			}
		} catch (FileNotFoundException ex) {
			logger.error(ex);
		}
		prop.setProperty(NeuGenConstants.PROP_DATE_KEY, (new Date().toString()));
		prop.setProperty(NeuGenConstants.PROP_PROJECT_NAME_KEY, projectType);
		String projectName = new File(projectPath).getName();
		prop.setProperty(projectName, prop.getProperty(NeuGenConstants.PROPERTIES_KEY));
		return prop;
	}

	/**
	 * @brief creates a project
	 *
	 * @param projectPath
	 * @param projectType
	 * @param force
	 * @return
	 */
	public Map<String, XMLObject> create_and_open_project(String projectPath, String projectType, boolean force) {
		logger.info("project path (project type: " + projectType + "): " + projectPath);
		File projectDir = new File(projectPath);
		if (!NGBackendUtil.fileExists(projectDir, force)) {
			if (projectType.equals(NeuGenConstants.HIPPOCAMPUS_PROJECT)) {
				String sourcePath = NeuGenConstants.CONFIG_DIR + System.getProperty("file.separator") + NeuGenConstants.HIPPOCAMPUS_PROJECT.toLowerCase();
				File sourceDir = new File(sourcePath);
				try {
					Utils.copyDir(sourceDir, projectDir);
					Properties prop = getProjectProp(projectPath, projectType);
					OutputStream out = new FileOutputStream(projectPath + System.getProperty("file.separator") + NeuGenConstants.NEUGEN_PROJECT_FILE);
					prop.storeToXML(out, "NeuGen project directory ", ENCODING);
					out.close();
					Region.setCortColumn(false);
					Region.setCa1Region(true);
				} catch (FileNotFoundException ex) {
					logger.error(ex);
				} catch (IOException ex) {
					logger.error(ex);
				}
			} else if (projectType.equals(NeuGenConstants.NEOCORTEX_PROJECT)) {
				String sourcePath = NeuGenConstants.CONFIG_DIR + System.getProperty("file.separator") + NeuGenConstants.NEOCORTEX_PROJECT.toLowerCase();
				logger.info("source path: " + sourcePath);
				logger.info("project path: " + projectDir.getPath());
				File sourceDir = new File(sourcePath);
				logger.info(projectType.toLowerCase() + ", path: " + sourceDir.getPath());
				try {
					Utils.copyDir(sourceDir, projectDir);
					Properties prop = getProjectProp(projectPath, projectType);
					OutputStream out = new FileOutputStream(projectPath + System.getProperty("file.separator") + NeuGenConstants.NEUGEN_PROJECT_FILE);
					prop.storeToXML(out, "NeuGen project directory ", ENCODING);
					out.close();
					Region.setCortColumn(true);
					Region.setCa1Region(false);
				} catch (FileNotFoundException ex) {
					logger.error(ex);
				} catch (IOException ex) {
					logger.error(ex);
				}
			} else {
				logger.fatal("Wrong project type specified aborting: "
					+ projectType + ". Supported project types are "
					+ NeuGenConstants.NEOCORTEX_PROJECT + " and "
					+ NeuGenConstants.HIPPOCAMPUS_PROJECT + ".");
			}
		}
		return initProjectParam(projectPath, projectType);
	}

	/**
	 * @brief saves the PARAM parameters
	 *
	 * @param currentRoot
	 * @param projectDirPath
	 */
	private void save_param(XMLNode currentRoot, String projectDirPath) {
		save(currentRoot, projectDirPath, NeuGenConstants.PARAM);
	}

	/**
	 * @brief saves the INTERNA parameters
	 *
	 * @param currentRoot
	 * @param projectDirPath
	 */
	private void save_interna(XMLNode currentRoot, String projectDirPath) {
		save(currentRoot, projectDirPath, NeuGenConstants.INTERNA);
	}

	/**
	 * @brief save all params
	 *
	 * @param paramTrees
	 * @param projectDirPath
	 */
	public void save(Map<String, XMLObject> paramTrees, String projectDirPath) {
		save_param(paramTrees.get(NeuGenConstants.PARAM), projectDirPath);
		save_interna(paramTrees.get(NeuGenConstants.INTERNA), projectDirPath);
	}

	/**
	 * @brief saves INTERNA or PARAM parameters
	 *
	 * @param currentRoot
	 * @param projectDirPath
	 * @param param
	 */
	private void save(XMLNode currentRoot, String projectDirPath, String param) {
		logger.info(currentRoot.getLeafCount());
		XMLObject rootCopy = XMLObject.getCopyXMLObject(XMLObject.convert(currentRoot));
		DefaultInheritance.reverseProcess(rootCopy);
		NeuGenConfigStreamer streamer = new NeuGenConfigStreamer(projectDirPath);
		String neuPath = null;
		if (param.equals(NeuGenConstants.PARAM)) {
			neuPath = projectDirPath + System.getProperty("file.separator") + NeuGenConstants.PARAM_FNAME;
		} else if (param.equals(NeuGenConstants.INTERNA)) {
			neuPath = projectDirPath + System.getProperty("file.separator") + NeuGenConstants.INTERNA_FNAME;
		}
		if (neuPath != null) {
			File neuFile = new File(neuPath);
			try {
				logger.info("Write *** " + param + " *** file to: " + neuFile.getAbsolutePath());
				streamer.streamOut(rootCopy, neuFile);
			} catch (IOException ex) {
				logger.error(ex, ex);
			}
		}
	}

	/**
	 * @brief init param table
	 *
	 * @param paramTrees
	 * @param paramPath
	 * @param internaPath
	 */
	private Map<String, XMLObject> initParamTable(Map<String, XMLObject> paramTrees, String paramPath, String internaPath) {
		XMLObject param = paramTrees.get(NeuGenConstants.PARAM);
		XMLObject interna = paramTrees.get(NeuGenConstants.INTERNA);
		Map<String, XMLObject> allParam = new HashMap<String, XMLObject>();

		allParam.put(paramPath, param);
		allParam.put(internaPath, interna);
		return allParam;
	}

	/**
	 * @brief init all parameters
	 *
	 * @param dirPath
	 * @param projectType
	 */
	private Map<String, XMLObject> initProjectParam(String dirPath, String projectType) {
		File param = new File(dirPath + System.getProperty("file.separator") + NeuGenConstants.PARAM_FNAME);
		File interna = new File(dirPath + System.getProperty("file.separator") + NeuGenConstants.INTERNA_FNAME);

		Map<String, XMLObject> paramTrees = new HashMap<String, XMLObject>();
		XMLObject paramRoot = loadParam(param);
		XMLObject internaRoot = loadParam(interna);
		paramTrees.put(NeuGenConstants.PARAM, paramRoot);
		paramTrees.put(NeuGenConstants.INTERNA, internaRoot);

		NeuGenLib.initParamData(initParamTable(paramTrees, param.getPath(), interna.getPath()), projectType);

		return paramTrees;
	}

	/**
	 * @brief generates the net actually
	 *
	 * @param projectType
	 */
	public void generate_network(String projectType) {
		ngLib.run(projectType);
		//ngLib.getNet().destroy();
		//ngLib.destroy();
	}
	
	/**
	 * @brief saves and closes project
	 * @param paramTrees
	 * @param projectDirPath
	 */
	public void save_and_close_project(Map<String, XMLObject> paramTrees, String projectDirPath) {
		/// save project
		save(paramTrees, projectDirPath);
		
		/**
		 * @todo how to close savely the project
		 */
		
		/// clear param data and destroy all net components
		NeuGenLib.clearOldParamData();
		ngLib.destroy();
		ngLib.getNet().destroy();
		
		/**
		 * @todo test if this works really
		 */
	}

	/**
	 * @brief exports a network
	 *
	 * @param type
	 * @param file
	 */
	public void export_network(String type, String file) {
		Net net = ngLib.getNet();
		if ("NGX".equalsIgnoreCase(type)) {
			logger.info("Exporting NGX data to... " + file);
			NGXWriter ngxWriter = new NGXWriter(net, new File(file));
			ngxWriter.exportNetToNGX();
		} else {
			logger.info("Unsupported exporter chosen for now.");
		}
	}

	/**
	 * @brief modify the project params and set them
	 *
	 * @param paramTrees;
	 * @param projectDirPath
	 */
	public void modifyProject(Map<String, XMLObject> paramTrees, String projectDirPath) {
		/// correct synapse distance
		modifySynapseDistance(paramTrees, projectDirPath, DIST_SYNAPSE);

		/// correct n parts density
		modifyNPartsDensity(paramTrees, projectDirPath, N_PARTS_DENSITY);

		/// save new parameters to file
		save(paramTrees, projectDirPath);
	}

	/**
	 * @brief modifies n parts density
	 *
	 * @param paramTrees
	 * @param projectDirPath
	 * @param density
	 */
	@SuppressWarnings("unchecked")
	public void modifyNPartsDensity(Map<String, XMLObject> paramTrees, String projectDirPath, double density) {
		for (Map.Entry<String, XMLObject> entry : paramTrees.entrySet()) {
			XMLObject obj = entry.getValue();

			Enumeration<XMLNode> childs = obj.children();

			while (childs.hasMoreElements()) {
				XMLNode node = childs.nextElement();
				if ("neuron".equals(node.toString())) {
					Enumeration<XMLNode> childs2 = node.children();
					while (childs2.hasMoreElements()) {
						XMLNode node2 = childs2.nextElement();
						if ("axon".equals(node2.toString())) {
							Enumeration<XMLNode> childs3 = node2.children();
							while (childs3.hasMoreElements()) {
								XMLNode child4 = childs3.nextElement();
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

										if ("siblings".equals(child6.getKey())) {
											correct_siblings(child6, "nparts_density", density);
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

										if ("siblings".equals(child6.getKey())) {
											correct_siblings(child6, "nparts_density", density);
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

	/**
	 * @brief correct synapse dist to custom value
	 *
	 * @param paramTrees
	 * @param projectDirPath
	 * @param dist_synapse
	 */
	public void modifySynapseDistance(Map<String, XMLObject> paramTrees, String projectDirPath, double dist_synapse) {
		for (Map.Entry<String, XMLObject> entry : paramTrees.entrySet()) {
			XMLObject obj = entry.getValue();
			@SuppressWarnings("unchecked")
			Enumeration<XMLNode> childs = obj.children();

			while (childs.hasMoreElements()) {
				XMLNode node = childs.nextElement();
				if ("net".equals(node.toString())) {
					@SuppressWarnings("unchecked")
					Enumeration<XMLNode> childs2 = node.children();
					while (childs2.hasMoreElements()) {
						XMLNode node2 = childs2.nextElement();
						if ("dist_synapse".equals(node2.getKey())) {
							node2.setValue(dist_synapse);
						}
					}

				}
			}
		}
	}

	
	/**
	 * @brief modifies a Neuron and Interna parameter
	 * @param paramTrees
	 * @param projectDirPath
	 * @param param
	 * @param param2
	 * @param identifier
	 * @param identifier2 
	 */
	public void modifyAllParameter(Map<String, XMLObject> paramTrees, String projectDirPath, double param, double param2, String identifier, String identifier2) {
		modifyParameter(paramTrees, projectDirPath, param, identifier, NeuGenConstants.PARAM);
		modifyParameter(paramTrees, projectDirPath, param2, identifier2, NeuGenConstants.INTERNA);
	}
	
	/**
	 * @brief modifies a Neuron or Interna parameter
	 * @param paramTrees
	 * @param projectDirPath
	 * @param param
	 * @param identifier
	 * @param parameter_domain 
	 */
	private void modifyParameter(Map<String, XMLObject> paramTrees, String projectDirPath, double param, String identifier, String parameter_domain) {
		for (Map.Entry<String, XMLObject> entry : paramTrees.entrySet()) {
			XMLObject obj = entry.getValue();
			if (entry.getKey().equals(NeuGenConstants.PARAM)) {
				modifyParameter(obj, projectDirPath, param, identifier);
			} else {
				logger.warn("You did not supply a NeuGen ***" + parameter_domain + "*** tree!");
			}
		}
	}
	
	/**
	 * @brief modify's an Interna parameter
	 * @param paramTrees
	 * @param projectDirPath
	 * @param param
	 * @param identifier 
	 */
	public void modifyInternaParameter(Map<String, XMLObject> paramTrees, String projectDirPath, double param, String identifier) {
		modifyParameter(paramTrees, projectDirPath, param, identifier, NeuGenConstants.INTERNA);
	}
	
	/**
	 * @brief modify's a Neuron parameter
	 * @param paramTrees
	 * @param projectDirPath
	 * @param param
	 * @param identifier 
	 */
	public void modifyNeuronParameter(Map<String, XMLObject> paramTrees, String projectDirPath, double param, String identifier) {
		modifyParameter(paramTrees, projectDirPath, param, identifier, NeuGenConstants.PARAM);
	}
	
	/**
	 * @brief modifies some NEURON parameter recursively
	 * @param paramTree
	 * @param projectDirPath
	 * @param param
	 * @param identifier
	 */
	@SuppressWarnings("unchecked")
	private void modifyParameter(XMLObject paramTree, String projectDirPath, double param, String identifier) {
		if (!identifier.equals("/")) {
			logger.warn("Apparently you did not supply a path to a parameter: " + identifier);
			return;
		}

		ArrayList<String> pathes = new ArrayList<String>(Arrays.asList(identifier.split("/")));
		Enumeration<XMLNode> childs = paramTree.children();		
		
		while (childs.hasMoreElements()) {
			XMLNode node = childs.nextElement();
			if (pathes.get(0).equals(node.getKey())) {
				modifyParameter_rec(node,
					param,
					(ArrayList<String>) pathes.subList(1, pathes.size()-1));
			} else {
				logger.warn("Invalid path to parameter specified: " + identifier);
			}
		}
	}

	/**
	 * @brief modifies some NEURON parameter recursively
	 * @param root
	 * @param param
	 * @param identifier
	 */
	@SuppressWarnings("unchecked")
	private void modifyParameter_rec(XMLNode root, double param, ArrayList<String> identifier) {
		Enumeration<XMLNode> childs = root.children();
		while (childs.hasMoreElements()) {
			XMLNode node = childs.nextElement();
			if (identifier.get(0).equals(node.getKey())) {
				if (identifier.size() == 1) {
					node.setValue(param);
				} else {
					modifyParameter_rec(node,
						param,
						(ArrayList<String>) identifier.subList(1, identifier.size()-1));
				}
			} else {
				logger.warn("Invalid path to parameter specified: " + identifier);
			}
		}
	}

	/**
	 * @brief replaces content of a given node specified by identifier with
	 * replacement
	 * @author stephanmg <stephan@syntaktischer-zucker.de>
	 *
	 * @param child
	 * @param identifier
	 * @param replacement
	 */
	@SuppressWarnings("unchecked")
	private void correct_siblings(XMLNode child, String identifier, double replacement) {
		/// only one child called siblings within childs of current XMLNode child
		Enumeration<XMLNode> childs = child.children();
		XMLNode sibling = childs.nextElement();
		Enumeration<XMLNode> childs_of_sibling = sibling.children();

		while (childs_of_sibling.hasMoreElements()) {
			/// current child of current sibling's child
			XMLNode current_child = childs_of_sibling.nextElement();

			/// replace node's content
			if (identifier.equals(current_child.getKey())) {
				current_child.setValue(replacement);
			}

			/// more siblings?
			if ("siblings".equals(current_child.getKey())) {
				correct_siblings(current_child, identifier, replacement);
			}
		}

	}

	/**
	 * @brief test
	 *
	 * @param args
	 */
	@SuppressWarnings("CallToPrintStackTrace")
	public static void main(String... args) {
		/**
		 * @brief todo works - test remaining functionality...
		 */
		try {
			NGBackend back = new NGBackend();
			Map<String, XMLObject> params = back.create_and_open_project("foo24", NeuGenConstants.NEOCORTEX_PROJECT, true);
			back.modifyNPartsDensity(params, "foo24/Neocortex", 0.1);
			back.generate_network(NeuGenConstants.NEOCORTEX_PROJECT);
			back.export_network("NGX", "foo24.ngx");
			back.save_and_close_project(params, "foo24/");
		} catch (Exception e) {
			logger.fatal("Make sure you selected a valid project directory: " + e);
			e.printStackTrace();
		}
	}
}