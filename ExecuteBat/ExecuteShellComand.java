package ExecuteBat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class ExecuteShellComand {

	public static void main(String[] args) {

		ExecuteShellComand obj = new ExecuteShellComand();

		
		//in mac oxs
		//String command = "ping -c 3 " + domainName;
		
		//in windows
		String[] command = new String[]{"keytool.exe",
				"-list", "-keystore", "serverkeystore.jks", "-storepass",
				"serVerstoRepasS", "-v"};
		
		String output = obj.executeCommand(command);

		System.out.println(output);

	}

	private String executeCommand(String[] command) {

		StringBuffer output = new StringBuffer();
		StringBuffer errorOut = new StringBuffer();

		Process p;
		
		File dir = new File("C:\\Program Files\\Java\\jre1.8.0_144\\bin");
		try {
			p = Runtime.getRuntime().exec(command, null, dir);
			p.waitFor();
			BufferedReader fromP = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader errP =
							new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
            String procInput = "";			
			while ((procInput = fromP.readLine())!= null) {
				output.append(procInput + "\n");
			}
			
			String errInput = "";			
			while ((errInput = errP.readLine())!= null) {
				errorOut.append(errInput + "\n");
			}
			if (errorOut.length() > 0) {
				System.err.println(errorOut.toString());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

}