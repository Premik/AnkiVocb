package vocb.pck

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import vocb.Helper


@Canonical
@CompileStatic
@Deprecated
@ToString(
	includeNames=true,
	ignoreNulls=true,
	includePackage=false	
	)
public class ParentInfo {
		
	ParentInfo parent
	List<PackInfo> pkgs = []
	Path path
	
	/*ParentInfo buildFromRootPath(Path path) {
		assert path				
		assert Helper.subFolderCountIn(path) > 0 : "No packages found. $path has no sub-folders"
		return buildFromParentInfo(new ParentInfo(path:path))		
	}
	
	ParentInfo buildFromParentInfo(ParentInfo pi) {
		if (Helper.subFolderCountIn(path) == 0) { //Leaf, this is a PackInfo
			PackInfo p = new PackInfo(
				name:name,
				parent: pi,
				packageRootPath: packageRootPath,
				destRootFolder: destRootFolder)
			ret.add(pi)
		}
		path.eachDir { Path dir->
			
		}
		
	}
		
	}*/
		
}
