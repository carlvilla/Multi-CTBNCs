package main.java.com.cig.mctbnc.learning.structure;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import main.java.com.cig.mctbnc.learning.parameters.CPTNode;
import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.Node;
import main.java.com.cig.mctbnc.models.PGM;
import main.java.com.cig.mctbnc.models.State;

public class StructureScoreFunctions {
	
	/**
	public static double logLikelihoodScore(PGM model, int nodeIndex,
			Dataset dataset,
			ParameterLearning parameterLearningAlg,
			boolean dimensionPenalty) throws RuntimeException {
		

		CTDiscreteNode node = model.getNode(nodeIndex);
		if( node.isStaticNode())
			throw new IllegalArgumentException("Error: this scoring function is not defined for static nodes");
		
		double llScore = 0.0;
		double mxx = (Double) paramsLearningAlg.getParameter("Mxx_prior");
		double tx = (Double) paramsLearningAlg.getParameter("Tx_prior");
		
		SufficientStatistics[] ss = paramsLearningAlg.learn(model, dataset).getSufficientStatistics();
		
		// Marginal value calculation
		double mx = mxx*(node.getStatesNumber() - 1);
		for(int fsE = 0; fsE < node.getStatesNumber(); ++fsE) {
			for(int pE = 0; pE < node.getNumberParentsEntries(); ++pE) {
				
				// Calculate MargLq (q-value)
				llScore += Gamma.logGamma( ss[nodeIndex].Mx[pE][fsE] + 1);
				llScore += (mx + 1) * Math.log( tx);
				llScore -= Gamma.logGamma( mx + 1);
				llScore -= (ss[nodeIndex].Mx[pE][fsE] + 1) * Math.log( ss[nodeIndex].Tx[pE][fsE]);
				
				// Calculate MargLth (theta)
				llScore += Gamma.logGamma( mx);
				llScore -= Gamma.logGamma( ss[nodeIndex].Mx[pE][fsE]);
				for(int ssE = 0; ssE < node.getStatesNumber(); ++ssE) {
					if( fsE == ssE)
						continue;
					
					llScore += Gamma.logGamma( ss[nodeIndex].Mxx[pE][fsE][ssE]);
					llScore -= Gamma.logGamma( mxx);
				}
			}
		}
		
		// llScore = llScore - ln|X|*Dim[X]/2
		if( dimensionPenalty) {
			double dimX = (node.getStatesNumber() - 1) * node.getStatesNumber() * node.getNumberParentsEntries();
			llScore -= Math.log( dataset.size()) * dimX / 2;
		}
		
		return llScore;
		**/
	
	public static double BNlogLikelihoodScore(List<CPTNode> nodes, Dataset dataset) {
		
		double llScore = 0.0;
		
		for(CPTNode node:nodes) {
			Map<State, Double> CPT = node.getCPT();
			Map<State, Integer> N = node.getSufficientStatistics();
			
			// Obtain an array with the values that the studied variable can take
			String[] possibleValuesStudiedNode = node.getPossibleStates().stream()
					.map(stateAux -> stateAux.getValueNode(node.getName()))
					.toArray(String[]::new);
			
			// All the possible states between the studied variable and its parents
			Set<State> states = N.keySet();
			
			for (State state : states) {
				// Number of times the studied variable and its parents take a certain value
				int Nijk = N.get(state);

				// Number of times the parents of the studied variable have a certain
				// state independently of the studied variable
				for (String k : possibleValuesStudiedNode) {
					State query = new State(state.getEvents());
					query.modifyEventValue(node.getName(), k);
					
					llScore += Nijk * Math.log(CPT.get(query)); // sum(wi (ri - 1))
				}
			}

		}
		return llScore;
	}

}
