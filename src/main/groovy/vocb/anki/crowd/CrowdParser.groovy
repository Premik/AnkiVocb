package vocb.anki.crowd

import vocb.Helper

public class CrowdParser {

	Map jsonRoot


	public void parse(String json) {
		jsonRoot = Helper.parseJson(json)
	}

	public String toJsonString() {
		Helper.jsonToString(jsonRoot)
	}

	public void saveTo(File deckJson) {
		deckJson.write( toJsonString())
	}


	public NoteModel[] getNoteModels() {
		assert jsonRoot : "Run parse() first"
		return jsonRoot.note_models.collect { Map mJson->
			new NoteModel(mJson)
		}
	}

	public NoteModel getAnkivocbModel() {
		noteModels.find { NoteModel m ->
			m.name.toLowerCase().startsWith("ankivocb")
		}
	}

	public void setNoteModels(List<NoteModel> cols) {
		assert jsonRoot : "Run parse() first"
		if (!cols) return
			jsonRoot.note_models = Helper.parseJson(Helper.jsonToString(cols))
	}

	public List<Note> getAllNotes() {
		NoteModel[] mods = noteModels
		def findModel = { String guid ->
			mods.find { NoteModel m ->
				m.crowdanki_uuid == guid				
			}
		}
 
		jsonRoot.notes.collect {Map jsonMap ->
			Note n = new Note(jsonMap)
			n.model = findModel(n.note_model_uuid)
			return n			
		} 
	}

	Map<String, Object> indexNotesByFirstField() {
		assert jsonRoot : "Run parse() first"
		Map<String, Object> ret = new HashMap<String, Object>()
		jsonRoot.notes.each { def n ->
			String key = n.fields[0]?.toString().toLowerCase()
			if (key) ret[key] = n
			//if (!n.tags) return
		}
		return ret
	}

	Set<String> notesAllFieldValues() {
		assert jsonRoot : "Run parse() first"
		Set<String> ret = new HashSet<String>()
		jsonRoot.notes.each { def n ->
			ret.addAll(n.fields)
		}
		return ret
	}


	public void appendMedia(String mediaKey) {
		assert jsonRoot : "Run parse() first"
		jsonRoot.media_files+=mediaKey
	}

	public boolean hasMedia(String mediaKey) {
		assert jsonRoot : "Run parse() first"
		if (jsonRoot.media_files == null) return false
		return (boolean)jsonRoot.media_files.find { it == mediaKey}
	}

	public void appendNote(String noteJsonString) {
		def noteJson = Helper.parseJson(noteJsonString)
		assert noteJson : "Failed to parse noteJsonString"
		jsonRoot.notes+= noteJson
	}
}
