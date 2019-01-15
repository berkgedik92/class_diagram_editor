package project.diagram.Controllers;

import project.diagram.Services.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.*;

//Manages save and load requests for diagrams
@RestController
@RequestMapping("api/diagram")
class DiagramController {

    //The folder where we keep JSON files for diagrams
    @Value("${diagramFolder}")
    private String diagramFolder;

    //The file name template for diagram JSON file
    @Value("${diagramFile}")
    private String saveFileName;

    @Autowired
    private FileOperations fileOperations;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseEntity<?> save(@RequestBody String diagramData) {

        Integer fileCount = fileOperations.fileAmount(diagramFolder) + 1;
        String fileName = saveFileName.replace("{x}", fileCount.toString());

        try {
            fileOperations.saveTextFile(diagramData, fileName, diagramFolder);
        }
        catch (FileNotFoundException fileException) {
            return new ResponseEntity<>("Cannot save data : Tried to save the diagram with using invalid filename", HttpStatus.BAD_REQUEST);
        }
        catch (UnsupportedEncodingException encodingException) {
            return new ResponseEntity<>("Cannot save data : Tried to save the diagram with unsupported encoding", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/load", method = RequestMethod.GET)
    public ResponseEntity<?> load() {
        int fileCount = fileOperations.fileAmount(diagramFolder);

        //If there is no files saved before, return an empty diagram
        if (fileCount == 0)
            return new ResponseEntity<>("", HttpStatus.OK);

        try {
            String fileName = saveFileName.replace("{x}", Integer.toString(fileCount));
            String result = fileOperations.readTextFile(fileName, diagramFolder);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        catch (FileNotFoundException fileException) {
            return new ResponseEntity<>("Cannot load data : Tried to open non-existing file", HttpStatus.BAD_REQUEST);
        }
        catch (UnsupportedEncodingException encodingException) {
            return new ResponseEntity<>("Cannot load data : Unsupported encoding (file might be corrupted)", HttpStatus.BAD_REQUEST);
        }
        catch (IOException ioException) {
            return new ResponseEntity<>("Cannot load data : cannot read save file properly (file might be corrupted)", HttpStatus.BAD_REQUEST);
        }
    }
}
