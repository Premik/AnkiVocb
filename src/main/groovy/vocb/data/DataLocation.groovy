package vocb.data;

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.ToString

@ToString(includes=["storagePath"], includePackage=false)
public class DataLocation {
	
	String filename = "concepts.yaml"	
	Path storageRootPath = Paths.get("/data/src/AnkiVocb/db/")
	
	Path getStoragePath()  {
		storageRootPath.resolve(filename.toString())
	}
	
	@Lazy Path mediaRootPath = {
		storagePath.resolve("media")
	}()
	
	boolean exists() {
		Files.exists(storagePath)
		
	}
	
	boolean dirty
	
	

}
