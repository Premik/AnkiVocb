package vocb.anki.crowd;

import vocb.Helper

public class NoteFields {

	String enWord
	String enSoundRef
	String czWord
	String czSoundRef

	NoteModel model

	private String sndField(String soundRef) {
		if (!soundRef) return ""
		return "[sound:$soundRef]"
	}

	public List<String> toFields(boolean fitToModel=true) {
		 List<String> fls = [enWord, sndField(enSoundRef) , czWord, sndField(czSoundRef)].collect { it?:""}
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

	@Override
	public String toString() {
		return "[enWord=" + enWord +  ", czWord=" + czWord + "]";
	}
}

public class NoteModel {

	String uuid
	int fieldsCount

	public void assertIsComplete() {
		assert uuid
		assert fieldsCount
	}


	@Override
	public String toString() {
		return "NoteModel [uuid=" + uuid + ", fieldsCount=" + fieldsCount + "]";
	}



}
