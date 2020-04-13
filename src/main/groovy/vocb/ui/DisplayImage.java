package vocb.ui;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.UIManager;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;
import java.awt.Font;
import javax.swing.JRadioButton;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class DisplayImage {
	private JFrame frmPickImage;
	private JLabel lbl;
	private JPanel panel_1;
	private JTextField searchTextField;

	public DisplayImage() throws IOException {

		BufferedImage img = ImageIO.read(new File("/tmp/ankivocb/1/httpstse1mmbingnetthidOIPYNwAR3DkuRkZfGGIeO_azQHaFLpidApi-e195c54f"));
		ImageIcon icon = new ImageIcon(img);

		JFrame frame = createFrame(icon);
		
		
		
		JLabel lblNewLabel_1 = new JLabel("New label");
		frmPickImage.getContentPane().setLayout(new BoxLayout(frmPickImage.getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel topPanel = new JPanel();
		frmPickImage.getContentPane().add(topPanel);
		GridBagLayout topPanelLayout = new GridBagLayout();
		topPanelLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0};
		topPanelLayout.rowWeights = new double[]{1.0, 0.0};
		topPanel.setLayout(topPanelLayout);
		
		searchTextField = new JTextField();
		GridBagConstraints gbc_searchTextField = new GridBagConstraints();
		gbc_searchTextField.insets = new Insets(0, 0, 5, 5);
		gbc_searchTextField.weightx = 1.0;
		gbc_searchTextField.fill = GridBagConstraints.BOTH;
		gbc_searchTextField.gridx = 0;
		gbc_searchTextField.gridy = 0;
		topPanel.add(searchTextField, gbc_searchTextField);
		searchTextField.setFont(new Font("Monospaced", Font.BOLD, 20));
		searchTextField.setBorder(new MatteBorder(10, 1, 10, 1, (Color) UIManager.getColor("Button.background")));
		searchTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		searchTextField.setToolTipText("Search string");
		searchTextField.setText("test");
		
		JButton searchMoreButton = new JButton("Search More");
		searchMoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		searchMoreButton.setIcon(UIManager.getIcon("FileChooser.listViewIcon"));
		searchMoreButton.setToolTipText("Search again with more results enabled");
		GridBagConstraints gbc_searchMoreButton = new GridBagConstraints();
		gbc_searchMoreButton.weightx = 0.1;
		gbc_searchMoreButton.gridx = 1;
		gbc_searchMoreButton.gridy = 0;
		topPanel.add(searchMoreButton, gbc_searchMoreButton);
		
		JCheckBox chckbxClipart = new JCheckBox("ClipArt");
		chckbxClipart.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
			}
		});
		chckbxClipart.setToolTipText("Search with ClipArt filter enabled");
		GridBagConstraints gbc_chckbxClipart = new GridBagConstraints();
		gbc_chckbxClipart.weightx = 0.1;
		gbc_chckbxClipart.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxClipart.gridx = 2;
		gbc_chckbxClipart.gridy = 0;
		topPanel.add(chckbxClipart, gbc_chckbxClipart);
		
		JButton asBlankButton = new JButton("Ignore this one");
		asBlankButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		asBlankButton.setToolTipText("Don't use image for this term");
		asBlankButton.setIcon(UIManager.getIcon("Tree.leafIcon"));
		GridBagConstraints gbc_asBlankButton = new GridBagConstraints();
		gbc_asBlankButton.insets = new Insets(0, 0, 5, 5);
		gbc_asBlankButton.weightx = 0.1;
		gbc_asBlankButton.gridx = 3;
		gbc_asBlankButton.gridy = 0;
		topPanel.add(asBlankButton, gbc_asBlankButton);
		
		JFormattedTextField helpText = new JFormattedTextField();
		helpText.setText("Press 'e' to edit with external editor");
		helpText.setEditable(false);
		GridBagConstraints gbc_helpText = new GridBagConstraints();
		gbc_helpText.gridwidth = 4;
		gbc_helpText.insets = new Insets(0, 0, 0, 5);
		gbc_helpText.fill = GridBagConstraints.HORIZONTAL;
		gbc_helpText.gridx = 0;
		gbc_helpText.gridy = 1;
		topPanel.add(helpText, gbc_helpText);
		
		JScrollPane scrollPane = new JScrollPane();
		frmPickImage.getContentPane().add(scrollPane);
		
		panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(1024, 4000));
		panel_1.setMaximumSize(new Dimension(4900, 32767));
		scrollPane.setViewportView(panel_1);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblNewLabel = new JLabel("asdasds");
		lblNewLabel.setForeground(Color.GREEN);
		lblNewLabel.setBackground(Color.RED);
		lblNewLabel.setPreferredSize(new Dimension(64, 64));
		panel_1.add(lblNewLabel);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("New check box");
		chckbxNewCheckBox.setBorder(new MatteBorder(5, 1, 5, 1, (Color) new Color(0, 0, 0)));
		panel_1.add(chckbxNewCheckBox);
		
		JLabel label = new JLabel("");
		panel_1.add(label);
		
		JPanel panel = new JPanel();
		panel_1.add(panel);
		panel.setName("1");
		panel.setBackground(UIManager.getColor("Label.background"));
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		
		JLabel lblSdfddf = new JLabel("sdfddf");
		panel_1.add(lblSdfddf);
		
		JLabel label_1 = new JLabel("");
		panel_1.add(label_1);
		
		JLabel v1 = new JLabel("dfgdfgdfs");
		panel_1.add(v1);
		v1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		v1.setBackground(Color.GREEN);
		v1.setBorder( new TitledBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), "123", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(51, 51, 51)));
		
		JLabel label_2 = new JLabel("");
		panel_1.add(label_2);
		lbl = new JLabel();
		lbl.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
			
			}
		});
		lbl.setSize(new Dimension(110, 110));
		panel_1.add(lbl);
		lbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
		});
		lbl.setIcon(icon);
		
		JLabel label_3 = new JLabel("");
		panel_1.add(label_3);
		
		JLabel label_4 = new JLabel("");
		panel_1.add(label_4);
		
		JLabel label_5 = new JLabel("");
		panel_1.add(label_5);
		chckbxNewCheckBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
	}

	private JFrame createFrame(ImageIcon icon) {
		frmPickImage = new JFrame();
		frmPickImage.setTitle("Pick image");
		frmPickImage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
		});
		frmPickImage.setSize(1783, 787);
		
		frmPickImage.setVisible(true);
		frmPickImage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frmPickImage;
	}

	public static void main(String avg[]) throws IOException {
		DisplayImage abc = new DisplayImage();
	}

	public JLabel getLbl() {
		return lbl;
	}
	public JPanel gridPanel() {
		return panel_1;
	}
}
