package vocb.anki.crowd;

import vocb.Helper

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
