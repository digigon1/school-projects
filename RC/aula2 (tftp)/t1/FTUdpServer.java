package t1;

/**
 * TftpServer - a very simple TFTP like server - RC FCT/UNL
 * 
 * Limitations:
 * 		default port is not 69;
 * 		ignores mode (always works as octet);
 *              only receives files
 * 		only receives files always assuming the default timeout
 *              data and ack numbers are a long instead of a short
 *              assumes that the sequence number and ack number is always equal
 *              to the order of the first byte (beginning at 1) of the block sent
 *              or acked
 * Note: this implementation assumes that all Java Strings used contain only
 * ASCII characters. If it's not so, length() and getBytes().length return different sizes
 * and unexpected problems can appear ... 
 **/

import static t1.TftpPacket.MAX_TFTP_PACKET_SIZE;
import static t1.TftpPacket.MAX_TFTP_DATA_SIZE;
import static t1.TftpPacket.OP_ACK;
import static t1.TftpPacket.OP_DATA;
import static t1.TftpPacket.OP_ERROR;
import static t1.TftpPacket.OP_WRQ;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Map;

public class FTUdpServer implements Runnable {
	static int DEFAULT_PORT = 9000; // my default port
	static int DEFAULT_TRANSFER_TIMEOUT = 15000; // terminates transfer after
    // this timeout if no data block is received

	private String filename;
	private SocketAddress cltAddr;



	FTUdpServer(TftpPacket req, SocketAddress cltAddr) {
		this.cltAddr = cltAddr;
		filename = req.getFilename();
	}

	public void run() {
		System.out.println("START!");
		receiveFile();
		System.out.println("DONE!");
	}

	private void receiveFile() {
		System.err.println("receiving file:" + filename );
		try {
			
			DatagramSocket socket = new DatagramSocket();

			// Defines the timeout to to end the server, in case the client stops sending data
			socket.setSoTimeout(DEFAULT_TRANSFER_TIMEOUT);

			//confirms the file transfer request
			sendAck(socket, 0L, cltAddr); 

			RandomAccessFile raf = new RandomAccessFile(filename + ".bak", "rw");
			boolean finished = false;
			
			long expectedByte = 1; // next byte in sequence
			do {
				byte[] buffer = new byte[MAX_TFTP_PACKET_SIZE];
				DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
				socket.receive(datagram);
				TftpPacket pkt = new TftpPacket(datagram.getData(), datagram.getLength());
				switch (pkt.getOpcode()) {
				case OP_DATA:
					//saves the data at the proper offset
					byte[] data = pkt.getBlockData();
					raf.seek(pkt.getBlockSeqN() - 1L);
					raf.write(data);
					if (pkt.getBlockSeqN() == expectedByte)
						expectedByte += data.length;
					sendAck(socket, expectedByte, cltAddr);
					finished = data.length < MAX_TFTP_DATA_SIZE;
					break;
				case OP_WRQ:
					sendAck(socket, 0L, cltAddr);
					break;
				case OP_ERROR:
					throw new IOException("Got error from server: " + pkt.getErrorCode() + ": " + pkt.getErrorMessage());
				default:
					throw new RuntimeException("Error receiving file." + filename + "/" + pkt.getOpcode());
				}
			} while (!finished);
			raf.close();

		} catch (SocketTimeoutException x) {
			System.err.printf("Interrupted transfer. No data received after %s ms\n", DEFAULT_TRANSFER_TIMEOUT);
		} catch (Exception x) {
			System.err.println("Receive failed: " + x.getMessage());
		}
	}

	/*
	 * Prepare and send an TftpPacket ACK
	 */
	private static void sendAck(DatagramSocket s, long seqN, SocketAddress dst) throws IOException {
		TftpPacket ack = new TftpPacket().putShort(OP_ACK).putLong(seqN);
		s.send(new DatagramPacket(ack.getPacketData(), ack.getLength(), dst));
		System.err.printf("sent: %s \n", ack);
	}

	public static void main(String[] args) throws Exception {

		// create and bind socket to port for receiving client requests
		DatagramSocket mainSocket = new DatagramSocket(DEFAULT_PORT);
		System.out.println("New tftp server started at local port " + mainSocket.getLocalPort());

		for (;;) { // infinite processing loop...
			try {
				// receives request from clients
				byte[] buffer = new byte[MAX_TFTP_PACKET_SIZE];
				DatagramPacket msg = new DatagramPacket(buffer, buffer.length);
				mainSocket.receive(msg);

				// look at datagram as a TFTP packet
				TftpPacket req = new TftpPacket(msg.getData(), msg.getLength());
				switch (req.getOpcode()) {
				case OP_WRQ: // Write Request
					System.err.println("Write Request:" + req.getFilename());

					// Launch a dedicated thread to handle the client
					// request...
					new Thread(new FTUdpServer(req, msg.getSocketAddress())).start();
					break;
				default: // unexpected packet op code!
					System.err.printf("???? packet opcode %d ignored\n", req.getOpcode());
					sendError(mainSocket, 0, "Unknown request type..." + req.getOpcode(), msg.getSocketAddress());
				}
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	/*
	 * Sends an error packet
	 */
	private static void sendError(DatagramSocket s, int err, String str, SocketAddress dstAddr) throws IOException {
		TftpPacket pkt = new TftpPacket().putShort(OP_ERROR).putShort(err).putString(str).putByte(0);
		s.send(new DatagramPacket(pkt.getPacketData(), pkt.getLength(), dstAddr));
	}

}
