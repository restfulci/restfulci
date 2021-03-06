package restfulci.job.shared.dao.git;

import java.io.File;
import java.io.IOException;
import java.util.List;

abstract class Executable {
	
	public abstract CommandResult execute() throws IOException, InterruptedException;

	static protected CommandResult executeWith(List<String> commandArray, File directory) throws IOException, InterruptedException {
		
		CommandResult commandResult = new CommandResult();
		commandResult.setCommand(String.join(" ", commandArray));
		
		/*
		 * TODO:
		 * May replace ProcessBuilder to either of the followings
		 * http://commons.apache.org/proper/commons-cli/ (seems much more popular from maven)
		 * https://github.com/zeroturnaround/zt-exec
		 */
		ProcessBuilder builder = new ProcessBuilder(commandArray);
		builder.directory(directory);
//		builder.redirectErrorStream(true);
		Process process = builder.start();
		
		commandResult.setNormalOutput(process);
		commandResult.setErrorOutput(process);
		commandResult.setExitCode(process);
		
//		System.out.println("$ "+commandResult.getCommand());
//		System.out.println(commandResult.getNormalOutput());
//		System.out.println(commandResult.getErrorOutput());
//		System.out.println(commandResult.getExitCode());
		
		return commandResult;
	}
}
