package com.aco;


import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

public class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
	private static Double MAXp;
	private static String MAXid;
	private static String MAXiter;
	private static int MAXiter_weight;
	private static int MAXiter_pathlength;
	private static int sumWeight;
	private static int sumPathlength;
	protected static String iter_buffer = "";
	protected static String id_buffer = "";
	protected static Double[][] pheromone;
	
	Reduce(){
		MAXp = 0.0;
		MAXid = "";
		MAXiter = "";
		MAXiter_weight = 0;
		MAXiter_pathlength = 0;
	}
	
	public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		//System.out.println("Reduce");
//		System.out.println("=================================");
//    	System.out.println("reduce");
//    	System.out.println("=================================");
		while (values.hasNext()) {
			//split values into <probability, iterID, iter, iter_wight, iter_pathlength>
			String[] value = values.next().toString().split("\t");
			Double P = Double.parseDouble(value[0]);
			String iterID = value[1];
			String iter = value[2];
			int iter_weight = Integer.valueOf(value[3]);
			int iter_pathlength = Integer.valueOf(value[4]);
			//record the MAX probability and iter_info
			if( P > MAXp ){
				MAXp = P;
				MAXid = iterID;
				MAXiter = iter;
				MAXiter_weight = iter_weight;
				MAXiter_pathlength = iter_pathlength;
			}
		}
		//filter the duplicated POIs
		boolean[] mark = new boolean[FirstServlet.POInum];
		for( int i = 0; i < mark.length; i++ )
			mark[i] = false;
		if(iter_buffer != ""){
			String[] selected_iter = Reduce.iter_buffer.split("\n");
			for(int i = 0; i < selected_iter.length; i++){
				//System.out.println("No."+i+" selected it: "+selected_iter[i]);
				String[] selected_POIs = selected_iter[i].split(":");
				for( int p = 0; p < selected_POIs.length; p++ ){
					mark[Integer.parseInt(selected_POIs[p])] = true;
				}
			}
		}
		String[] POIs = MAXiter.split(":");
		String temp = "";
		for(int i = 0; i < POIs.length; i++){
			if(mark[Integer.parseInt(POIs[i])] == true){
				System.out.println("delete POI: "+POIs[i]);
			}
			else{
				if( i == 0 )
					temp = POIs[i];
				else
					temp = temp+":"+POIs[i];
			}
		}
		MAXiter = temp;
		//record recently selected iter into buffer
		if( iter_buffer == "" ){
			iter_buffer = MAXiter;
			id_buffer = MAXid;
		}
		else{
			iter_buffer = iter_buffer+"\n"+MAXiter;
			id_buffer = id_buffer+"\t"+MAXid;
		}
		//compute the sum weight and pathlength
		sumWeight = sumWeight + MAXiter_weight;
		sumPathlength = sumPathlength + MAXiter_pathlength;
		System.out.println("iter_buffer: "+iter_buffer);
		System.out.println("sumWeight =  "+sumWeight+" ,MAXiter_weight = "+MAXiter_weight);
		System.out.println("sumPathlength =  "+sumPathlength+" ,MAXiter_pathlength = "+MAXiter_pathlength);
		//collect
		output.collect(new Text(iter_buffer+"\n"), new Text(sumWeight+"\t"+sumPathlength));
		
		//update pheromone
		Reduce.pheromone = Map.pheromone.clone();
		Double pher = 0.0;
		pher = 1/Double.valueOf(MAXiter_pathlength);
		System.out.println("pher: " + pher);
		String[] ITERs = id_buffer.split("\t");
		if(ITERs.length > 1){
			for( int i = 0; i < ITERs.length-1; i++ )
				pheromone[Integer.parseInt(ITERs[i])][Integer.parseInt(ITERs[i+1])] = pher;
		}
			
	}/*end reduce fun*/
}/*end Reduce class*/
