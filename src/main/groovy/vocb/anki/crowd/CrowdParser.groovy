package vocb.anki.crowd;

import groovy.json.JsonBuilder
import vocb.Helper

public class CrowdParser {

	Object jsonRoot



	public void parse(String json) {
		jsonRoot = Helper.parseJson(json)
	}

	public String getCrowdankiUuuid() {
		jsonRoot.crowdanki_uuid
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


	public JsonBuilder buildNote(NoteFields flds, String modeluuid) {
		JsonBuilder b = new groovy.json.JsonBuilder()
		b {
			__type__  "Note"
			fields(flds.toFields())
			guid flds.guid
			note_model_uuid modeluuid
			tags(['ankiVocb'])
		}
	}


}
