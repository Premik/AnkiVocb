package vocb.data

import java.util.stream.Collector
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(excludes=['concepts', 'examples'], includePackage=false)
public class ConceptDb {
	String version = "0.0.1"
	List<Concept> concepts = []
	List<Example> examples = []

	private List<DataLocation> dataLocations

	public List<Term> examplesByLang(String lng) {
		examples.collectMany {it.termsByLang(lng) }
	}

	public List<Term> conceptsByLang(String lng) {
		concepts.findAll {it.state != "ignore"}.collectMany {it.termsByLang(lng)}
	}

	public Stream<TermContainer> getTermContainers() {
		return Stream.concat(concepts.stream(), examples.stream())
	}


	public List<String> validate() {
		List<String> ret = []
		termContainers.forEach { o->
			List<String> innerVal = o.validate()
			if (innerVal) {
				ret.add("$o.firstTerm: ${innerVal.join('|')}")
			}
		}
		return ret
	}

	public List<DataLocation> getDataLocations() {
		if (!dataLocations) {
			dataLocations = termContainers
					.map {it.location}
					.filter {it}
					.collect(Collectors.toSet()).toList()
		}
		return dataLocations
	}
	
	
	public boolean flush() {
		concepts*.updateDataLocationDirty()
		examples*.updateDataLocationDirty()
		dataLocations = null
	}
	
	public void assignDataLocationToAll(DataLocation dl) {
		concepts*.location = dl
		concepts*.location = dl
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