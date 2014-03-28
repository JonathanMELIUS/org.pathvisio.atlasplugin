package org.pathvisio.atlasplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.bridgedb.IDMapperException;
import org.bridgedb.rdb.construct.DBConnector;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.preferences.GlobalPreference;
import org.pathvisio.core.preferences.PreferenceManager;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.core.util.ProgressKeeper;
import org.pathvisio.core.util.ProgressKeeper.ProgressEvent;
import org.pathvisio.core.util.ProgressKeeper.ProgressListener;
import org.pathvisio.data.DataException;
import org.pathvisio.data.DataInterface;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.data.DBConnectorSwing;
import org.pathvisio.desktop.util.RowNumberHeader;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorSet;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Visualization;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.gexplugin.GexTxtImporter;
import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.gui.DataSourceModel;
import org.pathvisio.gui.util.PermissiveComboBox;
import org.pathvisio.visualization.plugins.ColorByExpression;
import org.pathvisio.visualization.plugins.DataNodeLabel;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.nexes.wizard.Wizard;
import com.nexes.wizard.WizardPanelDescriptor;

public class AtlasWizard extends Wizard 
{
	private ImportInformation importInformation;

	private FilePage fpd;
	private ColumnPage cpd;
	private ImportPage ipd;

	private String experiment;
	private static final String COMMIT_ACTION = "commit";

	private final PvDesktop standaloneEngine;
	


	public AtlasWizard (PvDesktop standaloneEngine)
	{
		this.standaloneEngine = standaloneEngine;
		importInformation = new ImportInformation();
		experiment="";
		fpd = new FilePage();
		cpd = new ColumnPage();
		ipd = new ImportPage();
		

		getDialog().setTitle ("Atlas Expression data import wizard");
		registerWizardPanel(fpd);
		registerWizardPanel(cpd);
		registerWizardPanel(ipd);
		setCurrentPanel(FilePage.IDENTIFIER);
	}
	private class FilePage extends WizardPanelDescriptor implements ActionListener
	{
		public static final String IDENTIFIER = "FILE_PAGE";
		static final String ACTION_OUTPUT = "output";
		static final String ACTION_GDB = "gdb";

		private JTextField expInput;
		private JTextField txtOutput;
		private JTextField txtGdb;
		private JButton btnGdb;
		private JButton btnOutput;
		boolean txtOutputComplete;
		
		ArrayList<String> idExperiment;

		public void aboutToDisplayPanel()
		{
			getWizard().setNextFinishButtonEnabled(txtOutputComplete);
			getWizard().setPageTitle ("Choose an experiments and the file locations");
		}

		public FilePage()
		{
			super(IDENTIFIER);
		}

		public Object getNextPanelDescriptor()
		{
			return ColumnPage.IDENTIFIER;
		}

		public Object getBackPanelDescriptor()
		{
			return null;
		}
		/**
		 * Check the text output field. If it's empty the user can't continue
		 */
		public void updateTxt(){
			ArrayList<String> queryList = new ArrayList<String>(Arrays.asList(expInput.getText().split(" ")));
			if ( (!txtOutput.getText().equals(""))
								&& (idExperiment.containsAll(queryList)) ) {
				txtOutputComplete=true;
			}
			else {				
				txtOutputComplete = false;					
			}
			getWizard().setNextFinishButtonEnabled(txtOutputComplete);
		}

