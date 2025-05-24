package vocb.appenders

import java.nio.file.Files
import java.nio.file.Path

import groovy.text.GStringTemplateEngine
import vocb.ConfHelper
import vocb.Helper
import vocb.HttpHelper
import vocb.ImgTrn
import vocb.SearchData
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
		imgSelector.with {
			open()
			runSearch = { SearchData sd->
				loadSearchResult(bingSearch.thumbnailSearch(sd), httpHelper)
			}
		}
	}

	Path pathForSelected(SearchData sd) {
		if (!sd || sd.selected<0) return null
		httpHelper.cache.subPathForKey(sd.results[sd.selected].toString())
	}

	void run() {
		dbMan.load()

		List<Concept> noImgs = dbMan.db.concepts.findAll {
			(!it.img) && it.terms && it.state!="ignore" && it.state!="ignoreImage"
		}
		if (noImgs.size() <1) {
			println "All concepts have an image"
			return
		}
		int i =0




		SearchData sd
		for ( Concept c in noImgs) {
			sd =new SearchData(count:16)
			i++
			init()
			String trm = c.firstTerm
			imgSelector.with {
				sd.q = trm
				runSearch(sd)
				title = "Pick the image ${c.terms.values().collect{it.term}} ($i/${noImgs.size()}) "
				runAsModal()
			}
			sd = imgSelector.searchData
			if (sd.useBlank) {
				c.state = "ignoreImage"
				dbMan.save()
				continue
			}

			if (sd.selected<=-1) {
				println "cancelled"
				break
			}


			Path selectedImg = pathForSelected(sd)
			assert Files.exists(selectedImg)
			Path resizedP = ImgTrn.resizeImage(selectedImg, 320, 200)
			
			
			c.img = dbMan.resolveMedia(trm, "jpeg", "img") { Path dbPath->
				Files.move(resizedP, dbPath)
				println "Stored $dbPath"
				
			}
			dbMan.save()
			if (sd.runEditor) {
				String editorCmd = ConfHelper.cfg?.ui?.editor
				assert editorCmd : "No editor configured in the ankivocb.conf/ui/editor path"
				
				Helper.runCommand(editorCmd, [path:dbMan.mediaLinkPath(c.img, "img"), searchData:sd], 1)
			}
			
			
		}
	}


	public static void main(String[] args) {
		new groovy.text.GStringTemplateEngine()
		BingImageAppender a = new BingImageAppender()
		a.run()
		println "Done"
	}
}
