
package ExecuteBat;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This class can be used to execute a system command from a Java application.
 * See the documentation for the public methods of this class for more
 * information.
 * 
 * Documentation for this class is available at this URL:
 * 
 * http://devdaily.com/java/java-processbuilder-process-system-exec
 *
 * 
 * Copyright 2010 alvin j. alexander, devdaily.com.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.

 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Please see the following page for the LGPL license:
 * http://www.gnu.org/licenses/lgpl.txt
 * 
 */
public class SystemCommandExecutor
{
  private List<String> commandInformation;
  private ThreadedStreamHandler inputStreamHandler;
  private ThreadedStreamHandler errorStreamHandler;
  private KeytoolListCertSH KLCStreamHandler = null; //to retrieve fingerprints

  private String[] inputVars = null;
 
  private String commonName = null;
  private String orgU = null;
  private String org = null;
  private String local = null;
  private String state = null;
  private String country = null;
  private boolean runGenKey = false;
  private boolean runCertReq = false;
  private boolean fingerprints = false;
  private String fPrints = null;
  

/**
   * Pass in the system command you want to run as a List of Strings, as shown here:
   * 
   * List<String> commands = new ArrayList<String>();
   * commands.add("/sbin/ping");
   * commands.add("-c");
   * commands.add("5");
   * commands.add("www.google.com");
   * SystemCommandExecutor commandExecutor = new SystemCommandExecutor(commands);
   * commandExecutor.executeCommand();
   * 
   * Note: I've removed the other constructor that was here to support executing
   *       the sudo command. I'll add that back in when I get the sudo command
   *       working to the point where it won't hang when the given password is
   *       wrong.
   *
   * @param commandInformation The command you want to run.
   */
  public SystemCommandExecutor(final List<String> commandInformation)
  {
    if (commandInformation==null) throw new NullPointerException("The commandInformation is required.");
    this.commandInformation = commandInformation;
    
    for (String com : commandInformation) {
    	if (com.contains("genkey")) {
    		runGenKey = true; break; 
    	} else if (com.contains("certreq")) {
    		runCertReq = true; break;
    	} else if (com.contains("list")) {
    		fingerprints = true;
    		break;
    	} 
    }
    if (runGenKey) {
  //setup for eventual -genkey questions
    	
  System.out.println("Enter common name");
	// commonName = userInput.nextLine();
	System.out.println("Enter name of your organizational unit");
	// orgU = userInput.nextLine();
	System.out.println("name of your organization");
	// org = userInput.nextLine();
	System.out.println("name of your City or Locality");
	// local = userInput.nextLine();
	System.out.println("name of your State or Province");
	// state = userInput.nextLine();
	System.out.println("Enter two-letter country code");
	//country = userInput.nextLine();
  
    commonName = "ClientA";
	orgU = "testOrgU";
	org = "testOrg";
	local = "testLocality";
	state = "testState";
	country = "TC";
	
    inputVars = new String[] { commonName,	orgU, org, local, state, country}; 
    }
  }

  public int executeCommand()
  throws IOException, InterruptedException
  {
    int exitValue = -99;
    
    try
    {
      ProcessBuilder pb = new ProcessBuilder(commandInformation);
      Map<String, String> env = pb.environment();
      if (runGenKey || runCertReq || fingerprints) { //shortcut for elevated privileges keytool
          env.put("PATH", "C:\\Users\\Alex\\Desktop\\keytool.exe - Shortcut.lnk"); 
      } else {
        env.put("PATH", "C:\\Program Files\\Java\\jre1.8.0_144\\bin\\keytool.exe"); }
//      env.remove("OTHERVAR");
//      env.put("VAR2", env.get("VAR1") + "suffix");
      File dir = null;//the working directory for the process1
      if (runGenKey || runCertReq || fingerprints) { //TODO change this
        dir = new File("C:\\Program Files\\Java\\jre1.8.0_144\\bin"); //directory for main client server keystores
      } else {  dir = new File("C:\\Users\\Alex\\Desktop");}//directory for NEWclientkeystore TEST file
      pb.directory(dir);
      Process process = pb.start();

      // you need this if you're going to write something to the command's input stream
      // (such as when invoking the 'sudo' command, and it prompts you for a password).
      OutputStream stdOutput = process.getOutputStream();
      
      // i'm currently doing these on a separate line here in case i need to set them to null
      // to get the threads to stop.
      // see http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
      InputStream inputStream = process.getInputStream();
      InputStream errorStream = process.getErrorStream();
      // these need to run as java threads to get the standard output and error from the command.
      // the inputstream handler gets a reference to our stdOutput in case we need to write
      // something to it, such as with the sudo command
      
      if (runGenKey) {
	      inputStreamHandler = new KeytoolStreamHandler(errorStream, stdOutput, inputVars);
	      errorStreamHandler = new ThreadedStreamHandler(inputStream); //switch inputStream with errorStream to make keytool -genkey work 
      } else if (fingerprints) {
    	  KLCStreamHandler = new KeytoolListCertSH(inputStream);
	      errorStreamHandler = new ThreadedStreamHandler(errorStream);  
      } else {
	      inputStreamHandler = new ThreadedStreamHandler(inputStream);
	      errorStreamHandler = new ThreadedStreamHandler(errorStream); } //switch inputStream with errorStream to make keytool -genkey work 

      // TODO the inputStreamHandler has a nasty side-effect of hanging if the given password is wrong; fix it
      if (fingerprints){ KLCStreamHandler.start();
      } else { inputStreamHandler.start();}
      
      errorStreamHandler.start();

      // TODO a better way to do this?
      exitValue = process.waitFor();
      
      if (fingerprints){
    	  fPrints = KLCStreamHandler.getFingerprints();
      }
      
      // TODO a better way to do this?
      if (fingerprints){ KLCStreamHandler.interrupt();
      } else { inputStreamHandler.interrupt();}
      errorStreamHandler.interrupt();
      if (fingerprints){ KLCStreamHandler.join();
      } else {inputStreamHandler.join();}
      errorStreamHandler.join();
    }
    catch (IOException e)
    {
      // TODO deal with this here, or just throw it?
      throw e;
    }
    catch (InterruptedException e)
    {
      // generated by process.waitFor() call
      // TODO deal with this here, or just throw it?
      throw e;
    }
   
      return exitValue;
    
  }

  /**
   * Get the standard output (stdout) from the command you just exec'd.
   */
  public StringBuilder getStandardOutputFromCommand()
  {
	  
    return inputStreamHandler.getOutputBuffer();
  }
  
  public StringBuilder getListCertOutputFromCommand()
  {
	  
    return KLCStreamHandler.getOutputBuffer();
  }

  /**
   * Get the standard error (stderr) from the command you just exec'd.
   */
  public StringBuilder getStandardErrorFromCommand()
  {
    return errorStreamHandler.getOutputBuffer();
  }

  public String getfPrints() {return fPrints;}

}//eoc