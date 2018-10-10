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


public class Multiplication1_2 {
	// Complete the Matrix1_2_1_Mapper class. 
	// Definitely, Generic type (LongWritable, Text, Text, Text) must not be modified
	// Matrix1_2_1 _Mapper class handle the output data from Multiplication1_1 and use 'TextInputFileFormat' as InputFileFormat
		
	// Optional, you can use both 'setup' and 'cleanup' function, or either of them, or none of them.
	// Optional, you can add and use new methods in this class
	//handles the output of A X B matrix
	public static class Matrix1_2_1_Mapper extends Mapper<LongWritable, Text, Text, Text> {
		// Optional, Using, Adding, Modifying and Deleting variable is up to you
		int result_columns = 0;
		String newEntry = null;
		
		// Optional, Utilizing 'setup' function or not is up to you
		protected void setup(Context context) throws IOException, InterruptedException {
			result_columns = context.getConfiguration().getInt("n_third_cols", 0);
		}
		
		// Definitely, parameter type and name (LongWritable key, Text entry, Context context) must not be modified
		public void map(LongWritable key, Text entry, Context context) throws IOException, InterruptedException {
			// Implement map function.
			Text newKey = new Text();
			Text newValue = new Text();

			String keyVals[] = entry.toString().split("\t");
			String keyPos[] = keyVals[0].split(",");

			for(int i=0; i < result_columns; i++) {
				newKey.set(keyPos[0] + "," + i);
				//let us append an "x" to the value, just to say that this is matrix X
				newValue.set("x," + keyPos[1] + "," + keyVals[1]);
				context.write(newKey, newValue);
			}
		}
	}
	
	// Complete the Matrix1_2_2_Mapper class. 
	// Definitely, Generic type (Text, Text, Text, Text) must not be modified
	// Matrix1_2_2 _Mapper class handle the data from Matrix1_2 and use 'KeyValueTextInputFileFormat' as InputFileFormat
		
	// Optional, you can use both 'setup' and 'cleanup' function, or either of them, or none of them.
	// Optional, you can add and use new methods in this class
	//used for handling matrix c
	//output is of format
	// i,j \\tab\\ cij
	public static class Matrix1_2_2_Mapper extends Mapper<Text, Text, Text, Text> {
		// Optional, Using, Adding, Modifying and Deleting variable is up to you
		int result_rows = 0;
		String newEntry = null;
		
		// Optional, Utilizing 'setup' function or not is up to you
		protected void setup(Context context) throws IOException, InterruptedException {
			result_rows = context.getConfiguration().getInt("n_first_rows", 0);
		}
				
		public void map(Text matrix, Text entry, Context context) throws IOException, InterruptedException {
			// Impmelent map function.
			Text newKey = new Text();
			Text newValue = new Text();

			String[] record = entry.toString().split(",");
			for(int i=0; i < result_rows; i++) {
				newKey.set(i + "," + record[1]);
				newValue.set(matrix.toString() + "," + record[0] + "," + record[2]);
				context.write(newKey, newValue);
			}
		}
	}

	
	// Complete the Matrix1_2_Reducer class. 	
	// Definitely, Generic type (Text, Text, Text, Text) must not be modified
	// Definitely, Output format and values must be the same as given sample output 
	
	// Optional, you can use both 'setup' and 'cleanup' function, or either of them, or none of them.
	// Optional, you can add and use new methods in this class
	public static class Matrix1_2_Reducer extends Reducer<Text, Text, Text, Text> {
		// Optional, Using, Adding, Modifying and Deleting variable is up to you
		int n_first_rows = 0;
		int n_second_cols = 0;
		int n_third_cols = 0;
		
		// Optional, Utilizing 'setup' function or not is up to you
		protected void setup(Context context) throws IOException, InterruptedException {
			n_first_rows = context.getConfiguration().getInt("n_first_rows", 0);
			n_second_cols = context.getConfiguration().getInt("n_second_cols", 0);
			n_third_cols = context.getConfiguration().getInt("n_third_cols", 0);
		}
		
