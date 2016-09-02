package uni.akilis.x2ai;

import java.net.URL;

import uni.akilis.helper.LoggerX;

/**
 * data ETL tool.
 * @author leo
 *
 */
public interface XETLer {

	/**
	 * prepare examples.
	 * @param items absolute path.
	 * @return the absolute path of the examples
	 */
	default String cook(String items){
		/*
		 * dummy for testing.
		 */
		URL url = Thread.currentThread().getContextClassLoader().getResource(XConstant.test_data_predict);
		if(url == null){
			LoggerX.println("prediction input file for testing does not exist!");
			return null;
		}
		return url.getPath();
	}
}
