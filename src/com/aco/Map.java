package com.aco;


import java.io.IOException;
import java.text.NumberFormat;
import java.util.Random;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

public class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
	protected static int[][] map_cost = {{0, 81, 37, 64, 70, 83, 36, 57, 92, 84, 9, 37, 37, 79, 21, 25, 25, 92, 11, 17, 3, 30, 63, 65, 63, 40, 30, 5, 65, 2, 31, 14, 17, 22, 34, 18, 24, 100, 52, 22, 10, 69, 34, 26, 88, 15, 11, 28, 31, 29, },
		{81, 0, 23, 73, 65, 80, 41, 56, 7, 43, 51, 4, 33, 47, 81, 12, 90, 6, 23, 45, 15, 57, 55, 5, 42, 7, 74, 53, 45, 83, 21, 36, 21, 13, 47, 21, 36, 34, 50, 9, 58, 34, 7, 22, 31, 1, 54, 51, 64, 68, },
		{37, 23, 0, 29, 69, 4, 69, 96, 9, 1, 57, 12, 88, 99, 53, 80, 27, 16, 83, 61, 2, 57, 66, 49, 7, 31, 61, 50, 27, 78, 45, 68, 73, 78, 20, 76, 46, 6, 8, 74, 59, 38, 19, 34, 69, 24, 85, 26, 70, 71, },
		{64, 73, 29, 0, 24, 100, 61, 87, 50, 13, 15, 83, 80, 33, 94, 75, 86, 21, 3, 76, 51, 85, 39, 62, 46, 7, 36, 46, 73, 91, 69, 89, 65, 53, 85, 19, 31, 68, 7, 47, 85, 32, 57, 41, 64, 70, 33, 89, 96, 31, },
		{70, 65, 69, 24, 0, 89, 12, 64, 74, 14, 72, 48, 58, 98, 93, 48, 31, 67, 17, 47, 63, 43, 99, 12, 69, 27, 12, 36, 13, 5, 78, 24, 10, 22, 38, 40, 60, 30, 43, 4, 64, 69, 55, 37, 75, 65, 82, 79, 2, 45, },
		{83, 80, 4, 100, 89, 0, 57, 43, 82, 74, 92, 20, 38, 72, 51, 90, 44, 87, 69, 88, 34, 78, 98, 41, 54, 12, 4, 29, 40, 77, 92, 38, 22, 7, 66, 47, 84, 85, 16, 59, 45, 52, 18, 17, 10, 97, 87, 67, 61, 83, },
		{36, 41, 69, 61, 12, 57, 0, 67, 93, 21, 53, 6, 23, 76, 73, 36, 77, 84, 78, 18, 30, 75, 14, 96, 11, 89, 77, 91, 97, 40, 14, 36, 46, 50, 27, 37, 11, 84, 24, 64, 54, 18, 2, 54, 35, 70, 42, 86, 60, 46, },
		{57, 56, 96, 87, 64, 43, 67, 0, 47, 94, 57, 62, 64, 69, 43, 65, 43, 33, 52, 67, 76, 51, 24, 23, 26, 73, 99, 81, 31, 59, 31, 67, 51, 57, 20, 95, 27, 44, 4, 9, 1, 39, 91, 59, 51, 88, 59, 52, 76, 91, },
		{92, 7, 9, 50, 74, 82, 93, 47, 0, 53, 77, 46, 70, 53, 29, 58, 22, 98, 4, 44, 25, 29, 15, 94, 88, 50, 61, 95, 38, 47, 85, 89, 26, 65, 26, 24, 98, 88, 63, 39, 97, 15, 40, 80, 10, 8, 96, 59, 41, 41, },
		{84, 43, 1, 13, 14, 74, 21, 94, 53, 0, 86, 55, 77, 99, 33, 96, 10, 57, 59, 100, 73, 36, 53, 97, 22, 42, 3, 86, 82, 33, 36, 66, 78, 54, 35, 89, 26, 41, 89, 64, 73, 19, 41, 43, 68, 1, 99, 93, 80, 6, },
		{9, 51, 57, 15, 72, 92, 53, 57, 77, 86, 0, 61, 76, 57, 32, 27, 13, 85, 11, 47, 27, 97, 78, 48, 27, 82, 78, 95, 74, 19, 53, 66, 50, 19, 52, 34, 42, 61, 7, 52, 89, 42, 52, 92, 84, 77, 6, 50, 99, 24, },
		{37, 4, 12, 83, 48, 20, 6, 62, 46, 55, 61, 0, 22, 28, 89, 79, 52, 4, 27, 19, 60, 98, 51, 25, 87, 89, 61, 6, 20, 78, 34, 25, 70, 64, 23, 63, 80, 54, 100, 58, 42, 98, 82, 13, 21, 36, 65, 63, 30, 10, },
		{37, 33, 88, 80, 58, 38, 23, 64, 70, 77, 76, 22, 0, 10, 19, 63, 9, 92, 23, 99, 16, 77, 15, 4, 3, 60, 35, 17, 75, 40, 39, 68, 68, 56, 63, 32, 47, 29, 80, 16, 73, 39, 66, 60, 36, 52, 2, 16, 4, 53, },
		{79, 47, 99, 33, 98, 72, 76, 69, 53, 99, 57, 28, 10, 0, 65, 22, 41, 50, 21, 71, 19, 76, 12, 57, 53, 57, 51, 90, 70, 35, 69, 17, 6, 9, 76, 41, 93, 79, 7, 7, 53, 4, 81, 37, 12, 45, 80, 67, 86, 31, },
		{21, 81, 53, 94, 93, 51, 73, 43, 29, 33, 32, 89, 19, 65, 0, 54, 89, 52, 29, 76, 40, 3, 51, 43, 18, 13, 75, 20, 80, 45, 26, 78, 26, 62, 4, 47, 78, 28, 51, 27, 44, 7, 65, 100, 58, 73, 14, 3, 50, 80, },
		{25, 12, 80, 75, 48, 90, 36, 65, 58, 96, 27, 79, 63, 22, 54, 0, 49, 62, 97, 91, 49, 10, 78, 27, 66, 39, 18, 5, 55, 54, 99, 98, 92, 89, 25, 31, 14, 65, 17, 82, 97, 1, 96, 93, 21, 78, 47, 26, 86, 39, },
		{25, 90, 27, 86, 31, 44, 77, 43, 22, 10, 13, 52, 9, 41, 89, 49, 0, 69, 86, 70, 9, 32, 66, 17, 18, 69, 90, 10, 91, 10, 100, 33, 74, 13, 29, 19, 15, 26, 78, 77, 97, 91, 30, 85, 83, 96, 98, 45, 90, 10, },
		{92, 6, 16, 21, 67, 87, 84, 33, 98, 57, 85, 4, 92, 50, 52, 62, 69, 0, 53, 41, 88, 32, 92, 92, 63, 19, 37, 1, 73, 41, 16, 44, 22, 17, 18, 90, 48, 73, 50, 29, 11, 38, 40, 37, 45, 39, 83, 18, 100, 51, },
		{11, 23, 83, 3, 17, 69, 78, 52, 4, 59, 11, 27, 23, 21, 29, 97, 86, 53, 0, 58, 91, 46, 92, 55, 74, 15, 64, 48, 17, 37, 67, 10, 57, 20, 89, 66, 97, 33, 86, 69, 28, 73, 2, 39, 47, 81, 18, 23, 13, 28, },
		{17, 45, 61, 76, 47, 88, 18, 67, 44, 100, 47, 19, 99, 71, 76, 91, 70, 41, 58, 0, 32, 99, 20, 47, 90, 82, 33, 57, 78, 93, 42, 15, 61, 94, 59, 33, 29, 1, 2, 57, 55, 25, 4, 87, 33, 79, 21, 99, 36, 69, },
		{3, 15, 2, 51, 63, 34, 30, 76, 25, 73, 27, 60, 16, 19, 40, 49, 9, 88, 91, 32, 0, 93, 47, 10, 38, 32, 8, 30, 52, 91, 14, 83, 2, 36, 90, 54, 96, 92, 68, 46, 19, 47, 1, 66, 85, 39, 30, 54, 44, 68, },
		{30, 57, 57, 85, 43, 78, 75, 51, 29, 36, 97, 98, 77, 76, 3, 10, 32, 32, 46, 99, 93, 0, 62, 22, 25, 44, 29, 48, 37, 63, 5, 10, 27, 89, 17, 7, 45, 91, 75, 6, 28, 46, 2, 27, 16, 2, 68, 98, 37, 2, },
		{63, 55, 66, 39, 99, 98, 14, 24, 15, 53, 78, 51, 15, 12, 51, 78, 66, 92, 92, 20, 47, 62, 0, 22, 2, 82, 32, 94, 30, 76, 100, 89, 58, 58, 88, 69, 24, 82, 91, 72, 60, 17, 38, 69, 5, 50, 2, 90, 73, 52, },
		{65, 5, 49, 62, 12, 41, 96, 23, 94, 97, 48, 25, 4, 57, 43, 27, 17, 92, 55, 47, 10, 22, 22, 0, 79, 87, 60, 81, 49, 86, 59, 73, 97, 76, 19, 56, 50, 87, 60, 27, 88, 25, 95, 45, 44, 23, 96, 17, 42, 33, },
		{63, 42, 7, 46, 69, 54, 11, 26, 88, 22, 27, 87, 3, 53, 18, 66, 18, 63, 74, 90, 38, 25, 2, 79, 0, 36, 2, 60, 57, 26, 88, 24, 10, 55, 33, 19, 91, 83, 34, 79, 44, 98, 46, 5, 84, 23, 3, 43, 52, 72, },
		{40, 7, 31, 7, 27, 12, 89, 73, 50, 42, 82, 89, 60, 57, 13, 39, 69, 19, 15, 82, 32, 44, 82, 87, 36, 0, 88, 18, 59, 78, 7, 75, 47, 10, 8, 72, 39, 44, 14, 88, 12, 79, 93, 78, 9, 46, 39, 33, 57, 77, },
		{30, 74, 61, 36, 12, 4, 77, 99, 61, 3, 78, 61, 35, 51, 75, 18, 90, 37, 64, 33, 8, 29, 32, 60, 2, 88, 0, 82, 27, 20, 23, 78, 5, 23, 1, 3, 99, 40, 19, 50, 9, 18, 31, 45, 77, 43, 59, 87, 54, 94, },
		{5, 53, 50, 46, 36, 29, 91, 81, 95, 86, 95, 6, 17, 90, 20, 5, 10, 1, 48, 57, 30, 48, 94, 81, 60, 18, 82, 0, 92, 41, 4, 55, 30, 69, 84, 36, 27, 9, 30, 7, 50, 8, 94, 97, 57, 94, 64, 33, 6, 6, },
		{65, 45, 27, 73, 13, 40, 97, 31, 38, 82, 74, 20, 75, 70, 80, 55, 91, 73, 17, 78, 52, 37, 30, 49, 57, 59, 27, 92, 0, 88, 42, 4, 31, 32, 17, 49, 64, 15, 11, 15, 73, 66, 49, 29, 1, 60, 57, 80, 57, 20, },
		{2, 83, 78, 91, 5, 77, 40, 59, 47, 33, 19, 78, 40, 35, 45, 54, 10, 41, 37, 93, 91, 63, 76, 86, 26, 78, 20, 41, 88, 0, 95, 38, 49, 1, 96, 69, 49, 73, 15, 71, 35, 87, 8, 20, 72, 85, 86, 40, 67, 95, },
		{31, 21, 45, 69, 78, 92, 14, 31, 85, 36, 53, 34, 39, 69, 26, 99, 100, 16, 67, 42, 14, 5, 100, 59, 88, 7, 23, 4, 42, 95, 0, 12, 95, 5, 52, 56, 83, 9, 25, 83, 61, 72, 47, 79, 98, 89, 88, 93, 39, 41, },
		{14, 36, 68, 89, 24, 38, 36, 67, 89, 66, 66, 25, 68, 17, 78, 98, 33, 44, 10, 15, 83, 10, 89, 73, 24, 75, 78, 55, 4, 38, 12, 0, 6, 65, 26, 61, 27, 28, 39, 86, 32, 46, 64, 18, 89, 100, 45, 61, 69, 76, },
		{17, 21, 73, 65, 10, 22, 46, 51, 26, 78, 50, 70, 68, 6, 26, 92, 74, 22, 57, 61, 2, 27, 58, 97, 10, 47, 5, 30, 31, 49, 95, 6, 0, 38, 71, 79, 6, 93, 16, 87, 93, 23, 48, 17, 58, 54, 52, 98, 76, 92, },
		{22, 13, 78, 53, 22, 7, 50, 57, 65, 54, 19, 64, 56, 9, 62, 89, 13, 17, 20, 94, 36, 89, 58, 76, 55, 10, 23, 69, 32, 1, 5, 65, 38, 0, 44, 57, 69, 14, 79, 93, 68, 99, 68, 31, 8, 99, 22, 11, 88, 60, },
		{34, 47, 20, 85, 38, 66, 27, 20, 26, 35, 52, 23, 63, 76, 4, 25, 29, 18, 89, 59, 90, 17, 88, 19, 33, 8, 1, 84, 17, 96, 52, 26, 71, 44, 0, 58, 25, 7, 56, 31, 38, 77, 79, 46, 57, 15, 34, 19, 3, 44, },
		{18, 21, 76, 19, 40, 47, 37, 95, 24, 89, 34, 63, 32, 41, 47, 31, 19, 90, 66, 33, 54, 7, 69, 56, 19, 72, 3, 36, 49, 69, 56, 61, 79, 57, 58, 0, 72, 4, 98, 24, 56, 33, 72, 29, 7, 4, 87, 52, 27, 78, },
		{24, 36, 46, 31, 60, 84, 11, 27, 98, 26, 42, 80, 47, 93, 78, 14, 15, 48, 97, 29, 96, 45, 24, 50, 91, 39, 99, 27, 64, 49, 83, 27, 6, 69, 25, 72, 0, 24, 73, 53, 9, 5, 90, 31, 75, 11, 81, 48, 35, 76, },
		{100, 34, 6, 68, 30, 85, 84, 44, 88, 41, 61, 54, 29, 79, 28, 65, 26, 73, 33, 1, 92, 91, 82, 87, 83, 44, 40, 9, 15, 73, 9, 28, 93, 14, 7, 4, 24, 0, 43, 28, 59, 27, 40, 43, 34, 45, 45, 41, 50, 92, },
		{52, 50, 8, 7, 43, 16, 24, 4, 63, 89, 7, 100, 80, 7, 51, 17, 78, 50, 86, 2, 68, 75, 91, 60, 34, 14, 19, 30, 11, 15, 25, 39, 16, 79, 56, 98, 73, 43, 0, 49, 88, 44, 30, 11, 57, 71, 9, 58, 67, 98, },
		{22, 9, 74, 47, 4, 59, 64, 9, 39, 64, 52, 58, 16, 7, 27, 82, 77, 29, 69, 57, 46, 6, 72, 27, 79, 88, 50, 7, 15, 71, 83, 86, 87, 93, 31, 24, 53, 28, 49, 0, 93, 50, 89, 94, 41, 90, 22, 32, 81, 51, },
		{10, 58, 59, 85, 64, 45, 54, 1, 97, 73, 89, 42, 73, 53, 44, 97, 97, 11, 28, 55, 19, 28, 60, 88, 44, 12, 9, 50, 73, 35, 61, 32, 93, 68, 38, 56, 9, 59, 88, 93, 0, 7, 38, 31, 8, 95, 52, 57, 3, 6, },
		{69, 34, 38, 32, 69, 52, 18, 39, 15, 19, 42, 98, 39, 4, 7, 1, 91, 38, 73, 25, 47, 46, 17, 25, 98, 79, 18, 8, 66, 87, 72, 46, 23, 99, 77, 33, 5, 27, 44, 50, 7, 0, 77, 98, 19, 56, 71, 36, 17, 7, },
		{34, 7, 19, 57, 55, 18, 2, 91, 40, 41, 52, 82, 66, 81, 65, 96, 30, 40, 2, 4, 1, 2, 38, 95, 46, 93, 31, 94, 49, 8, 47, 64, 48, 68, 79, 72, 90, 40, 30, 89, 38, 77, 0, 91, 86, 76, 68, 28, 38, 8, },
		{26, 22, 34, 41, 37, 17, 54, 59, 80, 43, 92, 13, 60, 37, 100, 93, 85, 37, 39, 87, 66, 27, 69, 45, 5, 78, 45, 97, 29, 20, 79, 18, 17, 31, 46, 29, 31, 43, 11, 94, 31, 98, 91, 0, 62, 7, 17, 39, 67, 19, },
		{88, 31, 69, 64, 75, 10, 35, 51, 10, 68, 84, 21, 36, 12, 58, 21, 83, 45, 47, 33, 85, 16, 5, 44, 84, 9, 77, 57, 1, 72, 98, 89, 58, 8, 57, 7, 75, 34, 57, 41, 8, 19, 86, 62, 0, 49, 98, 11, 38, 27, },
		{15, 1, 24, 70, 65, 97, 70, 88, 8, 1, 77, 36, 52, 45, 73, 78, 96, 39, 81, 79, 39, 2, 50, 23, 23, 46, 43, 94, 60, 85, 89, 100, 54, 99, 15, 4, 11, 45, 71, 90, 95, 56, 76, 7, 49, 0, 2, 30, 79, 65, },
		{11, 54, 85, 33, 82, 87, 42, 59, 96, 99, 6, 65, 2, 80, 14, 47, 98, 83, 18, 21, 30, 68, 2, 96, 3, 39, 59, 64, 57, 86, 88, 45, 52, 22, 34, 87, 81, 45, 9, 22, 52, 71, 68, 17, 98, 2, 0, 30, 14, 46, },
		{28, 51, 26, 89, 79, 67, 86, 52, 59, 93, 50, 63, 16, 67, 3, 26, 45, 18, 23, 99, 54, 98, 90, 17, 43, 33, 87, 33, 80, 40, 93, 61, 98, 11, 19, 52, 48, 41, 58, 32, 57, 36, 28, 39, 11, 30, 30, 0, 54, 39, },
		{31, 64, 70, 96, 2, 61, 60, 76, 41, 80, 99, 30, 4, 86, 50, 86, 90, 100, 13, 36, 44, 37, 73, 42, 52, 57, 54, 6, 57, 67, 39, 69, 76, 88, 3, 27, 35, 50, 67, 81, 3, 17, 38, 67, 38, 79, 14, 54, 0, 69, },
		{29, 68, 71, 31, 45, 83, 46, 91, 41, 6, 24, 10, 53, 31, 80, 39, 10, 51, 28, 69, 68, 2, 52, 33, 72, 77, 94, 6, 20, 95, 41, 76, 92, 60, 44, 78, 76, 92, 98, 51, 6, 7, 8, 19, 27, 65, 46, 39, 69, 0, } };
	protected static Double[][] pheromone;
	protected static int[] query = FirstServlet.query.clone();
	private static int startA, seed,lastId, seedId;
	private static double selectP;
	private static boolean[] mark;
	
	Random rand = new Random();
	
	Map(){
		startA = 0;
		seed = rand.nextInt(50);
		seedId = rand.nextInt(preReduce.count);
		selectP = 0.0;
		mark = new boolean[FirstServlet.POInum];
		for( int i = 0; i < mark.length; i++ )
			mark[i] = false;
	}
	
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		//System.out.println("Map");
//		System.out.println("=================================");
//    	System.out.println("map");
//    	System.out.println("=================================");
		//split value
		String[] iter_info = value.toString().split("\t");
		int iterID = Integer.parseInt(iter_info[0]);
		String iter = iter_info[1];
		int iter_weight = Integer.parseInt(iter_info[2]);
		int iter_pathlength = Integer.parseInt(iter_info[3]);

		//startA
		String iter_buffer = Reduce.iter_buffer;
		String id_buffer = Reduce.id_buffer;
		if (id_buffer == ""){
			lastId = seedId;
		} else {
			String [] id_b = Reduce.id_buffer.split("\t");
			lastId = Integer.parseInt(id_b[id_b.length-1]);
		}
		if( iter_buffer == "" ){
			startA = seed;
		}
		else{
			String[] iter_b = Reduce.iter_buffer.split(",");
			String[] bPOIs = iter_b[iter_b.length-1].split(":");
			startA = Integer.valueOf(bPOIs[bPOIs.length-1]);
		}
		
		//load main pheromone
		Map.pheromone = FirstServlet.pheromone.clone();
		
		//compute match between query and iter
		int match = 0;
		String[] POIs = iter.split(":");
		for( int i = 0; i < POIs.length; i++ ){
			int j = 0;
			while( j < query.length ){
				if( POIs[i].equals(query[j]) ){
					match++;
					j = query.length;
				}
				j++;
			}
		}
		
		//compute duplicated rate
		int dul = 0, total = 0;
		Double rate = 0.0;
		if( Reduce.iter_buffer != "" ){
			String[] selected_iter = Reduce.iter_buffer.split("\n");
			for(int i = 0; i < selected_iter.length; i++){
//				System.out.println("No."+i+" selected it: "+selected_iter[i]);
				String[] selected_POIs = selected_iter[i].split(":");
				for( int p = 0; p < selected_POIs.length; p++ ){
					mark[Integer.parseInt(selected_POIs[p])-1] = true;
					total++;
				}
			}
		}
		for( int i = 0; i < POIs.length; i++ ){
			if( mark[Integer.parseInt(POIs[i])-1] == true ){
				dul++;
			}
		}
		//System.out.println("dul = " + dul + ", total = "+total);
		if( total == 0 )
			rate = 0.0;
		else
			rate = (double) (dul)/(double)(total);
		
		//compute probability
		Double P1 = 0.0, P2 = 0.0, P = 0.0;
		int iter_end = Integer.parseInt(POIs[POIs.length-1]);
		int iter_start = Integer.parseInt(POIs[0]);
		
		P1 = Double.valueOf(iter_weight)/Math.pow((map_cost[startA-1][iter_end-1]+rate), 1.5)*Math.pow(pheromone[lastId][iterID], 0.5);
		P2 = Double.valueOf(iter_weight)/Math.pow((map_cost[startA-1][iter_start-1]+rate), 1.5)*Math.pow(pheromone[lastId][iterID],0.5);
		if( P1 > P2 )
			P = P1;
		else
			P = P2;
		
		//random selectP
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		selectP = Double.valueOf(nf.format(rand.nextDouble()));
		
		//determine select or not
		if( dul < 2 ){
			String out = P+"\t"+iterID+"\t"+iter+"\t"+iter_weight+"\t"+iter_pathlength;
			output.collect(new Text("Done"), new Text(out));
		}
		
		/*if( P < selectP ){
			String out = P+"\t"+iter+"\t"+iter_weight+"\t"+iter_pathlength;
			output.collect(new Text("Done"), new Text(out));
		}
		else{
			//FileSystem.create("hdfs", new Path("iter/"), "");
		}*/
		
	}/*end map func*/
}/*end Map class*/
