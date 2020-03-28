package vocb;

import java.util.concurrent.TimeUnit

import groovy.json.JsonBuilder
import vocb.anki.crowd.CrowdParser
import vocb.anki.crowd.MediaMan
import vocb.anki.crowd.NoteFields
import vocb.anki.crowd.NoteModel
import vocb.aws.AwsCliPollyTTS
import vocb.aws.AwsTranslate

public class Supa {

	File ankiCrowdExportPath = new File("/tmp/work/cust")
	File deckPath = new File(ankiCrowdExportPath, 'deck.json')

	WordNormalizer n = new WordNormalizer()
	AwsTranslate trn = new AwsTranslate()
	AwsCliPollyTTS tts = new AwsCliPollyTTS()
	CrowdParser parser = new CrowdParser ()
	MediaMan mm = new MediaMan(ankiCrowdExportPath)

	void parseDeck() {
		assert ankiCrowdExportPath.exists()
		assert ankiCrowdExportPath.isDirectory()
		parser.parse(deckPath.text)
	}

	NoteFields makeNoteFields(String enWord) {
		String czW =trn.trn(enWord)
		File enSnd = mm.fileForWord(enWord)
		if (!enSnd.exists()) {
			//Process p=  tts.synth(enWord, "standard", "Emma", enSnd.canonicalPath)
			Process p=  tts.synth(enWord, "neural", "Emma", enSnd.canonicalPath)
			Helper.printProcOut(p)
			p.waitFor(5, TimeUnit.SECONDS)

		}
		assert enSnd.exists()
		if (!parser.hasMedia(enSnd.name)) {
			parser.appendMedia(enSnd.name)
		}
		return new NoteFields([enWord:enWord, enSoundRef:enSnd.name, czWord:czW])
	}


	void singleWords() {
		parseDeck()

		String supa = getClass().getResource('/Supaplex.txt').text
		List<String> words = new ArrayList(n.tokens(supa)).sort()
		Map<String, Object> indx = parser.indexNotesByFirstField()

		words = words.findAll { !indx.containsKey(it) }.take(1)
		NoteModel mod =  parser.noteModels[0]
		println mod

		int i =0
		words.each {
			NoteFields nf = makeNoteFields(it)
			JsonBuilder b = parser.buildNote(nf, mod)
			parser.appendNote(b.toPrettyString())
			i++
			if (i%10 == 0) {
				parser.saveTo(deckPath)
			}
		}
		parser.saveTo(deckPath)
		println "Done"

	}

	public static void main(String[] args) {
		new Supa().singleWords()
	}
}
