package myClient;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
/*The first window of the client program. First a dialog is displayed asking 
 * if you are a new or returning user. Then the main LoginWindow is displayed.
 * Here the client enters all user credentials for logging in. The certificate
 * alias MUST be one which is already installed in a keystore that this program 
 * knows about. Otherwise the TLS handshake won't complete.
 * Lastly, a second dialog is displayed asking the user in advance to consent to 
 * initiating the Diffie-Hellman key exchange with the next client who logs in,
 * or else he is that second client and consents to responding to the first client.
 */
public class LoginWindow extends JFrame {
    private final JTextField userNameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField aliasField = new JTextField(20);
    private final JPasswordField keyPassField = new JPasswordField(20);
    private final JTextField emailField = new JTextField(20);
    private final JButton enter = new JButton("enter");
    int new_return;
    
    public LoginWindow (String host, int portNumber){
    	String[] options = new String[2];
    	options[0] = "New";
    	options[1] = "Returning";
   	    new_return = JOptionPane.showOptionDialog(getParent(), "New or Returning User?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
   	    
    	JFrame.setDefaultLookAndFeelDecorated(true);
    	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    	setTitle("Enter username and password");
    	//setPreferredSize(new Dimension(300, 100));
    	setLayout(new BorderLayout());
    	JPanel panel = new JPanel();
    	JLabel un = new JLabel("Enter User Name:");
    	JLabel em = new JLabel("Enter User Email:");
    	JLabel pw = new JLabel("Enter User Password:");
    	JLabel alias = new JLabel("Enter certificate alias:");
    	JLabel keyPass = new JLabel("Enter Password for alias:");
    	passwordField.setEchoChar('*');
    	keyPassField.setEchoChar('*');
    	panel.setLayout(new GridLayout(5,2));
    	panel.add(un);
    	panel.add(userNameField);
    	panel.add(em);
    	panel.add(emailField);
    	panel.add(pw);
    	panel.add(passwordField);
    	panel.add(alias);
    	panel.add(aliasField);
    	panel.add(keyPass);
    	panel.add(keyPassField);
    	getRootPane().setDefaultButton(enter);
    	enter.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent ae){
    			String userName = userNameField.getText();
    			char[] password = passwordField.getPassword();
    			String alias = aliasField.getText();
    			char[] keyPass = keyPassField.getPassword();
    			String email = emailField.getText();
    	    	SwingUtilities.invokeLater(new Runnable() {
    	             //@Override
    	             public void run() {
					//We need to ask the user ahead of time to take responsibility for initiating 
					//Diffie-Hellman with the next client who logs in. For each pair of users 
					//the first one to log in MUST choose 'YES'
    	             	options[0] = "Yes, wait for correspondent";
    	            	options[1] = "No, I am 2nd correspondent";
    	            	 int dh = JOptionPane.showOptionDialog(getParent(), "Initiate Diffie-Hellman?", null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
    	            	 //yes=0, no=1
    	            	 new EchoClient(userName, password, alias, keyPass, email, host,
    	            			 portNumber, new_return, dh).start();
    	                 dispose();
    	             }
    	         });
    		}
    	});
    	add(panel, BorderLayout.CENTER);
    	add(enter, BorderLayout.SOUTH);
    	setVisible(true);
    	pack();
    }
    

}


