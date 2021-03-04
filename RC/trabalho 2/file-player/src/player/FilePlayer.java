package player;

import javax.xml.crypto.Data;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.*;

public class FilePlayer {

	public static void main(String[] args) throws Exception {
	    String fileName = args.length != 1 ? "finding-dory/128.ts" : args[0];

	    Player player = JavaFXMediaPlayer.getInstance().setSize(800, 460).mute(false);

		//FileInputStream fis = new FileInputStream(fileName);
		//DataInputStream dis = new DataInputStream(fis);

		URL url = new URL("http://localhost:8080/"+fileName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "X-RC2016");

        BufferedInputStream response = new BufferedInputStream(connection.getInputStream());
        DataInputStream dis = new DataInputStream(response);

		while (!dis.readUTF().equals("eof")) {
			System.out.println(dis.readLong());
			System.out.println(dis.readLong());
			byte[] data = new byte[dis.readInt()];
			dis.readFully(data);
			player.decode(data);
		}
		dis.close();
	}

}
