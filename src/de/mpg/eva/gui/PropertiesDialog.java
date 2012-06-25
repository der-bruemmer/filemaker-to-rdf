package de.mpg.eva.gui;

import de.mpg.eva.valency.Connector;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class PropertiesDialog extends JFrame {

	private static final int SCREEN_WIDTH = Toolkit.getDefaultToolkit()
			.getScreenSize().width;
	private static final int SCREEN_HEIGHT = Toolkit.getDefaultToolkit()
			.getScreenSize().height;

	private static final long serialVersionUID = 1L;
	private JTextField fmURL = null;
	private JTextField mappingFile = null;
	private JTextField fmUser = null;
	private JPasswordField fmPasswd = null;

	private JButton startButton = null;
	private JButton linkButton = null;

	public PropertiesDialog() {

		this.setLocation(SCREEN_WIDTH / 2 - 400 / 2,
				SCREEN_HEIGHT / 2 - 300 / 2);
		this.setPreferredSize(new Dimension(500, 200));
		this.setTitle("Filemaker to RDF Converter");

		GridBagConstraints grid;

		getContentPane().setLayout(new GridBagLayout());
		JLabel fmURLLabel = new JLabel("Filemaker URL:");
		JLabel mappingFileLabel = new JLabel("Mapping File:");
		JLabel fmUserLabel = new JLabel("Filemaker User:");
		JLabel fmPasswdLabel = new JLabel("Filemaker Password:");

		startButton = new JButton("Start");
		linkButton = new JButton("...");
		fmURL = new JTextField(20);
		mappingFile = new JTextField(20);
		fmUser = new JTextField(20);
		fmPasswd = new JPasswordField(20);

		grid = new GridBagConstraints();
		grid.gridx = 0;
		grid.gridy = 0;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(20, 20, 3, 20);
		add(fmURLLabel, grid);

		grid = new GridBagConstraints();
		grid.gridx = 1;
		grid.gridy = 0;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.gridwidth = 2;
		grid.insets = new Insets(20, 0, 3, 20);
		add(fmURL, grid);

		grid = new GridBagConstraints();
		grid.gridx = 0;
		grid.gridy = 2;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 20, 3, 20);
		add(fmUserLabel, grid);

		grid = new GridBagConstraints();
		grid.gridx = 1;
		grid.gridy = 2;
		grid.gridwidth = 2;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 0, 3, 20);
		add(fmUser, grid);

		grid = new GridBagConstraints();
		grid.gridx = 0;
		grid.gridy = 3;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 20, 3, 20);
		add(fmPasswdLabel, grid);

		grid = new GridBagConstraints();
		grid.gridx = 1;
		grid.gridy = 3;
		grid.gridwidth = 2;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 0, 3, 20);
		add(fmPasswd, grid);

		grid = new GridBagConstraints();
		grid.gridx = 0;
		grid.gridy = 4;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 20, 3, 20);
		add(mappingFileLabel, grid);

		grid = new GridBagConstraints();
		grid.gridx = 1;
		grid.gridy = 4;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 0, 3, 5);
		add(mappingFile, grid);

		grid = new GridBagConstraints();
		grid.gridx = 2;
		grid.gridy = 4;
		grid.fill = GridBagConstraints.HORIZONTAL;
		grid.insets = new Insets(0, 0, 3, 20);
		add(linkButton, grid);

		grid = new GridBagConstraints();
		grid.gridx = 1;
		grid.gridy = 5;
		add(startButton, grid);

		linkButton.addActionListener(new java.awt.event.ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser c = new JFileChooser();
				// Demonstrate "Open" dialog:
				int rVal = c.showOpenDialog(getComponent());
				if (rVal == JFileChooser.APPROVE_OPTION) {
					mappingFile.setText(c.getSelectedFile().getAbsolutePath()
							.toString());
				}
				if (rVal == JFileChooser.CANCEL_OPTION) {
					mappingFile.setText(mappingFile.getText());
				}

			}

		});

		startButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				startPressed();
			}

		});

	}

	private void startPressed() {
		
		Connector connector = new Connector(fmURL.getText(), fmUser.getText(),
				fmPasswd.getPassword().toString(), mappingFile.getText());

	}

	public static void main(String[] args) {

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}

		PropertiesDialog lp = new PropertiesDialog();
		lp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lp.pack();
		lp.setVisible(true);

	}

	public Component getComponent() {
		return this.getParent();
	}
}