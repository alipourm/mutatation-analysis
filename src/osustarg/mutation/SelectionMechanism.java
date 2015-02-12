package osustarg.mutation;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONObject;


public interface SelectionMechanism {
	public void setMutants(Set<JSONObject> mutants);
	public void setKilledMutants(Set<JSONObject> mutants);
	public HashSet<JSONObject> getSelectedMutants();

}
