import data.SiteMetadata;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.util.TreeMap;

public class Filter10Sites {

    public static class MetadataToLocalTop10 extends Mapper<Text, Text, Text, SiteMetadata> {

        private TreeMap<Integer, SiteMetadata> toRecordMap = new TreeMap<>();

        protected void setup( Context cont ) {
            System.err.println(">>>Processing>>> "+((FileSplit)cont.getInputSplit()).getPath().toString() );
        }

        @Override
        public void map(Text key, Text value, Context cont ) {
            SiteMetadata m = SiteMetadata.parse(value.toString());
            toRecordMap.put(m.nb, m);
            if (toRecordMap.size() > 10) {
                toRecordMap.remove(toRecordMap.firstKey());
            }
        }

        @Override
        protected void cleanup(Context context) {
            toRecordMap.forEach((key, value) -> {
                try {
                    context.write(new Text(value.site), value);
                } catch (Exception ignored) {
                }
            });
        }
    }

    public static class JoinTop10 extends Reducer<Text, SiteMetadata, Text, SiteMetadata> {

        private TreeMap<Integer, SiteMetadata> toRecordMap = new TreeMap<>();

        @Override
        public void reduce(Text key, Iterable<SiteMetadata> values, Context cont) {
            values.forEach(siteMetadata -> {
                toRecordMap.put(siteMetadata.nb, siteMetadata);
                if (toRecordMap.size() > 10) {
                    toRecordMap.remove(toRecordMap.firstKey());
                }
            });
            toRecordMap.descendingMap().forEach((nb, meta) -> {
                try {
                    cont.write(new Text(meta.site), meta);
                } catch (Exception ignored) {
                }
            });
        }
    }
}
