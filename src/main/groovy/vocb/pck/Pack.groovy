package vocb.pck

import static vocb.Ansi.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Map.Entry

import groovy.transform.CompileStatic
import vocb.Helper
import vocb.anki.crowd.Data2Crowd
import vocb.conf.ConfHelper
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.ord.ConceptExtra
import vocb.ord.OrderSolver

@CompileStatic
public class Pack {

	Path destRootFolder = Paths.get("/tmp/work/pkg")
	final Path packageRootPath = Paths.get("/data/src/AnkiVocb/pkg/")

	ConfHelper cfgHelper = new ConfHelper()
	@Lazy ConfigObject cfg = cfgHelper.config

	@Lazy List<String> allPackageNames = packageRootPath.toFile().listFiles().collect {it.name}
	
	@Lazy List<PackInfo> allPackInfos = {
		List<PackInfo> ret = []
		ParentInfo parent = null
		packageRootPath.eachDirRecurse { Path d->
			if (Helper.subFolderCountIn(d) == 0) { //Leaf
				PackInfo pi = new PackInfo(
					name:null,
					parent: parent, 
					packageRootPath: packageRootPath, 
					destRootFolder: destRootFolder)
				ret.add(pi)
			} else {
				if (parent) {
					parent = new ParentInfo(path:d, parent:parent)
				} else {
					parent = new ParentInfo(path:d)
				}
			}
		}
		return ret
	}()

	@Lazy Map<String, PackInfo> allPackages = allPackageNames.collectEntries {String name->
		[
			name,
			new PackInfo(name:name, packageRootPath: packageRootPath, destRootFolder: destRootFolder)
		]
	}




	WordNormalizer wn = new WordNormalizer()

	LinkedHashSet<Example> exportExamples = [] as LinkedHashSet


	void doExport(PackInfo info) {
		assert info
		File pkgFile = info.packagePath.toFile()
		cfgHelper.extraLookupFolders.add(pkgFile)
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		if (info.sentences) {
			exportSentences(info.sentences, d2c.dbMan)
			d2c.exportExamplesToCrowd(exportExamples)
		}

		if (info.wordList) {
			d2c.exportWordsToCrowd(info.wordList)
		}


		cfgHelper.extraLookupFolders.remove(pkgFile)
	}

	void exportSentences(String text, Manager dbMan) {
		assert text
		dbMan.withBestExample(text) { Example e, String sen, Set<String> com, Set<String> mis->

			if (!mis)  {
				println color(sen, BOLD)
				exportExamples.add(e)
				return
			}

			String col = NORMAL
			if (mis.size() > 1 || !e)  {
				col = RED
			} else {
				exportExamples.add(e)
			}
			println "${color(sen, col)} -> ${color(e?.firstTerm, BLUE)} ${color(mis.join(' '), MAGENTA)}"
		}
	}
	
	static Pack buildFromRootPath(Path path) {
		assert path
		assert Helper.subFolderCountIn(path) > 0 : "No packages found. $path has no sub-folders"
		new Pack(packageRootPath: path)
		
	}
	
	static boolean isFolderPackage(Path folder) {
		assert folder
		assert Files.isDirectory(folder)
		return Files.isRegularFile(folder.resolve("packageDescription.md"))		
	}



	public static void main(String[] args) {

		int cnt = new Pack().allPackages.size()
		(0..cnt-1).stream().parallel()
				.forEach( {int p->
					//if (p == 0)
					new Pack().with {
						//allPackages.each {println it}
						Collection<PackInfo>  pkgs
						synchronized (Pack.class ) {
							pkgs = [allPackages.values()[p]]
							//pkgs = [allPackages["MaryHadALittleLamb"]]
							//pkgs = [allPackages.values()[p]]
						}


						pkgs.each { PackInfo i->
							println '*'*100
							println "* ${i.name}"
							println '*'*100

							doExport(i)
						}

					}
				})

		println "Done"

	}
}
