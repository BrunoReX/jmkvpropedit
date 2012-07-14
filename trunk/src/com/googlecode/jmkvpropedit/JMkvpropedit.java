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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.border.EmptyBorder;
import org.ini4j.*;

public class JMkvpropedit {
	
	private static final String VERSION_NUMBER = "1.2";
	private static final int MAX_STREAMS = 30;
	private static String[] argsArray;
	
	private static Process proc = null;
	private static ProcessBuilder pb = new ProcessBuilder();
	private static SwingWorker<Void, Void> worker = null;
	
	private static final File iniFile = new File("JMkvpropedit.ini");
	private static final MkvLanguage mkvLang = new MkvLanguage();
	
	private JFileChooser chooser = new JFileChooser(System.getProperty("user.home")) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public int showOpenDialog(Component parent) {
			super.setSelectedFile(new File(""));
			
			return super.showOpenDialog(parent);
		}
		
		@Override 
		public void approveSelection() {
			if (!super.isMultiSelectionEnabled() || super.getSelectedFiles().length == 1) {
	            if (!this.getSelectedFile().exists()) {
	                return;
	            }
			}
            
            super.approveSelection();
	    }
	};
	
	
	private static final FileFilter EXE_EXT_FILTER =
			new FileNameExtensionFilter("Excecutable files (*.exe)", "exe");
	
	private static final FileFilter MATROSKA_EXT_FILTER =
			new FileNameExtensionFilter("Matroska files (*.mkv; *.mka; *.mk3d) ", "mkv", "mka", "mk3d");
	
	private static final FileFilter TXT_EXT_FILTER =
			new FileNameExtensionFilter("Plain text files (*.txt)", "txt");	
	
	private static final FileFilter XML_EXT_FILTER =
			new FileNameExtensionFilter("XML files (*.xml)", "xml");
	
	
	private String[] cmdLineGeneral = null;
	private String[] cmdLineGeneralOpt = null;
	
	private String[] cmdLineVideo = null;
	private String[] cmdLineVideoOpt = null;
	private int nVideo = 0;
	
	private String[] cmdLineAudio = null;
	private String[] cmdLineAudioOpt = null;
	private int nAudio = 0;
	
	private String[] cmdLineSubtitle = null;
	private String[] cmdLineSubtitleOpt = null;
	private int nSubtitle = 0;
	
	private ArrayList<String> cmdLineBatch = null;
	private ArrayList<String> cmdLineBatchOpt = null;
	
	
	// Window controls
	private JFrame frmJMkvpropedit;
	private JTabbedPane pnlTabs;
	private JButton btnProcessFiles;
	private JButton btnGenerateCmdLine;

	
	// Input tab controls
	private DefaultListModel modelFiles;
	private JList listFiles;
	private JButton btnAddFiles;
	private JButton btnRemoveFiles;
	private JButton btnTopFiles;
	private JButton btnUpFiles;
	private JButton btnDownFiles;
	private JButton btnBottomFiles;
	private JButton btnClearFiles;
	
	
	// General tab controls
	private JCheckBox chbTitleGeneral;
	private JTextField txtTitleGeneral;
	private JCheckBox chbNumbGeneral;
	private JLabel lblNumbStartGeneral;
	private JTextField txtNumbStartGeneral;
	private JLabel lblNumbPadGeneral;
	private JTextField txtNumbPadGeneral;
	private JLabel lblNumbExplainGeneral;
	private JCheckBox chbChapters;
	private JComboBox cbChapters;
	private JButton btnBrowseChapters;
	private JComboBox cbExtChapters;
	private JTextField txtChapters;
	private JCheckBox chbTags;
	private JComboBox cbTags;
	private JTextField txtTags;
	private JButton btnBrowseTags;
	private JComboBox cbExtTags;
	private JCheckBox chbExtraCmdGeneral;
	private JTextField txtExtraCmdGeneral;
	private JTextField txtMkvPropExe;
	private JCheckBox chbMkvPropExeDef;

	
	// Video tab controls
	private JComboBox cbVideo;
	private JButton btnAddVideo;
	private JButton btnRemoveVideo;
	private CardLayout lytLyrdPnlVideo;
	private JPanel lyrdPnlVideo;
	
	private JPanel[] subPnlVideo = new JPanel[MAX_STREAMS];
	private JCheckBox[] chbEditVideo = new JCheckBox[MAX_STREAMS];
	private JCheckBox[] chbDefaultVideo = new JCheckBox[MAX_STREAMS];
	private JRadioButton[] rbYesDefVideo = new JRadioButton[MAX_STREAMS];
	private JRadioButton[] rbNoDefVideo = new JRadioButton[MAX_STREAMS];
	private ButtonGroup[] bgRbDefVideo = new ButtonGroup[MAX_STREAMS];
	private JCheckBox[] chbForcedVideo = new JCheckBox[MAX_STREAMS];
	private JRadioButton[] rbYesForcedVideo = new JRadioButton[MAX_STREAMS];
	private JRadioButton[] rbNoForcedVideo = new JRadioButton[MAX_STREAMS];
	private ButtonGroup[] bgRbForcedVideo = new ButtonGroup[MAX_STREAMS];
	private JCheckBox[] chbNameVideo = new JCheckBox[MAX_STREAMS];
	private JTextField[] txtNameVideo = new JTextField[MAX_STREAMS];
	private JCheckBox[] chbNumbVideo = new JCheckBox[MAX_STREAMS];
	private JLabel[] lblNumbStartVideo = new JLabel[MAX_STREAMS];
	private JTextField[] txtNumbStartVideo = new JTextField[MAX_STREAMS];
	private JLabel[] lblNumbPadVideo = new JLabel[MAX_STREAMS];
	private JTextField[] txtNumbPadVideo = new JTextField[MAX_STREAMS];
	private JLabel[] lblNumbExplainVideo = new JLabel[MAX_STREAMS];
	private JCheckBox[] chbLangVideo = new JCheckBox[MAX_STREAMS];
	private JComboBox[] cbLangVideo = new JComboBox[MAX_STREAMS];
	private JCheckBox[] chbExtraCmdVideo = new JCheckBox[MAX_STREAMS];
	private JTextField[] txtExtraCmdVideo = new JTextField[MAX_STREAMS];

	
	// Audio tab controls
	private JComboBox cbAudio;
	private JButton btnAddAudio;
	private JButton btnRemoveAudio;
	private CardLayout lytLyrdPnlAudio;
	private JPanel lyrdPnlAudio;
	
	private JPanel[] subPnlAudio = new JPanel[MAX_STREAMS];
	private JCheckBox[] chbEditAudio = new JCheckBox[MAX_STREAMS];
	private JCheckBox[] chbDefaultAudio = new JCheckBox[MAX_STREAMS];
	private JRadioButton[] rbYesDefAudio = new JRadioButton[MAX_STREAMS];
	private JRadioButton[] rbNoDefAudio = new JRadioButton[MAX_STREAMS];
	private ButtonGroup[] bgRbDefAudio = new ButtonGroup[MAX_STREAMS];
	private JCheckBox[] chbForcedAudio = new JCheckBox[MAX_STREAMS];
	private JRadioButton[] rbYesForcedAudio = new JRadioButton[MAX_STREAMS];
	private JRadioButton[] rbNoForcedAudio = new JRadioButton[MAX_STREAMS];
	private ButtonGroup[] bgRbForcedAudio = new ButtonGroup[MAX_STREAMS];
	private JCheckBox[] chbNameAudio = new JCheckBox[MAX_STREAMS];
	private JTextField[] txtNameAudio = new JTextField[MAX_STREAMS];
	private JCheckBox[] chbNumbAudio = new JCheckBox[MAX_STREAMS];
	private JLabel[] lblNumbStartAudio = new JLabel[MAX_STREAMS];
	private JTextField[] txtNumbStartAudio = new JTextField[MAX_STREAMS];
	private JLabel[] lblNumbPadAudio = new JLabel[MAX_STREAMS];
	private JTextField[] txtNumbPadAudio = new JTextField[MAX_STREAMS];
	private JLabel[] lblNumbExplainAudio = new JLabel[MAX_STREAMS];
	private JCheckBox[] chbLangAudio = new JCheckBox[MAX_STREAMS];
	private JComboBox[] cbLangAudio = new JComboBox[MAX_STREAMS];
	private JCheckBox[] chbExtraCmdAudio = new JCheckBox[MAX_STREAMS];
	private JTextField[] txtExtraCmdAudio = new JTextField[MAX_STREAMS];
	
	
	// Subtitle tab controls
	private JComboBox cbSubtitle;
	private AbstractButton btnAddSubtitle;
	private AbstractButton btnRemoveSubtitle;
	private CardLayout lytLyrdPnlSubtitle;
	private JPanel lyrdPnlSubtitle;
	
	private JPanel[] subPnlSubtitle = new JPanel[MAX_STREAMS];
	private JCheckBox[] chbEditSubtitle = new JCheckBox[MAX_STREAMS];
	private JCheckBox[] chbDefaultSubtitle = new JCheckBox[MAX_STREAMS];
	private JRadioButton[] rbYesDefSubtitle = new JRadioButton[MAX_STREAMS];
	private JRadioButton[] rbNoDefSubtitle = new JRadioButton[MAX_STREAMS];
	private ButtonGroup[] bgRbDefSubtitle = new ButtonGroup[MAX_STREAMS];
	private JCheckBox[] chbForcedSubtitle = new JCheckBox[MAX_STREAMS];
	private JRadioButton[] rbYesForcedSubtitle = new JRadioButton[MAX_STREAMS];
	private JRadioButton[] rbNoForcedSubtitle = new JRadioButton[MAX_STREAMS];
	private ButtonGroup[] bgRbForcedSubtitle = new ButtonGroup[MAX_STREAMS];
	private JCheckBox[] chbNameSubtitle = new JCheckBox[MAX_STREAMS];
	private JTextField[] txtNameSubtitle = new JTextField[MAX_STREAMS];
	private JCheckBox[] chbNumbSubtitle = new JCheckBox[MAX_STREAMS];
	private JLabel[] lblNumbStartSubtitle = new JLabel[MAX_STREAMS];
	private JTextField[] txtNumbStartSubtitle = new JTextField[MAX_STREAMS];
	private JLabel[] lblNumbPadSubtitle = new JLabel[MAX_STREAMS];
	private JTextField[] txtNumbPadSubtitle = new JTextField[MAX_STREAMS];
	private JLabel[] lblNumbExplainSubtitle = new JLabel[MAX_STREAMS];
	private JCheckBox[] chbLangSubtitle = new JCheckBox[MAX_STREAMS];
	private JComboBox[] cbLangSubtitle = new JComboBox[MAX_STREAMS];
	private JCheckBox[] chbExtraCmdSubtitle = new JCheckBox[MAX_STREAMS];
	private JTextField[] txtExtraCmdSubtitle = new JTextField[MAX_STREAMS];
	
	
	// Output tab controls
	private JTextArea txtOutput;

	
	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					argsArray = args;
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
		frmJMkvpropedit.setTitle("JMkvpropedit " + VERSION_NUMBER);
		frmJMkvpropedit.setBounds(100, 100, 760, 440);
		frmJMkvpropedit.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileHidingEnabled(true);
		chooser.setAcceptAllFileFilterUsed(false);
		
		pnlTabs = new JTabbedPane(JTabbedPane.TOP);
		pnlTabs.setBorder(new EmptyBorder(10, 10, 0, 10));
		frmJMkvpropedit.getContentPane().add(pnlTabs, BorderLayout.CENTER);
		
		JPanel pnlInput = new JPanel();
		pnlInput.setBorder(new EmptyBorder(10, 10, 10, 0));
		pnlTabs.addTab("Input", null, pnlInput, null);
		pnlInput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollFiles = new JScrollPane();
		scrollFiles.setViewportBorder(null);
		pnlInput.add(scrollFiles);
		
		modelFiles = new DefaultListModel();
		listFiles = new JList(modelFiles);
		scrollFiles.setViewportView(listFiles);
		
		JPanel pnlListToolbar = new JPanel();
		pnlListToolbar.setBorder(new EmptyBorder(0, 5, 0, 5));
		pnlInput.add(pnlListToolbar, BorderLayout.EAST);
		pnlListToolbar.setLayout(new BoxLayout(pnlListToolbar, BoxLayout.Y_AXIS));
		
		btnAddFiles = new JButton("");
		btnAddFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
		btnAddFiles.setMargin(new Insets(0, 0, 0, 0));
		btnAddFiles.setBorderPainted(false);
		btnAddFiles.setContentAreaFilled(false);
		btnAddFiles.setFocusPainted(false);
		btnAddFiles.setOpaque(false);
		pnlListToolbar.add(btnAddFiles);
		
		Component verticalStrut1 = Box.createVerticalStrut(10);
		pnlListToolbar.add(verticalStrut1);
		
		btnRemoveFiles = new JButton("");
		btnRemoveFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
		btnRemoveFiles.setMargin(new Insets(0, 0, 0, 0));
		btnRemoveFiles.setBorderPainted(false);
		btnRemoveFiles.setContentAreaFilled(false);
		btnRemoveFiles.setFocusPainted(false);
		btnRemoveFiles.setOpaque(false);
		pnlListToolbar.add(btnRemoveFiles);
		
		Component verticalStrut2 = Box.createVerticalStrut(10);
		pnlListToolbar.add(verticalStrut2);
		
		btnTopFiles = new JButton("");
		btnTopFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-top.png")));
		btnTopFiles.setMargin(new Insets(0, 0, 0, 0));
		btnTopFiles.setBorderPainted(false);
		btnTopFiles.setContentAreaFilled(false);
		btnTopFiles.setFocusPainted(false);
		btnTopFiles.setOpaque(false);
		pnlListToolbar.add(btnTopFiles);
		
		Component verticalStrut3 = Box.createVerticalStrut(10);
		pnlListToolbar.add(verticalStrut3);
		
		btnUpFiles = new JButton("");
		btnUpFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-up.png")));
		btnUpFiles.setMargin(new Insets(0, 0, 0, 0));
		btnUpFiles.setBorderPainted(false);
		btnUpFiles.setContentAreaFilled(false);
		btnUpFiles.setFocusPainted(false);
		btnUpFiles.setOpaque(false);
		pnlListToolbar.add(btnUpFiles);
		
		Component verticalStrut4 = Box.createVerticalStrut(10);
		pnlListToolbar.add(verticalStrut4);
		
		btnDownFiles = new JButton("");
		btnDownFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-down.png")));
		btnDownFiles.setMargin(new Insets(0, 0, 0, 0));
		btnDownFiles.setBorderPainted(false);
		btnDownFiles.setContentAreaFilled(false);
		btnDownFiles.setFocusPainted(false);
		btnDownFiles.setOpaque(false);
		pnlListToolbar.add(btnDownFiles);
		
		Component verticalStrut5 = Box.createVerticalStrut(10);
		pnlListToolbar.add(verticalStrut5);
		
		btnBottomFiles = new JButton("");
		btnBottomFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/go-bottom.png")));
		btnBottomFiles.setMargin(new Insets(0, 0, 0, 0));
		btnBottomFiles.setBorderPainted(false);
		btnBottomFiles.setContentAreaFilled(false);
		btnBottomFiles.setFocusPainted(false);
		btnBottomFiles.setOpaque(false);
		pnlListToolbar.add(btnBottomFiles);
		
		Component verticalStrut6 = Box.createVerticalStrut(10);
		pnlListToolbar.add(verticalStrut6);
		
		btnClearFiles = new JButton("");
		btnClearFiles.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/edit-clear.png")));
		btnClearFiles.setMargin(new Insets(0, 0, 0, 0));
		btnClearFiles.setBorderPainted(false);
		btnClearFiles.setContentAreaFilled(false);
		btnClearFiles.setFocusPainted(false);
		btnClearFiles.setOpaque(false);
		pnlListToolbar.add(btnClearFiles);
		
		JPanel pnlGeneral = new JPanel();
		pnlGeneral.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlTabs.addTab("General", null, pnlGeneral, null);
		GridBagLayout gbl_pnlGeneral = new GridBagLayout();
		gbl_pnlGeneral.columnWidths = new int[]{75, 655, 0};
		gbl_pnlGeneral.rowHeights = new int[]{0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlGeneral.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlGeneral.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlGeneral.setLayout(gbl_pnlGeneral);
		
		chbTitleGeneral = new JCheckBox("Title:");
		GridBagConstraints gbc_chbTitleGeneral = new GridBagConstraints();
		gbc_chbTitleGeneral.insets = new Insets(0, 0, 5, 5);
		gbc_chbTitleGeneral.anchor = GridBagConstraints.WEST;
		gbc_chbTitleGeneral.gridx = 0;
		gbc_chbTitleGeneral.gridy = 0;
		pnlGeneral.add(chbTitleGeneral, gbc_chbTitleGeneral);
		
		txtTitleGeneral = new JTextField();
		txtTitleGeneral.setEnabled(false);
		GridBagConstraints gbc_txtTitleGeneral = new GridBagConstraints();
		gbc_txtTitleGeneral.insets = new Insets(0, 0, 5, 0);
		gbc_txtTitleGeneral.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTitleGeneral.gridx = 1;
		gbc_txtTitleGeneral.gridy = 0;
		pnlGeneral.add(txtTitleGeneral, gbc_txtTitleGeneral);
		txtTitleGeneral.setColumns(10);
		
		JPanel pnlNumbControlsGeneral = new JPanel();
		FlowLayout fl_pnlNumbControlsGeneral = (FlowLayout) pnlNumbControlsGeneral.getLayout();
		fl_pnlNumbControlsGeneral.setVgap(0);
		fl_pnlNumbControlsGeneral.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_pnlNumbControlsGeneral = new GridBagConstraints();
		gbc_pnlNumbControlsGeneral.insets = new Insets(0, 0, 5, 0);
		gbc_pnlNumbControlsGeneral.fill = GridBagConstraints.BOTH;
		gbc_pnlNumbControlsGeneral.gridx = 1;
		gbc_pnlNumbControlsGeneral.gridy = 1;
		pnlGeneral.add(pnlNumbControlsGeneral, gbc_pnlNumbControlsGeneral);
		
		chbNumbGeneral = new JCheckBox("Numbering:");
		chbNumbGeneral.setEnabled(false);
		pnlNumbControlsGeneral.add(chbNumbGeneral);
		
		Component horizontalStrut1 = Box.createHorizontalStrut(10);
		pnlNumbControlsGeneral.add(horizontalStrut1);
		
		lblNumbStartGeneral = new JLabel("Start");
		lblNumbStartGeneral.setEnabled(false);
		pnlNumbControlsGeneral.add(lblNumbStartGeneral);
		
		txtNumbStartGeneral = new JTextField();
		txtNumbStartGeneral.setEnabled(false);
		txtNumbStartGeneral.setText("1");
		pnlNumbControlsGeneral.add(txtNumbStartGeneral);
		txtNumbStartGeneral.setColumns(10);
		
		Component horizontalStrut2 = Box.createHorizontalStrut(5);
		pnlNumbControlsGeneral.add(horizontalStrut2);
		
		lblNumbPadGeneral = new JLabel("Padding");
		lblNumbPadGeneral.setEnabled(false);
		pnlNumbControlsGeneral.add(lblNumbPadGeneral);
		
		txtNumbPadGeneral = new JTextField();
		txtNumbPadGeneral.setEnabled(false);
		txtNumbPadGeneral.setText("1");
		txtNumbPadGeneral.setColumns(10);
		pnlNumbControlsGeneral.add(txtNumbPadGeneral);
		
		lblNumbExplainGeneral = new JLabel("      To use it, add {num} to the title (e.g. \"My Title {num}\")");
		lblNumbExplainGeneral.setEnabled(false);
		GridBagConstraints gbc_lblNumbExplainGeneral = new GridBagConstraints();
		gbc_lblNumbExplainGeneral.insets = new Insets(0, 0, 10, 0);
		gbc_lblNumbExplainGeneral.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNumbExplainGeneral.gridx = 1;
		gbc_lblNumbExplainGeneral.gridy = 2;
		pnlGeneral.add(lblNumbExplainGeneral, gbc_lblNumbExplainGeneral);
		
		chbChapters = new JCheckBox("Chapters:");
		GridBagConstraints gbc_chbChapters = new GridBagConstraints();
		gbc_chbChapters.anchor = GridBagConstraints.WEST;
		gbc_chbChapters.insets = new Insets(0, 0, 5, 5);
		gbc_chbChapters.gridx = 0;
		gbc_chbChapters.gridy = 3;
		pnlGeneral.add(chbChapters, gbc_chbChapters);
		
		cbChapters = new JComboBox();
		cbChapters.setEnabled(false);
		cbChapters.setModel(new DefaultComboBoxModel(new String[] {"Remove", "From file:", "Match file name with suffix:"}));
		cbChapters.setPrototypeDisplayValue("Match file name with suffix:  ");
		GridBagConstraints gbc_cbChapters = new GridBagConstraints();
		gbc_cbChapters.insets = new Insets(0, 0, 5, 0);
		gbc_cbChapters.anchor = GridBagConstraints.WEST;
		gbc_cbChapters.gridx = 1;
		gbc_cbChapters.gridy = 3;
		pnlGeneral.add(cbChapters, gbc_cbChapters);
		
		Component verticalStrut7 = Box.createVerticalStrut(35);
		GridBagConstraints gbc_verticalStrut7 = new GridBagConstraints();
		gbc_verticalStrut7.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut7.gridx = 0;
		gbc_verticalStrut7.gridy = 4;
		pnlGeneral.add(verticalStrut7, gbc_verticalStrut7);
		
		JPanel pnlChapControlsGeneral = new JPanel();
		GridBagConstraints gbc_pnlChapControlsGeneral = new GridBagConstraints();
		gbc_pnlChapControlsGeneral.insets = new Insets(0, 0, 5, 0);
		gbc_pnlChapControlsGeneral.fill = GridBagConstraints.BOTH;
		gbc_pnlChapControlsGeneral.gridx = 1;
		gbc_pnlChapControlsGeneral.gridy = 4;
		pnlGeneral.add(pnlChapControlsGeneral, gbc_pnlChapControlsGeneral);
		GridBagLayout gbl_pnlChapControlsGeneral = new GridBagLayout();
		gbl_pnlChapControlsGeneral.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlChapControlsGeneral.rowHeights = new int[]{0, 0};
		gbl_pnlChapControlsGeneral.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlChapControlsGeneral.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlChapControlsGeneral.setLayout(gbl_pnlChapControlsGeneral);
		
		txtChapters = new JTextField();
		txtChapters.setVisible(false);
		GridBagConstraints gbc_txtChapters = new GridBagConstraints();
		gbc_txtChapters.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtChapters.insets = new Insets(0, 0, 8, 5);
		gbc_txtChapters.gridx = 0;
		gbc_txtChapters.gridy = 0;
		pnlChapControlsGeneral.add(txtChapters, gbc_txtChapters);
		txtChapters.setColumns(10);
		
		btnBrowseChapters = new JButton("Browse...");
		btnBrowseChapters.setVisible(false);
		GridBagConstraints gbc_btnBrowseChapters = new GridBagConstraints();
		gbc_btnBrowseChapters.insets = new Insets(0, 5, 10, 5);
		gbc_btnBrowseChapters.anchor = GridBagConstraints.EAST;
		gbc_btnBrowseChapters.gridx = 1;
		gbc_btnBrowseChapters.gridy = 0;
		pnlChapControlsGeneral.add(btnBrowseChapters, gbc_btnBrowseChapters);
		
		cbExtChapters = new JComboBox();
		cbExtChapters.setVisible(false);
		cbExtChapters.setModel(new DefaultComboBoxModel(new String[] {".xml", ".txt"}));
		GridBagConstraints gbc_cbExtChapters = new GridBagConstraints();
		gbc_cbExtChapters.insets = new Insets(0, 0, 8, 0);
		gbc_cbExtChapters.gridx = 2;
		gbc_cbExtChapters.gridy = 0;
		pnlChapControlsGeneral.add(cbExtChapters, gbc_cbExtChapters);
		
		chbTags = new JCheckBox("Tags:");
		GridBagConstraints gbc_chbTags = new GridBagConstraints();
		gbc_chbTags.anchor = GridBagConstraints.WEST;
		gbc_chbTags.insets = new Insets(0, 0, 5, 5);
		gbc_chbTags.gridx = 0;
		gbc_chbTags.gridy = 5;
		pnlGeneral.add(chbTags, gbc_chbTags);
		
		cbTags = new JComboBox();
		cbTags.setEnabled(false);
		cbTags.setModel(new DefaultComboBoxModel(new String[] {"Remove", "From file:", "Match file name with suffix:"}));
		cbTags.setPrototypeDisplayValue("Match file name with suffix:  ");
		GridBagConstraints gbc_cbTags = new GridBagConstraints();
		gbc_cbTags.insets = new Insets(0, 0, 5, 0);
		gbc_cbTags.anchor = GridBagConstraints.WEST;
		gbc_cbTags.gridx = 1;
		gbc_cbTags.gridy = 5;
		pnlGeneral.add(cbTags, gbc_cbTags);
		
		Component verticalStrut8 = Box.createVerticalStrut(35);
		GridBagConstraints gbc_verticalStrut8 = new GridBagConstraints();
		gbc_verticalStrut8.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut8.gridx = 0;
		gbc_verticalStrut8.gridy = 6;
		pnlGeneral.add(verticalStrut8, gbc_verticalStrut8);
		
		JPanel pnlTagControlsGeneral = new JPanel();
		GridBagConstraints gbc_pnlTagControlsGeneral = new GridBagConstraints();
		gbc_pnlTagControlsGeneral.insets = new Insets(0, 0, 5, 0);
		gbc_pnlTagControlsGeneral.fill = GridBagConstraints.BOTH;
		gbc_pnlTagControlsGeneral.gridx = 1;
		gbc_pnlTagControlsGeneral.gridy = 6;
		pnlGeneral.add(pnlTagControlsGeneral, gbc_pnlTagControlsGeneral);
		GridBagLayout gbl_pnlTagControlsGeneral = new GridBagLayout();
		gbl_pnlTagControlsGeneral.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlTagControlsGeneral.rowHeights = new int[]{0, 0};
		gbl_pnlTagControlsGeneral.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlTagControlsGeneral.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlTagControlsGeneral.setLayout(gbl_pnlTagControlsGeneral);
		
		txtTags = new JTextField();
		txtTags.setVisible(false);
		txtTags.setColumns(10);
		GridBagConstraints gbc_txtTags = new GridBagConstraints();
		gbc_txtTags.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTags.insets = new Insets(0, 0, 8, 5);
		gbc_txtTags.gridx = 0;
		gbc_txtTags.gridy = 0;
		pnlTagControlsGeneral.add(txtTags, gbc_txtTags);
		
		btnBrowseTags = new JButton("Browse...");
		btnBrowseTags.setVisible(false);
		GridBagConstraints gbc_btnBrowseTags = new GridBagConstraints();
		gbc_btnBrowseTags.insets = new Insets(0, 5, 10, 5);
		gbc_btnBrowseTags.anchor = GridBagConstraints.EAST;
		gbc_btnBrowseTags.gridx = 1;
		gbc_btnBrowseTags.gridy = 0;
		pnlTagControlsGeneral.add(btnBrowseTags, gbc_btnBrowseTags);
		
		cbExtTags = new JComboBox();
		cbExtTags.setVisible(false);
		cbExtTags.setModel(new DefaultComboBoxModel(new String[] {".xml", ".txt"}));
		GridBagConstraints gbc_cbExtTags = new GridBagConstraints();
		gbc_cbExtTags.insets = new Insets(0, 0, 8, 0);
		gbc_cbExtTags.gridx = 2;
		gbc_cbExtTags.gridy = 0;
		pnlTagControlsGeneral.add(cbExtTags, gbc_cbExtTags);
		
		chbExtraCmdGeneral = new JCheckBox("Extra parameters:");
		GridBagConstraints gbc_chbExtraCmdGeneral = new GridBagConstraints();
		gbc_chbExtraCmdGeneral.anchor = GridBagConstraints.WEST;
		gbc_chbExtraCmdGeneral.insets = new Insets(0, 0, 5, 5);
		gbc_chbExtraCmdGeneral.gridx = 0;
		gbc_chbExtraCmdGeneral.gridy = 7;
		pnlGeneral.add(chbExtraCmdGeneral, gbc_chbExtraCmdGeneral);
		
		txtExtraCmdGeneral = new JTextField();
		txtExtraCmdGeneral.setEnabled(false);
		GridBagConstraints gbc_txtExtraCmdGeneral = new GridBagConstraints();
		gbc_txtExtraCmdGeneral.insets = new Insets(0, 0, 5, 0);
		gbc_txtExtraCmdGeneral.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtExtraCmdGeneral.gridx = 1;
		gbc_txtExtraCmdGeneral.gridy = 7;
		pnlGeneral.add(txtExtraCmdGeneral, gbc_txtExtraCmdGeneral);
		txtExtraCmdGeneral.setColumns(10);
		
		Component verticalGlue = Box.createVerticalGlue();
		GridBagConstraints gbc_verticalGlue = new GridBagConstraints();
		gbc_verticalGlue.insets = new Insets(0, 0, 20, 0);
		gbc_verticalGlue.gridx = 1;
		gbc_verticalGlue.gridy = 8;
		pnlGeneral.add(verticalGlue, gbc_verticalGlue);
		
		JLabel lblMkvPropExe = new JLabel("Mkvpropedit executable:");
		GridBagConstraints gbc_lblMkvPropExe = new GridBagConstraints();
		gbc_lblMkvPropExe.anchor = GridBagConstraints.WEST;
		gbc_lblMkvPropExe.insets = new Insets(0, 0, 5, 5);
		gbc_lblMkvPropExe.gridx = 0;
		gbc_lblMkvPropExe.gridy = 9;
		pnlGeneral.add(lblMkvPropExe, gbc_lblMkvPropExe);
		
		txtMkvPropExe = new JTextField("mkvpropedit");
		txtMkvPropExe.setEditable(false);
		GridBagConstraints gbc_txtMkvPropExe = new GridBagConstraints();
		gbc_txtMkvPropExe.insets = new Insets(0, 0, 5, 0);
		gbc_txtMkvPropExe.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMkvPropExe.gridx = 1;
		gbc_txtMkvPropExe.gridy = 9;
		pnlGeneral.add(txtMkvPropExe, gbc_txtMkvPropExe);
		txtMkvPropExe.setColumns(10);
		
		JPanel pnlMkvPropExeControls = new JPanel();
		GridBagConstraints gbc_pnlMkvPropExeControls = new GridBagConstraints();
		gbc_pnlMkvPropExeControls.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlMkvPropExeControls.gridx = 1;
		gbc_pnlMkvPropExeControls.gridy = 10;
		pnlGeneral.add(pnlMkvPropExeControls, gbc_pnlMkvPropExeControls);
		GridBagLayout gbl_pnlMkvPropExeControls = new GridBagLayout();
		gbl_pnlMkvPropExeControls.columnWidths = new int[]{0, 0, 0};
		gbl_pnlMkvPropExeControls.rowHeights = new int[]{0, 0};
		gbl_pnlMkvPropExeControls.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlMkvPropExeControls.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlMkvPropExeControls.setLayout(gbl_pnlMkvPropExeControls);
		
		chbMkvPropExeDef = new JCheckBox("Use default");
		chbMkvPropExeDef.setSelected(true);
		chbMkvPropExeDef.setEnabled(false);
		GridBagConstraints gbc_chckbxUseDefault = new GridBagConstraints();
		gbc_chckbxUseDefault.anchor = GridBagConstraints.WEST;
		gbc_chckbxUseDefault.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxUseDefault.gridx = 0;
		gbc_chckbxUseDefault.gridy = 0;
		pnlMkvPropExeControls.add(chbMkvPropExeDef, gbc_chckbxUseDefault);
		
		JButton btnBrowseMkvPropExe = new JButton("Browse...");
		GridBagConstraints gbc_btnBrowseMkvPropExe = new GridBagConstraints();
		gbc_btnBrowseMkvPropExe.gridx = 1;
		gbc_btnBrowseMkvPropExe.gridy = 0;
		pnlMkvPropExeControls.add(btnBrowseMkvPropExe, gbc_btnBrowseMkvPropExe);
		
		JPanel pnlVideo = new JPanel();
		pnlVideo.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlTabs.addTab("Video", null, pnlVideo, null);
		GridBagLayout gbl_pnlVideo = new GridBagLayout();
		gbl_pnlVideo.columnWidths = new int[]{705, 0};
		gbl_pnlVideo.rowHeights = new int[]{30, 283, 0};
		gbl_pnlVideo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlVideo.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlVideo.setLayout(gbl_pnlVideo);
		
		JPanel pnlControlsVideo = new JPanel();
		GridBagConstraints gbc_pnlControlsVideo = new GridBagConstraints();
		gbc_pnlControlsVideo.insets = new Insets(0, 0, 5, 0);
		gbc_pnlControlsVideo.fill = GridBagConstraints.BOTH;
		gbc_pnlControlsVideo.gridx = 0;
		gbc_pnlControlsVideo.gridy = 0;
		pnlVideo.add(pnlControlsVideo, gbc_pnlControlsVideo);
		pnlControlsVideo.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		cbVideo = new JComboBox();
		pnlControlsVideo.add(cbVideo);
		
		btnAddVideo = new JButton("");
		btnAddVideo.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
		btnAddVideo.setMargin(new Insets(0, 5, 0, 5));
		btnAddVideo.setBorderPainted(false);
		btnAddVideo.setContentAreaFilled(false);
		btnAddVideo.setFocusPainted(false);
		btnAddVideo.setOpaque(false);
		pnlControlsVideo.add(btnAddVideo);
		
		btnRemoveVideo = new JButton("");
		btnRemoveVideo.setEnabled(false);
		btnRemoveVideo.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
		btnRemoveVideo.setMargin(new Insets(0, 0, 0, 0));
		btnRemoveVideo.setBorderPainted(false);
		btnRemoveVideo.setContentAreaFilled(false);
		btnRemoveVideo.setFocusPainted(false);
		btnRemoveVideo.setOpaque(false);
		pnlControlsVideo.add(btnRemoveVideo);
		
		lyrdPnlVideo = new JPanel();
		GridBagConstraints gbc_lyrdPnlVideo = new GridBagConstraints();
		gbc_lyrdPnlVideo.fill = GridBagConstraints.BOTH;
		gbc_lyrdPnlVideo.gridx = 0;
		gbc_lyrdPnlVideo.gridy = 1;
		pnlVideo.add(lyrdPnlVideo, gbc_lyrdPnlVideo);
		lytLyrdPnlVideo = new CardLayout(0, 0);
		lyrdPnlVideo.setLayout(lytLyrdPnlVideo);
		
		JPanel pnlAudio = new JPanel();
		pnlAudio.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlTabs.addTab("Audio", null, pnlAudio, null);
		GridBagLayout gbl_pnlAudio = new GridBagLayout();
		gbl_pnlAudio.columnWidths = new int[]{705, 0};
		gbl_pnlAudio.rowHeights = new int[]{30, 283, 0};
		gbl_pnlAudio.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlAudio.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlAudio.setLayout(gbl_pnlAudio);
		
		JPanel pnlControlsAudio = new JPanel();
		GridBagConstraints gbc_pnlControlsAudio = new GridBagConstraints();
		gbc_pnlControlsAudio.insets = new Insets(0, 0, 5, 0);
		gbc_pnlControlsAudio.fill = GridBagConstraints.BOTH;
		gbc_pnlControlsAudio.gridx = 0;
		gbc_pnlControlsAudio.gridy = 0;
		pnlAudio.add(pnlControlsAudio, gbc_pnlControlsAudio);
		pnlControlsAudio.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		cbAudio = new JComboBox();
		pnlControlsAudio.add(cbAudio);
		
		btnAddAudio = new JButton("");
		btnAddAudio.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
		btnAddAudio.setMargin(new Insets(0, 5, 0, 5));
		btnAddAudio.setBorderPainted(false);
		btnAddAudio.setContentAreaFilled(false);
		btnAddAudio.setFocusPainted(false);
		btnAddAudio.setOpaque(false);
		pnlControlsAudio.add(btnAddAudio);
		
		btnRemoveAudio = new JButton("");
		btnRemoveAudio.setEnabled(false);
		btnRemoveAudio.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
		btnRemoveAudio.setMargin(new Insets(0, 0, 0, 0));
		btnRemoveAudio.setBorderPainted(false);
		btnRemoveAudio.setContentAreaFilled(false);
		btnRemoveAudio.setFocusPainted(false);
		btnRemoveAudio.setOpaque(false);
		pnlControlsAudio.add(btnRemoveAudio);
		
		lyrdPnlAudio = new JPanel();
		GridBagConstraints gbc_lyrdPnlAudio = new GridBagConstraints();
		gbc_lyrdPnlAudio.fill = GridBagConstraints.BOTH;
		gbc_lyrdPnlAudio.gridx = 0;
		gbc_lyrdPnlAudio.gridy = 1;
		pnlAudio.add(lyrdPnlAudio, gbc_lyrdPnlAudio);
		lytLyrdPnlAudio = new CardLayout(0, 0);
		lyrdPnlAudio.setLayout(lytLyrdPnlAudio);
		
		JPanel pnlSubtitle = new JPanel();
		pnlSubtitle.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlTabs.addTab("Subtitle", null, pnlSubtitle, null);
		GridBagLayout gbl_pnlSubtitle = new GridBagLayout();
		gbl_pnlSubtitle.columnWidths = new int[]{705, 0};
		gbl_pnlSubtitle.rowHeights = new int[]{30, 283, 0};
		gbl_pnlSubtitle.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlSubtitle.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlSubtitle.setLayout(gbl_pnlSubtitle);
		
		JPanel pnlControlsSubtitle = new JPanel();
		GridBagConstraints gbc_pnlControlsSubtitle = new GridBagConstraints();
		gbc_pnlControlsSubtitle.insets = new Insets(0, 0, 5, 0);
		gbc_pnlControlsSubtitle.fill = GridBagConstraints.BOTH;
		gbc_pnlControlsSubtitle.gridx = 0;
		gbc_pnlControlsSubtitle.gridy = 0;
		pnlSubtitle.add(pnlControlsSubtitle, gbc_pnlControlsSubtitle);
		pnlControlsSubtitle.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		cbSubtitle = new JComboBox();
		pnlControlsSubtitle.add(cbSubtitle);
		
		btnAddSubtitle = new JButton("");
		btnAddSubtitle.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-add.png")));
		btnAddSubtitle.setMargin(new Insets(0, 5, 0, 5));
		btnAddSubtitle.setBorderPainted(false);
		btnAddSubtitle.setContentAreaFilled(false);
		btnAddSubtitle.setFocusPainted(false);
		btnAddSubtitle.setOpaque(false);
		pnlControlsSubtitle.add(btnAddSubtitle);
		
		btnRemoveSubtitle = new JButton("");
		btnRemoveSubtitle.setEnabled(false);
		btnRemoveSubtitle.setIcon(new ImageIcon(JMkvpropedit.class.getResource("/res/list-remove.png")));
		btnRemoveSubtitle.setMargin(new Insets(0, 0, 0, 0));
		btnRemoveSubtitle.setBorderPainted(false);
		btnRemoveSubtitle.setContentAreaFilled(false);
		btnRemoveSubtitle.setFocusPainted(false);
		btnRemoveSubtitle.setOpaque(false);
		pnlControlsSubtitle.add(btnRemoveSubtitle);
		
		lyrdPnlSubtitle = new JPanel();
		GridBagConstraints gbc_lyrdPnlSubtitle = new GridBagConstraints();
		gbc_lyrdPnlSubtitle.fill = GridBagConstraints.BOTH;
		gbc_lyrdPnlSubtitle.gridx = 0;
		gbc_lyrdPnlSubtitle.gridy = 1;
		pnlSubtitle.add(lyrdPnlSubtitle, gbc_lyrdPnlSubtitle);
		lytLyrdPnlSubtitle = new CardLayout(0, 0);
		lyrdPnlSubtitle.setLayout(lytLyrdPnlSubtitle);
		
		JPanel pnlOutput = new JPanel();
		pnlOutput.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlTabs.addTab("Output", null, pnlOutput, null);
		pnlOutput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrPnlOutput = new JScrollPane();
		pnlOutput.add(scrPnlOutput, BorderLayout.CENTER);
		
		txtOutput = new JTextArea();
		txtOutput.setLineWrap(true);
		txtOutput.setEditable(false);
		scrPnlOutput.setViewportView(txtOutput);
		
		JPanel pnlButtons = new JPanel();
		frmJMkvpropedit.getContentPane().add(pnlButtons, BorderLayout.SOUTH);
		
		btnProcessFiles = new JButton("Process files");
		pnlButtons.add(btnProcessFiles);
		
		btnGenerateCmdLine = new JButton("Generate command line");
		pnlButtons.add(btnGenerateCmdLine);

		
		/* Start of mouse events for right-click menu */

		Utils.addRCMenuMouseListener(txtTitleGeneral);
		Utils.addRCMenuMouseListener(txtNumbStartGeneral);
		Utils.addRCMenuMouseListener(txtNumbPadGeneral);
		Utils.addRCMenuMouseListener(txtChapters);
		Utils.addRCMenuMouseListener(txtTags);
		Utils.addRCMenuMouseListener(txtExtraCmdGeneral);
		Utils.addRCMenuMouseListener(txtMkvPropExe);
		Utils.addRCMenuMouseListener(txtOutput);
		
		/* End of mouse events for right-click menu */
		
		
		frmJMkvpropedit.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				// Resize the window to make sure the components fit
				frmJMkvpropedit.pack();
				
				// Don't allow the window to be resized to a dimension smaller than the original
				frmJMkvpropedit.setMinimumSize(new Dimension(frmJMkvpropedit.getWidth(), frmJMkvpropedit.getHeight()));
				
				// Center the window on the screen
				frmJMkvpropedit.setLocationRelativeTo(null);
				
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
							"Do you really want to exit?",
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
		
		new FileDrop(listFiles, new FileDrop.Listener() {
        	public void filesDropped(java.io.File[] files) {
        		for (int i = 0; i < files.length; i++) {
        			try {
        				if (!modelFiles.contains(files[i].getCanonicalPath()) &&
        					MATROSKA_EXT_FILTER.accept(files[i]) && !files[i].isDirectory()) {
        					modelFiles.add(modelFiles.getSize(), files[i].getCanonicalPath());
        				}
        			} catch(java.io.IOException e) {
        			}
        		}
        	}
        });
		
		btnAddFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File[] files = null;
				
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select Matroska file(s) to edit");
				chooser.setMultiSelectionEnabled(true);
				chooser.resetChoosableFileFilters();
				chooser.setFileFilter(MATROSKA_EXT_FILTER);
				
				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					files = chooser.getSelectedFiles();
					for (int i = 0; i < files.length; i++) {
							try {
								if (!modelFiles.contains(files[i].getCanonicalPath()) && files[i].exists()) {
									modelFiles.add(modelFiles.getSize(), files[i].getCanonicalPath());
								}
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
					
					if (pos < modelFiles.getSize()) {
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
					chbNumbGeneral.setEnabled(!state);
					
					if (chbNumbGeneral.isSelected()) {
						lblNumbStartGeneral.setEnabled(!state);
						txtNumbStartGeneral.setEnabled(!state);
						lblNumbPadGeneral.setEnabled(!state);
						txtNumbPadGeneral.setEnabled(!state);
						lblNumbExplainGeneral.setEnabled(!state);
					}
				}
			}
		});
		
		chbNumbGeneral.addActionListener(new ActionListener() {
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
					if (Integer.parseInt(txtNumbStartGeneral.getText()) < 0) {
						txtNumbStartGeneral.setText("1");
					}
				} catch (NumberFormatException e1) {
					txtNumbStartGeneral.setText("1");
				}
			}
		});
		
		txtNumbPadGeneral.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					if (Integer.parseInt(txtNumbPadGeneral.getText()) < 0) {
						txtNumbPadGeneral.setText("1");
					}
				} catch (NumberFormatException e1) {
					txtNumbPadGeneral.setText("1");
				}
			}
		});
		
		chbChapters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean state = cbChapters.isEnabled();
				cbChapters.setEnabled(!state);
				
				if (cbChapters.getSelectedIndex() == 1) {
					txtChapters.setEditable(false);
					txtChapters.setVisible(true);
					txtChapters.setEnabled(!state);
					btnBrowseChapters.setVisible(true);
					btnBrowseChapters.setEnabled(!state);
					cbExtChapters.setVisible(false);
				} else if (cbChapters.getSelectedIndex() == 2) {
					txtChapters.setEditable(true);
					txtChapters.setVisible(true);
					txtChapters.setEnabled(!state);
					btnBrowseChapters.setVisible(false);
					btnBrowseChapters.setEnabled(!state);
					cbExtChapters.setVisible(true);
					cbExtChapters.setEnabled(!state);
				} else if (!chbChapters.isSelected()) {
					txtChapters.setVisible(false);
					btnBrowseChapters.setVisible(false);
					cbExtChapters.setVisible(false);
				}
			}
		});
		
		cbChapters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				if (cbChapters.getSelectedIndex() == 0) {
					txtChapters.setVisible(false);
					btnBrowseChapters.setVisible(false);
					cbExtChapters.setVisible(false);
				} else if (cbChapters.getSelectedIndex() == 1) {
					txtChapters.setText("");
					txtChapters.setEditable(false);
					txtChapters.setVisible(true);
					btnBrowseChapters.setVisible(true);
					cbExtChapters.setVisible(false);
				} else {
					txtChapters.setText("-chapters");
					txtChapters.setEditable(true);
					txtChapters.setVisible(true);
					btnBrowseChapters.setVisible(false);
					cbExtChapters.setVisible(true);					
				}
			}
		});
		
		btnBrowseChapters.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select chapters file");
				chooser.setMultiSelectionEnabled(false);
				chooser.resetChoosableFileFilters();
				chooser.setFileFilter(TXT_EXT_FILTER);
				chooser.setFileFilter(XML_EXT_FILTER);
				
				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile().exists()) {
						txtChapters.setText(chooser.getSelectedFile().toString());
					}
				}
			}
		});
		
		chbTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean state = cbTags.isEnabled();
				cbTags.setEnabled(!state);
				
				if (cbTags.getSelectedIndex() == 1) {
					txtTags.setEditable(false);
					txtTags.setVisible(true);
					txtTags.setEnabled(!state);
					btnBrowseTags.setVisible(true);
					btnBrowseTags.setEnabled(!state);
					cbExtTags.setVisible(false);
				} else if (cbTags.getSelectedIndex() == 2) {
					txtTags.setEditable(true);
					txtTags.setVisible(true);
					txtTags.setEnabled(!state);
					btnBrowseTags.setVisible(false);
					btnBrowseTags.setEnabled(!state);
					cbExtTags.setVisible(true);
					cbExtTags.setEnabled(!state);
				} else if (!chbTags.isSelected()) {
					txtTags.setVisible(false);
					btnBrowseTags.setVisible(false);
					cbExtTags.setVisible(false);
				}
			}
		});
		
		cbTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				if (cbTags.getSelectedIndex() == 0) {
					txtTags.setVisible(false);
					btnBrowseTags.setVisible(false);
					cbExtTags.setVisible(false);
				} else if (cbTags.getSelectedIndex() == 1) {
					txtTags.setText("");
					txtTags.setEditable(false);
					txtTags.setVisible(true);
					btnBrowseTags.setVisible(true);
					cbExtTags.setVisible(false);
				} else {
					txtTags.setText("-tags");
					txtTags.setEditable(true);
					txtTags.setVisible(true);
					btnBrowseTags.setVisible(false);
					cbExtTags.setVisible(true);					
				}
			}
		});
		
		btnBrowseTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select tags file");
				chooser.setMultiSelectionEnabled(false);
				chooser.resetChoosableFileFilters();
				chooser.setFileFilter(TXT_EXT_FILTER);
				chooser.setFileFilter(XML_EXT_FILTER);
				
				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile().exists()) {
						txtTags.setText(chooser.getSelectedFile().toString());
					}
				}
			}
		});		
		
		chbExtraCmdGeneral.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean state = txtExtraCmdGeneral.isEnabled();
				txtExtraCmdGeneral.setEnabled(!state);
			}
		});

		chbMkvPropExeDef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtMkvPropExe.setText("mkvpropedit");
				chbMkvPropExeDef.setEnabled(false);
				defaultIniFile();
			}
		});
		
		btnBrowseMkvPropExe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select mkvpropedit executable");
				chooser.setMultiSelectionEnabled(false);
				chooser.resetChoosableFileFilters();
				
				if (Utils.isWindows()) {
					chooser.setFileFilter(EXE_EXT_FILTER);
				}
				
				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile().exists()) {
						saveIniFile(chooser.getSelectedFile());
					}
				}
			}
		});
		
		cbVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lytLyrdPnlVideo.show(lyrdPnlVideo, "subPnlVideo[" + cbVideo.getSelectedIndex() + "]");
			}
		});
		
		
		btnAddVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				addVideoTrack();
				
				cbVideo.setSelectedIndex(cbVideo.getItemCount()-1);
				if (cbVideo.getItemCount() == MAX_STREAMS) {
					btnAddVideo.setEnabled(false);
				}
				
				if (!btnRemoveVideo.isEnabled()) {
					btnRemoveVideo.setEnabled(true);
				}
			}
			
		});
		
		btnRemoveVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cbVideo.getSelectedIndex() > 0) {
					int idx = cbVideo.getItemCount()-1;
					
					cbVideo.removeItemAt(idx);
					lyrdPnlVideo.remove(idx);				
					nVideo--;
				}
				
				if (cbVideo.getItemCount() < MAX_STREAMS && !btnAddVideo.isEnabled()) {
					btnAddVideo.setEnabled(true);
				}
				
				if (cbVideo.getItemCount() == 1) {
					btnRemoveVideo.setEnabled(false);
				}
				
				System.gc();
			}
		});
		
		
		cbAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lytLyrdPnlAudio.show(lyrdPnlAudio, "subPnlAudio[" + cbAudio.getSelectedIndex() + "]");
			}
		});
		
		
		btnAddAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				addAudioTrack();
				
				cbAudio.setSelectedIndex(cbAudio.getItemCount()-1);
				if (cbAudio.getItemCount() == MAX_STREAMS) {
					btnAddAudio.setEnabled(false);
				}
				
				if (!btnRemoveAudio.isEnabled()) {
					btnRemoveAudio.setEnabled(true);
				}
			}
			
		});
		
		btnRemoveAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cbAudio.getSelectedIndex() > 0) {
					int idx = cbAudio.getItemCount()-1;
					
					cbAudio.removeItemAt(idx);
					lyrdPnlAudio.remove(idx);				
					nAudio--;
				}
				
				if (cbAudio.getItemCount() < MAX_STREAMS && !btnAddAudio.isEnabled()) {
					btnAddAudio.setEnabled(true);
				}
				
				if (cbAudio.getItemCount() == 1) {
					btnRemoveAudio.setEnabled(false);
				}
				
				System.gc();
			}
		});
		
		
		cbSubtitle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lytLyrdPnlSubtitle.show(lyrdPnlSubtitle, "subPnlSubtitle[" + cbSubtitle.getSelectedIndex() + "]");
			}
		});
		
		
		btnAddSubtitle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				addSubtitleTrack();
				
				cbSubtitle.setSelectedIndex(cbSubtitle.getItemCount()-1);
				if (cbSubtitle.getItemCount() == MAX_STREAMS) {
					btnAddSubtitle.setEnabled(false);
				}
				
				if (!btnRemoveSubtitle.isEnabled()) {
					btnRemoveSubtitle.setEnabled(true);
				}
			}
			
		});
		
		btnRemoveSubtitle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cbSubtitle.getSelectedIndex() > 0) {
					int idx = cbSubtitle.getItemCount()-1;
					
					cbSubtitle.removeItemAt(idx);
					lyrdPnlSubtitle.remove(idx);			
					nSubtitle--;
				}
				
				if (cbSubtitle.getItemCount() < MAX_STREAMS && !btnAddSubtitle.isEnabled()) {
					btnAddSubtitle.setEnabled(true);
				}
				
				if (cbSubtitle.getItemCount() == 1) {
					btnRemoveSubtitle.setEnabled(false);
				}
				
				System.gc();
			}
		});
		
		btnProcessFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (modelFiles.getSize() == 0) {
					JOptionPane.showMessageDialog(frmJMkvpropedit,
							"The file list is empty!",
							"Empty list",
							JOptionPane.ERROR_MESSAGE);
				} else {
					setCmdLine();
					
					if (cmdLineBatchOpt.size() == 0) {
						JOptionPane.showMessageDialog(frmJMkvpropedit,
								"Nothing to do!",
								"",	JOptionPane.INFORMATION_MESSAGE);
					} else {
						if (isExecutableInPath(txtMkvPropExe.getText())) {
							executeBatch();
						} else {
							JOptionPane.showMessageDialog(frmJMkvpropedit,
									"Mkvpropedit executable not found!" +
									"\nPlease make sure it is installed and included in the system path.\n" +
									"Alternatively, you can manually set the path or copy its executable to the working folder.",
									"", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				
			}
		});
		
		
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
							
							pnlTabs.setSelectedIndex(pnlTabs.getTabCount()-1);
						}
					}
				}
			}
		});
	}
	
	
	/* Start of track addition methods */
	
	private void addVideoTrack() {
		if (nVideo < MAX_STREAMS) {
			subPnlVideo[nVideo] = new JPanel();
			lyrdPnlVideo.add(subPnlVideo[nVideo], "subPnlVideo[" + nVideo +"]");
			GridBagLayout gbl_subPnlVideo = new GridBagLayout();
			gbl_subPnlVideo.columnWidths = new int[]{0, 0, 0};
			gbl_subPnlVideo.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_subPnlVideo.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_subPnlVideo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			subPnlVideo[nVideo].setLayout(gbl_subPnlVideo);
			
			chbEditVideo[nVideo] = new JCheckBox("Edit this track:");
			GridBagConstraints gbc_chbEditVideo = new GridBagConstraints();
			gbc_chbEditVideo.insets = new Insets(0, 0, 10, 5);
			gbc_chbEditVideo.anchor = GridBagConstraints.WEST;
			gbc_chbEditVideo.gridx = 0;
			gbc_chbEditVideo.gridy = 0;
			subPnlVideo[nVideo].add(chbEditVideo[nVideo], gbc_chbEditVideo);
			
			chbDefaultVideo[nVideo] = new JCheckBox("Default track:");
			chbDefaultVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_chbDefaultVideo = new GridBagConstraints();
			gbc_chbDefaultVideo.insets = new Insets(0, 0, 5, 5);
			gbc_chbDefaultVideo.anchor = GridBagConstraints.WEST;
			gbc_chbDefaultVideo.gridx = 0;
			gbc_chbDefaultVideo.gridy = 1;
			subPnlVideo[nVideo].add(chbDefaultVideo[nVideo], gbc_chbDefaultVideo);
			
			JPanel pnlDefControlsVideo = new JPanel();
			FlowLayout fl_pnlDefControlsVideo = (FlowLayout) pnlDefControlsVideo.getLayout();
			fl_pnlDefControlsVideo.setAlignment(FlowLayout.LEFT);
			fl_pnlDefControlsVideo.setVgap(0);
			GridBagConstraints gbc_pnlDefControlsVideo = new GridBagConstraints();
			gbc_pnlDefControlsVideo.insets = new Insets(0, 0, 5, 0);
			gbc_pnlDefControlsVideo.fill = GridBagConstraints.HORIZONTAL;
			gbc_pnlDefControlsVideo.gridx = 1;
			gbc_pnlDefControlsVideo.gridy = 1;
			subPnlVideo[nVideo].add(pnlDefControlsVideo, gbc_pnlDefControlsVideo);
			
			rbYesDefVideo[nVideo] = new JRadioButton("Yes");
			rbYesDefVideo[nVideo].setEnabled(false);
			rbYesDefVideo[nVideo].setSelected(true);
			pnlDefControlsVideo.add(rbYesDefVideo[nVideo]);
			
			rbNoDefVideo[nVideo] = new JRadioButton("No");
			rbNoDefVideo[nVideo].setEnabled(false);
			pnlDefControlsVideo.add(rbNoDefVideo[nVideo]);
			
			bgRbDefVideo[nVideo] = new ButtonGroup();
			bgRbDefVideo[nVideo].add(rbYesDefVideo[nVideo]);
			bgRbDefVideo[nVideo].add(rbNoDefVideo[nVideo]);
			
			chbForcedVideo[nVideo] = new JCheckBox("Forced track:");
			chbForcedVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_chbForcedVideo = new GridBagConstraints();
			gbc_chbForcedVideo.insets = new Insets(0, 0, 5, 5);
			gbc_chbForcedVideo.anchor = GridBagConstraints.WEST;
			gbc_chbForcedVideo.gridx = 0;
			gbc_chbForcedVideo.gridy = 2;
			subPnlVideo[nVideo].add(chbForcedVideo[nVideo], gbc_chbForcedVideo);
			
			JPanel pnlForControlsVideo = new JPanel();
			FlowLayout fl_pnlForControlsVideo = (FlowLayout) pnlForControlsVideo.getLayout();
			fl_pnlForControlsVideo.setAlignment(FlowLayout.LEFT);
			fl_pnlForControlsVideo.setVgap(0);
			GridBagConstraints gbc_pnlForControlsVideo = new GridBagConstraints();
			gbc_pnlForControlsVideo.insets = new Insets(0, 0, 5, 0);
			gbc_pnlForControlsVideo.fill = GridBagConstraints.HORIZONTAL;
			gbc_pnlForControlsVideo.gridx = 1;
			gbc_pnlForControlsVideo.gridy = 2;
			subPnlVideo[nVideo].add(pnlForControlsVideo, gbc_pnlForControlsVideo);
			
			rbYesForcedVideo[nVideo] = new JRadioButton("Yes");
			rbYesForcedVideo[nVideo].setEnabled(false);
			rbYesForcedVideo[nVideo].setSelected(true);
			pnlForControlsVideo.add(rbYesForcedVideo[nVideo]);
			
			rbNoForcedVideo[nVideo] = new JRadioButton("No");
			rbNoForcedVideo[nVideo].setEnabled(false);
			pnlForControlsVideo.add(rbNoForcedVideo[nVideo]);
			
			bgRbForcedVideo[nVideo] = new ButtonGroup();
			bgRbForcedVideo[nVideo].add(rbYesForcedVideo[nVideo]);
			bgRbForcedVideo[nVideo].add(rbNoForcedVideo[nVideo]);
			
			chbNameVideo[nVideo] = new JCheckBox("Track name:");
			chbNameVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_chbNameVideo = new GridBagConstraints();
			gbc_chbNameVideo.insets = new Insets(0, 0, 5, 5);
			gbc_chbNameVideo.anchor = GridBagConstraints.WEST;
			gbc_chbNameVideo.gridx = 0;
			gbc_chbNameVideo.gridy = 3;
			subPnlVideo[nVideo].add(chbNameVideo[nVideo], gbc_chbNameVideo);
			
			txtNameVideo[nVideo] = new JTextField();
			txtNameVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_txtNameVideo = new GridBagConstraints();
			gbc_txtNameVideo.insets = new Insets(0, 0, 5, 0);
			gbc_txtNameVideo.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtNameVideo.gridx = 1;
			gbc_txtNameVideo.gridy = 3;
			subPnlVideo[nVideo].add(txtNameVideo[nVideo], gbc_txtNameVideo);
			txtNameVideo[nVideo].setColumns(10);
			
			JPanel pnlNumbControlsVideo = new JPanel();
			FlowLayout fl_pnlNumbControlsVideo = (FlowLayout) pnlNumbControlsVideo.getLayout();
			fl_pnlNumbControlsVideo.setAlignment(FlowLayout.LEFT);
			fl_pnlNumbControlsVideo.setVgap(0);
			GridBagConstraints gbc_pnlNumbControlsVideo = new GridBagConstraints();
			gbc_pnlNumbControlsVideo.insets = new Insets(0, 0, 5, 0);
			gbc_pnlNumbControlsVideo.fill = GridBagConstraints.BOTH;
			gbc_pnlNumbControlsVideo.gridx = 1;
			gbc_pnlNumbControlsVideo.gridy = 4;
			subPnlVideo[nVideo].add(pnlNumbControlsVideo, gbc_pnlNumbControlsVideo);
			
			chbNumbVideo[nVideo] = new JCheckBox("Numbering:");
			chbNumbVideo[nVideo].setEnabled(false);
			pnlNumbControlsVideo.add(chbNumbVideo[nVideo]);
			
			Component horizontalStrut1 = Box.createHorizontalStrut(10);
			pnlNumbControlsVideo.add(horizontalStrut1);
			
			lblNumbStartVideo[nVideo] = new JLabel("Start");
			lblNumbStartVideo[nVideo].setEnabled(false);
			pnlNumbControlsVideo.add(lblNumbStartVideo[nVideo]);
			
			txtNumbStartVideo[nVideo] = new JTextField();
			txtNumbStartVideo[nVideo].setText("1");
			txtNumbStartVideo[nVideo].setEnabled(false);
			txtNumbStartVideo[nVideo].setColumns(10);
			pnlNumbControlsVideo.add(txtNumbStartVideo[nVideo]);
			
			Component horizontalStrut2 = Box.createHorizontalStrut(5);
			pnlNumbControlsVideo.add(horizontalStrut2);
			
			lblNumbPadVideo[nVideo] = new JLabel("Padding");
			lblNumbPadVideo[nVideo].setEnabled(false);
			pnlNumbControlsVideo.add(lblNumbPadVideo[nVideo]);
			
			txtNumbPadVideo[nVideo] = new JTextField();
			txtNumbPadVideo[nVideo].setText("1");
			txtNumbPadVideo[nVideo].setEnabled(false);
			txtNumbPadVideo[nVideo].setColumns(10);
			pnlNumbControlsVideo.add(txtNumbPadVideo[nVideo]);
			
			lblNumbExplainVideo[nVideo] = new JLabel("      To use it, add {num} to the name (e.g. \"My Video {num}\")");
			lblNumbExplainVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_lblNumbExplainVideo = new GridBagConstraints();
			gbc_lblNumbExplainVideo.insets = new Insets(0, 0, 10, 0);
			gbc_lblNumbExplainVideo.anchor = GridBagConstraints.WEST;
			gbc_lblNumbExplainVideo.gridx = 1;
			gbc_lblNumbExplainVideo.gridy = 5;
			subPnlVideo[nVideo].add(lblNumbExplainVideo[nVideo], gbc_lblNumbExplainVideo);
			
			chbLangVideo[nVideo] = new JCheckBox("Language:");
			chbLangVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_chbLangVideo = new GridBagConstraints();
			gbc_chbLangVideo.anchor = GridBagConstraints.WEST;
			gbc_chbLangVideo.insets = new Insets(0, 0, 10, 5);
			gbc_chbLangVideo.gridx = 0;
			gbc_chbLangVideo.gridy = 6;
			subPnlVideo[nVideo].add(chbLangVideo[nVideo], gbc_chbLangVideo);
			
			cbLangVideo[nVideo] = new JComboBox();
			cbLangVideo[nVideo].setEnabled(false);
			cbLangVideo[nVideo].setModel(new DefaultComboBoxModel(mkvLang.getLangName()));
			cbLangVideo[nVideo].setSelectedIndex(mkvLang.getAsLangCode().indexOf("und"));
			GridBagConstraints gbc_cbLangVideo = new GridBagConstraints();
			gbc_cbLangVideo.insets = new Insets(0, 0, 10, 0);
			gbc_cbLangVideo.anchor = GridBagConstraints.WEST;
			gbc_cbLangVideo.gridx = 1;
			gbc_cbLangVideo.gridy = 6;
			subPnlVideo[nVideo].add(cbLangVideo[nVideo], gbc_cbLangVideo);
			
			chbExtraCmdVideo[nVideo] = new JCheckBox("Extra parameters:");
			chbExtraCmdVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_chbExtraCmdVideo = new GridBagConstraints();
			gbc_chbExtraCmdVideo.anchor = GridBagConstraints.WEST;
			gbc_chbExtraCmdVideo.gridx = 0;
			gbc_chbExtraCmdVideo.gridy = 7;
			subPnlVideo[nVideo].add(chbExtraCmdVideo[nVideo], gbc_chbExtraCmdVideo);
			
			txtExtraCmdVideo[nVideo] = new JTextField();
			txtExtraCmdVideo[nVideo].setEnabled(false);
			GridBagConstraints gbc_txtExtraCmdVideo = new GridBagConstraints();
			gbc_txtExtraCmdVideo.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtExtraCmdVideo.gridx = 1;
			gbc_txtExtraCmdVideo.gridy = 7;
			subPnlVideo[nVideo].add(txtExtraCmdVideo[nVideo], gbc_txtExtraCmdVideo);
			txtExtraCmdVideo[nVideo].setColumns(10);
			
			/* Start of mouse events for right-click menu */
			
			Utils.addRCMenuMouseListener(txtNameVideo[nVideo]);
			Utils.addRCMenuMouseListener(txtNumbStartVideo[nVideo]);
			Utils.addRCMenuMouseListener(txtNumbPadVideo[nVideo]);
			Utils.addRCMenuMouseListener(txtExtraCmdVideo[nVideo]);
			
			/* End of mouse events for right-click menu */
			
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
						chbNumbVideo[curCbVideo].setEnabled(!state);
						
						if (chbNumbVideo[curCbVideo].isSelected()) {
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
			
			
			chbNameVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = chbNumbVideo[curCbVideo].isEnabled();
					
					chbNumbVideo[curCbVideo].setEnabled(!state);
					txtNameVideo[curCbVideo].setEnabled(!state);

					if (chbNumbVideo[curCbVideo].isSelected()) {
						lblNumbStartVideo[curCbVideo].setEnabled(!state);
						txtNumbStartVideo[curCbVideo].setEnabled(!state);
						lblNumbPadVideo[curCbVideo].setEnabled(!state);
						txtNumbPadVideo[curCbVideo].setEnabled(!state);
						lblNumbExplainVideo[curCbVideo].setEnabled(!state);
					}
				}
			});
			
			chbNumbVideo[nVideo].addActionListener(new ActionListener() {
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
			
			txtNumbStartVideo[nVideo].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					
					try {
						if (Integer.parseInt(txtNumbStartVideo[curCbVideo].getText()) < 0) {
							txtNumbStartVideo[curCbVideo].setText("1");
						}
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
						if (Integer.parseInt(txtNumbPadVideo[curCbVideo].getText()) < 0) {
							txtNumbPadVideo[curCbVideo].setText("1");
						}
					} catch (NumberFormatException e1) {
						txtNumbPadVideo[curCbVideo].setText("1");
					}
				}
			});
			
			chbLangVideo[nVideo].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbVideo = cbVideo.getSelectedIndex();
					boolean state = cbLangVideo[curCbVideo].isEnabled();
					
					cbLangVideo[curCbVideo].setEnabled(!state);
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
		if (nAudio < MAX_STREAMS) {
			subPnlAudio[nAudio] = new JPanel();
			lyrdPnlAudio.add(subPnlAudio[nAudio], "subPnlAudio[" + nAudio +"]");
			GridBagLayout gbl_subPnlAudio = new GridBagLayout();
			gbl_subPnlAudio.columnWidths = new int[]{0, 0, 0};
			gbl_subPnlAudio.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_subPnlAudio.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_subPnlAudio.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			subPnlAudio[nAudio].setLayout(gbl_subPnlAudio);
			
			chbEditAudio[nAudio] = new JCheckBox("Edit this track:");
			GridBagConstraints gbc_chbEditAudio = new GridBagConstraints();
			gbc_chbEditAudio.insets = new Insets(0, 0, 10, 5);
			gbc_chbEditAudio.anchor = GridBagConstraints.WEST;
			gbc_chbEditAudio.gridx = 0;
			gbc_chbEditAudio.gridy = 0;
			subPnlAudio[nAudio].add(chbEditAudio[nAudio], gbc_chbEditAudio);
			
			chbDefaultAudio[nAudio] = new JCheckBox("Default track:");
			chbDefaultAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_chbDefaultAudio = new GridBagConstraints();
			gbc_chbDefaultAudio.insets = new Insets(0, 0, 5, 5);
			gbc_chbDefaultAudio.anchor = GridBagConstraints.WEST;
			gbc_chbDefaultAudio.gridx = 0;
			gbc_chbDefaultAudio.gridy = 1;
			subPnlAudio[nAudio].add(chbDefaultAudio[nAudio], gbc_chbDefaultAudio);
			
			JPanel pnlDefControlsAudio = new JPanel();
			FlowLayout fl_pnlDefControlsAudio = (FlowLayout) pnlDefControlsAudio.getLayout();
			fl_pnlDefControlsAudio.setAlignment(FlowLayout.LEFT);
			fl_pnlDefControlsAudio.setVgap(0);
			GridBagConstraints gbc_pnlDefControlsAudio = new GridBagConstraints();
			gbc_pnlDefControlsAudio.insets = new Insets(0, 0, 5, 0);
			gbc_pnlDefControlsAudio.fill = GridBagConstraints.HORIZONTAL;
			gbc_pnlDefControlsAudio.gridx = 1;
			gbc_pnlDefControlsAudio.gridy = 1;
			subPnlAudio[nAudio].add(pnlDefControlsAudio, gbc_pnlDefControlsAudio);
			
			rbYesDefAudio[nAudio] = new JRadioButton("Yes");
			rbYesDefAudio[nAudio].setEnabled(false);
			rbYesDefAudio[nAudio].setSelected(true);
			pnlDefControlsAudio.add(rbYesDefAudio[nAudio]);
			
			rbNoDefAudio[nAudio] = new JRadioButton("No");
			rbNoDefAudio[nAudio].setEnabled(false);
			pnlDefControlsAudio.add(rbNoDefAudio[nAudio]);
			
			bgRbDefAudio[nAudio] = new ButtonGroup();
			bgRbDefAudio[nAudio].add(rbYesDefAudio[nAudio]);
			bgRbDefAudio[nAudio].add(rbNoDefAudio[nAudio]);
			
			chbForcedAudio[nAudio] = new JCheckBox("Forced track:");
			chbForcedAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_chbForcedAudio = new GridBagConstraints();
			gbc_chbForcedAudio.insets = new Insets(0, 0, 5, 5);
			gbc_chbForcedAudio.anchor = GridBagConstraints.WEST;
			gbc_chbForcedAudio.gridx = 0;
			gbc_chbForcedAudio.gridy = 2;
			subPnlAudio[nAudio].add(chbForcedAudio[nAudio], gbc_chbForcedAudio);
			
			JPanel pnlForControlsAudio = new JPanel();
			FlowLayout fl_pnlForControlsAudio = (FlowLayout) pnlForControlsAudio.getLayout();
			fl_pnlForControlsAudio.setAlignment(FlowLayout.LEFT);
			fl_pnlForControlsAudio.setVgap(0);
			GridBagConstraints gbc_pnlForControlsAudio = new GridBagConstraints();
			gbc_pnlForControlsAudio.insets = new Insets(0, 0, 5, 0);
			gbc_pnlForControlsAudio.fill = GridBagConstraints.HORIZONTAL;
			gbc_pnlForControlsAudio.gridx = 1;
			gbc_pnlForControlsAudio.gridy = 2;
			subPnlAudio[nAudio].add(pnlForControlsAudio, gbc_pnlForControlsAudio);
			
			rbYesForcedAudio[nAudio] = new JRadioButton("Yes");
			rbYesForcedAudio[nAudio].setEnabled(false);
			rbYesForcedAudio[nAudio].setSelected(true);
			pnlForControlsAudio.add(rbYesForcedAudio[nAudio]);
			
			rbNoForcedAudio[nAudio] = new JRadioButton("No");
			rbNoForcedAudio[nAudio].setEnabled(false);
			pnlForControlsAudio.add(rbNoForcedAudio[nAudio]);
			
			bgRbForcedAudio[nAudio] = new ButtonGroup();
			bgRbForcedAudio[nAudio].add(rbYesForcedAudio[nAudio]);
			bgRbForcedAudio[nAudio].add(rbNoForcedAudio[nAudio]);
			
			chbNameAudio[nAudio] = new JCheckBox("Track name:");
			chbNameAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_chbNameAudio = new GridBagConstraints();
			gbc_chbNameAudio.insets = new Insets(0, 0, 5, 5);
			gbc_chbNameAudio.anchor = GridBagConstraints.WEST;
			gbc_chbNameAudio.gridx = 0;
			gbc_chbNameAudio.gridy = 3;
			subPnlAudio[nAudio].add(chbNameAudio[nAudio], gbc_chbNameAudio);
			
			txtNameAudio[nAudio] = new JTextField();
			txtNameAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_txtNameAudio = new GridBagConstraints();
			gbc_txtNameAudio.insets = new Insets(0, 0, 5, 0);
			gbc_txtNameAudio.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtNameAudio.gridx = 1;
			gbc_txtNameAudio.gridy = 3;
			subPnlAudio[nAudio].add(txtNameAudio[nAudio], gbc_txtNameAudio);
			txtNameAudio[nAudio].setColumns(10);
			
			JPanel pnlNumbControlsAudio = new JPanel();
			FlowLayout fl_pnlNumbControlsAudio = (FlowLayout) pnlNumbControlsAudio.getLayout();
			fl_pnlNumbControlsAudio.setAlignment(FlowLayout.LEFT);
			fl_pnlNumbControlsAudio.setVgap(0);
			GridBagConstraints gbc_pnlNumbControlsAudio = new GridBagConstraints();
			gbc_pnlNumbControlsAudio.insets = new Insets(0, 0, 5, 0);
			gbc_pnlNumbControlsAudio.fill = GridBagConstraints.BOTH;
			gbc_pnlNumbControlsAudio.gridx = 1;
			gbc_pnlNumbControlsAudio.gridy = 4;
			subPnlAudio[nAudio].add(pnlNumbControlsAudio, gbc_pnlNumbControlsAudio);
			
			chbNumbAudio[nAudio] = new JCheckBox("Numbering:");
			chbNumbAudio[nAudio].setEnabled(false);
			pnlNumbControlsAudio.add(chbNumbAudio[nAudio]);
			
			Component horizontalStrut1 = Box.createHorizontalStrut(10);
			pnlNumbControlsAudio.add(horizontalStrut1);
			
			lblNumbStartAudio[nAudio] = new JLabel("Start");
			lblNumbStartAudio[nAudio].setEnabled(false);
			pnlNumbControlsAudio.add(lblNumbStartAudio[nAudio]);
			
			txtNumbStartAudio[nAudio] = new JTextField();
			txtNumbStartAudio[nAudio].setText("1");
			txtNumbStartAudio[nAudio].setEnabled(false);
			txtNumbStartAudio[nAudio].setColumns(10);
			pnlNumbControlsAudio.add(txtNumbStartAudio[nAudio]);
			
			Component horizontalStrut2 = Box.createHorizontalStrut(5);
			pnlNumbControlsAudio.add(horizontalStrut2);
			
			lblNumbPadAudio[nAudio] = new JLabel("Padding");
			lblNumbPadAudio[nAudio].setEnabled(false);
			pnlNumbControlsAudio.add(lblNumbPadAudio[nAudio]);
			
			txtNumbPadAudio[nAudio] = new JTextField();
			txtNumbPadAudio[nAudio].setText("1");
			txtNumbPadAudio[nAudio].setEnabled(false);
			txtNumbPadAudio[nAudio].setColumns(10);
			pnlNumbControlsAudio.add(txtNumbPadAudio[nAudio]);
			
			lblNumbExplainAudio[nAudio] = new JLabel("      To use it, add {num} to the name (e.g. \"My Audio {num}\")");
			lblNumbExplainAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_lblNumbExplainAudio = new GridBagConstraints();
			gbc_lblNumbExplainAudio.insets = new Insets(0, 0, 10, 0);
			gbc_lblNumbExplainAudio.anchor = GridBagConstraints.WEST;
			gbc_lblNumbExplainAudio.gridx = 1;
			gbc_lblNumbExplainAudio.gridy = 5;
			subPnlAudio[nAudio].add(lblNumbExplainAudio[nAudio], gbc_lblNumbExplainAudio);
			
			chbLangAudio[nAudio] = new JCheckBox("Language:");
			chbLangAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_chbLangAudio = new GridBagConstraints();
			gbc_chbLangAudio.anchor = GridBagConstraints.WEST;
			gbc_chbLangAudio.insets = new Insets(0, 0, 10, 5);
			gbc_chbLangAudio.gridx = 0;
			gbc_chbLangAudio.gridy = 6;
			subPnlAudio[nAudio].add(chbLangAudio[nAudio], gbc_chbLangAudio);
			
			cbLangAudio[nAudio] = new JComboBox();
			cbLangAudio[nAudio].setEnabled(false);
			cbLangAudio[nAudio].setModel(new DefaultComboBoxModel(mkvLang.getLangName()));
			cbLangAudio[nAudio].setSelectedIndex(mkvLang.getAsLangCode().indexOf("und"));
			GridBagConstraints gbc_cbLangAudio = new GridBagConstraints();
			gbc_cbLangAudio.insets = new Insets(0, 0, 10, 0);
			gbc_cbLangAudio.anchor = GridBagConstraints.WEST;
			gbc_cbLangAudio.gridx = 1;
			gbc_cbLangAudio.gridy = 6;
			subPnlAudio[nAudio].add(cbLangAudio[nAudio], gbc_cbLangAudio);
			
			chbExtraCmdAudio[nAudio] = new JCheckBox("Extra parameters:");
			chbExtraCmdAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_chbExtraCmdAudio = new GridBagConstraints();
			gbc_chbExtraCmdAudio.anchor = GridBagConstraints.WEST;
			gbc_chbExtraCmdAudio.gridx = 0;
			gbc_chbExtraCmdAudio.gridy = 7;
			subPnlAudio[nAudio].add(chbExtraCmdAudio[nAudio], gbc_chbExtraCmdAudio);
			
			txtExtraCmdAudio[nAudio] = new JTextField();
			txtExtraCmdAudio[nAudio].setEnabled(false);
			GridBagConstraints gbc_txtExtraCmdAudio = new GridBagConstraints();
			gbc_txtExtraCmdAudio.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtExtraCmdAudio.gridx = 1;
			gbc_txtExtraCmdAudio.gridy = 7;
			subPnlAudio[nAudio].add(txtExtraCmdAudio[nAudio], gbc_txtExtraCmdAudio);
			txtExtraCmdAudio[nAudio].setColumns(10);
			
			/* Start of mouse events for right-click menu */
			
			Utils.addRCMenuMouseListener(txtNameAudio[nAudio]);
			Utils.addRCMenuMouseListener(txtNumbStartAudio[nAudio]);
			Utils.addRCMenuMouseListener(txtNumbPadAudio[nAudio]);
			Utils.addRCMenuMouseListener(txtExtraCmdAudio[nAudio]);
			
			/* End of mouse events for right-click menu */
			
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
						chbNumbAudio[curCbAudio].setEnabled(!state);
						
						if (chbNumbAudio[curCbAudio].isSelected()) {
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
			
			
			chbNameAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = chbNumbAudio[curCbAudio].isEnabled();
					
					chbNumbAudio[curCbAudio].setEnabled(!state);
					txtNameAudio[curCbAudio].setEnabled(!state);

					if (chbNumbAudio[curCbAudio].isSelected()) {
						lblNumbStartAudio[curCbAudio].setEnabled(!state);
						txtNumbStartAudio[curCbAudio].setEnabled(!state);
						lblNumbPadAudio[curCbAudio].setEnabled(!state);
						txtNumbPadAudio[curCbAudio].setEnabled(!state);
						lblNumbExplainAudio[curCbAudio].setEnabled(!state);
					}
				}
			});
			
			chbNumbAudio[nAudio].addActionListener(new ActionListener() {
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
			
			txtNumbStartAudio[nAudio].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					
					try {
						if (Integer.parseInt(txtNumbStartAudio[curCbAudio].getText()) < 0) {
							txtNumbStartAudio[curCbAudio].setText("1");
						}
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
						if (Integer.parseInt(txtNumbPadAudio[curCbAudio].getText()) < 0) {
							txtNumbPadAudio[curCbAudio].setText("1");
						}
					} catch (NumberFormatException e1) {
						txtNumbPadAudio[curCbAudio].setText("1");
					}
				}
			});
			
			chbLangAudio[nAudio].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbAudio = cbAudio.getSelectedIndex();
					boolean state = cbLangAudio[curCbAudio].isEnabled();
					
					cbLangAudio[curCbAudio].setEnabled(!state);
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
		if (nSubtitle < MAX_STREAMS) {
			subPnlSubtitle[nSubtitle] = new JPanel();
			lyrdPnlSubtitle.add(subPnlSubtitle[nSubtitle], "subPnlSubtitle[" + nSubtitle +"]");
			GridBagLayout gbl_subPnlSubtitle = new GridBagLayout();
			gbl_subPnlSubtitle.columnWidths = new int[]{0, 0, 0};
			gbl_subPnlSubtitle.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_subPnlSubtitle.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_subPnlSubtitle.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			subPnlSubtitle[nSubtitle].setLayout(gbl_subPnlSubtitle);
			
			chbEditSubtitle[nSubtitle] = new JCheckBox("Edit this track:");
			GridBagConstraints gbc_chbEditSubtitle = new GridBagConstraints();
			gbc_chbEditSubtitle.insets = new Insets(0, 0, 10, 5);
			gbc_chbEditSubtitle.anchor = GridBagConstraints.WEST;
			gbc_chbEditSubtitle.gridx = 0;
			gbc_chbEditSubtitle.gridy = 0;
			subPnlSubtitle[nSubtitle].add(chbEditSubtitle[nSubtitle], gbc_chbEditSubtitle);
			
			chbDefaultSubtitle[nSubtitle] = new JCheckBox("Default track:");
			chbDefaultSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_chbDefaultSubtitle = new GridBagConstraints();
			gbc_chbDefaultSubtitle.insets = new Insets(0, 0, 5, 5);
			gbc_chbDefaultSubtitle.anchor = GridBagConstraints.WEST;
			gbc_chbDefaultSubtitle.gridx = 0;
			gbc_chbDefaultSubtitle.gridy = 1;
			subPnlSubtitle[nSubtitle].add(chbDefaultSubtitle[nSubtitle], gbc_chbDefaultSubtitle);
			
			JPanel pnlDefControlsSubtitle = new JPanel();
			FlowLayout fl_pnlDefControlsSubtitle = (FlowLayout) pnlDefControlsSubtitle.getLayout();
			fl_pnlDefControlsSubtitle.setAlignment(FlowLayout.LEFT);
			fl_pnlDefControlsSubtitle.setVgap(0);
			GridBagConstraints gbc_pnlDefControlsSubtitle = new GridBagConstraints();
			gbc_pnlDefControlsSubtitle.insets = new Insets(0, 0, 5, 0);
			gbc_pnlDefControlsSubtitle.fill = GridBagConstraints.HORIZONTAL;
			gbc_pnlDefControlsSubtitle.gridx = 1;
			gbc_pnlDefControlsSubtitle.gridy = 1;
			subPnlSubtitle[nSubtitle].add(pnlDefControlsSubtitle, gbc_pnlDefControlsSubtitle);
			
			rbYesDefSubtitle[nSubtitle] = new JRadioButton("Yes");
			rbYesDefSubtitle[nSubtitle].setEnabled(false);
			rbYesDefSubtitle[nSubtitle].setSelected(true);
			pnlDefControlsSubtitle.add(rbYesDefSubtitle[nSubtitle]);
			
			rbNoDefSubtitle[nSubtitle] = new JRadioButton("No");
			rbNoDefSubtitle[nSubtitle].setEnabled(false);
			pnlDefControlsSubtitle.add(rbNoDefSubtitle[nSubtitle]);
			
			bgRbDefSubtitle[nSubtitle] = new ButtonGroup();
			bgRbDefSubtitle[nSubtitle].add(rbYesDefSubtitle[nSubtitle]);
			bgRbDefSubtitle[nSubtitle].add(rbNoDefSubtitle[nSubtitle]);
			
			chbForcedSubtitle[nSubtitle] = new JCheckBox("Forced track:");
			chbForcedSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_chbForcedSubtitle = new GridBagConstraints();
			gbc_chbForcedSubtitle.insets = new Insets(0, 0, 5, 5);
			gbc_chbForcedSubtitle.anchor = GridBagConstraints.WEST;
			gbc_chbForcedSubtitle.gridx = 0;
			gbc_chbForcedSubtitle.gridy = 2;
			subPnlSubtitle[nSubtitle].add(chbForcedSubtitle[nSubtitle], gbc_chbForcedSubtitle);
			
			JPanel pnlForControlsSubtitle = new JPanel();
			FlowLayout fl_pnlForControlsSubtitle = (FlowLayout) pnlForControlsSubtitle.getLayout();
			fl_pnlForControlsSubtitle.setAlignment(FlowLayout.LEFT);
			fl_pnlForControlsSubtitle.setVgap(0);
			GridBagConstraints gbc_pnlForControlsSubtitle = new GridBagConstraints();
			gbc_pnlForControlsSubtitle.insets = new Insets(0, 0, 5, 0);
			gbc_pnlForControlsSubtitle.fill = GridBagConstraints.HORIZONTAL;
			gbc_pnlForControlsSubtitle.gridx = 1;
			gbc_pnlForControlsSubtitle.gridy = 2;
			subPnlSubtitle[nSubtitle].add(pnlForControlsSubtitle, gbc_pnlForControlsSubtitle);
			
			rbYesForcedSubtitle[nSubtitle] = new JRadioButton("Yes");
			rbYesForcedSubtitle[nSubtitle].setEnabled(false);
			rbYesForcedSubtitle[nSubtitle].setSelected(true);
			pnlForControlsSubtitle.add(rbYesForcedSubtitle[nSubtitle]);
			
			rbNoForcedSubtitle[nSubtitle] = new JRadioButton("No");
			rbNoForcedSubtitle[nSubtitle].setEnabled(false);
			pnlForControlsSubtitle.add(rbNoForcedSubtitle[nSubtitle]);
			
			bgRbForcedSubtitle[nSubtitle] = new ButtonGroup();
			bgRbForcedSubtitle[nSubtitle].add(rbYesForcedSubtitle[nSubtitle]);
			bgRbForcedSubtitle[nSubtitle].add(rbNoForcedSubtitle[nSubtitle]);
			
			chbNameSubtitle[nSubtitle] = new JCheckBox("Track name:");
			chbNameSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_chbNameSubtitle = new GridBagConstraints();
			gbc_chbNameSubtitle.insets = new Insets(0, 0, 5, 5);
			gbc_chbNameSubtitle.anchor = GridBagConstraints.WEST;
			gbc_chbNameSubtitle.gridx = 0;
			gbc_chbNameSubtitle.gridy = 3;
			subPnlSubtitle[nSubtitle].add(chbNameSubtitle[nSubtitle], gbc_chbNameSubtitle);
			
			txtNameSubtitle[nSubtitle] = new JTextField();
			txtNameSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_txtNameSubtitle = new GridBagConstraints();
			gbc_txtNameSubtitle.insets = new Insets(0, 0, 5, 0);
			gbc_txtNameSubtitle.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtNameSubtitle.gridx = 1;
			gbc_txtNameSubtitle.gridy = 3;
			subPnlSubtitle[nSubtitle].add(txtNameSubtitle[nSubtitle], gbc_txtNameSubtitle);
			txtNameSubtitle[nSubtitle].setColumns(10);
			
			JPanel pnlNumbControlsSubtitle = new JPanel();
			FlowLayout fl_pnlNumbControlsSubtitle = (FlowLayout) pnlNumbControlsSubtitle.getLayout();
			fl_pnlNumbControlsSubtitle.setAlignment(FlowLayout.LEFT);
			fl_pnlNumbControlsSubtitle.setVgap(0);
			GridBagConstraints gbc_pnlNumbControlsSubtitle = new GridBagConstraints();
			gbc_pnlNumbControlsSubtitle.insets = new Insets(0, 0, 5, 0);
			gbc_pnlNumbControlsSubtitle.fill = GridBagConstraints.BOTH;
			gbc_pnlNumbControlsSubtitle.gridx = 1;
			gbc_pnlNumbControlsSubtitle.gridy = 4;
			subPnlSubtitle[nSubtitle].add(pnlNumbControlsSubtitle, gbc_pnlNumbControlsSubtitle);
			
			chbNumbSubtitle[nSubtitle] = new JCheckBox("Numbering:");
			chbNumbSubtitle[nSubtitle].setEnabled(false);
			pnlNumbControlsSubtitle.add(chbNumbSubtitle[nSubtitle]);
			
			Component horizontalStrut1 = Box.createHorizontalStrut(10);
			pnlNumbControlsSubtitle.add(horizontalStrut1);
			
			lblNumbStartSubtitle[nSubtitle] = new JLabel("Start");
			lblNumbStartSubtitle[nSubtitle].setEnabled(false);
			pnlNumbControlsSubtitle.add(lblNumbStartSubtitle[nSubtitle]);
			
			txtNumbStartSubtitle[nSubtitle] = new JTextField();
			txtNumbStartSubtitle[nSubtitle].setText("1");
			txtNumbStartSubtitle[nSubtitle].setEnabled(false);
			txtNumbStartSubtitle[nSubtitle].setColumns(10);
			pnlNumbControlsSubtitle.add(txtNumbStartSubtitle[nSubtitle]);
			
			Component horizontalStrut2 = Box.createHorizontalStrut(5);
			pnlNumbControlsSubtitle.add(horizontalStrut2);
			
			lblNumbPadSubtitle[nSubtitle] = new JLabel("Padding");
			lblNumbPadSubtitle[nSubtitle].setEnabled(false);
			pnlNumbControlsSubtitle.add(lblNumbPadSubtitle[nSubtitle]);
			
			txtNumbPadSubtitle[nSubtitle] = new JTextField();
			txtNumbPadSubtitle[nSubtitle].setText("1");
			txtNumbPadSubtitle[nSubtitle].setEnabled(false);
			txtNumbPadSubtitle[nSubtitle].setColumns(10);
			pnlNumbControlsSubtitle.add(txtNumbPadSubtitle[nSubtitle]);
			
			lblNumbExplainSubtitle[nSubtitle] = new JLabel("      To use it, add {num} to the name (e.g. \"My Subtitle {num}\")");
			lblNumbExplainSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_lblNumbExplainSubtitle = new GridBagConstraints();
			gbc_lblNumbExplainSubtitle.insets = new Insets(0, 0, 10, 0);
			gbc_lblNumbExplainSubtitle.anchor = GridBagConstraints.WEST;
			gbc_lblNumbExplainSubtitle.gridx = 1;
			gbc_lblNumbExplainSubtitle.gridy = 5;
			subPnlSubtitle[nSubtitle].add(lblNumbExplainSubtitle[nSubtitle], gbc_lblNumbExplainSubtitle);
			
			chbLangSubtitle[nSubtitle] = new JCheckBox("Language:");
			chbLangSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_chbLangSubtitle = new GridBagConstraints();
			gbc_chbLangSubtitle.anchor = GridBagConstraints.WEST;
			gbc_chbLangSubtitle.insets = new Insets(0, 0, 10, 5);
			gbc_chbLangSubtitle.gridx = 0;
			gbc_chbLangSubtitle.gridy = 6;
			subPnlSubtitle[nSubtitle].add(chbLangSubtitle[nSubtitle], gbc_chbLangSubtitle);
			
			cbLangSubtitle[nSubtitle] = new JComboBox();
			cbLangSubtitle[nSubtitle].setEnabled(false);
			cbLangSubtitle[nSubtitle].setModel(new DefaultComboBoxModel(mkvLang.getLangName()));
			cbLangSubtitle[nSubtitle].setSelectedIndex(mkvLang.getAsLangCode().indexOf("und"));
			GridBagConstraints gbc_cbLangSubtitle = new GridBagConstraints();
			gbc_cbLangSubtitle.insets = new Insets(0, 0, 10, 0);
			gbc_cbLangSubtitle.anchor = GridBagConstraints.WEST;
			gbc_cbLangSubtitle.gridx = 1;
			gbc_cbLangSubtitle.gridy = 6;
			subPnlSubtitle[nSubtitle].add(cbLangSubtitle[nSubtitle], gbc_cbLangSubtitle);
			
			chbExtraCmdSubtitle[nSubtitle] = new JCheckBox("Extra parameters:");
			chbExtraCmdSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_chbExtraCmdSubtitle = new GridBagConstraints();
			gbc_chbExtraCmdSubtitle.anchor = GridBagConstraints.WEST;
			gbc_chbExtraCmdSubtitle.gridx = 0;
			gbc_chbExtraCmdSubtitle.gridy = 7;
			subPnlSubtitle[nSubtitle].add(chbExtraCmdSubtitle[nSubtitle], gbc_chbExtraCmdSubtitle);
			
			txtExtraCmdSubtitle[nSubtitle] = new JTextField();
			txtExtraCmdSubtitle[nSubtitle].setEnabled(false);
			GridBagConstraints gbc_txtExtraCmdSubtitle = new GridBagConstraints();
			gbc_txtExtraCmdSubtitle.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtExtraCmdSubtitle.gridx = 1;
			gbc_txtExtraCmdSubtitle.gridy = 7;
			subPnlSubtitle[nSubtitle].add(txtExtraCmdSubtitle[nSubtitle], gbc_txtExtraCmdSubtitle);
			txtExtraCmdSubtitle[nSubtitle].setColumns(10);
			
			/* Start of mouse events for right-click menu */
			
			Utils.addRCMenuMouseListener(txtNameSubtitle[nSubtitle]);
			Utils.addRCMenuMouseListener(txtNumbStartSubtitle[nSubtitle]);
			Utils.addRCMenuMouseListener(txtNumbPadSubtitle[nSubtitle]);
			Utils.addRCMenuMouseListener(txtExtraCmdSubtitle[nSubtitle]);
			
			/* End of mouse events for right-click menu */
			
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
						chbNumbSubtitle[curCbSubtitle].setEnabled(!state);
						
						if (chbNumbSubtitle[curCbSubtitle].isSelected()) {
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
			
			
			chbNameSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = chbNumbSubtitle[curCbSubtitle].isEnabled();
					
					chbNumbSubtitle[curCbSubtitle].setEnabled(!state);
					txtNameSubtitle[curCbSubtitle].setEnabled(!state);

					if (chbNumbSubtitle[curCbSubtitle].isSelected()) {
						lblNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
						txtNumbStartSubtitle[curCbSubtitle].setEnabled(!state);
						lblNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
						txtNumbPadSubtitle[curCbSubtitle].setEnabled(!state);
						lblNumbExplainSubtitle[curCbSubtitle].setEnabled(!state);
					}
				}
			});
			
			chbNumbSubtitle[nSubtitle].addActionListener(new ActionListener() {
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
			
			txtNumbStartSubtitle[nSubtitle].addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					
					try {
						if (Integer.parseInt(txtNumbStartSubtitle[curCbSubtitle].getText()) < 0) {
							txtNumbStartSubtitle[curCbSubtitle].setText("1");
						}
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
						if (Integer.parseInt(txtNumbPadSubtitle[curCbSubtitle].getText()) < 0) {
							txtNumbPadSubtitle[curCbSubtitle].setText("1");
						}
					} catch (NumberFormatException e1) {
						txtNumbPadSubtitle[curCbSubtitle].setText("1");
					}
				}
			});
			
			chbLangSubtitle[nSubtitle].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int curCbSubtitle = cbSubtitle.getSelectedIndex();
					boolean state = cbLangSubtitle[curCbSubtitle].isEnabled();
					
					cbLangSubtitle[curCbSubtitle].setEnabled(!state);
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
	
	/* End of track addition methods */
	
	
	/* Start of command line methods */
	
	private void setCmdLineGeneral() {
		cmdLineGeneral = new String[modelFiles.size()];
		cmdLineGeneralOpt = new String[modelFiles.size()];
		int start = Integer.parseInt(txtNumbStartGeneral.getText());
	
		for (int i = 0; i < modelFiles.size(); i++) {
			cmdLineGeneral[i] = "";
			cmdLineGeneralOpt[i] = "";
			
			if (chbTags.isSelected()) {
				switch (cbTags.getSelectedIndex()) {
					case 0:
						cmdLineGeneral[i] += " --tags all:";
						cmdLineGeneralOpt[i] += " --tags all:";
						break;
					case 1:
						if (txtTags.getText().trim().isEmpty()) {
							cmdLineGeneral[i] += " --tags all:";
							cmdLineGeneralOpt[i] += " --tags all:";
						} else {
							if (Utils.isWindows()) {
								cmdLineGeneral[i] += " --tags all:\"" + txtTags.getText() + "\"";
								cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(txtTags.getText()) + "\"";
							} else {
								cmdLineGeneral[i] += " --tags all:\"" + Utils.escapeQuotes(txtTags.getText()) + "\"";
								cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(txtTags.getText()) + "\"";
							}
						}
						break;
					case 2:
						String tmpTags = Utils.getPathWithoutExt((String) modelFiles.get(i)) +
										 txtTags.getText() + "." + cbExtTags.getSelectedItem();
						
						if (Utils.isWindows()) {
							cmdLineGeneral[i] += " --tags all:\"" + tmpTags + "\"";
							cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(tmpTags) + "\"";
						} else {
							cmdLineGeneral[i] += " --tags all:\"" + Utils.escapeQuotes(tmpTags) + "\"";
							cmdLineGeneralOpt[i] += " --tags all:\"" + Utils.escapeName(tmpTags) + "\"";
						}
						break;
				}
			}
			
			if (chbChapters.isSelected()) {
				switch (cbChapters.getSelectedIndex()) {
					case 0:
						cmdLineGeneral[i] += " --chapters \"\"";
						cmdLineGeneralOpt[i] += " --chapters #EMPTY#";
						break;
					case 1:
						if (txtChapters.getText().trim().isEmpty()) {
							cmdLineGeneral[i] += " --chapters \"\"";
							cmdLineGeneralOpt[i] += " --chapters #EMPTY#";
						} else {
							if (Utils.isWindows()) {
								cmdLineGeneral[i] += " --chapters \"" + txtChapters.getText() + "\"";
								cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(txtChapters.getText()) + "\"";
							} else {
								cmdLineGeneral[i] += " --chapters \"" + Utils.escapeQuotes(txtChapters.getText()) + "\"";
								cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(txtChapters.getText()) + "\"";
							}
						}
						break;
					case 2:
						String tmpChaps = Utils.getPathWithoutExt((String) modelFiles.get(i)) +
										  txtChapters.getText() + "." + cbExtChapters.getSelectedItem();
						
						if (Utils.isWindows()) {
							cmdLineGeneral[i] += " --chapters \"" + tmpChaps + "\"";
							cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(tmpChaps) + "\"";
						} else {
							cmdLineGeneral[i] += " --chapters \"" + Utils.escapeQuotes(tmpChaps) + "\"";
							cmdLineGeneralOpt[i] += " --chapters \"" + Utils.escapeName(tmpChaps) + "\"";
						}
						break;
				}
			}
			
			if (chbTitleGeneral.isSelected()) {	
				cmdLineGeneral[i] += " --edit info";
				cmdLineGeneralOpt[i] += " --edit info";
				
				if (chbNumbGeneral.isSelected()) {
					int pad = 0;
					
					pad = Integer.parseInt(txtNumbPadGeneral.getText());
					
					String newTitle = txtTitleGeneral.getText();
					newTitle = newTitle.replace("{num}", Utils.padNumber(pad, start));
					start++;
					
					cmdLineGeneral[i] += " --set title=\"" + Utils.escapeQuotes(newTitle) + "\"";
					cmdLineGeneralOpt[i] += " --set title=\"" + Utils.escapeName(newTitle) + "\"";
				} else {
					cmdLineGeneral[i] += " --set title=\"" + Utils.escapeQuotes(txtTitleGeneral.getText()) + "\"";
					cmdLineGeneralOpt[i] += " --set title=\"" + Utils.escapeName(txtTitleGeneral.getText()) + "\"";
				}
			}

			if (chbExtraCmdGeneral.isSelected() && !txtExtraCmdGeneral.getText().trim().isEmpty()) {
				cmdLineGeneral[i] += " " + txtExtraCmdGeneral.getText();
				cmdLineGeneralOpt[i] += " " + Utils.escapeName(txtExtraCmdGeneral.getText());
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
						tmpCmdLineVideo[j] += " --set name=\"" + Utils.escapeQuotes(txtNameVideo[j].getText()) + "\"";
						tmpCmdLineVideoOpt[j] += " --set name=\"" + Utils.escapeName(txtNameVideo[j].getText()) + "\"";
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
						tmpCmdLineVideoOpt[j] += " " + Utils.escapeBackslashes(txtExtraCmdVideo[j].getText());
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
				
				if (chbNumbVideo[i].isSelected() && chbEditVideo[i].isSelected()) {
					tmpText = tmpText.replace("{num}", Utils.padNumber(numPadVideo[i], numStartVideo[i]));
					tmpText2 = tmpText.replace("{num}", Utils.padNumber(numPadVideo[i], numStartVideo[i]));
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
						tmpCmdLineAudio[j] += " --set name=\"" + Utils.escapeQuotes(txtNameAudio[j].getText()) + "\"";
						tmpCmdLineAudioOpt[j] += " --set name=\"" + Utils.escapeName(txtNameAudio[j].getText()) + "\"";
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
						tmpCmdLineAudioOpt[j] += " " + Utils.escapeBackslashes(txtExtraCmdAudio[j].getText());
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
				
				if (chbNumbAudio[i].isSelected() && chbEditAudio[i].isSelected()) {
					tmpText = tmpText.replace("{num}", Utils.padNumber(numPadAudio[i], numStartAudio[i]));
					tmpText2 = tmpText.replace("{num}", Utils.padNumber(numPadAudio[i], numStartAudio[i]));
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
						tmpCmdLineSubtitle[j] += " --set name=\"" + Utils.escapeQuotes(txtNameSubtitle[j].getText()) + "\"";
						tmpCmdLineSubtitleOpt[j] += " --set name=\"" + Utils.escapeName(txtNameSubtitle[j].getText()) + "\"";
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
						tmpCmdLineSubtitleOpt[j] += " " + Utils.escapeBackslashes(txtExtraCmdSubtitle[j].getText());
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
				
				if (chbNumbSubtitle[i].isSelected() && chbEditSubtitle[i].isSelected()) {
					tmpText = tmpText.replace("{num}", Utils.padNumber(numPadSubtitle[i], numStartSubtitle[i]));
					tmpText2 = tmpText.replace("{num}", Utils.padNumber(numPadSubtitle[i], numStartSubtitle[i]));
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
				
				if (Utils.isWindows()) {
					cmdLineBatch.add("\"" + txtMkvPropExe.getText() + "\" \"" + modelFiles.get(i) + "\"" + cmdLineAll);
					cmdLineBatchOpt.add("\"" + Utils.escapeName((String) modelFiles.get(i)) + "\"" + cmdLineAllOpt);
				} else {
					cmdLineBatch.add("\"" + Utils.escapeQuotes(txtMkvPropExe.getText()) + "\" "
									 + "\"" + Utils.escapeQuotes((String) modelFiles.get(i)) + "\"" + cmdLineAll);
					
					cmdLineBatchOpt.add("\"" + Utils.escapeName((String) modelFiles.get(i)) + "\"" + cmdLineAllOpt);
				}
			}
		}
	}
	
	private void executeBatch() {
		worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				txtOutput.setText("");
				pnlTabs.setSelectedIndex(pnlTabs.getTabCount()-1);
				pnlTabs.setEnabled(false);
				btnProcessFiles.setEnabled(false);
				btnGenerateCmdLine.setEnabled(false);
				
				for (int i = 0; i < cmdLineBatch.size(); i++) {
					try {
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
						
						pb.command(txtMkvPropExe.getText(), "@options.txt");
						pb.redirectErrorStream(true);
						
						txtOutput.append("File: " + modelFiles.get(i) + "\n");
						txtOutput.append("Command line: " + cmdLineBatch.get(i) + "\n\n");
						
						proc = pb.start();
						
						StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), txtOutput);
						outputGobbler.start();
						
						proc.waitFor();
						
						optFile.delete();
						
						if (i < cmdLineBatch.size()-1) {
							txtOutput.append("--------------\n\n");
						}
						
						Thread.sleep(10);
					} catch (IOException e) {
					} catch (InterruptedException e) {
						break;						
					}
				}

				return null;
		    }
		    
		    @Override
		    protected void done() {
		    	pnlTabs.setEnabled(true);
				btnProcessFiles.setEnabled(true);
				btnGenerateCmdLine.setEnabled(true);
		    }						   
		 };
		 
		 worker.execute();
	}
	
	private void parseFiles(String[] argsArray) {
		if (argsArray.length > 0) {
			File f = null;
			
			for (String arg : argsArray) {
				try {
					f = new File(arg);
					
					if (f.exists() && !f.isDirectory() && MATROSKA_EXT_FILTER.accept(f)
						&& !modelFiles.contains(f.getCanonicalPath())) {
						modelFiles.add(modelFiles.getSize(), f.getCanonicalPath());
					}
				} catch (Exception e) {
				}
			}
		}
	}
	
	private boolean found = true;
	private boolean isExecutableInPath(final String exe) {
		worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				try {
					pb.command(exe);
					pb.redirectErrorStream(true);
					proc = pb.start();
					
					BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					proc.waitFor();
					in.close();
					
					found = true;
				} catch (IOException e) {
					found = false;
				} catch (InterruptedException e) {
				}
				
				return null;
			}
		};
		
		worker.execute();
		while (!worker.isDone()) { }

		return found;
	}
	
	/* End of command line methods */
	
	
	/* Start of INI configuration file methods */
	
	private void readIniFile() {
		Ini ini = null;

		if (iniFile.exists()) {
			try {
				ini = new Ini(iniFile);
				String exePath = ini.get("General", "mkvpropedit");
				
				if (exePath != null) {
					if (exePath.equals("mkvpropedit")) {
						chbMkvPropExeDef.setSelected(true);
						chbMkvPropExeDef.setEnabled(false);
					} else {
						txtMkvPropExe.setText(exePath);
						chbMkvPropExeDef.setSelected(false);
						chbMkvPropExeDef.setEnabled(true);
					}
				}
			} catch (InvalidFileFormatException e) {

			} catch (IOException e) {

			}
		} else if (Utils.isWindows()) {
			String exePath = getMkvPropExeFromReg();
			
			if (exePath != null) {
				txtMkvPropExe.setText(exePath);
				chbMkvPropExeDef.setSelected(false);
				chbMkvPropExeDef.setEnabled(true);
				saveIniFile(new File(exePath));
			}
		}
	}
	
	private void saveIniFile(File exeFile) {
		Ini ini = null;
		
		txtMkvPropExe.setText(exeFile.toString());
		chbMkvPropExeDef.setSelected(false);
		chbMkvPropExeDef.setEnabled(true);
		
		try {
			if (!iniFile.exists()) {
				iniFile.createNewFile();
			}
			
			ini = new Ini(iniFile);
			ini.put("General", "mkvpropedit", exeFile.toString());
			ini.store();
		}
		catch (InvalidFileFormatException e1) {
		}
		catch (IOException e1) {		
		}
	}
	
	private void defaultIniFile() {
		Ini ini = null;
		
		try {
			if (!iniFile.exists()) {
				iniFile.createNewFile();
			}
			
			ini = new Ini(iniFile);
			
			ini.put("General", "mkvpropedit", "mkvpropedit");
			
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
			
			if (exePath == null) {
				exePath = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE,
						"Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MKVtoolnix",
						"UninstallString");
			}
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		
		if (exePath != null) {
			File tmpExe = new File(exePath);
			tmpExe = new File(tmpExe.getParent()+"\\mkvpropedit.exe");
			
			if (tmpExe.exists()) {
				exePath = tmpExe.toString();
			} else {
				exePath = null;
			}
		}
		
		return exePath;
	}
	
	/* End of INI configuration file methods */
	
}
