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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.neugen.datastructures.Net;
import org.neugen.datastructures.Region;
import org.neugen.datastructures.xml.XMLNode;
import org.neugen.datastructures.xml.XMLObject;
import org.neugen.gui.NeuGenConstants;
import org.neugen.gui.NeuGenLib;
import org.neugen.gui.NeuGenProject;
import org.neugen.parsers.DefaultInheritance;
import org.neugen.parsers.NGX.NGXWriter;
import org.neugen.parsers.NeuGenConfigStreamer;
import org.neugen.parsers.TXT.TXTWriter;
import org.neugen.utils.NeuGenLogger;
import org.neugen.utils.Utils;

/**
 * @brief NeuGen's backend
 * Note: This pseudo-backend attempts to disentangle the interweaved
 * GUI and backend components of NeuGen. Therefore this pseudo-backend
 * may be considered as a hack. Some functionality is available, cf. 
 * below, however this backend needs to be enhanced for desired functionality.
 * @author stephanmg <stephan@syntaktischer-zucker.de>
 * 
 * @todo NGBackend should probably be refactored to a singleton
 * @todo some functionality should be refactored to a facade (
 * @todo params map should be avoided - use this as a member
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
	 * @brief executes the project
	 * @param projectType
	 */
	public void execute(String projectType) {
		ngLib.run(projectType);
	}

	/**
	 * @brief loads the initial parameters from project directory
	 * @param file
	 * @param root
	 * @return parameter list
	 */
	@SuppressWarnings({"CallToPrintStackTrace", "CallToThreadDumpStack"})
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
	 * @brief get the project property
	 * @param projectPath
	 * @param projectType
	 * @return list of properties
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
	 * @brief creates a NeuGen project
	 * @param projectPath
	 * @param sourceTemplate  
	 * @param projectType
	 * @param force
	 * @param open_only - if specified opens only the project
	 * @return parameters as a map
	 */
	@SuppressWarnings("NestedAssignment")
	public Map<String, XMLObject> create_and_open_project(String projectPath, String sourceTemplate, String projectType, boolean force, boolean open_only) {
		if (!open_only) {
			logger.info("project path (project type: " + projectType + "): " + projectPath);
			File projectDir = new File(projectPath);
			System.err.println("projectPath: " + projectPath);
			if (!NGBackendUtil.fileExists(projectDir, force)) {
				URL inputUrl = NGBackend.class.getResource("/org/neugen/gui/resources/" + projectType.toLowerCase() + ".zip");
				File dest = new File(sourceTemplate + System.getProperty("file.separator") + projectPath + System.getProperty("file.separator") + projectType.toLowerCase() + ".zip");
				try {
					FileUtils.copyURLToFile(inputUrl, dest);
					System.err.println("Copying file from: " + inputUrl + " to " + dest);
				} catch (IOException ex) {
					java.util.logging.Logger.getLogger(NeuGenProject.class.getName()).log(Level.SEVERE, null, ex);
				} 

				ZipInputStream zis = null;
				try {

					zis = new ZipInputStream(new FileInputStream(projectDir + System.getProperty("file.separator") + projectType.toLowerCase() + ".zip"));
					ZipEntry entry;

					while ((entry = zis.getNextEntry()) != null) {

						File entryFile = new File(projectDir + System.getProperty("file.separator") + entry.getName());
						if (entry.isDirectory()) {

							if (entryFile.exists()) {
							} else {
								entryFile.mkdirs();
							}

						} else {
							if (entryFile.getParentFile() != null && !entryFile.getParentFile().exists()) {
								entryFile.getParentFile().mkdirs();
							}

							if (!entryFile.exists()) {
								entryFile.createNewFile();
							}

							OutputStream os = null;
							try {
								os = new FileOutputStream(entryFile);
								IOUtils.copy(zis, os);
							} finally {
								IOUtils.closeQuietly(os);
							}
						}
					}
				} catch (FileNotFoundException ex) {
					java.util.logging.Logger.getLogger(NeuGenProject.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IOException ex) {
					java.util.logging.Logger.getLogger(NeuGenProject.class.getName()).log(Level.SEVERE, null, ex);
				} finally {
					IOUtils.closeQuietly(zis);
				}
			}
		}
			
			File projectDir = new File(projectPath);
			if (!NGBackendUtil.fileExists(projectDir, force)) {
				if (projectType.equals(NeuGenConstants.HIPPOCAMPUS_PROJECT)) {
					String sourcePath = sourceTemplate + System.getProperty("file.separator") + NeuGenConstants.CONFIG_DIR + System.getProperty("file.separator") + NeuGenConstants.CONFIG_DIR + System.getProperty("file.separator") + NeuGenConstants.HIPPOCAMPUS_PROJECT.toLowerCase();
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
					String sourcePath = sourceTemplate + System.getProperty("file.separator") + NeuGenConstants.CONFIG_DIR + System.getProperty("file.separator") + NeuGenConstants.NEOCORTEX_PROJECT.toLowerCase();
					logger.info("source path: " + sourcePath);
					logger.info("project path: " + projectDir.getPath());
					System.err.println("source path: " + sourcePath);
					System.err.println("project path: " + projectPath);
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
					Region.setCortColumn(false);
					Region.setCa1Region(false);
					logger.fatal("Wrong project type specified aborting: "
						+ projectType + ". Supported project types are "
						+ NeuGenConstants.NEOCORTEX_PROJECT + " and "
						+ NeuGenConstants.HIPPOCAMPUS_PROJECT + ".");
				}
			}
			
	/*
		} 
		if (projectType.equals(NeuGenConstants.HIPPOCAMPUS_PROJECT)) {
			Region.setCortColumn(true);
			Region.setCa1Region(false);
		} else {
			Region.setCortColumn(false);
			Region.setCa1Region(false);
		}
	*/
		
		
		/// set the project type (if an existing project is used)
		if (projectType.equals(NeuGenConstants.NEOCORTEX_PROJECT)) {
			Region.setCortColumn(true);
			Region.setCa1Region(false);
		} else if (projectType.equals(NeuGenConstants.HIPPOCAMPUS_PROJECT)) {
			Region.setCortColumn(false);
			Region.setCa1Region(true);
		} else {
			Region.setCortColumn(false);
			Region.setCa1Region(false);
			logger.fatal("Wrong project type specified aborting: "
				+ projectType + ". Supported project types are "
				+ NeuGenConstants.NEOCORTEX_PROJECT + " and "
				+ NeuGenConstants.HIPPOCAMPUS_PROJECT + ".");
		}

		return initProjectParam(projectPath, projectType);
	}

	/**
	 * @brief saves the PARAM parameters (NeuGen parameters)
	 * @param currentRoot
	 * @param projectDirPath
	 */
	private void save_param(XMLNode currentRoot, String projectDirPath) {
		save(currentRoot, projectDirPath, NeuGenConstants.PARAM);
	}

	/**
	 * @brief saves the INTERNA parameters (NeuGen parameters)
	 * @param currentRoot
	 * @param projectDirPath
	 */
	private void save_interna(XMLNode currentRoot, String projectDirPath) {
		save(currentRoot, projectDirPath, NeuGenConstants.INTERNA);
	}

	/**
	 * @brief save all params
	 * @param paramTrees
	 * @param projectDirPath
	 */
	public void save(Map<String, XMLObject> paramTrees, String projectDirPath) {
		save_param(paramTrees.get(NeuGenConstants.PARAM), projectDirPath);
		save_interna(paramTrees.get(NeuGenConstants.INTERNA), projectDirPath);
	}

	/**
	 * @brief saves INTERNA or PARAM parameters (NeuGen parameters)
	 * @param currentRoot
	 * @param projectDirPath
	 * @param param
	 */
	private void save(XMLNode currentRoot, String projectDirPath, String param) {
		logger.info("leaf count: " + currentRoot.getLeafCount());
		
		/**
		 * @todo brief: tied to GUI! (implement the pseudocode below - more reasonable)
		 * workaround: 1) alter parameters in project file before generating the net
		 *             2) generate net 
		 * 	       3) save the file in a gui-independent way (i. e. 
		 * 		dont use the XML tree representations in the GUI 
		 * 		since those values are not availabel for us)
		 */
		///XMLObject rootCopy = XMLObject.getCopyXMLObject(XMLObject.convert(currentRoot));
		XMLObject rootCopy = (XMLObject) currentRoot; /// This should fix already the issue (concerning cannot save because tied to GUI)
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
	 * @brief inits param table
	 * @param paramTrees
	 * @param paramPath
	 * @param internaPath
	 * @return map of parameters
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
	 * @brief inits all parameters
	 * @param dirPath
	 * @param projectType
	 * @return map of all parameters
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
	 * @brief generates the NeuGen net 
	 * @param projectType
	 */
	public void generate_network(String projectType) {
		ngLib.run(projectType);
		/// not necessary:
		/// ngLib.getNet().destroy();
		/// ngLib.destroy();
	}
	
	/**
	 * @brief saves and closes the NeuGen project
	 * @param paramTrees
	 * @param projectDirPath
	 */
	public void save_and_close_project(Map<String, XMLObject> paramTrees, String projectDirPath) {
		/// save
		save(paramTrees, projectDirPath);
		/// clear param data and destroy all net components
		NeuGenLib.clearOldParamData();
		ngLib.destroy();
		ngLib.getNet().destroy();
	}

	/**
	 * @brief exports the NeuGen network
	 * Note: Enhance this function by needs, i. e. if you
	 * need another exporter, like HOC or something else, you
	 * may add it here
	 * @param type
	 * @param file
	 * @param withCellType
	 */
	public void export_network(String type, String file, boolean withCellType) {
		Net net = ngLib.getNet();
		if ("NGX".equalsIgnoreCase(type)) {
			logger.info("Exporting NGX data to... " + file);
			NGXWriter ngxWriter = new NGXWriter(net, new File(file));
			ngxWriter.exportNetToNGX();
		} else if ("TXT".equalsIgnoreCase(type)) {
			logger.info("Exporting TXT data to... " + file);
        		TXTWriter txtWriter = new TXTWriter(net, new File(file));
			txtWriter.setCompressed(false);
			txtWriter.setUncompressed(true);
			txtWriter.setWithCellType(withCellType);
			txtWriter.exportNetToTXT();
		} else {
			logger.info("Unsupported exporter chosen.");
		}
	}
	
	/**
	 * @brief modify the project params and set them
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
	 * @brief modifies n parts density (NeuGen property)
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
	 * @brief correct synapse dist to custom value (NeuGen property)
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
	 * @brief adjust network size 
	 * Network will contain *n* cells consisting of a predefined ratio
	 * of different cell types characteristic for the given network type
	 * @param paramTrees
	 * @param n 
	 */
	public void adjustNetworkSize(Map<String, XMLObject> paramTrees, int n) {
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
						if (! "dist_synapse".equals(node2.getKey())) {
							node2.setValue(Integer.parseInt(node2.getValue().toString()) * n);
						}
					}

				}
			}
		}
	} 

	
	/**
	 * @brief modifies a Neuron and INTERNA parameter
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
	 * @brief modifies a Neuron or INTERNA parameter
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
	 * @brief modifies an INTERNA parameter (NeuGen parameter)
	 * @param paramTrees
	 * @param projectDirPath
	 * @param param
	 * @param identifier 
	 */
	public void modifyInternaParameter(Map<String, XMLObject> paramTrees, String projectDirPath, double param, String identifier) {
		modifyParameter(paramTrees, projectDirPath, param, identifier, NeuGenConstants.INTERNA);
	}
	
	/**
	 * @brief modifies a Neuron parameter (NeuGen parameter)
	 * @param paramTrees
	 * @param projectDirPath
	 * @param param
	 * @param identifier 
	 */
	public void modifyNeuronParameter(Map<String, XMLObject> paramTrees, String projectDirPath, double param, String identifier) {
		modifyParameter(paramTrees, projectDirPath, param, identifier, NeuGenConstants.PARAM);
	}
	
	/**
	 * @brief modifies some Neuron parameter recursively (NeuGen parameter)
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
	 * @brief modifies some Neuron parameter recursively (NeuGen parameter)
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
	 * @brief replaces content of a given node.
	 * The replacement is specified by identifier.
	 * @author stephanmg <stephan@syntaktischer-zucker.de>
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
	 * @brief returns the project type
	 * @return project type
	 */
	public String getProjectType() {
		if (Region.isCa1Region()) {
			return NeuGenConstants.HIPPOCAMPUS_PROJECT;
		} else if (Region.isCortColumn()) {
			return NeuGenConstants.NEOCORTEX_PROJECT;
		} else {
			return "";
		}
	}

	/**
	 * @brief main method
	 * @param args
	 */
	@SuppressWarnings({"CallToPrintStackTrace", "CallToThreadDumpStack"})
	public static void main(String... args) {
		try {
			NGBackend back = new NGBackend();
			Map<String, XMLObject> params = back.create_and_open_project("foo27", "/Users/stephan/Code/git/NeuGen_source/NeuGen", NeuGenConstants.NEOCORTEX_PROJECT, true, false);
			back.modifyNPartsDensity(params, "foo27/Neocortex", 1000);
			////back.adjustNetworkSize(params, 1000);
			////back.generate_network(NeuGenConstants.NEOCORTEX_PROJECT);
			back.export_network("NGX", "foo27.ngx", false);
			back.save_and_close_project(params, "foo27");
                        
		} catch (Exception e) {
			logger.fatal("Make sure you selected a valid project directory: " + e);
			e.printStackTrace();
		}
	}
}
