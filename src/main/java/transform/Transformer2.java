package transform;

/*Class Generates a xslt FILE to be used for transformations eReport processing. */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ListIterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
public class Transformer2 {

	String[][] convarray;
	static String xsltString;
	static String outputfilepath;
	static String inputfilepath;
	static String[] colname;
	static String ReportName;
	public String transVersion;
	public static String ReportTypeName;
	public static String ReportedByPersonName;
	public static String Title;
	public static String LocationName;
	public static String ReportedDate;
	public String excelPath;
	public String seperator;
	public String Map;
	public String Root;
	public String Out;
	public String Oname;
	public String Ver;
	public static Logger lg;
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");

	// Constructor
	public Transformer2(Logger lgg) throws DocumentException, IOException {
		lg = lgg;
		defaultVariables();
		defaultMapping();
		xsltString = "";
		createDirectoryTree();
	}
	
	//RUNS TRANSFORMATION 0
	public void runTrans(String is, String of) {
		if (importMap(is) == true) {
			exportExternal(of);
		}
	}
	//Imports Mapping Data From the EXCEL FILE 1
	public boolean importMap(String is) {
		boolean b = false;
		String msg = "T-IMPORT MAPPING DATA -";
		if (is != null && is != "" && is.endsWith(".xlsx")) {
			inputfilepath = is;
		} else {
			if (Map != null && Map != "" && Map.endsWith(".xlsx")) {
				inputfilepath = Map;
			} else {
				inputfilepath = getExcelFilePath();
			}
		}
		lg.l(msg);
		if (inputfilepath != null && inputfilepath != "" && inputfilepath.endsWith(".xlsx")) {
			convarray = generateSoureData(inputfilepath);
			//CopyFile(inputfilepath, "./src/main/resources/mappingRules/CurrentMappingFile.xlsx");
			//CopyFile(inputfilepath, Root+"\\mapping\\CurrentMappingFile.xlsx");
			CopyMap(inputfilepath);
			msg = "T-IMPORT MAP - INPORTING FILE [" + inputfilepath + "]";

		} else {
			msg = "T-IMPORT MAP - ERROR INPUT MAPPED TO INVALID FILE [" + inputfilepath + "]";

		}
		lg.l(msg);
		if (convarray != null && convarray.length > 0) {
			// EXPORT XSLT CONTENT
			msg = "MAPPING IMPORT SUCCESSFULL - FILE[" + inputfilepath + "]";
			lg.l(msg);
			b = true;
		}
		return b;
	}
	
	public void createDirectoryTree() {
		String rt = Root;
		String mp = Root+"\\mapping\\";
		String mpb = mp+"backups\\";
		String ot = Root+"\\transformation\\";
		String otb = ot+"backups\\";
		String dirlog = Root+"\\log\\";
		if(Root!=null&&Root!="") {
		lg.l("DIRECTORY TREE Started");
		createDir(rt);
		createDir(mp);
		createDir(mpb);
		createDir(ot);
		createDir(otb);
		createDir(dirlog);
		lg.l("DIRECTORY TREE COMPLETED");
		}else {
			lg.l("ERROR CREATING DIRECTORY TREE - PLEASE SET THE ROOT PATH");
		}
	}
	
	//Creates Directory From provided Path
	public void createDir(String Dirpath) {
		if(Dirpath!=null&&Dirpath!="") {
			new File(Dirpath).mkdirs();
		}
	}
	//Returns Current Date As String
	private static String getDateEvent() {
		String res = "";
		LocalDateTime now = LocalDateTime.now();
		res = dtf.format(now);
		return res;
	}
	
