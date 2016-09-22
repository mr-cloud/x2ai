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
	 * @param outputDir recommendation results directory.
	 * @param webRoot
	 * @return the ranking results.
	 */
	String predict(String algoName, String examples, String outputDir, String webRoot);
	

	/**
	 * train the specific model with examples.
	 * @param algoName
	 * @param examples
	 * @param webRoot
	 * @return
	 */
	boolean train(String algoName, String examples, String webRoot);
	
	/**
	 * train all the supported models.
	 * @param examples
	 * @param webRoot
	 * @return
	 */
	boolean trainAll(String examples, String webRoot);
	
}
