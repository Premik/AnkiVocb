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
@ToString(
	includeNames=true,
	ignoreNulls=true,
	includePackage=false	
	)
public class RootInfo {
		
	
	List<PackInfo> pkgs = []
	Path rootPath
	
	RootInfo buildFromRootPath(Path path) {
		assert path				
		assert Helper.subFolderCountIn(path) > 0 : "No packages found. $path has no sub-folders"
		new RootInfo(rootPath: path).tap {
			
		}		
	}
	
		
}
