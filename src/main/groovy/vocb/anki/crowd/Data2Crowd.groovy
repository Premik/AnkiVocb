package vocb.anki.crowd

import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

import vocb.ConfHelper
import vocb.Helper
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.data.Term
import vocb.template.Render

//@CompileStatic
public class Data2Crowd {

	ConfHelper cfgHelper = ConfHelper.instance
	@Lazy ConfigObject cfg = cfgHelper.cfg

	Path rootPath= Paths.get("/data/src/AnkiVocb")
	Path dataPath= rootPath.resolve("db")
	Path templatePath = ["src", "main", "resources", "template"].inject(rootPath) { Path p, String ch-> p.resolve(ch)}
	String pkgName
	@Lazy Path pkgPath = rootPath.resolve("pkg").resolve(pkgName)
	@Lazy String backgroundName = "_${pkgName}Background"
	
	List<CharSequence> staticMedia = ["_lightBulb.png" as CharSequence]
	

	CharSequence destCrowdRootFolder = "/tmp/work/test"
	@Lazy Render render = new Render(cfgHelper:cfgHelper)



	@Lazy Manager dbMan =  {
		assert dataPath
		new Manager(storagePath: dataPath).tap {
			load()
		}
	}()

	@Lazy VocbModel vocbModel = {
		assert destCrowdRootFolder
		new VocbModel(
				destCrowdRootFolder: Paths.get(destCrowdRootFolder),
				resolveMediaLink: {String mediaLink ->
					String fn = new File(mediaLink).name
					List<String> lookupPaths = [pkgPath, dataPath, templatePath]
					
					List<Path> resolved = Helper.matchingFiles(lookupPaths, fn) //Exact match first
					if (!resolved) {
						Pattern fnP = ~/${Pattern.quote(fn)}\.?(jpeg|jpg|png|mp3|gif)?/
						resolved = Helper.matchingFiles(lookupPaths, fnP) //Any extension						
					}
					if (!resolved) { //Non-existing. Assume db/media
						return dataPath.resolve("media").resolve(mediaLink)
					}
					assert resolved.size() == 1 : "The $mediaLink was found on multiple locations. $resolved"
					return resolved[0]
					/*
			 Path lnk = dataPath.resolve("media").resolve(mediaLink)
			 if (!Files.exists(lnk)) {
			 Path tmlpLnk = templatePath.resolve(mediaLink)
			 if (Files.exists(tmlpLnk)) {
			 return tmlpLnk
			 }
			 }
			 return lnk*/
				})
	}()



	void concept2CrowdNote(Concept c, Example e, Note n) {
		assert c?.firstTerm
		assert n
		int stars = dbMan.numberOfStarsFreq(c?.freq)
		String star = cfg.starSymbol?:"🟊"
		def link = vocbModel.&mediaLink2CrowdLink
		n.with {
			Term ent = c[0]
			Term cst1 = c[1]
			Term cst2 = c[2]
			Term enx = e[0]
			Term csx = e[1]

			img= link(c?.img)
			freq= stars
			background = thisObject.backgroundName
			foreign= ent.term
			foreignTTS= link(ent?.tts)
			foreignExample= enx?.term
			foreignExampleTTS= link(enx?.tts)
			n.'native' = cst1?.term
			nativeTTS= link(cst1?.tts)
			nativeAlt= cst2?.term
			nativeAltTTS= link(cst2?.tts)
			nativeExample= csx?.term
			nativeExampleTTS = link(csx?.tts)

			(1..5).each {tags.remove(star*it)}
			if (stars >0) {
				tags.add(star*stars)
			}
		}
	}


	void mapConcept(Concept c, Example e) {
		if (c.state == "ignore") return
			assert c?.firstTerm
		println c
		Note n = vocbModel.updateNoteHaving(c.firstTerm)
		concept2CrowdNote(c, e, n)
	}

	NoteModel renderCardTemplate( ConfigObject renderCardTemplate, NoteModel targetM=vocbModel.noteModel) {
		targetM.css = render.render(renderCardTemplate.css)

		List cards = renderCardTemplate.cards
		assert cards
		//Ensure target list as at least same number of elements as the source
		List<TemplateModel> padded = targetM.tmpls.withEagerDefault { new TemplateModel() }
		padded[cards.size()-1] //Pad with new template models if needed
		padded.take(cards.size())
		//assert cards.size() == targetM.tmpls.size()

		[cards, targetM.tmpls].transpose().each {Map card, TemplateModel m->
			println "$card.name -> $m"
			m.name = card.name
			m.qfmt = render.render( card.qfmt)
			m.afmt = render.render( card.afmt)
			m.bqfmt = card.bqfmt
			m.bafmt = card.bafmt

		}
		targetM.tmpls = padded
		return  targetM

	}

	@Deprecated
	void exportToCrowd(Collection<Concept> toExport) {
		assert false : "Depricated"
		vocbModel.notes.clear()
		renderCardTemplate(cfg.renderCardTemplate)
		vocbModel.copyMediaLinks(staticMedia)
		toExport.each { mapConcept(it) }
		vocbModel.save()
	}

	void exportExamplesToCrowd(Collection<Example> toExport, Set<Concept> ignore = []) {
		WordNormalizer wn =dbMan.wn
		vocbModel.notes.clear()
		renderCardTemplate(cfg.renderCardTemplate)
		vocbModel.copyMediaLinks(staticMedia)
		toExport.each { Example e->
			dbMan.conceptsFromWordsInExample(e)
					.findAll {!ignore.contains(it) }
					.each { Concept c->
						mapConcept(c, e)
					}
		}
		vocbModel.save()

	}




	public static void main(String[] args) {

		new Data2Crowd().with {
			exportExamplesToCrowd(dbMan.db.examples.take(1) )
		}
		//println a.dbMan.db.concepts.take(40).collect {it.firstTerm}
		//println a.optimizeOrder().take(40).collect {it.firstTerm}

		//println a.similarConcepts(a.dbMan.db.concepts[0], a.dbMan.db.concepts[1])



	}
}
