package vocb.appenders

import java.nio.file.Files
import java.nio.file.Path

import vocb.HttpHelper
import vocb.ImgTrn
import vocb.azure.BingWebSearch
import vocb.data.Concept
import vocb.data.Manager
import vocb.ui.ImageSelector

public class BingImageAppender {

	ImageSelector imgSelector = new ImageSelector()

	int searchResults=32
	HttpHelper httpHelper = new HttpHelper()
	BingWebSearch bingSearch = new BingWebSearch(httpHelper: httpHelper)

	Manager dbMan = new Manager()

	void init() {
		imgSelector.runSearch = { String newQ->
			imgSelector.loadSearchResult(bingSearch.thumbnailSearch(newQ, searchResults), httpHelper)
		}
	}

	void run() {
		dbMan.autoSave {
			List<Concept> noImgs = dbMan.db.concepts.findAll {(!it.img) && it.terms}
			if (noImgs.size() <1) {
				println "All concepts have an image"
				return
			}
			int i =0
			init()
			imgSelector.open()

			for ( Concept c in noImgs) {
				def trm = c.terms[0].term
				imgSelector.runSearch(trm)
				imgSelector.title = "Pick the image. ($i/${noImgs.size()}) "
				imgSelector.runAsModal()
				int selIndx = imgSelector.searchData.selected
				if (selIndx<=-1) break
					Path selectedImg = httpHelper.cache.subPathForKey(imgSelector.searchData.results[selIndx])
				assert Files.exists(selectedImg)
				Path resizedP = ImgTrn.resizeImage(selectedImg, 320, 200)
				dbMan.resolveMedia(trm, "jpeg") { Path dbPath->
					Files.move(resizedP, dbPath)
					println "Stored $dbPath"
				}
			}
		}
	}


	public static void main(String[] args) {
		BingImageAppender a = new BingImageAppender()
		a.run()
		println "Done"
	}
}
