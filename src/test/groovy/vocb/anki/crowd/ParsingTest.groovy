package vocb.anki.crowd

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import vocb.Helper

class ParsingTest {

	String smallDeck = getClass().getResource('/vocb/crowd/deckSmall.json').text
	CrowdParser p = new CrowdParser(json:smallDeck)

	@Test
	void reparse() {
		//new File('/tmp/work/formatted.json') << Helper.jsonToString(Helper.parseJson(smallDeck))
		assert Helper.jsonToString(Helper.parseJson(smallDeck)) == smallDeck
	}
	
	
	def equiv(List a, List b) {
		a.toSet() == b.toSet()
	}

	def equiv(a, b) {
		a == b
	}

	void assertMapEq(Map map1, Map map2) {

		map1.each {
			k, v ->
			assert map2.containsKey(k)
			/*if ( v instanceof List ) {
			 if (v.toSet() != map2[k].toSet()) return false
			 }*/
			assert equiv(v, map2[k])
			//println "$k:$v"
		}
	}

	@Test
	void reparseModels() {
		NoteModel[] models = p.noteModels
		assert models*.assureIsComplete()
		p.noteModels = models
		//p.saveTo(new File("/tmp/work/m.json"))
		Map newJson = Helper.parseJson(p.toJsonString())
		Map oldJson = Helper.parseJson(smallDeck)
		//assertMapEq(newJson, oldJson)
		//assertMapEq(oldJson, newJson)
	}
	
	@Test
	void templEdit() {
		NoteModel m = p.ankivocbModel
		m.css = "edited"
		//p.saveTo(new File("/tmp/work/m.json"))
		String json = p.toJsonString()
		CrowdParser p2 = new CrowdParser(json:json)
		NoteModel m2 = p.ankivocbModel
		assert m.css == m2.css
		assert m2.css == "edited"
		
		//assertMapEq(newJson, oldJson)
		//assertMapEq(oldJson, newJson)
	}

	@Test
	void modelsParse() {
		NoteModel[] mods = p.noteModels
		assert mods
		assert mods.size() == 3
		assert p.ankivocbModel
		
	}

	@Test
	void fieldModel() {
		NoteModel m = p.noteModels[0]
		assert m.crowdanki_uuid == "0f93612e-70f8-11ea-a10e-d8cb8a536b75"
		assert m.flds
		m.assureIsComplete()
	}

	@Test
	void indexByFirstField() {
		Map<String, Object> idx = p.indexNotesByFirstField()
		assert idx
		assert idx.size() > 1
		println idx.keySet()

		assert idx["f"]
		assert idx["f"].fields[1] == "b"
	}

	@Test
	void allFields() {
		Set<String> s = p.notesAllFieldValues()
		assert s
		assert s.size() > 1
		assert s.contains('a12')
	}
}
