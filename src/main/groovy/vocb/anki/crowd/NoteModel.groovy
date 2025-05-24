package vocb.anki.crowd

import groovy.transform.ToString

@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false,
includes=['name', 'flds']
)
public class NoteModel  {


	String __type__ = "Note"
	String crowdanki_uuid	
	String css	
	List<FieldModel> flds = []
	String latexPost
	String latexPre
	String name = "anikvob"
	List req
	int sortf
	List<String> tags
	List tmpls
	int type
	List vers
	

	private String[] getFieldNames() {
		assertIsComplete()
		flds.collect {it.name}
	}
	
	public setFlds(Object newFlds) {		
		assert newFlds
		List l = newFlds as List
		if (l.size() ==0) {
			flds.clear()
			return
		}
		flds = l.collect {
			if (it instanceof FieldModel) return it
			return new FieldModel(it)
		}
		
	}

	String getFielValue(Note note, String name) {
		note.assertIsComplete() 		
		return note[getFieldIndex(name)]
	}
	
	String setFielValue(Note note, String name, String value) {
		note.assertIsComplete()
		note[getFieldIndex(name)] = value
	}

	int getFieldIndex(String name, boolean mustExists=true) {
		assert name
		assertIsComplete()
		int i = flds.findIndexOf {it.name = name }
		if (mustExists) {
		assert i >-1 : "Field $name not found. Fields: $flds"
		}
		return i 
	}

	public void assertIsComplete() {
		assert name
		assert flds
		
		flds.sort { FieldModel fm1, FieldModel fm2 ->
			return fm1.ord <=> fm2.ord
		}
		flds.eachWithIndex {FieldModel m, int i -> m.ord = i }
		assert flds*.assertIsComplete()

		if (!crowdanki_uuid) {
			crowdanki_uuid = "ankivocb-0.0.1-$name"
		}
	}
}
