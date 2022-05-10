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
import vocb.Helper
import vocb.anki.crowd.Data2Crowd
import vocb.conf.ConfHelper
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.ExampleComparatorMatch
import vocb.data.Manager

@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false
)
@CompileStatic
public class PackExport {


	PackInfo info

	WordNormalizer wn = WordNormalizer.instance
	Data2Crowd data2crowd
	boolean silent=false



	Manager getDbMan() {
		data2crowd?.dbMan
	}

	ConfHelper getConfHelper() {
		data2crowd?.cfgHelper
	}

	Stream<Example> sentencesForExport() {
		info.sentences.parallelStream().map {String s->			
			ExampleComparatorMatch m = dbMan.bestExampleForSentence(s)[0]
			dbMan.silent
			if (!m && !dbMan.silent) {
				println "No example for '$s'"
			}
			return m
		}.filter {it as boolean}
		.map { ExampleComparatorMatch m->
			if (!silent) {
				println m.toAnsiString()
			}
			assert m?.b?.example
			m.b.example
		}
	}

	LinkedHashSet<Example> sentencesExportLegacy(String text) {
		assert text
		LinkedHashSet<Example> examplesToExport = [] as LinkedHashSet
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
		return examplesToExport
	}

	Stream<ExportItem> sentencesExportLegacy() {
		if (!info?.sentences) return Stream.empty()
		assert dbMan

		sentencesExportLegacy(info.sentencesText).stream()
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

	Stream<ExportItem> sentencesExport() {
		if (!info?.sentences) return Stream.empty()
		assert dbMan
		sentencesForExport()		
				.flatMap {Example e->
										
					dbMan.conceptsFromWordsInExample(e).stream().filter {Concept c->
						//If strictlyWordlist, exclude concept which are not listed in the pack wordlist
						if (!info.strictlyWordlist ) return true 												
						info.wn.wordVariantsWithBrackets(c.firstTerm).any {info.wordList.contains(it)}						
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
		//silent = false
		Helper.startWatch()
		int fileCount=0
		assert folder
		Path trgPath = folder.resolve(info.treeConf.relativePath)		
		Files.createDirectories(trgPath.resolve("sorted"))
		Closure<Path> pw = { String name, Closure c->
			fileCount++
			Path t = trgPath.resolve(name)
			t.withPrintWriter(Helper.utf8, c)
			return t			
		}
		
		Closure cps = { Path p->
			assert Files.exists(p)
			pw("sorted/$p.fileName") {PrintWriter w->
				p.withReader(Helper.utf8) { Reader r->  
					r.readLines().toSorted().each {
						w.println(it)
					}
				}
			}		
		}
		Closure cp = { Path p->
			if (!Files.exists(p)) return
			Path t = trgPath.resolve(p.fileName)
			Files.copy(p, t, StandardCopyOption.REPLACE_EXISTING)			
			fileCount++
			cps(t)
		}
		Closure pws = { String name, Closure c->
			cps(pw(name, c))
		}
		

		Closure pl = { String name, Iterable it->
			pws(name) { PrintWriter w->
				it.forEach {
					w.println(it)
				}
			}
		}

		cp(info.sentencesPath)
		cp(info.wordsPath)
		pl("sentences-reparsed.txt", info.sentences)		

		List<String> wordDups = info.wordList.groupBy { it }.findAll { k,v-> v.size() >1 }.collect{k,v->v[0]}

		if (wordDups) {
			pl("words-dups.txt", wordDups)
		}

		pl("words-exported.txt", exportedWords)
		pl("sentences-exported.txt",
				sentencesExport()
				.map {ExportItem ex->
					ex.example.firstTerm
				}
				.toList() as LinkedHashSet
				)
		Helper.printLapseTime()
		println "Dumped $fileCount files to the $trgPath"
	}
}
