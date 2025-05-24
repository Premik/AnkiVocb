package vocb

import org.junit.jupiter.api.Test

import vocb.Helper
import vocb.anki.crowd.CrowdParser
import vocb.anki.crowd.NoteFields

class SimpleParsing {

	String smallDeck = getClass().getResource('/anki/crowd/deckSmall.json').text
	CrowdParser p = new CrowdParser()

	@Test
	void reparse() {
		//new File('/tmp/work/formatted.json') << Helper.jsonToString(Helper.parseJson(smallDeck))
		assert Helper.jsonToString(Helper.parseJson(smallDeck)) == smallDeck
	}

	@Test
	void reparseFields() {
		p.parse(smallDeck)
		assert p.crowdankiUuuid == "0f934964-70f8-11ea-a10e-d8cb8a536b75"
		assert p.notes
		assert p.notes[0].__type__ == 'Note'
	}

	@Test
	void indexByFirstField() {
		p.parse(smallDeck)
		Map<String, Object> idx = p.indexNotesByFirstField()
		assert idx
		assert idx.size() > 1
		println idx.keySet()

		assert idx["f"]
		assert idx["f"].fields[1] == "b"
	}

	@Test
	void bldNote() {
		NoteFields nf = new NoteFields([enWord:"enw", enSoundRef:"rf"] )
		String js= p.buildNote(nf,  "123")
		assert js == '''{"__type__":"Note","fields":["enw","rf",null],"guid":"vsocbenw","note_model_uuid":"123","tags":["ankiVocb"]}'''
	}
}
