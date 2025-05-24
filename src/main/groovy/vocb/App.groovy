package vocb;

import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

import groovy.json.JsonBuilder
import vocb.anki.ProfileSupport
import vocb.anki.crowd.CrowdParser
import vocb.anki.crowd.MediaMan
import vocb.anki.crowd.Note
import vocb.anki.crowd.NoteModel
import vocb.aws.AwsTranslate
import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.tts.AwsCliPollyTTS
import vocb.tts.LocalTTS

@Deprecated
public class App {

	File ankiCrowdExportPath
	File deckPath
	double commonWordThreashold = 0.95

	WordNormalizer normalizer = new WordNormalizer()
	AwsTranslate trn = new AwsTranslate()
	AwsCliPollyTTS enTts = new AwsCliPollyTTS()
	LocalTTS ttsCz = new LocalTTS()
	CrowdParser parser = new CrowdParser ()
	ProfileSupport profileSupport  = new ProfileSupport()

	@Lazy private Set<String> profiles = {
		profileSupport.listProfiles()
	}()

	String profile

	@Lazy MediaMan mm = {
		ensureCrowdExportExists()
		new MediaMan(ankiCrowdExportPath)
	}()

	
	@Lazy Set<String> knownWords = {
		ensureProfileExists()
		List<String> fields = profileSupport.listAllFields(profile)
		normalizer.tokens(fields.stream()).collect(Collectors.toSet())
	}()

	
	boolean isUnknownWord(String w) {
		return !knownWords.contains(w)
	}


	void parseDeck(File exportPath=null) {
		if (exportPath) {
			this.ankiCrowdExportPath =exportPath
		}
		assert ankiCrowdExportPath : "The ankiCrowd export path was not provided"
		assert ankiCrowdExportPath.exists()
		assert ankiCrowdExportPath.isDirectory()
		deckPath = new File(ankiCrowdExportPath, 'deck.json')
		parser.parse(deckPath.text)
	}

	void ensureCrowdExportExists() {
		if (!deckPath) {
			parseDeck()
		}
	}

	void ensureProfileExists() {
		assert profile in profiles : "The selected profile '$profile' not found. Profiles: $profiles "
	}


	Note makeNoteFields(String enWord) {
		ensureCrowdExportExists()
		String czW =trn.trn(enWord)
		File enSnd = mm.fileForWord(enWord)
		if (!enSnd.exists()) {
			//Process p=  tts.synth(enWord, "standard", "Emma", enSnd.canonicalPath)
			Process p=  enTts.synth(enWord, "neural", "Emma", enSnd.canonicalPath)
			Helper.printProcOut(p)
			p.waitFor(5, TimeUnit.SECONDS)
		}
		assert enSnd.exists()
		if (!parser.hasMedia(enSnd.name)) {
			parser.appendMedia(enSnd.name)
		}
		return new Note([enWord:enWord, enSoundRef:enSnd.name, czWord:czW])
	}

	List<String> findPopularUnknownWordsIn(String text) {
		assert text
		Set<String> words = normalizer.tokens(text)
				.filter(this.&isCommonWord) //Skip exotic words
				.filter(this.&isUnknownWord) //Skip words which has cards already (assume to be known)
				.collect(Collectors.toSet())

		return new ArrayList(words).sort()
	}


	void singleWords() {
		ensureCrowdExportExists()

		String supa = getClass().getResource('/Supaplex.txt').text
		List<String> words = new ArrayList(normalizer.uniqueueTokens(supa)).sort()
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
		App a = new App([profile:'Honzik'])
	
	}
}
