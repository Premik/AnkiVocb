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

	ImageSelector imgSelector

	int searchResults=64
	@Lazy HttpHelper httpHelper
	@Lazy BingWebSearch bingSearch = {
		new BingWebSearch(httpHelper: httpHelper)
	}()

	@Lazy Manager dbMan

	void init() {
		imgSelector = new ImageSelector()
		imgSelector.open()
		imgSelector.runSearch = { String newQ->
			imgSelector.loadSearchResult(bingSearch.thumbnailSearch(newQ, searchResults), httpHelper)
		}
	}

	void run() {
		dbMan.load()

		List<Concept> noImgs = dbMan.db.concepts.findAll {
			(!it.img) && it.terms && it.state!="ignore"
		}
		if (noImgs.size() <1) {
			println "All concepts have an image"
			return
		}
		int i =0



		for ( Concept c in noImgs) {
			i++
			init()
			String trm = c.terms[0].term
			imgSelector.runSearch(trm)
			imgSelector.title = "Pick the image. ($i/${noImgs.size()}) "
			imgSelector.runAsModal()
			int selIndx = imgSelector.searchData.selected
			if (selIndx<=-1) {
				println "cancelled"
				break
			}
			Path selectedImg = httpHelper.cache.subPathForKey(imgSelector.searchData.results[selIndx].toString())
			assert Files.exists(selectedImg)
			Path resizedP = ImgTrn.resizeImage(selectedImg, 320, 200)
			dbMan.resolveMedia(trm, "jpeg") { Path dbPath->
				Files.move(resizedP, dbPath)
				println "Stored $dbPath"
			}
			c.img =  dbMan.termd2MediaLink(trm, "jpeg")
			dbMan.save()
		}
	}


	public static void main(String[] args) {
		BingImageAppender a = new BingImageAppender()
		a.run()
		println "Done"
	}
}
