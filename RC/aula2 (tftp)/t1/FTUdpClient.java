
import java.io.*;
import java.net.*;

public class FTUdpClient{

	static int DEFAULT_PORT = 9000; // my default port
	static int DEFAULT_TRANSFER_TIMEOUT = 15000; // terminates transfer after
    // this timeout if no data block is received



	public static void main(String[] args) {
		if( args.length < 3 || args.length > 4 ) {
			System.err.printf("usage: java FTUdpClient fromFile toFile server [milliseconds]\n") ;
			System.exit(0);
		}

		String fromFile = args[0] ;
		String toFile = args[1];
		String server = args[2];
		int milliSecondsToWait = 0;

		if ( args.length == 4) {
			try {
				milliSecondsToWait = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
		        System.err.printf("argument " + args[3] + " must be an integer\n");
		        System.exit(0);
			}
		}

		File f = new File(fromFile);
		if ( f.exists() && f.canRead() ) {
			System.out.printf("file: "+fromFile+" Ok to send to server\n"); 
		} else {
		    System.err.printf("Can't open from file "+fromFile+ "\n");
		    System.exit(0);
		}

		try{
			//TODO
			boolean finished = false;
			

		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("Can't open / send file\n");
	    	System.exit(0);
		}
	}
	
}