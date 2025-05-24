package vocb.anki.crowd;

import groovy.json.JsonBuilder
import vocb.Helper

public class CrowdParser {

	Object jsonRoot


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
		return jsonRoot.note_models.collect {m->
			new NoteModel([uuid:m.crowdanki_uuid, fieldsCount:m.flds.size() ])
		}
	}

	public Object getNotes() {
		assert jsonRoot : "Run parse() first"
		jsonRoot.notes
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

	public JsonBuilder buildNote(NoteFields flds) {
		flds.assertIsComplete()

		JsonBuilder b = new groovy.json.JsonBuilder()
		b {
			__type__  "Note"
			data flds.data
			fields(flds.toRichFields())
			flags flds.flags
			guid flds.guid
			note_model_uuid flds.model.uuid
			tags flds.tags
		}
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
