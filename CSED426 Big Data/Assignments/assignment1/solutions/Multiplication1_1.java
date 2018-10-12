import java.io.IOException;
import java.util.StringTokenizer;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;


public class Multiplication1_1 {

	// Complete the Matrix1_1_Mapper class. 
	// Definitely, Generic type (Text, Text, Text, Text) must not be modified
	
	// Optional, you can add and use new methods in this class
	// Optional, you can use both 'setup' and 'cleanup' function, or either of them, or none of them.

	public static class Matrix1_1_Mapper extends Mapper<Text, Text, Text, Text> {
		// Optional, Using, Adding, Modifying and Deleting variable is up to you
		int n_first_rows = 0;
		int n_first_cols = 0;
		int n_second_cols = 0;
		
		// Optional, Utilizing 'setup' function or not is up to you
		protected void setup(Context context) throws IOException, InterruptedException {
			n_first_rows = context.getConfiguration().getInt("n_first_rows", 0);
			n_first_cols = context.getConfiguration().getInt("n_first_cols", 0);
			n_second_cols = context.getConfiguration().getInt("n_second_cols", 0);
		}
				
		// Definitely, parameter type and name (Text matrix, Text entry, Context context) must not be modified
		public void map(Text matrix, Text entry, Context context) throws IOException, InterruptedException {
			// Implement map function.
			//Output key and value of map function
			Text newKey = new Text();
			Text newValue = new Text();


			String[] posVal = entry.toString().split(",");

			if(matrix.toString().equals("a")) {
				for(int i=0; i < n_second_cols; i++) {
					newKey.set(posVal[0] + "," + i);
					newValue.set(matrix.toString() + "," + posVal[1] + "," + posVal[2]);
					context.write(newKey, newValue);
				}
			}

			else if(matrix.toString().equals("b")) {
				for(int i=0; i < n_first_rows; i++) {
					newKey.set(i + "," + posVal[1]);
					newValue.set(matrix.toString() + "," + posVal[0] + "," + posVal[2]);
					context.write(newKey, newValue);
				}
			}
		}
	}

	// Complete the Matrix1_1_Reducer class. 	
	// Definitely, Generic type (Text, Text, Text, Text) must not be modified
	// Definitely, Output format and values must be the same as given sample output 
	
	// Optional, you can use both 'setup' and 'cleanup' function, or either of them, or none of them.
	// Optional, you can add and use new methods in this class
	public static class Matrix1_1_Reducer extends Reducer<Text, Text, Text, Text> {
		// Optional, Using, Adding, Modifying and Deleting variable is up to you
		int n_first_rows = 0;
		int n_first_cols = 0;
		int n_second_cols = 0;
		
		
		// Optional, Utilizing 'setup' function or not is up to you
		protected void setup(Context context) throws IOException, InterruptedException {
			n_first_rows = context.getConfiguration().getInt("n_first_rows", 0);
			n_first_cols = context.getConfiguration().getInt("n_first_cols", 0);
			n_second_cols = context.getConfiguration().getInt("n_second_cols", 0);
		}
		
		// Definitely, parameters type (Text, Iterable<Text>, Context) must not be modified
		// Optional, parameters name (key, values, context) can be modified
		public void reduce(Text entry, Iterable<Text> entryComponents, Context context) throws IOException, InterruptedException {
			// Implement reduce function.
			String[] entRec;
			ArrayList<Entry<Integer,Integer>> aMatEntry = new ArrayList<Entry<Integer,Integer>>();
			ArrayList<Entry<Integer,Integer>> bMatEntry = new ArrayList<Entry<Integer,Integer>>();

			for(Text e: entryComponents) {
				//split the output value received from Map function
				entRec = e.toString().split(",");
				if(entRec[0].equals("a")) {
					aMatEntry.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(entRec[1]), Integer.parseInt(entRec[2])));
				} else if(entRec[0].equals("b")){
					bMatEntry.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(entRec[1]), Integer.parseInt(entRec[2])));
				}
			}

			int i, k;
			//value to be output by the multiplication i.e. (i, k, cik)
			Text newValue = new Text();
			int a_ij, b_jk;
			int res = 0;
			for(Entry<Integer, Integer> a: aMatEntry) {
				i = a.getKey();
				a_ij = a.getValue();
				for(Entry<Integer, Integer> b: bMatEntry) {
					k = b.getKey();
					b_jk = b.getValue();
					if(i == k) {
						res += a_ij * b_jk;
					}
					//newValue.set(i + "," + k + "," + Integer.toString(a_ij * b_jk));
				}
				
			}
			newValue.set(Integer.toString(res));
			context.write(entry, newValue);

		}
	}
	
	// Definitely, Main function must not be modified 
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Matrix Multiplication1_1");

		job.setJarByClass(Multiplication1_1.class);
		job.setMapperClass(Matrix1_1_Mapper.class);
		job.setReducerClass(Matrix1_1_Reducer.class);

		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.getConfiguration().setInt("n_first_rows", Integer.parseInt(args[2]));
		job.getConfiguration().setInt("n_first_cols", Integer.parseInt(args[3]));
		job.getConfiguration().setInt("n_second_cols", Integer.parseInt(args[4]));
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}