package vocb;

import java.util.concurrent.TimeUnit

import groovy.json.JsonBuilder
import vocb.anki.crowd.CrowdParser
import vocb.anki.crowd.MediaMan
import vocb.anki.crowd.Note
import vocb.anki.crowd.NoteModel
import vocb.aws.AwsTranslate
import vocb.corp.WordNormalizer
import vocb.tts.AwsCliPollyTTS

@Deprecated
public class Supa {

	File ankiCrowdExportPath = new File("/tmp/work/Supa1")
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

	Note makeNoteFields(String enWord) {
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
		return new Note([enWord:enWord, enSoundRef:enSnd.name, czWord:czW])
	}


	void singleWords() {
		parseDeck()

		String supa = getClass().getResource('/Supaplex.txt').text
		List<String> words = new ArrayList(n.uniqueueTokens(supa)).sort()
		Map<String, Object> indx = parser.indexNotesByFirstField()

		words = words.findAll { !indx.containsKey(it) }.take(100)
		NoteModel[] modls = parser.noteModels
		assert modls : "No note model found. Plase export at least one note"
		NoteModel mod =  parser.noteModels[0]
		println mod

		int i =0

		words.each {
			Note nf = makeNoteFields(it)
			JsonBuilder b = parser.buildNote(nf, mod)
			parser.appendNote(b.toPrettyString())
			i++
			if (i%10 == 0) {
				parser.saveTo(deckPath)
				println "$i(${words.size()})"
			}
		}
		parser.saveTo(deckPath)
		println "Done"

	}

	public static void main(String[] args) {
		new Supa().singleWords()
	}
}
