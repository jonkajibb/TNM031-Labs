package server;
// An example class that uses the secure server socket class

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;

public class SecureAdditionServer {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "src/server/LIUkeystore.ks";
	static final String TRUSTSTORE = "src/server/LIUtruststore.ks";
	static final String KEYSTOREPASS = "123456";
	static final String TRUSTSTOREPASS = "abcdef";
	
	public File file;

	/**
	 * Constructor
	 * 
	 * @param port The port where the server will listen for requests
	 */
	SecureAdditionServer(int port) {
		this.port = port;
	}

	/** The method that does the work for the class */
	public void run() {
		try {
			
			// Here the keystore and truststore are created
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(KEYSTORE), KEYSTOREPASS.toCharArray());

			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(TRUSTSTORE), TRUSTSTOREPASS.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYSTOREPASS.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);

			// TLS, for extra security
			SSLContext sslContext = SSLContext.getInstance("TLS");
			// SSL handshake
			// null as random number parameter -> default seed is used
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket(port);
			// All cipher suits are enabled for flexibility
			sss.setEnabledCipherSuites(sss.getSupportedCipherSuites());
			//System.out.println(sss.getEnabledCipherSuites()[3]);

			System.out.println("\n>>>> SecureAdditionServer: active ");
			SSLSocket incoming = (SSLSocket) sss.accept();
			
			
			DataInputStream socketIn = new DataInputStream(incoming.getInputStream());
			DataOutputStream socketOut = new DataOutputStream(incoming.getOutputStream());
			FileInputStream fis = null;
			FileOutputStream fos = null;
			
			int option = socketIn.readInt();
			System.out.println("Option: " + option);
			String fileName = ""; 
			byte[] fileData = null;
			
		    File resourcesDirectory = new File("src/server/");

		    switch (option) {
		    case 1 :
		    	fileName = socketIn.readUTF();
				
				fis = new FileInputStream(new File(resourcesDirectory.getAbsolutePath() + "\\" + fileName));
				fileData = new byte[fis.available()];
				fis.read(fileData);
				fis.close();
				
				socketOut.writeInt(fileData.length);
				socketOut.write(fileData);
		    	break;
		    case 2 :
		    	fileName = socketIn.readUTF();

				// Receiving file length from client
		    	int fileLength = socketIn.readInt();
		    	fileData = new byte[fileLength];

		    	// File output stream to correct file path
		    	fos = new FileOutputStream(new File(resourcesDirectory.getAbsolutePath() + "\\" + fileName));
		    	socketIn.read(fileData);
		    	fos.write(fileData);
		    	fos.close(); // closes output stream
		    	break;
		    case 3 :
		    	fileName = socketIn.readUTF();
				
				File f = new File(resourcesDirectory.getAbsolutePath() + "\\" + fileName);
				
				f.delete();
				
				System.out.println(fileName + " was deleted.");
		    	break;
		    	
		    default :
		    	System.out.println("Invalid input. Must be 1, 2 or 3.");
		    }
		    
			incoming.close();
		} catch (Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	/**
	 * The test method for the class
	 * 
	 * @param args[0] Optional port number in place of the default
	 */
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		SecureAdditionServer addServe = new SecureAdditionServer(port);
		addServe.run();
	}
}
