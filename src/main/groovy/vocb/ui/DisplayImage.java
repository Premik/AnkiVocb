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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class DisplayImage {
	private JFrame frmPickImage;
	private JLabel lbl;

	public DisplayImage() throws IOException {

		BufferedImage img = ImageIO.read(new File("/tmp/ankivocb/1/httpstse1mmbingnetthidOIPYNwAR3DkuRkZfGGIeO_azQHaFLpidApi-e195c54f"));
		ImageIcon icon = new ImageIcon(img);

		JFrame frame = createFrame(icon);
		
		JPanel panel = new JPanel();
		panel.setName("1");
		panel.setBackground(UIManager.getColor("Label.background"));
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		frmPickImage.getContentPane().add(panel);
		
		JLabel lblSdfddf = new JLabel("sdfddf");
		panel.add(lblSdfddf);
		
		JLabel v1 = new JLabel("dfgdfgdfs");
		v1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		v1.setBackground(Color.GREEN);
		v1.setBorder( new TitledBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null), "123", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(51, 51, 51)));
		frmPickImage.getContentPane().add(v1);
		lbl = new JLabel();
		lbl.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
		});
		lbl.setIcon(icon);
		frame.getContentPane().add(lbl);
	}

	private JFrame createFrame(ImageIcon icon) {
		frmPickImage = new JFrame();
		frmPickImage.setTitle("Pick image");
		frmPickImage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
		});
		frmPickImage.getContentPane().setLayout(new GridLayout(0, 4, 10, 10));
		
		JLabel lblNewLabel = new JLabel("asdasds");
		frmPickImage.getContentPane().add(lblNewLabel);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("New check box");
		chckbxNewCheckBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			}
		});
		frmPickImage.getContentPane().add(chckbxNewCheckBox);
		frmPickImage.setSize(1019, 532);
		
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
}
