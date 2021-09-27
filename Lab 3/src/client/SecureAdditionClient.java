package client;
// A client-side class that uses a secure TCP/IP socket

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class SecureAdditionClient {
	private InetAddress host;
	private int port;
	// This is not a reserved port number 
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "src/client/LIUkeystore.ks";
	static final String TRUSTSTORE = "src/client/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
	
	
	// Constructor @param host Internet address of the host where the server is located
	// @param port Port number on the host where the server is listening
	public SecureAdditionClient( InetAddress host, int port ) {
		this.host = host;
		this.port = port;
	}
	
  // The method used to start a client object
	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
			SSLSocket client =  (SSLSocket)sslFact.createSocket(host, port);
			client.setEnabledCipherSuites( client.getSupportedCipherSuites() );
			System.out.println("\n>>>> SSL/TLS handshake completed");

			DataInputStream socketIn = new DataInputStream(client.getInputStream());
			DataOutputStream socketOut = new DataOutputStream(client.getOutputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			
			FileInputStream fis = null;
			FileOutputStream fos = null;
			
			File resourcesDirectory = new File("src/client/");
			
			System.out.println("Choose an option: \n1. Download file \n2. Upload file \n3. Delete file");
		    
			String input = reader.readLine();
		    int option = 0;
			try {
		    	option = Integer.parseInt(input);
		    	
				String fileName = "";
				String fileData = "";
		    	switch (option) {
			    
			    case 1 :
			    	System.out.println("Enter file name: ");
			    	fileName = reader.readLine();

			    	socketOut.writeUTF("DOWNLOAD_FILE");

			    	socketOut.writeUTF(fileName);
			    	
			    	fileData = socketIn.readUTF();
			    	
			    	fos = new FileOutputStream(resourcesDirectory.getAbsolutePath() + "\\" + fileName);
			    	fos.write(fileData.getBytes());
			    	fos.close();
			    	
			    	System.out.println("File was downloaded to: " + resourcesDirectory.getAbsolutePath() + "\\" + fileName);
			    	
			    	break;
			    case 2 :
			    	System.out.println("Enter file name...");
			    	
			    	break;
			    case 3 :
			    	System.out.println("Enter file name:");
			    	
			    	break;
			    	
			    default :
			    	System.out.println("Invalid input. Must be 1, 2 or 3.");
			    }
		    } catch (NumberFormatException e){
		    	System.out.println("Invalid input. Must be 1, 2 or 3.");
		    }
			
			
			// Replace this with file transfer code???
			/*BufferedReader socketIn;
			socketIn = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
			PrintWriter socketOut = new PrintWriter( client.getOutputStream(), true );
			
			String numbers = "1.2 3.4 5.6";
			System.out.println( ">>>> Sending the numbers " + numbers+ " to SecureAdditionServer" );
			socketOut.println( numbers );
			System.out.println( socketIn.readLine() );

			socketOut.println ( "" );*/
		}
		catch( Exception x ) {
			System.out.println( x );
			x.printStackTrace();
		}
	}
	
	
	// The test method for the class @param args Optional port number and host name
	public static void main( String[] args ) {
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if ( args.length > 0 ) {
				port = Integer.parseInt( args[0] );
			}
			if ( args.length > 1 ) {
				host = InetAddress.getByName( args[1] );
			}
			SecureAdditionClient addClient = new SecureAdditionClient( host, port );
			addClient.run();
		}
		catch ( UnknownHostException uhx ) {
			System.out.println( uhx );
			uhx.printStackTrace();
		}
	}
}
