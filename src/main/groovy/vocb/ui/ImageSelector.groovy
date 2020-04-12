package vocb.ui

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
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
	//private Semaphore searchSemaphore = new Semaphore(1, true)
	private static Object searchLock= new Object()
	private AtomicInteger searchCounter = new AtomicInteger(0)

	JFrame frame
	JTextField searchTextField
	JCheckBox chckbxClipart
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

	void setTitle(String title) {
		frame?.title=title
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
		GridBagLayout topPanelLayout = new GridBagLayout()
		topPanelLayout.with {
			columnWidths = [30, 30, 30, 30, 0, 0, 108]
			rowHeights = [30, 30]
			columnWeights = [
				1.0,
				0.0,
				0.0,
				0.0,
				0.0,
				0.0,
				0.0
			]
			rowWeights = [1.0, 0.0]
		}
		topPanel.setLayout(topPanelLayout)

		//topPanel.setLayout(new GridLayout(0, 1, 0, 0))


		searchTextField = new JTextField()
		GridBagConstraints gbc_searchTextField = new GridBagConstraints()
		gbc_searchTextField.with {
			insets = new Insets(0, 0, 5, 5)
			weightx = 0.6
			fill = GridBagConstraints.BOTH
			gridx = 0
			gridy = 0
		}
		topPanel.add(searchTextField, gbc_searchTextField)
		searchTextField.with {
			border = new MatteBorder(10, 10, 10, 10, (Color) UIManager.getColor("Button.background"))
			font = new Font("Monospaced", Font.BOLD, 20)
			//preferredSize = new Dimension(20, 20)
			toolTipText = "Search string"
			addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JTextField f= e.source
							runSearch(f.text)
						}
					})
		}

		JButton searchMoreButton = new JButton("Search More")
		searchMoreButton.with {
			addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							searchData.count=128
							runSearch(searchTextField.text)
						}
					})
			icon = UIManager.getIcon("FileChooser.listViewIcon")
			toolTipText = "Search again with more results enabled"
		}
		GridBagConstraints gbc_searchMoreButton = new GridBagConstraints()
		gbc_searchMoreButton.with {
			insets = new Insets(0, 0, 5, 5)
			gridx = 1
			gridy = 0
		}
		topPanel.add(searchMoreButton, gbc_searchMoreButton)

		chckbxClipart = new JCheckBox("ClipArt")
		chckbxClipart.with {
			addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							searchData.clipArt = chckbxClipart.selected
							runSearch(searchTextField.text)

						}
					})
			toolTipText = "Search with ClipArt filter enabled"
		}
		GridBagConstraints gbc_chckbxClipart = new GridBagConstraints()
		gbc_chckbxClipart.with {
			insets = new Insets(0, 0, 5, 5)
			gridx = 2
			gridy = 0
		}
		topPanel.add(chckbxClipart, gbc_chckbxClipart)






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
		println searchCounter.getAndIncrement()
		//assert s
		searchData = s
		println "Loading ${s.results.size()} thumbnails"
		searchTextField.text = s.q
		chckbxClipart.selected = searchData.clipArt

		Thread.start {
			synchronized (searchLock) {
				final int  myCounter = searchCounter.get()
				print "me"
				println myCounter 
				gridPanel.removeAll()
				precreateImageLabels(s.results.size())
				println "Found: ${s.results.size()}"

				Component[] components = gridPanel.components
				s.results.eachWithIndex { URL u, int i->
					if (myCounter != searchCounter.get()) return
					//Thread.sleep(1000)
					EventQueue.invokeAndWait {
						hh.withDownloadResponse(u) {BufferedInputStream res->
							loadImage(res, components[i])
						}
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

		int searchResults=4
		HttpHelper hh = new HttpHelper()
		BingWebSearch bs = new BingWebSearch(httpHelper: hh)
		s.runSearch = { String newQ->

			s.loadSearchResult(bs.thumbnailSearch(newQ, searchResults), hh)
		}
		s.runSearch("test")
		s.runAsModal()

		println "Done. Selected: $s.searchData.selected"
	}
}
