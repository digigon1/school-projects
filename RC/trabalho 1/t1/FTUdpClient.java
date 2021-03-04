package t1;

import static t1.TftpPacketV16.MAX_TFTP_PACKET_SIZE;
import static t1.TftpPacketV16.DEFAULT_BLOCK_SIZE;
import static t1.TftpPacketV16.OP_ACK;
import static t1.TftpPacketV16.OP_DATA;
import static t1.TftpPacketV16.OP_WRQ;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.TreeMap;
import java.util.NavigableMap;
import java.util.Map;
import java.util.SortedMap;

public class FTUdpClient {
    static final int DEFAULT_TIMEOUT = 1000;
    static final int DEFAULT_MAX_RETRIES = 7;
    static int DEFAULT_WINDOW_SIZE = 20;

    static final float ALPHA = 0.75f;

    static int WindowSize = 1;
    static int BlockSize = DEFAULT_BLOCK_SIZE;
    static int Timeout = DEFAULT_TIMEOUT;

    private String filename;
    private Stats stats;
    private DatagramSocket socket;
    private BlockingQueue<TftpPacketV16> receiverQueue;
    volatile private SocketAddress srvAddress;

    private long maxSeq;

    FTUdpClient(String filename, SocketAddress srvAddress) {
		this.filename = filename;
		this.srvAddress = srvAddress;
		maxSeq = -1;
    }

    void sendFile() {
		try {
	
		    
		    socket = new DatagramSocket();
	
		    // create producer/consumer queue for ACKs
		    receiverQueue = new ArrayBlockingQueue<>(WindowSize);

		    // for statistics
		    stats = new Stats();
	
		    // start a receiver process to feed the queue
		    new Thread(() -> {
			    try {
					for (;;) {
					    byte[] buffer = new byte[MAX_TFTP_PACKET_SIZE];
					    DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
					    socket.receive(msg);
					    // update server address (it may change due to WRQ
					    // coming from a different port
					    srvAddress = msg.getSocketAddress();

					    System.err.println("CLT: " + new TftpPacketV16(msg.getData(), msg.getLength()));

					    // make the packet available to sender process
					    TftpPacketV16 pkt = new TftpPacketV16(msg.getData(), msg.getLength());
					    receiverQueue.put(pkt);
					}
			    } catch (Exception e) {
			    }
		    }).start();
	
		    System.out.println("\nSending file: \"" + filename + "\" to server: " + srvAddress + " from local port:"
				       + socket.getLocalPort() + "\n");

		    TftpPacketV16 wrr = new TftpPacketV16().putShort(OP_WRQ).putString(filename).putByte(0).putString("octet")
			.putByte(0).putString("blksize").putByte(0).putString(Integer.toString(BlockSize)).putByte(0);

			TreeMap<Long, TftpPacketV16> pkt = new TreeMap<>();

			pkt.put(0L, wrr);

		    sendRetry(pkt, 0L, DEFAULT_MAX_RETRIES, null);

		    try {
	
				FileInputStream f = new FileInputStream(filename);

				long byteCount = 1L; // block byte count starts at 1
				// read and send blocks
				byte[] buffer = new byte[BlockSize];

				int n = 1;

				pkt = new TreeMap<>();

				for (int i = 0; i < WindowSize && (n = f.read(buffer)) > 0; i++) {
					TftpPacketV16 pk = new TftpPacketV16().putShort(OP_DATA).putLong(byteCount).putBytes(buffer, n);
						
					pkt.put(byteCount, pk);
					byteCount += n;
   					stats.newPacketSent(n);
				}

				sendRetry(pkt, byteCount, DEFAULT_MAX_RETRIES, f);	

				f.close();
	
		    } catch (Exception e) {
				System.err.println("failed with error \n" + e.getMessage());
				System.exit(0);
		    }
		    socket.close();
		    System.out.println("Done...");
		} catch (Exception x) {
		    x.printStackTrace();
		    System.exit(0);
		}
		stats.printReport();
    }

