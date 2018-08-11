package tests;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import login.SaltHashPassW;

public class TestSaltHashPass {

	public String testHash() {
		// TODO Auto-generated method stub
		String userPass = "hellokitty";
		
		SaltHashPassW shpw = new SaltHashPassW(userPass, 40000);
		String generatedSaltHashedPass = null;
		try {
			generatedSaltHashedPass = shpw.generatePasswordHash(64, "PBKDF2WithHmacSHA256");
			
			
			System.out.println(generatedSaltHashedPass);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return generatedSaltHashedPass;
		
	}

}
