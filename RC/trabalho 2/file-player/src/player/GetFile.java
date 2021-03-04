package player;

import utils.HTTPUtilities;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class GetFile {

	private static volatile BlockingDeque<byte []> receiverQueue; //Check if its not queue
	private static volatile ArrayList<String> files;
	private static volatile int choice;
	private static volatile long totalTime;
	private static volatile boolean done;
	private static Player player;
	private static Map<Integer, Map<Integer, Integer>> offsets;
    private static ReducedStats stats;

    public static void main(String[] args) throws Exception {
		//GET INDEX.DAT
		String url = args.length > 0 ? args[0] : "http://localhost:8080/finding-dory/";
        URL indexURL = new URL(url+"index.dat");
//		System.out.print("\nShow URL components in the screen? (answer y for yes) ");
//		String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
//		if ( line.equalsIgnoreCase("Y")) {
//			System.out.println("URL = " + url);
//			System.out.println("protocol = " + u.getProtocol());
//			System.out.println("authority = " + u.getAuthority());
//			System.out.println("host = " + u.getHost());
//			System.out.println("port = " + u.getPort());
//			System.out.println("path = " + u.getPath());
//			System.out.println("query = " + u.getQuery());
//			System.out.println("filename = " + u.getFile());
//			System.out.println("ref = " + indexURL.getRef());
//		}
		InetAddress serverAddr = InetAddress.getByName(indexURL.getHost());
		int port = indexURL.getPort();
        if ( port == -1 )
			port = 80;
		String fileName = indexURL.getFile().substring(indexURL.getFile().lastIndexOf('/')+1);
		System.out.println("filename = "+fileName);
		File f = new File ("."+fileName);
		long size = f.length();
		FileOutputStream fos = new FileOutputStream(f,true); // append mode
		getFileByRange (indexURL, fos, size);
		fos.close();


		//READ BITRATES
		RandomAccessFile fsc = new RandomAccessFile(f, "r");
        List<Integer> rates = new LinkedList<>();
		String line;
		files = new ArrayList<>();
		while(!(line = fsc.readLine()).equalsIgnoreCase("")){
			if(line.charAt(0)!=';') {
                files.add(line);
                rates.add(Integer.parseInt(line.substring(0, line.indexOf('.'))));
            }
		}

		offsets = new TreeMap<>();
		Map<Integer, Integer> times =  new TreeMap<>();
		choice = 0;
        line = fsc.readLine();
		while(line != null && !line.equalsIgnoreCase("")){
            if(line.charAt(0)!=';'){
                Map<Integer, Integer> choiceOffsets = new TreeMap<>();
                for(Scanner ls; line != null && files.get(choice).equalsIgnoreCase((ls = new Scanner(line)).next()); line = fsc.readLine()){
                    int seg = ls.nextInt();
                    choiceOffsets.put(seg, ls.nextInt());
                    times.put(seg, ls.nextInt());
                }
                offsets.put(choice, choiceOffsets);
                choice++;
            } else {
                line = fsc.readLine();
            }
		}
        stats = new ReducedStats(rates, times.size());
		fsc.close();
		f.delete();


		//Creating player
		player = JavaFXMediaPlayer.getInstance().setSize(800, 480).mute(false); //TODO


		int delay = args.length > 1?
				Integer.parseInt(args[1]):
				5;

		choice = 0;
		totalTime = 0;
		done = false;
		receiverQueue = new LinkedBlockingDeque<>();
		new Thread(
				()->{
					try {
						int segment = 0;
						while(!done) {
							long connTime = System.nanoTime();
							String fName = files.get(choice);
							URL u = new URL(url + fName);
							HttpURLConnection connection = (HttpURLConnection) u.openConnection();
							connection.setRequestProperty("User-Agent", "X-RC2016");


							long start = offsets.get(choice).get(segment);
							long segPart = times.get(segment);
							totalTime += segPart;

                            if(offsets.get(choice).size()-2 > segment) {
								connection.setRequestProperty("Range", start + "-" + offsets.get(choice).get(segment + 1));
							} else {
								connection.setRequestProperty("Range", start + "-");
							}

							InputStream in = connection.getInputStream();
							BufferedInputStream response = new BufferedInputStream(in);
							DataInputStream dis = new DataInputStream(response);
							String segName = dis.readUTF();
                            if(segName.equalsIgnoreCase("eof")) {
								connection.disconnect();
								done = true;
								break;
							}

                            long waitTime = dis.readLong();
                            long sendTime = dis.readLong();

                            int contSize = dis.readInt();
							byte[] arr = new byte[contSize+(Long.BYTES+Long.BYTES)];//new byte[contSize+Long.BYTES];
							ByteBuffer b = ByteBuffer.wrap(arr);
							b.putLong(waitTime);

							b.putLong(sendTime);

							byte[] content = new byte[contSize];
							assert(contSize == dis.available());
							dis.readFully(content);
							stats.logGetSegment(choice, contSize);

							b.put(content);
							receiverQueue.addLast(b.array());
							dis.close();

							segment++;

							connTime = System.nanoTime() - connTime;


                            if((totalTime-(delay*1000))/(connTime/1E6) < 1.1) {
                                if (choice > 0) {
                                    choice--;
									if((totalTime-(delay*1000))/(connTime/1E6) < 0.4 && choice > 0) {
										choice--;
									}
                                }
                            } else if((totalTime-(delay*1000))/(connTime/1E6) > 1.2){
                                if (choice < files.size() - 1) {
                                    choice++;
									if((totalTime-(delay*1000))/(connTime/1E6) > 2.6 && choice < files.size() - 1){
										choice++;
									}
                                }
                            }

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		).start();

		waitForPlayoutDelay(delay);

		long maxWait = 0;

		long start = System.currentTimeMillis();
		long waitTime = 0;
        while (!receiverQueue.isEmpty() || !done) {

            if(totalTime == 0){
				System.out.println("rebuffering");
				stats.logRebuffering();
                waitForPlayoutDelay(delay);
            }

			byte[] data = null;
			try {
				data = receiverQueue.removeFirst();
			} catch (Exception e){
				System.out.println("rebuffering");
				stats.logRebuffering();
				data = Utils.poll(receiverQueue, delay*1000);
			}

            if (data == null || data.length == 0)
                continue;

            ByteBuffer b = ByteBuffer.wrap(data);
            waitTime = b.getLong();

            if(waitTime > maxWait)
               	maxWait = waitTime;

            totalTime -= waitTime;

            long sendTime = b.getLong();

			byte[] decoding = new byte[data.length - (Long.BYTES + Long.BYTES)];//new byte[data.length - Long.BYTES];
            b.get(decoding, 0, data.length - (Long.BYTES + Long.BYTES));//data.length - Long.BYTES);


            stats.logPlaySegment();
			player.decode(decoding);

			while(start + sendTime > System.currentTimeMillis())
				Utils.sleep(5);
        }
		System.out.println(waitTime+" ms");
		Utils.sleep(5*maxWait);
		stats.dumpStats();


	}

	private static void waitForPlayoutDelay(int delay) {
		while(delay*1000 > totalTime && !done)
            Utils.sleep(10);
	}

	public static boolean getFileByRange (URL u, FileOutputStream f, 
			long starting ) throws Exception {
		// Assuming URL of the form http:// ....
		InetAddress serverAddr = InetAddress.getByName(u.getHost());
		int port = u.getPort();
		if ( port == -1 )
			port = 80;
		String fileName = u.getPath();
		Socket sock = new Socket( serverAddr, port );
		OutputStream toServer = sock.getOutputStream();
		InputStream fromServer = sock.getInputStream();
		System.out.println("\nConnected to server");
		String request = String.format("GET %s HTTP/1.0\r\n", fileName);
		toServer.write(request.getBytes());
		request = String.format("User-Agent: X-RC2016\r\n", fileName);
		toServer.write(request.getBytes());
		System.out.println("Sent request: "+request);
		if ( starting != 0 ) { 
			String line = String.format("Range: bytes=%d-\r\n\r\n",starting);
			toServer.write(line.getBytes());
			System.out.println("Sent request: "+line);
		} else { 
			toServer.write("\r\n".getBytes());
		}
		String answerLine = HTTPUtilities.readLine(fromServer);
		System.out.println("Got answer: "+answerLine);
		String[] result = HTTPUtilities.parseHttpRequest(answerLine); //HTTPUtilities.parseHttpReply(answerLine);
		if (  result[1].equalsIgnoreCase("200") && result[2].equalsIgnoreCase("OK") ||
			  result[1].equalsIgnoreCase("206") && result[2].equalsIgnoreCase("Partial Content")) {
			System.out.println("The file exists, answer was: "+answerLine);
			while ( !answerLine.equals("") ) {
				System.out.println("Got:\t"+answerLine);
				answerLine = HTTPUtilities.readLine(fromServer);
			}
			System.out.println("Got:\tan empty line\n");
			byte[] buffer = new byte[100000];
			int count = 0; 
			long total = starting;
			int n;
			// return fromServer;
			while( (n = fromServer.read(buffer) ) >= 0 ) {
				f.write(buffer, 0, n);
				total += n;
				count += n;
				if ( count > 1000000 ) {
					System.out.print(" "+total+" ");
					count = 0;
				}
			}
		} else 
			System.out.println("Got unexpected line: "+answerLine);
		System.out.println("\nGot everything: \n");
		sock.close();
		return true;
	}
}



