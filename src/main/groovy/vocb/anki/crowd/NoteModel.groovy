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
	boolean latexsvg
	String name = "anikvob"
	List req
	int sortf
	List<String> tags
	List<TemplateModel> tmpls
	int type
	List vers
	

	private String[] getFieldNames() {
		assureIsComplete()
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
	
	public setTmpls(Object newTmpls) {
		assert newTmpls
		List l = newTmpls as List
		if (l.size() ==0) {
			tmpls.clear()
			return
		}
		tmpls = l.collect {
			if (it instanceof TemplateModel) return it
			return new TemplateModel(it)
		}
		
	}

	String getFielValue(Note note, String name) {
		assert name
		note.assertIsComplete() 		
		return note[getFieldIndex(name)]
	}
	
	String setFielValue(Note note, String name, Object value) {
		assert name
		note.assertIsComplete()
		note[getFieldIndex(name)] = value?.toString()?:""
	}

	int getFieldIndex(String name, boolean mustExists=true) {
		assert name
		assureIsComplete()
		int i = flds.findIndexOf {it.name == name }
		if (mustExists) {
		assert i >-1 : "The field '$name' not found. Fields in the model: ${flds.collect{it.name}}"
		}
		return i 
	}

	public void assureIsComplete() {
		assert name
		assert flds
		
		flds.sort { FieldModel fm1, FieldModel fm2 ->
			fm1.ord <=> fm2.ord
		}
		flds.eachWithIndex {FieldModel m, int i -> m.ord = i }
		assert flds*.assertIsComplete()
		assert tmpls
		tmpls.eachWithIndex {TemplateModel m, int i -> m.ord = i }
		assert tmpls*.assertIsComplete()

		if (!crowdanki_uuid) {
			crowdanki_uuid = "ankivocb-0.0.1-$name"
		}
	}
}
