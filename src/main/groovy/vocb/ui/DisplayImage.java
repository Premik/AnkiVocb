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
		
		searchTextField = new JTextField();
		frmPickImage.getContentPane().add(searchTextField);
		searchTextField.setBorder(new MatteBorder(10, 1, 10, 1, (Color) UIManager.getColor("Button.background")));
		searchTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		searchTextField.setToolTipText("Search string");
		searchTextField.setText("test");
		
		JScrollPane scrollPane = new JScrollPane();
		frmPickImage.getContentPane().add(scrollPane);
		
		panel_1 = new JPanel();
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
		frmPickImage.setSize(1728, 891);
		
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
