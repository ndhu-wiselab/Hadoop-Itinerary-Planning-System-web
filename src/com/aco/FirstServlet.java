package com.aco;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

public class FirstServlet extends HttpServlet {
	protected static int[] query = {1};/*query = { 1, 6, 2, 8, 4, 11, 26, 38, 44 };*/
	protected static int Kday = 1; /*Kday = 3*/
	protected static int POInum = 50;
	protected static int iternum;
	protected static Double[][] pheromone;
	private static final long serialVersionUID = 1L;
	
	public String getHTML() {
	      URL url;
	      HttpURLConnection conn;
	      BufferedReader rd;
	      String line;
	      String result = "";
	      try {
	         url = new URL("http://localhost:50075/webhdfs/v1/user/hadoop/ACO_output/part-00000?op=OPEN");
	         conn = (HttpURLConnection) url.openConnection();
	         conn.setRequestMethod("GET");
	         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	         while ((line = rd.readLine()) != null) {
	        	System.out.println();
	            result += line;
	            //改成result += "end"+line ???不然結果都黏在一起？
	         }
	         rd.close();
	      } catch (IOException e) {
	         e.printStackTrace();
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	      return result;
	   }
	
	private static class IntWritableDecreasingComparator extends IntWritable.Comparator {
		public int compare(WritableComparable a, WritableComparable b) {
			return -super.compare(a, b);
		}
		public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
			return -super.compare(b1, s1, l1, b2, s2, l2);
		}
	}
	
    @SuppressWarnings("deprecation")
	protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
    	//request.setCharacterEncoding("UTF-8");
    	System.out.println("=================================");
    	System.out.println("dopost");
    	System.out.println("=================================");
    	Reduce.iter_buffer="";
		/*get request*/
    	String[] req = new String[15];
    	Kday = Integer.parseInt(request.getParameter("Kday"));
    	System.out.println("Kay = " + Kday);
    	int [] query = this.query;
    	try {
    		req = request.getParameterValues("POI");
        	query = new int[req.length];
        	for( int i = 0; i < req.length; i++ )
        		query[i] = Integer.parseInt(req[i]);
        	for( int i = 0; i < query.length; i++ )
        		System.out.println(query[i]);
    	} catch(NullPointerException $e) {
    		
    	}
		/*Previous processing*/
		JobClient preclient = new JobClient();
		JobConf preconf = new JobConf(com.aco.FirstServlet.class);
		// TODO: specify output types
		preconf.setOutputKeyClass(Text.class);
		preconf.setOutputValueClass(Text.class);
		// TODO: specify input and output DIRECTORIES (not files)
		preconf.setInputFormat(TextInputFormat.class);
		preconf.setOutputFormat(TextOutputFormat.class);
		FileInputFormat.setInputPaths(preconf, new Path("hdfs://localhost:9000/user/hadoop/10round-9/part-00000"));
		FileOutputFormat.setOutputPath(preconf, new Path("hdfs://localhost:9000/user/hadoop/inputData"));
		// TODO: specify a mapper and reducer
		preconf.setMapperClass(preMap.class);
		preconf.setReducerClass(preReduce.class);
		preclient.setConf(preconf);
		try{
			Path indata = new Path("hdfs://localhost:9000/user/hadoop/inputData");
			if(indata.getFileSystem(preconf).exists(indata))
				indata.getFileSystem(preconf).delete(indata);
			JobClient.runJob(preconf);
		}catch(Exception e){
			e.printStackTrace();
		}
		iternum = preReduce.count;
		pheromone = new Double[iternum][iternum];
		/*Plan Stage*/
		for( int k = 0; k < Kday; k++ ){
			JobClient client = new JobClient();
			JobConf conf = new JobConf(com.aco.FirstServlet.class);
			// TODO: specify output types
			conf.setOutputKeyClass(Text.class);
			conf.setOutputValueClass(Text.class);
			// TODO: specify input and output DIRECTORIES (not files)
			conf.setInputFormat(TextInputFormat.class);
			conf.setOutputFormat(TextOutputFormat.class);
			FileInputFormat.setInputPaths(conf, new Path("hdfs://localhost:9000/user/hadoop/inputData"));
			FileOutputFormat.setOutputPath(conf, new Path("hdfs://localhost:9000/user/hadoop/ACO_output"));
			// TODO: specify a mapper and reducer
			conf.setMapperClass(Map.class);
			conf.setReducerClass(Reduce.class);
			client.setConf(conf);
			try {
				//upload pheromone
				FileSystem out = FileSystem.get(conf);
				for( int i = 0; i < pheromone.length; i++ ){
					for( int j = 0; j < pheromone[i].length; j++ ){
						pheromone[i][j] = 1.0;
					}
				}
				FileSystem.get(conf).delete(new Path("pheromone"));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out.create(new Path("pheromone"))));
				for( int i = 0; i < pheromone.length; i++ ){
					for( int j = 0; j < pheromone[i].length; j++ ){
						writer.write(pheromone[i][j]+" ");
					}writer.write("\n");
				}
				writer.close();
				out.close();
				//load pheromone
				FileSystem infs = FileSystem.get(conf);
				BufferedReader bReader = new BufferedReader(new InputStreamReader(infs.open(new Path("pheromone"))));
				String line = bReader.readLine();
				int count = 0;
				while(line!=null){
					String[] str_p = line.split(" ");
					for( int i = 0; i < str_p.length; i++ ){
						pheromone[count][i] = Double.parseDouble(str_p[i]);
					}
					count++;
					line = bReader.readLine();
				}
				bReader.close();
				infs.close();
				//ACO_output is exist or not
				Path aco = new Path("hdfs://localhost:9000/user/hadoop/ACO_output");
				if(aco.getFileSystem(conf).exists(aco))
					aco.getFileSystem(conf).delete(aco);
				//run job
				
