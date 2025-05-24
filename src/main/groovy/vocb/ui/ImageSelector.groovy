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
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFormattedTextField
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
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

	SearchData searchData = new SearchData()
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
			searchData.selected = selected
			if (SwingUtilities.isRightMouseButton(e)) {
				searchData.runEditor = true
			}
			closeFrame()
		}
	}

	KeyAdapter imageKeyAdapter = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			println e
		}
	}

	void closeFrame() {
		frame.visible = false
		synchronized (lock) {
			lock.notify()
		}
	}

	void setTitle(String title) {
		frame?.title=title
	}

	JFrame createFrame() {
		frame=new JFrame()
		frame.with {
			title = "Pick the image"
			iconImage = Toolkit.defaultToolkit.getImage(DisplayImage.class.getResource("/com/sun/java/swing/plaf/motif/icons/image-delayed.png"))
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
			/*columnWidths = [100, 30, 30, 30]
			 rowHeights = [30, 20]
			 columnWeights = [
			 0.0,
			 0.0,
			 0.0,
			 0.0,
			 ]*/
		}
		topPanel.setLayout(topPanelLayout)

		//topPanel.setLayout(new GridLayout(0, 1, 0, 0))


		searchTextField = new JTextField()
		GridBagConstraints gbc_searchTextField = new GridBagConstraints()
		gbc_searchTextField.with {
			insets = new Insets(0, 0, 5, 5)
			weightx = 0.8
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
							searchData.q = searchTextField.text
							runSearch(searchData)
						}
					})
		}

		JButton searchMoreButton = new JButton("Search More")
		searchMoreButton.with {
			addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							searchData.count=128
							searchData.q = searchTextField.text
							runSearch(searchData)
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
			weightx = 0.1
		}
		topPanel.add(searchMoreButton, gbc_searchMoreButton)

		chckbxClipart = new JCheckBox("ClipArt")
		chckbxClipart.with {
			addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							searchData.q = searchTextField.text
							searchData.imageType = chckbxClipart.selected ? "Clipart" : null
							runSearch(searchData)

						}
					})
			toolTipText = "Search with ClipArt filter enabled"
		}
		GridBagConstraints gbc_chckbxClipart = new GridBagConstraints()
		gbc_chckbxClipart.with {
			insets = new Insets(0, 0, 5, 5)
			gridx = 2
			gridy = 0
			weightx = 0.1
		}
		topPanel.add(chckbxClipart, gbc_chckbxClipart)

		JButton asBlankButton = new JButton("Ignore this one")
		asBlankButton.with {
			addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							searchData.useBlank = true
							closeFrame()
						}
					})
			toolTipText = "Don't use image for this term"
			icon = UIManager.getIcon("Tree.leafIcon")
		}
		GridBagConstraints gbc_asBlankButton = new GridBagConstraints()
		gbc_asBlankButton.with {
			insets = new Insets(0, 0, 5, 5)
			weightx = 0.5
			gridx = 3
			gridy = 0
			weightx = 0.1
		}
		topPanel.add(asBlankButton, gbc_asBlankButton)

		JFormattedTextField helpText = new JFormattedTextField()
		helpText.with {
			text = "Press left mouse to select as is. Press right button to edit afterwards with an external editor"
			editable = false
		}
		GridBagConstraints gbc_helpText = new GridBagConstraints()
		gbc_helpText.with {
			gridwidth = 4
			insets = new Insets(0, 0, 0, 5)
			fill = GridBagConstraints.HORIZONTAL
			gridx = 0
			gridy = 1
		}
		topPanel.add(helpText, gbc_helpText)






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
		final int myCounter = searchCounter.incrementAndGet()
		//assert s
		searchData = s
		println "Loading ${s.results.size()} thumbnails. For '$s.q' search"
		searchTextField.text = s.q
		chckbxClipart.selected = searchData.imageType == 'Clipart'

		Thread.start {
			synchronized (searchLock) {
				//final int  myCounter = searchCounter.get()
				print "me"
				println myCounter
				if (myCounter != searchCounter.get()) {
					println "Search cancelled"
					return
				}
				gridPanel.removeAll()
				precreateImageLabels(s.results.size())
				println "Found: ${s.results.size()}"

				Component[] components = gridPanel.components
				s.results.eachWithIndex { URL u, int i->
					if (myCounter != searchCounter.get()) {
						println "Search cancelled"
						return

					}
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


		HttpHelper hh = new HttpHelper()
		BingWebSearch bs = new BingWebSearch(httpHelper: hh)
		s.runSearch = { SearchData sd->
			s.loadSearchResult(bs.thumbnailSearch(sd), hh)
		}
	
		s.runSearch(new SearchData(q:"test"))
		s.runAsModal()

		println "Done. Selected: $s.searchData.selected"
	}
}