		protected JPanel createContents()
		{

			expInput = new JTextField(40);
			txtOutput = new JTextField(40);
			txtGdb = new JTextField(40);
			btnGdb = new JButton ("Browse");;
			btnOutput = new JButton ("Browse");
			idExperiment = new ArrayList<String>();
			expInput.setFocusTraversalKeysEnabled(false);
			String sparqlEndpoint = "http://www.ebi.ac.uk/rdf/services/atlas/sparql";
			String sparqlQuery =
							"PREFIX dcterms: <http://purl.org/dc/terms/>"+
							"PREFIX atlasterms: <http://rdf.ebi.ac.uk/terms/atlas/>"+
							"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#>"+
							"SELECT DISTINCT ?id  WHERE {"+
							"?experiment a atlasterms:Experiment ."+
							"?experiment dcterms:identifier ?id."+   
							"?experiment atlasterms:hasAnalysis ?analysis ."+
							"}";
			
			Query query = QueryFactory.create(sparqlQuery, Syntax.syntaxARQ) ;

			ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString(query.toString());

			QueryEngineHTTP httpQuery = new QueryEngineHTTP(sparqlEndpoint,parameterizedSparqlString.asQuery());
			// execute a Select query
			ResultSet results = httpQuery.execSelect();
			while (results.hasNext()) {
				QuerySolution solution = results.next();
				// get the value of the variables in the select clause
				String id = solution.get("id").asLiteral().getLexicalForm();
				idExperiment.add(id);
			}
		
			System.out.println(idExperiment.size());
			// Our words to complete
			
			/*
			Auto autoComplete = new Auto(expInput, idExperiment);


			expInput.getDocument().addDocumentListener(autoComplete);

			// Maps the tab key to the commit action, which finishes the autocomplete
			// when given a suggestion
			expInput.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
			expInput.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), COMMIT_ACTION);
			expInput.getInputMap().put(KeyStroke.getKeyStroke("SPACE"), COMMIT_ACTION);
			expInput.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());
			 */

			FormLayout layout = new FormLayout (
					"right:pref, 3dlu, pref, 3dlu, pref",
					"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");

			PanelBuilder builder = new PanelBuilder(layout);
			builder.setDefaultDialogBorder();
			CellConstraints cc = new CellConstraints();			

			builder.addLabel("Experiment ID", cc.xy(1,3));
			builder.add (expInput, cc.xy (3,3));
			builder.addLabel ("Output file", cc.xy (1,7));
			builder.add (txtOutput, cc.xy (3,7));
			builder.add (btnOutput, cc.xy (5,7));
			builder.addLabel ("Gene database", cc.xy (1,9));
			builder.add (txtGdb, cc.xy (3,9));
			builder.add (btnGdb, cc.xy (5,9));

			btnOutput.addActionListener(this);
			btnOutput.setActionCommand(ACTION_OUTPUT);
			btnGdb.addActionListener(this);
			btnGdb.setActionCommand(ACTION_GDB);

			txtGdb.setText(
					PreferenceManager.getCurrent()
					.get(GlobalPreference.DB_CONNECTSTRING_GDB)
					);
			
			expInput.getDocument().addDocumentListener(new DocumentListener()
			{
				public void changedUpdate(DocumentEvent arg0)
				{
					updateTxt();
				}

				public void insertUpdate(DocumentEvent arg0)
				{
					updateTxt();
				}

				public void removeUpdate(DocumentEvent arg0)
				{
					updateTxt();
				}

			});
			txtOutput.getDocument().addDocumentListener(new DocumentListener()
			{
				public void changedUpdate(DocumentEvent arg0)
				{
					updateTxt();
				}

				public void insertUpdate(DocumentEvent arg0)
				{
					updateTxt();
				}

				public void removeUpdate(DocumentEvent arg0)
				{
					updateTxt();
				}

			});
			return builder.getPanel();
		}

