import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * @author Koen
 *
 */
public class GUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame Jframe;
	private JLabel headerLabel;
	private JComboBox<String> combobox;
	private JPanel controlPanel;
	private static SimpleRunner runner;
	private JTextArea text;
	private static JProgressBar progress;

	public static void main(String[] args) {
		runner = new SimpleRunner();
		GUI gui = new GUI();
		gui.run();
	}

	private GUI() {
		Jframe = new JFrame("Car Reviewer");
		Jframe.setMinimumSize(new Dimension(700, 500));
		Jframe.setLayout(new GridBagLayout());
		Jframe.setResizable(true);
		Jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagConstraints c = new GridBagConstraints();

		headerLabel = new JLabel("", SwingConstants.CENTER);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 50;
		c.ipadx = 0;
		c.weightx = 0.0;
		c.gridwidth = 0;
		c.gridx = 0;
		c.gridy = 0;
		Jframe.add(headerLabel, c);

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		combobox = new JComboBox<String>();
		combobox.setSize(new Dimension(500, 200));
		combobox.addItem("All");
		panel.add(combobox);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 20;
		c.ipadx = 0;
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		Jframe.add(panel, c);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 20;
		c.ipadx = 0;
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 2;
		Jframe.add(controlPanel, c);
		
		progress = new JProgressBar(0, 100);
		progress.setVisible(false);
		progress.setStringPainted(true);
		c.fill = GridBagConstraints.CENTER;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 10;
		c.ipadx = 50;
		c.weightx = 0.0;
		c.weighty = 1.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 4;
		Jframe.add(progress, c);

		text = new JTextArea("Feature		Sentiment \n");
		text.setAutoscrolls(true);
		text.setMargin(new Insets(10, 10, 10, 10));
		text.setEditable(true);

		JScrollPane scrollpane = new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 150;
		c.ipadx = 400;
		c.weightx = 0;
		c.gridwidth = 0;
		c.gridx = 1;
		c.gridy = 5;
		Jframe.add(scrollpane, c);
	}

	private void run() {
		headerLabel.setText("Select a car to start...");

		JButton runButton = new JButton("Run");

		List<String> keys = new ArrayList<String>();
		keys = (List<String>) runner.getKeys();
		keys = trimKeys(keys);
		java.util.Collections.sort(keys);
		for (String key : keys) {
			combobox.addItem(key);
		}

		runButton.setActionCommand("Run");

		runButton.addActionListener(new ButtonClickListener());

		controlPanel.add(runButton);

		Jframe.pack();	
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Jframe.setLocation(dim.width/2-Jframe.getSize().width/2, dim.height/2-Jframe.getSize().height/2);
		Jframe.setVisible(true);
	}
	
	public static void setProgress(int progress, int total) {
			//System.out.println(progress + " " + total);
			//progress.setVisible(true);
			//progress.set;
			//progress.revalidate();
			//progress.repaint();
	}

	private class ButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("Run")) {
				ArrayList<Pattern> patterns = (ArrayList<Pattern>) runner
						.run(unTrimKey((String) combobox.getSelectedItem()));
				
				
				text.removeAll();
				text.setText("Feature		Sentiment \n");
				for (Pattern pattern : patterns) {
					if (pattern.toAspect().length() <= 14)
						text.setText(text.getText() + pattern.toAspect() + "		" + pattern.getSentiment() + "\n");
					else 
						text.setText(text.getText() + pattern.toAspect() + "	" + pattern.getSentiment() + "\n");
				}
				text.setText(text.getText() + "Overall		" + runner.overallSentiment);
				text.revalidate();
				text.repaint();
			}
		}
	}

	private List<String> trimKeys(List<String> keys) {
		List<String> newKeys = new ArrayList<String>();
		for (String key : keys) {
			key = key.replace("_", " ");
			key = key.substring(0, 1).toUpperCase() + key.substring(1);
			newKeys.add(key);
		}
		if (newKeys.size() == keys.size())
			return newKeys;
		else
			throw new Error();
	}

	private String unTrimKey(String key) {
		key = key.replace(" ", "_");
		key = key.substring(0, 1).toLowerCase() + key.substring(1);
		return key;
	}
}
