package vocb.conf;

import java.nio.file.Files
import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.transform.ToString
import vocb.Helper

@CompileStatic
@ToString(
includeNames=false,
ignoreNulls=true,
includePackage=false,
includes=["name", "children"]
)
public class TreeConf {

	Path path
	TreeConf parent

	@Lazy
	List<TreeConf> children = {
		assert path

		path.toFile()
				.listFiles()				
				.findAll(subFolderFilter)
				.collect { it.toPath() }
				.collect { Path cp->
					new TreeConf(
						path:cp, 
						parent:this,
						subFolderFilter: subFolderFilter
						)
				}
	}()
	
	String getName() {
		path?.fileName
	}

	Closure<Boolean> subFolderFilter = Helper.subFolderFilter

	public List<CharSequence> validate() {
		List<CharSequence> ret = []		
		if (!path || !Files.isDirectory(path) || !Files.exists(path) ) {
			ret.add("The '$path' is not an existing directory")
		}
		Set<TreeConf> visited = []
		deepFindChild { TreeConf t->
			if (visited.contains(t)) {
				ret.add("Cycle detected in the TreeConf $t")
				return false
			}
			return true
		}
		List<TreeConf> wrongLinks = children.findAll {it.parent != this}
		if (wrongLinks) {
			ret.add("Children has unknown parent: $wrongLinks")
		}
		return ret
	}

	boolean getIsRoot() {
		parent == null
	}
	boolean getIsLeaf() {
		!children
	}


	List<TreeConf> getParents() {
		if (isRoot) return []
		return [this, *parent.parents] as List<TreeConf>
	}

	TreeConf getRoot() {
		if (isRoot) return this
		return parents[-1]
	}

	List<TreeConf> deepFindChild(Closure<Boolean> cl, boolean includeSelf=true) {
		List<TreeConf> ret = []
		if (includeSelf) {
			if (!cl(this)) return ret
			ret.add(this)
		}
		for (TreeConf t : children) {
			if (!cl(t)) return ret
			ret.add(t)
		}
	}
}
