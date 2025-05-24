package vocb;

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import vocb.anki.crowd.CrowdParser
import vocb.anki.crowd.Note
import vocb.conf.ConfHelper
import vocb.template.Render

@CompileStatic
public class Zbrojak {

	Path origDeckJsonPath = Paths.get( Zbrojak.getResource('/zbrojak/deck.json').toURI())
	Path origDesckRootPath = origDeckJsonPath.parent

	ConfHelper cfg = ConfHelper.instance.tap {
		extraLookupFolders.add(origDesckRootPath.toFile())
		
	}
	String backgroundName = '_ZbrojakBackground.jpeg'
	Path ankiCrowdExportPath = ConfHelper.resolveOutputPath("Zbrojak")
	Path deckPath = ankiCrowdExportPath.resolve('deck.json')
	Path deckMediaPath = deckPath.resolve("media")
	Path backgroundPath = deckPath.resolve(backgroundName)
	Path deckDescriptionPath = deckPath.resolve("readme.md")
	
	


	Path htmlRootPath = origDesckRootPath.resolve("htmlCache")

	@Lazy
	Render render = {
		new Render(cfgHelper: cfg).tap {
			extraVars.putAll([
				backgroundImg: backgroundName])
		}
	}()

	@Lazy CrowdParser parser = new CrowdParser (defaultModelNamePrefix:"zbrojak", json: origDeckJsonPath.text)


	@Lazy
	List<Note> notes = {
		parser.allNotes.each { Note n-> assureNote(n) }
		return parser.allNotes
	}()

	@CompileDynamic
	void assureNote(Note n) {
		assert n
		n.assertIsComplete()
		String q = Helper.word2Key(n['question']).replaceAll(" ", "")
		n.guid = "$n.questionNumber-${q.take(30)}"
		assert n.guid
		println n.guid
	}

	Note addNewNote() {
		Note n =new Note(model: parser.defaultModel)
		notes.add(n)
		return n
	}

	void syncMedia() {
		Path mp = ankiCrowdExportPath.resolve("media")
		/*if (!Files.exists(mp)) {
		 Files.createSymbolicLink(mp, htmlRootPath)
		 }*/
		mp.toFile().mkdirs()
		Closure sync = { Path p->
			Path trg = mp.resolve(p.getFileName())
			if (!Files.exists(trg)) {
				Files.createSymbolicLink(trg, p)
			}
		}
		sync(origDesckRootPath.resolve(backgroundName))
		htmlRootPath.eachFileMatch(~/.*\.(jpe?g)|(png)/, sync)
	}

	void save() {
		ankiCrowdExportPath.toFile().mkdirs()
		notes.each { Note n-> assureNote(n) }
		parser.jsonRoot.notes = notes
		parser.deckDesc = render.render([templates:[main: "deck-description"]])
		parser.saveTo(deckPath.toFile())
		println "Saved notes to $deckPath"
		syncMedia()
	}

	Path scrape(String locPath) {
		htmlRootPath.toFile().mkdirs()
		String name = locPath.split("/")[-1]
		assert name
		Path p = htmlRootPath.resolve(name)
		if (Files.exists(p)) {
			//println "Not scraping since $p exists "
			return p
		}
		p.withOutputStream { OutputStream out ->
			new URL("http://zbranekvalitne.cz$locPath").withInputStream { InputStream from ->
				out << from
			}
		}
		//println "Cached response to $p"
		return p
	}




	@CompileDynamic
	void parseWeb(Path htmlPath) {
		assert Files.exists(htmlPath)
		def p =new org.ccil.cowan.tagsoup.Parser()
		GPathResult page = new XmlSlurper(p).parse(htmlPath)
		GPathResult questions = page.body.'**'.find {it.@id == 'questions'}
		questions.div.eachWithIndex { GPathResult rootDiv, int qNo->
			Matcher m = rootDiv.div[0] =~/Otázka č. (\d+)/
			if (m.matches()) {
				assert Integer.parseInt(m.group(1)) == qNo+1
			}
			Iterable rows = rootDiv.div[1].div
			int offset = 0
			assert rows.size() == 4 || rows.size() == 5
			Path imgPath
			if (rows.size() == 5) {
				String imgLoc = rows[0].img.@src
				imgPath = scrape(imgLoc)
				rows = rows.drop(1)
			}
			int correctIndex = rows.findIndexOf { it.@class.text()?.contains("correct-answer") }-1
			String question = rows[0].p.b.text()
			def (String a, String b, String c) = rows.drop(1).collect {it.p[1].text()}
			Note n = addNewNote()
			n.question = question
			n.img = Helper.imgField(imgPath?.fileName?.toString(),  true)
			n.a=a
			n.b=b
			n.c=c
			n.aRes = correctIndex == 0 ? "correct" : "wrong"
			n.bRes = correctIndex == 1 ? "correct" : "wrong"
			n.cRes = correctIndex == 2 ? "correct" : "wrong"
			n.questionNumber = qNo+1
		}
	}

	public static void main(String[] args) {

		new Zbrojak().with {
			parseWeb(scrape("/zbrojni-prukaz/testove-otazky"))
			assert parser.defaultModel
			save()
		}
		println "Done"
	}
}
