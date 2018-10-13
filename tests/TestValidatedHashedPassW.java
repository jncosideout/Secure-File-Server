package tests;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import login.ValidateHashedPassW;

public class TestValidatedHashedPassW {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String givenPass1 = "apple3456";
		String givenPass2 = "password1";
		
		System.out.println("input salt and hash to check as \'#iterations\':\'salt\':\'hash\'");
		Scanner in = new Scanner(System.in);
		String storedSaltAndHash = in.nextLine();
		
		String[] parts = storedSaltAndHash.split(":");
		int iterations = Integer.parseInt(parts[0]);
		String salt = parts[1];
		String hash = parts[2];
		
		ValidateHashedPassW vhpw = new ValidateHashedPassW(givenPass1, hash, "given cert fingrints",
				"given cert fingerprints", iterations, salt);
		
		boolean matched = false;
		boolean cert_match = false;
		try {
			matched = vhpw.validate();
			cert_match = vhpw.compareFingerPrints();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (matched) {
			System.out.println(givenPass1 + " is correct");		
		} else {
			System.out.println("something went wrong");
		}
		if (cert_match) {
			System.out.println("cert is correct");		
		} else {
			System.out.println("cert match went wrong");
		}
		
		ValidateHashedPassW vhpw2 = new ValidateHashedPassW(givenPass2, hash, "given cert fingerprints",
																"stored cert fingerprints", iterations, salt);

		try {
			matched = vhpw2.validate();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (matched) {
			System.out.println(givenPass2 + " is valid, which shouldn't be the case.");
		} else {
			System.out.println(givenPass2 + " was not validated, as expected.");
		}
		
		in.close();
	}//end class

}
