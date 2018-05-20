package transform;

/**
 * 
 * settings for Java application.
 * @author Andrew Lipinski
 *
 */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.dom4j.DocumentException;

public class Gui extends JFrame {
	/**
	 * APPLICATION GUI
	 */
	private static final long serialVersionUID = 1L;
	public File configFile=new File("./config.properties");
	public Properties configProps;
	private JLabel labelRoot = new JLabel("Data Mapper Root Directory:  ");
	private JLabel labelMap = new JLabel("Mapping Data Location: ");
	private JLabel labelOut = new JLabel("Transformation File Save Location: ");
	private JLabel labelrtn = new JLabel("ReportTypeName ");
	private JLabel labelrbpn = new JLabel("ReportedByPersonName");
	private JLabel labelrd = new JLabel("ReportedDate");
	private JTextField textRoot = new JTextField(50);
	private JButton buttonSelectRoot = new JButton("SELECT ROOT DIR");
	private JTextField textMap = new JTextField(50);
	private JButton buttonSelectSrc = new JButton("SELECT MAPPING FILE");
	private JTextField textOut = new JTextField(50);
	private JButton buttonSelectOutput = new JButton("SELECT OUTPUT DIR");
	private JTextField textrtn = new JTextField(50);
	private JTextField textrbpn = new JTextField(50);
	private JTextField textrd = new JTextField(50);
	private JButton buttonSave = new JButton("Save Properties");
	private JButton buttonExport = new JButton("Generate Transformation File");
	private JButton buttonOpenSrc = new JButton("Open Mapping File");
	private JButton buttonOpenTrans = new JButton("Open Transformation File");
	private JButton buttonOpenCfg = new JButton("Open Config File");
	public Trans dt;
	public Logger lg;
	public int subver = 0;

