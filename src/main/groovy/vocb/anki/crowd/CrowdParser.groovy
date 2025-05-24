package vocb.anki.crowd

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import vocb.Helper

public class CrowdParser {

	String json

	JsonGenerator noteGenerator = new JsonGenerator.Options()
	.excludeFieldsByName("model", "fieldNames", "mediaLinks")
	.build()

	@Lazy Map jsonRoot = {
		Map j =Helper.parseJson(json)
		assert j : "Failed to parse the input json:\n '${json?.take(1000)}'"
		return j
	}()

	@Lazy NoteModel[] noteModels = {
		jsonRoot.note_models.collect { Map mJson->
			new NoteModel(mJson)
		}
	}()

	List<Note> getAllNotes()  {
		def findModel = { String guid ->
			noteModels.find { NoteModel m -> m.crowdanki_uuid == guid}
		}

		jsonRoot.notes.collect {Map jsonMap ->
			Note n = new Note(jsonMap)
			n.model = findModel(n.note_model_uuid)
			return n
		}
	}

	String getDeckName() {
		jsonRoot.name
	}

	void setDeckName(String name) {
		jsonRoot.name = name
	}
	
	String getDeckCrowdUuid() {
		jsonRoot.crowdanki_uuid
	}
	
	void setDeckCrowdUuid(String id) {
		jsonRoot.crowdanki_uuid = id
	}
	
	String getDeckDesc() {
		jsonRoot.desc
	}
	
	void setDeckDesc(String d) {
		jsonRoot.desc = d
	}
	
	

	Set<String> notesAllFieldValues() {
		assert jsonRoot : "Run parse() first"
		Set<String> ret = new HashSet<String>()
		jsonRoot.notes.each { def n ->
			ret.addAll(n.fields)
		}
		return ret
	}


	public NoteModel getAnkivocbModel() {
		noteModels.find { NoteModel m ->
			m.name.toLowerCase().startsWith("ankivocb")
		}
	}



	public String toJsonString() {
		JsonOutput.prettyPrint(noteGenerator.toJson(jsonRoot))
	}

	public void saveTo(File deckJson) {
		println ankivocbModel
		deckJson.write( toJsonString())
	}


	public void setNoteModels(List<NoteModel> cols) {
		if (!cols) {
			return
		}
		jsonRoot.note_models = Helper.parseJson(Helper.jsonToString(cols))
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
