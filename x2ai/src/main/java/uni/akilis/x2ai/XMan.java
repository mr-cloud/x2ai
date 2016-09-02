package uni.akilis.x2ai;
/**
 * recommender engine.
 * @author leo
 *
 */
public interface XMan {

	/**
	 * predict the scores for input items.
	 * @param algoName
	 * @param examples
	 * @param outputDir
	 * @param webRoot
	 * @return the ranking results.
	 */
	String predict(String algoName, String examples, String outputDir, String webRoot);
	

	
}
