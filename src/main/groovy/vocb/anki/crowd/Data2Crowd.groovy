package vocb.anki.crowd

import java.nio.file.Path
import java.nio.file.Paths

import vocb.ConfHelper
import vocb.corp.Similarity
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term
import vocb.template.Render

public class Data2Crowd {
	
	ConfHelper cfgHelper = ConfHelper.instance
	@Lazy ConfigObject cfg = cfgHelper.cfg

	Path dataPath= Paths.get("/data/src/AnkiVocb/db/")
	String destCrowdRootFolder = "/tmp/work/test"	
	@Lazy Render render = new Render(cfgHelper:cfgHelper)

	BigDecimal[] freqRanges = [
		0,
		11000,
		151000,
		1511000,
		2121000,
		2811000,
		new BigDecimal("10e10")
	]

	Integer numberOfStarts(BigDecimal freq) {
		if (!freq) return null
		freqRanges.findIndexOf { freq < it} -1
	}


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
					Paths.get("/data/src/AnkiVocb/db/media").resolve(mediaLink)
				})
	}()

	void concept2CrowdNote(Concept c, Note n) {
		assert c?.firstTerm
		assert n
		int stars = numberOfStarts(c?.freq)
		String star = cfg.starSymbol?:"🟊"
		n.with {
			Term ent = c.terms.values()[0]
			Term cst1 = c.terms.values()[1]
			Term cst2 = c.terms.values()[2]
			Term enx = c.examples.values()[0]
			Term csx = c.examples.values()[1]

			img= c?.img
			freq= stars
			foreign= ent.term
			foreignTTS= ent.tts
			foreignExample= enx?.term
			foreignExampleTTS= enx?.tts
			n.'native' = cst1?.term
			nativeTTS= cst1?.tts
			nativeAlt= cst2?.term
			nativeAltTTS= cst2?.tts
			nativeExample= csx?.term
			nativeExampleTTS = csx?.tts
			
			(1..5).each {tags.remove(star*it)}
			if (stars >0) {
				tags.add(star*stars)
			}
		}
	}


	void mapConcept(Concept c) {
		if (c.state == "ignore") return
			assert c?.firstTerm
		println c
		Note n = vocbModel.updateNoteHaving(c.firstTerm)
		concept2CrowdNote(c, n)
	}
	
	NoteModel renderCardTemplate( ConfigObject renderCardTemplate, NoteModel targetM=vocbModel.noteModel) {		
		targetM.css = render.render(renderCardTemplate.css)
		List cards = renderCardTemplate.cards
		assert cards			
		//Ensure target list as at least same number of elements as the source
		List<TemplateModel> padded = targetM.tmpls.withEagerDefault { new TemplateModel() }
		padded[cards.size()-1] //Pad with new template models if needed
		//assert cards.size() == targetM.tmpls.size()
		 
		[cards, targetM.tmpls ].transpose().each {Map card, TemplateModel m-> 
			m.name = card.name
			m.afmt = render.render( card.afmt)
			m.qfmt = render.render( card.qfmt)
			m.bafmt = card.bafmt
			m.bqfmt = card.bqfmt			
		}
		return  targetM
		 
	}

	void exportToCrowd(int limit=10, int mult=1) {
		vocbModel.notes.clear()
		(0..limit).collect{it*mult} each {
			mapConcept(dbMan.db.concepts[it])
		}
		vocbModel.save()
	}


	public static void main(String[] args) {
		Data2Crowd a = new Data2Crowd()
		a.exportToCrowd(10, 1)
		//println a.dbMan.db.concepts.take(40).collect {it.firstTerm}
		//println a.optimizeOrder().take(40).collect {it.firstTerm}

		//println a.similarConcepts(a.dbMan.db.concepts[0], a.dbMan.db.concepts[1])



	}
}
