package lab2;

import java.math.*;
import java.io.*;
import java.util.Random;

public class Main {

	public static void main(String[] args) throws IOException {
		// to convert an integer b into a BigInteger
		//int b = 170; 
		//BigInteger bigB = new BigInteger(String.valueOf(b));

		Random rand = new Random();
		// User input
		System.out.print("Enter message: ");
	    String input = (new BufferedReader(new InputStreamReader(System.in))).readLine();

	    // Convert input string to a BigInteger
	    BigInteger m = new BigInteger(input.getBytes());
		System.out.println("message as int = " + m);


		//------------- RSA starts here ------------------
		
		BigInteger p, q, n, phi_n, e, d;
		
		// Generate two primes p and q, then compute n
		// The bit length has to be larger than the message
		
		p = BigInteger.probablePrime(100, new Random());
		q = BigInteger.probablePrime(100, new Random());

		n = p.multiply(q); // n is public
		
		// Encryption exponent e
		// gcd(e, (p-1)(q-1)) means that e and (p-1)(q-1) are coprimes
		
		phi_n = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		
		// e must be 0 < e < phi_n, but should be large cuz security (according to Math begind RSA on course website)
		// e can't be even (?) since p-1 and n-1 are even
		
		e = phi_n.subtract(BigInteger.ONE); // makes it odd since phi_n is even
		System.out.println("Computing e...");
		
		while(true) {
			if(e.gcd(phi_n).equals(BigInteger.ONE)) {
				break;
			}
			
			e.subtract(BigInteger.TWO);
		}
		/*while( e.compareTo(BigInteger.ONE) == 1 || e.compareTo(phi_n) == -1 || !e.gcd(phi_n).equals(BigInteger.ONE)) {
			e = new BigInteger(phi_n.bitLength(), rand);
		}*/
		System.out.println("e = " + e);
		
		// Compute private key d
		// de === 1 mod(phi_n) -> 1 = ed mod(phi_n)
		
		d = e.modInverse(phi_n);
		
		
		// ENCRYPTION
		BigInteger ciphertext = m.modPow(e, n);
		
		System.out.println("Ciphertext: " + ciphertext);
		
		// Decryption
		BigInteger decrypted_m = ciphertext.modPow(d, n);
		
	    // to convert a BigInteger back to a string	
	    String s = new String(decrypted_m.toByteArray());
	    System.out.println(s);
	}

}
