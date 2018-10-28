package ExecuteBat;

import java.io.IOException;
import java.util.*;

public class ProcessBuilderExample
{
  
	// build the system command we want to run
    List<String> commands = new ArrayList<String>();
    private String fPrints = null;

	
  public static void main(String[] args) throws Exception
  {
	  Scanner userInput = new Scanner(System.in);
	  ProcessBuilderExample pbe = new ProcessBuilderExample("list", "testAlias1", "testKP1", 
			  								"NEWclientkeystore.jks", "NEWkeYs4clianTs", "testNEW1.csr", userInput);
    System.out.println("here are fPrints");
    System.out.println(pbe.getfPrints());
  }
 
  public ProcessBuilderExample(String keytoolCommand, String newAlias, String newKeyPass,
		  								String keystore, String storePass, String csrFile, Scanner userInput) throws IOException, InterruptedException
  {
    setCommands(keytoolCommand, newAlias, newKeyPass, keystore, storePass, csrFile);
   		
    // execute the command
    SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands, userInput);
    int result = commandExecutor.executeCommand();
    fPrints = commandExecutor.getfPrints();//in case keytool list was called for fingerprints
    // get the stdout and stderr from the command that was run
    StringBuilder stdout = null;
    if (keytoolCommand.equals("list")){stdout = commandExecutor.getListCertOutputFromCommand();//for fPrints
    } else { stdout = commandExecutor.getStandardOutputFromCommand();}
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
		    commands.add("-keyalg");
		    commands.add("RSA");
		    commands.add("-keysize");
		    commands.add("2048");
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
		    commands.add("–sigalg");         // cert req csr -keyalg in java 8
		    commands.add("rsa");             // cert req csr
		    commands.add("–file");           // cert req csr
		    commands.add(csrFile);  		 // cert req csr
		    commands.add("-keypass");
		    commands.add(myKeyPass);
		    break;
	  default:
	  }
	    commands.add("-alias");
	    commands.add(myAlias);
	    commands.add("-keystore");
	    commands.add(myKeystore);
	    commands.add("-storepass");
	    commands.add(myStorepass);
	    commands.add("-v");
  }
  
  public String getfPrints() {return fPrints;}

}//end class
