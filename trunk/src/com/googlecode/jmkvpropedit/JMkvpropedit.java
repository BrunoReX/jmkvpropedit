/* 
 * Copyright (c) 2012 Bruno Barbieri
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package com.googlecode.jmkvpropedit;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import org.ini4j.*;

public class JMkvpropedit {
	private JFrame frmJMkvpropedit;

	private int maxStreams = 30; // Track number limit for batch mode
	Runtime rt = Runtime.getRuntime();
	private Process proc = null;
	private SwingWorker<Void, Void> worker = null;
	
	private JPanel[] subPnlVideo = new JPanel[maxStreams];
	private JCheckBox[] chbEditVideo = new JCheckBox[maxStreams];
	private JCheckBox[] chbDefaultVideo = new JCheckBox[maxStreams];
	private JRadioButton[] rbYesDefVideo = new JRadioButton[maxStreams];
	private JRadioButton[] rbNoDefVideo = new JRadioButton[maxStreams];
	private ButtonGroup[] bgRbDefVideo = new ButtonGroup[maxStreams];
	private JCheckBox[] chbForcedVideo = new JCheckBox[maxStreams];
	private JRadioButton[] rbYesForcedVideo = new JRadioButton[maxStreams];
	private JRadioButton[] rbNoForcedVideo = new JRadioButton[maxStreams];
	private ButtonGroup[] bgRbForcedVideo = new ButtonGroup[maxStreams];
	private JCheckBox[] chbNameVideo = new JCheckBox[maxStreams];
	private JTextField[] txtNameVideo = new JTextField[maxStreams];
	private JCheckBox[] cbNumbVideo = new JCheckBox[maxStreams];
	private JTextField[] txtNumbStartVideo = new JTextField[maxStreams];
	private JLabel[] lblNumbStartVideo = new JLabel[maxStreams];
	private JLabel[] lblNumbExplainVideo = new JLabel[maxStreams];
	private JLabel[] lblNumbPadVideo = new JLabel[maxStreams];
	private JTextField[] txtNumbPadVideo = new JTextField[maxStreams];
	private JCheckBox[] chbLangVideo = new JCheckBox[maxStreams];
	private JComboBox[] cbLangVideo = new JComboBox[maxStreams];
	private JCheckBox[] chbExtraCmdVideo = new JCheckBox[maxStreams];
	private JTextField[] txtExtraCmdVideo = new JTextField[maxStreams];	

	private JPanel[] subPnlAudio = new JPanel[maxStreams];
	private JCheckBox[] chbEditAudio = new JCheckBox[maxStreams];
	private JCheckBox[] chbDefaultAudio = new JCheckBox[maxStreams];
	private JRadioButton[] rbYesDefAudio = new JRadioButton[maxStreams];
	private JRadioButton[] rbNoDefAudio = new JRadioButton[maxStreams];
	private ButtonGroup[] bgRbDefAudio = new ButtonGroup[maxStreams];
	private JCheckBox[] chbForcedAudio = new JCheckBox[maxStreams];
	private JRadioButton[] rbYesForcedAudio = new JRadioButton[maxStreams];
	private JRadioButton[] rbNoForcedAudio = new JRadioButton[maxStreams];
	private ButtonGroup[] bgRbForcedAudio = new ButtonGroup[maxStreams];
	private JCheckBox[] chbNameAudio = new JCheckBox[maxStreams];
	private JTextField[] txtNameAudio = new JTextField[maxStreams];
	private JCheckBox[] cbNumbAudio = new JCheckBox[maxStreams];
	private JTextField[] txtNumbStartAudio = new JTextField[maxStreams];
	private JLabel[] lblNumbStartAudio = new JLabel[maxStreams];
	private JLabel[] lblNumbExplainAudio = new JLabel[maxStreams];
	private JLabel[] lblNumbPadAudio = new JLabel[maxStreams];
	private JTextField[] txtNumbPadAudio = new JTextField[maxStreams];
	private JCheckBox[] chbLangAudio = new JCheckBox[maxStreams];
	private JComboBox[] cbLangAudio = new JComboBox[maxStreams];
	private JCheckBox[] chbExtraCmdAudio = new JCheckBox[maxStreams];
	private JTextField[] txtExtraCmdAudio = new JTextField[maxStreams];

	private JPanel[] subPnlSubtitle = new JPanel[maxStreams];
	private JCheckBox[] chbEditSubtitle = new JCheckBox[maxStreams];
	private JCheckBox[] chbDefaultSubtitle = new JCheckBox[maxStreams];
	private JRadioButton[] rbYesDefSubtitle = new JRadioButton[maxStreams];
	private JRadioButton[] rbNoDefSubtitle = new JRadioButton[maxStreams];
	private ButtonGroup[] bgRbDefSubtitle = new ButtonGroup[maxStreams];
	private JCheckBox[] chbForcedSubtitle = new JCheckBox[maxStreams];
	private JRadioButton[] rbYesForcedSubtitle = new JRadioButton[maxStreams];
	private JRadioButton[] rbNoForcedSubtitle = new JRadioButton[maxStreams];
	private ButtonGroup[] bgRbForcedSubtitle = new ButtonGroup[maxStreams];
	private JCheckBox[] chbNameSubtitle = new JCheckBox[maxStreams];
	private JTextField[] txtNameSubtitle = new JTextField[maxStreams];
	private JCheckBox[] cbNumbSubtitle = new JCheckBox[maxStreams];
	private JTextField[] txtNumbStartSubtitle = new JTextField[maxStreams];
	private JLabel[] lblNumbStartSubtitle = new JLabel[maxStreams];
	private JLabel[] lblNumbExplainSubtitle = new JLabel[maxStreams];
	private JLabel[] lblNumbPadSubtitle = new JLabel[maxStreams];
	private JTextField[] txtNumbPadSubtitle = new JTextField[maxStreams];
	private JCheckBox[] chbLangSubtitle = new JCheckBox[maxStreams];
	private JComboBox[] cbLangSubtitle = new JComboBox[maxStreams];
	private JCheckBox[] chbExtraCmdSubtitle = new JCheckBox[maxStreams];
	private JTextField[] txtExtraCmdSubtitle = new JTextField[maxStreams];
	
	private JCheckBox chbTitleGeneral;
	private JTextField txtTitleGeneral;
	private JCheckBox cbNumbGeneral;
	private JTextField txtNumbStartGeneral;
	private JTextField txtNumbPadGeneral;
	private JCheckBox chbRemoveChapters;
	private JCheckBox chbRemoveTags;
	private JCheckBox chbExtraCmdGeneral;
	private JTextField txtExtraCmdGeneral;
	private JTextField txtMkvPropExe;
	private JCheckBox cbMkvPropExeDef;
	
	private JTabbedPane tabbedPane;
	private DefaultListModel modelFiles;
	private JList listFiles;
	private JComboBox cbVideo;
	private JComboBox cbAudio;
	private JComboBox cbSubtitle;
	private JLayeredPane lyrdPnlVideo;
	private JLayeredPane lyrdPnlAudio;
	private JLayeredPane lyrdPnlSubtitle;
	private JButton btnProcessFiles;
	private JButton btnGenerateCmdLine;
	private JTextArea txtOutput;
	private int nVideo = 0;
	private int nAudio = 0;
	private int nSubtitle = 0;
	
	MkvLanguage mkvLang = new MkvLanguage();
	URL imgRes = null;
	JFileChooser chooser = null;
	private static ArrayList<String> argsArray = new ArrayList<String>();
	private File iniFile = new File("JMkvpropedit.ini");
	
	private String[] cmdLineGeneral = null;
	private String[] cmdLineGeneralOpt = null;
	private String[] cmdLineVideo = null;
	private String[] cmdLineVideoOpt = null;
	private String[] cmdLineAudio = null;
	private String[] cmdLineAudioOpt = null;
	private String[] cmdLineSubtitle = null;
	private String[] cmdLineSubtitleOpt = null;
	private ArrayList<String> cmdLineBatch = null;
	private ArrayList<String> cmdLineBatchOpt = null;
	
	
	/**
	 * Launch the application.
	 */
	public static final void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					for (String arg : args) {
						argsArray.add(arg);
					}
					
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Use native theme for GUI
					JMkvpropedit window = new JMkvpropedit();
					window.frmJMkvpropedit.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JMkvpropedit() {
		initialize();
		parseFiles(argsArray);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJMkvpropedit = new JFrame();
		frmJMkvpropedit.setTitle("JMkvpropedit 1.0.4"); /* Version */
		frmJMkvpropedit.setResizable(false);
		frmJMkvpropedit.setBounds(100, 100, 759, 444);
		frmJMkvpropedit.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frmJMkvpropedit.getContentPane().setLayout(null);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 11, 733, 360);
		frmJMkvpropedit.getContentPane().add(tabbedPane);
		
		JPanel pnlInput = new JPanel();
		tabbedPane.addTab("Input", null, pnlInput, null);
		pnlInput.setLayout(null);
		
		JScrollPane scrollFiles = new JScrollPane();
		scrollFiles.setBounds(10, 11, 676, 310);
		pnlInput.add(scrollFiles);
		
		modelFiles = new DefaultListModel();
		listFiles = new JList(modelFiles);
		scrollFiles.setViewportView(listFiles);
		
		imgRes = ClassLoader.getSystemResource("res/list-add.png");
		JButton btnAddFiles = new JButton("");
		btnAddFiles.setIcon(new ImageIcon(imgRes));
		btnAddFiles.setBounds(696, 21, 22, 23);
		pnlInput.add(btnAddFiles);
		
		imgRes = ClassLoader.getSystemResource("res/list-remove.png");
		JButton btnRemoveFiles = new JButton("");
		btnRemoveFiles.setIcon(new ImageIcon(imgRes));
		btnRemoveFiles.setBounds(696, 65, 22, 23);
		pnlInput.add(btnRemoveFiles);
		
		imgRes = ClassLoader.getSystemResource("res/edit-clear.png");
		JButton btnClearFiles = new JButton("");
		btnClearFiles.setIcon(new ImageIcon(imgRes));
		btnClearFiles.setBounds(696, 109, 22, 23);
		pnlInput.add(btnClearFiles);
		
		imgRes = ClassLoader.getSystemResource("res/go-top.png");
		JButton btnTopFiles = new JButton("");
		btnTopFiles.setIcon(new ImageIcon(imgRes));
		btnTopFiles.setBounds(696, 153, 22, 23);
		pnlInput.add(btnTopFiles);
		
		imgRes = ClassLoader.getSystemResource("res/go-up.png");
		JButton btnUpFiles = new JButton("");
		btnUpFiles.setIcon(new ImageIcon(imgRes));
		btnUpFiles.setBounds(696, 197, 22, 23);
		pnlInput.add(btnUpFiles);
		
		imgRes = ClassLoader.getSystemResource("res/go-down.png");
		JButton btnDownFiles = new JButton("");
		btnDownFiles.setIcon(new ImageIcon(imgRes));
		btnDownFiles.setBounds(696, 241, 22, 23);
		pnlInput.add(btnDownFiles);
		
		imgRes = ClassLoader.getSystemResource("res/go-bottom.png");
		JButton btnBottomFiles = new JButton("");
		btnBottomFiles.setIcon(new ImageIcon(imgRes));
		btnBottomFiles.setBounds(696, 285, 22, 23);
		pnlInput.add(btnBottomFiles);
		
		chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileHidingEnabled(true);
		chooser.setAcceptAllFileFilterUsed(false);
		
		JPanel pnlGeneral = new JPanel();
		tabbedPane.addTab("General", null, pnlGeneral, null);
		pnlGeneral.setLayout(null);
		
		chbTitleGeneral = new JCheckBox("Title");
		chbTitleGeneral.setBounds(6, 7, 45, 23);
		if (!isWindows())
			chbTitleGeneral.setBounds(6, 7, 66, 23);
		pnlGeneral.add(chbTitleGeneral);
		
		txtTitleGeneral = new JTextField();
		txtTitleGeneral.setEnabled(false);
		txtTitleGeneral.setBounds(57, 8, 661, 20);
		if (!isWindows())
			txtTitleGeneral.setBounds(78, 8, 640, 20);
		pnlGeneral.add(txtTitleGeneral);
		txtTitleGeneral.setColumns(10);
		
		cbNumbGeneral = new JCheckBox("Numbering");
		cbNumbGeneral.setEnabled(false);
		cbNumbGeneral.setBounds(16, 37, 104, 23);
		pnlGeneral.add(cbNumbGeneral);
		
		final JLabel lblNumbStartGeneral = new JLabel("Start");
		lblNumbStartGeneral.setEnabled(false);
		lblNumbStartGeneral.setBounds(191, 40, 31, 14);
		if (!isWindows())
			lblNumbStartGeneral.setBounds(191, 40, 45, 14);
		pnlGeneral.add(lblNumbStartGeneral);
		
		txtNumbStartGeneral = new JTextField();
		txtNumbStartGeneral.setEnabled(false);
		txtNumbStartGeneral.setText("1");
		txtNumbStartGeneral.setBounds(220, 38, 70, 20);
		if (!isWindows())
			txtNumbStartGeneral.setBounds(232, 38, 70, 20);
		pnlGeneral.add(txtNumbStartGeneral);
		txtNumbStartGeneral.setColumns(10);
				
		final JLabel lblNumbPadGeneral = new JLabel("Padding");
		lblNumbPadGeneral.setEnabled(false);
		lblNumbPadGeneral.setBounds(322, 40, 45, 14);
		if (!isWindows())
			lblNumbPadGeneral.setBounds(337, 40, 64, 14);
		pnlGeneral.add(lblNumbPadGeneral);
		
		txtNumbPadGeneral = new JTextField();
		txtNumbPadGeneral.setEnabled(false);
		txtNumbPadGeneral.setText("1");
		txtNumbPadGeneral.setBounds(366, 38, 70, 20);
		if (!isWindows())
			txtNumbPadGeneral.setBounds(402, 38, 70, 20);
		pnlGeneral.add(txtNumbPadGeneral);
		txtNumbPadGeneral.setColumns(10);
		
		final JLabel lblNumbExplainGeneral = new JLabel("To use it, add {num} to tittle (e.g. \"My Title {num}\")");
		lblNumbExplainGeneral.setEnabled(false);
		lblNumbExplainGeneral.setBounds(33, 67, 473, 14);
		pnlGeneral.add(lblNumbExplainGeneral);
		
		chbRemoveChapters = new JCheckBox("Remove chapters");
		chbRemoveChapters.setBounds(6, 88, 150, 23);
		pnlGeneral.add(chbRemoveChapters);
		
		chbRemoveTags = new JCheckBox("Remove tags");
		chbRemoveTags.setBounds(6, 114, 150, 23);
		pnlGeneral.add(chbRemoveTags);
		
		chbExtraCmdGeneral = new JCheckBox("Extra parameters");
		chbExtraCmdGeneral.setBounds(6, 140, 114, 23);
		if (!isWindows())
			chbExtraCmdGeneral.setBounds(6, 140, 150, 23);
		pnlGeneral.add(chbExtraCmdGeneral);
		
		txtExtraCmdGeneral = new JTextField();
		txtExtraCmdGeneral.setEnabled(false);
		txtExtraCmdGeneral.setBounds(126, 141, 592, 20);
		if (!isWindows())
			txtExtraCmdGeneral.setBounds(166, 141, 552, 20);
		pnlGeneral.add(txtExtraCmdGeneral);
		txtExtraCmdGeneral.setColumns(10);
		
		txtMkvPropExe = new JTextField();
		txtMkvPropExe.setText("mkvpropedit.exe");
		if (!isWindows())
			txtMkvPropExe.setText("/usr/bin/mkvpropedit");
		txtMkvPropExe.setEditable(false);
		txtMkvPropExe.setBounds(10, 272, 708, 20);
		pnlGeneral.add(txtMkvPropExe);
		txtMkvPropExe.setColumns(10);
		
		JLabel lblMkvPropExe = new JLabel("Mkvpropedit executable");
		lblMkvPropExe.setBounds(10, 255, 209, 14);
		pnlGeneral.add(lblMkvPropExe);
		
		cbMkvPropExeDef = new JCheckBox("Use default");
		cbMkvPropExeDef.setEnabled(false);
		cbMkvPropExeDef.setSelected(true);
		cbMkvPropExeDef.setBounds(6, 292, 120, 23);
		pnlGeneral.add(cbMkvPropExeDef);
		
		JButton btnBrowseMkvPropExe = new JButton("Browse...");
		btnBrowseMkvPropExe.setBounds(618, 295, 100, 23);
		if (!isWindows())
			btnBrowseMkvPropExe.setBounds(583, 295, 135, 23);
		pnlGeneral.add(btnBrowseMkvPropExe);
		
		final JPanel pnlVideo = new JPanel();
		tabbedPane.addTab("Video", null, pnlVideo, null);
		pnlVideo.setLayout(null);
		
		cbVideo = new JComboBox();
		cbVideo.setBounds(10, 10, 146, 20);
		pnlVideo.add(cbVideo);
		
		imgRes = ClassLoader.getSystemResource("res/list-add.png");
		final JButton btnAddVideo = new JButton("");
		btnAddVideo.setIcon(new ImageIcon(imgRes));
		btnAddVideo.setBounds(166, 10, 22, 20);
		pnlVideo.add(btnAddVideo);
		
		lyrdPnlVideo = new JLayeredPane();
		lyrdPnlVideo.setBounds(0, 38, 728, 294);
		pnlVideo.add(lyrdPnlVideo);
		
		final JPanel pnlAudio = new JPanel();
		tabbedPane.addTab("Audio", null, pnlAudio, null);
		pnlAudio.setLayout(null);
		
		cbAudio = new JComboBox();
		cbAudio.setBounds(10, 10, 146, 20);
		pnlAudio.add(cbAudio);
		
		final JButton btnAddAudio = new JButton("");
		btnAddAudio.setIcon(new ImageIcon(imgRes));
		btnAddAudio.setBounds(166, 10, 22, 20);
		pnlAudio.add(btnAddAudio);
		
		lyrdPnlAudio = new JLayeredPane();
		lyrdPnlAudio.setBounds(0, 38, 728, 294);
		pnlAudio.add(lyrdPnlAudio);
		
		final JPanel pnlSubtitle = new JPanel();
		tabbedPane.addTab("Subtitle", null, pnlSubtitle, null);
		pnlSubtitle.setLayout(null);
		
		cbSubtitle = new JComboBox();
		cbSubtitle.setBounds(10, 10, 146, 20);
		pnlSubtitle.add(cbSubtitle);
		
		final JButton btnAddSubtitle = new JButton("");
		btnAddSubtitle.setIcon(new ImageIcon(imgRes));
		btnAddSubtitle.setBounds(166, 10, 22, 20);
		pnlSubtitle.add(btnAddSubtitle);
		
		lyrdPnlSubtitle = new JLayeredPane();
		lyrdPnlSubtitle.setBounds(0, 38, 728, 294);
		pnlSubtitle.add(lyrdPnlSubtitle);
				
		JPanel pnlOutput = new JPanel();
		tabbedPane.addTab("Output", null, pnlOutput, null);
		pnlOutput.setLayout(new BorderLayout(0, 0));
		
		txtOutput = new JTextArea();
		txtOutput.setLineWrap(true);
		txtOutput.setEditable(false);
		JScrollPane scrollOutput = new JScrollPane(txtOutput);
		pnlOutput.add(scrollOutput);
		
		btnProcessFiles = new JButton("Process files");
		btnProcessFiles.setBounds(151, 382, 150, 23);
		if (!isWindows())
			btnProcessFiles.setBounds(94, 382, 235, 23);
		frmJMkvpropedit.getContentPane().add(btnProcessFiles);
		
		btnGenerateCmdLine = new JButton("Generate command line");
		btnGenerateCmdLine.setBounds(452, 382, 150, 23);
		if (!isWindows())
			btnGenerateCmdLine.setBounds(423, 382, 235, 23);
		frmJMkvpropedit.getContentPane().add(btnGenerateCmdLine);
		
		frmJMkvpropedit.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				readIniFile();
				addVideoTrack();
				addAudioTrack();
				addSubtitleTrack();
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				boolean wRunning;
				
				try {
					wRunning = !worker.isDone();
				} catch (Exception e1) {
					wRunning = false;
				}
				
				if (wRunning) {
					int choice = JOptionPane.showConfirmDialog(frmJMkvpropedit,
							"Do you really wanna exit?",
							"", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						worker.cancel(true);
						frmJMkvpropedit.dispose();
						System.exit(0);
					}
				} else {
					frmJMkvpropedit.dispose();
					System.exit(0);
				}
			}
		});
		
		
		/* Button "Process files" */
		
		btnProcessFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File mkvPropExe = new File(txtMkvPropExe.getText());
				
				if (modelFiles.getSize() == 0) {
					JOptionPane.showMessageDialog(frmJMkvpropedit,
							"The file list is empty!",
							"Empty list",
							JOptionPane.ERROR_MESSAGE);
				} else if (!mkvPropExe.exists()) {
					JOptionPane.showMessageDialog(frmJMkvpropedit,
							"Mkvpropedit executable not found!" +
							"\nPlease set the right path for it or copy it to the working folder (default setting).",
							"Mkvpropedit not found",
							JOptionPane.ERROR_MESSAGE);
				} else {
					setCmdLine();
					if (cmdLineBatchOpt.size() == 0) {
						JOptionPane.showMessageDialog(frmJMkvpropedit,
								"Nothing to do!",
								"",	JOptionPane.INFORMATION_MESSAGE);
					} else {
						executeBatch();
					}
				}
				
			}
		});
		
		
		/* Button "Generate command line" */
		
		btnGenerateCmdLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (modelFiles.getSize() == 0) {
					JOptionPane.showMessageDialog(frmJMkvpropedit,
							"The file list is empty!",
							"Empty list",
							JOptionPane.ERROR_MESSAGE);
				} else {
					setCmdLine();
					
					if (cmdLineBatch.size() == 0) {
						JOptionPane.showMessageDialog(frmJMkvpropedit,
								"Nothing to do!",
								"",	JOptionPane.INFORMATION_MESSAGE);
					} else {
						txtOutput.setText("");
						
						if (cmdLineBatch.size() > 0) {
							for (int i = 0; i < modelFiles.size(); i++) {
								txtOutput.append(cmdLineBatch.get(i) + "\n");
							}
							
							tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
						}
					}
				}
			}
		});

		new FileDrop(listFiles, new FileDrop.Listener() {
        	public void filesDropped(java.io.File[] files) {
        		for (int i = 0; i < files.length; i++) {
        			try {
        				FileFilter filter = new FileNameExtensionFilter("Matroska files (*.mkv; *.mka; *.mk3d) ", "mkv", "mka", "mk3d");
        				
        				if (!modelFiles.contains(files[i].getCanonicalPath()) && filter.accept(files[i]) && !files[i].isDirectory())
        					modelFiles.add(modelFiles.getSize(), files[i].getCanonicalPath());
        			} catch(java.io.IOException e) {
        			}
        		}
        	}
        });
        
		btnAddFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] files = null;
				
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select Matroska file to edit");
				chooser.setMultiSelectionEnabled(true);
				FileFilter filter = new FileNameExtensionFilter("Matroska files (*.mkv; *.mka; *.mk3d) ", "mkv", "mka", "mk3d");
				chooser.resetChoosableFileFilters();
				chooser.setFileFilter(filter);
				
				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					files = chooser.getSelectedFiles();
					for (int i = 0; i < files.length; i++) {
							try {
								if (!modelFiles.contains(files[i].getCanonicalPath()))
									modelFiles.add(modelFiles.getSize(), files[i].getCanonicalPath());
							} catch (IOException e1) {
							}
				    }
				}
				
			}
		});
		
	    btnRemoveFiles.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
				if (modelFiles.getSize() > 0) {
					while (listFiles.getSelectedIndex() != -1) {
						int[] idx = listFiles.getSelectedIndices();
						modelFiles.remove(idx[0]);
					}
				}
	    	}
	    });
	    
		btnClearFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modelFiles.removeAllElements();
			}
		});
		
		btnTopFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] idx = listFiles.getSelectedIndices();
				for (int i = 0; i < idx.length; i++) {
					int pos = idx[i];
					if (pos > 0) {
						String temp = (String)modelFiles.remove(pos);
						modelFiles.add(i, temp);
						listFiles.ensureIndexIsVisible(0);
						idx[i] = i;
					}
				}
				listFiles.setSelectedIndices(idx);
			}
		});
		
		btnUpFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] idx = listFiles.getSelectedIndices();
				for (int i = 0; i < idx.length; i++) {
					int pos = idx[i];
					
					if (pos > 0 && listFiles.getMinSelectionIndex() != 0) {
						String temp = (String)modelFiles.remove(pos);
						modelFiles.add(pos-1, temp);
						listFiles.ensureIndexIsVisible(pos-1);
						idx[i]--;
					}
				}
				listFiles.setSelectedIndices(idx);
			}
		});

		btnDownFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] idx = listFiles.getSelectedIndices();
				for (int i = idx.length-1; i > -1; i--) {
					int pos = idx[i];
					if (pos < modelFiles.getSize()-1 && listFiles.getMaxSelectionIndex() != modelFiles.getSize()-1) {
						String temp = (String)modelFiles.remove(pos);
						modelFiles.add(pos+1, temp);
						listFiles.ensureIndexIsVisible(pos+1);
						idx[i]++;
					}
				}
				listFiles.setSelectedIndices(idx);
			}
		});
		
		btnBottomFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] idx = listFiles.getSelectedIndices();
				int j = 0;
				for (int i = idx.length-1; i > -1; i--) {
					int pos = idx[i];
					
					if (pos < modelFiles.getSize()-1 && listFiles.getMaxSelectionIndex() != modelFiles.getSize()-1) {
						String temp = (String)modelFiles.remove(pos);
						modelFiles.add(modelFiles.getSize()-j, temp);
						j++;
						listFiles.ensureIndexIsVisible(modelFiles.getSize()-1);
						idx[i] = modelFiles.getSize()-j;
					}
				}
				listFiles.setSelectedIndices(idx);
			}
		});
		
		chbTitleGeneral.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean state = txtTitleGeneral.isEnabled();
				if (txtTitleGeneral.isEnabled() || chbTitleGeneral.isSelected()) { 
					txtTitleGeneral.setEnabled(!state);
					cbNumbGeneral.setEnabled(!state);
					
					if (cbNumbGeneral.isSelected()) {
						lblNumbStartGeneral.setEnabled(!state);
						txtNumbStartGeneral.setEnabled(!state);
						lblNumbPadGeneral.setEnabled(!state);
						txtNumbPadGeneral.setEnabled(!state);
						lblNumbExplainGeneral.setEnabled(!state);
					}
				}
			}
		});
		
		cbNumbGeneral.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean state = txtNumbStartGeneral.isEnabled();
				lblNumbStartGeneral.setEnabled(!state);
				txtNumbStartGeneral.setEnabled(!state);
				lblNumbPadGeneral.setEnabled(!state);
				txtNumbPadGeneral.setEnabled(!state);
				lblNumbExplainGeneral.setEnabled(!state);
			}
		});
		
		txtNumbStartGeneral.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					if (Integer.parseInt(txtNumbStartGeneral.getText()) < 0)
						txtNumbStartGeneral.setText("1");
				} catch (NumberFormatException e1) {
					txtNumbStartGeneral.setText("1");
				}
			}
		});
		
		txtNumbPadGeneral.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					if (Integer.parseInt(txtNumbPadGeneral.getText()) < 0)
						txtNumbPadGeneral.setText("1");
				} catch (NumberFormatException e1) {
					txtNumbPadGeneral.setText("1");
				}
			}
		});
		
		chbExtraCmdGeneral.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean state = txtExtraCmdGeneral.isEnabled();
				txtExtraCmdGeneral.setEnabled(!state);
			}
		});
		
		btnBrowseMkvPropExe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select mkvpropedit executable");
				chooser.setMultiSelectionEnabled(false);
				FileFilter filter = new FileNameExtensionFilter("Excecutable files (*.exe)", "exe");
				chooser.resetChoosableFileFilters();
				chooser.setFileFilter(filter);
				
				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					saveIniFile(chooser.getSelectedFile());
				}
			}
		});
		
		cbMkvPropExeDef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isWindows()) {
					txtMkvPropExe.setText("mkvpropedit.exe");
					cbMkvPropExeDef.setEnabled(false);
					defaultIniFile();
				} else {
					txtMkvPropExe.setText("/usr/bin/mkvpropedit");
					cbMkvPropExeDef.setEnabled(false);
					defaultIniFile();
				}
			}
		});
		
		cbVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cbVideo.getItemCount() != 0)
					lyrdPnlVideo.moveToFront(subPnlVideo[cbVideo.getSelectedIndex()]);
			}
		});
		
		btnAddVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addVideoTrack();
				cbVideo.setSelectedIndex(cbVideo.getItemCount()-1);
				if (cbVideo.getItemCount() == maxStreams) {
					btnAddVideo.setEnabled(false);
				}
			}
		});
		
		cbAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cbAudio.getItemCount() != 0)
					lyrdPnlAudio.moveToFront(subPnlAudio[cbAudio.getSelectedIndex()]);
			}
		});
		
		btnAddAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAudioTrack();
				cbAudio.setSelectedIndex(cbAudio.getItemCount()-1);
				if (cbAudio.getItemCount() == maxStreams) {
					btnAddAudio.setEnabled(false);
				}
			}
		});
		
		cbSubtitle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cbSubtitle.getItemCount() != 0)
					lyrdPnlSubtitle.moveToFront(subPnlSubtitle[cbSubtitle.getSelectedIndex()]);
			}
		});
		
		btnAddSubtitle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addSubtitleTrack();
				cbSubtitle.setSelectedIndex(cbSubtitle.getItemCount()-1);
				if (cbSubtitle.getItemCount() == maxStreams) {
					btnAddSubtitle.setEnabled(false);
				}
			}
		});
	}
	
	/* Start of track addition operations */
	
	private void addVideoTrack() {
		if (nVideo < maxStreams) {
			subPnlVideo[nVideo] = new JPanel();		
			subPnlVideo[nVideo].setBounds(0, 0, 728, 294);
			lyrdPnlVideo.add(subPnlVideo[nVideo]);
			subPnlVideo[nVideo].setLayout(null);
			
			chbEditVideo[nVideo] = new JCheckBox("Edit this track");
			chbEditVideo[nVideo].setBounds(6, 7, 139, 23);
			subPnlVideo[nVideo].add(chbEditVideo[nVideo]);

			chbDefaultVideo[nVideo] = new JCheckBox("Default track");
			chbDefaultVideo[nVideo].setEnabled(false);
			chbDefaultVideo[nVideo].setBounds(6, 32, 91, 23);
			if (!isWindows())
				chbDefaultVideo[nVideo].setBounds(6, 32, 120, 23);
			subPnlVideo[nVideo].add(chbDefaultVideo[nVideo]);
			
			rbYesDefVideo[nVideo] = new JRadioButton("Yes");
			rbYesDefVideo[nVideo].setSelected(true);
			rbYesDefVideo[nVideo].setBounds(99, 32, 46, 23);
			if (!isWindows())
				rbYesDefVideo[nVideo].setBounds(131, 32, 55, 23);
			rbYesDefVideo[nVideo].setEnabled(false);
			subPnlVideo[nVideo].add(rbYesDefVideo[nVideo]);
			
			rbNoDefVideo[nVideo] = new JRadioButton("No");
			rbNoDefVideo[nVideo].setBounds(143, 32, 46, 23);
			if (!isWindows())
				rbNoDefVideo[nVideo].setBounds(194, 32, 55, 23);
			rbNoDefVideo[nVideo].setEnabled(false);
			subPnlVideo[nVideo].add(rbNoDefVideo[nVideo]);
			
			bgRbDefVideo[nVideo] = new ButtonGroup();
			bgRbDefVideo[nVideo].add(rbYesDefVideo[nVideo]);
			bgRbDefVideo[nVideo].add(rbNoDefVideo[nVideo]);
			
			chbForcedVideo[nVideo] = new JCheckBox("Forced track");
			chbForcedVideo[nVideo].setEnabled(false);
			chbForcedVideo[nVideo].setBounds(6, 57, 85, 23);
			if (!isWindows())
				chbForcedVideo[nVideo].setBounds(6, 57, 120, 23);
			subPnlVideo[nVideo].add(chbForcedVideo[nVideo]);
			
			rbYesForcedVideo[nVideo] = new JRadioButton("Yes");
			rbYesForcedVideo[nVideo].setSelected(true);
			rbYesForcedVideo[nVideo].setBounds(99, 57, 46, 23);
			if (!isWindows())
				rbYesForcedVideo[nVideo].setBounds(131, 57, 55, 23);
			rbYesForcedVideo[nVideo].setEnabled(false);
			subPnlVideo[nVideo].add(rbYesForcedVideo[nVideo]);
			
			rbNoForcedVideo[nVideo] = new JRadioButton("No");
			rbNoForcedVideo[nVideo].setBounds(143, 57, 46, 23);
			if (!isWindows())
				rbNoForcedVideo[nVideo].setBounds(194, 57, 55, 23);
			rbNoForcedVideo[nVideo].setEnabled(false);
			subPnlVideo[nVideo].add(rbNoForcedVideo[nVideo]);
			
			bgRbForcedVideo[nVideo] = new ButtonGroup();
			bgRbForcedVideo[nVideo].add(rbYesForcedVideo[nVideo]);
			bgRbForcedVideo[nVideo].add(rbNoForcedVideo[nVideo]);
			
			chbNameVideo[nVideo] = new JCheckBox("Track name");
			chbNameVideo[nVideo].setEnabled(false);
			chbNameVideo[nVideo].setBounds(6, 82, 81, 23);
			if (!isWindows())
				chbNameVideo[nVideo].setBounds(6, 82, 114, 23);
			subPnlVideo[nVideo].add(chbNameVideo[nVideo]);
			
			txtNameVideo[nVideo] = new JTextField();
			txtNameVideo[nVideo].setEnabled(false);
			txtNameVideo[nVideo].setBounds(93, 83, 625, 20);
			if (!isWindows())
				txtNameVideo[nVideo].setBounds(123, 83, 595, 20);
			subPnlVideo[nVideo].add(txtNameVideo[nVideo]);
			txtNameVideo[nVideo].setColumns(10);
			
			cbNumbVideo[nVideo] = new JCheckBox("Numbering");
			cbNumbVideo[nVideo].setEnabled(false);
			cbNumbVideo[nVideo].setBounds(16, 113, 81, 23);
			if (!isWindows())
				cbNumbVideo[nVideo].setBounds(16, 113, 104, 23);
			subPnlVideo[nVideo].add(cbNumbVideo[nVideo]);
			
			lblNumbStartVideo[nVideo] = new JLabel("Start");
			lblNumbStartVideo[nVideo].setEnabled(false);
			lblNumbStartVideo[nVideo].setBounds(191, 115, 31, 14);
			if (!isWindows())
				lblNumbStartVideo[nVideo].setBounds(191, 115, 45, 14);
			subPnlVideo[nVideo].add(lblNumbStartVideo[nVideo]);
			
			txtNumbStartVideo[nVideo] = new JTextField();
			txtNumbStartVideo[nVideo].setEnabled(false);
			txtNumbStartVideo[nVideo].setText("1");
			txtNumbStartVideo[nVideo].setBounds(220, 113, 70, 20);
			if (!isWindows())
				txtNumbStartVideo[nVideo].setBounds(232, 113, 70, 20);
			subPnlVideo[nVideo].add(txtNumbStartVideo[nVideo]);
			txtNumbStartVideo[nVideo].setColumns(10);
			
			lblNumbPadVideo[nVideo] = new JLabel("Padding");
			lblNumbPadVideo[nVideo].setEnabled(false);
			lblNumbPadVideo[nVideo].setBounds(322, 115, 45, 14);
			if (!isWindows())
				lblNumbPadVideo[nVideo].setBounds(337, 115, 64, 14);
			subPnlVideo[nVideo].add(lblNumbPadVideo[nVideo]);
			
			txtNumbPadVideo[nVideo] = new JTextField();
			txtNumbPadVideo[nVideo].setEnabled(false);
			txtNumbPadVideo[nVideo].setText("1");
			txtNumbPadVideo[nVideo].setBounds(366, 113, 70, 20);
			if (!isWindows())
				txtNumbPadVideo[nVideo].setBounds(402, 113, 70, 20);
			subPnlVideo[nVideo].add(txtNumbPadVideo[nVideo]);
			txtNumbPadVideo[nVideo].setColumns(10);
			
			lblNumbExplainVideo[nVideo] = new JLabel("To use it, add {num} to track name (e.g. \"My Video {num}\")");
			lblNumbExplainVideo[nVideo].setEnabled(false);
			lblNumbExplainVideo[nVideo].setBounds(33, 143, 473, 14);
			if (!isWindows())
				lblNumbExplainVideo[nVideo].setBounds(33, 143, 473, 14);
			subPnlVideo[nVideo].add(lblNumbExplainVideo[nVideo]);
			
			chbLangVideo[nVideo] = new JCheckBox("Language");
			chbLangVideo[nVideo].setEnabled(false);
			chbLangVideo[nVideo].setBounds(6, 164, 73, 23);
			if (!isWindows())
				chbLangVideo[nVideo].setBounds(6, 164, 104, 23);
			subPnlVideo[nVideo].add(chbLangVideo[nVideo]);

			cbLangVideo[nVideo] = new JComboBox();
			cbLangVideo[nVideo].setEnabled(false);
			cbLangVideo[nVideo].setBounds(93, 164, 430, 20);
			if (!isWindows())
				cbLangVideo[nVideo].setBounds(123, 164, 595, 20);
			cbLangVideo[nVideo].setModel(new DefaultComboBoxModel(mkvLang.getLangName()));
			subPnlVideo[nVideo].add(cbLangVideo[nVideo]);
			
			int pos = mkvLang.getAsLangCode().indexOf("und");
			cbLangVideo[nVideo].setSelectedIndex(pos);
			
			chbExtraCmdVideo[nVideo] = new JCheckBox("Extra parameters");
			chbExtraCmdVideo[nVideo].setEnabled(false);
			chbExtraCmdVideo[nVideo].setBounds(6, 190, 109, 23);
			if (!isWindows())	
				chbExtraCmdVideo[nVideo].setBounds(6, 190, 153, 23);
			subPnlVideo[nVideo].add(chbExtraCmdVideo[nVideo]);
			
			txtExtraCmdVideo[nVideo] = new JTextField();
			txtExtraCmdVideo[nVideo].setEnabled(false);
			txtExtraCmdVideo[nVideo].setBounds(126, 191, 592, 20);
			if (!isWindows())
				txtExtraCmdVideo[nVideo].setBounds(165, 191, 553, 20);
			subPnlVideo[nVideo].add(txtExtraCmdVideo[nVideo]);
			txtExtraCmdVideo[nVideo].setColumns(10);

			chbEditVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {			
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = chbDefaultVideo[curCbVideo].isEnabled();
					
					chbDefaultVideo[curCbVideo].setEnabled(!state);
					chbForcedVideo[curCbVideo].setEnabled(!state);
					chbNameVideo[curCbVideo].setEnabled(!state);
					chbLangVideo[curCbVideo].setEnabled(!state);
					chbExtraCmdVideo[curCbVideo].setEnabled(!state);
					
					if (txtNameVideo[curCbVideo].isEnabled() || chbNameVideo[curCbVideo].isSelected()) { 
						txtNameVideo[curCbVideo].setEnabled(!state);
						cbNumbVideo[curCbVideo].setEnabled(!state);
						if (cbNumbVideo[curCbVideo].isSelected()) {
							lblNumbStartVideo[curCbVideo].setEnabled(!state);
							txtNumbStartVideo[curCbVideo].setEnabled(!state);
							lblNumbPadVideo[curCbVideo].setEnabled(!state);
							txtNumbPadVideo[curCbVideo].setEnabled(!state);
							lblNumbExplainVideo[curCbVideo].setEnabled(!state);
						}
					}
					
					if (rbNoDefVideo[curCbVideo].isEnabled() || chbDefaultVideo[curCbVideo].isSelected()) {
						rbNoDefVideo[curCbVideo].setEnabled(!state);
						rbYesDefVideo[curCbVideo].setEnabled(!state);
					}
					
					if (rbNoForcedVideo[curCbVideo].isEnabled() || chbForcedVideo[curCbVideo].isSelected()) {
						rbNoForcedVideo[curCbVideo].setEnabled(!state);
						rbYesForcedVideo[curCbVideo].setEnabled(!state);
					}
					
					if (cbLangVideo[curCbVideo].isEnabled() || chbLangVideo[curCbVideo].isSelected()) {
						cbLangVideo[curCbVideo].setEnabled(!state);
					}
					
					if (txtExtraCmdVideo[curCbVideo].isEnabled() || chbExtraCmdVideo[curCbVideo].isSelected()) {
						chbExtraCmdVideo[curCbVideo].setEnabled(!state);
						txtExtraCmdVideo[curCbVideo].setEnabled(!state);
					}
				}
			});
			
			chbNameVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = cbNumbVideo[curCbVideo].isEnabled();
					
					cbNumbVideo[curCbVideo].setEnabled(!state);
					txtNameVideo[curCbVideo].setEnabled(!state);

					if (cbNumbVideo[curCbVideo].isSelected()) {
						lblNumbStartVideo[curCbVideo].setEnabled(!state);
						txtNumbStartVideo[curCbVideo].setEnabled(!state);
						lblNumbPadVideo[curCbVideo].setEnabled(!state);
						txtNumbPadVideo[curCbVideo].setEnabled(!state);
						lblNumbExplainVideo[curCbVideo].setEnabled(!state);
					}
				}
			});
			
			chbDefaultVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {				
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = rbNoDefVideo[curCbVideo].isEnabled();
					
					rbNoDefVideo[curCbVideo].setEnabled(!state);
					rbYesDefVideo[curCbVideo].setEnabled(!state);
				}
			});
			
			chbForcedVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = rbNoForcedVideo[curCbVideo].isEnabled();
					
					rbNoForcedVideo[curCbVideo].setEnabled(!state);
					rbYesForcedVideo[curCbVideo].setEnabled(!state);
				}
			});
			
			cbNumbVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = txtNumbStartVideo[curCbVideo].isEnabled();
					
					lblNumbStartVideo[curCbVideo].setEnabled(!state);
					txtNumbStartVideo[curCbVideo].setEnabled(!state);
					lblNumbPadVideo[curCbVideo].setEnabled(!state);
					txtNumbPadVideo[curCbVideo].setEnabled(!state);
					lblNumbExplainVideo[curCbVideo].setEnabled(!state);
				}
			});
			
			chbLangVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = cbLangVideo[curCbVideo].isEnabled();
					
					cbLangVideo[curCbVideo].setEnabled(!state);
				}
			});
			
			txtNumbStartVideo[nVideo].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					
					try {
						if (Integer.parseInt(txtNumbStartVideo[curCbVideo].getText()) < 0)
							txtNumbStartVideo[curCbVideo].setText("1");
					} catch (NumberFormatException e1) {
						txtNumbStartVideo[curCbVideo].setText("1");
					}
				}
			});
			
			txtNumbPadVideo[nVideo].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
	        
					try {
						if (Integer.parseInt(txtNumbPadVideo[curCbVideo].getText()) < 0)
							txtNumbPadVideo[curCbVideo].setText("1");
					} catch (NumberFormatException e1) {
						txtNumbPadVideo[curCbVideo].setText("1");
					}
				}
			});
			
			chbExtraCmdVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = txtExtraCmdVideo[curCbVideo].isEnabled();
					
					txtExtraCmdVideo[curCbVideo].setEnabled(!state);
				}
			});
			cbVideo.addItem("Video Track " + (nVideo+1));
		}
		nVideo++;
	}

	private void addAudioTrack() {
		if (nAudio < maxStreams) {
			subPnlAudio[nAudio] = new JPanel();		
			subPnlAudio[nAudio].setBounds(0, 0, 728, 294);
			lyrdPnlAudio.add(subPnlAudio[nAudio]);
			subPnlAudio[nAudio].setLayout(null);
			
			chbEditAudio[nAudio] = new JCheckBox("Edit this track");
			chbEditAudio[nAudio].setBounds(6, 7, 139, 23);
			subPnlAudio[nAudio].add(chbEditAudio[nAudio]);

			chbDefaultAudio[nAudio] = new JCheckBox("Default track");
			chbDefaultAudio[nAudio].setEnabled(false);
			chbDefaultAudio[nAudio].setBounds(6, 32, 91, 23);
			if (!isWindows())
				chbDefaultAudio[nAudio].setBounds(6, 32, 120, 23);
			subPnlAudio[nAudio].add(chbDefaultAudio[nAudio]);
			
			rbYesDefAudio[nAudio] = new JRadioButton("Yes");
			rbYesDefAudio[nAudio].setSelected(true);
			rbYesDefAudio[nAudio].setBounds(99, 32, 46, 23);
			if (!isWindows())
				rbYesDefAudio[nAudio].setBounds(131, 32, 55, 23);
			rbYesDefAudio[nAudio].setEnabled(false);
			subPnlAudio[nAudio].add(rbYesDefAudio[nAudio]);
			
			rbNoDefAudio[nAudio] = new JRadioButton("No");
			rbNoDefAudio[nAudio].setBounds(143, 32, 46, 23);
			if (!isWindows())
				rbNoDefAudio[nAudio].setBounds(194, 32, 55, 23);
			rbNoDefAudio[nAudio].setEnabled(false);
			subPnlAudio[nAudio].add(rbNoDefAudio[nAudio]);
			
			bgRbDefAudio[nAudio] = new ButtonGroup();
			bgRbDefAudio[nAudio].add(rbYesDefAudio[nAudio]);
			bgRbDefAudio[nAudio].add(rbNoDefAudio[nAudio]);
			
			chbForcedAudio[nAudio] = new JCheckBox("Forced track");
			chbForcedAudio[nAudio].setEnabled(false);
			chbForcedAudio[nAudio].setBounds(6, 57, 85, 23);
			if (!isWindows())
				chbForcedAudio[nAudio].setBounds(6, 57, 120, 23);
			subPnlAudio[nAudio].add(chbForcedAudio[nAudio]);
			
			rbYesForcedAudio[nAudio] = new JRadioButton("Yes");
			rbYesForcedAudio[nAudio].setSelected(true);
			rbYesForcedAudio[nAudio].setBounds(99, 57, 46, 23);
			if (!isWindows())
				rbYesForcedAudio[nAudio].setBounds(131, 57, 55, 23);
			rbYesForcedAudio[nAudio].setEnabled(false);
			subPnlAudio[nAudio].add(rbYesForcedAudio[nAudio]);
			
			rbNoForcedAudio[nAudio] = new JRadioButton("No");
			rbNoForcedAudio[nAudio].setBounds(143, 57, 46, 23);
			if (!isWindows())
				rbNoForcedAudio[nAudio].setBounds(194, 57, 55, 23);
			rbNoForcedAudio[nAudio].setEnabled(false);
			subPnlAudio[nAudio].add(rbNoForcedAudio[nAudio]);
			
			bgRbForcedAudio[nAudio] = new ButtonGroup();
			bgRbForcedAudio[nAudio].add(rbYesForcedAudio[nAudio]);
			bgRbForcedAudio[nAudio].add(rbNoForcedAudio[nAudio]);
			
			chbNameAudio[nAudio] = new JCheckBox("Track name");
			chbNameAudio[nAudio].setEnabled(false);
			chbNameAudio[nAudio].setBounds(6, 82, 81, 23);
			if (!isWindows())
				chbNameAudio[nAudio].setBounds(6, 82, 114, 23);
			subPnlAudio[nAudio].add(chbNameAudio[nAudio]);
			
			txtNameAudio[nAudio] = new JTextField();
			txtNameAudio[nAudio].setEnabled(false);
			txtNameAudio[nAudio].setBounds(93, 83, 625, 20);
			if (!isWindows())
				txtNameAudio[nAudio].setBounds(123, 83, 595, 20);
			subPnlAudio[nAudio].add(txtNameAudio[nAudio]);
			txtNameAudio[nAudio].setColumns(10);
			
			cbNumbAudio[nAudio] = new JCheckBox("Numbering");
			cbNumbAudio[nAudio].setEnabled(false);
			cbNumbAudio[nAudio].setBounds(16, 113, 81, 23);
			if (!isWindows())
				cbNumbAudio[nAudio].setBounds(16, 113, 104, 23);
			subPnlAudio[nAudio].add(cbNumbAudio[nAudio]);
			
			lblNumbStartAudio[nAudio] = new JLabel("Start");
			lblNumbStartAudio[nAudio].setEnabled(false);
			lblNumbStartAudio[nAudio].setBounds(191, 115, 31, 14);
			if (!isWindows())
				lblNumbStartAudio[nAudio].setBounds(191, 115, 45, 14);
			subPnlAudio[nAudio].add(lblNumbStartAudio[nAudio]);
			
			txtNumbStartAudio[nAudio] = new JTextField();
			txtNumbStartAudio[nAudio].setEnabled(false);
			txtNumbStartAudio[nAudio].setText("1");
			txtNumbStartAudio[nAudio].setBounds(220, 113, 70, 20);
			if (!isWindows())
				txtNumbStartAudio[nAudio].setBounds(232, 113, 70, 20);
			subPnlAudio[nAudio].add(txtNumbStartAudio[nAudio]);
			txtNumbStartAudio[nAudio].setColumns(10);
			
			lblNumbPadAudio[nAudio] = new JLabel("Padding");
			lblNumbPadAudio[nAudio].setEnabled(false);
			lblNumbPadAudio[nAudio].setBounds(322, 115, 45, 14);
			if (!isWindows())
				lblNumbPadAudio[nAudio].setBounds(337, 115, 64, 14);
			subPnlAudio[nAudio].add(lblNumbPadAudio[nAudio]);
			
			txtNumbPadAudio[nAudio] = new JTextField();
			txtNumbPadAudio[nAudio].setEnabled(false);
			txtNumbPadAudio[nAudio].setText("1");
			txtNumbPadAudio[nAudio].setBounds(366, 113, 70, 20);
			if (!isWindows())
				txtNumbPadAudio[nAudio].setBounds(402, 113, 70, 20);
			subPnlAudio[nAudio].add(txtNumbPadAudio[nAudio]);
			txtNumbPadAudio[nAudio].setColumns(10);
			
			lblNumbExplainAudio[nAudio] = new JLabel("To use it, add {num} to track name (e.g. \"My Audio {num}\")");
			lblNumbExplainAudio[nAudio].setEnabled(false);
			lblNumbExplainAudio[nAudio].setBounds(33, 143, 473, 14);
			if (!isWindows())
				lblNumbExplainAudio[nAudio].setBounds(33, 143, 473, 14);
			subPnlAudio[nAudio].add(lblNumbExplainAudio[nAudio]);
			
			chbLangAudio[nAudio] = new JCheckBox("Language");
			chbLangAudio[nAudio].setEnabled(false);
			chbLangAudio[nAudio].setBounds(6, 164, 73, 23);
			if (!isWindows())
				chbLangAudio[nAudio].setBounds(6, 164, 104, 23);
			subPnlAudio[nAudio].add(chbLangAudio[nAudio]);

			cbLangAudio[nAudio] = new JComboBox();
			cbLangAudio[nAudio].setEnabled(false);
			cbLangAudio[nAudio].setBounds(93, 164, 430, 20);
			if (!isWindows())
				cbLangAudio[nAudio].setBounds(123, 164, 595, 20);
			cbLangAudio[nAudio].setModel(new DefaultComboBoxModel(mkvLang.getLangName()));
			subPnlAudio[nAudio].add(cbLangAudio[nAudio]);
			
			int pos = mkvLang.getAsLangCode().indexOf("und");
			cbLangAudio[nAudio].setSelectedIndex(pos);
			
			chbExtraCmdAudio[nAudio] = new JCheckBox("Extra parameters");
			chbExtraCmdAudio[nAudio].setEnabled(false);
			chbExtraCmdAudio[nAudio].setBounds(6, 190, 109, 23);
			if (!isWindows())	
				chbExtraCmdAudio[nAudio].setBounds(6, 190, 153, 23);
			subPnlAudio[nAudio].add(chbExtraCmdAudio[nAudio]);
			
			txtExtraCmdAudio[nAudio] = new JTextField();
			txtExtraCmdAudio[nAudio].setEnabled(false);
			txtExtraCmdAudio[nAudio].setBounds(126, 191, 592, 20);
			if (!isWindows())
				txtExtraCmdAudio[nAudio].setBounds(165, 191, 553, 20);
			subPnlAudio[nAudio].add(txtExtraCmdAudio[nAudio]);
			txtExtraCmdAudio[nAudio].setColumns(10);

			chbEditAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {			
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = chbDefaultAudio[curCbAudio].isEnabled();
					
					chbDefaultAudio[curCbAudio].setEnabled(!state);
					chbForcedAudio[curCbAudio].setEnabled(!state);
					chbNameAudio[curCbAudio].setEnabled(!state);
					chbLangAudio[curCbAudio].setEnabled(!state);
					chbExtraCmdAudio[curCbAudio].setEnabled(!state);
					
					if (txtNameAudio[curCbAudio].isEnabled() || chbNameAudio[curCbAudio].isSelected()) { 
						txtNameAudio[curCbAudio].setEnabled(!state);
						cbNumbAudio[curCbAudio].setEnabled(!state);
						if (cbNumbAudio[curCbAudio].isSelected()) {
							lblNumbStartAudio[curCbAudio].setEnabled(!state);
							txtNumbStartAudio[curCbAudio].setEnabled(!state);
							lblNumbPadAudio[curCbAudio].setEnabled(!state);
							txtNumbPadAudio[curCbAudio].setEnabled(!state);
							lblNumbExplainAudio[curCbAudio].setEnabled(!state);
						}
					}
					
					if (rbNoDefAudio[curCbAudio].isEnabled() || chbDefaultAudio[curCbAudio].isSelected()) {
						rbNoDefAudio[curCbAudio].setEnabled(!state);
						rbYesDefAudio[curCbAudio].setEnabled(!state);
					}
					
					if (rbNoForcedAudio[curCbAudio].isEnabled() || chbForcedAudio[curCbAudio].isSelected()) {
						rbNoForcedAudio[curCbAudio].setEnabled(!state);
						rbYesForcedAudio[curCbAudio].setEnabled(!state);
					}
					
					if (cbLangAudio[curCbAudio].isEnabled() || chbLangAudio[curCbAudio].isSelected()) {
						cbLangAudio[curCbAudio].setEnabled(!state);
					}
					
					if (txtExtraCmdAudio[curCbAudio].isEnabled() || chbExtraCmdAudio[curCbAudio].isSelected()) {
						chbExtraCmdAudio[curCbAudio].setEnabled(!state);
						txtExtraCmdAudio[curCbAudio].setEnabled(!state);
					}
				}
			});
			
			chbNameAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = cbNumbAudio[curCbAudio].isEnabled();
					
					cbNumbAudio[curCbAudio].setEnabled(!state);
					txtNameAudio[curCbAudio].setEnabled(!state);

					if (cbNumbAudio[curCbAudio].isSelected()) {
						lblNumbStartAudio[curCbAudio].setEnabled(!state);
						txtNumbStartAudio[curCbAudio].setEnabled(!state);
						lblNumbPadAudio[curCbAudio].setEnabled(!state);
						txtNumbPadAudio[curCbAudio].setEnabled(!state);
						lblNumbExplainAudio[curCbAudio].setEnabled(!state);
					}
				}
			});
			
			chbDefaultAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {				
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = rbNoDefAudio[curCbAudio].isEnabled();
					
					rbNoDefAudio[curCbAudio].setEnabled(!state);
					rbYesDefAudio[curCbAudio].setEnabled(!state);
				}
			});
			
			chbForcedAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = rbNoForcedAudio[curCbAudio].isEnabled();
					
					rbNoForcedAudio[curCbAudio].setEnabled(!state);
					rbYesForcedAudio[curCbAudio].setEnabled(!state);
				}
			});
			
			cbNumbAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = txtNumbStartAudio[curCbAudio].isEnabled();
					
					lblNumbStartAudio[curCbAudio].setEnabled(!state);
					txtNumbStartAudio[curCbAudio].setEnabled(!state);
					lblNumbPadAudio[curCbAudio].setEnabled(!state);
					txtNumbPadAudio[curCbAudio].setEnabled(!state);
					lblNumbExplainAudio[curCbAudio].setEnabled(!state);
				}
			});
			
			chbLangAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = cbLangAudio[curCbAudio].isEnabled();
					
					cbLangAudio[curCbAudio].setEnabled(!state);
				}
			});
			
			txtNumbStartAudio[nAudio].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					
					try {
						if (Integer.parseInt(txtNumbStartAudio[curCbAudio].getText()) < 0)
							txtNumbStartAudio[curCbAudio].setText("1");
					} catch (NumberFormatException e1) {
						txtNumbStartAudio[curCbAudio].setText("1");
					}
				}
			});
			
			txtNumbPadAudio[nAudio].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
	        
					try {
						if (Integer.parseInt(txtNumbPadAudio[curCbAudio].getText()) < 0)
							txtNumbPadAudio[curCbAudio].setText("1");
					} catch (NumberFormatException e1) {
						txtNumbPadAudio[curCbAudio].setText("1");
					}
				}
			});
			
			chbExtraCmdAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = txtExtraCmdAudio[curCbAudio].isEnabled();
					
					txtExtraCmdAudio[curCbAudio].setEnabled(!state);
				}
			});
			cbAudio.addItem("Audio Track " + (nAudio+1));
		}
		nAudio++;
	}
	
	private void addSubtitleTrack() {
		if (nSubtitle < maxStreams) {
			subPnlSubtitle[nSubtitle] = new JPanel();		
			subPnlSubtitle[nSubtitle].setBounds(0, 0, 728, 294);
			lyrdPnlSubtitle.add(subPnlSubtitle[nSubtitle]);
			subPnlSubtitle[nSubtitle].setLayout(null);
			
			chbEditSubtitle[nSubtitle] = new JCheckBox("Edit this track");
			chbEditSubtitle[nSubtitle].setBounds(6, 7, 139, 23);
			subPnlSubtitle[nSubtitle].add(chbEditSubtitle[nSubtitle]);

			chbDefaultSubtitle[nSubtitle] = new JCheckBox("Default track");
			chbDefaultSubtitle[nSubtitle].setEnabled(false);
			chbDefaultSubtitle[nSubtitle].setBounds(6, 32, 91, 23);
			if (!isWindows())
				chbDefaultSubtitle[nSubtitle].setBounds(6, 32, 120, 23);
			subPnlSubtitle[nSubtitle].add(chbDefaultSubtitle[nSubtitle]);
			
			rbYesDefSubtitle[nSubtitle] = new JRadioButton("Yes");
			rbYesDefSubtitle[nSubtitle].setSelected(true);
			rbYesDefSubtitle[nSubtitle].setBounds(99, 32, 46, 23);
			if (!isWindows())
				rbYesDefSubtitle[nSubtitle].setBounds(131, 32, 55, 23);
			rbYesDefSubtitle[nSubtitle].setEnabled(false);
			subPnlSubtitle[nSubtitle].add(rbYesDefSubtitle[nSubtitle]);
			
			rbNoDefSubtitle[nSubtitle] = new JRadioButton("No");
			rbNoDefSubtitle[nSubtitle].setBounds(143, 32, 46, 23);
			if (!isWindows())
				rbNoDefSubtitle[nSubtitle].setBounds(194, 32, 55, 23);
			rbNoDefSubtitle[nSubtitle].setEnabled(false);
			subPnlSubtitle[nSubtitle].add(rbNoDefSubtitle[nSubtitle]);
			
			bgRbDefSubtitle[nSubtitle] = new ButtonGroup();
			bgRbDefSubtitle[nSubtitle].add(rbYesDefSubtitle[nSubtitle]);
			bgRbDefSubtitle[nSubtitle].add(rbNoDefSubtitle[nSubtitle]);
			
			chbForcedSubtitle[nSubtitle] = new JCheckBox("Forced track");
			chbForcedSubtitle[nSubtitle].setEnabled(false);
			chbForcedSubtitle[nSubtitle].setBounds(6, 57, 85, 23);
			if (!isWindows())
				chbForcedSubtitle[nSubtitle].setBounds(6, 57, 120, 23);
			subPnlSubtitle[nSubtitle].add(chbForcedSubtitle[nSubtitle]);
			
			rbYesForcedSubtitle[nSubtitle] = new JRadioButton("Yes");
			rbYesForcedSubtitle[nSubtitle].setSelected(true);
			rbYesForcedSubtitle[nSubtitle].setBounds(99, 57, 46, 23);
			if (!isWindows())
				rbYesForcedSubtitle[nSubtitle].setBounds(131, 57, 55, 23);
			rbYesForcedSubtitle[nSubtitle].setEnabled(false);
			subPnlSubtitle[nSubtitle].add(rbYesForcedSubtitle[nSubtitle]);
			
			rbNoForcedSubtitle[nSubtitle] = new JRadioButton("No");
			rbNoForcedSubtitle[nSubtitle].setBounds(143, 57, 46, 23);
			if (!isWindows())
				rbNoForcedSubtitle[nSubtitle].setBounds(194, 57, 55, 23);
			rbNoForcedSubtitle[nSubtitle].setEnabled(false);
			subPnlSubtitle[nSubtitle].add(rbNoForcedSubtitle[nSubtitle]);
			
			bgRbForcedSubtitle[nSubtitle] = new ButtonGroup();
			bgRbForcedSubtitle[nSubtitle].add(rbYesForcedSubtitle[nSubtitle]);
			bgRbForcedSubtitle[nSubtitle].add(rbNoForcedSubtitle[nSubtitle]);
			
			chbNameSubtitle[nSubtitle] = new JCheckBox("Track name");
			chbNameSubtitle[nSubtitle].setEnabled(false);
			chbNameSubtitle[nSubtitle].setBounds(6, 82, 81, 23);
			if (!isWindows())
				chbNameSubtitle[nSubtitle].setBounds(6, 82, 114, 23);
			subPnlSubtitle[nSubtitle].add(chbNameSubtitle[nSubtitle]);
			
			txtNameSubtitle[nSubtitle] = new JTextField();
			txtNameSubtitle[nSubtitle].setEnabled(false);
			txtNameSubtitle[nSubtitle].setBounds(93, 83, 625, 20);
			if (!isWindows())
				txtNameSubtitle[nSubtitle].setBounds(123, 83, 595, 20);
			subPnlSubtitle[nSubtitle].add(txtNameSubtitle[nSubtitle]);
			txtNameSubtitle[nSubtitle].setColumns(10);
			
			cbNumbSubtitle[nSubtitle] = new JCheckBox("Numbering");
			cbNumbSubtitle[nSubtitle].setEnabled(false);
			cbNumbSubtitle[nSubtitle].setBounds(16, 113, 81, 23);
			if (!isWindows())
				cbNumbSubtitle[nSubtitle].setBounds(16, 113, 104, 23);
			subPnlSubtitle[nSubtitle].add(cbNumbSubtitle[nSubtitle]);
			
			lblNumbStartSubtitle[nSubtitle] = new JLabel("Start");
			lblNumbStartSubtitle[nSubtitle].setEnabled(false);
			lblNumbStartSubtitle[nSubtitle].setBounds(191, 115, 31, 14);
			if (!isWindows())
				lblNumbStartSubtitle[nSubtitle].setBounds(191, 115, 45, 14);
			subPnlSubtitle[nSubtitle].add(lblNumbStartSubtitle[nSubtitle]);
			
			txtNumbStartSubtitle[nSubtitle] = new JTextField();
			txtNumbStartSubtitle[nSubtitle].setEnabled(false);
			txtNumbStartSubtitle[nSubtitle].setText("1");
			txtNumbStartSubtitle[nSubtitle].setBounds(220, 113, 70, 20);
			if (!isWindows())
				txtNumbStartSubtitle[nSubtitle].setBounds(232, 113, 70, 20);
			subPnlSubtitle[nSubtitle].add(txtNumbStartSubtitle[nSubtitle]);
			txtNumbStartSubtitle[nSubtitle].setColumns(10);
			
			lblNumbPadSubtitle[nSubtitle] = new JLabel("Padding");
			lblNumbPadSubtitle[nSubtitle].setEnabled(false);
			lblNumbPadSubtitle[nSubtitle].setBounds(322, 115, 45, 14);
			if (!isWindows())
				lblNumbPadSubtitle[nSubtitle].setBounds(337, 115, 64, 14);
			subPnlSubtitle[nSubtitle].add(lblNumbPadSubtitle[nSubtitle]);
			
			txtNumbPadSubtitle[nSubtitle] = new JTextField();
			txtNumbPadSubtitle[nSubtitle].setEnabled(false);
			txtNumbPadSubtitle[nSubtitle].setText("1");
			txtNumbPadSubtitle[nSubtitle].setBounds(366, 113, 70, 20);
			if (!isWindows())
				txtNumbPadSubtitle[nSubtitle].setBounds(402, 113, 70, 20);
			subPnlSubtitle[nSubtitle].add(txtNumbPadSubtitle[nSubtitle]);
			txtNumbPadSubtitle[nSubtitle].setColumns(10);
			
			lblNumbExplainSubtitle[nSubtitle] = new JLabel("To use it, add {num} to track name (e.g. \"My Subtitle {num}\")");
			lblNumbExplainSubtitle[nSubtitle].setEnabled(false);
			lblNumbExplainSubtitle[nSubtitle].setBounds(33, 143, 473, 14);
			if (!isWindows())
				lblNumbExplainSubtitle[nSubtitle].setBounds(33, 143, 473, 14);
			subPnlSubtitle[nSubtitle].add(lblNumbExplainSubtitle[nSubtitle]);
			
			chbLangSubtitle[nSubtitle] = new JCheckBox("Language");
			chbLangSubtitle[nSubtitle].setEnabled(false);
			chbLangSubtitle[nSubtitle].setBounds(6, 164, 73, 23);
			if (!isWindows())
				chbLangSubtitle[nSubtitle].setBounds(6, 164, 104, 23);
			subPnlSubtitle[nSubtitle].add(chbLangSubtitle[nSubtitle]);

			cbLangSubtitle[nSubtitle] = new JComboBox();
			cbLangSubtitle[nSubtitle].setEnabled(false);
			cbLangSubtitle[nSubtitle].setBounds(93, 164, 430, 20);
			if (!isWindows())
				cbLangSubtitle[nSubtitle].setBounds(123, 164, 595, 20);
			cbLangSubtitle[nSubtitle].setModel(new DefaultComboBoxModel(mkvLang.getLangName()));
			subPnlSubtitle[nSubtitle].add(cbLangSubtitle[nSubtitle]);
			
			int pos = mkvLang.getAsLangCode().indexOf("und");
			cbLangSubtitle[nSubtitle].setSelectedIndex(pos);
			
			chbExtraCmdSubtitle[nSubtitle] = new JCheckBox("Extra parameters");
			chbExtraCmdSubtitle[nSubtitle].setEnabled(false);
			chbExtraCmdSubtitle[nSubtitle].setBounds(6, 190, 109, 23);
			if (!isWindows())	
				chbExtraCmdSubtitle[nSubtitle].setBounds(6, 190, 153, 23);
			subPnlSubtitle[nSubtitle].add(chbExtraCmdSubtitle[nSubtitle]);
			
			txtExtraCmdSubtitle[nSubtitle] = new JTextField();
			txtExtraCmdSubtitle[nSubtitle].setEnabled(false);
			txtExtraCmdSubtitle[nSubtitle].setBounds(126, 191, 592, 20);
			if (!isWindows())
				txtExtraCmdSubtitle[nSubtitle].setBounds(165, 191, 553, 20);
			subPnlSubtitle[nSubtitle].add(txtExtraCmdSubtitle[nSubtitle]);
			txtExtraCmdSubtitle[nSubtitle].setColumns(10);

			chbEditSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {			
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = chbDefaultSubtitle[curCbSubtitle].isEnabled();
					
					chbDefaultSubtitle[curCbSubtitle].setEnabled(!state);
					chbForcedSubtitle[curCbSubtitle].setEnabled(!state);
					chbNameSubtitle[curCbSubtitle].setEnabled(!state);
					chbLangSubtitle[curCbSubtitle].setEnabled(!state);
					chbExtraCmdSubtitle[curCbSubtitle].setEnabled(!state);
					
					if (txtNameSubtitle[curCbSubtitle].isEnabled() || chbNameSubtitle[curCbSubtitle].isSelected()) { 
						txtNameSubtitle[curCbSubtitle].setEnabled(!state);
						cbNumbSubtitle[curCbSubtitle].setEnabled(!state);
						if (cbNumbSubtitle[curCbSubtitle].isSelected()) {
							lblNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
							txtNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
							lblNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
							txtNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
							lblNumbExplainSubtitle[curCbSubtitle].setEnabled(!state);
						}
					}
					
					if (rbNoDefSubtitle[curCbSubtitle].isEnabled() || chbDefaultSubtitle[curCbSubtitle].isSelected()) {
						rbNoDefSubtitle[curCbSubtitle].setEnabled(!state);
						rbYesDefSubtitle[curCbSubtitle].setEnabled(!state);
					}
					
					if (rbNoForcedSubtitle[curCbSubtitle].isEnabled() || chbForcedSubtitle[curCbSubtitle].isSelected()) {
						rbNoForcedSubtitle[curCbSubtitle].setEnabled(!state);
						rbYesForcedSubtitle[curCbSubtitle].setEnabled(!state);
					}
					
					if (cbLangSubtitle[curCbSubtitle].isEnabled() || chbLangSubtitle[curCbSubtitle].isSelected()) {
						cbLangSubtitle[curCbSubtitle].setEnabled(!state);
					}
					
					if (txtExtraCmdSubtitle[curCbSubtitle].isEnabled() || chbExtraCmdSubtitle[curCbSubtitle].isSelected()) {
						chbExtraCmdSubtitle[curCbSubtitle].setEnabled(!state);
						txtExtraCmdSubtitle[curCbSubtitle].setEnabled(!state);
					}
				}
			});
			
			chbNameSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = cbNumbSubtitle[curCbSubtitle].isEnabled();
					
					cbNumbSubtitle[curCbSubtitle].setEnabled(!state);
					txtNameSubtitle[curCbSubtitle].setEnabled(!state);

					if (cbNumbSubtitle[curCbSubtitle].isSelected()) {
						lblNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
						txtNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
						lblNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
						txtNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
						lblNumbExplainSubtitle[curCbSubtitle].setEnabled(!state);
					}
				}
			});
			
			chbDefaultSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {				
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = rbNoDefSubtitle[curCbSubtitle].isEnabled();
					
					rbNoDefSubtitle[curCbSubtitle].setEnabled(!state);
					rbYesDefSubtitle[curCbSubtitle].setEnabled(!state);
				}
			});
			
			chbForcedSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = rbNoForcedSubtitle[curCbSubtitle].isEnabled();
					
					rbNoForcedSubtitle[curCbSubtitle].setEnabled(!state);
					rbYesForcedSubtitle[curCbSubtitle].setEnabled(!state);
				}
			});
			
			cbNumbSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = txtNumbStartSubtitle[curCbSubtitle].isEnabled();
					
					lblNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
					txtNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
					lblNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
					txtNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
					lblNumbExplainSubtitle[curCbSubtitle].setEnabled(!state);
				}
			});
			
			chbLangSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = cbLangSubtitle[curCbSubtitle].isEnabled();
					
					cbLangSubtitle[curCbSubtitle].setEnabled(!state);
				}
			});
			
			txtNumbStartSubtitle[nSubtitle].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					
					try {
						if (Integer.parseInt(txtNumbStartSubtitle[curCbSubtitle].getText()) < 0)
							txtNumbStartSubtitle[curCbSubtitle].setText("1");
					} catch (NumberFormatException e1) {
						txtNumbStartSubtitle[curCbSubtitle].setText("1");
					}
				}
			});
			
			txtNumbPadSubtitle[nSubtitle].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
	        
					try {
						if (Integer.parseInt(txtNumbPadSubtitle[curCbSubtitle].getText()) < 0)
							txtNumbPadSubtitle[curCbSubtitle].setText("1");
					} catch (NumberFormatException e1) {
						txtNumbPadSubtitle[curCbSubtitle].setText("1");
					}
				}
			});
			
			chbExtraCmdSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = txtExtraCmdSubtitle[curCbSubtitle].isEnabled();
					
					txtExtraCmdSubtitle[curCbSubtitle].setEnabled(!state);
				}
			});
			cbSubtitle.addItem("Subtitle Track " + (nSubtitle+1));
		}
		nSubtitle++;
	}
	
	/* End of track addition operations */
	
	
	/* Start of command line operations */
	
	private void setCmdLineGeneral() {
		cmdLineGeneral = new String[modelFiles.size()];
		cmdLineGeneralOpt = new String[modelFiles.size()];
		int start = Integer.parseInt(txtNumbStartGeneral.getText());
	
		for (int i = 0; i < modelFiles.size(); i++) {
			cmdLineGeneral[i] = "";
			cmdLineGeneralOpt[i] = "";
			
			if (chbRemoveTags.isSelected()) {
				cmdLineGeneral[i] += " --tags all:";
				cmdLineGeneralOpt[i] += " --tags all:";
			}
			
			if (chbRemoveChapters.isSelected()) {
				cmdLineGeneral[i] += " --chapters \"\"";
				cmdLineGeneralOpt[i] += " --chapters #EMPTY#";
			}
			
			if (chbTitleGeneral.isSelected()) {	
				cmdLineGeneral[i] += " --edit info";
				cmdLineGeneralOpt[i] += " --edit info";
				
				if (cbNumbGeneral.isSelected()) {
					int pad = 0;
					
					pad = Integer.parseInt(txtNumbPadGeneral.getText());
					
					String newTitle = txtTitleGeneral.getText();
					newTitle = newTitle.replace("{num}", padNumber(pad).format(start));
					start++;
					
					cmdLineGeneral[i] += " --set title=\"" + escapeNameCmdLine(newTitle) + "\"";
					cmdLineGeneralOpt[i] += " --set title=\"" + escapeName(newTitle) + "\"";
				} else {
					cmdLineGeneral[i] += " --set title=\"" + escapeNameCmdLine(txtTitleGeneral.getText()) + "\"";
					cmdLineGeneralOpt[i] += " --set title=\"" + escapeName(txtTitleGeneral.getText()) + "\"";
				}
			}

			if (chbExtraCmdGeneral.isSelected() && !txtExtraCmdGeneral.getText().trim().isEmpty()) {
				cmdLineGeneral[i] += " " + txtExtraCmdGeneral.getText();
				cmdLineGeneralOpt[i] += " " + txtExtraCmdGeneral.getText();
			}
		}
		
	}
	
	private void setCmdLineVideo() {
		cmdLineVideo = new String[modelFiles.size()];
		cmdLineVideoOpt = new String[modelFiles.size()];
		String[] tmpCmdLineVideo = new String[nVideo];
		String[] tmpCmdLineVideoOpt = new String[nVideo];
		int[] numStartVideo = new int[nVideo];
		int[] numPadVideo = new int[nVideo];
		
		for (int i = 0; i < modelFiles.size(); i++) {
			int editCount = 0;
			cmdLineVideo[i] = "";
			cmdLineVideoOpt[i] = "";
			
			for (int j = 0; j < nVideo; j++) {
				if (chbEditVideo[j].isSelected()) {
					numStartVideo[j] = Integer.parseInt(txtNumbStartVideo[j].getText());
					numPadVideo[j] = Integer.parseInt(txtNumbPadVideo[j].getText());
					
					tmpCmdLineVideo[j] = "";
					tmpCmdLineVideoOpt[j] = "";
					
					if (chbEditVideo[j].isSelected()) {
						tmpCmdLineVideo[j] += " --edit track:v" + (j+1);
						tmpCmdLineVideoOpt[j] += " --edit track:v" + (j+1);
					}
					
					if (chbDefaultVideo[j].isSelected()) {
						tmpCmdLineVideo[j] += " --set flag-default=";
						tmpCmdLineVideoOpt[j] += " --set flag-default=";
						
						if (rbYesDefVideo[j].isSelected()) {
							tmpCmdLineVideo[j] += "1";
							tmpCmdLineVideoOpt[j] += "1";
						} else { 
							tmpCmdLineVideo[j] += "0";
							tmpCmdLineVideoOpt[j] += "0";
						}
						editCount++;
					}
					
					if (chbForcedVideo[j].isSelected()) {
						tmpCmdLineVideo[j] += " --set flag-forced=";
						tmpCmdLineVideoOpt[j] += " --set flag-forced=";
						
						if (rbYesForcedVideo[j].isSelected()) {
							tmpCmdLineVideo[j] += "1";
							tmpCmdLineVideoOpt[j] += "1";
						} else {
							tmpCmdLineVideo[j] += "0";
							tmpCmdLineVideoOpt[j] += "0";
						}
						editCount++;
					}
					
					if (chbNameVideo[j].isSelected()) {					
						tmpCmdLineVideo[j] += " --set name=\"" + escapeNameCmdLine(txtNameVideo[j].getText()) + "\"";
						tmpCmdLineVideoOpt[j] += " --set name=\"" + escapeName(txtNameVideo[j].getText()) + "\"";
						editCount++;
					}
					
					if (chbLangVideo[j].isSelected()) {
						String curLangCode = mkvLang.getAsLangCode().get(cbLangVideo[j].getSelectedIndex());
						tmpCmdLineVideo[j] += " --set language=\"" + curLangCode + "\"";
						tmpCmdLineVideoOpt[j] += " --set language=\"" + curLangCode + "\"";
						editCount++;
					}
					
					if (chbExtraCmdVideo[j].isSelected() && !txtExtraCmdVideo[j].getText().trim().isEmpty()) {
						tmpCmdLineVideo[j] += " " + txtExtraCmdVideo[j].getText();
						tmpCmdLineVideoOpt[j] += " " + txtExtraCmdVideo[j].getText();
						editCount++;
					}
					
					if (editCount == 0) {
						tmpCmdLineVideo[j] = "";
						tmpCmdLineVideoOpt[j] = "";
					}
				} else {
					tmpCmdLineVideo[j] = "";
					tmpCmdLineVideoOpt[j] = "";
				}
			}
		}
		
		for (int i = 0; i < nVideo; i++) {
			for (int j = 0; j < modelFiles.size(); j++) {
				String tmpText = tmpCmdLineVideo[i];
				String tmpText2 = tmpCmdLineVideoOpt[i];
				
				if (cbNumbVideo[i].isSelected() && chbEditVideo[i].isSelected()) {
					tmpText = tmpText.replace("{num}", padNumber(numPadVideo[i]).format(numStartVideo[i]));
					tmpText2 = tmpText.replace("{num}", padNumber(numPadVideo[i]).format(numStartVideo[i]));
					numStartVideo[i]++;
				}
				cmdLineVideo[j] += tmpText;
				cmdLineVideoOpt[j] += tmpText2;
			}
		}
	}
	
	private void setCmdLineAudio() {
		cmdLineAudio = new String[modelFiles.size()];
		cmdLineAudioOpt = new String[modelFiles.size()];
		String[] tmpCmdLineAudio = new String[nAudio];
		String[] tmpCmdLineAudioOpt = new String[nAudio];
		int[] numStartAudio = new int[nAudio];
		int[] numPadAudio = new int[nAudio];
		
		for (int i = 0; i < modelFiles.size(); i++) {
			int editCount = 0;
			cmdLineAudio[i] = "";
			cmdLineAudioOpt[i] = "";
			
			for (int j = 0; j < nAudio; j++) {
				if (chbEditAudio[j].isSelected()) {
					numStartAudio[j] = Integer.parseInt(txtNumbStartAudio[j].getText());
					numPadAudio[j] = Integer.parseInt(txtNumbPadAudio[j].getText());
					
					tmpCmdLineAudio[j] = "";
					tmpCmdLineAudioOpt[j] = "";
					
					if (chbEditAudio[j].isSelected()) {
						tmpCmdLineAudio[j] += " --edit track:a" + (j+1);
						tmpCmdLineAudioOpt[j] += " --edit track:a" + (j+1);
					}
					
					if (chbDefaultAudio[j].isSelected()) {
						tmpCmdLineAudio[j] += " --set flag-default=";
						tmpCmdLineAudioOpt[j] += " --set flag-default=";
						
						if (rbYesDefAudio[j].isSelected()) {
							tmpCmdLineAudio[j] += "1";
							tmpCmdLineAudioOpt[j] += "1";
						} else { 
							tmpCmdLineAudio[j] += "0";
							tmpCmdLineAudioOpt[j] += "0";
						}
						editCount++;
					}
					
					if (chbForcedAudio[j].isSelected()) {
						tmpCmdLineAudio[j] += " --set flag-forced=";
						tmpCmdLineAudioOpt[j] += " --set flag-forced=";
						
						if (rbYesForcedAudio[j].isSelected()) {
							tmpCmdLineAudio[j] += "1";
							tmpCmdLineAudioOpt[j] += "1";
						} else {
							tmpCmdLineAudio[j] += "0";
							tmpCmdLineAudioOpt[j] += "0";
						}
						editCount++;
					}
					
					if (chbNameAudio[j].isSelected()) {					
						tmpCmdLineAudio[j] += " --set name=\"" + escapeNameCmdLine(txtNameAudio[j].getText()) + "\"";
						tmpCmdLineAudioOpt[j] += " --set name=\"" + escapeName(txtNameAudio[j].getText()) + "\"";
						editCount++;
					}
					
					if (chbLangAudio[j].isSelected()) {
						String curLangCode = mkvLang.getAsLangCode().get(cbLangAudio[j].getSelectedIndex());
						tmpCmdLineAudio[j] += " --set language=\"" + curLangCode + "\"";
						tmpCmdLineAudioOpt[j] += " --set language=\"" + curLangCode + "\"";
						editCount++;
					}
					
					if (chbExtraCmdAudio[j].isSelected() && !txtExtraCmdAudio[j].getText().trim().isEmpty()) {
						tmpCmdLineAudio[j] += " " + txtExtraCmdAudio[j].getText();
						tmpCmdLineAudioOpt[j] += " " + txtExtraCmdAudio[j].getText();
						editCount++;
					}
					
					if (editCount == 0) {
						tmpCmdLineAudio[j] = "";
						tmpCmdLineAudioOpt[j] = "";
					}
				} else {
					tmpCmdLineAudio[j] = "";
					tmpCmdLineAudioOpt[j] = "";
				}
			}
		}
		
		for (int i = 0; i < nAudio; i++) {
			for (int j = 0; j < modelFiles.size(); j++) {
				String tmpText = tmpCmdLineAudio[i];
				String tmpText2 = tmpCmdLineAudioOpt[i];
				
				if (cbNumbAudio[i].isSelected() && chbEditAudio[i].isSelected()) {
					tmpText = tmpText.replace("{num}", padNumber(numPadAudio[i]).format(numStartAudio[i]));
					tmpText2 = tmpText.replace("{num}", padNumber(numPadAudio[i]).format(numStartAudio[i]));
					numStartAudio[i]++;
				}
				cmdLineAudio[j] += tmpText;
				cmdLineAudioOpt[j] += tmpText2;
			}
		}
	}
	
	private void setCmdLineSubtitle() {
		cmdLineSubtitle = new String[modelFiles.size()];
		cmdLineSubtitleOpt = new String[modelFiles.size()];
		String[] tmpCmdLineSubtitle = new String[nSubtitle];
		String[] tmpCmdLineSubtitleOpt = new String[nSubtitle];
		int[] numStartSubtitle = new int[nSubtitle];
		int[] numPadSubtitle = new int[nSubtitle];
		
		for (int i = 0; i < modelFiles.size(); i++) {
			int editCount = 0;
			cmdLineSubtitle[i] = "";
			cmdLineSubtitleOpt[i] = "";
			
			for (int j = 0; j < nSubtitle; j++) {
				if (chbEditSubtitle[j].isSelected()) {
					numStartSubtitle[j] = Integer.parseInt(txtNumbStartSubtitle[j].getText());
					numPadSubtitle[j] = Integer.parseInt(txtNumbPadSubtitle[j].getText());
					
					tmpCmdLineSubtitle[j] = "";
					tmpCmdLineSubtitleOpt[j] = "";
					
					if (chbEditSubtitle[j].isSelected()) {
						tmpCmdLineSubtitle[j] += " --edit track:s" + (j+1);
						tmpCmdLineSubtitleOpt[j] += " --edit track:s" + (j+1);
					}
					
					if (chbDefaultSubtitle[j].isSelected()) {
						tmpCmdLineSubtitle[j] += " --set flag-default=";
						tmpCmdLineSubtitleOpt[j] += " --set flag-default=";
						
						if (rbYesDefSubtitle[j].isSelected()) {
							tmpCmdLineSubtitle[j] += "1";
							tmpCmdLineSubtitleOpt[j] += "1";
						} else { 
							tmpCmdLineSubtitle[j] += "0";
							tmpCmdLineSubtitleOpt[j] += "0";
						}
						editCount++;
					}
					
					if (chbForcedSubtitle[j].isSelected()) {
						tmpCmdLineSubtitle[j] += " --set flag-forced=";
						tmpCmdLineSubtitleOpt[j] += " --set flag-forced=";
						
						if (rbYesForcedSubtitle[j].isSelected()) {
							tmpCmdLineSubtitle[j] += "1";
							tmpCmdLineSubtitleOpt[j] += "1";
						} else {
							tmpCmdLineSubtitle[j] += "0";
							tmpCmdLineSubtitleOpt[j] += "0";
						}
						editCount++;
					}
					
					if (chbNameSubtitle[j].isSelected()) {					
						tmpCmdLineSubtitle[j] += " --set name=\"" + escapeNameCmdLine(txtNameSubtitle[j].getText()) + "\"";
						tmpCmdLineSubtitleOpt[j] += " --set name=\"" + escapeName(txtNameSubtitle[j].getText()) + "\"";
						editCount++;
					}
					
					if (chbLangSubtitle[j].isSelected()) {
						String curLangCode = mkvLang.getAsLangCode().get(cbLangSubtitle[j].getSelectedIndex());
						tmpCmdLineSubtitle[j] += " --set language=\"" + curLangCode + "\"";
						tmpCmdLineSubtitleOpt[j] += " --set language=\"" + curLangCode + "\"";
						editCount++;
					}
					
					if (chbExtraCmdSubtitle[j].isSelected() && !txtExtraCmdSubtitle[j].getText().trim().isEmpty()) {
						tmpCmdLineSubtitle[j] += " " + txtExtraCmdSubtitle[j].getText();
						tmpCmdLineSubtitleOpt[j] += " " + txtExtraCmdSubtitle[j].getText();
						editCount++;
					}
					
					if (editCount == 0) {
						tmpCmdLineSubtitle[j] = "";
						tmpCmdLineSubtitleOpt[j] = "";
					}
				} else {
					tmpCmdLineSubtitle[j] = "";
					tmpCmdLineSubtitleOpt[j] = "";
				}
			}
		}
		
		for (int i = 0; i < nSubtitle; i++) {
			for (int j = 0; j < modelFiles.size(); j++) {
				String tmpText = tmpCmdLineSubtitle[i];
				String tmpText2 = tmpCmdLineSubtitleOpt[i];
				
				if (cbNumbSubtitle[i].isSelected() && chbEditSubtitle[i].isSelected()) {
					tmpText = tmpText.replace("{num}", padNumber(numPadSubtitle[i]).format(numStartSubtitle[i]));
					tmpText2 = tmpText.replace("{num}", padNumber(numPadSubtitle[i]).format(numStartSubtitle[i]));
					numStartSubtitle[i]++;
				}
				cmdLineSubtitle[j] += tmpText;
				cmdLineSubtitleOpt[j] += tmpText2;
			}
		}
	}
	
	private void setCmdLine() {
		setCmdLineGeneral();
		setCmdLineVideo();
		setCmdLineAudio();
		setCmdLineSubtitle();
		
		cmdLineBatch = new ArrayList<String>();
		cmdLineBatchOpt = new ArrayList<String>();
		
		String cmdTemp = cmdLineGeneral[0] + cmdLineVideo[0] + cmdLineAudio[0] + cmdLineSubtitle[0];
		
		if (!cmdTemp.isEmpty()) {
			for (int i = 0; i < modelFiles.getSize(); i++) {
				String cmdLineAll = cmdLineGeneral[i] + cmdLineVideo[i] + cmdLineAudio[i] + cmdLineSubtitle[i];
				String cmdLineAllOpt = cmdLineGeneralOpt[i] + cmdLineVideoOpt[i] + cmdLineAudioOpt[i] + cmdLineSubtitleOpt[i];
				
				if (isWindows()) {
					cmdLineBatch.add("\"" + txtMkvPropExe.getText() + "\" \"" + modelFiles.get(i) + "\"" + cmdLineAll);
					cmdLineBatchOpt.add("\"" + escapePath((String) modelFiles.get(i)) + "\"" + cmdLineAllOpt);
				} else {
					cmdLineBatch.add(escapePath(txtMkvPropExe.getText()) + " " + escapePath((String) modelFiles.get(i)) + cmdLineAll);
					cmdLineBatchOpt.add("\"" + (String) modelFiles.get(i) + "\"" + cmdLineAllOpt);
				}
			}
		}

	}
	
	private void executeBatch() {
		worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				try {
					txtOutput.setText("");
					tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
					tabbedPane.setEnabled(false);
					btnProcessFiles.setEnabled(false);
					btnGenerateCmdLine.setEnabled(false);
					
					for (int i = 0; i < cmdLineBatch.size(); i++) {
						File optFile = new File("options.txt");
						PrintWriter optFilePW = new PrintWriter(optFile, "UTF-8");
						String[] optFileContents = Commandline.translateCommandline(cmdLineBatchOpt.get(i));
						
						if (!optFile.exists()) {
							optFile.createNewFile();
						}
												
					    for (String content:optFileContents) {
					    	optFilePW.println(content);
					    }
					    
						optFilePW.flush();
						optFilePW.close();
						
						if (isWindows()) {
							proc = rt.exec("\"" + txtMkvPropExe.getText() + "\" @options.txt");
						} else {
							proc = rt.exec(escapePath(txtMkvPropExe.getText()) + " @options.txt");
						}
						
						txtOutput.append("File: " + modelFiles.get(i) + "\n");
						txtOutput.append("Command line: " + cmdLineBatch.get(i) + "\n\n");
						
						StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), txtOutput);
						StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), txtOutput);
						
						outputGobbler.start();
						errorGobbler.start();
						proc.waitFor();
						
						optFile.delete();
						
						if (i < cmdLineBatch.size()-1)
							txtOutput.append("--------------\n\n");
					}
				} catch (IOException e) {
				} catch (InterruptedException e) {
				}

				return null;
		    }
		    
		    @Override
		    protected void done() {
		    	tabbedPane.setEnabled(true);
				btnProcessFiles.setEnabled(true);
				btnGenerateCmdLine.setEnabled(true);
		    }						   
		 };
		 worker.execute();
	}
	
	private void parseFiles(ArrayList<String> argsArray) {
		FileFilter filter = new FileNameExtensionFilter("Matroska files (*.mkv; *.mka; *.mk3d) ", "mkv", "mka", "mk3d");
		
		if (argsArray.size() > 0) {
			File f = null;
			for (String arg : argsArray) {
				try {
					f = new File(arg);
					if (f.exists() && !f.isDirectory() && filter.accept(f) && !modelFiles.contains(f.getCanonicalPath())) {
						modelFiles.add(modelFiles.getSize(), f.getCanonicalPath());
					}
				} catch (Exception e) {
				}
			}
		}
	}
	
	/* End of command line operations */
	
	
	/* Start of INI configuration file operations */
	
	private void readIniFile() {
		Ini ini = null;

		if (iniFile.exists()) {
			try {
				ini = new Ini(iniFile);
				String exePath = ini.get("General", "mkvpropedit");
				
				if (exePath != null) {
					File exeFile = new File(exePath);
					if (exeFile.exists()) {
						if (exeFile.toString().equals("mkvpropedit.exe") && isWindows()) {
							cbMkvPropExeDef.setSelected(true);
							cbMkvPropExeDef.setEnabled(false);
						} else if (exeFile.toString().equals("/usr/bin/mkvpropedit") && !isWindows()) {
							cbMkvPropExeDef.setSelected(true);
							cbMkvPropExeDef.setEnabled(false);
						} else {
							txtMkvPropExe.setText(exePath);
							cbMkvPropExeDef.setSelected(false);
							cbMkvPropExeDef.setEnabled(true);
						}
					} else {
						if (isWindows())
							txtMkvPropExe.setText("mvkpropedit.exe");
						else
							txtMkvPropExe.setText("/usr/bin/mkvpropedit");
						
						cbMkvPropExeDef.setSelected(true);
						cbMkvPropExeDef.setEnabled(false);
					}
				}
			} catch (InvalidFileFormatException e) {

			} catch (IOException e) {

			}
		} else if (isWindows()) {
			String exePath = getMkvPropExeFromReg();
			
			if (exePath != null) {
				txtMkvPropExe.setText(exePath);
				cbMkvPropExeDef.setSelected(false);
				cbMkvPropExeDef.setEnabled(true);
				saveIniFile(new File(exePath));
			}
		}
	}
	
	private void saveIniFile(File exeFile) {
		if (exeFile.exists()) {
			Ini ini = null;
			
			if (!iniFile.exists()) {
				try {
					iniFile.createNewFile();
				} catch (IOException e1) {
				}
			}
			
			txtMkvPropExe.setText(exeFile.toString());
			cbMkvPropExeDef.setSelected(false);
			cbMkvPropExeDef.setEnabled(true);
			
			try {
				ini = new Ini(iniFile);
				ini.put("General", "mkvpropedit", exeFile.toString());
				ini.store();
			}
			catch (InvalidFileFormatException e1) {
			}
			catch (IOException e1) {		
			}
		}
	}
	
	private void defaultIniFile() {
		Ini ini = null;
		
		if (!iniFile.exists()) {
			try {
				iniFile.createNewFile();
			} catch (IOException e1) {
			}
		}
		
		try {
			ini = new Ini(iniFile);
			if (isWindows())
				ini.put("General", "mkvpropedit", "mkvpropedit.exe");
			else
				ini.put("General", "mkvpropedit", "/usr/bin/mkvpropedit");
			ini.store();
		}
		catch (InvalidFileFormatException e1) {
		}
		catch (IOException e1) {		
		}
	}
	
	private String getMkvPropExeFromReg() {
		String exePath = null;
		
		try {			
			exePath = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE,
					"Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MKVtoolnix",
					"UninstallString");
			
			if (exePath == null)
				exePath = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE,
						"Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MKVtoolnix",
						"UninstallString");
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		
		if (exePath != null) {
			File tmpExe = new File(exePath);
			tmpExe = new File(tmpExe.getParent()+"\\mkvpropedit.exe");
			if (tmpExe.exists()) {
				exePath = tmpExe.toString();
			}
		}
		
		return exePath;
	}
	
	/* End of INI configuration file operations */
	

	/* Start of escaping functions */
	
	private String escapeName (String name) {
		if (!name.isEmpty()) {
			name = name.replace("\\","\\\\");
			name = name.replace(" ", "\\s");
			name = name.replace("\"","\\2");
			name = name.replace(":","\\c");
			name = name.replace("#","\\h");
		}
		
		return name;
	}
	
	private String escapeNameCmdLine (String name) {
		name = name.replace("\"", "\\\"");
		
		return name;
	}
	
	private String escapePath (String path) {
		if (isWindows()) {
			path = path.replace("\\", "\\\\");
		} else {
			path = path.replace("\\", "\\\\");
			path = path.replace(" ", "\\ ");
			path = path.replace("[", "\\[");
			path = path.replace("]", "\\]");
			path = path.replace("{", "\\{");
			path = path.replace("}", "\\}");
			path = path.replace("<", "\\<");
			path = path.replace(">", "\\>");
			path = path.replace("\'", "\\\'");
			path = path.replace("\"", "\\\"");
			path = path.replace("&", "\\&");
			path = path.replace("*", "\\*");
			path = path.replace("?", "\\?");
			path = path.replace("|", "\\|");
			path = path.replace(":", "\\:");
			path = path.replace(";", "\\;");
		}
		
		return path;
	}
	
	/* End of escaping functions */
	
	
	private NumberFormat padNumber(int pad) {
		NumberFormat formatter = new DecimalFormat("0");
		if (pad > 0) {
			String n = "";
			for (int i = 0; i < pad; i++) {
				n += 0;
			}
			formatter = new DecimalFormat(n);
		}
		return formatter;
	}
	
	private boolean isWindows() {
		String OS = System.getProperty("os.name");
		
		if (OS.startsWith("Windows"))
			return true;
		else
			return false;
	}
}
