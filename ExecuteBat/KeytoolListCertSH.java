package ExecuteBat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class KeytoolListCertSH extends ThreadedStreamHandler {

	private String fingerprints = null;
	
	KeytoolListCertSH(InputStream inputStream) {
		super(inputStream);
		// TODO Auto-generated constructor stub
		
	}
	
	public void run()
	  {
	   

	    BufferedReader bufferedReader = null;
	    try
	    {
	      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	      String line = null;
	      while ((line = bufferedReader.readLine()) != null)
	      {
	        outputBuffer.append(line + "\n");
	        if (line.contains("SHA256:")){
	        	fingerprints = line;
	        	fingerprints = fingerprints.substring(10);
	        	break;
	        	}
	      }
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
	
	public String getFingerprints() {return fingerprints;}
}
