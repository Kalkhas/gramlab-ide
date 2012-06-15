/*
 * Unitex
 *
 * Copyright (C) 2001-2012 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */
package fr.umlv.unitex.frames;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.umlv.unitex.config.Config;
import fr.umlv.unitex.config.ConfigManager;
import fr.umlv.unitex.process.Launcher;
import fr.umlv.unitex.process.ToDo;
import fr.umlv.unitex.process.commands.ErrorMessageCommand;
import fr.umlv.unitex.process.commands.FlattenCommand;
import fr.umlv.unitex.process.commands.Fst2TxtCommand;
import fr.umlv.unitex.process.commands.Grf2Fst2Command;
import fr.umlv.unitex.process.commands.MkdirCommand;
import fr.umlv.unitex.process.commands.MultiCommands;
import fr.umlv.unitex.process.commands.NormalizeCommand;
import fr.umlv.unitex.process.commands.Seq2GrfCommand;
import fr.umlv.unitex.process.commands.TokenizeCommand;

/**
 * This class describes the "Construct Text FST" frame that offers to the user
 * to build the text automaton.
 * 
 * @author Sébastien Paumier
 */
public class ConstructSeqTfstFrame extends JInternalFrame implements ActionListener{
	private final JTextField GRFfile = new JTextField(20);
	private final JTextField SourceFile = new JTextField(20);
	private String SourceFileName,SourceFileShortName, GRFFileName;
	private int n_w = 0, n_r = 0, n_d = 0, n_a = 0;
	JSpinner spinner_w, spinner_r, spinner_d, spinner_a;
	SpinnerNumberModel sm_w, sm_r, sm_d, sm_a;
	JRadioButton bTEI, bTXT, bSNT;
	private final JCheckBox applyBeautify= new JCheckBox("Apply Beautify ",true);
	private File sequenceGRF;
	private File ReplaceGRF;

