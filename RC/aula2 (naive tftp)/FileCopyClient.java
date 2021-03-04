
import java.io.*;
import java.net.*;


public class FileCopyClient {

	/**
	 * @param args
	 * usage: java FileCopy fromFile toFile server [milliseconds]
     *
	 */
	
	static final int BLOCKSIZE = 1024 ; // buffer for file copy - 1 KByte
	static final int PORT = 8000 ; // server port
	
	public static void main(String[] args) {
		// reading arguments
		if( args.length < 3 || args.length > 4 ) {
			System.err.printf("usage: java FileCopyClient fromFile toFile server [milliseconds]\n") ;
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
		// does fromFile exists and is readable ?
		File f = new File(fromFile);
		if ( f.exists() && f.canRead() ) {
			System.out.printf("file: "+fromFile+" Ok to send to server\n"); 
		} else {
		    System.err.printf("Can't open from file "+fromFile+ "\n");
		    System.exit(0);
		}		
		try {
			InetAddress serverAddress = InetAddress.getByName(server);
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket fileNamePacket = 
				new DatagramPacket (toFile.getBytes(),toFile.getBytes().length, serverAddress,PORT);
			socket.send(fileNamePacket);
			byte[] fileBuffer = new byte[BLOCKSIZE];
			DatagramPacket datagram = new DatagramPacket (fileBuffer,BLOCKSIZE,serverAddress,PORT);
			FileInputStream in = new FileInputStream (fromFile);
			boolean finished = false;
			long byteCount = 0;
			int blockCount = 0;
			long speed = 0;
			int n;
			double milliSeconds = System.currentTimeMillis();
			do {
				n = in.read(fileBuffer);
				if ( n == -1 ) n = 0;
				System.out.printf("bytes read %d \n",  n );
				if ( n < BLOCKSIZE ) { 
					finished=true;  // no more bytes to read
					datagram.setLength(n); // fileBuffer is not fully occupied
				}
				if ( milliSecondsToWait > 0 ) Thread.sleep(milliSecondsToWait);
				if ( n > 0 ) socket.send(datagram);
				byteCount += n;
				blockCount += 1;
			 } while ( !finished );
			 // compute time spent sending bytes
			 milliSeconds = 0.00001 + System.currentTimeMillis() - milliSeconds;
			 speed = 1000 * 8 * Math.round( byteCount / milliSeconds );
			 System.out.printf("%d blocks and %d bytes sent, in %s milli seconds, at %s bps\n", 
					 blockCount, byteCount, milliSeconds, speed );
			 in.close();
			 socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.printf("Can't open / send file\n");
	    System.exit(0);
		}
	}
}
