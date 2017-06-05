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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * @author Koen
 *
 */
public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JFrame Jframe;
	private JLabel headerLabel;
	private JComboBox<String> combobox;
	private JPanel controlPanel;
	private static JTextArea text;
	private List<String>chosenCategories = new ArrayList<String>();
	private JTextField maxOutputField;
	private JPanel categoriesPanel;

	public GUI(List<String> chosenCategories) {
		this.chosenCategories = chosenCategories;
		Jframe = new JFrame("Car Reviewer");
		Jframe.setMinimumSize(new Dimension(800, 600));
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

		categoriesPanel = new JPanel();
		categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
		JLabel categoriesLabel = new JLabel("Select categories interested in:\n");
		categoriesPanel.add(categoriesLabel);
		c.fill = GridBagConstraints.CENTER;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 10;
		c.ipadx = 0;
		c.weightx = 0.5;
		c.gridwidth = 0;
		c.gridx = 0;
		c.gridy = 2;
		Jframe.add(categoriesPanel, c);


		JPanel outputPanel = new JPanel();
		panel.setLayout(new FlowLayout());
		JLabel maxOutputLabel = new JLabel("Maximum cars to show: ", SwingConstants.LEFT);
		maxOutputField = new JTextField("5", SwingConstants.RIGHT);
		outputPanel.add(maxOutputLabel);
		outputPanel.add(maxOutputField);
		c.fill = GridBagConstraints.CENTER;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 10;
		c.ipadx = 30;
		c.weightx = 0.0;
		c.gridwidth = 0;
		c.gridx = 0;
		c.gridy = 3;
		Jframe.add(outputPanel, c);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 10;
		c.ipadx = 0;
		c.weightx = 0.5;
		c.gridwidth = 0;
		c.gridx = 0;
		c.gridy = 4;
		Jframe.add(controlPanel, c);

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
		run();
	}

	private void run() {
		headerLabel.setText("Select a car to start...");

		JButton runButton = new JButton("Run");

		List<String> keys = new ArrayList<String>();
		keys = (List<String>) SimpleRunner.getKeys();
		keys = SimpleRunner.trimKeys(keys);
		java.util.Collections.sort(keys);
		for (String key : keys) {
			combobox.addItem(key);
		}

		ArrayList<List<String>> categories = Categories.makeCategories();
		for(List<String> category : categories) {
			/*for(String subcategory : category) { // For all categories
				JCheckBox checkbox = new JCheckBox(subcategory);
				checkbox.setSelected(true);
				categoriesPanel.add(checkbox);
			}*/
			JCheckBox checkbox = new JCheckBox(category.get(0));
			checkbox.setSelected(true);
			categoriesPanel.add(checkbox);
		}

		runButton.setActionCommand("Run");

		runButton.addActionListener(new ButtonClickListener());

		controlPanel.add(runButton);

		Jframe.pack();	
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Jframe.setLocation(dim.width/2-Jframe.getSize().width/2, dim.height/2-Jframe.getSize().height/2);
		Jframe.setVisible(true);
	}

	private class ButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("Run")) {
				//ArrayList<Pattern> patterns = new ArrayList<Pattern>();
				text.removeAll();
				text.setText("Category		Sentiment \n");
				List<String> cars = new ArrayList<String>();
				chosenCategories.clear();
				for (int i=1; i<categoriesPanel.getComponentCount(); i++) {
					JCheckBox checkbox = (JCheckBox)categoriesPanel.getComponent(i);
					if(checkbox.isSelected())
						chosenCategories.add(checkbox.getText());
				}
				if (combobox.getSelectedItem().equals("All")){
					new API(chosenCategories, cars);
					/*for (int i=1; i<combobox.getItemCount(); i++) {
						patterns = (ArrayList<Pattern>) runner
								.run(SimpleRunner.unTrimKey((String) combobox.getItemAt(i)));

						if (patterns.size() != 0) {
							text.setText(text.getText() + (String)combobox.getItemAt(i) + "\n");
							runText(patterns, maxOutputField.getText().toString());
						}
					}*/
				}
				else {
					/*patterns = (ArrayList<Pattern>) runner
						.run(SimpleRunner.unTrimKey((String) combobox.getSelectedItem()));
					runText(patterns, maxOutputField.getText().toString());*/
					cars.add((String) combobox.getSelectedItem());
					new API(chosenCategories, cars);
				}

				text.revalidate();
				text.repaint();
			}
		}
	}

	public static void APIReturn(String str) throws NullPointerException {
		try {
			text.setText(text.getText() + str + "\n");
			text.revalidate();
			text.repaint();
		}
		catch (Exception e){}
	}

	/*private void runText(List<Pattern> patterns, String maxOutput) {
		new Categories(patterns, chosenCategories);
		ArrayList<List<String>> arrayCategories = Categories.groupCategories(patterns);

		for (List<String> category : arrayCategories) {
			double categorySentiment = 0;
			for(int i=1;i<category.size();i++) {
				categorySentiment += (Double.parseDouble(category.get(i)));
			}
			text.setText(text.getText() + category.get(0) + "		" + categorySentiment/(category.size()-1) + "\n");
		}
		double overallSentiment = SimpleRunner.calculateOverallSentiment(patterns);
		text.setText(text.getText() + "Overall" + "		" + overallSentiment + "\n\n");
		text.revalidate();
		text.repaint();
	}*/
}
