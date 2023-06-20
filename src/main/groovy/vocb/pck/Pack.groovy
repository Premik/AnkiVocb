package vocb.pck

import static vocb.Ansi.*

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import vocb.Helper
import vocb.anki.ProfileSupport
import vocb.anki.crowd.Data2Crowd
import vocb.conf.ConfHelper
import vocb.conf.TreeConf
import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.ExampleComparatorMatch
import vocb.data.Manager
import vocb.template.Render

//@CompileStatic
public class Pack {

	ConfHelper cfgHelper = ConfHelper.instance

	@Lazy
	Path destRootFolder = cfgHelper.outPath.resolve("pkg")

	@Lazy
	Path dbgOutPath = Paths.get("/tmp/work/vocbDebug").tap {
		Files.createDirectories(it)
	}


	@Lazy
	Path packageRootPath = cfgHelper.pkgPath
	boolean silent=false


	//ConfHelper cfgHelper = new ConfHelper()
	//@Lazy ConfigObject cfg = cfgHelper.config

	@Lazy
	volatile TreeConf<PackInfo> treeConf = new TreeConf<PackInfo>(subFolderFilter:this.&isFolderPackage, path: packageRootPath)

	@Lazy
	volatile List<PackInfo> allPackInfos = {
		treeConf.leafs.collect { TreeConf<PackInfo> tc->
			PackInfo pi = new PackInfo(
					pack: this,
					treeConf: tc).tap {
						tc.obj= it
					}

			return pi
		}
	}()

	@Lazy
	volatile Manager dbMan = {
		Manager.defaultInstance
		//new Manager(defaultExamplesFileName:"examplesDraft.yaml", silent:silent).tap {load()}
	}()

	WordNormalizer wn = WordNormalizer.instance


	@Memoized
	PackExport packExportOf(PackInfo info) {
		assert info
		ConfHelper cfgHelper = new ConfHelper()
		Data2Crowd d2c = new Data2Crowd (info : info, dbMan : dbMan, cfgHelper:cfgHelper)
		return new PackExport(data2crowd: d2c, info:info, silent:silent)
	}

	public List<PackExport>  packExportsOf(String ... names) {
		pkgsByName(names).collect {
			packExportOf(it)
		}
	}

	void doExport(PackInfo info) {
		assert info
		File pkgFile = info.treeConf.path.toFile()
		packExportOf(info).with {
			confHelper.extraLookupFolders.add(pkgFile)
			data2crowd.export(export())
			confHelper.extraLookupFolders.remove(pkgFile)
		}
	}

	Set<String> exportedWordsOf(String ... names) {
		pkgsByName(names).parallelStream()
				.filter {
					it as Boolean
				}
				.map {
					packExportOf(it)
				}
				.flatMap {
					it.exportedWords.stream()
				}
				.toList() as LinkedHashSet
	}


	Map<String, Set<PackExport>> packExportsByExportedWord(String ... pkgNames) {
		Map<String, Set<PackExport>> ret = [:].withDefault { [] as LinkedHashSet }
		pkgsByName(pkgNames).parallelStream().map {
			packExportOf(it).tap {
				exportedWords //prefetch
			}
		}.forEach { PackExport pe->
			pe.exportedWords.each { String w->
				ret[w].add(pe)
			}
		}
		return ret
	}

	void savePackByWordList() {
		Helper.startWatch()
		dbgOutPath.resolve("word2pkg.txt").withPrintWriter("UTF8") { PrintWriter w->
			packExportsByExportedWord()
					.toSorted{Map.Entry<String, Set<PackExport>> e-> e.key.toLowerCase()}
					.each { String k, Set<PackExport> v->
						String s = "${k.padRight(20)} : ${v.collect{it.info.name.padRight(20)}.join(' ')}"
						w.println(s)
						println s
					}
		}
		Helper.printLapseTime()
	}