	/**
	 * Creates and shows a new <code>ConstructSeqFstFrame</code>.
	 */
	ConstructSeqTfstFrame() {
		super("Construct the Seq GRF", false);
		setContentPane(constructPanel());
		pack();
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	private JPanel constructPanel() {
		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel top = new JPanel(new FlowLayout());
		final JPanel FilePanel = new JPanel(new BorderLayout());
		panel.add(textPanel(), BorderLayout.CENTER);
		panel.add(constructButtonsPanel(), BorderLayout.SOUTH);
		FilePanel.add(FileFormatPanel(), BorderLayout.CENTER);
		FilePanel.add(SourceFilePanel(),BorderLayout.NORTH);
		FilePanel.add(outputFilePanel(), BorderLayout.SOUTH);
		top.add(FilePanel);
		top.add(wildcardsPanel(), BorderLayout.EAST);
		panel.add(top, BorderLayout.CENTER);
		return panel;
	}

	private JPanel constructButtonsPanel() {
		final JPanel buttons = new JPanel(new GridLayout(1, 2));
		buttons.setBorder(new EmptyBorder(6, 6, 2, 2));
		final Action okAction = new AbstractAction("Construct GRF") {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
				constructSeqTfst();
			}
		};
		final Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		};
		final JButton OK = new JButton(okAction);
		final JButton CANCEL = new JButton(cancelAction);
		buttons.add(CANCEL);
		buttons.add(OK);
		return buttons;
	}

	private JPanel textPanel() {
		final JPanel textP = new JPanel();
		textP.setBorder(new TitledBorder("Name the output GRF File"));
		textP
		.add(new JLabel(
				"The programm will construct the automata that regognizes all theses sequences."));
		return textP;
	}

	private JPanel FileFormatPanel(){
		final JPanel fileP = new JPanel(new GridLayout(1,3));
		bTEI = new JRadioButton("TEILite");
		bTEI.setMnemonic(1);
		bTXT = new JRadioButton("TXT");
		bTXT.setMnemonic(2);
		bSNT = new JRadioButton("SNT");
		bSNT.setMnemonic(3);

		ButtonGroup group = new ButtonGroup();
		group.add(bTEI);
		group.add(bTXT);								
		group.add(bSNT);
		bTEI.addActionListener(this);
		bTXT.addActionListener(this);
		bSNT.addActionListener(this);
		fileP.add(bTEI);
		fileP.add(bTXT);
		fileP.add(bSNT);

		return fileP;
	}
	private JPanel SourceFilePanel() {
		final JPanel fileP = new JPanel(new GridLayout(2, 1));
		final JPanel fileP2 = new JPanel(new FlowLayout());
		fileP.setBorder(new EmptyBorder(8, 8, 1, 1));
		fileP.add(new JLabel("select the Sequence Corpus :"));
		fileP2.add(SourceFile, BorderLayout.LINE_START);
		final Action setAction = new AbstractAction("Set ...") {
			public void actionPerformed(ActionEvent arg1) {
				final JFileChooser chooser = Config.getFileEditionDialogBox();
				System.out.println("chooser dir : "
						+ chooser.getCurrentDirectory());
				chooser.setCurrentDirectory(Config.getCurrentCorpusDir());
				final int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					// we return if the user has clicked on CANCEL
					return;
				}
				try {
					SourceFileName=chooser.getSelectedFile().getCanonicalPath();
					SourceFileShortName=chooser.getSelectedFile().getName();
				} catch (IOException e) {
					e.printStackTrace();
				}
				SourceFile.setText(SourceFileName);

				if (SourceFile.getText().endsWith(".txt") ||
						SourceFile.getText().endsWith(".snt") ||
						SourceFile.getText().endsWith(".xml")
						){
					System.out.println("sourcefile = "+SourceFile.getText());
					System.out.println("sourcefile_sort : "+SourceFile.getText());
					System.out.println("Config.getCurrentGraphDir() ="+Config.getCurrentGraphDir());

					GRFFileName=Config.getCurrentGraphDir().getAbsolutePath()
							+File.separatorChar
							+SourceFileShortName.substring(0,SourceFileShortName.length()-4)
							+"_"
							+n_w
							+n_a
							+n_r
							+n_d
							+".grf";
					System.out.println("grfFileName = "+GRFFileName);
				}
				if (SourceFile.getText().endsWith(".snt")){
					bSNT.setSelected(true);
					bTEI.setSelected(false);
					bTXT.setSelected(false);
					Config.setCurrentSnt(new File(SourceFileName));
					System.out.println("selected : SNT");
				}
				else if (SourceFile.getText().endsWith(".xml")){
					bTEI.setSelected(true);
					bTXT.setSelected(false);
					bSNT.setSelected(false);
					System.out.println("selected : TEI");
				}
				else {
					bTXT.setSelected(true);
					bTEI.setSelected(false);
					bSNT.setSelected(false);
					System.out.println("selected : TXT");
				}		
				try {
					if (SourceFile.getText().endsWith(".txt") ||
							SourceFile.getText().endsWith(".snt") ||
							SourceFile.getText().endsWith(".xml")
							){
						GRFFileName=Config.getUserCurrentLanguageDir().getCanonicalPath()
								+File.separatorChar
								+"Graphs"
								+File.separatorChar
								+SourceFileShortName.substring(0,SourceFileShortName.length()-4)
								+"_"
								+n_w
								+n_a
								+n_r
								+n_d
								+".grf";
					}else{
						GRFFileName=Config.getUserCurrentLanguageDir().getCanonicalPath()
								+File.separatorChar
								+"Graphs"
								+File.separatorChar
								+SourceFileShortName
								+"_"
								+n_w
								+n_a
								+n_r
								+n_d
								+".grf";
					}
					GRFfile.setText(
							GRFFileName
							);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		final JButton setButton = new JButton(setAction);
		setButton.setSize(4, 1);
		fileP2.add(setButton);
		fileP.add(fileP2);
		return fileP;
	}
	private JPanel outputFilePanel() {
		final JPanel fileP = new JPanel(new GridLayout(2, 1));
		JPanel fileP2= new JPanel(new FlowLayout());
		fileP.setBorder(new EmptyBorder(8, 8, 1, 1));
		fileP.add(new JLabel("choose the name of the resulting grf file"));
		fileP2.add(GRFfile, BorderLayout.LINE_START);
		final Action setAction = new AbstractAction("Set ...") {
			public void actionPerformed(ActionEvent arg1) {
				final JFileChooser chooser = Config.getGrfAndFst2DialogBox();
				System.out.println("GRFFileName = "+GRFFileName);
				File f = new File(GRFFileName);
				chooser.setSelectedFile(f);
				System.out.println("chooser dir : "
						+ chooser.getCurrentDirectory());
				chooser.setCurrentDirectory(Config.getCurrentGraphDir());
				final int returnVal = chooser.showOpenDialog(null);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					// we return if the user has clicked on CANCEL
					return;
				}
				try {
					GRFFileName=chooser.getSelectedFile().getCanonicalPath();
				} catch (IOException e) {
					e.printStackTrace();
				}
				GRFfile.setText(GRFFileName);
			}
		};
		final JButton setButton = new JButton(setAction);
		setButton.setSize(4, 1);
		fileP2.add(setButton);
		fileP.add(fileP2);
		return fileP;
	}

	void constructSeqTfst(){
		File originalTextFile =new File(SourceFile.getText());
		if (originalTextFile.exists()){
			final MultiCommands commands = new MultiCommands();
			System.out.println("HELLO");
			File sntFile = null;
			File dir = null;
			if (bSNT.isSelected()){
				System.out.println(">bSNT");
				sntFile = new File(SourceFile.getText());
				dir =new File (SourceFile.getText()+"_snt");
			}else {
				if(bTEI.isSelected()){
					System.out.println(">bTEI ");
					String dirName;
					if(SourceFile.getText().endsWith(".xml")){
						dirName=SourceFile.getText().substring(0, SourceFile.getText().length()-4)+"_snt";
						sntFile=new File(SourceFile.getText().substring(0, SourceFile.getText().length()-4)+".snt");
					}else{
						dirName=SourceFile.getText()+"_snt";
						sntFile=new File(SourceFile.getText()+".snt");
					}
					dir = new File (dirName);
					if (!dir.exists()){
						//Mkdir
						final MkdirCommand mkdir = new MkdirCommand().name(dir);
						commands.addCommand(mkdir);
					}
					//Normalize
					NormalizeCommand normalizeCmd = new NormalizeCommand()
					.textWithDefaultNormalization(originalTextFile);
					commands.addCommand(normalizeCmd);
					sequenceGRF=new File(Config.getUserCurrentLanguageDir(), "Graphs");
					sequenceGRF=new File(sequenceGRF, "Preprocessing");
					sequenceGRF=new File(sequenceGRF, "Sentence");
					sequenceGRF=new File(sequenceGRF, "SequenceTEI.grf");
					ReplaceGRF =new File(Config.getUserCurrentLanguageDir(),"Graphs");
					ReplaceGRF =new File(ReplaceGRF,"Preprocessing");
					ReplaceGRF =new File(ReplaceGRF,"Replace");
					ReplaceGRF =new File(ReplaceGRF,"ReplaceTEI.grf");
				}
				if (bTXT.isSelected()){
					System.out.println(">bTXT");
					String dirName;
					if(SourceFile.getText().endsWith(".txt")){
						System.out.println(SourceFile.getText()+" ends with .txt");
						dirName=SourceFile.getText().substring(0, SourceFile.getText().length()-4)+"_snt";
						sntFile=new File(SourceFile.getText().substring(0, SourceFile.getText().length()-4)+".snt");
					}else{
						dirName=SourceFile.getText()+"_snt";
						sntFile=new File(SourceFile.getText()+".snt");
						System.out.println("sourcefile : "+SourceFile.getText());
						System.out.println("sntFile : "+sntFile.getName());
					}
					dir = new File (dirName);
					if (!dir.exists()){
						//Mkdir
						final MkdirCommand mkdir = new MkdirCommand().name(dir);
						commands.addCommand(mkdir);
					}
					System.out.println("UserCurrentLanguageDir : "+Config.getUserCurrentLanguageDir());
					//Normalize
					NormalizeCommand normalizeCmd = new NormalizeCommand().textWithDefaultNormalization(originalTextFile);
					commands.addCommand(normalizeCmd);
					sequenceGRF=new File(Config.getUserCurrentLanguageDir(), "Graphs");
					sequenceGRF=new File(sequenceGRF, "Preprocessing");
					sequenceGRF=new File(sequenceGRF, "Sentence");
					sequenceGRF=new File(sequenceGRF, "SequenceTXT.grf");
				}

				if (sequenceGRF==null){
					System.out.println("sequenceGRF == null");
				}
				else if (!sequenceGRF.exists()){
					System.out.println("sequenceGRF : "+sequenceGRF.toString()+"does not exist");
					commands.addCommand(new ErrorMessageCommand(
							"*** WARNING: sentence delimitation skipped because the graph was not found ***\n"));
				}else{
					System.out.println("sequenceGRF exists");
					//Grf2Fst2
					final Grf2Fst2Command grfCmd = new Grf2Fst2Command().grf(sequenceGRF)
							.enableLoopAndRecursionDetection(true)
							.tokenizationMode(null, sequenceGRF).repositories()
							.emitEmptyGraphWarning().displayGraphNames();
					commands.addCommand(grfCmd);
					System.out.println("grfCmd added");
					System.out.println("\tgrfCmd : "+grfCmd.getCommandLine());
					String fst2Name = sequenceGRF.getAbsolutePath().substring(0, sequenceGRF.getAbsolutePath().length()-3);
					System.out.println("fst2Name = "+fst2Name+"\n");
					fst2Name = fst2Name + "fst2";
					File fst2= new File(fst2Name);
					//Flatten
					final FlattenCommand flattenCmd = new FlattenCommand().fst2(
							fst2).resultType(false).depth(5);
					commands.addCommand(flattenCmd);
					//Fst2Txt
					Fst2TxtCommand Fst2Txtcmd = new Fst2TxtCommand().text(sntFile)
																	.fst2(fst2)
																	.alphabet(ConfigManager.getManager().getAlphabet(null))
																	.mode(true);
					Fst2Txtcmd = Fst2Txtcmd.charByChar(ConfigManager.getManager().isCharByCharLanguage(null));
					Fst2Txtcmd=Fst2Txtcmd.morphologicalUseOfSpace(ConfigManager.getManager().isMorphologicalUseOfSpaceAllowed(null));
					commands.addCommand(Fst2Txtcmd);
					if(bTEI.isSelected()){
						//grfCmd2
						final Grf2Fst2Command grfCmd2 = new Grf2Fst2Command().grf(ReplaceGRF)
								.enableLoopAndRecursionDetection(true)
								.tokenizationMode(null, ReplaceGRF).repositories()
								.emitEmptyGraphWarning().displayGraphNames();
						commands.addCommand(grfCmd2);
						String fst2Name2 = ReplaceGRF.getAbsolutePath().substring(0,ReplaceGRF.getAbsolutePath().length()-3);
						fst2Name2= fst2Name2+ "fst2";
						File fst22 = new File(fst2Name2);
						//Fst2Txt2
						Fst2TxtCommand Fst2Txt2 = new Fst2TxtCommand().text(
								sntFile
								).fst2(fst22).alphabet(
										ConfigManager.getManager().getAlphabet(null)).mode(false);

						//Tokenize
						Fst2Txt2 = Fst2Txt2.charByChar(ConfigManager.getManager().isCharByCharLanguage(null));
						Fst2Txt2=Fst2Txt2.morphologicalUseOfSpace(ConfigManager.getManager()
									.isMorphologicalUseOfSpaceAllowed(null));
						commands.addCommand(Fst2Txt2);	
					}
				}
				dir = new File(SourceFile.getText()+"_snt");
				/* Cleaning files */
				Config.cleanTfstFiles(true);
				//tokenizeCmd			
				TokenizeCommand tokenizeCmd = new TokenizeCommand().text(
						sntFile
						).alphabet(
								ConfigManager.getManager().getAlphabet(null));
				if (ConfigManager.getManager().isCharByCharLanguage(null)) {
					tokenizeCmd = tokenizeCmd.tokenizeCharByChar();
				}
				commands.addCommand(tokenizeCmd);
			}
			//Seq2Grf		
			final Seq2GrfCommand seqCmd = new Seq2GrfCommand().alphabet(
					ConfigManager.getManager().getAlphabet(null).getAbsolutePath())
					.output(GRFfile.getText()).wildcards(n_w).wildcard_insert(n_a)
					.wildcard_replace(n_r).wildcard_delete(n_d).applyBeautify(applyBeautify.isSelected()).text(
							sntFile);
			System.out.println("seqCmd =" + seqCmd.getCommandLine());
			commands.addCommand(seqCmd);
			InternalFrameManager.getManager(null).closeTextAutomatonFrame();
			InternalFrameManager.getManager(null).closeTfstTagsFrame();
			/* We also have to rebuild the text automaton */
			Config.cleanTfstFiles(true);
			final File GRF = new File(GRFfile.getText());
			for( int i=0;i<commands.numberOfCommands();i++){
				System.out.println(i+" : "+commands.getCommand(i).getCommandLine());
			}
			Launcher.exec(commands, true, new ConstructTfstDo(GRF), false);
		}
	}

	class ConstructTfstDo implements ToDo {
		File GrfFileName;

		ConstructTfstDo(File grf) {
			GrfFileName = grf;
		}

		public void toDo(boolean success) {
			Config.cleanTfstFiles(true);
			InternalFrameManager.getManager(GrfFileName).newGraphFrame(
					GrfFileName);
		}
	}

	private JPanel wildcardsPanel() {
		final JPanel p = new JPanel(new GridLayout(5, 1));
		p.setBorder(BorderFactory.createRaisedBevelBorder());
		final JLabel wildcards = new JLabel("wildcards");
		final JLabel insert = new JLabel("insert");
		final JLabel replace = new JLabel("replace");
		final JLabel delete = new JLabel("delete");
		sm_w = new SpinnerNumberModel(0, 0, 10, 1);
		sm_w.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				n_w = sm_w.getNumber().intValue();
				String rad=SourceFileShortName;
				if (SourceFileShortName.endsWith(".txt") ||
						SourceFileShortName.endsWith(".snt") ||
						SourceFileShortName.endsWith(".xml")){
					rad=rad.substring(0,SourceFileShortName.length()
							-4); 
				}
				System.out.println("SourceFileShortName = "+SourceFileShortName);
				GRFFileName = Config.getCurrentGraphDir().getAbsolutePath()
						+File.separatorChar
						+rad
						+"_"
						+n_w+n_a+n_r+n_d+".grf";
				GRFfile.setText(GRFFileName);
			}
		});
		spinner_w = new JSpinner(sm_w);
		sm_a = new SpinnerNumberModel(0, 0, 10, 1);
		sm_a.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (!SourceFileShortName.isEmpty()){
					n_a = sm_a.getNumber().intValue();	
					String rad=SourceFileShortName;
					if (SourceFileShortName.endsWith(".txt") ||
							SourceFileShortName.endsWith(".snt") ||
							SourceFileShortName.endsWith(".xml")){
						rad=rad.substring(0,SourceFileShortName.length()
								-4); 
					}
					System.out.println("SourceFileShortName = "+SourceFileShortName);
					GRFFileName=Config.getCurrentGraphDir().getAbsolutePath()
							+File.separatorChar
							+rad
							+"_"
							+n_w
							+n_a
							+n_r
							+n_d
							+".grf";
					GRFfile.setText(GRFFileName);
				}
			}
		});
		spinner_a = new JSpinner(sm_a);
		sm_d = new SpinnerNumberModel(0, 0, 10, 1);
		sm_d.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				n_d = sm_d.getNumber().intValue();
				if (!SourceFileShortName.isEmpty()){
					String rad=SourceFileShortName;
					if (SourceFileShortName.endsWith(".txt") ||
							SourceFileShortName.endsWith(".snt") ||
							SourceFileShortName.endsWith(".xml")){
						rad=rad.substring(0,SourceFileShortName.length()
								-4); 
					}
					GRFFileName=Config.getCurrentGraphDir().getAbsolutePath()
							+File.separatorChar
							+rad
							+"_"
							+n_w
							+n_a
							+n_r
							+n_d
							+".grf";
					GRFfile.setText(GRFFileName);
				}
			}
		});
		spinner_d = new JSpinner(sm_d);
		sm_r = new SpinnerNumberModel(0, 0, 10, 1);
		sm_r.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (!SourceFileShortName.isEmpty()){
					n_r = sm_r.getNumber().intValue();
					String rad=SourceFileShortName;
					if (SourceFileShortName.endsWith(".txt") ||
							SourceFileShortName.endsWith(".snt") ||
							SourceFileShortName.endsWith(".xml")){
						rad=rad.substring(0,SourceFileShortName.length()
								-4); 
					}
					GRFFileName=Config.getCurrentGraphDir().getAbsolutePath()
							+File.separatorChar
							+rad
							+"_"
							+n_w
							+n_a
							+n_r
							+n_d
							+".grf";
					GRFfile.setText(GRFFileName);
				}
			}
		});
		spinner_r = new JSpinner(sm_r);
		JPanel _p1 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		_p1.add(wildcards);
		_p1.add(spinner_w);
		p.add(_p1);

		JPanel _p2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		_p2.add(insert);
		_p2.add(spinner_a);
		p.add(_p2);

		JPanel _p3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		_p3.add(replace);
		_p3.add(spinner_r);
		p.add(_p3);

		JPanel _p4 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		_p4.add(delete);
		_p4.add(spinner_d);
		p.add(_p4);

		JPanel _p5 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		_p5.add(applyBeautify);
		p.add(_p5);
		return p;
	}

	public void actionPerformed(ActionEvent arg0) {

	}
}
