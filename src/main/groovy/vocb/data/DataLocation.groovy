package vocb.data;

import java.nio.file.Path
import java.nio.file.Paths

public class DataLocation {
	
	String filename = "concepts.yaml"	
	Path storageRootPath = Paths.get("/data/src/AnkiVocb/db/")
	
	Path getStoragePath()  {
		storageRootPath.resolve(filename.toString())
	}
	
	@Lazy Path mediaRootPath = {
		storagePath.resolve("media")
	}()
	
	boolean dirty
	
	

}
