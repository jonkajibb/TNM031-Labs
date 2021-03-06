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
			// Here the keystore and truststore are created
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			ks.load( new FileInputStream( KEYSTORE ), KEYSTOREPASS.toCharArray() );
			
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), TRUSTSTOREPASS.toCharArray() );
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, KEYSTOREPASS.toCharArray() );
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			
			// TLS, for extra security
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			// SSL handshake
			// null as random number parameter -> default seed is used
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
			SSLSocket client =  (SSLSocket)sslFact.createSocket(host, port);
			// All cipher suits are enabled for flexibility
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
				byte[] fileData = null;
		    	switch (option) {
			    
			    case 1 :
			    	System.out.println("Enter file name: ");
			    	fileName = reader.readLine();

			    	// Sending option and file name to server
			    	socketOut.writeInt(option);
			    	socketOut.writeUTF(fileName);
			    	
			    	// Receiving file length from server
			    	int fileLength = socketIn.readInt();
			    	fileData = new byte[fileLength];
			    	
			    	// File output stream to correct file path
			    	fos = new FileOutputStream(new File(resourcesDirectory.getAbsolutePath() + "\\" + fileName));
			    	
			    	// This loop is needed to write ALL of fileData
			    	// Rest of the DataInputStream is read here, i.e. the file contents
			    	int count;
			    	while ((count = socketIn.read(fileData)) >= 0)
			    	{
			    		fos.write(fileData, 0, count);
			    	}
			    	fos.close(); // closes output stream
			    	
			    	System.out.println("File was downloaded to: " + resourcesDirectory.getAbsolutePath() + "\\" + fileName);
			    	
			    	break;
			    case 2 :
			    	System.out.println("Enter file name: ");
			    	fileName = reader.readLine();
			    	
			    	fis = new FileInputStream(new File(resourcesDirectory.getAbsolutePath() + "\\" + fileName));
					fileData = new byte[fis.available()];

					fis.read(fileData);
					
			    	// Sending option and file name to server
			    	socketOut.writeInt(option);
			    	socketOut.writeUTF(fileName);
			    	socketOut.writeInt(fileData.length);

			    	socketOut.write(fileData);
					fis.close();

			    	break;
			    case 3 :
			    	System.out.println("Enter file name:");
			    	fileName = reader.readLine();

			    	// Sending option and file name to server
			    	socketOut.writeInt(option);
			    	socketOut.writeUTF(fileName);
			    	
			    	break;
			    	
			    default :
			    	System.out.println("Invalid input. Must be 1, 2 or 3.");
			    }
		    } catch (NumberFormatException e){
		    	System.out.println("Invalid input. Must be 1, 2 or 3.");
		    }
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
