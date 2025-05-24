package vocb.pck

import static vocb.Ansi.*

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import java.util.stream.Stream

import org.codehaus.groovy.runtime.MethodClosure

import groovy.transform.CompileStatic
import groovy.transform.ToString
import vocb.anki.crowd.Data2Crowd
import vocb.conf.ConfHelper
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager

@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false
)
@CompileStatic
public class PackExport {


	PackInfo info
	LinkedHashSet<Example> examplesToExport = [] as LinkedHashSet
	WordNormalizer wn = WordNormalizer.instance
	Data2Crowd data2crowd
	boolean silent=false


	Manager getDbMan() {
		data2crowd?.dbMan
	}

	ConfHelper getConfHelper() {
		data2crowd?.cfgHelper
	}

	void collectSentencesForExport(String text) {
		assert text
		dbMan.withBestExample(text) { Example e, String sen, Set<String> com, Set<String> mis->

			if (!mis)  {
				//Exact match, just export
				if (!silent) {
					println color(sen, BOLD)
				}
				examplesToExport.add(e)
				return
			}

			String col = NORMAL
			if (mis.size() > 1 || !e)  {
				col = RED
			} else {
				examplesToExport.add(e)
			}
			if (!silent) {
				println "${color(sen, col)} -> ${color(e?.firstTerm, BLUE)} ${color(mis.join(' '), MAGENTA)}"
			}
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
					}
					.filter {it!=null && !it.ignore}
					.map { Concept c->
						new ExportItem(concept: c, example: e)
					}
				}
	}

	Stream<ExportItem> wordListExport() {
		//When strictlyWordlist the wordList is supposed to be exported as part of sentences already
		if (!info?.wordList ||info.strictlyWordlist) return Stream.empty()
		assert dbMan
		info.wordList.stream().map { String w->
			//Concept c =dbMan.conceptByFirstTerm[w]
			Concept c = dbMan.findConceptByFirstTermAnyVariant(w)


			if (c == null && !silent) println "Concept not found for word:'${color(w, BOLD)}'"
			//assert c : "Concept not found for word:'${color(w, BOLD)}'"
			return c
		}
		.filter {it!=null && !it.ignore}
		.map { Concept c->
			new ExportItem(concept: c)
		}
	}

	Stream<ExportItem> export() {
		Stream.concat(sentencesExport(), wordListExport())
	}

	@Lazy
	Set<String> exportedWords = {
		MethodClosure mc = LinkedHashSet.&new as MethodClosure
		export().map { ExportItem ei->
			ei.concept.firstTerm
		}
		.collect(Collectors.toCollection(mc)) as Set<String>
	}()

	public debugDumpTo(Path folder) {
		int fileCount=0
		assert folder
		Path trgPath = folder.resolve(info.treeConf.relativePath)
		Files.createDirectories(trgPath)
		Closure cp = { Path p->
			if (!Files.exists(p)) return
				Files.copy(p, trgPath.resolve(p.fileName), StandardCopyOption.REPLACE_EXISTING)
			fileCount++
		}
		Closure pw = { String name, Closure c->
			trgPath.resolve(name).withPrintWriter(StandardCharsets.UTF_8.toString(), c)
			fileCount++
		}

		Closure pl = { String name, Iterable it->
			pw(name) { PrintWriter w->
				it.forEach {
					w.println(it)
				}
				
			}
		}

		cp(info.sentencesPath)
		cp(info.wordsPath)
		pl("sentences-reparsed.txt", info.sentences)
		pl("words-sorted.txt", info.wordList.toSorted())
		
		List<String> wordDups = info.wordList.groupBy { it }.findAll { k,v-> v.size() >1 }.collect{k,v->v[0]}

		if (wordDups) {
			pl("words-dups.txt", wordDups)
		}
		pl("words-exported.txt", exportedWords)
		pl("sentences-exported.txt",
			sentencesExport()
					.map {ExportItem ex-> ex.example.firstTerm}
					.toList()
		)
		println "Dumped $fileCount files to the $trgPath"
	}
}
