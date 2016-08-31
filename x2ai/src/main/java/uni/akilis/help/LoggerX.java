package uni.akilis.help;
/**
 * 记日志、调试的专用logger
 * @author Akilis
 *
 */
public class LoggerX {
	private static final boolean LOG_ON = true;
	private static final boolean LOG_OFF = false;
	private static boolean LOG_STATUS = LOG_ON;
//	private static boolean LOG_STATUS = LOG_OFF;
	
	/**
	 * 输出日志到控制台
	 * @param msg 想要输出的日志信息
	 */
	public static void print(Object msg){
		if(LOG_STATUS){
			System.out.print(msg);
		}
		else
			;
	}
	/**
	 * 输出日志到控制台
	 * @param tag 调用日志的标签，用以标记调用者信息
	 * @param msg 想要输出的日志信息
	 */
	public static void print(String tag, Object msg){
		print(tag + ": ");
		print(msg);
	}
	/**
	 * 输出日志到控制台，并换行
	 * @param msg 想要输出的日志信息
	 */
	public static void println(Object msg){
		print(msg + "\n");
	}
	/**
	 * 输出日志到控制台，并换行
	 * @param tag 调用日志的标签，用以标记调用者信息
	 * @param msg 想要输出的日志信息
	 */
	public static void println(String tag, Object msg){
		print(tag, msg);
		print("\n");
	}
	
	/**
	 * 输出错误日志到控制台并换行
	 * @param msg 想要输出的日志信息
	 */
	public static void error(Object msg){
		System.err.println(msg);
	}
	/**
	 * 输出错误日志到控制台
	 * @param tag 调用日志的标签，用以标记调用者信息
	 * @param msg 想要输出的日志信息
	 */
	public static void error(String tag, Object msg){
		error(tag + ": " + msg);
		error(msg);
	}
	public static void println() {
		// TODO Auto-generated method stub
		print("\n");
	}
	

}
