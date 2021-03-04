package data;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

public class SiteMetadata implements Serializable, Writable {
    public String site = "";
    public int nb = 0;
    public long t = 0;
    public Date start = null, end = null;

    @Override
    public String toString() {
        return site + '\t' + String.valueOf(nb) + '\t' + t + '\t' + (1.0 * nb) / t + '\t';// + content;
    }

    public static SiteMetadata parse(String s) {
        SiteMetadata result = new SiteMetadata();

        String[] split = s.split("\t", 5);
        result.site = split[0];
        result.nb = Integer.parseInt(split[1]);
        result.t = Long.parseLong(split[2]);

        return result;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(site);
        dataOutput.writeInt(nb);
        dataOutput.writeLong(t);
        dataOutput.writeLong(start.getTime());
        dataOutput.writeLong(end.getTime());
    }

    public void readFields(DataInput dataInput) throws IOException {
        site = dataInput.readUTF();
        nb = dataInput.readInt();
        t = dataInput.readLong();
        start = new Date(dataInput.readLong());
        end = new Date(dataInput.readLong());
    }
}