		// Definitely, parameters type (Text, Iterable<Text>, Context) must not be modified
		// Optional, parameters name (key, values, context) can be modified
		public void reduce(Text entry, Iterable<Text> entryComponents, Context context) throws IOException, InterruptedException {
			// Implement reduce function.

			String[] entRec;
			ArrayList<Entry<Integer, Integer>> xMatEntry = new ArrayList<Entry<Integer, Integer>>();
			ArrayList<Entry<Integer, Integer>> cMatEntry = new ArrayList<Entry<Integer, Integer>>();

			for(Text e: entryComponents) {
				//split the output value received from Map function
				entRec = e.toString().split(",");
				if(entRec[0].equals("c")) {
					cMatEntry.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(entRec[1]), Integer.parseInt(entRec[2])));
				} else if(entRec[0].equals("x")) {
					xMatEntry.add(new SimpleEntry<Integer, Integer>(Integer.parseInt(entRec[1]), Integer.parseInt(entRec[2])));
				}
			}
			int i, l;
			Text newValue = new Text();
			int x_ik, c_kl;
			int res=0;
			for(Entry<Integer, Integer> x: xMatEntry) {
				i = x.getKey();
				x_ik = x.getValue();
				for(Entry<Integer, Integer> c: cMatEntry) {
					l = c.getKey();
					c_kl = c.getValue();
					if(i==l) {
						res += x_ik * c_kl;
					}
				}
			}
			newValue.set(Integer.toString(res));
			context.write(entry, newValue);

		}
			
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "Matrix Multiplication1_1");
		//this portion does the matrix multiplication of matrix A and B
		job.setJarByClass(Multiplication1_1.class);
		job.setMapperClass(Multiplication1_1.Matrix1_1_Mapper.class);
		job.setReducerClass(Multiplication1_1.Matrix1_1_Reducer.class);

		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		//the output file of first job (A X B) will be written in the path of arg[2], 3rd argument
		//i.e. the output directory we wished to provide
		//thus the file part-r-00000 was created in the output folder
		FileOutputFormat.setOutputPath(job, new Path(args[2]));
		job.getConfiguration().setInt("n_first_rows", Integer.parseInt(args[3]));
		job.getConfiguration().setInt("n_first_cols", Integer.parseInt(args[4]));
		job.getConfiguration().setInt("n_second_cols", Integer.parseInt(args[5]));
		
		if(!job.waitForCompletion(true))
			return;
		
		
		Configuration conf2 = new Configuration();
		Job job2 = Job.getInstance(conf2, "Matrix Multiplication1_2");

		job2.setJarByClass(Multiplication1_2.class);
		//one of the inputs for job2 will be the data read from the output directory i.e. 3rd argument i.e. part-r-00000 from output, 
		//and it will be handled by Matrix1_2_1_Mapper class
		MultipleInputs.addInputPath(job2, new Path(args[2]), TextInputFormat.class, Matrix1_2_1_Mapper.class);
		//the other input for job2 will be the 2nd argument that we passed, i.e. the matrix C from file matrix1_2, 
		//and it will be handled by Matrix1_2_2 mapper
		MultipleInputs.addInputPath(job2, new Path(args[1]), KeyValueTextInputFormat.class, Matrix1_2_2_Mapper.class);
		//job2.setMapperClass(Matrix1_2_1_Mapper.class);							// Not needed
		job2.setReducerClass(Matrix1_2_Reducer.class);

		// job2.setInputFormatClass(KeyValueTextInputFormat.class);					// Not needed
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);

		//FileInputFormat.addInputPath(job2, new Path(args[2]));					// Not needed
		FileOutputFormat.setOutputPath(job2, new Path(args[2] + "//final"));
		
		job2.getConfiguration().setInt("n_first_rows", Integer.parseInt(args[3]));
		job2.getConfiguration().setInt("n_second_cols", Integer.parseInt(args[5]));
		job2.getConfiguration().setInt("n_third_cols", Integer.parseInt(args[6]));
		
		System.exit(job2.waitForCompletion(true) ? 0 : 1);
	}
}
