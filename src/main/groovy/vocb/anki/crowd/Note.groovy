package vocb.anki.crowd;

public class NoteFields {

	String enWord
	String enSoundRef
	String czWord



	public List<String> toFields() {
		return [enWord, enSoundRef, czWord]
	}

	public String getGuid() {
		return "vsocb$enWord"
	}

	@Override
	public String toString() {
		return "[enWord=" + enWord +  ", czWord=" + czWord + "]";
	}
}
