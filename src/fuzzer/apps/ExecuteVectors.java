package fuzzer.apps;

import java.util.ArrayList;
import java.util.List;

import fuzzer.apps.VVector.VVector;

public class ExecuteVectors {
	private List<VVector> mVectors;
	public ExecuteVectors(List<VVector> aVectors){
		mVectors = new ArrayList<VVector>();
	}
	
	public void execute()
	{
		List<Boolean> results = new ArrayList<Boolean>();
		for (VVector vector : mVectors)
		{
			results.add(vector.test());
		}
	}
}
