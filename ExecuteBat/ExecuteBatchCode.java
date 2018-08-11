package ExecuteBat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ExecuteBatchCode {

	public static void main(String[] args) {
		
		ExecuteBatchCode exec = new ExecuteBatchCode();
		
		String output = exec.executeCommand();
		
		System.out.println(output);
		
	}
	
	private String executeCommand() {
		
		Process p;
		StringBuffer output = new StringBuffer();
		
		String command  = null;
		Scanner readBatFile = openFile("myBat.bat");
		readBatFile.useDelimiter("\\n");
		
		while ((command = readBatFile.next()) != null) {

			try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = 
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";			
			while ((line = reader.readLine())!= null) {
				output.append(line + "\n");
			}
			} catch (IOException io) {
				io.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		readBatFile.close();
		
		return output.toString();
	}
	
	public static Scanner openFile(String fileName)
    {
    	Scanner scr = null;
    	
    	 try {
             // Attempt to open the file
    		 scr = new Scanner(new FileInputStream(fileName));
           
         } catch (FileNotFoundException e) {
             // If the file could not be found, this code is executed
             // and then the program exits.
             System.out.println("File not found.");
             System.exit(0);
         }
    	  return scr;
    }
	
	
}//end class
