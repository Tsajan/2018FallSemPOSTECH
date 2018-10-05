import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class InvertedIndex {

	public static class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, Text> {
		
		//file ko naam nikalna euta variable delcare garr
		Text fileName = new Text();
		
		//setup method le implicitly file ko naam nikalna set garyo
		public void setup(Mapper.Context context) {
			fileName.set(((FileSplit) context.getInputSplit()).getPath().getName());
		}
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
  		{
	  			//content lai string ma convert garyo ani same case banayo ani punctuation hatayo
	  			String line = value.toString();
	  			line = line.toLowerCase();
	  			for (String word : line.split("\\W+"))
	  			{
	      			if (word.length() > 0)
	      			{
	    	  			context.write(new Text(word), fileName);
	      			}
	      			
	  			}
		}
		
	}

	public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
		public void reduce(Text term, Iterable<Text> docIDs, Context context) throws IOException, InterruptedException {

			StringBuilder documents = new StringBuilder();
			boolean first = true;

			for (Text docID : docIDs) {
				if (!first)
					documents.append(",");
				else
					first = false;

				documents.append(docID.toString());
			}
			context.write(term, new Text(documents.toString()));
		}
	}
	

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Inverted Index");

		job.setJarByClass(InvertedIndex.class);
		job.setMapperClass(InvertedIndexMapper.class);
		job.setReducerClass(InvertedIndexReducer.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);		
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