		public void aboutToHidePanel()
		{
			String outFile = null;
			File f = new File(txtOutput.getText());
			String path = "/home/mael/outpute.txt";
			File file = null;
			PrintWriter f0 = null;			
			try {
				f.getCanonicalPath();
				f=FileUtils.replaceExtension(f, "pgex");
				outFile = f.getCanonicalPath();
				
				file = new File(path);
				f0 = new PrintWriter(new FileWriter(file));
				f0.println("id"+"\t"+"Experiment_ID"+"\t"+"propertyValue"+"\t"+"pValue"+"\t"+"tStat");
				f0.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			importInformation.setGexName (outFile);
			experiment = expInput.getText();
			ArrayList<String> queryList = new ArrayList<String>(Arrays.asList(experiment.split(" ")));
			for (String query : queryList){
				System.out.println(query);
				new SparqlQuery (query,file);
			}
			//E-GEOD-18842 E-GEOD-6731 E-GEOD-8977 E-GEOD-6088
			//new SparqlQuery ("E-GEOD-18842",file);
			//new SparqlQuery ("E-GEOD-6731",file);
			//new SparqlQuery (experiment,file);
			
			//E-GEOD-10821

			try {
				importInformation.setTxtFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			importInformation.setFirstDataRow(1);
			importInformation.setFirstHeaderRow(0);
			importInformation.guessSettings();
			importInformation.setDelimiter("\t");
		}

		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();

			if(ACTION_GDB.equals(action)) {
				standaloneEngine.selectGdb("Gene");
				txtGdb.setText(
						PreferenceManager.getCurrent()
						.get(GlobalPreference.DB_CONNECTSTRING_GDB)
						);
			}
			else if(ACTION_OUTPUT.equals(action)) {
				try {
					DBConnector dbConn = standaloneEngine.getGexManager().getDBConnector();
					String output = ((DBConnectorSwing)dbConn).openNewDbDialog(
							getPanelComponent(), importInformation.getGexName()
							);
					if(output != null) {
						String outFile = FileUtils.removeExtension(output) + ".pgex";
						txtOutput.setText(outFile);
						updateTxt();
						//getWizard().setNextFinishButtonEnabled(true);
					}
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(
							getPanelComponent(), "The database connector is not supported"
							);
					Logger.log.error("No gex database connector", ex);
				}
			}
		}
	}
	
	private class ColumnPage extends WizardPanelDescriptor
	{
	    public static final String IDENTIFIER = "COLUMN_PAGE";

	    private ColumnTableModel ctm;
		private JTable tblColumn;

	    private JComboBox cbColId;
	    //private JComboBox cbColSyscode;
	    //private JRadioButton rbFixedNo;
	    private JRadioButton rbFixedYes;
	    private JComboBox cbDataSource;
	    private DataSourceModel mDataSource;

	    public ColumnPage()
	    {
	        super(IDENTIFIER);
	    }

	    public Object getNextPanelDescriptor()
	    {
	        return ImportPage.IDENTIFIER;
	    }

	    public Object getBackPanelDescriptor()
	    {
	        return FilePage.IDENTIFIER;
	    }