	Set<String> findTopWordsNotInDb(int topx=1000) {
		Set<String> top = Corpus.buildDef().topX(topx) as LinkedHashSet
		//Set<String> topInDb = top.collectMany { dbMan.findConceptsByFirstTermAllVariant(it) }.collect {it.firstTerm} as LinkedHashSet
		top.findAll {
			!dbMan.findConceptsByFirstTermAllVariant(it)
		}
		.findAll {
			!it.contains("'")
		}
		.findAll {
			!it.contains(".")
		}
		.findAll {
			it.size()>1
		} as LinkedHashSet
	}

	Set<Concept> findTopConceptsFromAllPackages(int topx) {
		Helper.startWatch()

		Set<Concept> allExp = exportedItemsFromPackages()
				.map {
					it.concept
				}
				.filter {
					!it.ignore
				}
				.toSet()
				.toSorted {
					0-it.freq
				}
				.take(topx) as LinkedHashSet
		Helper.printLapseTime()
		return allExp
		//allExp.take(100).each {println it}
	}

	void printBestExamplesFor() {


		Collection<String> words='''		
		resource 
		  
		'''.split(/\s+/).reverse().findAll()

		Helper.startWatch("allWordPackages")
		//Set<String> knownWords = exportedWordsFromPackages()
		Collection<String> knownWords = dbMan.db.concepts.toSorted{-1*(it.freq?:0)}.collect{it.firstTerm}
		ExampleComparatorMatch.preferedWords = ( knownWords + ExampleComparatorMatch.preferedWords) as LinkedHashSet
		Helper.printLapseTime("allWordPackages")


		findBestExamplesFor(wn.expandBrackets(words), knownWords)
	}



	void printFirstX(int x=1000) {
		Helper.startWatch()
		//Arrays.parallelSo
		Concept[] topDbAr = dbMan.db.concepts.findAll {
			!it.ignore
		}.toArray() as Concept[]
		Arrays.parallelSort(topDbAr, { Concept a, Concept b->
			(b.freq?:0) <=>(a.freq?:0)
		})

		Set<String> topDb = topDbAr.take(x).collect {
			it.firstTerm
		} as LinkedHashSet

		//Set<String> ignore =  []
		//Set<String> ignore =  exportedWordsOf("Simple", "Supa", "Uncomm", "Basic", "First")
		//Set<String> ignore =  exportedWordsOf("Simple", "Basic1K" )

		Set<String> ignore = exportedWordsFromPackages() - exportedWordsOf("First" ) + exportedWordsOf("Basic" )


		//Prefer the ignored (known) words
		ExampleComparatorMatch.preferedWords = ( ignore + ExampleComparatorMatch.preferedWords) as LinkedHashSet

		Set<String> list = (topDb - ignore) as LinkedHashSet

		Helper.printLapseTime()
		Paths.get("/tmp/work/first${x}.txt").withPrintWriter { PrintWriter w->
			list.each {
				w.println(it)
			}
		}
		List<String> ignoreExtra = [
			"doesn't",
			"does",
			"not",
			"does not",
			"pre",
			"james",
			"James"
		]
		findBestExamplesFor(wn.expandBrackets(list) - ignoreExtra, ignore)
	}

