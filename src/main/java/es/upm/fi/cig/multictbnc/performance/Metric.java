package es.upm.fi.cig.multictbnc.performance;

import java.util.Map;

/**
 * Interface used to be able to pass evaluation metrics as parameters of other
 * methods. This can be used, for example, to create a macro-average method
 * which receives metrics such as precision or recall.
 * 
 * @author Carlos Villa Blanco
 *
 */
public interface Metric {

	/**
	 * Computes the value of the evaluation metric given a Map containing a
	 * confusion matrix. The Map should contain four keys "tp" (true positive), "fp"
	 * (false positive), "tn" (true negative) and "fn" (false negative).
	 * 
	 * @param confusionMatrix a {@code Map} containing a confusion matrix
	 * @return result of the evaluation metric.
	 */
	public double compute(Map<String, Integer> confusionMatrix);
}