	// EXTENRAL FILE PUBLISHING
	public void exportExternal(String of) {
		String msg = "";
		// out
		String outpath = "";
		if (of != null && of.endsWith(".xslt")) {
			outpath = of;
		} else {
			outpath = Out;
		}
		if (outpath != null && outpath.endsWith(".xslt")) {
			msg = "OUTPUT MAPPED TO FILE [" + outpath + "]";
			lg.l(msg);

			try {
				generateXSLFileUser(xsltString, outpath);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			msg = "NO MAPPING DATA AVAILABLE [Try to Use another MAPPING FILE as the current is Invalid]";
			lg.l(msg);
		}
	}

	public void defaultMapping() {
		ReportTypeName = "Air / Ground Safety Report";
		ReportedByPersonName = "<xsl:value-of select=\"/AMT_DSE_Logbook/eReport/CreatedBy\" />";
		Title = "<xsl:value-of select=\"/AMT_DSE_Logbook/eReport/Title\" />";
		LocationName = "<xsl:value-of select=\"/AMT_DSE_Logbook/eReport/LocationName\" />";
		ReportedDate = "<xsl:value-of select=\"/AMT_DSE_Logbook/eReport/EntryDateTime\" />";
	}
	
	public void setReportHeaderData(String rtn, String rbpn, String rd) {
		if(rtn!=null&&rtn!=""&&rtn.length()>0) {
			ReportTypeName = rtn;
		}
		if(rtn!=null&&rbpn!=""&&rbpn.length()>0) {
			ReportedByPersonName = rbpn;
		}
		if(rd!=null&&rd!=""&&rd.length()>0) {
			ReportedDate = rd;
		}
	}
	
	public void defaultVariables() {
		transVersion = "7";
		String inputversion = "2";
		inputfilepath = "./src/main/resources/mappingRules/eReport-qpulse-" + inputversion + ".xlsx";
		outputfilepath = "./src/main/resources/tempTransformation/";
		seperator = "-";
		
	}

	public static String getHeader() {
		String xmlInfo = "<!-- [Author: Andrew Lipinski] [Email:andrew.lipinski@pwc.com] [Date:"+getDateEvent()+"] -->";
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" + xmlInfo
				+ "\n <xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:v1=\"http://www.qpulse.com/QPulseWebServices/v1.2/\">"
				+ "\n <xsl:param name=\"token\"/>"
				+ "\n <xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\" xml:space=\"preserve\"/>"
				+ "\n <xsl:template match=\"/\">" + "\n\t <v1:CreateNewOccurrence>"
				+ "\n\t\t <v1:token><xsl:value-of select=\"$token\"></xsl:value-of></v1:token>"
				+ "\n\t\t <v1:newOccurrence>" + "\n\t\t\t <v1:DataFields>"
				+ "\n\t\t\t <xsl:for-each select=\"/AMT_DSE_Logbook/eReport/DataEntry\">" + "\n\t\t\t <xsl:choose>";

		return header;
	}

	public static String getFooter() {
		String footer = "\n\t\t\t\t <xsl:otherwise></xsl:otherwise>" + "\n\t\t\t </xsl:choose>"
				+ "\n\t\t\t </xsl:for-each>" + "\n\t\t\t </v1:DataFields>" + "\n\t\t\t <v1:ReportTypeName>"
				+ ReportTypeName + "</v1:ReportTypeName>" + "\n\t\t\t <v1:ReportedByPersonName>" + ReportedByPersonName
				+ "</v1:ReportedByPersonName>"
				+ "\n\t\t\t <v1:ReportedDate><xsl:value-of select=\"/AMT_DSE_Logbook/eReport/EntryDateTime\" /></v1:ReportedDate>"
				+ "\n\t\t </v1:newOccurrence>" + "\n\t </v1:CreateNewOccurrence>" + "\n  </xsl:template>"
				+ "\n</xsl:stylesheet>";

		return footer;
	}

	public static String[][] generateSoureData(String inputfilepath) {
		lg.l("READING EXCEL MAPPING FILE ");
		// ESSENTIAL PROCESSING
		XSSFRow row;
		XSSFCell cell;

		String[][] value = null;
		
		double mone = 2.0;
		double mtwo = 1.0;
		int clo1 = 1;
		int clo2 = 1;
		int wd1 = 1;
		int wd2 = 1;
		// Temp Arrays used for Combined DataEntry Records
		String[] merlin1 = new String[6];
		String[] cloud = new String[4];
		String[] wind = new String[4];
		// MAIN OUTPUT STRING
		String outputXML = "";
		String outputlog = "";
		// PRINT HEADER
		outputXML = outputXML + getHeader();
		String pfx = "\n\t\t\t\t";
		try {
			// DATA SOURCE LOCATION IS READ HERE
			FileInputStream inputStream = new FileInputStream(inputfilepath);
			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

			// get sheet number Present in Excel WorkBook
			int sheetCn = workbook.getNumberOfSheets();
			int lastrow = 0;
			int totalrows = 0;
			int totalcol = 0;
			int rows = 0;
			int cells = 0;
			boolean rowProcessed = false;
			// create Array using total Number of rows
			for (int cno = 0; cno < sheetCn; cno++) {
				XSSFSheet sheeto = workbook.getSheetAt(cno);
				totalrows = totalrows + sheeto.getPhysicalNumberOfRows();
				if (cno == 0) {
					totalcol = sheeto.getRow(cno).getPhysicalNumberOfCells();
				}
			}
			value = new String[totalrows][totalcol + 1];
			
			// START Reading Excel data---------------------------------------
			for (int cn = 0; cn < sheetCn; cn++) {
				// Read Data For Each Excel Sheet
				XSSFSheet sheet = workbook.getSheetAt(cn);
				if (cn > 0) {
					int prevcn = cn - 1;
					XSSFSheet prevsheet = workbook.getSheetAt(prevcn);
					lastrow = lastrow + prevsheet.getPhysicalNumberOfRows() - 1;
					// get number of rows from sheet
					rows = sheet.getPhysicalNumberOfRows();
					// get number of cell from row
					cells = totalcol;
				} else {
					// get number of rows from sheet
					rows = sheet.getPhysicalNumberOfRows();
					// get number of cell from row
					cells = sheet.getRow(cn).getPhysicalNumberOfCells();
					if (cn == 0) {
						lastrow = 0;
					}
				}
				// String[] colrow = sheet.getRow(0);
				// Read Excel Data---------------------------------------------
				for (int r = 0; r < rows; r++) {
					// READS EXCEL FILE DATA
					// int rr = r - 1 + lastrow;
					int rr = r - 0 + lastrow;
					row = sheet.getRow(r); // Brings row from Excel Sheet
					rowProcessed = false;
					if (row != null && row != sheet.getRow(r - 1)) {
						for (int c = 0; c < cells; c++) {
							cell = row.getCell(c);
							if (cell != null) {
								// SAVES DATA TO ARRAY BASED ON EXCEL CELL DATA
								// TYPE
								switch (cell.getCellType()) {
								case Cell.CELL_TYPE_FORMULA:
									value[rr][c] = cell.getCellFormula();
									break;
								case Cell.CELL_TYPE_NUMERIC:
									double inttemp = cell.getNumericCellValue();
									int intt = (int) inttemp;
									value[rr][c] = "" + String.valueOf(intt);
									break;
								case Cell.CELL_TYPE_STRING:
									if (c == 6) {
										value[rr][c] = "" + convertDataType(cell.getStringCellValue());
									} else {
										value[rr][c] = "" + cell.getStringCellValue();
									}
									break;
								case Cell.CELL_TYPE_BLANK:
									value[rr][c] = null;
									break;
								case Cell.CELL_TYPE_ERROR:
									value[rr][c] = "" + cell.getErrorCellValue();
									break;
								default:
								}// END GETS CELL VALUE DEPENDING ON TYPE
							} // END Checks Cell NULLS

						} // END COLUMN and CELL DATA PROCESSING
							// if (value[rr][0] != null && value[rr][1] != null && value[rr][4] != null &&
							// r!=0)
						
						if (value[rr][0] != null && value[rr][1] != null && value[rr][4] != null && r != 0) {
							// DATA Mapping and String Genration
							String mrn = value[rr][0]; // Report Name
							String mfn = value[rr][1]; // Flightman Name
							String mfv = value[rr][2]; // Flightman Value
							String mpdf = value[rr][3]; // pdf column
							String mqn = value[rr][4]; // Qpulse Name
							String mqv = value[rr][5]; // Qpulse Value
							String mdt = value[rr][6]; // Data Type
							String mrln = value[rr][7]; // Reference List Name
							String mlidv = value[rr][8]; // List Item Display
															// Value
							// COMMENT LINE
							String cmt = getComment(rr, value[0], value[rr]);
							outputlog = outputlog+"\n"+cmt;
							outputXML = outputXML + cmt;
							// FORMAT PREFIXES, Prefix from Header
							String l1 = pfx;
							String l2 = l1 + "\t";
							String l3 = l2 + "\t";
							String l4 = l3 + "\t";
							String l5 = l4 + "\t";
							String mtest2 = ""; // " and Value='"+ value[rr][2]

							if (mqn != null
									&& ((mqn.contains("MERLIN")) || (mqn.contains("CLOUD")) || (mqn.contains("WIND")))
									&& (mfv == null || mfv == "")) {
								String merlinString1 = "";
								// String merlinString2 = "";
								String cloudString = "";
								String windString = "";
								double mNUMBER = -1;
								if (mqn != null && mqn.contains("MERLIN")) {
									mNUMBER = -1;
									if (mpdf.contains("ITEM")) {
										merlin1[0] = value[rr][1];
										merlin1[1] = mfv;
										if (mfn.contains("actechRef")) {
											mtwo--;
											mNUMBER = mtwo;
										}
									}
									if (mpdf.contains("TRIP NO")) {
										merlin1[2] = value[rr][1];
										merlin1[3] = value[rr][2];
									}
									if (mpdf.contains("DL SEQ NO")) {
										merlin1[4] = value[rr][1];
										merlin1[5] = value[rr][2];
										if (mfn.contains("field")) {
											mone--;
											mNUMBER = mone;
										}
									}

									if (merlin1[4] != null && merlin1[0] != null && merlin1[2] != null
											&& mNUMBER >= 0) {
										merlinString1 = l1 + "<xsl:when test=\"Name='" + merlin1[0] + "'" + " or Name='"
												+ merlin1[2] + "'" + " or Name='" + merlin1[4] + "'" + "\">" + l2
												+ "<xsl:choose>" + l3 + "<xsl:when test=\"Name='" + merlin1[0] + "'\" >"
												+ l4 + "<v1:DataField>" + l5 + "<v1:DataType>" + mdt + "</v1:DataType>"
												+ l5 + "<v1:Name>" + mqn + "</v1:Name>" + l5
												+ "<v1:Value><xsl:value-of select=\"" + "concat(../DataEntry[Name='"
												+ merlin1[0] + "']/Value,'/'" + ",../DataEntry[Name='" + merlin1[2]
												+ "']/Value,'/'" + ",../DataEntry[Name='" + merlin1[4]
												+ "']/Value)\"></xsl:value-of></v1:Value>" + l4 + "</v1:DataField>" + l3
												+ "</xsl:when>" + l3 + "<xsl:otherwise></xsl:otherwise>" + l2
												+ "</xsl:choose>" + l1 + "</xsl:when>";

										outputXML = outputXML + merlinString1;
										merlin1 = new String[6];
									}
								} // end of MERLIN
								if (mqn.contains("CLOUD") || mpdf.contains("20. CLOUD")) {
									int clonum = -1;
									if (mrn.contains("ASRGSR Bird Strike")
											|| (mrn.contains("Bird") && mrn.contains("Strike"))) {

										if (mfn.contains("field19")) {

											cloud[0] = value[rr][1];
											cloud[1] = value[rr][2];
										}
										if (mfn.contains("field17")) {

											cloud[2] = value[rr][1];
											cloud[3] = value[rr][2];
										}
										if (cloud[2] != null && cloud[0] != null) {
											clo1--;
											clonum = clo1;
										}

									} else {
										if (mfn.contains("field20")) {
											cloud[0] = value[rr][1];
											cloud[1] = value[rr][2];
										}
										if (mfn.contains("field18")) {
											cloud[2] = value[rr][1];
											cloud[3] = value[rr][2];
										}
										if (cloud[2] != null && cloud[0] != null) {
											clo2--;
											clonum = clo2;
										}
									}

									if (cloud[2] != null && cloud[0] != null && clonum >= 0) {
										String cloud1 = cloud[0];
										String cloud2 = cloud[2];
										cloudString = l1 + "<xsl:when test=\"Name='" + cloud1 + "' or Name='" + cloud2
												+ "'\">" + l2 + "<xsl:choose>" + l3 + "<xsl:when test=\"Name='" + cloud1
												+ "'\" >" + l4 + "<v1:DataField>" + l5 + "<v1:DataType>" + mdt
												+ "</v1:DataType>" + l5 + "<v1:Name>" + mqn + "</v1:Name>" + l5
												+ "<v1:Value><xsl:value-of select=\"" + "concat(../DataEntry[Name='"
												+ cloud1 + "']/Value,'/'," + "../DataEntry[Name='" + cloud2
												+ "']/Value)\"></xsl:value-of></v1:Value>" + l4 + "</v1:DataField>" + l3
												+ "</xsl:when>" + l3 + "<xsl:otherwise></xsl:otherwise>" + l2
												+ "</xsl:choose>" + l1 + "</xsl:when>";
										outputXML = outputXML + cloudString;
										cloud = new String[4];
									}
								} // END OF CLOUD
								if (mqn.contains("WIND") && (mfv == null || mfv == "")
										&& mpdf.contains("20. WX ACTUAL")) {
									int wdnum = -1;
									if (mrn.contains("ASRGSR Bird Strike")) {
										if (mfn.contains("field15")) {
											wind[0] = value[rr][1];
											wind[1] = value[rr][2];
										}
										if (mfn.contains("field16")) {
											wind[2] = value[rr][1];
											wind[3] = value[rr][2];
										}
										if (wind[2] != null && wind[0] != null) {
											wd1--;
											wdnum = wd1;
										}
									} else {
										if (mfn.contains("field16")) {
											wind[0] = value[rr][1];
											wind[1] = value[rr][2];
										}
										if (mfn.contains("field17")) {
											wind[2] = value[rr][1];
											wind[3] = value[rr][2];
										}
										if (wind[2] != null && wind[0] != null) {
											wd2--;
											wdnum = wd2;
										}
									}
									if (wind[2] != null && wind[0] != null && wdnum >= 0) {
										String wind1 = wind[0];
										String wind2 = wind[2];
										windString = l1 + "<xsl:when test=\"Name='" + wind1 + "' or Name='" + wind2
												+ "'\">" + l2 + "<xsl:choose>" + l3 + "<xsl:when test=\"Name='" + wind1
												+ "'\" >" + l4 + "<v1:DataField>" + l5 + "<v1:DataType>" + mdt
												+ "</v1:DataType>" + l5 + "<v1:Name>" + mqn + "</v1:Name>" + l5
												+ "<v1:Value><xsl:value-of select=\"" + "concat(../DataEntry[Name='"
												+ wind1 + "']/Value,'/'," + "../DataEntry[Name='" + wind2
												+ "']/Value)\"></xsl:value-of></v1:Value>" + l4 + "</v1:DataField>" + l3
												+ "</xsl:when>" + l3 + "<xsl:otherwise></xsl:otherwise>" + l2
												+ "</xsl:choose>" + l1 + "</xsl:when>";
										outputXML = outputXML + windString;
										wind = new String[4];
									}
								} // END OF WIND

							} else {

								// Genral Mapping Condition. If Both Sides are
								// Null
								// then There Mapping cannot take place
								if ((mfn != null || mfn != "") && (mqn != null || mqn != "")) {
									String sval = "";
									String sdt = "";
									String sname = "";
									mtest2 = mfv;
									String stest = "";
									String xmlrow = "";

									// TEST
									// CONDITIONS---------------------------

									String smrn = " and ../ReportName='" + mrn + "'";
									if (mrn == null || mrn == "" || mrn == " ") {
										smrn = "";
									}
									if (mtest2 == null || mtest2 == "" || mtest2 == " ") {
										stest = l1 + "<xsl:when test=\"Name='" + mfn + "'" + smrn + "\">";
									} else {
										if (mfv.equals("1") || mfv.equals("2-10") || mfv.equals("11-100")) {
											stest = l1 + "<xsl:when test=\"Name='" + mfn + "'" + smrn + " and (Value='"
													+ mfv + "')" + "\">";
										} else {
											// copy of mfv
											String mfv1 = "Value='" + mfv + "'";
											// SentenceCase mfv
											String mfv2 = sentCase(mfv);
											if (mfv2.contentEquals(mfv)) {
												mfv2 = "";
											} else {
												mfv2 = "or Value='" + mfv2 + "'";
											}
											// All Capitals
											String mfv3 = mfv.toUpperCase();
											if (mfv3.contentEquals(mfv) || mfv3.contentEquals(mfv1)
													|| mfv3.contentEquals(mfv2)) {
												mfv3 = "";
											} else {
												mfv3 = " or Value='" + mfv.toUpperCase() + "'";
											}
											// all small caps
											String mfv4 = mfv.toLowerCase();
											if (mfv4.contentEquals(mfv) || mfv4.contentEquals(mfv2)
													|| mfv4.contentEquals(mfv3)) {
												mfv4 = "";
											} else {
												mfv4 = "or Value='" + mfv.toLowerCase() + "'";
											}
											stest = l1 + "<xsl:when test=\"Name='" + mfn + "'" + smrn + " and (" + mfv1
													+ mfv2 + mfv3 + mfv4 + ")" + "\">";
										}
									}
									// DATATYPE----------------------------
									sdt = "";
									if (mdt != null && mdt != "") {
										sdt = l3 + "<v1:DataType>" + mdt + "</v1:DataType>";
									} else {
										String tempdt = "String";
										sdt = l3 + "<v1:DataType>" + tempdt + "</v1:DataType>";
									}
									// NAME -----------------------------------
									sname = "";
									if (mqn != null && mqn != "") {
										sname = l3 + "<v1:Name>" + mqn + "</v1:Name>";
									}

									// ListItemDisplayValue - lidv-----------------------------------
									String slidv = "";
									if (mlidv != null) {
										slidv = l3 + "<v1:ListItemDisplayValue>" + mlidv + "</v1:ListItemDisplayValue>";
									} else {
										slidv = "";
									}
									// ReferenceListName - rln-------------------------------------
									String srln = "";
									if (mrln != null) {
										srln = l3 + "<v1:ReferenceListName>" + mrln + "</v1:ReferenceListName>";
									} else {
										srln = "";
									}

									// VALUE----------------------------------------
									if (mfv == null || mfv == "" || mfv == " ") {
										sval = l3 + "<v1:Value>" + "<xsl:value-of select=\"Value\"></xsl:value-of>"
												+ "</v1:Value>";
									} else {
										// both NOT null thus test for both
										if (mfv == "1" && mqv != null) {
											sval = l3 + "<v1:Value>" + mqv + "</v1:Value>";
										}
										if (mfv != null && mfv != "1") {
											sval = l3 + "<v1:Value>" + mqv + "</v1:Value>";
										}
									}

									// fv null and qv not null FORMULA

									// SPECIAL CASE: Medium BIRDS SEEN TO MIDDLE
									if (mfv == "Medium" && mqv == "MIDDLE" && mqn == "SIZE OF BIRDS"
											&& mdt.equalsIgnoreCase("FIXEDLIST")) {
										sval = l3 + "<v1:Value>" + "MIDDLE" + "</v1:Value>";
									}

									xmlrow = stest + l2 + "<v1:DataField>" + sdt + sname + slidv + srln + sval + l2
											+ "</v1:DataField>" + l1 + "</xsl:when>";
									if (rowProcessed) {
										break;
									} else {
										outputXML = outputXML + xmlrow;
										rowProcessed = true;
									}

								} else {
									break;
								}
							} // END OF GENERAL CASE
						} // END of Rules
					} // Reads Data PER ROW
				} // END OF EXEC DATA READ FOR
			} // end of DATA ENTRY-----------------------------------
			String footerpfx = "\n\t\t\t\t";
			@SuppressWarnings("unused")
			String slocName = "";
			@SuppressWarnings("unused")
			String stitle = "";
			if (LocationName == "" || LocationName == null) {
				slocName = footerpfx + "<v1:LocationName/>";
			} else {
				slocName = footerpfx + "<v1:LocationName>" + LocationName + "</v1:LocationName>";
			}
			if (Title == null || Title == "") {
				stitle = footerpfx + "<v1:Title/>";
			} else {
				stitle = footerpfx + "<v1:Title>" + Title + "</v1:Title>";
			}
			outputXML = outputXML + getFooter();
			String cmtMapProp = getProcessStats(value);
					outputXML = outputXML + cmtMapProp;
			xsltString = outputXML;
			
			outputlog=outputlog+cmtMapProp;
			lg.l(outputlog);
			lg.l("EXCEL MAPPING FILE IMPORT COMPLETE");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(xsltString);
		return value;
	}
	// END OF DATA IMPORT FROM
	// EXCEL----------------------------------------------------------------------------------------------

	//Prints Processing Statistics 
	public static String getProcessStats(String[][] arr) {
		String res = "";
		String coln = "";
		int coll = 0;
		for(int i=0;i<arr[0].length;i++) {
			if(arr[0][i]!=null) {
				coll=coll+1;
				coln = coln+"{"+coll+"}"+arr[0][i]+" ";
			}
			
		}
		res = "\n <!--"
				+" END OF TRANSFORMATION FILE: Processing Stats:"
				+"\n User: ["+System.getProperty("user.name")+"]"
				+"\n Total Records"+"["+arr.length +"] "
				+"\n Total Columns"+"["+coll+"] ["+coln+"]"
				+ " -->";
		lg.l(res);
		return res;
	}
	
	
	// Searches and allowing for Offsets
	public static int searchMappingRules(String[][] combinedArray, int start, int end, String ReportName,
			String flightmanName, String qpulseName, String pdfName) {
		int row = -1;
		String[][] a = combinedArray;
		for (int r = start; r < end; r++) {
			if (a[r] != null) {
				if (a[r][0] != null && a[r][4] != null && a[r][0].contains(ReportName)
						&& a[r][4].contains(qpulseName)) {

					if (a[r][3] != null && a[r][3].contains(pdfName)) {
						row = r;
						break;
					} else {
						row = 0;
					}
				}
			}
		}
		return row;
	}

	// Returns a string XSLT TRansformation Segment for Repeatable Flightman
	// DataEntries
	// Combines Strings Need for variables during processing
	public static String printCombinedData(String prefix, String qpulseName, String name1, String name2, String name3) {
		String pfx = "";
		if (prefix == null) {
			pfx = "\n\t\t\t\t\t";
		} else {
			pfx = prefix;
		}
		String qn = qpulseName;
		String val = "";

		// VALUE------------------------
		if (name1 != null || name1 != "") {
			val = "../DataEntry[Name='" + name1 + "']/Value";
			if (name2 != null || name2 != "") {
				val = "concat(/DataEntry[Name='" + name1 + "']/Value" + ",'/'," + "../DataEntry[Name='" + name2
						+ "']/Value" + ")";
				if (name3 != null || name3 != "") {
					val = "concat(../DataEntry[Name='" + name1 + "']/Value" + ",'/'," + "../DataEntry[Name='" + name2
							+ "']/Value" + ",'/'," + "../DataEntry[Name='" + name3 + "']/Value" + ")";
				}
			}
		}

		String l1 = pfx + "\t";
		String l2 = l1 + "\t";
		String l3 = l2 + "\t";
		String printString = l1 + "<xsl:when test=\"Name='" + name1 + "'" + "\">" + l2 + "<xsl:choose>" + l3
				+ "<xsl:when test=\"Name='" + name1 + "'\" >" + l3 + "\t" + "<v1:DataField>" + l3 + "\t\t"
				+ "<v1:DataType>String</v1:DataType>" + l3 + "\t\t" + "<v1:Name>" + qn + "</v1:Name>" + l3 + "\t\t"
				+ "<v1:Value><xsl:value-of select=\"" + val + "\"></xsl:value-of></v1:Value>" + l3 + "\t"
				+ "</v1:DataField>" + l3 + "</xsl:when>" + l3 + "<xsl:otherwise></xsl:otherwise>" + l2 + "</xsl:choose>"
				+ l1 + "</xsl:when>";
		return printString;
	}

	// Returns Data From Array [row][col]
	public String getDataCell(int r, int c) {
		String convString = convarray[r][c].toString();
		return convString;
	}

	public int searchMap(String fr, String fn) {
		int result = -1;
		// String res = "No Data Found";
		if (fr != null && fn != null && convarray != null) {
			for (int r = 0; r < convarray.length; r++) {
				for (int c = 0; c < 3; c++) {
					if (convarray[r][c] != null && convarray[r][0].contains(fr) && convarray[r][1].contains(fn)) {
						result = r;
					}
				}
			}
		} else {
			result = -1;
		}
		return result;
	}

	// Returns Excel File Column Names
	public String[][] getColumnNames(String[][] DataArray) {
		String[] col = DataArray[0];
		String[][] result = new String[10][4];
		int rc = 0;
		for (int c = 0; c < col.length; c++) {
			if (col[c] != null) {
				if (col[c].contains("Report") && col[c].contains("Name")) {
					rc = 0;
					result[rc][0] = "ReportName";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("Flightman") && col[c].contains("Name")) {
					rc = 1;
					result[rc][0] = "FlightmanName";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("Flightman") && col[c].contains("Value")) {
					rc = 2;
					result[rc][0] = "FlightmanValue";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("PDF") || col[c].equalsIgnoreCase("PDF")) {
					rc = 3;
					result[rc][0] = "PDF";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("Qpulse") && col[c].contains("Name")) {
					rc = 4;
					result[rc][0] = "QpulseName";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("Qpulse") && col[c].contains("Value")) {
					rc = 5;
					result[rc][0] = "QpulseValue";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("Data") && col[c].contains("Type")) {
					rc = 6;
					result[rc][0] = "DataType";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("Reference") && col[c].contains("ListName")) {
					rc = 7;
					result[c][0] = "ReferenceListName";
					result[c][1] = Integer.toString(rc);
					result[c][2] = col[c];
					result[c][3] = Integer.toString(c);
				}
				if (col[c].contains("ListItem") && col[c].contains("DisplayValue")) {
					rc = 8;
					result[rc][0] = "ListItemDisplayValue";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
				if (col[c].contains("Element") && col[c].contains("Identifier")) {
					rc = 9;
					result[rc][0] = "ElementIdentifier";
					result[rc][1] = Integer.toString(rc);
					result[rc][2] = col[c];
					result[rc][3] = Integer.toString(c);
				}
			}
		}
		return result;
	}

	// RETURNS THE QPULSE NAME, SEARCHING BY REPORT NAME, FLIGHTMAN NAME,
	// FLIGHTMAN VALUE and QPULSE NAME
	public String getQpulseNameFromString(String flightmanReport, String flightmanName, String flightmanValue) {
		String res = "";
		for (int r = 0; r < convarray.length; r++) {
			for (int c = 0; c < convarray[0].length; c++) {
				if (flightmanName.equalsIgnoreCase(convarray[r][1]) && flightmanReport.equalsIgnoreCase(convarray[r][0])
						&& flightmanValue.equalsIgnoreCase(convarray[r][2])) {
					res = convarray[r][4];
				}
			}
		}
		return res;
	}

	// RETURNS THE QPULSE NAME, SEARCHING BY REPORT NAME, FLIGHTMAN NAME and
	// FLIGHTMAN VALUE
	public String getQpulseName(String flightmanReport, String flightmanName, String flightmanValue) {
		String res = "";
		for (int r = 0; r < convarray.length; r++) {
			for (int c = 0; c < convarray[0].length; c++) {
				if (flightmanName.equalsIgnoreCase(convarray[r][1]) && flightmanReport.equalsIgnoreCase(convarray[r][0])
						&& flightmanValue.equalsIgnoreCase(convarray[r][2])) {
					res = convarray[r][4];
				}
			}
		}
		return res;
	}

	// RETURNS THE QPULSE VALUE, SEARCHING BY REPORT NAME, FLIGHTMAN NAME,
	// FLIGHTMAN VALUE and QPULSE NAME
	public String getQpulseValue(String flightmanReport, String flightmanName, String flightmanValue,
			String qpulseName) {
		String res = "";
		for (int r = 0; r < convarray.length; r++) {
			for (int c = 0; c < convarray[0].length; c++) {
				if (flightmanName.equalsIgnoreCase(convarray[r][1]) && flightmanReport.equalsIgnoreCase(convarray[r][0])
						&& flightmanValue.equalsIgnoreCase(convarray[r][2])
						&& flightmanValue.equalsIgnoreCase(convarray[r][4])) {
					res = convarray[r][5];
				}
			}
		}
		return res;
	}

	/*
	 * returns row number, Searching All the CELLS FOR THE GIVEN String IN: String,
	 * Search all the cells for Equivalent Content OUT: [result<0]NO DATA FOUND OUT:
	 * [result>0] Result equals the Row Number Containing the Cell whose content
	 * Matches the Search String.
	 */
	public int getRowNumber(String SearchString) {
		int result = -1;
		if (SearchString != null && convarray != null) {
			for (int r = 0; r < convarray.length; r++) {
				for (int c = 0; c < convarray[0].length; c++) {
					if (convarray[r][c] != null && SearchString.equalsIgnoreCase(convarray[r][c])) {
						result = r;
					}
				}
			}
		} else {
			result = -1;
		}
		return result;
	}

	// RETURNS THE QPULSE NAME, SEARCHING BY REPORT NAME AND FLIGHTMAN NAME
	public String getQpulse(String report, String flightman) {
		String result = "";
		// String res = "No Data Found";
		if (flightman != null && report != null && convarray != null) {
			for (int r = 0; r < convarray.length; r++) {
				for (int c = 0; c < convarray[0].length; c++) {
					if (convarray[r][c] != null && flightman.equalsIgnoreCase(convarray[r][1])
							&& report.equalsIgnoreCase(convarray[r][0])) {
						result = convarray[r][3];
					}
				}
			}
		} else {
			result = "";
		}
		return result;
	}

	// SPLITS TEXT PRESENT IN THE ORIGINAL Template Excel File
	public static String splitValue(String[][] arr, int c, int r) {
		String res = arr[r][c];
		String s1 = "";
		String s2 = "";
		String sc = "none";
		String printTab = "\t";
		if (res.contains("->")) {
			sc = "->";
		} else {
			if (res.contains("- >")) {
				sc = "- >";
			}
		}
		if (sc != null && sc != "none") {
			String[] splittext = res.split(sc);
			if (splittext[0] != null) {
				s1 = splittext[0];
			}
			if (splittext[1] != null) {
				s2 = splittext[1];
			}
			res = s1 + printTab + s2;
		}
		return res;
	}

	// Search and Returns Column Names Present in the Data Source EXCEL File
	public String getColumnNames() {
		String res = "id,Report,Flightman,fvalue,pdf,qpulse,qvalue,type,reflist,displayName,elementid";
		for (int i = 0; i < convarray[0].length; i++) {
			res = res + convarray[0][i];
		}
		return res;
	}

	// Returns the Column Name
	public String getColumnName(int c) {
		String res = "";
		res = convarray[0][c];
		return res;
	}

	// Returns Row from Excel Data Array
	public String[] getMapData(int r) {
		int l = convarray[r].length;
		String[] res = new String[l];
		for (int i = 0; i < l; i++) {
			res[i] = convarray[r][i];
		}
		return res;
	}

	// DataField Type Processing
	public static String convertDataType(String dt) {
		String res = "Unknown";
		if (dt != null || dt != "") {
			ArrayList<String> dta = new ArrayList<String>();
			dta.add("String");
			dta.add("Numeric");
			dta.add("Date");
			dta.add("Boolean");
			dta.add("FixedList");
			dta.add("ListReference");
			ListIterator<String> itr = dta.listIterator();
			while (itr.hasNext()) {
				String curr = itr.next();
				if (curr != null) {
					if (curr.toLowerCase().indexOf(dt.toLowerCase()) != -1) {
						if (res == "Unknown") {
							res = curr;
							break;
						}
					}
				}
			} // for
		}
		return res;
	}

	// CALLED WHEN EXCEL FILE DATA IS IMPORTED
	// CREATES XSLT FILE USING CONTENT GENERATED IN generateSourceData Method
	public boolean generateXSLFile(String sxml) throws DocumentException, IOException {
		Document d = DocumentHelper.parseText(sxml);
		File file = new File(Out);
		if (!file.exists()) {
			file.createNewFile();
		}		
		FileWriter out = new FileWriter(file);
		d.setXMLEncoding("UTF-8");
		d.write(out);
		out.close();
		CopyFile(file.getAbsolutePath(), Root + "\\transformation\\backups\\"+getDateEvent()+file.getName());
		return true;
	}
	//CREATES XSLT FILE USING CONTENT STRING AND PATH STRING
	public boolean generateXSLFileUser(String sxml, String pth) throws DocumentException, IOException {
		String fn = pth;
		Document d = DocumentHelper.parseText(sxml);
		String filename = fn;
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter out = new FileWriter(file);
		d.setXMLEncoding("UTF-8");
		d.write(out);
		out.close();
		CopyFile(filename,Root+"\\transformation\\backups\\Trans_"+getDateEvent()+file.getName());
		return true;
	}
	// SETS THE CURRENT FILE VERSION NUMBER
	public String setTransVersion(String srcfilepath) {
		String ver = "1";
		int tg = 0;
		String[] version = srcfilepath.split(".x", 2);
		if (version[1] != null) {
			String[] version2 = version[1].split("-", 3);
			if (version2[2] != null) {
				ver = version2[2];
				tg = Integer.parseInt(ver);
				tg = tg + 1;
				// ver = ver+1;
			}
		}
		transVersion = String.valueOf(tg);
		return ver;
	}

	public String getTransSubVersion(String outfilepath) {
		String ver = "1";
		String[] version = outfilepath.split("-");
		if (version[version.length] != null) {
			ver = version[1];
			String[] subver = version[1].split("-");
			if (subver[subver.length] != null) {
				int sv = Integer.parseInt(subver[1]) + 1;
				ver = ver + "-" + String.valueOf(sv);
			}
		}
		return ver;
	}

	// Returns the Newest Verion of the Excel Source File
	// STEP 1 START OF XSLT FILE CREATION
	public String getExcelFilePath() {
		String curver = "";
		int v = 0;
		int sv = 0;
		String res = "";
		String[] a = getExcelFiles();
		for (int i = 0; i < a.length; i++) {
			if (a[i] != null) {
				String[] version = a[i].split(".xls");
				if (version[0] != null) {
					String[] version2 = version[0].split("-", 3);
					if (version2[0] != null && version2[1] != null && version2[2] != null) {
						if (version2[0].contains("mapping") && version2[1].contains("rules")) {
							if (version2[2] == null) {
								v = 1;
							} else {
								v = Integer.parseInt(version2[2]);
								curver = version2[2];
							}
						}

					}
					if (v <= sv) {
						v = sv;
						res = a[i];
						transVersion = curver;
						inputfilepath = System.getProperty("user.dir")+"\\mapping\\"+res+"-"+ transVersion + ".xlsx";
						curver = inputfilepath + transVersion + ".xlsx";
						//System.out.println(inputfilepath);
					}
				}
			}
		}
		//System.out.println(curver);
		return curver;
	}

	// returns list of
	// files------------------------------------------------------
	public String[] getExcelFiles() {
		File file = new File("src/main/resources/mappingRules/");
		String[] files = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".xlsx")) {
					return true;
				} else {
					return false;
				}
			}
		});
		// for (String f : files) {
		// System.out.println(f);
		// }
		return files;
	}

	// RETURNS OUPUT DIRECTY FILE NAMES
	public String[] getTransformFiles() {

		File file = new File("./src/main/resources/tempTransformation/");
		//File file = new File(Out);
		String[] files = file.list(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".xslt")) {
					return true;
				} else {
					return false;
				}
			}
		});
		

		return files;
	}

	// PROVIDES INFO FOR NAMING OF CONSECUTIVE OUTFILES
	public String getTransformFile() {
		
		String outputfile = Out;
		
		return outputfile;
	}

	public String getTransVersion() {
		return transVersion;
	}

	public String getXsltString() {
		return xsltString;
	}

	public void setXsltString(String xsltString) {
		Transformer2.xsltString = xsltString;
	}

	//RETURNS TEXT with Sentence Case
	public static String sentCase(String in) {
		String res = "";
		if (in != null) {
			String ins = in;
			if (ins.contains("-") || ins.contains(" ")) {
				String sep = "-";
				if (ins.contains(" ")) {
					sep = " ";
				}
				String[] insa = ins.split(sep);
				String a = "";
				for (int i = 0; i < insa.length; i++) {
					if (insa[i] != null) {
						if (i == insa.length - 1) {
							sep = "";
						}
						a = a + insa[i].substring(0, 1).toUpperCase() + insa[i].substring(1).toLowerCase() + sep;
					}
				}
				res = a;
			} else {
				res = ins.substring(0, 1).toUpperCase() + ins.substring(1).toLowerCase();
			}
			if (res.contentEquals(in)) {
				res = in;
			}
		}
		return res;
	}
	
	//GENERATES COMMENT including Mapping Data
	public static String getComment(int rownum, String[] colnames, String[] rowdata) {
		String res = "";
		String comline = "";
		String prefix = "\n\t" + "<!--[" + rownum + "]";
		String sufix = "-->";
		for (int cline = 0; cline < colnames.length; cline++) {
			String columnName = colnames[cline];
			String rowValue = rowdata[cline];
			if (columnName != null) {
				if (rowValue == null) {
					rowValue = "n\\a";
				}
				comline = comline + "[" + columnName + ":" + rowValue + "]";
			}
		}
		// System.out.print(comline);
		//lg.l(comline);
		res = prefix + comline + sufix;
		return res;
	}

	public void CopyFile(String in, String out) {
		Path source = Paths.get(in);
		Path destination = Paths.get(out);
		destination.getParent();
		try {
			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
			lg.l("FILE COPIED SUCCESSFULLY in["+in+"] out["+out+"]");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			lg.l("FILE COPY ERROR - " + e.getMessage());
		}
	}
	
	public void CopyMap(String in) {
		String i = in;
		String o = Root+"\\mapping\\backups\\"+"map_"+getDateEvent()+".xlsx";
		//CopyFile(i,Root+"transformation\\backups\\"+file.getName());
		CopyFile(i,o);
	}
	// END
}
