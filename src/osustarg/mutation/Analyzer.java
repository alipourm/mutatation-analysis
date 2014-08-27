package osustarg.mutation;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Analyzer {
	@Option(name  = "--file", 
			usage = "Name of JSON file",
			required = true)
	String inFile;
	
	@Option(name  = "--method", 
			usage = "Name of class that implements the technique",
			required = true)
	String method; 
	
	@Option(name = "--out",
			usage = "name of output file",
			required = true)
	String outFile;
	
	
	@Option(name = "--ts_num",
			usage = "number of randomly selected test suites that kills all "
					+ "selected mutants",
			required = true)
	int ts_num;
	
	@Option(name = "--ts_size",
			usage = "size of randomly selected test suites.",
			required = true)
	int ts_size;
	
	
	@Option(name = "--n",
			usage = "number of experiments",
			required = true)
	int n;
	
	JSONParser parser = new JSONParser();
	
	// TODO change the Set types List
	
	JSONArray mutationResult             = new JSONArray();
	HashSet<JSONObject> mutants          = new HashSet<JSONObject>() ;
	HashSet<String> tests                = new HashSet<String>();
	HashSet<JSONObject> killedMutants    = new HashSet<JSONObject>();
	
	
	HashMap<JSONObject,ArrayList<String>> muKillMap = new HashMap<JSONObject, ArrayList<String>>(); 
	/*
	 * For sanity check, intersection of survived and killed mutants must
	 * be empty.
	 */
	
	HashSet<JSONObject> survivedMutants  = new HashSet<JSONObject>(); 
	
	public void loadMutationResult(String file) throws ParseException, IOException{
		
		Path path = Paths.get(file);
	    try (Scanner scanner =  new Scanner(path)){
	      while (scanner.hasNextLine()){
	    	 String m = scanner.nextLine();
	    	 JSONObject muResult = (JSONObject)parser.parse(m);
	    	 mutationResult.add(muResult);

	    	 JSONObject mutant = (JSONObject) muResult.get("mutant");
	    	 mutants.add((JSONObject) muResult.get("mutant"));
	    	 JSONArray killing = (JSONArray) muResult.get("killing");
	    	 if (killing.size() != 0){
	    		 killedMutants.add(mutant);
	    		 ArrayList<String> ar = new ArrayList<String>();
	    		 for (int i = 0; i < killing.size(); i ++)
		    		 ar.add((String) killing.get(i));

	    		 this.muKillMap.put(mutant, ar);
	    	 }
	    	 else
	    		 survivedMutants.add(mutant);
	    	 
	    	  
	    	 JSONArray coveringTests = (JSONArray) muResult.get("coveredBy");
	    	 for (int i = 0; i < coveringTests.size(); i ++){
	    		 tests.add((String)coveringTests.get(i));
	    	 }
	    	 
	    	 
	      }      
	    }
	    killedMutants.retainAll(survivedMutants);
	    if (killedMutants.size() > 0){
	    	System.err.println("the intersection of killed and survived " +
	    			"mutants is not empty!" + killedMutants.toString());
	    	System.exit(1);
	    }
	} 

	public void runTechnique(SelectionMechanism sm){
		sm.setMutants(mutants);
		sm.setKilledMutants(killedMutants);
		HashSet<JSONObject> selectedMutants = sm.getSelectedMutants();
	    evaluateMutants(selectedMutants);		
	}

	private void evaluateMutants(Set<JSONObject> selectedMutants) {
		Util.setMutationTestMap(this.muKillMap);
		Util.correlataion(selectedMutants, this.mutants, this.tests, this.ts_size);
		
		for (int i = 0; i < this.ts_num; i ++)
			Util.evaluateEffectiveness(selectedMutants, this.mutants, this.tests);
		
	}


	public void run() throws IOException, ParseException{
		loadMutationResult(inFile);
		Util.setPrintStream(outFile);
		
		Util.setSeed(System.currentTimeMillis());
		
		SelectionMechanism sm = loadSelectionMechanisms(method);
		sm.setMutants(mutants);
		sm.setKilledMutants(killedMutants);
		
		for (int i = 0; i < this.n; i ++){
			Set selectedMutants = sm.getSelectedMutants();
			evaluateMutants(selectedMutants);
		}
	}
	
	private SelectionMechanism loadSelectionMechanisms(String method) {
		ClassLoader loader = Analyzer.class.getClassLoader();
		try {
			Class clazz = loader.loadClass(method);
			SelectionMechanism sm = (SelectionMechanism) clazz.newInstance();
			return sm;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		 Analyzer analyzer = new Analyzer();
		 CmdLineParser parser = new CmdLineParser(analyzer);
		 try {
		 parser.parseArgument(args);
		 analyzer.run();
		 } catch (CmdLineException e) {
		 // handling of wrong arguments
		 System.err.println(e.getMessage());
		 parser.printUsage(System.err);
		 System.exit(1);
		 } catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
