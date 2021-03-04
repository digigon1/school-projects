import java.io.*;


public class FileCopy {

	/**
	 * @param args
	 * usage: java FileCopy fromFile toFile [seconds]
     *
     * This program is an example of how to copy a file to another one,
     * block by block. It can be easily used to write a client and a
     * server programs allowing to copy a file from a local computer to
     * a remote one using an optimistic protocol that supposes that all
     * datagrams will arrive to the destination if the sender refrains
     * from sending too quickly
     *
	 */
	
	static final int BLOCKSIZE = 1024 ; // buffer for file copy - 1 KByte
	
	public static void main(String[] args) {
		// reading arguments
		if( args.length < 2 || args.length > 3 ) {
			System.err.printf("usage: java FileCopy fromFile toFile [seconds]\n") ;
			System.exit(0);
		}
		String fromFile = args[0] ;
		String toFile = args[1];
		int secondsToWait = 0;
		if ( args.length == 3) {
			try {
				secondsToWait = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
		        System.err.printf("argument " + args[2] + " must be an integer\n");
		        System.exit(0);
			}
		}
		// does fromFile exists and is readable ?
		File f = new File(fromFile);
		if ( f.exists() && f.canRead() ) {
//			System.out.printf("file: "+fromFile+" Ok to copy\n"); 
		} else {
		    System.err.printf("Can't open from file "+fromFile+ "\n");
		    System.exit(0);
		}
		try {
			long milliSeconds = System.currentTimeMillis();
			FileInputStream in = new FileInputStream (fromFile);
			FileOutputStream out = new FileOutputStream (toFile);
			byte[] fileBuffer = new byte[BLOCKSIZE];
			boolean finished = false;
			long byteCount = 0;
			int blockCount = 0;
			long speed = 0;
			int n;
			do {
				n = in.read(fileBuffer);
				if ( n == -1 ) n = 0;
//				System.out.printf("bytes read %d \n",  n );
				if ( n < BLOCKSIZE ) finished=true;  // no more bytes to read
				if ( secondsToWait > 0 ) Thread.sleep(secondsToWait * 1000);
				if ( n > 0 ) out.write(fileBuffer,0,n);
				byteCount += n;
				blockCount += 1;
			 } while ( !finished );
			 in.close();
			 out.close();
			 // compute time spent copying bytes
			 milliSeconds = System.currentTimeMillis() - milliSeconds;
			 speed = 1000 * 8 * Math.round( byteCount / milliSeconds );
			 System.out.printf("%d blocks and %d bytes copied, in %d milli seconds, at %d bps\n", 
					 blockCount, byteCount, milliSeconds, speed );
		} catch (Exception e) {System.err.printf("Can't copy file\n");
	    System.exit(0);
		}
	}
}
