package com.aco;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class preMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
	
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
//		System.out.println("=================================");
//    	System.out.println("preMap");
//    	System.out.println("=================================");
		output.collect(new Text("Collect"), value);
	}
}
