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
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.EmptyBorder;
import org.ini4j.*;

public class JMkvpropedit {
	
	private static final String VERSION_NUMBER = "1.3.2";
	private static final int MAX_STREAMS = 30;
	private static String[] argsArray;
	
	private Process proc = null;
	private ProcessBuilder pb = new ProcessBuilder();
	private SwingWorker<Void, Void> worker = null;
	private boolean exeFound = true;
	
	private File iniFile = new File("JMkvpropedit.ini");
	private static final MkvStrings mkvStrings = new MkvStrings();
	
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
	
	
	private FileFilter EXE_EXT_FILTER = 
			new FileNameExtensionFilter("Excecutable files (*.exe)", "exe");
	
	private FileFilter MATROSKA_EXT_FILTER =
			new FileNameExtensionFilter("Matroska files (*.mkv; *.mka; *.mk3d) ", "mkv", "mka", "mk3d");
	
	private FileFilter TXT_EXT_FILTER =
			new FileNameExtensionFilter("Plain text files (*.txt)", "txt");	
	
	private FileFilter XML_EXT_FILTER =
			new FileNameExtensionFilter("XML files (*.xml)", "xml");
	
	
	private static final String[] COLUMNS_ATTACHMENTS_ADD = { "File", "Name", "Description", "MIME Type" };
	private static final double[] COLUMN_SIZES_ATTACHMENTS_ADD = { 0.35, 0.20, 0.25, 0.20 };
	private DefaultTableModel modelAttachmentsAdd = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_ADD) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	};
	
	
	private static final String[] COLUMNS_ATTACHMENTS_REPLACE = {
		"Type", "Original Value", "Replacement",
		"Name", "Description", "MIME Type" };
	private static final double[] COLUMN_SIZES_ATTACHMENTS_REPLACE = { 0.15, 0.15, 0.20, 0.20, 0.15, 0.15 };
	private DefaultTableModel modelAttachmentsReplace = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_REPLACE) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	};
	
	private static final String[] COLUMNS_ATTACHMENTS_DELETE = { "Type", "Value" };
	private static final double[] COLUMN_SIZES_ATTACHMENTS_DELETE = { 0.40, 0.60 };
	private DefaultTableModel modelAttachmentsDelete = new DefaultTableModel(null, COLUMNS_ATTACHMENTS_DELETE) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	};
	
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
	
	private String cmdLineAttachmentsAdd = null;
	private String cmdLineAttachmentsAddOpt = null;
	
	private String cmdLineAttachmentsReplace = null;
	private String cmdLineAttachmentsReplaceOpt = null;
	
	private String cmdLineAttachmentsDelete = null;
	private String cmdLineAttachmentsDeleteOpt = null;
	
	private ArrayList<String> cmdLineBatch = null;
	private ArrayList<String> cmdLineBatchOpt = null;
	
	
	// Window controls
	private Dimension frmJMkvpropeditDim = new Dimension(0, 0);
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
	
	
	//Attachments tab controls
	private JTabbedPane pnlAttachments;
	private JPanel pnlAttachAdd;
	private JScrollPane spAttachAdd;
	private JTable tblAttachAdd;
	private JPanel pnlAttachAddControls;
	private JLabel lblAttachAddFile;
	private JTextField txtAttachAddFile;
	private JButton btnBrowseAttachAddFile;
	private JLabel lblAttachAddName;
	private JTextField txtAttachAddName;
	private JLabel lblAttachAddDesc;
	private JTextField txtAttachAddDesc;
	private JLabel lblAttachAddMime;
	private JComboBox cbAttachAddMime;
	private JPanel pnlAttachAddControlsBottom;
	private JButton btnAttachAddAdd;
	private JButton btnAttachAddRemove;
	private JButton btnAttachAddEdit;
	private JButton btnAttachAddCancel;
	
	private JPanel pnlAttachReplace;
	private JScrollPane spAttachReplace;
	private JTable tblAttachReplace;
	private JPanel pnlAttachReplaceControls;
	private JLabel lblAttachReplaceType;
	private JPanel pnlAttachReplaceType;
	private ButtonGroup bgAttachReplaceType = new ButtonGroup();
	private JRadioButton rbAttachReplaceID;
	private JRadioButton rbAttachReplaceName;
	private JRadioButton rbAttachReplaceMime;
	private JPanel pnlAttachReplaceOrig;
	private JLabel lblAttachReplaceOrig;
	private JTextField txtAttachReplaceOrig;
	private JComboBox cbAttachReplaceOrig;
	private JLabel lblAttachReplaceNew;
	private JTextField txtAttachReplaceNew;
	private JButton btnAttachReplaceNewBrowse;
	private JLabel lblAttachReplaceName;
	private JTextField txtAttachReplaceName;
	private JLabel lblAttachReplaceDesc;
	private JTextField txtAttachReplaceDesc;
	private JLabel lblAttachReplaceMime;
	private JComboBox cbAttachReplaceMime;
	private JPanel pnlAttachReplaceControlsBottom;
	private JButton btnAttachReplaceAdd;
	private JButton btnAttachReplaceEdit;
	private JButton btnAttachReplaceRemove;
	private JButton btnAttachReplaceCancel;
	
	private JPanel pnlAttachDelete;
	private JScrollPane spAttachDelete;
	private JTable tblAttachDelete;
	private JPanel pnlAttachDeleteControls;
	private ButtonGroup bgAttachDeleteType = new ButtonGroup();
	private JLabel lblAttachDeleteType;
	private JPanel pnlAttachDeleteType;
	private JRadioButton rbAttachDeleteName;
	private JRadioButton rbAttachDeleteID;
	private JRadioButton rbAttachDeleteMime;
	private JLabel lblAttachDeleteValue;
	private JPanel pnlAttachDeleteValue;
	private JTextField txtAttachDeleteValue;
	private JComboBox cbAttachDeleteValue;
	private JPanel pnlAttachDeleteControlsBottom;
	private JButton btnAttachDeleteAdd;
	private JButton btnAttachDeleteEdit;
	private JButton btnAttachDeleteRemove;
	private JButton btnAttachDeleteCancel;
	
	
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
		frmJMkvpropedit.setBounds(100, 100, 760, 500);
		frmJMkvpropedit.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setFileHidingEnabled(true);
		
		pnlTabs = new JTabbedPane(JTabbedPane.TOP);
		pnlTabs.setBorder(new EmptyBorder(10, 10, 0, 10));
		frmJMkvpropedit.getContentPane().add(pnlTabs, BorderLayout.CENTER);
		
		JPanel pnlInput = new JPanel();
		pnlInput.setBorder(new EmptyBorder(10, 10, 10, 0));
		pnlTabs.addTab("Input", null, pnlInput, null);
		pnlInput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane spFiles = new JScrollPane();
		spFiles.setViewportBorder(null);
		pnlInput.add(spFiles);
		
		modelFiles = new DefaultListModel();
		listFiles = new JList(modelFiles);
		spFiles.setViewportView(listFiles);
		
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
		pnlTabs.addTab("Subtitles", null, pnlSubtitle, null);
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
		
		pnlAttachments = new JTabbedPane(JTabbedPane.TOP);
		pnlTabs.addTab("Attachments", null, pnlAttachments, null);
		
		pnlAttachAdd = new JPanel();
		pnlAttachments.addTab("Add Attachments", null, pnlAttachAdd, null);
		pnlAttachAdd.setLayout(new BorderLayout(0, 0));
		
		spAttachAdd = new JScrollPane();
		pnlAttachAdd.add(spAttachAdd, BorderLayout.CENTER);
		
		tblAttachAdd = new JTable();
		tblAttachAdd.setShowGrid(false);
		tblAttachAdd.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblAttachAdd.setModel(modelAttachmentsAdd);
		tblAttachAdd.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblAttachAdd.setAutoscrolls(false);
		tblAttachAdd.setFillsViewportHeight(true);
		
		spAttachAdd.setViewportView(tblAttachAdd);
		
		pnlAttachAddControls = new JPanel();
		pnlAttachAddControls.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnlAttachAdd.add(pnlAttachAddControls, BorderLayout.SOUTH);
		GridBagLayout gbl_pnlAttachAddControls = new GridBagLayout();
		gbl_pnlAttachAddControls.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlAttachAddControls.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlAttachAddControls.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlAttachAddControls.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		pnlAttachAddControls.setLayout(gbl_pnlAttachAddControls);
		
		lblAttachAddFile = new JLabel("File:");
		GridBagConstraints gbc_lblAttachAddFile = new GridBagConstraints();
		gbc_lblAttachAddFile.anchor = GridBagConstraints.WEST;
		gbc_lblAttachAddFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachAddFile.gridx = 0;
		gbc_lblAttachAddFile.gridy = 0;
		pnlAttachAddControls.add(lblAttachAddFile, gbc_lblAttachAddFile);
		
		txtAttachAddFile = new JTextField();
		txtAttachAddFile.setEditable(false);
		GridBagConstraints gbc_txtAttachAddFile = new GridBagConstraints();
		gbc_txtAttachAddFile.insets = new Insets(0, 0, 5, 5);
		gbc_txtAttachAddFile.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAttachAddFile.gridx = 1;
		gbc_txtAttachAddFile.gridy = 0;
		pnlAttachAddControls.add(txtAttachAddFile, gbc_txtAttachAddFile);
		txtAttachAddFile.setColumns(10);
		
		btnBrowseAttachAddFile = new JButton("Browse...");
		GridBagConstraints gbc_btnBrowseAttachAddFile = new GridBagConstraints();
		gbc_btnBrowseAttachAddFile.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowseAttachAddFile.gridx = 2;
		gbc_btnBrowseAttachAddFile.gridy = 0;
		pnlAttachAddControls.add(btnBrowseAttachAddFile, gbc_btnBrowseAttachAddFile);
		
		lblAttachAddName = new JLabel("Name:");
		GridBagConstraints gbc_lblAttachAddName = new GridBagConstraints();
		gbc_lblAttachAddName.anchor = GridBagConstraints.WEST;
		gbc_lblAttachAddName.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachAddName.gridx = 0;
		gbc_lblAttachAddName.gridy = 1;
		pnlAttachAddControls.add(lblAttachAddName, gbc_lblAttachAddName);
		
		txtAttachAddName = new JTextField();
		GridBagConstraints gbc_txtAttachAddName = new GridBagConstraints();
		gbc_txtAttachAddName.insets = new Insets(0, 0, 5, 5);
		gbc_txtAttachAddName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAttachAddName.gridx = 1;
		gbc_txtAttachAddName.gridy = 1;
		pnlAttachAddControls.add(txtAttachAddName, gbc_txtAttachAddName);
		txtAttachAddName.setColumns(10);
		
		lblAttachAddDesc = new JLabel("Description:");
		GridBagConstraints gbc_lblAttachAddDesc = new GridBagConstraints();
		gbc_lblAttachAddDesc.anchor = GridBagConstraints.EAST;
		gbc_lblAttachAddDesc.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachAddDesc.gridx = 0;
		gbc_lblAttachAddDesc.gridy = 2;
		pnlAttachAddControls.add(lblAttachAddDesc, gbc_lblAttachAddDesc);
		
		txtAttachAddDesc = new JTextField();
		GridBagConstraints gbc_txtAttachAddDesc = new GridBagConstraints();
		gbc_txtAttachAddDesc.insets = new Insets(0, 0, 5, 5);
		gbc_txtAttachAddDesc.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAttachAddDesc.gridx = 1;
		gbc_txtAttachAddDesc.gridy = 2;
		pnlAttachAddControls.add(txtAttachAddDesc, gbc_txtAttachAddDesc);
		txtAttachAddDesc.setColumns(10);
		
		lblAttachAddMime = new JLabel("MIME Type:");
		GridBagConstraints gbc_lblAttachAddMime = new GridBagConstraints();
		gbc_lblAttachAddMime.anchor = GridBagConstraints.EAST;
		gbc_lblAttachAddMime.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachAddMime.gridx = 0;
		gbc_lblAttachAddMime.gridy = 3;
		pnlAttachAddControls.add(lblAttachAddMime, gbc_lblAttachAddMime);
		
		cbAttachAddMime = new JComboBox();
		cbAttachAddMime.setModel(new DefaultComboBoxModel(mkvStrings.getMimeType()));
		GridBagConstraints gbc_cbAttachAddMime = new GridBagConstraints();
		gbc_cbAttachAddMime.insets = new Insets(0, 0, 5, 5);
		gbc_cbAttachAddMime.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbAttachAddMime.gridx = 1;
		gbc_cbAttachAddMime.gridy = 3;
		pnlAttachAddControls.add(cbAttachAddMime, gbc_cbAttachAddMime);
		
		pnlAttachAddControlsBottom = new JPanel();
		GridBagConstraints gbc_pnlAttachAddControlsBottom = new GridBagConstraints();
		gbc_pnlAttachAddControlsBottom.insets = new Insets(0, 0, 0, 5);
		gbc_pnlAttachAddControlsBottom.fill = GridBagConstraints.BOTH;
		gbc_pnlAttachAddControlsBottom.gridx = 1;
		gbc_pnlAttachAddControlsBottom.gridy = 4;
		pnlAttachAddControls.add(pnlAttachAddControlsBottom, gbc_pnlAttachAddControlsBottom);
		GridBagLayout gbl_pnlAttachAddControlsBottom = new GridBagLayout();
		gbl_pnlAttachAddControlsBottom.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlAttachAddControlsBottom.rowHeights = new int[]{0, 0};
		gbl_pnlAttachAddControlsBottom.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlAttachAddControlsBottom.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlAttachAddControlsBottom.setLayout(gbl_pnlAttachAddControlsBottom);
		
		btnAttachAddAdd = new JButton("Add");
		GridBagConstraints gbc_btnAttachAddAdd = new GridBagConstraints();
		gbc_btnAttachAddAdd.insets = new Insets(0, 0, 0, 5);
		gbc_btnAttachAddAdd.gridx = 0;
		gbc_btnAttachAddAdd.gridy = 0;
		pnlAttachAddControlsBottom.add(btnAttachAddAdd, gbc_btnAttachAddAdd);
		
		btnAttachAddEdit = new JButton("Edit");
		btnAttachAddEdit.setEnabled(false);
		GridBagConstraints gbc_btnAttachAddEdit = new GridBagConstraints();
		gbc_btnAttachAddEdit.insets = new Insets(0, 0, 0, 5);
		gbc_btnAttachAddEdit.gridx = 1;
		gbc_btnAttachAddEdit.gridy = 0;
		pnlAttachAddControlsBottom.add(btnAttachAddEdit, gbc_btnAttachAddEdit);
		
		btnAttachAddRemove = new JButton("Remove");
		btnAttachAddRemove.setEnabled(false);
		GridBagConstraints gbc_btnAttachAddRemove = new GridBagConstraints();
		gbc_btnAttachAddRemove.insets = new Insets(0, 0, 0, 5);
		gbc_btnAttachAddRemove.anchor = GridBagConstraints.SOUTH;
		gbc_btnAttachAddRemove.gridx = 2;
		gbc_btnAttachAddRemove.gridy = 0;
		pnlAttachAddControlsBottom.add(btnAttachAddRemove, gbc_btnAttachAddRemove);
		
		btnAttachAddCancel = new JButton("Cancel");
		btnAttachAddCancel.setEnabled(false);
		GridBagConstraints gbc_btnAttachAddCancel = new GridBagConstraints();
		gbc_btnAttachAddCancel.gridx = 3;
		gbc_btnAttachAddCancel.gridy = 0;
		pnlAttachAddControlsBottom.add(btnAttachAddCancel, gbc_btnAttachAddCancel);
		
		pnlAttachReplace = new JPanel();
		pnlAttachments.addTab("Replace Attachments", null, pnlAttachReplace, null);
		pnlAttachReplace.setLayout(new BorderLayout(0, 0));
		
		spAttachReplace = new JScrollPane();
		pnlAttachReplace.add(spAttachReplace, BorderLayout.CENTER);
		
		tblAttachReplace = new JTable();
		tblAttachReplace.setShowGrid(false);
		tblAttachReplace.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblAttachReplace.setModel(modelAttachmentsReplace);
		tblAttachReplace.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblAttachReplace.setAutoscrolls(false);
		tblAttachReplace.setFillsViewportHeight(true);
		spAttachReplace.setViewportView(tblAttachReplace);
		
		pnlAttachReplaceControls = new JPanel();
		pnlAttachReplaceControls.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnlAttachReplace.add(pnlAttachReplaceControls, BorderLayout.SOUTH);
		GridBagLayout gbl_pnlAttachReplaceControls = new GridBagLayout();
		gbl_pnlAttachReplaceControls.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlAttachReplaceControls.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlAttachReplaceControls.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlAttachReplaceControls.rowWeights = new double[]{1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlAttachReplaceControls.setLayout(gbl_pnlAttachReplaceControls);
		
		lblAttachReplaceType = new JLabel("Type:");
		GridBagConstraints gbc_lblAttachReplaceType = new GridBagConstraints();
		gbc_lblAttachReplaceType.anchor = GridBagConstraints.WEST;
		gbc_lblAttachReplaceType.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachReplaceType.gridx = 0;
		gbc_lblAttachReplaceType.gridy = 0;
		pnlAttachReplaceControls.add(lblAttachReplaceType, gbc_lblAttachReplaceType);
		
		pnlAttachReplaceType = new JPanel();
		GridBagConstraints gbc_pnlAttachReplaceType = new GridBagConstraints();
		gbc_pnlAttachReplaceType.insets = new Insets(0, 0, 5, 5);
		gbc_pnlAttachReplaceType.fill = GridBagConstraints.BOTH;
		gbc_pnlAttachReplaceType.gridx = 1;
		gbc_pnlAttachReplaceType.gridy = 0;
		pnlAttachReplaceControls.add(pnlAttachReplaceType, gbc_pnlAttachReplaceType);
		GridBagLayout gbl_pnlAttachReplaceType = new GridBagLayout();
		gbl_pnlAttachReplaceType.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlAttachReplaceType.rowHeights = new int[]{0, 0};
		gbl_pnlAttachReplaceType.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlAttachReplaceType.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlAttachReplaceType.setLayout(gbl_pnlAttachReplaceType);
		
		rbAttachReplaceName = new JRadioButton("Attachment name");
		rbAttachReplaceName.setSelected(true);
		GridBagConstraints gbc_rbAttachReplaceName = new GridBagConstraints();
		gbc_rbAttachReplaceName.insets = new Insets(0, 0, 0, 5);
		gbc_rbAttachReplaceName.gridx = 0;
		gbc_rbAttachReplaceName.gridy = 0;
		pnlAttachReplaceType.add(rbAttachReplaceName, gbc_rbAttachReplaceName);
		bgAttachReplaceType.add(rbAttachReplaceName);
		
		rbAttachReplaceID = new JRadioButton("Attachment ID");
		GridBagConstraints gbc_rbAttachReplaceID = new GridBagConstraints();
		gbc_rbAttachReplaceID.insets = new Insets(0, 0, 0, 5);
		gbc_rbAttachReplaceID.gridx = 1;
		gbc_rbAttachReplaceID.gridy = 0;
		pnlAttachReplaceType.add(rbAttachReplaceID, gbc_rbAttachReplaceID);
		bgAttachReplaceType.add(rbAttachReplaceID);
		
		rbAttachReplaceMime = new JRadioButton("Attachment(s) MIME Type");
		GridBagConstraints gbc_rbAttachReplaceMime = new GridBagConstraints();
		gbc_rbAttachReplaceMime.gridx = 2;
		gbc_rbAttachReplaceMime.gridy = 0;
		pnlAttachReplaceType.add(rbAttachReplaceMime, gbc_rbAttachReplaceMime);
		bgAttachReplaceType.add(rbAttachReplaceMime);
		
		lblAttachReplaceOrig = new JLabel("Original value:");
		GridBagConstraints gbc_lblAttachReplaceOrig = new GridBagConstraints();
		gbc_lblAttachReplaceOrig.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachReplaceOrig.anchor = GridBagConstraints.WEST;
		gbc_lblAttachReplaceOrig.gridx = 0;
		gbc_lblAttachReplaceOrig.gridy = 1;
		pnlAttachReplaceControls.add(lblAttachReplaceOrig, gbc_lblAttachReplaceOrig);
		
		pnlAttachReplaceOrig = new JPanel();
		GridBagConstraints gbc_pnlAttachReplaceOrig = new GridBagConstraints();
		gbc_pnlAttachReplaceOrig.insets = new Insets(0, 0, 5, 5);
		gbc_pnlAttachReplaceOrig.fill = GridBagConstraints.BOTH;
		gbc_pnlAttachReplaceOrig.gridx = 1;
		gbc_pnlAttachReplaceOrig.gridy = 1;
		pnlAttachReplaceControls.add(pnlAttachReplaceOrig, gbc_pnlAttachReplaceOrig);
		pnlAttachReplaceOrig.setLayout(new CardLayout(0, 0));
		
		txtAttachReplaceOrig = new JTextField();
		pnlAttachReplaceOrig.add(txtAttachReplaceOrig, "txtAttachReplaceOrig");
		txtAttachReplaceOrig.setColumns(10);
		
		cbAttachReplaceOrig = new JComboBox();
		ArrayList<String> mimeList = mkvStrings.getMimeTypeList();
		mimeList.remove(0);
		cbAttachReplaceOrig.setModel(new DefaultComboBoxModel(mimeList.toArray()));
		cbAttachReplaceOrig.setVisible(false);
		pnlAttachReplaceOrig.add(cbAttachReplaceOrig, "cbAttachReplaceOrig");
		
		lblAttachReplaceNew = new JLabel("Replacement:");
		GridBagConstraints gbc_lblAttachReplaceNew = new GridBagConstraints();
		gbc_lblAttachReplaceNew.anchor = GridBagConstraints.WEST;
		gbc_lblAttachReplaceNew.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachReplaceNew.gridx = 0;
		gbc_lblAttachReplaceNew.gridy = 2;
		pnlAttachReplaceControls.add(lblAttachReplaceNew, gbc_lblAttachReplaceNew);
		
		txtAttachReplaceNew = new JTextField();
		txtAttachReplaceNew.setEditable(false);
		GridBagConstraints gbc_txtAttachReplaceNew = new GridBagConstraints();
		gbc_txtAttachReplaceNew.insets = new Insets(0, 0, 5, 5);
		gbc_txtAttachReplaceNew.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAttachReplaceNew.gridx = 1;
		gbc_txtAttachReplaceNew.gridy = 2;
		pnlAttachReplaceControls.add(txtAttachReplaceNew, gbc_txtAttachReplaceNew);
		txtAttachReplaceNew.setColumns(10);
		
		btnAttachReplaceNewBrowse = new JButton("Browse....");
		GridBagConstraints gbc_btnAttachReplaceNewBrowse = new GridBagConstraints();
		gbc_btnAttachReplaceNewBrowse.insets = new Insets(0, 0, 5, 0);
		gbc_btnAttachReplaceNewBrowse.gridx = 2;
		gbc_btnAttachReplaceNewBrowse.gridy = 2;
		pnlAttachReplaceControls.add(btnAttachReplaceNewBrowse, gbc_btnAttachReplaceNewBrowse);
		
		lblAttachReplaceName = new JLabel("Name:");
		GridBagConstraints gbc_lblAttachReplaceName = new GridBagConstraints();
		gbc_lblAttachReplaceName.anchor = GridBagConstraints.WEST;
		gbc_lblAttachReplaceName.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachReplaceName.gridx = 0;
		gbc_lblAttachReplaceName.gridy = 3;
		pnlAttachReplaceControls.add(lblAttachReplaceName, gbc_lblAttachReplaceName);
		
		txtAttachReplaceName = new JTextField();
		txtAttachReplaceName.setColumns(10);
		GridBagConstraints gbc_txtAttachReplaceName = new GridBagConstraints();
		gbc_txtAttachReplaceName.insets = new Insets(0, 0, 5, 5);
		gbc_txtAttachReplaceName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAttachReplaceName.gridx = 1;
		gbc_txtAttachReplaceName.gridy = 3;
		pnlAttachReplaceControls.add(txtAttachReplaceName, gbc_txtAttachReplaceName);
		
		lblAttachReplaceDesc = new JLabel("Description:");
		GridBagConstraints gbc_lblAttachReplaceDesc = new GridBagConstraints();
		gbc_lblAttachReplaceDesc.anchor = GridBagConstraints.WEST;
		gbc_lblAttachReplaceDesc.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachReplaceDesc.gridx = 0;
		gbc_lblAttachReplaceDesc.gridy = 4;
		pnlAttachReplaceControls.add(lblAttachReplaceDesc, gbc_lblAttachReplaceDesc);
		
		txtAttachReplaceDesc = new JTextField();
		txtAttachReplaceDesc.setColumns(10);
		GridBagConstraints gbc_txtAttachReplaceDesc = new GridBagConstraints();
		gbc_txtAttachReplaceDesc.insets = new Insets(0, 0, 5, 5);
		gbc_txtAttachReplaceDesc.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtAttachReplaceDesc.gridx = 1;
		gbc_txtAttachReplaceDesc.gridy = 4;
		pnlAttachReplaceControls.add(txtAttachReplaceDesc, gbc_txtAttachReplaceDesc);
		
		lblAttachReplaceMime = new JLabel("MIME Type:");
		GridBagConstraints gbc_lblAttachReplaceMime = new GridBagConstraints();
		gbc_lblAttachReplaceMime.anchor = GridBagConstraints.WEST;
		gbc_lblAttachReplaceMime.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachReplaceMime.gridx = 0;
		gbc_lblAttachReplaceMime.gridy = 5;
		pnlAttachReplaceControls.add(lblAttachReplaceMime, gbc_lblAttachReplaceMime);
		
		cbAttachReplaceMime = new JComboBox();
		cbAttachReplaceMime.setModel(new DefaultComboBoxModel(mkvStrings.getMimeType()));
		GridBagConstraints gbc_cbAttachReplaceMime = new GridBagConstraints();
		gbc_cbAttachReplaceMime.insets = new Insets(0, 0, 5, 5);
		gbc_cbAttachReplaceMime.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbAttachReplaceMime.gridx = 1;
		gbc_cbAttachReplaceMime.gridy = 5;
		pnlAttachReplaceControls.add(cbAttachReplaceMime, gbc_cbAttachReplaceMime);
		
		pnlAttachReplaceControlsBottom = new JPanel();
		GridBagConstraints gbc_pnlAttachReplaceControlsBottom = new GridBagConstraints();
		gbc_pnlAttachReplaceControlsBottom.anchor = GridBagConstraints.WEST;
		gbc_pnlAttachReplaceControlsBottom.insets = new Insets(0, 0, 0, 5);
		gbc_pnlAttachReplaceControlsBottom.fill = GridBagConstraints.VERTICAL;
		gbc_pnlAttachReplaceControlsBottom.gridx = 1;
		gbc_pnlAttachReplaceControlsBottom.gridy = 6;
		pnlAttachReplaceControls.add(pnlAttachReplaceControlsBottom, gbc_pnlAttachReplaceControlsBottom);
		GridBagLayout gbl_pnlAttachReplaceControlsBottom = new GridBagLayout();
		gbl_pnlAttachReplaceControlsBottom.columnWidths = new int[] {0, 0, 0, 0};
		gbl_pnlAttachReplaceControlsBottom.rowHeights = new int[] {0, 0};
		gbl_pnlAttachReplaceControlsBottom.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0};
		gbl_pnlAttachReplaceControlsBottom.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlAttachReplaceControlsBottom.setLayout(gbl_pnlAttachReplaceControlsBottom);
		
		btnAttachReplaceAdd = new JButton("Add");
		GridBagConstraints gbc_btnAttachReplaceAdd = new GridBagConstraints();
		gbc_btnAttachReplaceAdd.insets = new Insets(0, 0, 0, 5);
		gbc_btnAttachReplaceAdd.gridx = 0;
		gbc_btnAttachReplaceAdd.gridy = 0;
		pnlAttachReplaceControlsBottom.add(btnAttachReplaceAdd, gbc_btnAttachReplaceAdd);
		
		btnAttachReplaceEdit = new JButton("Edit");
		btnAttachReplaceEdit.setEnabled(false);
		GridBagConstraints gbc_btnAttachReplaceEdit = new GridBagConstraints();
		gbc_btnAttachReplaceEdit.insets = new Insets(0, 0, 0, 5);
		gbc_btnAttachReplaceEdit.gridx = 1;
		gbc_btnAttachReplaceEdit.gridy = 0;
		pnlAttachReplaceControlsBottom.add(btnAttachReplaceEdit, gbc_btnAttachReplaceEdit);
		
		btnAttachReplaceRemove = new JButton("Remove");
		btnAttachReplaceRemove.setEnabled(false);
		GridBagConstraints gbc_btnAttachReplaceRemove = new GridBagConstraints();
		gbc_btnAttachReplaceRemove.anchor = GridBagConstraints.SOUTH;
		gbc_btnAttachReplaceRemove.insets = new Insets(0, 0, 0, 5);
		gbc_btnAttachReplaceRemove.gridx = 2;
		gbc_btnAttachReplaceRemove.gridy = 0;
		pnlAttachReplaceControlsBottom.add(btnAttachReplaceRemove, gbc_btnAttachReplaceRemove);
		
		btnAttachReplaceCancel = new JButton("Cancel");
		btnAttachReplaceCancel.setEnabled(false);
		GridBagConstraints gbc_btnAttachReplaceCancel = new GridBagConstraints();
		gbc_btnAttachReplaceCancel.gridx = 3;
		gbc_btnAttachReplaceCancel.gridy = 0;
		pnlAttachReplaceControlsBottom.add(btnAttachReplaceCancel, gbc_btnAttachReplaceCancel);
		
		pnlAttachDelete = new JPanel();
		pnlAttachments.addTab("Delete Attachments", null, pnlAttachDelete, null);
		pnlAttachDelete.setLayout(new BorderLayout(0, 0));
		
		spAttachDelete = new JScrollPane();
		pnlAttachDelete.add(spAttachDelete, BorderLayout.CENTER);
		
		tblAttachDelete = new JTable();
		tblAttachDelete.setShowGrid(false);
		tblAttachDelete.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblAttachDelete.setModel(modelAttachmentsDelete);
		tblAttachDelete.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblAttachDelete.setAutoscrolls(false);
		tblAttachDelete.setFillsViewportHeight(true);
		spAttachDelete.setViewportView(tblAttachDelete);
		
		pnlAttachDeleteControls = new JPanel();
		pnlAttachDeleteControls.setBorder(new EmptyBorder(5, 5, 5, 5));
		pnlAttachDelete.add(pnlAttachDeleteControls, BorderLayout.SOUTH);
		GridBagLayout gbl_pnlAttachDeleteControls = new GridBagLayout();
		gbl_pnlAttachDeleteControls.columnWidths = new int[]{0, 0, 0};
		gbl_pnlAttachDeleteControls.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlAttachDeleteControls.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlAttachDeleteControls.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		pnlAttachDeleteControls.setLayout(gbl_pnlAttachDeleteControls);
		
		lblAttachDeleteType = new JLabel("Type:");
		GridBagConstraints gbc_lblAttachDeleteType = new GridBagConstraints();
		gbc_lblAttachDeleteType.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachDeleteType.gridx = 0;
		gbc_lblAttachDeleteType.gridy = 0;
		pnlAttachDeleteControls.add(lblAttachDeleteType, gbc_lblAttachDeleteType);
		
		pnlAttachDeleteType = new JPanel();
		GridBagConstraints gbc_pnlAttachDeleteType = new GridBagConstraints();
		gbc_pnlAttachDeleteType.anchor = GridBagConstraints.WEST;
		gbc_pnlAttachDeleteType.insets = new Insets(0, 0, 5, 0);
		gbc_pnlAttachDeleteType.fill = GridBagConstraints.VERTICAL;
		gbc_pnlAttachDeleteType.gridx = 1;
		gbc_pnlAttachDeleteType.gridy = 0;
		pnlAttachDeleteControls.add(pnlAttachDeleteType, gbc_pnlAttachDeleteType);
		GridBagLayout gbl_pnlAttachDeleteType = new GridBagLayout();
		gbl_pnlAttachDeleteType.columnWidths = new int[] {0, 0, 0};
		gbl_pnlAttachDeleteType.rowHeights = new int[] {0, 0};
		gbl_pnlAttachDeleteType.columnWeights = new double[]{0.0, 0.0, 0.0};
		gbl_pnlAttachDeleteType.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlAttachDeleteType.setLayout(gbl_pnlAttachDeleteType);
		
		rbAttachDeleteName = new JRadioButton("Attachment name");
		rbAttachDeleteName.setSelected(true);
		GridBagConstraints gbc_rbAttachDeleteName = new GridBagConstraints();
		gbc_rbAttachDeleteName.insets = new Insets(0, 0, 0, 5);
		gbc_rbAttachDeleteName.gridx = 0;
		gbc_rbAttachDeleteName.gridy = 0;
		pnlAttachDeleteType.add(rbAttachDeleteName, gbc_rbAttachDeleteName);
		bgAttachDeleteType.add(rbAttachDeleteName);
		
		rbAttachDeleteID = new JRadioButton("Attachment ID");
		GridBagConstraints gbc_rbAttachDeleteID = new GridBagConstraints();
		gbc_rbAttachDeleteID.insets = new Insets(0, 0, 0, 5);
		gbc_rbAttachDeleteID.gridx = 1;
		gbc_rbAttachDeleteID.gridy = 0;
		pnlAttachDeleteType.add(rbAttachDeleteID, gbc_rbAttachDeleteID);
		bgAttachDeleteType.add(rbAttachDeleteID);
		
		rbAttachDeleteMime = new JRadioButton("Attachment(s) MIME Type");
		GridBagConstraints gbc_rbAttachDeleteMime = new GridBagConstraints();
		gbc_rbAttachDeleteMime.gridx = 2;
		gbc_rbAttachDeleteMime.gridy = 0;
		pnlAttachDeleteType.add(rbAttachDeleteMime, gbc_rbAttachDeleteMime);
		bgAttachDeleteType.add(rbAttachDeleteMime);
		
		lblAttachDeleteValue = new JLabel("Value:");
		GridBagConstraints gbc_lblAttachDeleteValue = new GridBagConstraints();
		gbc_lblAttachDeleteValue.anchor = GridBagConstraints.EAST;
		gbc_lblAttachDeleteValue.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttachDeleteValue.gridx = 0;
		gbc_lblAttachDeleteValue.gridy = 1;
		pnlAttachDeleteControls.add(lblAttachDeleteValue, gbc_lblAttachDeleteValue);
		
		pnlAttachDeleteValue = new JPanel();
		GridBagConstraints gbc_pnlAttachDeleteValue = new GridBagConstraints();
		gbc_pnlAttachDeleteValue.insets = new Insets(0, 0, 5, 0);
		gbc_pnlAttachDeleteValue.fill = GridBagConstraints.BOTH;
		gbc_pnlAttachDeleteValue.gridx = 1;
		gbc_pnlAttachDeleteValue.gridy = 1;
		pnlAttachDeleteControls.add(pnlAttachDeleteValue, gbc_pnlAttachDeleteValue);
		pnlAttachDeleteValue.setLayout(new CardLayout(0, 0));
		
		txtAttachDeleteValue = new JTextField();
		pnlAttachDeleteValue.add(txtAttachDeleteValue, "txtAttachDeleteValue");
		txtAttachDeleteValue.setColumns(10);
		
		cbAttachDeleteValue = new JComboBox();
		cbAttachDeleteValue.setVisible(false);
		cbAttachDeleteValue.setModel(new DefaultComboBoxModel(mimeList.toArray()));
		pnlAttachDeleteValue.add(cbAttachDeleteValue, "cbAttachDeleteValue");
		
		pnlAttachDeleteControlsBottom = new JPanel();
		GridBagConstraints gbc_pnlAttachDeleteControlsBottom = new GridBagConstraints();
		gbc_pnlAttachDeleteControlsBottom.fill = GridBagConstraints.BOTH;
		gbc_pnlAttachDeleteControlsBottom.gridx = 1;
		gbc_pnlAttachDeleteControlsBottom.gridy = 2;
		pnlAttachDeleteControls.add(pnlAttachDeleteControlsBottom, gbc_pnlAttachDeleteControlsBottom);
		GridBagLayout gbl_pnlAttachDeleteControlsBottom = new GridBagLayout();
		gbl_pnlAttachDeleteControlsBottom.columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlAttachDeleteControlsBottom.rowHeights = new int[] {0, 0};
		gbl_pnlAttachDeleteControlsBottom.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlAttachDeleteControlsBottom.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlAttachDeleteControlsBottom.setLayout(gbl_pnlAttachDeleteControlsBottom);
		
		btnAttachDeleteAdd = new JButton("Add");
		GridBagConstraints gbc_btnAttachDeleteAdd = new GridBagConstraints();
		gbc_btnAttachDeleteAdd.insets = new Insets(0, 0, 5, 5);
		gbc_btnAttachDeleteAdd.gridx = 0;
		gbc_btnAttachDeleteAdd.gridy = 0;
		pnlAttachDeleteControlsBottom.add(btnAttachDeleteAdd, gbc_btnAttachDeleteAdd);
		
		btnAttachDeleteEdit = new JButton("Edit");
		btnAttachDeleteEdit.setEnabled(false);
		GridBagConstraints gbc_btnAttachDeleteEdit = new GridBagConstraints();
		gbc_btnAttachDeleteEdit.insets = new Insets(0, 0, 5, 5);
		gbc_btnAttachDeleteEdit.gridx = 1;
		gbc_btnAttachDeleteEdit.gridy = 0;
		pnlAttachDeleteControlsBottom.add(btnAttachDeleteEdit, gbc_btnAttachDeleteEdit);
		
		btnAttachDeleteRemove = new JButton("Remove");
		btnAttachDeleteRemove.setEnabled(false);
		GridBagConstraints gbc_btnAttachDeleteRemove = new GridBagConstraints();
		gbc_btnAttachDeleteRemove.anchor = GridBagConstraints.SOUTH;
		gbc_btnAttachDeleteRemove.insets = new Insets(0, 0, 5, 5);
		gbc_btnAttachDeleteRemove.gridx = 2;
		gbc_btnAttachDeleteRemove.gridy = 0;
		pnlAttachDeleteControlsBottom.add(btnAttachDeleteRemove, gbc_btnAttachDeleteRemove);
		
		btnAttachDeleteCancel = new JButton("Cancel");
		btnAttachDeleteCancel.setEnabled(false);
		GridBagConstraints gbc_btnAttachDeleteCancel = new GridBagConstraints();
		gbc_btnAttachDeleteCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnAttachDeleteCancel.gridx = 3;
		gbc_btnAttachDeleteCancel.gridy = 0;
		pnlAttachDeleteControlsBottom.add(btnAttachDeleteCancel, gbc_btnAttachDeleteCancel);
		
		JPanel pnlOutput = new JPanel();
		pnlOutput.setBorder(new EmptyBorder(10, 10, 10, 10));
		pnlTabs.addTab("Output", null, pnlOutput, null);
		pnlOutput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane spOutput = new JScrollPane();
		pnlOutput.add(spOutput, BorderLayout.CENTER);
		
		txtOutput = new JTextArea();
		txtOutput.setLineWrap(true);
		txtOutput.setEditable(false);
		spOutput.setViewportView(txtOutput);
		
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
		Utils.addRCMenuMouseListener(txtAttachAddFile);
		Utils.addRCMenuMouseListener(txtAttachAddName);
		Utils.addRCMenuMouseListener(txtAttachAddDesc);
		Utils.addRCMenuMouseListener(txtAttachReplaceOrig);
		Utils.addRCMenuMouseListener(txtAttachReplaceNew);
		Utils.addRCMenuMouseListener(txtAttachReplaceName);
		Utils.addRCMenuMouseListener(txtAttachReplaceDesc);
		Utils.addRCMenuMouseListener(txtAttachDeleteValue);
		Utils.addRCMenuMouseListener(txtOutput);
		
		/* End of mouse events for right-click menu */
		
		
		frmJMkvpropedit.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				// Resize the window to make sure the components fit
				//frmJMkvpropedit.pack();
				
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
		
		frmJMkvpropedit.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// Check if window width changed before resizing columns
				if (frmJMkvpropedit.getWidth() != frmJMkvpropeditDim.getWidth()) {
					resizeColumns(tblAttachAdd, COLUMN_SIZES_ATTACHMENTS_ADD);
					resizeColumns(tblAttachReplace, COLUMN_SIZES_ATTACHMENTS_REPLACE);
					resizeColumns(tblAttachDelete, COLUMN_SIZES_ATTACHMENTS_DELETE);
				}
				
				// Store new dimensions
				frmJMkvpropeditDim = new Dimension(frmJMkvpropedit.getWidth(), frmJMkvpropedit.getHeight());
			}
		});
		
		
		new FileDrop(listFiles, new FileDrop.Listener() {
        	public void filesDropped(File[] files) {
        		for (int i = 0; i < files.length; i++) {
        			try {
        				if (!modelFiles.contains(files[i].getCanonicalPath()) &&
        					MATROSKA_EXT_FILTER.accept(files[i]) && !files[i].isDirectory()) {
        					modelFiles.add(modelFiles.getSize(), files[i].getCanonicalPath());
        				}
        			} catch(IOException e) {
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
				chooser.setAcceptAllFileFilterUsed(false);
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
				chooser.setAcceptAllFileFilterUsed(false);
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
				chooser.setAcceptAllFileFilterUsed(false);
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
				chooser.setAcceptAllFileFilterUsed(false);
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
		
		new FileDrop(txtAttachAddFile, new FileDrop.Listener() {
        	public void filesDropped(File[] files) {
        		try {
    				if (!files[0].isDirectory()) {
    					txtAttachAddFile.setText(files[0].getCanonicalPath());
    				}
    			} catch(IOException e) {
    			}
        	}
        });
		
		btnBrowseAttachAddFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select attachment");
				chooser.setMultiSelectionEnabled(false);
				chooser.resetChoosableFileFilters();
				chooser.setAcceptAllFileFilterUsed(true);
				

				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					
					if (f.exists()) {
						try {
							txtAttachAddFile.setText(f.getCanonicalPath());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		
		tblAttachAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (modelAttachmentsAdd.getRowCount() == 0 || !tblAttachAdd.isEnabled()) {
					return;
				}
				
				int selection = tblAttachAdd.getSelectedRow();
				
				if (selection != -1) {
					String file = modelAttachmentsAdd.getValueAt(selection, 0).toString();
					String name = modelAttachmentsAdd.getValueAt(selection, 1).toString();
					String desc = modelAttachmentsAdd.getValueAt(selection, 2).toString();
					String mime = modelAttachmentsAdd.getValueAt(selection, 3).toString();
					
					txtAttachAddFile.setText(file);
					txtAttachAddName.setText(name);
					txtAttachAddDesc.setText(desc);
					cbAttachAddMime.setSelectedItem(mime);
					
					tblAttachAdd.setEnabled(false);
					btnAttachAddAdd.setEnabled(false);
					btnAttachAddRemove.setEnabled(true);
					btnAttachAddEdit.setEnabled(true);
					btnAttachAddCancel.setEnabled(true);
				}				
			}
		});
		
		btnAttachAddAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txtAttachAddFile.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"The file is mandatory for the attachment!",
							"", JOptionPane.ERROR_MESSAGE);
					
					return;
				}
				
				String[] rowData = { 
						txtAttachAddFile.getText(),
						txtAttachAddName.getText().trim(),
						txtAttachAddDesc.getText().trim(),
						cbAttachAddMime.getSelectedItem().toString()
						};
				
				modelAttachmentsAdd.addRow(rowData);
				
				Utils.adjustColumnPreferredWidths(tblAttachAdd);
				tblAttachAdd.revalidate();
				
				txtAttachAddFile.setText("");
				txtAttachAddName.setText("");
				txtAttachAddDesc.setText("");
				cbAttachAddMime.setSelectedIndex(0);
			}
		});
		
		btnAttachAddEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (txtAttachAddFile.getText().trim().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"The file is mandatory for the attachment!",
							"", JOptionPane.ERROR_MESSAGE);
					
					return;
				}
				
				int selection = tblAttachAdd.getSelectedRow();
				
				String file = txtAttachAddFile.getText().trim();
				String name = txtAttachAddName.getText().trim();
				String desc = txtAttachAddDesc.getText().trim();
				String mime = cbAttachAddMime.getSelectedItem().toString();
				
				modelAttachmentsAdd.setValueAt(file, selection, 0);
				modelAttachmentsAdd.setValueAt(name, selection, 1);
				modelAttachmentsAdd.setValueAt(desc, selection, 2);
				modelAttachmentsAdd.setValueAt(mime, selection, 3);
				
				Utils.adjustColumnPreferredWidths(tblAttachAdd);
				tblAttachAdd.revalidate();
				
				txtAttachAddFile.setText("");
				txtAttachAddName.setText("");
				txtAttachAddDesc.setText("");
				cbAttachAddMime.setSelectedIndex(0);
				
				tblAttachAdd.setEnabled(true);
				btnAttachAddAdd.setEnabled(true);
				btnAttachAddRemove.setEnabled(false);
				btnAttachAddEdit.setEnabled(false);
				btnAttachAddCancel.setEnabled(false);
				tblAttachAdd.clearSelection();
			}
		});
		
		btnAttachAddRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selection = tblAttachAdd.getSelectedRow();
				
				modelAttachmentsAdd.removeRow(selection);
				
				txtAttachAddFile.setText("");
				txtAttachAddName.setText("");
				txtAttachAddDesc.setText("");
				cbAttachAddMime.setSelectedIndex(0);
				
				tblAttachAdd.setEnabled(true);
				btnAttachAddAdd.setEnabled(true);
				btnAttachAddRemove.setEnabled(false);
				btnAttachAddEdit.setEnabled(false);
				btnAttachAddCancel.setEnabled(false);
			}
		});
		
		btnAttachAddCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtAttachAddFile.setText("");
				txtAttachAddName.setText("");
				txtAttachAddDesc.setText("");
				cbAttachAddMime.setSelectedIndex(0);
				
				tblAttachAdd.setEnabled(true);
				btnAttachAddAdd.setEnabled(true);
				btnAttachAddRemove.setEnabled(false);
				btnAttachAddEdit.setEnabled(false);
				btnAttachAddCancel.setEnabled(false);
				tblAttachAdd.clearSelection();
			}
		});
		
		rbAttachReplaceName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbAttachReplaceOrig.setVisible(false);
				txtAttachReplaceOrig.setVisible(true);
				txtAttachReplaceOrig.setText("");
			}
		});
		
		rbAttachReplaceID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbAttachReplaceOrig.setVisible(false);
				txtAttachReplaceOrig.setVisible(true);
				txtAttachReplaceOrig.setText("1");
			}
		});
		
		rbAttachReplaceMime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtAttachReplaceOrig.setVisible(false);
				cbAttachReplaceOrig.setVisible(true);
				cbAttachReplaceOrig.setSelectedIndex(0);
			}
		});
		
		txtAttachReplaceOrig.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!rbAttachReplaceID.isSelected()) {
					return;
				}
				
				try {
					int id = Integer.parseInt(txtAttachReplaceOrig.getText());
					
					if (id < 1) {
						txtAttachReplaceOrig.setText("1");
					}
				} catch (NumberFormatException e1) {
					txtAttachReplaceOrig.setText("1");
				}
			}
		});
		
		new FileDrop(txtAttachReplaceNew, new FileDrop.Listener() {
        	public void filesDropped(File[] files) {
        		try {
    				if (!files[0].isDirectory()) {
    					txtAttachReplaceNew.setText(files[0].getCanonicalPath());
    				}
    			} catch(IOException e) {
    			}
        	}
        });
		
		btnAttachReplaceNewBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Select attachment");
				chooser.setMultiSelectionEnabled(false);
				chooser.resetChoosableFileFilters();
				chooser.setAcceptAllFileFilterUsed(true);
				

				int open = chooser.showOpenDialog(frmJMkvpropedit);
				
				if (open == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					
					if (f.exists()) {
						try {
							txtAttachReplaceNew.setText(f.getCanonicalPath());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		
		tblAttachReplace.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (modelAttachmentsReplace.getRowCount() == 0 || !tblAttachReplace.isEnabled()) {
					return;
				}
				
				int selection = tblAttachReplace.getSelectedRow();
				
				if (selection != -1) {
					String type = modelAttachmentsReplace.getValueAt(selection, 0).toString();
					String orig = modelAttachmentsReplace.getValueAt(selection, 1).toString();
					String replace = modelAttachmentsReplace.getValueAt(selection, 2).toString();
					String name = modelAttachmentsReplace.getValueAt(selection, 3).toString();
					String desc = modelAttachmentsReplace.getValueAt(selection, 4).toString();
					String mime = modelAttachmentsReplace.getValueAt(selection, 5).toString();
					
					txtAttachReplaceNew.setText(replace);
					
					if (type.equals(rbAttachReplaceName.getText())) {
						txtAttachReplaceOrig.setVisible(true);
						cbAttachReplaceOrig.setVisible(false);
						rbAttachReplaceName.setSelected(true);
						txtAttachReplaceOrig.setText(orig);
					} else  if (type.equals(rbAttachReplaceID.getText())) {
						txtAttachReplaceOrig.setVisible(true);
						cbAttachReplaceOrig.setVisible(false);
						rbAttachReplaceID.setSelected(true);
						txtAttachReplaceOrig.setText(orig);
					} else {
						txtAttachReplaceOrig.setVisible(false);
						cbAttachReplaceOrig.setVisible(true);
						rbAttachReplaceMime.setSelected(true);
						cbAttachReplaceOrig.setSelectedItem(replace);
					}
					
					txtAttachReplaceName.setText(name);
					txtAttachReplaceDesc.setText(desc);
					cbAttachReplaceMime.setSelectedItem(mime);
					
					tblAttachReplace.setEnabled(false);
					btnAttachReplaceAdd.setEnabled(false);
					btnAttachReplaceRemove.setEnabled(true);
					btnAttachReplaceEdit.setEnabled(true);
					btnAttachReplaceCancel.setEnabled(true);
				}				
			}
		});
		
		btnAttachReplaceAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = "";
				String orig = "";
				
				if (rbAttachReplaceName.isSelected()) {
					type = rbAttachReplaceName.getText();
					orig = txtAttachReplaceOrig.getText().trim();
				} else if (rbAttachReplaceID.isSelected()) {
					type = rbAttachReplaceID.getText();
					orig = txtAttachReplaceOrig.getText();
				} else {
					type = rbAttachReplaceMime.getText();
					orig = cbAttachReplaceOrig.getSelectedItem().toString();
				}
				
				if (orig.isEmpty() || txtAttachReplaceNew.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"The original value and replacement are mandatory for the attachment!",
							"", JOptionPane.ERROR_MESSAGE);
					
					return;
				}
				
				
				String[] rowData = {
						type,
						orig,
						txtAttachReplaceNew.getText(),
						txtAttachReplaceName.getText().trim(),
						txtAttachReplaceDesc.getText().trim(),
						cbAttachReplaceMime.getSelectedItem().toString()
					};
				
				modelAttachmentsReplace.addRow(rowData);
				
				Utils.adjustColumnPreferredWidths(tblAttachReplace);
				tblAttachReplace.revalidate();
				
				txtAttachReplaceOrig.setText("");
				txtAttachReplaceNew.setText("");
				txtAttachReplaceName.setText("");
				txtAttachReplaceDesc.setText("");
				cbAttachReplaceMime.setSelectedIndex(0);
				rbAttachReplaceName.setSelected(true);
				txtAttachReplaceOrig.setVisible(true);
				cbAttachReplaceOrig.setVisible(false);
			}
		});
		
		btnAttachReplaceEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = "";
				String orig = "";
				
				if (rbAttachReplaceName.isSelected()) {
					type = rbAttachReplaceName.getText();
					orig = txtAttachReplaceOrig.getText().trim();
				} else if (rbAttachReplaceID.isSelected()) {
					type = rbAttachReplaceID.getText();
					orig = txtAttachReplaceOrig.getText();
				} else {
					type = rbAttachReplaceMime.getText();
					orig = cbAttachReplaceOrig.getSelectedItem().toString();
				}
				
				int selection = tblAttachReplace.getSelectedRow();
				
				if (orig.isEmpty() || txtAttachReplaceNew.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"The original value and replacement are mandatory for the attachment!",
							"", JOptionPane.ERROR_MESSAGE);
					
					return;
				}
				
				modelAttachmentsReplace.setValueAt(type, selection, 0);
				modelAttachmentsReplace.setValueAt(orig, selection, 1);
				modelAttachmentsReplace.setValueAt(txtAttachReplaceNew.getText(), selection, 2);
				modelAttachmentsReplace.setValueAt(txtAttachReplaceName.getText(), selection, 3);
				modelAttachmentsReplace.setValueAt(txtAttachReplaceDesc.getText(), selection, 4);
				modelAttachmentsReplace.setValueAt(cbAttachReplaceMime.getSelectedItem().toString(), selection, 5);
				
				Utils.adjustColumnPreferredWidths(tblAttachReplace);
				tblAttachReplace.revalidate();
				
				tblAttachReplace.setEnabled(true);
				btnAttachReplaceAdd.setEnabled(true);
				btnAttachReplaceEdit.setEnabled(false);
				btnAttachReplaceRemove.setEnabled(false);
				btnAttachReplaceCancel.setEnabled(false);
				tblAttachReplace.clearSelection();
				
				txtAttachReplaceOrig.setText("");
				txtAttachReplaceNew.setText("");
				txtAttachReplaceName.setText("");
				txtAttachReplaceDesc.setText("");
				cbAttachReplaceMime.setSelectedIndex(0);
				rbAttachReplaceName.setSelected(true);
				txtAttachReplaceOrig.setVisible(true);
				cbAttachReplaceOrig.setVisible(false);
			}
		});
		
		btnAttachReplaceRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selection = tblAttachReplace.getSelectedRow();
				
				modelAttachmentsReplace.removeRow(selection);
				
				tblAttachReplace.setEnabled(true);
				btnAttachReplaceAdd.setEnabled(true);
				btnAttachReplaceEdit.setEnabled(false);
				btnAttachReplaceRemove.setEnabled(false);
				btnAttachReplaceCancel.setEnabled(false);
				tblAttachReplace.clearSelection();
				
				txtAttachReplaceOrig.setText("");
				txtAttachReplaceNew.setText("");
				txtAttachReplaceName.setText("");
				txtAttachReplaceDesc.setText("");
				cbAttachReplaceMime.setSelectedIndex(0);
				rbAttachReplaceName.setSelected(true);
				txtAttachReplaceOrig.setVisible(true);
				cbAttachReplaceOrig.setVisible(false);
			}
		});
		
		btnAttachReplaceCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tblAttachReplace.setEnabled(true);
				btnAttachReplaceAdd.setEnabled(true);
				btnAttachReplaceEdit.setEnabled(false);
				btnAttachReplaceRemove.setEnabled(false);
				btnAttachReplaceCancel.setEnabled(false);
				tblAttachReplace.clearSelection();
				
				txtAttachReplaceOrig.setText("");
				txtAttachReplaceNew.setText("");
				txtAttachReplaceName.setText("");
				txtAttachReplaceDesc.setText("");
				cbAttachReplaceMime.setSelectedIndex(0);
				rbAttachReplaceName.setSelected(true);
				txtAttachReplaceOrig.setVisible(true);
				cbAttachReplaceOrig.setVisible(false);
			}
		});
		
		tblAttachDelete.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (modelAttachmentsDelete.getRowCount() == 0 || !tblAttachDelete.isEnabled()) {
					return;
				}
				
				int selection = tblAttachDelete.getSelectedRow();
				
				if (selection != -1) {
					String type = modelAttachmentsDelete.getValueAt(selection, 0).toString();
					String value = modelAttachmentsDelete.getValueAt(selection, 1).toString();
					
					if (type.equals(rbAttachDeleteName.getText())) {
						rbAttachDeleteName.setSelected(true);
						cbAttachDeleteValue.setVisible(false);
						txtAttachDeleteValue.setVisible(true);
						txtAttachDeleteValue.setText(value);
					} else if (type.equals(rbAttachDeleteID.getText())) {
						rbAttachDeleteID.setSelected(true);
						cbAttachDeleteValue.setVisible(false);
						txtAttachDeleteValue.setVisible(true);
						txtAttachDeleteValue.setText(value);
					} else {
						rbAttachDeleteMime.setSelected(true);
						txtAttachDeleteValue.setVisible(false);
						cbAttachDeleteValue.setVisible(true);
						cbAttachDeleteValue.setSelectedItem(value);
					}
				
					tblAttachDelete.setEnabled(false);
					btnAttachDeleteAdd.setEnabled(false);
					btnAttachDeleteEdit.setEnabled(true);
					btnAttachDeleteRemove.setEnabled(true);
					btnAttachDeleteCancel.setEnabled(true);
				}
			}
		});
		
		rbAttachDeleteName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbAttachDeleteValue.setVisible(false);
				txtAttachDeleteValue.setVisible(true);
				txtAttachDeleteValue.setText("");
			}
		});
		
		rbAttachDeleteID.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbAttachDeleteValue.setVisible(false);
				txtAttachDeleteValue.setVisible(true);
				txtAttachDeleteValue.setText("1");
			}
		});
		
		rbAttachDeleteMime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtAttachDeleteValue.setVisible(false);
				cbAttachDeleteValue.setVisible(true);
				cbAttachDeleteValue.setSelectedIndex(0);
			}
		});
		
		txtAttachDeleteValue.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!rbAttachDeleteID.isSelected()) {
					return;
				}
				
				try {
					int id = Integer.parseInt(txtAttachDeleteValue.getText());
					
					if (id < 1) {
						txtAttachDeleteValue.setText("1");
					}
				} catch (NumberFormatException e1) {
					txtAttachDeleteValue.setText("1");
				}
			}
		});
		
		btnAttachDeleteAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = "";
				String value = "";
				
				if (rbAttachDeleteName.isSelected()) {
					type = rbAttachDeleteName.getText();
					value = txtAttachDeleteValue.getText().trim();
				} else if (rbAttachDeleteID.isSelected()) {
					type = rbAttachDeleteID.getText();
					value = txtAttachDeleteValue.getText();
				} else {
					type = rbAttachDeleteMime.getText();
					value = cbAttachDeleteValue.getSelectedItem().toString();
				}
				
				if (value.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"The value is mandatory for the attachment!",
							"", JOptionPane.ERROR_MESSAGE);
					
					return;
				}
				
				String[] rowData = { type, value };
				
				modelAttachmentsDelete.addRow(rowData);
				
				Utils.adjustColumnPreferredWidths(tblAttachDelete);
				tblAttachDelete.revalidate();
				
				rbAttachDeleteName.setSelected(true);
				cbAttachDeleteValue.setVisible(false);
				txtAttachDeleteValue.setVisible(true);
				txtAttachDeleteValue.setText("");
				tblAttachDelete.clearSelection();
			}
		});
		
		btnAttachDeleteEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String type = "";
				String value = "";
				
				if (rbAttachDeleteName.isSelected()) {
					type = rbAttachDeleteName.getText();
					value = txtAttachDeleteValue.getText().trim();
				} else if (rbAttachDeleteID.isSelected()) {
					type = rbAttachDeleteID.getText();
					value = txtAttachDeleteValue.getText();
				} else {
					type = rbAttachDeleteMime.getText();
					value = cbAttachDeleteValue.getSelectedItem().toString();
				}
				
				int selection = tblAttachDelete.getSelectedRow();
				
				if (value.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"The value is mandatory for the attachment!",
							"", JOptionPane.ERROR_MESSAGE);
					
					return;
				}
				
				modelAttachmentsDelete.setValueAt(type, selection, 0);
				modelAttachmentsDelete.setValueAt(value, selection, 1);
				
				Utils.adjustColumnPreferredWidths(tblAttachDelete);
				tblAttachDelete.revalidate();
				
				tblAttachDelete.setEnabled(true);
				btnAttachDeleteAdd.setEnabled(true);
				btnAttachDeleteEdit.setEnabled(false);
				btnAttachDeleteRemove.setEnabled(false);
				btnAttachDeleteCancel.setEnabled(false);
				tblAttachDelete.clearSelection();
				
				rbAttachDeleteName.setSelected(true);
				cbAttachDeleteValue.setVisible(false);
				txtAttachDeleteValue.setVisible(true);
				txtAttachDeleteValue.setText("");
				tblAttachDelete.clearSelection();
			}
		});
		
		btnAttachDeleteRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selection = tblAttachDelete.getSelectedRow();
				
				modelAttachmentsDelete.removeRow(selection);
				
				tblAttachDelete.setEnabled(true);
				btnAttachDeleteAdd.setEnabled(true);
				btnAttachDeleteEdit.setEnabled(false);
				btnAttachDeleteRemove.setEnabled(false);
				btnAttachDeleteCancel.setEnabled(false);
				tblAttachDelete.clearSelection();
				
				rbAttachDeleteName.setSelected(true);
				cbAttachDeleteValue.setVisible(false);
				txtAttachDeleteValue.setVisible(true);
				txtAttachDeleteValue.setText("");
			}
		});
		
		btnAttachDeleteCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tblAttachDelete.setEnabled(true);
				btnAttachDeleteAdd.setEnabled(true);
				btnAttachDeleteEdit.setEnabled(false);
				btnAttachDeleteRemove.setEnabled(false);
				btnAttachDeleteCancel.setEnabled(false);
				tblAttachDelete.clearSelection();
				
				rbAttachDeleteName.setSelected(true);
				cbAttachDeleteValue.setVisible(false);
				txtAttachDeleteValue.setVisible(true);
				txtAttachDeleteValue.setText("");
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
			cbLangVideo[nVideo].setModel(new DefaultComboBoxModel(mkvStrings.getLangName()));
			cbLangVideo[nVideo].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
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
			cbLangAudio[nAudio].setModel(new DefaultComboBoxModel(mkvStrings.getLangName()));
			cbLangAudio[nAudio].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
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
			cbLangSubtitle[nSubtitle].setModel(new DefaultComboBoxModel(mkvStrings.getLangName()));
			cbLangSubtitle[nSubtitle].setSelectedIndex(mkvStrings.getLangCodeList().indexOf("und"));
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
										 txtTags.getText() + cbExtTags.getSelectedItem();
						
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
										  txtChapters.getText() + cbExtChapters.getSelectedItem();
						
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
						String curLangCode = mkvStrings.getLangCodeList().get(cbLangVideo[j].getSelectedIndex());
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
						String curLangCode = mkvStrings.getLangCodeList().get(cbLangAudio[j].getSelectedIndex());
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
						String curLangCode = mkvStrings.getLangCodeList().get(cbLangSubtitle[j].getSelectedIndex());
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
	
	private void setCmdLineAttachmentsAdd() {
		cmdLineAttachmentsAdd = "";
		cmdLineAttachmentsAddOpt = "";
		
		for (int i = 0; i < modelAttachmentsAdd.getRowCount(); i++) {
			String file = modelAttachmentsAdd.getValueAt(i, 0).toString();
			String name = modelAttachmentsAdd.getValueAt(i, 1).toString();
			String desc = modelAttachmentsAdd.getValueAt(i, 2).toString();
			String mime = modelAttachmentsAdd.getValueAt(i, 3).toString();
			
			
			if (!name.isEmpty() || !desc.isEmpty() || !mime.isEmpty()) {
				if (!name.isEmpty()) {
					cmdLineAttachmentsAdd += " --attachment-name \"" + name + "\"";
					cmdLineAttachmentsAddOpt += " --attachment-name \"" + Utils.escapeName(name) + "\"";
				}
				
				if (!desc.isEmpty()) {
					cmdLineAttachmentsAdd += " --attachment-description \"" + desc + "\"";
					cmdLineAttachmentsAddOpt += " --attachment-description \"" + Utils.escapeName(desc) + "\"";
				}
				
				if (!mime.isEmpty()) {
					cmdLineAttachmentsAdd += " --attachment-mime-type \"" + mime + "\"";
					cmdLineAttachmentsAddOpt += " --attachment-mime-type \"" + Utils.escapeName(mime) + "\"";
				}
			}
				
			cmdLineAttachmentsAdd += " --add-attachment \"" + file + "\"";
			cmdLineAttachmentsAddOpt += " --add-attachment \"" + Utils.escapeName(file) + "\"";
		}
	}
	
	
	private void setCmdLineAttachmentsReplace() {
		cmdLineAttachmentsReplace = "";
		cmdLineAttachmentsReplaceOpt = "";
		
		for (int i = 0; i < modelAttachmentsReplace.getRowCount(); i++) {
			String type = modelAttachmentsReplace.getValueAt(i, 0).toString();
			String orig = modelAttachmentsReplace.getValueAt(i, 1).toString();
			String replace = modelAttachmentsReplace.getValueAt(i, 2).toString();
			String name = modelAttachmentsReplace.getValueAt(i, 3).toString();
			String desc = modelAttachmentsReplace.getValueAt(i, 4).toString();
			String mime = modelAttachmentsReplace.getValueAt(i, 5).toString();
			
			
			if (!name.isEmpty() || !desc.isEmpty() || !mime.isEmpty()) {
				if (!name.isEmpty()) {
					cmdLineAttachmentsReplace += " --attachment-name \"" + name + "\"";
					cmdLineAttachmentsReplaceOpt += " --attachment-name \"" + Utils.escapeName(name) + "\"";
				}
				
				if (!desc.isEmpty()) {
					cmdLineAttachmentsReplace += " --attachment-description \"" + desc + "\"";
					cmdLineAttachmentsReplaceOpt += " --attachment-description \"" + Utils.escapeName(desc) + "\"";
				}
				
				if (!mime.isEmpty()) {
					cmdLineAttachmentsReplace += " --attachment-mime-type \"" + mime + "\"";
					cmdLineAttachmentsReplaceOpt += " --attachment-mime-type \"" + Utils.escapeName(mime) + "\"";
				}
				
			}
			
			if (type.equals(rbAttachReplaceName.getText())) {
				cmdLineAttachmentsReplace += " --replace-attachment \"name:" + Utils.escapeColons(orig)
						+ ":" + replace + "\"";
				cmdLineAttachmentsReplaceOpt += " --replace-attachment \"name:" + Utils.escapeName(orig) 
						+ ":" + Utils.escapeName(replace) + "\"";
			} else if (type.equals(rbAttachReplaceID.getText())) {
				cmdLineAttachmentsReplace += " --replace-attachment \"" + orig	+ ":" + replace + "\"";
				cmdLineAttachmentsReplaceOpt += " --replace-attachment \"" + orig + ":" + Utils.escapeName(replace) + "\"";
			} else {
				cmdLineAttachmentsReplace += " --replace-attachment \"mime-type:" + Utils.escapeColons(orig)
						+ ":" + replace + "\"";
				cmdLineAttachmentsReplaceOpt += " --replace-attachment \"mime-type:" + Utils.escapeName(orig) 
						+ ":" + Utils.escapeName(replace) + "\"";
			}
		}
	}
	
	private void setCmdLineAttachmentsDelete() {
		cmdLineAttachmentsDelete = "";
		cmdLineAttachmentsDeleteOpt = "";
		
		for (int i = 0; i < modelAttachmentsDelete.getRowCount(); i++) {
			String type = modelAttachmentsDelete.getValueAt(i, 0).toString();
			String value = modelAttachmentsDelete.getValueAt(i, 1).toString();
			
			if (type.equals(rbAttachDeleteName.getText())) {
				cmdLineAttachmentsDelete += " --delete-attachment \"name:" + value + "\"";
				cmdLineAttachmentsDeleteOpt += " --delete-attachment \"name:" + Utils.escapeName(value) + "\"";
			} else if (type.equals(rbAttachDeleteID.getText())) {
				cmdLineAttachmentsDelete += " --delete-attachment \"" + value + "\"";
				cmdLineAttachmentsDeleteOpt += " --delete-attachment \"" + value + "\"";
			} else {
				cmdLineAttachmentsDelete += " --delete-attachment \"mime-type:" + Utils.escapeColons(value) + "\"";
				cmdLineAttachmentsDeleteOpt += " --delete-attachment \"mime-type:" + Utils.escapeName(value) + "\"";
			}
		}
	}
	
	private void setCmdLine() {
		setCmdLineGeneral();
		setCmdLineVideo();
		setCmdLineAudio();
		setCmdLineSubtitle();
		setCmdLineAttachmentsAdd();
		setCmdLineAttachmentsReplace();
		setCmdLineAttachmentsDelete();
		
		cmdLineBatch = new ArrayList<String>();
		cmdLineBatchOpt = new ArrayList<String>();
		
		String cmdTemp = cmdLineGeneral[0] + cmdLineAttachmentsDelete + cmdLineAttachmentsAdd
				+ cmdLineAttachmentsReplace + cmdLineVideo[0] + cmdLineAudio[0] + cmdLineSubtitle[0];
		
		if (!cmdTemp.isEmpty()) {
			for (int i = 0; i < modelFiles.getSize(); i++) {
				String cmdLineAll = cmdLineGeneral[i] + cmdLineAttachmentsDelete + cmdLineAttachmentsAdd
						+ cmdLineAttachmentsReplace	+ cmdLineVideo[i] + cmdLineAudio[i] + cmdLineSubtitle[i];

				String cmdLineAllOpt = cmdLineGeneralOpt[i] + cmdLineAttachmentsDeleteOpt + cmdLineAttachmentsAddOpt
						+ cmdLineAttachmentsReplaceOpt + cmdLineVideoOpt[i] + cmdLineAudioOpt[i] + cmdLineSubtitleOpt[i];
				
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
					
					exeFound = true;
				} catch (IOException e) {
					exeFound = false;
				} catch (InterruptedException e) {
					exeFound = false;
				}
				
				return null;
			}
		};
		
		worker.execute();
		while (!worker.isDone()) { }

		return exeFound;
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
	
	
	/* Start of table methods */
	
	private void resizeColumns(JTable table, double[] colSizes) {
		TableColumnModel columnModel = table.getColumnModel();
		int[] colWidths = new int[colSizes.length];
		
		int parWidth = table.getParent().getWidth();
		
		int total = 0;
		for (int i = 0; i < colSizes.length; i++) {
			colWidths[i] = (int) (parWidth * colSizes[i]);
			total += colWidths[i];
		}	
		
		colWidths[colWidths.length-1] += parWidth-total;
		
		for (int i = 0; i < colSizes.length; i++) {
			// Set minimum size for column
			columnModel.getColumn(i).setMinWidth(colWidths[i]);
			
			// Set prefered size for column
			columnModel.getColumn(i).setPreferredWidth(colWidths[i]);
		}
		
		table.revalidate();
	}
	
	/* End of table methods */
	
}
