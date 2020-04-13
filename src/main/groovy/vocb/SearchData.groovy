package vocb

import groovy.transform.ToString

@ToString(includeNames=true, excludes='imageType,license')
public class SearchData {
	String q
	int count=3
	String imageType="Clipart"
	String license="Public"
	List<URL> results=[]
	int selected=-1
	boolean useBlank=false
}
