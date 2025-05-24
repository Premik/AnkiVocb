package vocb.anki.crowd

import groovy.json.JsonOutput
import vocb.Helper

public class Note {
	
	String __type__ = "Note"
	String[] fields = new String[5]
	 

	String data=""
	int flags = 0
	List<String> tags = ["ankiVocb"]

	NoteModel model

	public String sndField(String soundRef) {
		if (!soundRef) return ""
		return "[sound:$soundRef]"
	}

	public String imgField(String ref) {
		if (!ref) return ""
		return """<img src=":$ref">"""
	}

	public List<String> toRichFields(boolean fitToModel=true) {
		List<String> fls = [
			enWord,
			sndField(enSoundRef) ,
			czWord,
			sndField(czSoundRef),
			imgField(imageRef)
		].collect { it?:""}
		if (!fitToModel) return fls
		assert model : "Can't find to model since model is null"
		return Helper.padList(fls, "", model.fieldsCount, true)
	}

	public String getGuid() {
		return "vocb_$enWord"
	}

	public void assertIsComplete() {
		assert enWord
		assert czWord
		assert model : "Note has no model set"
		model.assertIsComplete()
		assert model.flds.size() == fields.length : "Model fields doesn't match the note"
	}
	
	String getEnWord() {
		fields[0]
	}
	String getenSoundRef() {
		fields[1]
	}
	String getczWord() {
		fields[2]
	}
	String getczSoundRef() {
		fields[3]
	}
	String getimageRef() {
		fields[4]
	}

	public toJson() {
		JsonOutput.prettyPrint(JsonOutput.toJson(this))
	}

	@Override
	public String toString() {
		return "[enWord=" + enWord +  ", czWord=" + czWord + "]"
	}
}
