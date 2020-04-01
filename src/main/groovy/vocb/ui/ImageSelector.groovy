package vocb.ui

import java.awt.Color
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.UIManager
import javax.swing.border.BevelBorder
import javax.swing.border.TitledBorder

import vocb.azure.BingWebSearch

public class ImageSelector   {

	Tuple2<Integer,Integer> gridSize=[4, 4]
	JFrame frame
	List<JLabel> labels =[]
	List<Tuple2<BufferedImage, URL>> imgs= []
	Tuple2<BufferedImage, URL> selected
	


	public DisplayImage() throws IOException {
		BufferedImage img=ImageIO.read(new File("f://images.jpg"))
		ImageIcon icon=new ImageIcon(img)
		JFrame frame=new JFrame()
		frame.setLayout(new FlowLayout())
		frame.setSize(200,300)
		JLabel lbl=new JLabel()
		lbl.setIcon(icon)
		frame.add(lbl)
		frame.setVisible(true)
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
	}

	JFrame createFrame() {
		frame=new JFrame()
		frame.with {
			title = "Pick the image"
			layout = new GridLayout(gridSize.v1, gridSize.v2, 10, 10)
			extendedState = JFrame.MAXIMIZED_BOTH
			//undecorated = true

			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		}
	}

	void buildGrid() {
		int len = gridSize.v1*gridSize.v2
		labels.clear()
		MouseAdapter ad = new MouseAdapter() {

					public void mouseEntered(MouseEvent e) {
						JLabel l= e.source
						l.setBackground(Color.GREEN)
					}

					public void mouseExited(MouseEvent e) {
						JLabel l= e.source
						l.setBackground(UIManager.getColor("Label.background"))
					}
					
					public void mouseClicked(MouseEvent e) {
						JLabel l= e.source
						selected = imgs[l.name as Integer] 
						println selected
						frame.dispose()
					}
					
					
				}

		for (int i=0;i<len;i++) {
			JLabel l = new JLabel()
			String name = i.toString()
			l.name = name
			
			l.border = new TitledBorder(new BevelBorder(BevelBorder.RAISED), name, TitledBorder.CENTER, TitledBorder.ABOVE_TOP)
			frame.contentPane.add(l)
			labels.add(l)
			l.addMouseListener(ad)
		}
	}

	public void open() {
		createFrame()
		buildGrid()
		frame.visible = true
	}
	
	public loadImage(BufferedInputStream bis, URL url) {
		assert labels : "call open() first"
		BufferedImage img = ImageIO.read(bis)
		
		imgs.add(  new Tuple2<BufferedImage, URL>(img, url))
		JLabel l = labels[imgs.size()-1]
	
		ImageIcon icon = new ImageIcon(img)
		l.icon = icon
		
	}

	public static void main(String[] args) {
		ImageSelector s = new ImageSelector()
		s.open()
		BingWebSearch bs = new BingWebSearch()
		bs.withEachThumbnailStream("test", 5) {BufferedInputStream bis, URL url ->
			s.loadImage(bis, url)
			
		}
		
		println "Done. Selected: ${s.selected?.v2}"
	}
}
