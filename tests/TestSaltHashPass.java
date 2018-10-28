package tests;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import login.SaltHashPassW;

public class TestSaltHashPass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String userPass = "newClientB-65478password";
		
		SaltHashPassW shpw = new SaltHashPassW(userPass, 40000);
		String[] generatedSaltHashedPass = null;
		
			generatedSaltHashedPass = shpw.createNewHash();
			
			for (String gshp : generatedSaltHashedPass){
			System.out.println(gshp);
			}
		
		
	}

}
