package vocb.anki.crowd

import java.nio.file.Path
import java.nio.file.Paths

import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class Data2Crowd {

	Path dataPath= Paths.get("/data/src/AnkiVocb/db/")
	String destCrowdRootFolder = "/tmp/work/test"
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
			nativeTTS= cst2?.tts
			nativeExample= csx?.term
			nativeExampleTTS = csx?.tts
			(1..5).each {tags.remove('🟊'*it)}
			if (stars >0) {
				tags.add('🟊'*stars)
			}
		}
	}

	void mapConcept(Concept c) {
		assert c?.firstTerm
		Note n = vocbModel.updateNoteHaving(c.firstTerm)
		concept2CrowdNote(c, n)
	}


	public static void main(String[] args) {
		Data2Crowd a = new Data2Crowd()
		a.vocbModel.notes.clear()
		a.mapConcept(a.dbMan.db.concepts[0])
		a.vocbModel.save()
	}
}
