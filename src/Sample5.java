import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;

import osustarg.mutation.SelectionMechanism;



public class Sample5 implements SelectionMechanism{
	
	Set<JSONObject> mutants;

	
     
	public void setMutants(Set<JSONObject> mutants) {
		this.mutants = mutants;	
	}


	public void setKilledMutants(Set<JSONObject> mutants) {
		
	}

	@Override
	public HashSet<JSONObject> getSelectedMutants() {
		int size = this.mutants.size();
		int target = (size*5)/100;
		List mutationList = Arrays.asList(this.mutants.toArray());
		HashSet<JSONObject> selected = new HashSet<JSONObject>();
		Collections.shuffle(mutationList);
		
		for (int i = 0; i < target; i ++)
			selected.add((JSONObject) mutationList.get(i));
		
		return selected;
	}

}
