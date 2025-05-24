package vocb.anki.crowd;

import vocb.Helper

class VocbModel {

	String version = "ankivocb 1"
	URL crowdJsonUrl = getClass().getResource('/template/deck.json')

	@Lazy CrowdParser parser = new CrowdParser(json:crowdJsonUrl.text)


	@Lazy NoteModel noteModel = {
		NoteModel n = parser.ankivocbModel
		assert n : "Failed to find the ankivobc model. The model name must start with 'ankivocb' "
		return n
	}()

	@Lazy List<Note> notes = {
		parser.allNotes.each {assureNote(it)}
		return parser.allNotes
	}()

	void assureNote(Note n) {
		assert n
		n.assertIsComplete()
		n.tags.remove("ankiVocb") //Legacy tag
		if (!n.hasTagWithPrefix("ankivocb")) {
			n.tags.add(version)
		}		
		n.guid = "avcb_${n.foreign?:n.hashCode() }"
	}

	void syncNoteFields() {
		//println Helper.objectToJson(notes)
		notes.each {assureNote(it)}
		parser.jsonRoot.notes = notes
	}
	
	void syncMedia() {
		
	}

	void saveTo(File f) {
		f.parentFile?.mkdirs()
		syncNoteFields()
		parser.saveTo(f)
	}

	Note updateNoteHaving(String foreignTerm) {
		assert foreignTerm
		Note n = notes.find {it.foreign == foreignTerm }
		if (!n) {
			n = new Note(model:noteModel)
			notes.add(n)
			syncNoteFields()
		}
		return n

	}

	static void main(String... args) {
		new VocbModel().tap {
			updateNoteHaving("newWord").tap {
				img = "newWordImg"
			}
			saveTo(new File("/tmp/work/test/test.json"))
			//syncNoteFields()
		}
		println "done"

	}

}
