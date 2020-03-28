package vocb.anki.crowd;

public class NoteFields {

	String enWord
	String enSoundRef
	String czWord



	public List<String> toFields() {
		return [enWord, "[sound:$enSoundRef]" , czWord]
	}

	public String getGuid() {
		return "vsocb_$enWord"
	}

	@Override
	public String toString() {
		return "[enWord=" + enWord +  ", czWord=" + czWord + "]";
	}
}

public class NoteModel {

	String uuid
	int fieldsCount


	@Override
	public String toString() {
		return "NoteModel [uuid=" + uuid + ", fieldsCount=" + fieldsCount + "]";
	}



}
