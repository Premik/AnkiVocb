package vocb.anki.crowd;

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import vocb.Helper

public class NoteFields {

	String enWord
	String enSoundRef
	String czWord
	String czSoundRef
	String imageRef
	
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
		 List<String> fls = [enWord, sndField(enSoundRef) , czWord, sndField(czSoundRef), imgField(imageRef)].collect { it?:""}
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

	}
	
	public toJson() {
		JsonOutput.prettyPrint(JsonOutput.toJson(this))
	}

	@Override
	public String toString() {
		return "[enWord=" + enWord +  ", czWord=" + czWord + "]";
	}
}
