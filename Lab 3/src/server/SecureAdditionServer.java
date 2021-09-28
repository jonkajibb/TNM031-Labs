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
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(KEYSTORE), KEYSTOREPASS.toCharArray());

			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(TRUSTSTORE), TRUSTSTOREPASS.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYSTOREPASS.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket(port);
			sss.setEnabledCipherSuites(sss.getSupportedCipherSuites());

			System.out.println("\n>>>> SecureAdditionServer: active ");
			SSLSocket incoming = (SSLSocket) sss.accept();
			
			//BufferedReader reader = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			DataInputStream socketIn = new DataInputStream(incoming.getInputStream());
			DataOutputStream socketOut = new DataOutputStream(incoming.getOutputStream());
			//PrintWriter out = new PrintWriter(incoming.getOutputStream(), true);
			FileInputStream fis = null;
			FileOutputStream fos = null;
						
			String option = socketIn.readUTF();
			System.out.println("Option: " + option);
			String fileName = ""; 
			byte[] fileData = null;
			//byte[] data;
			
		    File resourcesDirectory = new File("src/server/");

			
			if(option.equals("DOWNLOAD")) {
				fileName = socketIn.readUTF();
								
				fis = new FileInputStream(new File(resourcesDirectory.getAbsolutePath() + "\\" + fileName));
				fileData = new byte[fis.available()];
				fis.read(fileData);
				fis.close();
				
				/*
				// Test
				// image is fully read on server side
				// image is not complete after being sent to client...
				fos = new FileOutputStream(new File(resourcesDirectory.getAbsolutePath() + "\\" + "new.png"));
				fos.write(fileData);
				fos.close();
				*/
				
				socketOut.writeInt(fileData.length);
				socketOut.write(fileData);
				
				
			} else if (option.equals("UPLOAD")){
				fileName = socketIn.readUTF();
		    	//System.out.println(socketIn.available());

				// Receiving file length from client
		    	int fileLength = socketIn.readInt();
		    	fileData = new byte[fileLength];

		    	// File output stream to correct file path
		    	fos = new FileOutputStream(new File(resourcesDirectory.getAbsolutePath() + "\\" + fileName));
		    	//String data = socketIn.readUTF();
		    	socketIn.read(fileData);
		    	//fileData = data.getBytes();
		    	fos.write(fileData);
		    	// This loop is needed to write ALL of fileData
		    	// Rest of the DataInputStream is read here, i.e. the file contents
		    	/*int count;
		    	//System.out.println(socketIn.available());
		    	while ((count = socketIn.read(fileData)) >= 0)
		    	{
		    		fos.write(fileData, 0, count);
		    	}*/
		    	fos.close(); // closes output stream
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
