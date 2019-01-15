package project.diagram.Services;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class FileOperations {

    //Returns the amount of files in a folder
    public int fileAmount(String folderName) {
        File[] fList = new File(folderName).listFiles((dir, name) -> name.endsWith(".json"));
        return Objects.requireNonNull(fList).length;
    }

    public void saveTextFile(String data, String fileName, String folderName)
            throws FileNotFoundException, UnsupportedEncodingException {

        String fullFilePath = folderName + File.separator + fileName + ".json";
        PrintWriter writer = new PrintWriter(fullFilePath, "UTF-8");
        writer.print(data);
        writer.close();
    }

    public String readTextFile(String fileName, String folderName) throws IOException {

        String fullFilePath = folderName + File.separator + fileName + ".json";
        StringBuilder builder = new StringBuilder();
        String line;

        FileInputStream fileInputStream = new FileInputStream(fullFilePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        while ((line = bufferedReader.readLine()) != null)
            builder.append(line).append("\n");

        fileInputStream.close();
        inputStreamReader.close();
        bufferedReader.close();

        return builder.toString();
    }
}
