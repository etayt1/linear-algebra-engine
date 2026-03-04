package spl.lae;
import java.io.IOException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
        LinearAlgebraEngine engine = null;
        try {
            InputParser parser = new InputParser();
            ComputationNode root = parser.parse(args[0]); 
            
            engine = new LinearAlgebraEngine(Integer.parseInt(args[2]));
            ComputationNode result = engine.run(root);

            OutputWriter.write(result.getMatrix(), args[1]);      
            System.out.println(engine.getWorkerReport());
        } catch (Exception e) {
            if (engine != null)
                engine.initiateShutdown();
            OutputWriter.write(e.getMessage(), args[1]);
        }
    }
}