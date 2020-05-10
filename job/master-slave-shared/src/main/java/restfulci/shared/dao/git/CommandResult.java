package restfulci.shared.dao.git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandResult {

	private String command;
	
	private String normalOutput;
	private String errorOutput;
	
	private Integer exitCode;
	
	void setNormalOutput(Process process) throws IOException {
		normalOutput = inputStreamToString(process.getInputStream());
	}
	
	/*
	 * TODO:
	 * Not understand yet why `git` commands give `errorStream`
	 * even if the exit code is 0?
	 */
	void setErrorOutput(Process process) throws IOException {
		errorOutput = inputStreamToString(process.getErrorStream());
	}
	
	void setExitCode(Process process) throws InterruptedException {
		exitCode = process.waitFor();
	}
	
	private String inputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String output = "";
		String line;
		while ((line = reader.readLine()) != null) {
			output = output + line + "\n";
		}
		return output;
	}
}
