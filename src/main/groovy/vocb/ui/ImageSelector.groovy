package vocb.ui

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.UIManager
import javax.swing.border.BevelBorder
import javax.swing.border.MatteBorder
import javax.swing.border.TitledBorder

import vocb.HttpHelper
import vocb.SearchData
import vocb.azure.BingWebSearch

public class ImageSelector   {

	private static Object lock = new Object()
	JFrame frame
	JTextField searchTextField
	JPanel gridPanel

	SearchData searchData
	Closure runSearch

	MouseAdapter imageMouseAdapter = new MouseAdapter() {

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
			int selected = l.name as Integer
			println selected
			searchData.selected = selected
			frame.visible = false
			synchronized (lock) {			
				lock.notify()
			}
			
			
		}
	}

	JFrame createFrame() {
		frame=new JFrame()
		frame.with {
			title = "Pick the image"
			//layout = new FlowLayout(FlowLayout.LEFT, 5, 5)
			layout = new BoxLayout(frame.contentPane, BoxLayout.Y_AXIS)
			extendedState = JFrame.MAXIMIZED_BOTH
			//undecorated = true
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE)
		}
	}


	JPanel buildSearchPanel(Component container = frame.contentPane) {
		JPanel topPanel = new JPanel()
		container.add(topPanel)
		topPanel.setLayout(new GridLayout(0, 1, 0, 0))


		searchTextField = new JTextField()
		topPanel.add(searchTextField)


		searchTextField.with {
			border = new MatteBorder(10, 3, 10, 3, (Color) UIManager.getColor("Button.background"))
			toolTipText = "Search string"
			preferredSize = new Dimension(20, 20)
			//text = "Enter search string"
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JTextField f= e.source
					runSearch(f.text)
				}
			})

		}

	}


	public void open() {
		createFrame()
		buildSearchPanel()
		def layout = new FlowLayout(FlowLayout.CENTER, 5, 5)
		gridPanel=new JPanel(layout)
		gridPanel.setPreferredSize(new Dimension(1024, 4000))

		JScrollPane scrollPane = new JScrollPane(gridPanel)
		scrollPane.verticalScrollBar.unitIncrement = 64 //More sensitive scroll

		frame.contentPane.add(scrollPane)
		frame.visible = true
	}

	public BufferedImage scale(BufferedImage before, double scale=0.4) {
		//https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage/4216635
		int w = before.width*scale
		int h = before.height*scale
		BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
		AffineTransform at = new AffineTransform()
		at.scale(scale, scale)
		AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC)
		after = scaleOp.filter(before, after)
		return after
	}

	public BufferedImage scaleToWidth(BufferedImage before, int preferedWidth) {
		assert preferedWidth
		int w = before.width
		if (w <= preferedWidth) return before
		return scale(before, 1.0d*w/preferedWidth)
	}

	public loadImage(BufferedInputStream bis, JLabel label, int preferedW=100) {
		BufferedImage img = ImageIO.read(bis)
		ImageIcon imageIcon = new ImageIcon(scale(img))
		label.icon = imageIcon
		label.text =""
		frame.invalidate()

	}

	public precreateImageLabels(int count, Component container = gridPanel) {
		for (int index=0;index<count;index++) {
			JLabel l = new JLabel()
			l.with {
				name = index.toString()
				text = "loading"
				//l.preferredSize = new Dimension(100, 100)
				border = new TitledBorder(new BevelBorder(BevelBorder.RAISED), name, TitledBorder.CENTER, TitledBorder.ABOVE_TOP)
				addMouseListener(imageMouseAdapter)
			}
			container.add(l)
		}
	}

	public loadSearchResult(SearchData s, HttpHelper hh) {
		assert s
		searchData = s
		println "Loading ${s.results.size()} thumbnails"
		searchTextField.text = s.q
		gridPanel.removeAll()
		precreateImageLabels(s.results.size())
		Thread.start {
			Component[] components = gridPanel.components
			s.results.eachWithIndex { URL u, int i->
				//Thread.sleep(10)
				EventQueue.invokeAndWait {
					hh.withDownloadResponse(u) {BufferedInputStream res->
						loadImage(res, components[i])

					}
				}

			}
		}
	}



	public void runAsModal() {
		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent arg0) {
				println "Closing"
				synchronized (lock) {
					frame.setVisible(false)
					lock.notify()
				}
			}
		})

		Thread t =Thread.start {
			synchronized(lock) {
				while (frame.isVisible())
				try {
					lock.wait()
				} catch (InterruptedException e) {
					e.printStackTrace()
				}
				println "frame not visible. Disposing"
				frame.dispose()
			}
		}
		t.join()


	}



	public static void main(String[] args) {


		ImageSelector s = new ImageSelector()
		s.open()
		s.runSearch = { String newQ->
			int searchResults=32
			HttpHelper hh = new HttpHelper()
			BingWebSearch bs = new BingWebSearch(httpHelper: hh)
			s.loadSearchResult(bs.thumbnailSearch(newQ, searchResults), hh)
		}
		s.runSearch("test")
		s.runAsModal()

		println "Done. Selected: $s.searchData.selected"
	}
}
