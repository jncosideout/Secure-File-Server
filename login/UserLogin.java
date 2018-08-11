package login;

public class UserLogin {

	private String userName;
	private String email;
	private String givenPassword;
	
	public UserLogin(String userName, String email, String givenPassword) {
		this.userName = userName;
		this.email = email;
		this.givenPassword = givenPassword;
	}
	
	public void newUser() {
		
	}
	
	public boolean returningUser(String input, boolean isUserNameOfEmail) {
		return false;
	}
	
	public String createCertificate() {
		return new String();
	}
}
