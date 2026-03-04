package spl.lae;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainTest {
    @Test
    void testMainHappyPath() throws Exception {
        // prepare paths
        String inFile = "happy_input.json";
        String outFile = "happy_output.json";

        // json content: [[1,2],[3,4]] + [[5,6],[7,8]]
        // result must be: [[6,8],[10,12]]
        String jsonContent = """
            {
                "operator": "+",
                "operands": [
                    [[1.0, 2.0], [3.0, 4.0]],
                    [[5.0, 6.0], [7.0, 8.0]]
                ]
            }
        """;

        // create temp file
        Files.writeString(Path.of(inFile), jsonContent);

        // run main (2 threads)
        Main.main(new String[]{inFile, outFile, "2"});

        // check output file
        File out = new File(outFile);
        assertTrue(out.exists());

        // verify numbers
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(out);
        JsonNode res = root.get("result");

        assertNotNull(res);
        // check 1+5 = 6
        assertEquals(6.0, res.get(0).get(0).asDouble());
        // check 4+8 = 12
        assertEquals(12.0, res.get(1).get(1).asDouble());

        // clean trash
        new File(inFile).delete();
        out.delete();
    }  
    
    String outPath = "test_bad_out.json";

    @AfterEach
    void clean() {
        // delete file after finish
        new File(outPath).delete();
    }

    @Test
    void testFileNotFound() throws IOException {
        // give main fake file
        String[] args = {"ghost_file.json", outPath, "2"};
        Main.main(args);

        // read result file
        String content = Files.readString(Path.of(outPath));
        
        // check if has error word
        assertTrue(content.contains("error"));
    }

    @TempDir
    Path tempDir; // junit make temp folder here

    @Test
    void testMainBadMathPath() throws Exception {
        // bad math json (2x2 multiply 3x3)
        String badInput = "{" +
                "\"operator\": \"*\"," +
                "\"operands\": [" +
                "   [[1, 2], [3, 4]]," +
                "   [[1, 0, 0], [0, 1, 0], [0, 0, 1]]" +
                "]" +
                "}";

        Path inputPath = tempDir.resolve("bad_input.json");
        Path outputPath = tempDir.resolve("error_output.json");

        // write input file
        Files.writeString(inputPath, badInput);

        // run main app with 2 threads
        String[] args = {inputPath.toString(), outputPath.toString(), "2"};
        Main.main(args);

        // read result file
        String content = Files.readString(outputPath);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(content);

        // check if error field exist
        assertTrue(root.has("error"), "output file need have error field");
    }
}
