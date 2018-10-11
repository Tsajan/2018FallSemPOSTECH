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


public class Multiplication2 {
	// Complete the Matrix2_Mapper class. 
	// Definitely, Generic type (Text, Text, Text, Text) must not be modified
	
	// Optional, you can use both 'setup' and 'cleanup' function, or either of them, or none of them.
	// Optional, you can add and use new methods in this class

	public static class Matrix2_Mapper extends Mapper<Text, Text, Text, Text> {
		// Optional, Using, Adding, Modifying and Deleting variable is up to you
		int result_rows = 0;
		int result_columns = 0;
		int n_first_cols = 0;
		int n_second_cols = 0;
		int[][] aMat, bMat;
		boolean preCalculated = false;

		// Optional, Utilizing 'setup' function or not is up to you
		protected void setup(Context context) throws IOException, InterruptedException {

			result_rows = context.getConfiguration().getInt("n_first_rows", 0);
			n_first_cols = context.getConfiguration().getInt("n_first_cols", 0);
			n_second_cols = context.getConfiguration().getInt("n_second_cols", 0);
			result_columns = context.getConfiguration().getInt("n_third_cols", 0);

			aMat = new int[result_rows][n_first_cols];
			bMat = new int[n_first_cols][n_second_cols];

		}

		// Definitely, parameter type and name (Text matrix, Text entry, Context context) must not
		// be modified
		public void map(Text matrix, Text entry, Context context) throws IOException, InterruptedException {
			// Implement map function.

			String[] rec = entry.toString().split(",");
			if (matrix.toString().equals("c")) {
				if (!preCalculated) {
					for (int i = 0; i < result_rows; i++) {
						for (int k = 0; k < n_second_cols; k++) {
							int sum = 0;
							for (int j = 0; j < n_first_cols; j++) {
								sum += aMat[i][j] * bMat[j][k];
							}
							for (int l=0; l<result_columns; l++) {
								//put an indicator x to the value and we assume it is the product of a and b
								context.write(new Text(i + "," + l), new Text("x," + k + "," + sum));
							}
						}
					}
					preCalculated = true;
				}

				for (int l=0; l<result_rows; l++) {
					//put an indicator c to the value and we assume it is the elements from matrix c
					context.write(new Text(l + "," + rec[1]), new Text("c," + rec[0] + "," + rec[2]));
				}

			} 
			
			else if(matrix.toString().equals("a")) {
				aMat[Integer.parseInt(rec[0])][Integer.parseInt(rec[1])] = Integer.parseInt(rec[2]);
			} 
			
			else if(matrix.toString().equals("b")) {
				bMat[Integer.parseInt(rec[0])][Integer.parseInt(rec[1])] = Integer.parseInt(rec[2]);
			}
		}

	}


	// Complete the Matrix2_Reducer class. 	
	// Definitely, Generic type (Text, Text, Text, Text) must not be modified
	// Definitely, Output format and values must be the same as given sample output 
	
	// Optional, you can use both 'setup' and 'cleanup' function, or either of them, or none of them.
	// Optional, you can add and use new methods in this class
	public static class Matrix2_Reducer extends Reducer<Text, Text, Text, Text> {
		// Optional, Using, Adding, Modifying and Deleting variable is up to you
		int result_rows = 0;
		int result_columns = 0;
		int n_first_cols = 0;
		int n_second_cols = 0;

		// Optional, Utilizing 'setup' function or not is up to you
		protected void setup(Context context) throws IOException, InterruptedException {
			result_rows = context.getConfiguration().getInt("n_first_rows", 0);
			n_first_cols = context.getConfiguration().getInt("n_first_cols", 0);
			n_second_cols = context.getConfiguration().getInt("n_second_cols", 0);
			result_columns = context.getConfiguration().getInt("n_third_cols", 0);
		}

		// Definitely, parameters type (Text, Iterable<Text>, Context) must not be modified		
		// Optional, parameters name (key, values, context) can be modified
		public void reduce(Text entry, Iterable<Text> entryComponents, Context context) throws IOException, InterruptedException {
			// Implement reduce function.
			String[] entRec;
			ArrayList<Entry<Integer,Integer>> cMatEntry = new ArrayList<Entry<Integer,Integer>>();
			ArrayList<Entry<Integer,Integer>> xMatEntry = new ArrayList<Entry<Integer,Integer>>();

			for(Text e: entryComponents) {
				//split the output value received from Map function
				entRec = e.toString().split(",");
				if(entRec[0].equals("c")) {
					cMatEntry.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(entRec[1]), Integer.parseInt(entRec[2])));
				} else if(entRec[0].equals("x")){
					xMatEntry.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(entRec[1]), Integer.parseInt(entRec[2])));
				}
			}

			int i, k;
			Text newValue = new Text();
			int x_ik, c_kl;
			int res = 0;
			for(Entry<Integer, Integer> xe: xMatEntry) {
				i = xe.getKey();
				x_ik = xe.getValue();
				for(Entry<Integer, Integer> c: cMatEntry) {
					k = c.getKey();
					c_kl = c.getValue();
					if(i == k) {
						res += x_ik * c_kl;
					}
				}
				
			}
			newValue.set(Integer.toString(res));
			context.write(entry, newValue);
		}
	}

	// Definitely, Main function must not be modified 
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Matrix Multiplication");

		job.setJarByClass(Multiplication2.class);
		job.setMapperClass(Matrix2_Mapper.class);
		job.setReducerClass(Matrix2_Reducer.class);

		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.getConfiguration().setInt("n_first_rows", Integer.parseInt(args[2]));
		job.getConfiguration().setInt("n_first_cols", Integer.parseInt(args[3]));
		job.getConfiguration().setInt("n_second_cols", Integer.parseInt(args[4]));
		job.getConfiguration().setInt("n_third_cols", Integer.parseInt(args[5]));

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}