    /*
     * Send a block to the server, repeating until the expected ACK is received,
     * or the number of allowed retries is exceeded.
     */
    void sendRetry(SortedMap<Long, TftpPacketV16> blk, long maxExpectedACK, int retries, FileInputStream f) throws Exception {
    	int n = 1;
    	maxSeq = maxExpectedACK;
    	for (int a = 0; a < retries; a++) {
    		SortedMap<Long, Long> times = new TreeMap<>();

    		for(TftpPacketV16 pkt : blk.values()){
    			socket.send(new DatagramPacket(pkt.getPacketData(), pkt.getLength(), srvAddress));
    			if(pkt.getOpcode() == OP_DATA)
    				times.put(pkt.getSeqN(), System.currentTimeMillis());
    			else
    				times.put(0L, System.currentTimeMillis());
    		}

    		long sendTime = times.get(times.firstKey());

	   		//Thread.sleep(500); //if you want to transmit slowly...
			long remaining;
			while((remaining = (sendTime + Timeout) - System.currentTimeMillis()) > 0) {

				TftpPacketV16 ack = receiverQueue.poll(remaining, TimeUnit.MILLISECONDS);
				
				if (ack != null){
				    if (ack.getOpcode() == OP_ACK){
						if(blk.containsKey(ack.getCurrentSeqN())){

							System.err.println("got expected ack: " + ack.getCurrentSeqN());

							stats.newRttMeasure(System.currentTimeMillis() - sendTime);

							blk.remove(ack.getCurrentSeqN());
							times.remove(ack.getCurrentSeqN());

							if(maxSeq < ack.getCumulativeSeqN()){
								maxSeq = ack.getCumulativeSeqN();

								blk = blk.tailMap(maxSeq);
								blk.remove(maxSeq);
								times = times.tailMap(maxSeq);
								times.remove(maxSeq);
								
								if(ack.getCurrentSeqN() == 0){
									return;
								}
								
							} else {
								a = 0;
							}

							stats.newTimeoutMeasure(Timeout);
							//TIMEOUT ADAPTATIVO
							Timeout = (int)(Timeout + Math.pow(2, -WindowSize)*(stats.getAverage()-Timeout));
							
							stats.newWindowSizeMeasure(WindowSize);
							//JANELA ADAPTATIVA
							WindowSize = Math.min(WindowSize + 1, DEFAULT_WINDOW_SIZE);

							byte[] buffer = new byte[BlockSize];
							for (; blk.size() < WindowSize && f!=null && (n = f.read(buffer)) > 0; ) {
								TftpPacketV16 pk = new TftpPacketV16().putShort(OP_DATA).putLong(maxSeq).putBytes(buffer, n);
								
								blk.put(maxSeq, pk);
								maxSeq += n;
    							stats.newPacketSent(n);

    							socket.send(new DatagramPacket(pk.getPacketData(), pk.getLength(), srvAddress));
    							times.put(pk.getSeqN(), System.currentTimeMillis());
							}
						    
							if(blk.size()==0)
								return;

						    if(n > 0)
						    	sendTime = times.get(blk.firstKey());
						
						} else {
							WindowSize = 1;
						    System.err.println("got wrong ack");
						    continue;
						}
				    	
				    } else {
							System.err.println("got unexpected packet (error)");
							continue;
				    }
				} else{
				    System.err.println("timeout...");
				    Timeout = DEFAULT_TIMEOUT;
				}
				
			}
		}
		throw new IOException("too many retries");
    }

    class Stats {
		private long totalRtt = 0;
		private int timesMeasured = 0;
		private int window = 1;
		private int totalPackets = 0;
		private int totalBytes = 0;
		private long startTime = 0L;

		private long totalWindowSize = 0;
		private long windowSizeMeasures = 0;

		private long totalTimeout = 0;
		private long timeoutMeasure = 0;
	
		Stats() {
		    startTime = System.currentTimeMillis();
		}
	
		void newPacketSent(int n) {
		    totalPackets++;
		    totalBytes += n;
		}
	
		void newRttMeasure(long t) {
		    timesMeasured++;
		    totalRtt += t;
		}

		void newWindowSizeMeasure(long w){
			windowSizeMeasures++;
			totalWindowSize += w;
		}

		void newTimeoutMeasure(long t){
			timeoutMeasure++;
			totalTimeout += t;
		}

		float getAverage(){
			return (totalRtt*1.0f)/timesMeasured;
		}
	
		void printReport() {
		    // compute time spent receiving bytes
		    int milliSeconds = (int) (System.currentTimeMillis() - startTime);
		    float speed = (float) (totalBytes * 8.0 / milliSeconds / 1000); // M
		    // bps
		    float averageRtt = (float) totalRtt / timesMeasured;
		    window = WindowSize;

		    System.out.println("\nTransfer stats:");
		    System.out.println("\nFile size:\t\t\t" + totalBytes);
		    System.out.println("Packets sent:\t\t\t" + totalPackets);
		    System.out.printf("End-to-end transfer time:\t%.3f s\n", (float) milliSeconds / 1000);
		    System.out.printf("End-to-end transfer speed:\t%.3f M bps\n", speed);
		    System.out.printf("Average rtt:\t\t\t%.3f ms\n", averageRtt);
		    //System.out.printf("Sending window size:\t\t%d packet(s)\n\n", window);
		    System.out.printf("Average window size: %.3f\n", (float)(totalWindowSize*1.0/windowSizeMeasures));
		    System.out.printf("Average timeout measure: %.3f ms\n", (totalTimeout * 1.0)/timeoutMeasure);
		}
	}

	public static void main(String[] args) throws Exception {
		// MyDatagramSocket.init(1, 1);
		try {
		    switch (args.length) {
		    	case 5:
				// By the moment this parameter is ignored and the client
				// WindowSize
				// is always equal to 1 (stop and wait)
					DEFAULT_WINDOW_SIZE = Integer.parseInt(args[4]);
		    	case 4:
					Timeout = Integer.valueOf(args[3]);
		    	case 3:
					BlockSize = Integer.valueOf(args[2]);
		    	case 2:
					break;
		    	default:
					throw new Exception("bad parameters");
		    }
		} catch (Exception x) {
		    System.out.printf("usage: java FTUdpClient filename servidor [blocksize [ timeout [ windowsize ]]]\n");
		    System.exit(0);
		}
		String filename = args[0];
		String server = args[1];
		SocketAddress srvAddr = new InetSocketAddress(server, FTUdpServer.DEFAULT_PORT);
		new FTUdpClient(filename, srvAddr).sendFile();
    }

} // FTUdpClient

