package ExecuteBat;

import java.io.IOException;
import java.util.*;

public class ProcessBuilderExample
{
  
  public static void main(String[] args) throws Exception
  {
    new ProcessBuilderExample("testAlias1", "testKP1");
  }

  // can run basic ls or ps commands
  // can run command pipelines
  // can run sudo command if you know the password is correct
  public ProcessBuilderExample(String newAlias, String newKeyPass) throws IOException, InterruptedException
  {
    // build the system command we want to run
    List<String> commands = new ArrayList<String>();
    commands.add("keytool.exe");
    commands.add("-keystore");
    commands.add("NEWclientkeystore.jks");
    commands.add("-genkey");
    commands.add("-alias");
    commands.add(newAlias);
    commands.add("-keypass");
    commands.add(newKeyPass);
    commands.add("-validity");
    commands.add("365");
    commands.add("-storepass");
    commands.add("NEWkeYs4clianTs");
    commands.add("-v");
    

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
}
