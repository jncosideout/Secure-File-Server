package ExecuteBat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class KeytoolStreamHandler extends ThreadedStreamHandler {
	
	private BufferedReader bufferedReader = null;
	private String line = null;
	String[] inputVars = null;
	
	KeytoolStreamHandler(InputStream inputStream, OutputStream outputStream, String[] inputVars) {
		super(inputStream, outputStream, null);
		// TODO Auto-generated constructor stub
		this.inputVars = inputVars;
		
	}
	
	@Override // 
	 public void run()
	  {
	    
	    try
	    {
	      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	      
	      while ((line = bufferedReader.readLine()) != null)
	      {
	        outputBuffer.append(line + "\n");

		    if (line.equals("What is your first and last name?")) {
		    	sendToProcess(inputVars[0]);
		    }
		    if (line.contains("organizational unit?")) {
		    	sendToProcess(inputVars[1]);
		    }
		    if (line.contains("organization?")) {
		    	sendToProcess(inputVars[2]);
		    }
		    if (line.contains("City or Locality?")) {
		    	sendToProcess(inputVars[3]);
		    }
		    if (line.contains("State or Province?")) {
		    	sendToProcess(inputVars[4]);
		    }
		    if (line.contains("two-letter country code")) {
		    	sendToProcess(inputVars[5]);
		    }
		    if (line.contains("correct?")) {
		    	sendToProcess("yes");
		    }	
		    		
		   	 
	      }//end while
	    }
	    catch (IOException ioe)
	    {
	      // TODO handle this better
	      ioe.printStackTrace();
	    }
	    catch (Throwable t)
	    {
	      // TODO handle this better
	      t.printStackTrace();
	    }
	    finally
	    {
	      try
	      {
	        bufferedReader.close();
	      }
	      catch (IOException e)
	      {
	        // ignore this one
	      }
	    }
	  }
	
	private void sendToProcess(String input) throws IOException {
		//if ((line = bufferedReader.readLine()).equals("[Unknown]:")) {
    		doSleep(500);
		      printWriter.println(input);
		      printWriter.flush();
//    	} else if (line.equals("[no]:")) {
//    		doSleep(500);
//		      printWriter.println(input);
//		      printWriter.flush();
//    	}
	}

}//endclass
