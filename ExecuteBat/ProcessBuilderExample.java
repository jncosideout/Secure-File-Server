package ExecuteBat;

import java.io.IOException;
import java.util.*;

public class ProcessBuilderExample
{
  
	// build the system command we want to run
    List<String> commands = new ArrayList<String>();
	
  public static void main(String[] args) throws Exception
  {
    new ProcessBuilderExample("testAlias3", "testKP3");
  }

  // can run basic ls or ps commands
  // can run command pipelines
  // can run sudo command if you know the password is correct
  public ProcessBuilderExample(String newAlias, String newKeyPass) throws IOException, InterruptedException
  {
    setCommands("genkey", newAlias, newKeyPass, "NEWclientkeystore.jks", "NEWkeYs4clianTs", null);
    //if using genkey, switch inputStream with errorStream in ThreadHandlers
//    commands.add("keytool.exe");
//   //-keystore *name* here for -genkey? don't think it matters
//    commands.add("–certreq");        // cert req csr
//    commands.add("-genkey");  //key generation
//    commands.add("-alias");
//    commands.add(newAlias);
////    commands.add("–keyalg");         // cert req csr -keyalg in java 8
////    commands.add("rsa");             // cert req csr
////    commands.add("–file");           // cert req csr
////    commands.add("NEWclient.csr");   // cert req csr
//    commands.add("-keypass");
//    commands.add(newKeyPass);
//    commands.add("-keystore");
//    commands.add("NEWclientkeystore.jks");
//    commands.add("-storepass");
//    commands.add("NEWkeYs4clianTs");
//    commands.add("-validity");  //key generation
//    commands.add("365");		  //key generation
//     commands.add("-v");			
    
//keytool list
    
//    commands.add("-keystore");
//    commands.add("clientkeystore.jks");
//    commands.add("-list");
//    commands.add("-storepass");
//    commands.add("keYs4clianTs");
//    commands.add("-v");
 
 
  
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
	    commands.add("-keystore");
	    commands.add(myKeystore);
	    commands.add("-storepass");
	    commands.add(myStorepass);
	    commands.add("-v");
	  
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
		    commands.add("–keyalg");         // cert req csr -keyalg in java 8
		    commands.add("rsa");             // cert req csr
		    commands.add("–file");           // cert req csr
		    commands.add(csrFile);  		 // cert req csr
		    commands.add("-keypass");
		    commands.add(myKeyPass);
		    break;
	  default:
	  }
  }
}//end class
