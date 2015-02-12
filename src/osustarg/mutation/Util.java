package osustarg.mutation;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.json.simple.JSONObject;




public class Util {

	public static Logger LOG = Logger.getLogger("MUTANALYSIS");
	private static HashMap<JSONObject, ArrayList<String>> muKillMap;
	public static PrintStream outCorr;
	public static PrintStream outEff;
	
	public static Random randomGen = new Random();
	
	public static void setSeed(long seed){
		randomGen.setSeed(seed);
	}
	
	
	public static void setPrintStream(String fileName){
		try {
			outCorr = new PrintStream("Corr." + fileName);
			outEff = new PrintStream("Eff." + fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	// TODO make report functions more flexible
	
	public static void reportCorr(String str){
		outCorr.println(str);
	}
	
	public static void reportEff(String str){
		outEff.println(str);
	}
	
	
	
	public static boolean hasIntersection(List<String> list1, List<String> list2){
		for(String str: list1)
			if (list2.contains(str))
				return true;
		return false;
		
	}
	

	public static boolean isDetected(JSONObject mutant, 
			List<String> testSuite){
		ArrayList<String> killList = muKillMap.get(mutant);
		if (killList != null && hasIntersection(killList, testSuite))
			return true;
		else
			return false;
	
	}
	
	
	public static int calculateMutationScore(List<String> testSuite, 
			Set<JSONObject> selectedMutants){
		int muScore = 0;
		
		for(JSONObject m: selectedMutants)
			if (isDetected(m, testSuite))
				muScore ++;

		return muScore;
	}
	
	
	
	/*
	 *  Correlation:
	 *   report the mutation score N randomly selected test suites
	 *   on selected mutants and the entire mutants
	 *   
	 */
	public static void correlataion(Set<JSONObject> selectedMutants,
			Set<JSONObject> allMutants,
			HashSet<String> tests,
			int ts_size) {
		List<Object> testsList = Arrays.asList(tests.toArray());
		Collections.shuffle(testsList);
		List<String> newTS = new ArrayList<String>();
		for(int index = 0; index < ts_size; index ++){
			newTS.add((String) testsList.get(index));
		}
		int muScoreSelected = Util.calculateMutationScore(newTS, selectedMutants);
		int muScoreAll = Util.calculateMutationScore(newTS, allMutants);
			
		reportCorr(muScoreSelected + "," + muScoreAll + "," + newTS.size());
		
		
	}

	/*
	 *  Test Effectiveness:
	 *  Given selected mutants:
	 *  1- find a M "random" testsuites that kill "almost"
	 *     all of mutants.
	 *  2- Get get mutation score of those test suites on
	 *     the entire test suite
	 *  
	 */
	public static void evaluateEffectiveness(Set<JSONObject> selectedMutants,
			HashSet<JSONObject> allMutants,
			HashSet<String> tests) {
		ArrayList<String> ts = new ArrayList<String>();
		for (JSONObject m : selectedMutants){
			if (!isDetected(m,ts)){
				List<String> killingTestCases = muKillMap.get(m);
				if (killingTestCases != null){
					int len = killingTestCases.size();
					int i = randomGen.nextInt(len);
					ts.add(killingTestCases.get(i));
				}
			}
		}
		
		int muScoreSelected = calculateMutationScore(ts, allMutants);
		int muScoreAll      = calculateMutationScore(toStringList(tests), allMutants);
		
		
		reportEff(muScoreSelected + "," + muScoreAll + "," + ts.size()	);
		
	}
	
	
	private static ArrayList<String> toStringList(Set<String> set){
		ArrayList<String> arList = new ArrayList<String>();
		for (String str: set){
			arList.add(str);
		}
		return arList;
	}




	public static void setMutationTestMap(
			HashMap<JSONObject, ArrayList<String>> muKillMap) {
		Util.muKillMap = muKillMap;
	}

}
