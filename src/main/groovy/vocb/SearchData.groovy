package vocb

import groovy.transform.ToString

@ToString(includeNames=true, excludes='imageType,license')
public class SearchData {
	String q
	int count=3
	String imageType="Clipart"
	String license="Public"
	List<URL> results=[]
	boolean clipArt = true
	int selected=-1
}
