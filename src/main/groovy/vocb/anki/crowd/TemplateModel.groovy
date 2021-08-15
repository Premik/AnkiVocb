package vocb.anki.crowd

import groovy.transform.ToString

@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false,
includes=['name', 'ord']
)
public class TemplateModel  {

	String afmt
	String bafmt
	String bqfmt
	String did
	String name
	int    ord = -1
	String qfmt
	String bfont
	int bsize
	
	
	public void assertIsComplete() {
		assert ord > -1
		assert name
		assert afmt
		assert qfmt
	}
}
