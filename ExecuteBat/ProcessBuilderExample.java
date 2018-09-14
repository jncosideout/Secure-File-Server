package ExecuteBat;

import java.io.IOException;
import java.util.*;

public class ProcessBuilderExample
{
  
	// build the system command we want to run
    List<String> commands = new ArrayList<String>();
	
  public static void main(String[] args) throws Exception
  {
    new ProcessBuilderExample("testAlias1", "testKP1");
  }
 
  public ProcessBuilderExample(String newAlias, String newKeyPass) throws IOException, InterruptedException
  {
    setCommands("list", newAlias, newKeyPass, "NEWclientkeystore.jks", "NEWkeYs4clianTs", "testNEW1.csr");
   		
    // execute the command
    SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
    int result = commandExecutor.executeCommand();

    // get the stdout and stderr from the command that was run
    StringBuilder stdout = commandExecutor.getStandardOutputFromCommand();
    StringBuilder stderr = commandExecutor.getStandardErrorFromCommand();
    
    // print the stdout and stderr
    System.out.println("The numeric result of the command was: " + result);
    System.out.println("STDOUT:");
    System.out.println(stdout);
    System.out.println("STDERR:");
    System.out.println(stderr);
  }
  
  void setCommands(String useCase, String myAlias, String myKeyPass,
		  String myKeystore, String myStorepass, String csrFile) {
	  
	    commands.add("keytool.exe");
	    
	  switch (useCase) {
	  case "genkey": 
		    commands.add("-genkey");  //key generation
		    commands.add("-alias");
		    commands.add(myAlias);
		    commands.add("-keypass");
		    commands.add(myKeyPass);
		    commands.add("-validity");  //key generation
		    commands.add("365");		  //key generation
		    break;
	  case "list":
		    commands.add("-list");
		    break;
	  case "certreq":	  
		    commands.add("–certreq");        // cert req csr
		    commands.add("-alias");
		    commands.add(myAlias);
		    commands.add("–sigalg");         // cert req csr -keyalg in java 8
		    commands.add("rsa");             // cert req csr
		    commands.add("–file");           // cert req csr
		    commands.add(csrFile);  		 // cert req csr
		    commands.add("-keypass");
		    commands.add(myKeyPass);
		    break;
	  default:
	  }
	  
	    commands.add("-keystore");
	    commands.add(myKeystore);
	    commands.add("-storepass");
	    commands.add(myStorepass);
	    commands.add("-v");
  }
}//end class