	public Gui() {
		super("LOT Flightman to Qpulse Data Transformer");
		// START APPLICATION PROCESSES
		

		// USER INTERFACE
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.insets = new Insets(10, 5, 10, 5);
		constraints.anchor = GridBagConstraints.WEST;
		add(labelRoot, constraints);
		constraints.gridx = 1;
		add(textRoot, constraints);
		constraints.gridx = 2;
		add(buttonSelectRoot, constraints);

		// MAP
		constraints.gridy = 1;
		constraints.gridx = 0;
		add(labelMap, constraints);
		constraints.gridx = 1;
		add(textMap, constraints);
		constraints.gridx = 2;
		add(buttonSelectSrc, constraints);

		// OUT
		constraints.gridy = 2;
		constraints.gridx = 0;
		add(labelOut, constraints);
		constraints.gridx = 1;
		add(textOut, constraints);
		constraints.gridx = 2;
		add(buttonSelectOutput, constraints);
		
		// RtypeName
		constraints.gridy = 3;
		constraints.gridx = 0;
		add(labelrtn, constraints);
		constraints.gridx = 1;
		add(textrtn, constraints);
		//ReportedByPerson
		constraints.gridy = 4;
		constraints.gridx = 0;
		add(labelrbpn, constraints);
		constraints.gridx = 1;
		add(textrbpn, constraints);
		//ReportedDate
		constraints.gridy = 5;
		constraints.gridx = 0;
		add(labelrd, constraints);
		constraints.gridx = 1;
		constraints.gridwidth = 2;
		add(textrd, constraints);
		
		
		// USER ACTIONS
		// SAVE
		constraints.gridy = 6;
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.WEST;
		add(buttonSave, constraints);
		// OPEN CONFIG
		constraints.gridy = 6;
		constraints.gridx = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		add(buttonOpenCfg, constraints);
		// OPEN SRC
		constraints.gridy = 7;
		constraints.gridx = 0;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.WEST;
		add(buttonOpenSrc, constraints);
		// OPEN TRANS
		constraints.gridy = 7;
		constraints.gridx = 1;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.CENTER;
		add(buttonOpenTrans, constraints);		
		// RUN
		constraints.gridy = 7;
		constraints.gridx = 3;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.EAST;
		add(buttonExport, constraints);
				
		// Save properties
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String msg = "Properties were saved successfully!";
				try {
					saveProperties();
					JOptionPane.showMessageDialog(Gui.this, "Properties were saved successfully!");
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(Gui.this, "Error saving properties file: " + ex.getMessage());
					msg = "Error saving properties file: " + ex.getMessage();
				}
				lg.l(msg);
			}
		});
		// EXPORT
		buttonExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String msg = "XSLT CONTENT Generated: For MAP[" + textMap.getText() + "] OUT[" + textOut.getText()
						+ "]";
				try {
					// METHOD
					setProperties();
					dt.runTrans(textMap.getText(), textOut.getText());
					//dt.CopyFile("./src/main/resources/logs/log.txt", dt.Root + "\\logs\\log.txt");
					//dt.CopyFile(textOut.getText(), dt.Root + "transformation\\backups\\");
					JOptionPane.showMessageDialog(Gui.this, msg);
				} catch (Error ex) {
					String msgerr = "Error-BUT-EXPORT-Exporting Transformation File Data: " + ex.getMessage();
					JOptionPane.showMessageDialog(Gui.this, msgerr);
					msg = msgerr;
				}
				lg.l(msg);
			}
		});
		// OPEN MAPPING FILE
		buttonOpenSrc.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						String msg = "OPENING MAP FILE:[" + textMap.getText() + "]";
						try {
							// METHOD
							JOptionPane.showMessageDialog(Gui.this, msg);
							Runtime.getRuntime().exec("C:\\Windows\\explorer.exe "+textMap.getText());
						} catch (Error | IOException ex) {
							String msgerr = "Error-OPENING FILE-: " + ex.getMessage();
							JOptionPane.showMessageDialog(Gui.this, msgerr);
							msg = msgerr;
						}
						lg.l(msg);
					}
				});
		// OPEN TRANS
		buttonOpenTrans.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						String msg = "OPENING Transformation FILE:[" + textOut.getText() + "]";
						try {
							// METHOD
							JOptionPane.showMessageDialog(Gui.this, msg);
							Runtime.getRuntime().exec("C:\\Windows\\explorer.exe "+textOut.getText());
						} catch (Error | IOException ex) {
							String msgerr = "Error-OPENING FILE-: " + ex.getMessage();
							JOptionPane.showMessageDialog(Gui.this, msgerr);
							msg = msgerr;
						}
						lg.l(msg);
					}
				});
		// OPEN CFG FILE
		buttonOpenCfg.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						String msg = "OPENING Properties File:[" + configFile.getAbsolutePath() + "]";
						try {
							// METHOD
							JOptionPane.showMessageDialog(Gui.this, msg);
							Runtime.getRuntime().exec("C:\\Windows\\explorer.exe "+configFile.getAbsolutePath());
						} catch (Error | IOException ex) {
							String msgerr = "Error-OPENING FILE-: " + ex.getMessage();
							JOptionPane.showMessageDialog(Gui.this, msgerr);
							msg = msgerr;
						}
						lg.l(msg);
					}
				});
		// SELECTS AND SETS ROOT
		buttonSelectRoot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// Method to Run
					String msg = "SET ROOT PATH: " + configProps.getProperty("Root");
					String troot = chooserDirectory(configProps.getProperty("Root"));
					textRoot.setText(troot);
					configProps.setProperty("Root", textRoot.getText());
					dt.Root = textRoot.getText();
					dt.createDirectoryTree();
					Runtime.getRuntime().exec("C:\\Windows\\explorer.exe "+textRoot.getText());
					lg.l(msg);
				} catch (Error | IOException ex) {
					JOptionPane.showMessageDialog(Gui.this, "Error Processing Root File: " + ex.getMessage());
				}
			}
		});

		// SELECTS AND SETS MAP
		buttonSelectSrc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String msg = "SET MAP PATH: " + configProps.getProperty("Map");
				try {
					// Method to Run
					String mapt = chooserFile(configProps.getProperty("Map"));
					textMap.setText(mapt);
					configProps.setProperty("Map", textMap.getText());
					dt.Map = configProps.getProperty("Map");
					//dt.CopyMap(configProps.getProperty("Map")); //dt.CopyFile(configProps.getProperty("Map"), textRoot.getText()+"\\resources\\mapping\\bkg\\eReport-qpulse-6-1.xlsx");
					lg.l(msg);
					JOptionPane.showMessageDialog(Gui.this,
							"Data Mapping Complete: Mapping File Processed is: " + configProps.getProperty("Map"));
				} catch (Error ex) {
					JOptionPane.showMessageDialog(Gui.this, "Error Processing Src File: " + ex.getMessage());
				}
				lg.l(msg);
			}
		});
		// SETS TRANS PATH VALUE
		buttonSelectOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String msg = "SET OUTPUT PATH: " + configProps.getProperty("Out");
				try {
					// Method to Run
					String transt = chooserFileTrans(configProps.getProperty("Out"));
					textOut.setText(transt);
					configProps.setProperty("Out", textOut.getText());
					dt.Out = textOut.getText();

					JOptionPane.showMessageDialog(Gui.this,
							"Output Complete: Transformation File Processed is: " + configProps.getProperty("Out"));
				} catch (Error ex) {
					JOptionPane.showMessageDialog(Gui.this, "Error Processing Transformation File: " + ex.getMessage());
				}
				lg.l(msg);
			}
		});
		// EXIT
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		//SETS VARIABLES
		try {
			loadProperties();
			
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this,"The config.properties file does not exist, default properties loaded.");
		}
		//SETS GUI TEXTAREA VALUES
		textRoot.setText(configProps.getProperty("Root"));
		textMap.setText(configProps.getProperty("Map"));
		textOut.setText(configProps.getProperty("Out"));
		textrtn.setText(configProps.getProperty("ReportTypeName"));
		textrbpn.setText(configProps.getProperty("ReportedByPersonName"));
		textrd.setText(configProps.getProperty("ReportedDate"));
		//INITIALIZE
		
		try {

			lg = new Logger(configProps.getProperty("Root"));
			dt = new Trans(lg);
			lg.l("0-RUNNING");
			//lg.l("1-Logger Started - log file:");
			//lg.l("2-Data Transformer - Started");
			
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	private void loadProperties() throws IOException {
		Properties defaultProps = new Properties();
		// sets default properties
		String rt = null;
		//rt = System.getProperty("user.home") + "\\dataTransformer\\";
		//rt = System.getProperty("user.dir") + "\\dataTransformer\\";
		rt = System.getenv("SystemDrive") + "\\dataTransformer\\";
		defaultProps.setProperty("Root", rt);
		defaultProps.setProperty("Map", rt + "mapping\\mapping-default.xlsx");
		defaultProps.setProperty("Out", rt + "transformation\\flightman-qpulse-Transform-1.xslt");
		defaultProps.setProperty("ReportTypeName","Air / Ground Safety Report");
		defaultProps.setProperty("ReportedByPersonName","Chudy, Kamil");
		defaultProps.setProperty("ReportedDate","<xsl:value-of select=\"/AMT_DSE_Logbook/eReport/EntryDateTime\" />");
		configProps = new Properties(defaultProps);

		// loads properties from file
		InputStream inputStream = new FileInputStream("./config.properties");
		configProps.load(inputStream);
		inputStream.close();	
	}
	
	//SETS Config Properties and Transformer Variables using TEXTFIELD Values
	public void setProperties() {
		configProps.setProperty("Root", textRoot.getText());
		configProps.setProperty("Map", textMap.getText());
		configProps.setProperty("Out", textOut.getText());
		configProps.setProperty("ReportTypeName",textrtn.getText());
		configProps.setProperty("ReportedByPersonName",textrbpn.getText());
		configProps.setProperty("ReportedDate",textrd.getText());
		dt.Map = textMap.getText();
		dt.Root = textRoot.getText();
		dt.Out = textOut.getText();
		dt.setReportHeaderData(textrtn.getText(), textrbpn.getText(), textrd.getText());
	}
	
	//SETS AND SAVES CONFIG PROPERTIES using TEXTFIELD Values
	private void saveProperties() throws IOException {
		setProperties();
		OutputStream outputStream;
		outputStream = new FileOutputStream(configFile);
		configProps.store(outputStream, "Lot Data Mapper Application");
		String msg = "SAVING PROPERTIES WITH VALUES: \n\t 1 \t" + dt.Root+"\n \t 2 \t" + dt.Map+" \n\t 3 \t" + dt.Out+" \n\t 4 \t" + Trans.ReportTypeName+" \n\t 5 \t"+Trans.ReportedByPersonName+" \n\t 6\t"+Trans.ReportedDate + "\n PROPERTIIES SAVED";
		lg.l(msg);
		outputStream.close();
	}
	
	// File Chooser - UI Open the Mapping Source File.
	public String chooserFile(String def) {
		String res = "";
		String defpath = def;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(defpath));

		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".xlsx") || f.isDirectory();
			}

			public String getDescription() {
				return "Excel Mapping File";
			}
		});

		int r = chooser.showOpenDialog(new JFrame());
		if (r == JFileChooser.APPROVE_OPTION) {
			//String name = chooser.getSelectedFile().getName();
			String path = chooser.getSelectedFile().getPath();
			//System.out.println(name + "\n" + path);
			res = path;
		} else if (r == JFileChooser.CANCEL_OPTION) {
			JOptionPane.showMessageDialog(this, "User cancelled operation. No file was chosen.");
		} else if (r == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(this, "An error occured. No file was chosen.");
		} else {
			JOptionPane.showMessageDialog(this, "Unknown operation occured.");
		}
		return res;
	}

	// File Chooser - UI Open the TRANSFORMATION File.
	public String chooserFileTrans(String def) {
		String res = "";
		String defpath = def;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(defpath));

		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".xslt") || f.isDirectory();
			}

			public String getDescription() {
				return "XSLT FILE";
			}
		});

		int r = chooser.showOpenDialog(new JFrame());
		if (r == JFileChooser.APPROVE_OPTION) {
			//String name = chooser.getSelectedFile().getName();
			String path = chooser.getSelectedFile().getPath();
			//System.out.println(name + "\n" + path);
			res = path;
		} else if (r == JFileChooser.CANCEL_OPTION) {
			JOptionPane.showMessageDialog(this, "User cancelled operation. No file was chosen.");
		} else if (r == JFileChooser.ERROR_OPTION) {
			JOptionPane.showMessageDialog(this, "An error occured. No file was chosen.");
		} else {
			JOptionPane.showMessageDialog(this, "Unknown operation occured.");
		}
		return res;
	}

	// DIRECTORY CHOOSER
	public static String chooserDirectory(String def) {
		String res = "";
		String defpath = def;

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(defpath));
		// chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Choose an Directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			System.out.println("CURRENT DIR: " + chooser.getCurrentDirectory());
			System.out.println("SELECTED DIRECTORY() : " + chooser.getSelectedFile());
			res = chooser.getSelectedFile().getPath();
		} else {
			System.out.println("No Selection Made");
		}
		return res;
	}
}
