package uni.akilis.helper;

import java.io.BufferedReader;
import java.io.IOException;

import uni.akilis.helper.LoggerX;
/**
 * 读取Java进程与shell进程之间的管道流
 * @author leo
 *
 */
public class InterProcessStreamReader implements Runnable{
	public static final String SHELL_OUTPUT_STREAM = "shellOutputReader";
	public static final String SHELL_ERROR_STREAM = "shellErrorReader";
	/**
	 * 进程间的管道流
	 */
	private BufferedReader br;
	/**
	 * 区分流的类型
	 */
	private String LOG_TAG;
	
	public InterProcessStreamReader(){
		
	}
	public InterProcessStreamReader(BufferedReader _br, String tag){
		this.br = _br;
		this.LOG_TAG = tag;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String msg;
		try {
			while((msg=br.readLine()) != null){
				switch(this.LOG_TAG){
				case SHELL_OUTPUT_STREAM:
					LoggerX.println(LOG_TAG, msg);
					break;
				case SHELL_ERROR_STREAM:
					LoggerX.error(LOG_TAG, msg);
					break;
				default:;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
