/**
 *  GetMetadata.java - counts the number of pages each site has (the number
 *  of times each hostname appears at document header)
 *  usage example of edu.cmu.lemurproject.* InputFormat and RecordReader
 *  Vitor Duarte, FCT/NOVA
 *  based on public WordCount examples
 */

import data.SiteMetadata;
import edu.cmu.lemurproject.WarcRecord;
import edu.cmu.lemurproject.WritableWarcRecord;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class GetMetadata {

    public static class WarcToMetadata extends Mapper<LongWritable, WritableWarcRecord, Text, SiteMetadata> {

        protected void setup( Context cont ) {
            System.err.println(">>>Processing>>> "+((FileSplit)cont.getInputSplit()).getPath().toString() );
        }

        public void map(LongWritable key, WritableWarcRecord value, Context cont )
                throws IOException, InterruptedException {
            Text word = new Text();
            SiteMetadata metadata = new SiteMetadata();

            WarcRecord val = value.getRecord();
            String url = val.getHeaderMetadataItem( "WARC-Target-URI" );
            if (url == null)
                return;

            String time = val.getHeaderMetadataItem("WARC-Date");
            try {
                Date date = DatatypeConverter.parseDateTime(time).getTime();
                word.set(new URL(url).getHost());
                metadata.nb = Integer.parseInt(val.getHeaderMetadataItem("Content-Length"));
                metadata.start = metadata.end = date;
                cont.write( word, metadata );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public static class JoinMetadata extends Reducer<Text, SiteMetadata, Text, SiteMetadata> {

        public void reduce(Text key, Iterable<SiteMetadata> values, Context cont)
                throws IOException, InterruptedException {
            SiteMetadata metadata = new SiteMetadata();

            for ( SiteMetadata val : values ) {
                metadata.nb += val.nb;
                metadata.t += val.t;
                if (metadata.start == null || metadata.start.after(val.start)) {
                    metadata.start = val.start;
                }
                if (metadata.end == null || metadata.end.before(val.end)) {
                    metadata.end = val.end;
                }
            }

            if (metadata.start == metadata.end)
                metadata.t = Long.MAX_VALUE;
            else
                metadata.t = (metadata.end.getTime() - metadata.start.getTime()) / 1000;

            cont.write(key, metadata);
        }
    }
}
