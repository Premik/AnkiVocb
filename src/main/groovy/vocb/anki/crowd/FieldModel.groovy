package vocb.anki.crowd

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.transform.ToString
import vocb.Helper

@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false,
includes=['name', 'ord']
)
public class FieldModel{

	
	String font= "Liberation Sans"
	String[] media = []
	String  name
	int ord = -1
	boolean rtl = false
	int size = 20
	boolean sticky = false

	public void assertIsComplete() {
		assert ord > -1
		assert name
	}
}
