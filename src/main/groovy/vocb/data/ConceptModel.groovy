package vocb.data;

import groovy.transform.Canonical;
import groovy.transform.ToString

public enum State {
   populate, done, ignore
   
}
@Canonical
@ToString(excludes='concepts')
public class ConceptDb {
	String version
	List<Concept> concepts = []
}

@Canonical
public class Concept {
	List<Term> terms
	//State state
	String state
	String img
	Double freq
	List<String> origins = []
}

@Canonical
public class Term {
	String term
	String lang
	String tts
}
