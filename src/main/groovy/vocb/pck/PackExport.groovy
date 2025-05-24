package vocb.pck


import static vocb.Ansi.*

import java.util.stream.Stream

import groovy.transform.CompileStatic
import groovy.transform.ToString
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager



@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false,
)
@CompileStatic
public class PackExport {

	Manager dbMan
	PackInfo info
	LinkedHashSet<Example> examplesToExport = [] as LinkedHashSet

	void collectSentencesForExport(String text) {
		assert text
		dbMan.withBestExample(text) { Example e, String sen, Set<String> com, Set<String> mis->

			if (!mis)  {
				//Exact match, jsut export
				println color(sen, BOLD)
				examplesToExport.add(e)
				return
			}

			String col = NORMAL
			if (mis.size() > 1 || !e)  {
				col = RED
			} else {
				examplesToExport.add(e)
			}
			println "${color(sen, col)} -> ${color(e?.firstTerm, BLUE)} ${color(mis.join(' '), MAGENTA)}"
		}
	}

	Stream<ExportItem> sentencesExport() {
		if (!info?.sentences) return Stream.empty()
		collectSentencesForExport(info.sentencesText)
		assert dbMan
		examplesToExport.stream()
				.flatMap {Example e->
					dbMan.conceptsFromWordsInExample(e).stream().filter {Concept c->
						//If strictlyWordlist, exclude concept which are not listed in the pack wordlist
						!info.strictlyWordlist || info.wordList.contains(c.firstTerm)
					}.map { Concept c->
						new ExportItem(concept: c, example: e)
					}
				}
	}

	Stream<ExportItem> wordListExport() {
		//When strictlyWordlist the wordList is supposed to be exported as part of sentences already
		if (!info?.wordList ||info.strictlyWordlist) return Stream.empty()
		assert dbMan
		info.wordList.stream().map { String w->
			Concept c =dbMan.conceptByFirstTerm[w]
			if (c == null) println "Concept not found for word:'${color(w, BOLD)}'"
			return c
		}
		.filter {it!=null}
		.map { Concept c->
			new ExportItem(concept: c)
		}
	}
	
	Stream<ExportItem> export() {
		Stream.concat(sentencesExport(), wordListExport())
	}
	
}