	private void findBestExamplesFor(Collection<String> wordList, Collection<String> highligh=[]) {
		println "${wordList.take(50).join(' ')} \nSize: ${wordList.size()}"
		int lastDec = 0
		//Set<String> matchedVariants = [] as LinkedHashSet
		List<String> matched = []
		Closure<String> colourer =  { ExampleComparatorMatch self, String word, String defaultColor ->
			if (highligh.contains(word) ){
				return ExampleComparatorMatch.invertedFgBgColors.call(self, word, defaultColor)
			}
			return ExampleComparatorMatch.defaultColors.call(self, word, defaultColor)
		}

		Helper.startWatch()
		while (wordList.size() > 0) {

			List<ExampleComparatorMatch> ms = dbMan.bestExampleForSentence(wordList)
			ms.take(500).each { ExampleComparatorMatch m->
				println m.toAnsiString(colourer)
				println "${m.b.example[0]?.term} (${m.b.example[1]?.term}) $m.similarityScore"
				println "${(m.commonWords-highligh).join('\n')}\n"
			}
			//Collection<String> all = ms.collectMany { it.commonWords }.toUnique()
			Collection<String> all = ms.collectMany {
				it.matchesWordlist(wordList)
			}.toUnique()
			(wordList - all).each {
				//println it
			}

			break
			ExampleComparatorMatch m =ms[0]

			if (!m) break
				println "-$lastDec ${m.toAnsiString()}"
			Collection<String> remove = m.matchesWordlist(wordList)
			matched.addAll(remove)
			/*if (!removed) {
			 //remove = m.a.wordsMatchingVariant(m.commonWordVariants)
			 println "Removing variants"					
			 removed = matchedVariants.intersect(list)
			 }*/
			println remove
			lastDec = remove.size()
			wordList-=remove

			if (lastDec == 0) {
				println "No more common words"
				break
			}
		}
		Helper.printLapseTime()
		println "Matched: $matched"

		//println "Vars   : $matchedVariants"
		println "Left   : ${wordList-matched}"

		Paths.get("/tmp/work/matched.txt").withPrintWriter { PrintWriter w->
			matched.each {
				w.println(it)
			}
		}
	}

	void printExamplesExport() {
		Helper.startWatch()
		//exportedExamplesFromPackages(pkgsByName("First")).forEach {

		//}
		//return

		Set<Example> exported = exportedExamplesFromPackages().toSet() as LinkedHashSet
		Set<Example> ignore = exportedExamplesFromPackages(allPackInfos-pkgsByName("First")).toSet() as LinkedHashSet
		println ignore
		dbMan.db.examples.toSorted{it.firstTerm}.each { Example e->
			if (ignore.contains(e)) {
				println color(e.firstTerm, WHITE)
			}
			else {
				if (exported.contains(e)) {
					println color(e.firstTerm, BOLD)
				} else
					println e.firstTerm
			}
		}
		Helper.printLapseTime()
	}

	/*Stream<String> wordsFromPackages(List<PackInfo> infos) {
	 ConfHelper cfgHelper = new ConfHelper()
	 infos.stream()
	 .flatMap { PackInfo info->
	 Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
	 //Words from the whole db without words from any package
	 //wn.lemming(wordsFromAllPackages(packs).stream())
	 }
	 }*/


	void findBasicWords() {
		Set<String> l = exportedWordsOf("Basic") - exportedWordsOf("Simple")
		//def l = exportedWordsOf("BasicWords")
		println l
		l.each {
			new File("/tmp/work/BasicWordsNoSimple.txt") <<"$it\n"
		}
	}


	static Pack buildFromRootPath(Path path) {
		assert path
		assert Helper.subFolderCountIn(path) > 0 : "No packages found. $path has no sub-folders"
		new Pack(packageRootPath: path)
	}

	@CompileDynamic
	static boolean isFolderPackage(Path folder) {
		assert folder
		return Helper.subFolderFilter(folder.toFile())
		//if (!Helper.subFolderFilter(folder.toFile())) return false
		//return Files.isRegularFile(folder.resolve("packageDescription.md"))
	}

	static boolean isFolderPackage(File folder) {
		isFolderPackage(folder.toPath())
	}

	public export(List<PackInfo> pkgs=allPackInfos) {
		int cnt = pkgs.size()
		(0..cnt-1).stream().parallel().forEach( {int p->
			PackInfo pi
			synchronized (Pack.class ) {
				pi = pkgs[p]
			}

			println '*'*100
			println "* ${pi.treeConf.name}"
			println '*'*100

			doExport(pi)
		})

		println "Done"
	}


	public List<PackInfo>  pkgsByName(String ... names) {
		if (!names) return allPackInfos
		Map<String, Boolean> matches = names.collectEntries {[it, false]}
		List<PackInfo> ret = allPackInfos.findAll { PackInfo pi->
			names.any {String name->
				if (pi.treeConf.name.containsIgnoreCase(name)) {
					matches[name] = true
					return true
				}
				return false
			}
		}
		assert matches.each { String pkgSearch, Boolean m->
			assert m : "No package matching '$pkgSearch'"
		}
		return ret
	}


