package vocb.anki.crowd

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
	void simpleFields() {
		p.parse(smallDeck)
		assert p.noteModels
		assert p.noteModels[0].uuid == "0f93612e-70f8-11ea-a10e-d8cb8a536b75"
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
	void allFields() {
		p.parse(smallDeck)
		Set<String> s = p.notesAllFieldValues()
		assert s
		assert s.size() > 1
		assert s.contains('a12')
	}



	@Test
	void bldNote() {
		NoteModel m = new NoteModel([uuid:"123", fieldsCount:4 ])
		NoteFields nf = new NoteFields([enWord:"enw", czWord:"czw", enSoundRef:"rf", model:m] )
		nf.assertIsComplete()
		String js= p.buildNote(nf)
		//assert js == '''{"__type__":"Note","fields":["enw","[sound:rf]","czw",""],"guid":"vocb_enw","note_model_uuid":"123","tags":["ankiVocb"]}'''
		assert js.contains("123")
		assert js.contains("czw")
	}
}
