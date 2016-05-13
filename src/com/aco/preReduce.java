package com.aco;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class preReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
	protected static int count = FirstServlet.iternum;
	public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		System.out.println("previous Reduce");
		while (values.hasNext()) {
			//split values into <iter, iter_weight, iter_pathlength>
			String[] value = values.next().toString().split("\t");
			String iter = value[0];
			String iter_weight = value[1];
			String iter_pathlength = value[2];
			String k = String.valueOf(count);
			String v = iter+"\t"+iter_weight+"\t"+iter_pathlength;
			count++;
			output.collect(new Text(k), new Text(v));
		}
	}
}