	public exportByName(String name) {
		export(pkgsByName(name))
	}


	Set<String> exportedWordsFromPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collect {packExportOf(it)}
		.collectMany { it.exportedWords } as HashSet
	}

	Stream<Example> exportedExamplesFromPackages( List<PackInfo> pkgInfos = allPackInfos) {
		exportedItemsFromPackages(pkgInfos).map{it.example}.filter {it as boolean}
	}



	Stream<ExportItem> exportedItemsFromPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.parallelStream().flatMap {packExportOf(it).export()}
	}

	Set<String> wordsFromAllPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collectMany {it.allWords}.toSet()
	}


	@Deprecated
	List<PackInfo> findAllPacksWithAnyWord(List<PackInfo> pkgInfos = allPackInfos, Closure wordFilter) {
		pkgInfos.findAll { PackInfo pi->
			pi.allWords.any (wordFilter)
		}
	}

	@Deprecated
	List<PackInfo> findAllPacksWithWord(String word, List<PackInfo> pkgInfos = allPackInfos) {
		findAllPacksWithAnyWord(pkgInfos) { String w->
			w.containsIgnoreCase(word)
		}
	}

	@CompileDynamic
	List<String> sentencesFromAllPackages(List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collect {wn.sentences(it.sentences)}.flatten().toUnique().sort()
	}

	private deleteDupFirst1k() {
		//PackExport first100 = packExportsOf("First")[0]
		//first100.cardsFieldsInDb().collect {List flds->flds[2]}

		Helper.startWatch()
		exportedWordsOf("First", "Basic") //Prefetch in paral.

		println ((exportedWordsOf("First" ) - exportedWordsOf("Basic" )).join(" OR "))
		Helper.printLapseTime()
	}

	private saveGeneralDescription() {
		Path readmePath= Paths.get(cfgHelper.cfg.rootPath).resolve("readme.md")
		assert Files.exists(readmePath)
		Render render = new Render(cfgHelper: cfgHelper)
		String html=render.renderMarkdownText(readmePath.text)
		//Remove html tags not supported in the ankiweb description
		html=html.replaceAll("<p>", "")
		html=html.replaceAll("</p>", "")
		html=html.replaceAll($/<h\d>/$, "\n\n<b>")
		html=html.replaceAll($/</h\d>/$, "</b>\n")
		html=html.replaceAll("doc/example.gif", '''https://raw.githubusercontent.com/Premik/AnkiVocb/master/doc/example.gif''')
		
		Path outP = cfgHelper.resolveOutputPath("readme.html")
		outP.withWriter(StandardCharsets.UTF_8.toString()) { Writer w->
			w.print(html)
		}
		
		println "Saved to $outP"
		//render.render(RED)
	}


	public static void main(String[] args) {

		new Pack().tap { Pack p->
			println(p.packageRootPath)
			silent=true
			saveGeneralDescription()
			//print(allPackInfos.collect { it.displayName })
			//p.exportByName("DuckTales")


			//deleteDupFirst1k()
			//return

			//p.exportByName("Basic1K")
			//packExportsOf("Basic1K").each {
			/*packExportsOf().each {
			 it.debugDumpTo(dbgOutPath)
			 }*/
			//return


			//savePackByWordList()
			//return

			//return
			/*findTopxNotInDb(1000).each {
			 println it
			 }*/
			/*exportedWordsOf("Jing").each {
			 println it
			 }*/
			//printFirstX()
			//ExampleComparatorMatch.preferedWords = Corpus.buildDef(25000).topX(25000) as LinkedHashSet
			//printBestExamplesFor()

			//packExportsOf("Basic").first().debugDumpTo(dbgOutPath)

			//printExamplesExport()
			//p.export()
			//findBasicWords()
		}
		println "Done"
	}
}