	    @Override
		protected JPanel createContents()
		{	    	
		    FormLayout layout = new FormLayout (
		    		"pref, 7dlu, pref:grow",
		    		"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, fill:[100dlu,min]:grow");

		    PanelBuilder builder = new PanelBuilder(layout);
		    builder.setDefaultDialogBorder();

		    CellConstraints cc = new CellConstraints();

			//rbFixedNo = new JRadioButton("Select a column to specify system code");
			rbFixedYes = new JRadioButton("Use the same system code for all rows");
			//ButtonGroup bgSyscodeCol = new ButtonGroup ();
			//bgSyscodeCol.add (rbFixedNo);
			//bgSyscodeCol.add (rbFixedYes);

			cbColId = new JComboBox();
			//cbColSyscode = new JComboBox();

			mDataSource = new DataSourceModel();
			String[] types = {"metabolite","protein","gene","interaction"};
			mDataSource.setTypeFilter(types);
			cbDataSource = new PermissiveComboBox(mDataSource);

			ctm = new ColumnTableModel(importInformation);
			tblColumn = new JTable(ctm);
			tblColumn.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tblColumn.setDefaultRenderer(Object.class, ctm.getTableCellRenderer());
			tblColumn.setCellSelectionEnabled(false);

			tblColumn.getTableHeader().addMouseListener(new ColumnPopupListener());
			JTable rowHeader = new RowNumberHeader(tblColumn);
			rowHeader.addMouseListener(new RowPopupListener());
			JScrollPane scrTable = new JScrollPane(tblColumn);

			JViewport jv = new JViewport();
		    jv.setView(rowHeader);
		    jv.setPreferredSize(rowHeader.getPreferredSize());
		    scrTable.setRowHeader(jv);
//		    scrTable.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, rowHeader
//		            .getTableHeader());

			builder.addLabel ("Select primary identifier column:", cc.xy(1,1));
			builder.add (cbColId, cc.xy(3,1));

			//builder.add (rbFixedNo, cc.xyw(1,3,3));
			//builder.add (cbColSyscode, cc.xy(3,5));
			builder.add (rbFixedYes, cc.xyw (1,7,3));
			builder.add (cbDataSource, cc.xy (3,9));

			builder.add (scrTable, cc.xyw(1,11,3));

			ActionListener rbAction = new ActionListener() {
				public void actionPerformed (ActionEvent ae)
				{
					boolean result = (ae.getSource() == rbFixedYes);
					importInformation.setSyscodeFixed(result);
			    	columnPageRefresh();
				}
			};
			rbFixedYes.addActionListener(rbAction);
			//rbFixedNo.addActionListener(rbAction);

			mDataSource.addListDataListener(new ListDataListener()
			{
				public void contentsChanged(ListDataEvent arg0)
				{
					importInformation.setDataSource(mDataSource.getSelectedDataSource());
				}

				public void intervalAdded(ListDataEvent arg0) {}

				public void intervalRemoved(ListDataEvent arg0) {}
			});
/*
			cbColSyscode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					importInformation.setSysodeColumn(cbColSyscode.getSelectedIndex());
					columnPageRefresh();
				}
			});*/
			cbColId.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					importInformation.setIdColumn(cbColId.getSelectedIndex());
			    	columnPageRefresh();
				}
			});
			return builder.getPanel();
		}

	    private class ColumnPopupListener extends MouseAdapter
	    {
	    	@Override public void mousePressed (MouseEvent e)
			{
				showPopup(e);
			}

			@Override public void mouseReleased (MouseEvent e)
			{
				showPopup(e);
			}

			int clickedCol;

			private void showPopup(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					JPopupMenu popup;
					popup = new JPopupMenu();
					clickedCol = tblColumn.columnAtPoint(e.getPoint());
					if (clickedCol != importInformation.getSyscodeColumn())
						popup.add(new SyscodeColAction());
					if (clickedCol != importInformation.getIdColumn())
						popup.add(new IdColAction());
					popup.show(e.getComponent(),
							e.getX(), e.getY());
				}
			}

			private class SyscodeColAction extends AbstractAction
			{
				public SyscodeColAction()
				{
					putValue(Action.NAME, "SystemCode column");
				}

				public void actionPerformed(ActionEvent arg0)
				{
					// if id and code column are about to be the same, swap them
					if (clickedCol == importInformation.getIdColumn())
						importInformation.setIdColumn(importInformation.getSyscodeColumn());
					importInformation.setSysodeColumn(clickedCol);
					columnPageRefresh();
				}
			}

			private class IdColAction extends AbstractAction
			{
				public IdColAction()
				{
					putValue(Action.NAME, "Identifier column");
				}

				public void actionPerformed(ActionEvent arg0)
				{
					// if id and code column are about to be the same, swap them
					if (clickedCol == importInformation.getSyscodeColumn())
						importInformation.setSysodeColumn(importInformation.getIdColumn());
					importInformation.setIdColumn(clickedCol);
					columnPageRefresh();
				}
			}
	    }

	    private class RowPopupListener extends MouseAdapter
	    {
	    	@Override public void mousePressed (MouseEvent e)
			{
				showPopup(e);
			}

			@Override public void mouseReleased (MouseEvent e)
			{
				showPopup(e);
			}

			int clickedRow;

			private void showPopup(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					JPopupMenu popup;
					popup = new JPopupMenu();
					clickedRow = tblColumn.rowAtPoint(e.getPoint());
					popup.add(new DataStartAction());
					popup.add(new HeaderStartAction());
					popup.show(e.getComponent(),
							e.getX(), e.getY());
				}
			}

			private class DataStartAction extends AbstractAction
			{
				public DataStartAction()
				{
					putValue(Action.NAME, "First data row");
				}

				public void actionPerformed(ActionEvent arg0)
				{
					importInformation.setFirstDataRow(clickedRow);
					columnPageRefresh();
				}
			}

			private class HeaderStartAction extends AbstractAction
			{
				public HeaderStartAction()
				{
					putValue(Action.NAME, "First header row");
				}

				public void actionPerformed(ActionEvent arg0)
				{
					importInformation.setFirstHeaderRow(clickedRow);
					columnPageRefresh();
				}
			}

	    }

	    private void columnPageRefresh()
	    {
	    	String error = null;
			if (importInformation.isSyscodeFixed())
			{
				rbFixedYes.setSelected (true);
				//cbColSyscode.setEnabled (false);
				cbDataSource.setEnabled (true);
			}
			else
			{
				//rbFixedNo.setSelected (true);
				//cbColSyscode.setEnabled (true);
				cbDataSource.setEnabled (false);

				if (importInformation.getIdColumn() == importInformation.getSyscodeColumn())
	    		{
	    			error = "System code column and Id column can't be the same";
	    		}
			}
		    getWizard().setNextFinishButtonEnabled(error == null);
		    getWizard().setErrorMessage(error == null ? "" : error);
			getWizard().setPageTitle ("Choose column types");

	    	ctm.refresh();
	    }

	    private void refreshComboBoxes()
	    {
	    	mDataSource.setSelectedItem(importInformation.getDataSource());
			cbColId.setSelectedIndex(importInformation.getIdColumn());
			//cbColSyscode.setSelectedIndex(importInformation.getSyscodeColumn());
	    }

	    /**
	     * A simple cell Renderer for combo boxes that use the
	     * column index integer as value,
	     * but will display the column name String
	     */
	    private class ColumnNameRenderer extends JLabel implements ListCellRenderer
	    {
			public ColumnNameRenderer()
			{
				setOpaque(true);
				setHorizontalAlignment(CENTER);
				setVerticalAlignment(CENTER);
			}

			/*
			* This method finds the image and text corresponding
			* to the selected value and returns the label, set up
			* to display the text and image.
			*/
			public Component getListCellRendererComponent(
			                        JList list,
			                        Object value,
			                        int index,
			                        boolean isSelected,
			                        boolean cellHasFocus)
			{
				//Get the selected index. (The index param isn't
				//always valid, so just use the value.)
				int selectedIndex = ((Integer)value).intValue();

				if (isSelected)
				{
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}

				String[] cn = importInformation.getColNames();
				String column = cn[selectedIndex];
				setText(column);
				setFont(list.getFont());

				return this;
			}
		}

	    public void aboutToDisplayPanel()
	    {
	    	// create an array of size getSampleMaxNumCols()
	    	Integer[] cn;
	    	int max = importInformation.getSampleMaxNumCols();
    		cn = new Integer[max];
    		for (int i = 0; i < max; ++i) cn[i] = i;

	    	cbColId.setRenderer(new ColumnNameRenderer());
	    	//cbColSyscode.setRenderer(new ColumnNameRenderer());
	    	cbColId.setModel(new DefaultComboBoxModel(cn));
	    	//cbColSyscode.setModel(new DefaultComboBoxModel(cn));

			columnPageRefresh();
			refreshComboBoxes();

	    	ctm.refresh();
	    }

	    @Override
	    public void aboutToHidePanel()
	    {
	    	importInformation.setSyscodeFixed(rbFixedYes.isSelected());
	    	if (rbFixedYes.isSelected())
	    	{
		    	importInformation.setDataSource(mDataSource.getSelectedDataSource());
	    	}
	    }
	    
	}
	
	
	private class ImportPage extends WizardPanelDescriptor implements ProgressListener
	{
		public static final String IDENTIFIER = "IMPORT_PAGE";

		public ImportPage()
		{
			super(IDENTIFIER);
		}

		public Object getNextPanelDescriptor()
		{
			return FINISH;
		}

		public Object getBackPanelDescriptor()
		{
			return ColumnPage.IDENTIFIER;
		}

		private JProgressBar progressSent;
		private JTextArea progressText;
		private ProgressKeeper pk;
		private JLabel lblTask;

		@Override
		public void aboutToCancel()
		{
			// let the progress keeper know that the user pressed cancel.
			pk.cancel();
		}

		protected JPanel createContents()
		{
			FormLayout layout = new FormLayout(
					"fill:[100dlu,min]:grow",
					"pref, pref, fill:pref:grow"
					);

			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
			builder.setDefaultDialogBorder();

			pk = new ProgressKeeper((int)1E6);
			pk.addListener(this);
			progressSent = new JProgressBar(0, pk.getTotalWork());
			builder.append(progressSent);
			builder.nextLine();
			lblTask = new JLabel();
			builder.append(lblTask);

			progressText = new JTextArea();

			builder.append(new JScrollPane(progressText));
			return builder.getPanel();
		}

		public void setProgressValue(int i)
		{
			progressSent.setValue(i);
		}

		public void setProgressText(String msg)
		{
			progressText.setText(msg);
		}

		public void aboutToDisplayPanel()
		{
			getWizard().setPageTitle ("Perform import");
			setProgressValue(0);
			setProgressText("");

			getWizard().setNextFinishButtonEnabled(false);
			getWizard().setBackButtonEnabled(false);
		}

		public void displayingPanel()
		{			
			SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
				@Override protected Void doInBackground() {
					pk.setTaskName("Importing pathway");
					try
					{
						GexTxtImporter.importFromTxt(
								importInformation,
								pk,
								standaloneEngine.getSwingEngine().getGdbManager().getCurrentGdb(),
								standaloneEngine.getGexManager()
								);
						if (standaloneEngine.getVisualizationManager().getActiveVisualization() == null)
							createDefaultVisualization(importInformation);
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
						Logger.log.error ("During import", e);
						setProgressValue(0);
						setProgressText("An Error Has Occurred: " + e.getMessage() + "\nSee the log for details");

						getWizard().setBackButtonEnabled(true);
					} finally {
						pk.finished();
					}
					return null;
				}

				@Override public void done()
				{
					getWizard().setNextFinishButtonEnabled(true);
					getWizard().setBackButtonEnabled(true);
				}
			};
			sw.execute();
		}

		public void progressEvent(ProgressEvent e)
		{
			switch(e.getType())
			{
			case ProgressEvent.FINISHED:
				progressSent.setValue(pk.getTotalWork());
			case ProgressEvent.TASK_NAME_CHANGED:
				lblTask.setText(pk.getTaskName());
				break;
			case ProgressEvent.REPORT:
				progressText.append(e.getProgressKeeper().getReport() + "\n");
				break;
			case ProgressEvent.PROGRESS_CHANGED:
				progressSent.setValue(pk.getProgress());
				break;
			}
		}
	}
	static double makeRoundNumber(double input)
	{
		double order = Math.pow(10, Math.round(Math.log10(input))) / 10;
		return Math.round (input / order) * order;
	}

	private void createDefaultVisualization(ImportInformation info) throws IDMapperException, DataException
	{
		VisualizationManager visMgr = standaloneEngine.getVisualizationManager(); 
		ColorSetManager csmgr = visMgr.getColorSetManager();
		ColorSet cs = new ColorSet(csmgr);
		csmgr.addColorSet(cs);

		ColorGradient gradient = new ColorGradient();
		cs.setGradient(gradient);

		double lowerbound = makeRoundNumber (info.getMinimum() - info.getMinimum() / 10); 
		double upperbound = makeRoundNumber (info.getMaximum() + info.getMaximum() / 10);
		gradient.addColorValuePair(new ColorValuePair(Color.YELLOW, lowerbound));
		gradient.addColorValuePair(new ColorValuePair(Color.BLUE, upperbound));

		Visualization v = new Visualization("auto-generated");

		ColorByExpression cby = new ColorByExpression(standaloneEngine.getGexManager(), 
				standaloneEngine.getVisualizationManager().getColorSetManager());
		DataInterface gex = standaloneEngine.getGexManager().getCurrentGex();

		int count = Math.min (5, gex.getSamples().keySet().size());
		for (int i = 0; i < count; ++i)
		{			
			cby.addUseSample(gex.getSample(i));
		}
		cby.setSingleColorSet(cs);
		v.addMethod(cby);

		DataNodeLabel dnl = new DataNodeLabel();
		v.addMethod(dnl);

		visMgr.addVisualization(v);
		visMgr.setActiveVisualization(v);
	}	
}
