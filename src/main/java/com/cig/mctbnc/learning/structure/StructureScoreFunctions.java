package main.java.com.cig.mctbnc.learning.structure;

import java.util.Collection;

import org.apache.commons.math3.special.Gamma;

import CTBNCToolkit.CTDiscreteNode;
import CTBNCToolkit.ICTClassifier;
import CTBNCToolkit.ILearningAlgorithm;
import CTBNCToolkit.ITrajectory;
import CTBNCToolkit.SufficientStatistics;


import main.java.com.cig.mctbnc.models.Dataset;
import main.java.com.cig.mctbnc.models.PGM;

public class StructureScoreFunctions {
	
	public static double logLikelihoodScore(PGM model, int nodeIndex,
			Dataset dataset,
			ParameterLearningAlg  parameterLearningAlg,
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

}