				JobClient.runJob(conf);
				iternum = 0;
				//update pheromone
				FileSystem outfs = FileSystem.get(conf);
				FirstServlet.pheromone = Reduce.pheromone.clone();
				
				if( !outfs.exists(new Path("pheromone")) ){
					System.out.println("pheromone file not exsits!");
				}
				else{
					FileSystem.get(conf).delete(new Path("pheromone"));
					BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(outfs.create(new Path("pheromone"))));
					for( int i = 0; i < pheromone.length; i++ ){
						for( int j = 0; j < pheromone[i].length; j++ ){
							bWriter.write(pheromone[i][j]+" ");
						}bWriter.write("\n");
					}
					bWriter.close();
					outfs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		/*TODO
		 * 
		 * resContent是原始資料格式的結果，要將它再者裡mapping成符合google map格式的json檔案，
		 * 然後當網頁端post近來時，在將json傳給網頁端，最後讓google map讀取。
		 * 
		 * 下面"json example區塊中是java製作json的範例，參考來源：
		 * http://stackoverflow.com/questions/6154845/returning-json-response-from-servlet-to-javascript-jsp-page
		 *
		 * */
		//PrintWriter out = response.getWriter();
		JobClient client = new JobClient();
		JobConf conf = new JobConf(com.aco.FirstServlet.class);
		//record doubleL
		Path dl = new Path("hdfs://localhost:9000/user/hadoop/doubleL");
		FileSystem infs = FileSystem.get(conf);
		BufferedReader bReader = new BufferedReader(new InputStreamReader(dl.getFileSystem(conf).open(dl)));
		String line = bReader.readLine();
		String [][] point = new String[50][2];
		String [] poi_info = new String[50];
		while(line != null){
			String[] str_pois = line.split("\t");
			poi_info[Integer.parseInt(str_pois[0])-1] = str_pois[1];
			String[] latNland = str_pois[2].split(",");
			point[Integer.parseInt(str_pois[0])-1][0] = latNland[0];
			point[Integer.parseInt(str_pois[0])-1][1] = latNland[1];
			line = bReader.readLine();
		}
		
		//-------------------json example--------------------------------------------------------------------------
		Path aco = new Path("hdfs://localhost:9000/user/hadoop/ACO_output/part-00000");
		bReader = new BufferedReader(new InputStreamReader(aco.getFileSystem(conf).open(aco)));
		line = bReader.readLine();
		int count = 0, poi_sum = 0;
		String[][] days = new String[Kday][];
		while(line != null && count < Kday){
			String[] str_pois = line.split(":");
			days[count] = new String[str_pois.length];
			if (str_pois.length > 1) {
				poi_sum += str_pois.length;
				for( int i = 0; i < str_pois.length; i++ ){
					days[count][i] = str_pois[i];
				}
				count++;
			}
			line = bReader.readLine();
		}
		//mapping poi info
		String[] schedule = new String[Kday];
		for( int c = 0; c < Kday; c++ ){
			if(days[c].length <= 0) {
				break;
			}
			for( int i = 0; i < days[c].length; i++ ) {
				int int_poi = Integer.parseInt(days[c][i]);
				if (i == 0) {
					schedule[c] = "Day" + (c+1) + ": " + poi_info[int_poi-1];
				} else {
					schedule[c] += " >> " + poi_info[int_poi-1] ;
				}
			}
		}
		
		// mapping ladtitude and landtitude
		JSONArray map_points = new JSONArray();
		for( int d = 0; d < Kday; d++ ){
			String[][] day_points = new String[days[d].length][2];
			for( int p = 0; p < days[d].length; p++ ){
				int int_poi = Integer.parseInt(days[d][p]);
				day_points[p][0] = point[int_poi-1][0];
				day_points[p][1] = point[int_poi-1][1];
			}
			map_points.put(day_points);
		}
		
		// To Json
		JSONObject json = new JSONObject();
		try {
			json.put("map_points", map_points);
			json.put("schedule", schedule);
			json.put("sum_pathlength", Reduce.sumPathlength);
			json.put("sum_weight", Reduce.sumWeight);
		} catch (JSONException jse) { 

		}
		
		System.out.println(json.toString());
		
		 OutputStream out = response.getOutputStream();   
		 out.write(json.toString().getBytes("UTF-8"));
   }
}