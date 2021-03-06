package uni.akilis.x2ai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uni.akilis.helper.InterProcessCaller;
import uni.akilis.helper.LoggerX;

public class Wolverine implements XMan{
	public static final String LOG_TAG = Wolverine.class.getName();

	
	@Override
	public String predict(String algoName, String examples, String outputDir, String webRoot) {
		// TODO Auto-generated method stub
		InterProcessCaller caller = new InterProcessCallerImpl();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File engine = new File(classLoader.getResource(XConstant.XMAN_ENGINE).getPath());
		if(!engine.exists()){
			LoggerX.println(LOG_TAG, "cannot find engine: " + XConstant.XMAN_ENGINE);
			return null;
		}
		File modelMementoDir = new File(webRoot + XConstant.model_memento_dir);
		if(!modelMementoDir.exists()){
			if(!modelMementoDir.mkdirs()){
				LoggerX.println(LOG_TAG, "cannot mkdir for model memento!");
				return null;
			}
		}
		List<String> cmd = caller.makeCommand(engine.getAbsolutePath(), XConstant.ACTION_PREDICT, algoName, examples, outputDir, modelMementoDir.getAbsolutePath() + "/");
		try {
			caller.processCall(cmd, XConstant.ACTION_PREDICT);
			String basename = examples.substring(examples.lastIndexOf("/") + 1);
			File rankingsFile = new File(outputDir  + basename);
			if(!rankingsFile.exists()){
				LoggerX.println(LOG_TAG, "rankings file does not exist: " + rankingsFile.getAbsolutePath());
				return null;
			}
			return rankingsFile.getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}

	// process caller implement internal class.
	class InterProcessCallerImpl extends InterProcessCaller{
		
		@Override
		public List<String> makeCommand(String... args) {
			// TODO Auto-generated method stub
			// get the path of python file.
			List<String> cmds = new ArrayList<>();
			cmds.add(XConstant.PYTHON_ENV);
			for(String arg: args){
				cmds.add(arg);
			}
			return cmds;
		}
		
	}

	@Override
	public boolean train(String algoName, String examples, String webRoot) {
		// TODO Auto-generated method stub
		InterProcessCaller caller = new InterProcessCallerImpl();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File engine = new File(classLoader.getResource(XConstant.XMAN_ENGINE).getPath());
		if(!engine.exists()){
			LoggerX.println(LOG_TAG, "cannot find engine: " + XConstant.XMAN_ENGINE);
			return false;
		}
		File modelMementoDir = new File(webRoot + XConstant.model_memento_dir);
		if(!modelMementoDir.exists()){
			if(!modelMementoDir.mkdirs()){
				LoggerX.println(LOG_TAG, "cannot mkdir for model memento!");
				return false;
			}
		}
		List<String> cmd = caller.makeCommand(engine.getAbsolutePath(), XConstant.ACTION_TRAIN, algoName, examples, modelMementoDir.getAbsolutePath() + "/", webRoot);
		try {
			caller.processCall(cmd, XConstant.ACTION_TRAIN);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean trainAll(String examples, String webRoot) {
		// TODO Auto-generated method stub
		InterProcessCaller caller = new InterProcessCallerImpl();
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		File engine = new File(classLoader.getResource(XConstant.XMAN_ENGINE).getPath());
		if(!engine.exists()){
			LoggerX.println(LOG_TAG, "cannot find engine: " + XConstant.XMAN_ENGINE);
			return false;
		}
		File modelMementoDir = new File(webRoot + XConstant.model_memento_dir);
		if(!modelMementoDir.exists()){
			if(!modelMementoDir.mkdirs()){
				LoggerX.println(LOG_TAG, "cannot mkdir for model memento!");
				return false;
			}
		}
		List<String> cmd = caller.makeCommand(engine.getAbsolutePath(), XConstant.ACTION_TRAIN_ALL, examples, modelMementoDir.getAbsolutePath() + "/", webRoot);
		try {
			caller.processCall(cmd, XConstant.ACTION_TRAIN_ALL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
