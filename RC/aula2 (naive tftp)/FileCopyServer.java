
import java.io.*;
import java.net.*;


public class FileCopyServer {

	/**
	 * @param args
	 * usage: java FileCopyServer
     *
	 */
	
	static final int BLOCKSIZE = 1024 ; // buffer for file copy - 1 KByte
	static final int PORT = 8000 ; // server port
	
	public static void main(String[] args) {
		try {
			DatagramSocket socket = new DatagramSocket(PORT);
			byte[] fileBuffer = new byte[BLOCKSIZE];
			DatagramPacket packet = new DatagramPacket (fileBuffer,BLOCKSIZE);
			socket.receive(packet);
			String toFile = new String(packet.getData(), 0, packet.getLength());
			packet.setLength(BLOCKSIZE); // ajust packet length to the full size
			System.out.printf("Ready to receive file %s \n",  toFile );
			FileOutputStream out = new FileOutputStream (toFile);
			boolean finished = false;
			long byteCount = 0;
			int blockCount = 0;
			long speed = 0;
			int n;
			long milliSeconds = System.currentTimeMillis();

			do {
				socket.receive(packet);
				n = packet.getLength();
				// System.out.printf("bytes received %d \n",  n );
				if ( n < BLOCKSIZE ) finished=true;  // no more bytes to receive
				if ( n > 0 ) out.write(packet.getData(), 0, n );
				byteCount += n;
				blockCount += 1;
			 } while ( !finished );

			 // compute time spent receiving bytes
			 milliSeconds = System.currentTimeMillis() - milliSeconds;
			 speed = 1000 * 8 * Math.round( byteCount / milliSeconds );
			 System.out.printf("%d blocks and %d bytes received, in %d milli seconds, at %d bps\n", 
					 blockCount, byteCount, milliSeconds, speed );
			 out.close();
			 socket.close();
		} catch (Exception e) {System.err.printf("Can't create / receive file\n");
	    System.exit(0);
		}
	}
}
