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
		return jsonRoot.note_models.collect {m->
			new NoteModel([uuid:m.crowdanki_uuid, fieldsCount:m.flds.size() ])
		}
	}

	public Object getNotes() {
		jsonRoot.notes
	}

	Map<String, Object> indexNotesByFirstField() {
		Map<String, Object> ret = new HashMap<String, Object>()
		jsonRoot.notes.each { def n ->
			String key = n.fields[0]?.toString().toLowerCase()
			if (key) ret[key] = n
			//if (!n.tags) return
		}
		return ret
	}

	public JsonBuilder buildNote(NoteFields flds, NoteModel model=null) {
		if (!model) model = noteModels[0]
		assert model?.uuid
		assert model?.fieldsCount
		// To avoid error on import if model expects more fields that we have
		List<String> fLst = flds.toFields().findAll()
		fLst.addAll([""]*model.fieldsCount)
		String[] fldsAr = fLst.take(model.fieldsCount)
		JsonBuilder b = new groovy.json.JsonBuilder()
		b {
			__type__  "Note"
			fields(fldsAr)
			guid flds.guid
			note_model_uuid model.uuid
			tags(['ankiVocb'])
		}
	}

	public void appendMedia(String mediaKey) {
		jsonRoot.media_files+=mediaKey
	}

	public boolean hasMedia(String mediaKey) {
		if (jsonRoot.media_files == null) return false
		return (boolean)jsonRoot.media_files.find { it == mediaKey}
	}

	public void appendNote(String noteJsonString) {
		def noteJson = Helper.parseJson(noteJsonString)
		assert noteJson
		jsonRoot.notes+= noteJson
	}


}
