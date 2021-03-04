import data.SiteMetadata;
import edu.cmu.lemurproject.WarcFileInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class Driver {
    public static void main(String[] args) throws Exception {
        Job conf1 = Job.getInstance( new Configuration(), "sitecount" );
        conf1.setJarByClass(GetMetadata.class);

        conf1.setOutputKeyClass(Text.class);
        conf1.setOutputValueClass(SiteMetadata.class);

        conf1.setMapperClass(GetMetadata.WarcToMetadata.class);

        conf1.setCombinerClass(GetMetadata.JoinMetadata.class);
        conf1.setReducerClass(GetMetadata.JoinMetadata.class);

        conf1.setInputFormatClass(WarcFileInputFormat.class);
        conf1.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf1, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf1, new Path(args[1]));

        Date d = new Date();
        conf1.waitForCompletion(true); // submit and wait
        double timeTaken = (new Date().getTime() - d.getTime()) / 1000.;
        System.out.println("Time taken: " + timeTaken + "s");

        /*
        Job conf2 = Job.getInstance( new Configuration(), "filter top 10" );
        conf2.setJarByClass(GetMetadata.class);

        conf2.setOutputKeyClass(Text.class);
        conf2.setOutputValueClass(data.SiteMetadata.class);

        conf2.setMapperClass(Filter10Sites.MetadataToLocalTop10.class);
        conf2.setCombinerClass(Filter10Sites.JoinTop10.class);
        conf2.setReducerClass(Filter10Sites.JoinTop10.class);
        conf2.setNumReduceTasks(1);

        conf2.setInputFormatClass(TextInputFormat.class);
        conf2.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf2, new Path(args[0] + "_temp"));
        FileOutputFormat.setOutputPath(conf2, new Path(args[0]));

        conf2.waitForCompletion(true); // submit and wait
        */
    }
}
