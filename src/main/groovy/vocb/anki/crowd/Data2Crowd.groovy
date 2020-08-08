package vocb.anki.crowd

import static vocb.Ansi.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

import groovy.transform.CompileStatic
import vocb.ConfHelper
import vocb.Helper
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.data.Term
import vocb.pck.PackInfo
import vocb.template.Render

//@CompileStatic
public class Data2Crowd {

	ConfHelper cfgHelper = ConfHelper.instance
	@Lazy ConfigObject cfg = cfgHelper.cfg

	Path rootPath= Paths.get("/data/src/AnkiVocb")
	Path dataPath= rootPath.resolve("db")
	Path templatePath = ["src", "main", "resources", "template"].inject(rootPath) { Path p, String ch-> p.resolve(ch)}
	PackInfo info
	
	


	List<CharSequence> staticMedia = ["_lightBulb.png" as CharSequence]


	
	
	@Lazy Render render = {		  
		 new Render(cfgHelper:cfgHelper).tap {
			 assert info?.backgroundName
			 extraVars.putAll( [
				  info: info, 
				  backgroundImg: addExtensionToMediaLink(info.backgroundName)])
		 }
	}()



	@Lazy Manager dbMan =  {
		assert dataPath
		assert info
		new Manager(storagePath: dataPath).tap {
			load()
		}
	}()

	@Lazy VocbModel vocbModel = {
		assert info?.destPath
		new VocbModel(destCrowdRootFolder: info.destRootFolder).tap {
			resolveMediaLink = thisObject.&resolveMediaLink
		}
	}()

	Path resolveMediaLink(String mediaLink) {
		String fn = new File(mediaLink).name
		Path pkgPath = rootPath.resolve("pkg").resolve(info.name)
		List<Path> lookupPaths = [pkgPath, dataPath, templatePath]

		List<Path> resolved = Helper.matchingFiles(lookupPaths, fn) //Exact match first
		if (!resolved) {
			Pattern fnP = ~/${Pattern.quote(fn)}\.?(jpeg|jpg|png|mp3|gif)?/
			resolved = Helper.matchingFiles(lookupPaths, fnP) //Any extension
		}
		if (!resolved) { //Non-existing. Assume db/media
			return dataPath.resolve("media").resolve(mediaLink)
		}
		//assert resolved.size() == 1 : "The $mediaLink was found on multiple locations. $resolved"
		if (resolved.size() > 1) {
			 println (color(mediaLink, BOLD) +  color(" was found on multiple locations: ",YELLOW) + color(resolved.join("|"), BLUE))			
		}
		return resolved[0]
	}
			
	String addExtensionToMediaLink(String mediaLink) {
		if (!mediaLink) return null
		Tuple2<String, String> fn = Helper.splitFileNameExt(mediaLink)
		if (!fn.v2) { //Media link has no extension. Have to take it from the actual resolved path
			Path sourcePath =resolveMediaLink(mediaLink)
			if (!Files.exists(sourcePath)) return null
			Tuple2<String, String> rFn = Helper.splitFileNameExt(sourcePath.fileName.toString())
			assert rFn.v2 : "Resolved media file has no extension. MediaLink: $mediaLink"
			mediaLink = "${mediaLink}.${rFn.v2}"
		}
		return mediaLink
	}
	
	


	void concept2CrowdNote(Concept c, Example e, Note n) {
		assert info
		assert c?.firstTerm
		assert n
		int stars = dbMan.numberOfStarsFreq(c?.freq)
		String star = cfg.starSymbol?:"🟊"
		Closure<String> link = vocbModel.&mediaLink2CrowdLink
		Closure<String> sndField = { String s->
			n.mediaLinks.add(s)
			Helper.sndField(link(s), cfg);
		}
		
		Closure<String> imgField = { String s->
			n.mediaLinks.add(s)
			Helper.imgField(link(s), cfg);
		}
		
		
		n.with {
			Term ent = c[0]
			Term cst1 = c[1]
			Term cst2 = c[2]
			Term enx = e[0]
			Term csx = e[1]

			img= imgField(c?.img)
			freq= stars
			background = addExtensionToMediaLink(thisObject.info.backgroundName)
			foreign= ent.term
			foreignTTS= sndField(ent?.tts)
			foreignExample= enx?.term
			foreignExampleTTS= sndField(enx?.tts)
			n.'native' = cst1?.term
			nativeTTS= sndField(cst1?.tts)
			nativeAlt= cst2?.term
			nativeAltTTS= sndField(cst2?.tts)
			nativeExample= csx?.term
			nativeExampleTTS = sndField(csx?.tts)

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
		//Ensure target list has at least same number of elements as the source
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
	
	void renderDeckDescriptionTemplate( ConfigObject deckDescriptionPreview=cfg.render.deckDescriptionRender) {
		assert deckDescriptionPreview
		vocbModel.parser.deckDesc = render.render(deckDescriptionPreview)
	}
	
	void exportExamplesToCrowd(Collection<Example> toExport, Set<Concept> ignore = []) {
		WordNormalizer wn =dbMan.wn
		vocbModel.parser.deckName= info.displayName
		vocbModel.parser.deckCrowdUuid = info.uuid
		renderDeckDescriptionTemplate()
		
		vocbModel.notes.clear()
		renderCardTemplate(cfg.renderCardTemplate)
		staticMedia.add(addExtensionToMediaLink(info.backgroundName))
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
