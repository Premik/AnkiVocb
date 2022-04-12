package vocb.pck

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import vocb.conf.TreeConf
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example

@Canonical
@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false
)
public class ExportItem {

	Concept concept
	Example example=Example.empty
}
