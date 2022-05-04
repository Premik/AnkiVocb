package vocb.data

import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

import groovy.transform.Canonical
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.ToString

@Canonical
@ToString(excludes=['concepts', 'examples', 'dataLocations'], includePackage=false)
@CompileStatic
public class ConceptDb {
	String version = "0.0.1"
	List<Concept> concepts = []
	List<Example> examples = []

	 
	List<DataLocation> getDataLocations() {  termContainers
					.map {
						assert it.location
						it.location
						}
					//.filter {it}
					.collect(Collectors.toSet()).toList()
	}
	
	public void dedup() {
		concepts = new LinkedHashSet(concepts).toList() 
		examples = new LinkedHashSet(examples).toList()
	}

	public List<Term> examplesByLang(String lng) {
		examples.collectMany {it.termsByLang(lng) }
	}

	public List<Term> conceptsByLang(String lng, Closure conceptFilter= {Concept c->!c.ignore}) {
		concepts.findAll(conceptFilter).collectMany {it.termsByLang(lng)}
	}
	
	
	@CompileDynamic
	public Stream<TermContainer> getTermContainers() {
		return Stream.concat(concepts.stream(), examples.stream())
	}


	public List<String> validate(ValidationProfile vp) {
		assert vp
		List<String> ret = []
		termContainers.forEach { o->
			List<String> innerVal = o.validate(vp)
			if (innerVal) {
				ret.add("${o.firstTerm.padRight(20)}: ${innerVal.join('|')}".toString())
			}
		}
		return ret
	}

	
	
	public boolean flush() {
		concepts*.updateDataLocationDirty()
		examples*.updateDataLocationDirty()
		//this.@$dataLocations = null
	}
	
	public void assignDataLocationToAll(DataLocation dl) {
		concepts*.location = dl
		examples*.location = dl
		flush()
	}
	
	public void mergeWith(ConceptDb ctd) {
		if (!ctd) return
		assert examples.intersect(ctd.examples).empty
		assert concepts.intersect(ctd.concepts).empty
		concepts.addAll(ctd.concepts)
		examples.addAll(ctd.examples)		
	} 
	
}