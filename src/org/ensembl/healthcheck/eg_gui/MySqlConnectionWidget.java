package org.ensembl.healthcheck.eg_gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ensembl.healthcheck.configuration.ConfigureHost;
import org.ensembl.healthcheck.util.ActionAppendable;
import org.ensembl.healthcheck.util.ProcessExec;

public class MySqlConnectionWidget extends JPanel implements ActionListener {

	protected JButton OpenInMySqlCli;
	protected JTextField MysqlConnectionCmd;
	protected List<ConfigureHost> dbDetails; 
	protected JComboBox dbServerSelector;
	protected DatabaseTabbedPane databaseTabbedPane;
	
	protected String konsoleCmd = "konsole";
	
	protected boolean checkCanExecute(String programName) {
		
		String fullPath = findInSystemPath(programName);		
		return new File(fullPath).canExecute();
	}
	
	protected String findInSystemPath(String programName) {
		
		List<String> param = new LinkedList<String>(); 
		
		param.add("which");			
		param.add(programName);		
		
		String[] cmdLineItems = param.toArray(new String[] { "" });
		Map<String,String> environmentVars = new HashMap<String,String>(System.getenv());
		
		final StringBuffer locationOfProgram = new StringBuffer();
		
		try {
			int exit = ProcessExec.exec(
				cmdLineItems, 
				new ActionAppendable() {
					@Override public void process(String message) {
						locationOfProgram.append(message);
					}
				}, 
				new ActionAppendable() {
					@Override public void process(String message) {
						System.err.println(message);
					}
				}, 
				false, 
				environmentVars
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Trim is important, because which returns a carriage return at the end.
		return locationOfProgram.toString().trim();
	}
	
	protected JButton createRunMysqlInConsoleButton() {
		
		JButton OpenInMySqlCli = new JButton("Open in MySql CLI");

	    OpenInMySqlCli.setActionCommand(Constants.OPEN_MYSQL_CLI);
	    OpenInMySqlCli.addActionListener(this);
	    OpenInMySqlCli.setMnemonic(KeyEvent.VK_M);
	    OpenInMySqlCli.setSize(
	    		Constants.DEFAULT_BUTTON_WIDTH, 
	    		Constants.DEFAULT_BUTTON_HEIGHT
	    );
		return OpenInMySqlCli;
	}
	
	protected JTextField createMysqlCmdTextField() {
		
		JTextField MysqlCmdTextField = new JTextField(); 
		new CopyAndPastePopupBuilder().addPopupMenu(MysqlCmdTextField);
		return MysqlCmdTextField;
	}
	
	public MySqlConnectionWidget(
		List<ConfigureHost> dbDetails, 
		JComboBox dbServerSelector, 
		DatabaseTabbedPane databaseTabbedPane
	) {
		
		this.dbDetails = dbDetails;
		this.dbServerSelector = dbServerSelector;
		this.databaseTabbedPane = databaseTabbedPane;
	    
		MysqlConnectionCmd = createMysqlCmdTextField();
		
		setBorder(
			BorderFactory.createTitledBorder(
				GuiTestRunnerFrameComponentBuilder.defaultEmptyBorder, 
				"Connect to the selected database"
			)
		);
		setLayout(new BorderLayout());
	    
		JLabel label = new JLabel("Or copy and paste this:"); 

		Box b = Box.createHorizontalBox();

		if (checkCanExecute(konsoleCmd)) {
			
			OpenInMySqlCli = createRunMysqlInConsoleButton();
			add(OpenInMySqlCli, BorderLayout.WEST);
			label = new JLabel("or copy and paste this:");
			b.add(label);
			
		} else {

			label = new JLabel("Copy and paste:");
			b.add(label);
			
		}		

		b.add(MysqlConnectionCmd);
		
		add(b, BorderLayout.CENTER);
		
		updateDbCmdLine();
	}
	
	protected void updateDbCmdLine() {

		if (MysqlConnectionCmd ==null) { return; }
		MysqlConnectionCmd.setText(createDbCmdLine());
		MysqlConnectionCmd.selectAll();
	}
	
	protected String createDbCmdLine() {		
	
		// if nothing has been selected
		//
		int selectedIndex = dbServerSelector.getSelectedIndex();

		if (selectedIndex==-1) {
			return "No database has been selected.";
		}
		
		List<String> param = createMysqlExecParameters();
		
		StringBuffer dbCmdLine = new StringBuffer();
		
		for(String currentParam : param) {
			dbCmdLine.append(currentParam);
			dbCmdLine.append(" ");
		}
		return dbCmdLine.toString().trim();
	}

	protected List<String> createMysqlExecParameters() {
		
		int selectedIndex = dbServerSelector.getSelectedIndex();
		ConfigureHost selectedDbServerConf = dbDetails.get(selectedIndex);
		
		List<String> param = new LinkedList<String>();
		
		param.add("mysql");
		param.add("--host");
		param.add(selectedDbServerConf.getHost() );
		param.add("--port");
		param.add(selectedDbServerConf.getPort());
		param.add("--user");
		param.add(selectedDbServerConf.getUser());
		
		if (!selectedDbServerConf.getPassword().isEmpty()) {
			param.add("--password=" + selectedDbServerConf.getPassword());
		}
		
		String selectedDatabaseName = getSelectedDatabaseName(); 
		
		if (selectedDatabaseName!=null) {
			param.add(selectedDatabaseName);
		}

		return param;
	}

	protected String getSelectedDatabaseName() {
		
		DatabaseRadioButton selectedDbButton
			= databaseTabbedPane
				.getSelectedDbButton(); 
		
		if (selectedDbButton==null) {
			return null;
		}
		return databaseTabbedPane
			.getSelectedDbButton()
			.getText();
	}
	
	public void runMysqlConsole() {
		
		List<String> param = new LinkedList<String>(); 
		
		param.add("konsole");		
		param.add("-e");			
		param.addAll(
			createMysqlExecParameters()
		);
		
		String[] cmdLineItems = param.toArray(new String[] { "" });
		Map<String,String> environmentVars = new HashMap<String,String>(System.getenv());
		
		try {
			int exit = ProcessExec.exec(
				cmdLineItems, 
				new ActionAppendable() {
					@Override public void process(String message) {
						System.out.println(message);
					}
				}, 
				new ActionAppendable() {
					@Override public void process(String message) {
						System.err.println(message);
					}
				}, 
				false, 
				environmentVars
			);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void runThreadedMysqlConsole() {
		Thread t = new Thread() {
		    public void run() {
		    	runMysqlConsole();
		    }
	    };
		t.start();	
	}
	
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		
		String cmd = actionEvent.getActionCommand();
		
		if (cmd.equals(Constants.selectedDatabaseChanged)) {
			updateDbCmdLine();
		}
		if (cmd.equals(Constants.DB_SERVER_CHANGED)) {
			updateDbCmdLine();
		}
		if (cmd.equals(Constants.OPEN_MYSQL_CLI)) {
			runThreadedMysqlConsole();
		}
	}
}
