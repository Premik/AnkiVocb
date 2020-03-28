package vocb;

import vocb.anki.crowd.CrowdParser
import vocb.anki.crowd.NoteFields
import vocb.aws.AwsCliPollyTTS
import vocb.aws.AwsTranslate

public class Supa {

	File ankiCrowdExportPath = new File("/tmp/work/cust")

	WordNormalizer n = new WordNormalizer()
	AwsTranslate trn = new AwsTranslate()
	AwsCliPollyTTS tts = new AwsCliPollyTTS()
	CrowdParser parser = new CrowdParser ()

	void parseDeck(File path=ankiCrowdExportPath) {
		assert path.exists()
		assert path.isDirectory()
		parser.parse(new File(path, 'deck.json').text)
	}

	NoteFields makeNoteFields(String enWord) {
		String czW =trn.trn(enWord)
		return new NoteFields([enWord:enWord, czWord:czW])
	}


	void singleWords() {
		parseDeck()

		String supa = getClass().getResource('/Supaplex.txt').text
		List<String> words = new ArrayList(n.tokens(supa)).sort()
		Map<String, Object> indx = parser.indexNotesByFirstField()

		words = words.findAll { !indx.containsKey(it) }.take(1)
		println words.collect {makeNoteFields(it)}
	}

	public static void main(String[] args) {
		new Supa().singleWords()
	}
}
