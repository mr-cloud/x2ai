package uni.akilis.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

/**
 * tool for inter-process call.
 * @author leo
 *
 */
public abstract class InterProcessCaller {
	// you should override this property.
	public static final String LOG_TAG = "InterProcessCaller";
	/**
	 * 
	 * @param cmd 命令
	 * @param string 进程名称
	 * @throws IOException 
	 */
	public void processCall(List<String> cmd, String processName) throws IOException {
		// TODO Auto-generated method stub
		if(cmd == null){
			LoggerX.error(LOG_TAG, "args doesnot match the needed!\ntask terminated.");
			return;
		}
		//record this command
		File recorder = new File("interprocess-recorder.txt");
		BufferedWriter bfw = new BufferedWriter(new FileWriter(recorder, true));
		String startTime = new Date().toString();
		bfw.write("<job>\n<start>"
				+ startTime
				+ "</start>\n");
		String command = new String("<cmd>");
		for (String arg : cmd) {
			command = command + arg + " ";
		}
		command = command + "</cmd>";
		LoggerX.println(LOG_TAG, command);
		bfw.write(command + "\n</job>");
		bfw.newLine();
		bfw.flush();
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process extractProcess;
		try {
        	LoggerX.println("start a process for " + processName + "...");
        	TimeConsume consumer = new TimeConsume();
            extractProcess = pb.start();
            consumer.resetBeginTime();
            //开启线程读取shell进程流
            InterProcessStreamReader shellOutputReader = new InterProcessStreamReader(new BufferedReader(new InputStreamReader(extractProcess.getInputStream())), InterProcessStreamReader.SHELL_OUTPUT_STREAM);
            InterProcessStreamReader shellErrorReader = new InterProcessStreamReader(new BufferedReader(new InputStreamReader(extractProcess.getErrorStream())), InterProcessStreamReader.SHELL_ERROR_STREAM);
            Thread t1 = new Thread(shellOutputReader);
            Thread t2 = new Thread(shellErrorReader);
            t1.start();
            t2.start();
             try{
				waitForProcessBeforeContinueCurrentThread(extractProcess);
				LoggerX.println(LOG_TAG, "process returned.");
                requireSuccessfulExitStatus(extractProcess);
                LoggerX.println(LOG_TAG, "task finished.");
                //write the cost time for this proocess
                long milis = consumer.getTimeConsume();
                long hours = milis/(1000 * 3600);
                long mins = (milis - hours * 1000 * 3600)/(1000 * 60);
                long secs = (milis - hours * 1000 * 3600 - mins * 1000 * 60)/1000;
        		bfw.write("<job>\n<start>"
        				+ startTime
        				+ "</start>\n");
                String cost = "<cost>" + hours + ":" + mins + ":" + secs + "</cost>";
                bfw.write(cost);
                bfw.newLine();
                bfw.write("<job>\n");
                bfw.flush();
                LoggerX.println(LOG_TAG, cost);
            }
            catch (Exception ex) {
        		bfw.write("<job>\n<start>"
        				+ startTime
        				+ "</start>\n");
            	bfw.write("<cost></cost>");
            	bfw.newLine();
                bfw.write("<job>\n");
                bfw.flush();
                throw new RuntimeException("failed to execute " + processName + ".");
            }
            finally {
                extractProcess.destroy();
                bfw.close();
            }
		}
		catch (IOException ex) {
    		bfw.write("<job>\n<start>"
    				+ startTime
    				+ "</start>\n");
        	bfw.write("<cost></cost>");
        	bfw.newLine();
            bfw.write("<job>\n");
            bfw.flush();
        	bfw.close();
            throw new RuntimeException("failed to start " + processName + "!");
        }
	}
    private void waitForProcessBeforeContinueCurrentThread(Process process) {
        try {
        	//process.waitFor(30L, TimeUnit.SECONDS);//since 1.8
        	process.waitFor();
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void requireSuccessfulExitStatus(Process process) {
        if (process.exitValue() != 0) {
            throw new RuntimeException("data sampling failed");
        }
    }

    /**
     * make process call.
     * @param args
     * @return
     */
    public abstract List<String> makeCommand(String... args);
}
