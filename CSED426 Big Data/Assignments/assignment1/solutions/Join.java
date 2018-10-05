import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class Join {
	// Complete the JoinMapper class. 
	// Definitely, Generic type (LongWritable, Text, Text, Text) must not be modified
	public static class JoinMapper extends Mapper<LongWritable, Text, Text, Text> {
		// You have to use these variables to generalize the Join.
		String[] tableNames = new String[2];
		String first_table_name = null;
		int first_table_join_index;
		String second_table_name = null;
		int second_table_join_index;
		
		protected void setup(Context context) throws IOException, InterruptedException {
			// Don't change setup function
			
			//table_names is an argument passed to the program as third parameter and getStrings() will return an array of String and will be stored in variable tableNames
			tableNames = context.getConfiguration().getStrings("table_names");
			
			//first_table_join_index & second_table_join_index are passed as 4th and 5th parameter, else their default value is 0
			first_table_join_index = context.getConfiguration().getInt("first_table_join_index", 0);
			second_table_join_index = context.getConfiguration().getInt("second_table_join_index", 0);
			first_table_name = tableNames[0];
			second_table_name = tableNames[1];
		}
		
		public void map(LongWritable key, Text record, Context context) throws IOException, InterruptedException {
			//Implement map function
			Text newKey = new Text();
			Text newValue = new Text();
			//Take given value i.e. `Text record` to and convert it to string and split each record into attributes
			String rec = record.toString();
			String[] attr = rec.split(",");

			//newKey should be hard-coded to attr[1] 
			//because this position of the attributes contain the join column for both tables
			newKey = new Text(attr[1]);

			//newValue should be the record
			newValue = new Text(rec);
			context.write(newKey, newValue);
		}
	}

	// Don't change (key, value) types
	public static class JoinReducer extends Reducer<Text, Text, Text, Text> {
		String[] tableNames = new String[2];
		String first_table_name = null;
		int first_table_join_index;
		String second_table_name = null;
		int second_table_join_index;

		Text joinTable = new Text();

		protected void setup(Context context) throws IOException, InterruptedException {
			// Similar to Mapper Class
			tableNames = context.getConfiguration().getStrings("table_names");
			first_table_join_index = context.getConfiguration().getInt("first_table_join_index", 0);
			second_table_join_index = context.getConfiguration().getInt("second_table_join_index", 0);
			first_table_name = tableNames[0];
			second_table_name = tableNames[1];

		}
		
		public void reduce(Text order_id, Iterable<Text> records, Context context) throws IOException, InterruptedException {
			// Implement reduce function
			// You can see form of new (key, value) pair in sample output file on server.
			// You can use Array or List or other Data structure for 'cache'.

			//create two ArrayList<String> to contain individual table records
			ArrayList<String> firstTableRecs = new ArrayList<String>();
			ArrayList<String> secondTableRecs = new ArrayList<String>();

			//strip the inverted commas from order_id
			String oid = order_id.toString();
			oid = oid.replaceAll("^\"|\"$", "");
			for(Text rec: records) {
				String[] attrSet = rec.toString().split(",");
				if(attrSet != null && (attrSet[0].equals("\"" + first_table_name + "\""))) {
					firstTableRecs.add(rec.toString());
				}
				if(attrSet != null && (attrSet[0].equals("\"" + second_table_name + "\""))) {
					secondTableRecs.add(rec.toString());
				}
			}
			for(int i=0; i < firstTableRecs.size(); i++) {
				for(int j=0; j < secondTableRecs.size(); j++) {
					String temp = firstTableRecs.get(i) + "," + secondTableRecs.get(j);
					joinTable.set(temp);
					context.write(new Text(oid), joinTable);
				}
			}
			

		}
	}

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Table Join");

		job.setJarByClass(Join.class);
		job.setMapperClass(JoinMapper.class);
		job.setReducerClass(JoinReducer.class);

		job.setInputFormatClass(TextInputFormat.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
	    
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.getConfiguration().setStrings("table_names", args[2]);
		job.getConfiguration().setInt("first_table_join_index", Integer.parseInt(args[3]));
		job.getConfiguration().setInt("second_table_join_index", Integer.parseInt(args[4]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
