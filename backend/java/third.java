package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.vecmath.Point3d;

import eu.printingin3d.javascad.coords.Coords3d;
import eu.printingin3d.javascad.models.Import;
import eu.printingin3d.javascad.utils.Pair;
import eu.printingin3d.javascad.vrl.CSG;
import eu.printingin3d.javascad.vrl.Facet;
import eu.printingin3d.javascad.vrl.Polygon;
import eu.printingin3d.javascad.vrl.export.StlBinaryFile;
import eu.printingin3d.javascad.vrl.export.StlTextFile;

import org.joml.Intersectiond;

public class third {
	private static Map<String,Double> hren = new HashMap<String, Double>();
	private static Map<String, Double> voxs = new HashMap<String, Double>();
	private static Map<String, Double> confMap = new HashMap<String, Double>();
	private static Map<String, Double> normalizedConfMap = new HashMap<String, Double>();
	private static Map<String, Boolean> boolConfMap = new HashMap<String, Boolean>();
	private static Map<String, Double> graph = new HashMap<String, Double>();
	private final static double extra = 50;
	public static List<Point3d[]> trias = new ArrayList<Point3d[]>();
	private static List<Double> trias_sq = new ArrayList<Double>();
	private static double[] max = new double[3];
	private static double[] min = new double[3];
	private static Point3d start = new Point3d(0, 0, 0);
	private static List<Pair<Point3d, Point3d>> normals = new ArrayList<>();
	public static Connection conn = null;
	private static byte[] buffer1 = null;
	private static String separator = ",";
	private static String max_key = null;
	private static String tridb_name = null;
	private static String outGraph = null;
	private static List<Point3d> points = new ArrayList<Point3d>();


	private static double dacc = 6.601;//5.601;
	private static double vox_step = Math.floor(dacc) / 2;
	private static double normSize = vox_step;
	private static double radius =  dacc;
	private static double rad = 2*dacc;
	
	
	public static String OUT_PATH = "C:\\Users\\TrofimovDM\\eclipse-workspace\\centerline_fin\\data\\";

	public static void main(String[] args) throws ParseException, SQLException, IOException, ClassNotFoundException {
		if (args.length != 0) {
			tridb_name = args[0];
			outGraph = args[1];
			dacc = Double.parseDouble(args[2]);
			vox_step = Double.parseDouble(args[3]);
		} else {
			tridb_name = "1.tridb";//"files\\ÊÀÌ-4-7-2.tridb";// "curr\\ÇÄ-3_64.tridb";
			outGraph = "outGraph";
		}
		long t2 = System.nanoTime();
		long t1 = System.nanoTime();
		p("vox: " + vox_step);
		Integer[] N = getmaxIDfromTridb(tridb_name);
		p(N[0]);
		p("blobs: " + N.length);
		int DO = 0;
		for (int i = DO; i < DO+1; i++) {
			tridbToTriangles(N[i]);
			//
			// export
			
			// objExport.trias2obj(trias);
			
			createNormals();
			createStart();
			
			checkNormals();
			
			p(start);
			savePointsToTxt("fullSuk");
			write2stl(trias, "newsuk3.stl", true, false);
			// p(start.toString());
			p("start: " + ((double) (System.nanoTime() - t1) / 1000000000));
			t1 = System.nanoTime();
			createAccMap();
			p("acc: " + ((double) (System.nanoTime() - t1) / 1000000000));
			t1 = System.nanoTime();
			createConfMap();
			p("conf: " + ((double) (System.nanoTime() - t1) / 1000000000));
			t1 = System.nanoTime();
			normalizeConfMap();
			p("normConf: " + ((double) (System.nanoTime() - t1) / 1000000000));

			t1 = System.nanoTime();
			// Great job
			// out(confMap, "_6");
//			makeHren("hren.txt");
//			seek4value("seek4value.txt");
			makeGraph(outGraph + Integer.toString(i));
			p("graph: " + ((double) (System.nanoTime() - t1) / 1000000000));
			t1 = System.nanoTime();
			String key2 ="986,265,143"; //"1,8,18";/
			createLine(key2);
			p("createLine: " + ((double) (System.nanoTime() - t1) / 1000000000));
			t1 = System.nanoTime();
			// String key1 = "100,6,5";
			// String key2 = "986,265,143";
			// connectPoints(key1, key2);

			clearAll();
		}
		p("all time: " + ((double) (System.nanoTime() - t2) / 1000000000));
	}
	
	private static void checkNormals() {
		//import
		int wrong = 0;
		for(int n = 0; n<normals.size();n++) {
			int intersections = 0;
			for(int t = 0; t < trias.size(); t++)
			{
				double originX = normals.get(n).getValue1().x;
				double originY = normals.get(n).getValue1().y;
				double originZ = normals.get(n).getValue1().z;
				double dirX = normals.get(n).getValue2().x - originX;
				double dirY = normals.get(n).getValue2().y - originY;
				double dirZ = normals.get(n).getValue2().z - originZ; 
				double v0X = trias.get(t)[0].x;
				double v0Y = trias.get(t)[0].y;
				double v0Z = trias.get(t)[0].z;
				double v1X = trias.get(t)[1].x;
				double v1Y = trias.get(t)[1].y;
				double v1Z = trias.get(t)[1].z;
				double v2X = trias.get(t)[2].x;
				double v2Y = trias.get(t)[2].y;
				double v2Z = trias.get(t)[2].z;
				double epsilon = 0.001;
				double tt = Intersectiond.intersectRayTriangle(originX, originY, originZ, dirX, dirY, dirZ, v0X, v0Y, v0Z, v1X, v1Y, v1Z, v2X, v2Y, v2Z, epsilon);
				if(tt != -1.0)
				{
					intersections++;
				}
			}
			if(intersections%2 == 0)
			{
				wrong++;
//				p(intersections+ " WRONG NORMAL ");
			}
			else
			{
//				p("Good normal");
			}
			if(n%(normals.size()/100) == 0)
			{
				p(n/(normals.size()/100)+"% is done");
			}
		}
		p(wrong+" out of " + normals.size()+" are wrong");
	}

	private static Map<String,Double> mapEquality(Map<String,Double> inmap){
		Map<String, Double> outmap = new HashMap<String, Double>();
		for (Map.Entry<String, Double> entry : inmap.entrySet()) {
			String s = entry.getKey();
			double d = entry.getValue();
			outmap.put(s,d);
		}
		return outmap;
		
	}
	
	private static void seek4value(String string) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(OUT_PATH + string, "UTF-8");
		Map<String, Double> curmap = hren;//graph;
		double max_value = Collections.max(curmap.entrySet(), Map.Entry.comparingByValue()).getValue();
		String key = "2,2,2";
		writer.println(key);
		writer.close();
		
	}

	private static void makeHren(String string) throws FileNotFoundException, UnsupportedEncodingException {
		
		Map<String, Double> curmap = normalizedConfMap;
		PrintWriter temper = new PrintWriter(OUT_PATH + string, "UTF-8");
		int i = 0;
		double avg = 0;
		for (Map.Entry<String, Double> entry : curmap.entrySet()) {
			i++;
			double val = (Double) entry.getValue();
			avg += val;
		}
		avg = avg/i;
		for (Map.Entry<String, Double> entry : curmap.entrySet()) {
			String key = (String) entry.getKey();
			double val = (Double) entry.getValue();
			if(val>= 0.8*avg)
				temper.println(key+','+Double.toString(val));
				hren.put(key, val);
		}
		temper.close();
		
	}

	private static String key2;
	
	public static void write2stl(List<Point3d[]> trias, String string2, boolean OverWrite, boolean ASCII)
			throws IOException {
		boolean moveStart = true;
		
		List<Polygon> p_list = new ArrayList<Polygon>();
		List<Facet> fac_list = new ArrayList<Facet>();
		for(int i = 0 ;i<trias.size(); i++) {
			List<Coords3d> list_t = new ArrayList<Coords3d>();
			for(int j = 0; j<3;j++) {
				Coords3d c1;
				if(moveStart)
				{
					c1 = new Coords3d(trias.get(i)[j].x-start.x, trias.get(i)[j].y-start.y, trias.get(i)[j].z-start.z);
				}else {
					c1 = new Coords3d(trias.get(i)[j].x, trias.get(i)[j].y, trias.get(i)[j].z);
				}
				list_t.add(c1);
			}
			Polygon p = null;
			p = Polygon.fromPolygons(list_t, null);
			p_list.add(p);			
		}
		Polygon[] p_arr = null;
		p_arr = p_list.toArray(new Polygon[p_list.size()]);
		CSG s = CSG.fromPolygons(p_arr);
		fac_list = s.toFacets();
		if (OverWrite) {
			File del = new File(string2);
			del.delete();
		}
		FileOutputStream s4 = new FileOutputStream(string2);
		if (ASCII) {
			eu.printingin3d.javascad.vrl.export.StlTextFile q_temp_ascii = new StlTextFile(s4);
			q_temp_ascii.writeToFile(fac_list);
			q_temp_ascii.close();
		} else {
			eu.printingin3d.javascad.vrl.export.StlBinaryFile q_temp = new StlBinaryFile(s4);
			q_temp.writeToFile(fac_list);
			q_temp.close();
		}
	}
	
	@SuppressWarnings("unused")
	private static void createLine(String key_in) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(OUT_PATH + "createLine.txt", "UTF-8");
		Map<String, Double> curmap = new HashMap<String, Double>();//graph;
		curmap = mapEquality(graph);//normalizedConfMap);
		curmap.remove(key_in);
		double temp_rad = rad;
		writer.println(key_in + ";");
		Map<String, Double> k = curmap.entrySet().stream().filter(x -> {
//			if (dist(x.getKey(), key_in) < 4*dacc) {
//				return true;	
//			}
//			return false;
			return true;
		}).collect(Collectors.toMap(x -> x.getKey(), x -> dist(x.getKey(), key_in)));
		String next_key = Collections.min(k.entrySet(), Map.Entry.comparingByValue()).getKey();
		curmap.remove(next_key);
		writer.println(next_key + ";");
		String key1 = key_in;
		key2 = next_key;
		String key3 = "";
		Map<String, Double> rem1 = removeCylinder(key1,key2,curmap,dacc/2);
		for (Map.Entry<String, Double> entry : rem1.entrySet()) {
//			curmap.remove(entry.getKey());
		}
		while (true) {
			rad = dacc;
			Map<String, Double> temp_map;
			while (true) {
				temp_map = curmap.entrySet().stream().filter(x -> {
					if (dist(x.getKey(), key2) < rad) {
						return true;
					}
					return false;
				}).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
				if (temp_map.size() < 2) {
					p(temp_map.size());
					p("rad1:" +rad+"___"+key1);
					p(curmap.toString());
					rad *= 1.1;	
					if( rad > 1000)
						break;
				} else
				{
					double minAngle = 0;
					String temp_key = "";			
					Pair<Double,String> p = getMinAngle(temp_map,key1,key2,0,0);
					temp_key = p.getValue2();
					minAngle = p.getValue1();
					p("doin' " + temp_key + " " + minAngle);
					if (minAngle < 90) {	
						rad *= 1.1;	
						p("rad2:" +rad);
						if( rad > 1000)
							break;
					} else
						break;
				}
			}
			p("temp_map: " + temp_map.toString());
			double minAngle = 0;
			String temp_key = "";			
			Pair<Double,String> p = getMinAngle(temp_map,key1,key2,0,0);
			temp_key = p.getValue2();
			minAngle = p.getValue1();
			p("doin' " + temp_key + " " + minAngle);
			if (minAngle < 90) {	
				p("break;");
				break;
			}
			curmap.remove(temp_key);
			writer.println(temp_key + ";");
			Map<String, Double> rem = removeCylinder(key1, key2, curmap, dacc/2);
			for (Map.Entry<String, Double> entry : rem.entrySet()) {
//				curmap.remove(entry.getKey());
			}
			// shift keys
			key1 = key2;
			key2 = temp_key;
			
		}
		writer.close();
		PrintWriter w1 = new PrintWriter(OUT_PATH + "remainGraph.txt", "UTF-8");
		for (Map.Entry<String, Double> entry : curmap.entrySet()) {
			w1.println(entry.getKey() + ";");
		}
		w1.close();

	}

	/*
	 * double a = dist(keyB,keyC); double b = dist(keyA,keyC); double c =
	 * dist(keyA,keyB); double p = (a+b+c)/2; double S = sqrt(p*(p-a)*(p-b)*(p-c));
	 * double h = 2*S/c; if(h>d) return false; else{
	 * if((Math.degrees(Math.asin(h/a)) > 90)||(Math.degrees(Math.asin(h/b)) > 90))
	 * return false; } return true;
	 */
	
	private static Pair<Double,String> getMinAngle(Map<String, Double> temp_map,String key1, String key2,double w0, double w1)
	{
		double minAngle = 0;
		double comp = 0;
		double maxValue = 0; //
		String temp_key = "";
		double[] weights = new double[2];
		weights[0] = w0;
		weights[1] = w1;//5
		for (Map.Entry<String, Double> entry : temp_map.entrySet()) {
			String key3 = entry.getKey();
			double curAngle = angle(key1, key2, key3);
			double value = entry.getValue();
//			if (curAngle > minAngle) {
			if (curAngle+weights[0]/dist(key2,key3)+weights[1]*value > comp) {
				temp_key = key3;
				minAngle = curAngle;
				comp = curAngle+weights[0]/dist(key2,key3)+weights[1]*value;
			}
		}
		Pair<Double,String> p = new Pair<Double,String>(minAngle,temp_key);
		return p;
	}
	
	private static Map<String, Double> removeCylinder(String keyA, String keyB, Map<String, Double> curmap,
			double in_h) {
		return curmap.entrySet().stream().filter(x -> {
			double a = dist(keyB, x.getKey());
			double b = dist(keyA, x.getKey());
			double c = dist(keyA, keyB);
			double p = (a + b + c) / 2;
			double S = sqrt(p * (p - a) * (p - b) * (p - c));
			double h = 2 * S / c;
			if (h > in_h) {////////////
				return false;
			} else {
				if ((Math.toDegrees(Math.asin(h / a)) < 90) && (Math.toDegrees(Math.asin(h / b)) < 90)) {
					return true;
				}
				return false;
			}

		}).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

	}

	private static double angle(String keyA, String keyB, String keyC) {
		double a = dist(keyB, keyC);
		double b = dist(keyA, keyC);
		double c = dist(keyA, keyB);
		double cosABC = (sqr2(b) - sqr2(a) - sqr2(c)) / (-2 * a * c);
		double ABC = Math.acos(cosABC);
		return Math.toDegrees(ABC);
	}

	@SuppressWarnings("unused")
	private static void connectPoints(String key1, String key2)
			throws FileNotFoundException, UnsupportedEncodingException {
		// 100,6,5 986,265,143
		Map<String, Double> curmap = new HashMap<String, Double>();
		curmap = graph;
		String filename = "connected.txt";
		PrintWriter writer = new PrintWriter(OUT_PATH + filename, "UTF-8");
		writer.println(key1 + ";");
		curmap.remove(key1);
		String key = key1;
		while (!(key.equals(key2)) && (!curmap.isEmpty())) {
			key = getSuitable(key, key2, curmap);
			writer.println(key + ";");
			curmap.remove(key);
			// p(key + ";");
		}
		if (key.equals(key2))
			p("cP : got the end ");

		writer.close();
	}

	private static String getSuitable(String key, String key2, Map<String, Double> curmap) {

		double d = 1000000;
		String retKey = null;
		Map<String, Double> k = new HashMap<String, Double>();
		rad = 8;
		while (true) {
			k = curmap.entrySet().stream().filter(x -> {
				if (dist(x.getKey(), key) < rad) {
					return true;
				}
				return false;
			}).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
			if ((k.size() < 4) || (k.isEmpty())) {
				// p(rad);
				rad += 2;
			} else
				break;
		}
		for (Map.Entry<String, Double> entry : k.entrySet()) {
			String nkey = (String) entry.getKey();
			double tempd = Math.abs(dist(nkey, key2));
			// p(tempd);

			if (tempd < d) {
				d = tempd;
				retKey = nkey;
			}
		}
		// p(d);
		// p(" ");
		return retKey;

	}

	@SuppressWarnings("unused")
	private static void complexDist(String key1, String key2, Map<String, Double> curmap) {
		// nkey, key2
		List<String> curList = new ArrayList<String>();
		for (Entry<String, Double> entry : curmap.entrySet()) {
			curList.add(entry.getKey());
		}

	}

	@SuppressWarnings("unused")
	public static void savePointsToTxt(String filename) throws FileNotFoundException, UnsupportedEncodingException {
		filename = filename + ".txt";

		PrintWriter writer = new PrintWriter(OUT_PATH + filename, "UTF-8");
		for (int i = 0; i < points.size(); i++) {
			double x = points.get(i).x - start.x;
			double y = points.get(i).y - start.y;
			double z = points.get(i).z - start.z;
			writer.println(x + separator + y + separator + z+ ";");
		}
		writer.close();
		p("points: " + filename);
	}

	private static void normalizeConfMap() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(OUT_PATH + "normConfMap.txt", "UTF-8");
		double max_value = Collections.max(confMap.entrySet(), Map.Entry.comparingByValue()).getValue();
		double min_value = Collections.min(confMap.entrySet(), Map.Entry.comparingByValue()).getValue();
		for (Entry<String, Double> entry : confMap.entrySet()) {
			String tempKey = entry.getKey();
			normalizedConfMap.put(tempKey, entry.getValue() / max_value);
			boolConfMap.put(tempKey, (entry.getValue() / max_value) > ((max_value - min_value) / (2 * max_value)));
			writer.println(tempKey + ";");
		}
		p("here"+normalizedConfMap.size());
		writer.close();
	}

	public static void clearAll() {
		trias.clear();
		trias_sq.clear();
		normals.clear();
		voxs.clear();
		confMap.clear();
		start = new Point3d(0, 0, 0);
		points.clear();

	}

	public static Integer[] getmaxIDfromTridb(String tridb_name2) throws SQLException, IOException {
		connect2tridb(tridb_name2);		
		String query = "SELECT COUNT(ID) FROM 'Geometry';";
		PreparedStatement s = conn.prepareStatement(query);
		ResultSet rs = s.executeQuery();
		Integer out[] = new Integer[rs.getInt(1)];
		rs.close();
		query = "SELECT ID FROM 'Geometry';";
		s = conn.prepareStatement(query);
		rs = s.executeQuery();
		int i = 0;		
		while(rs.next())
		{
			out[i] = rs.getInt(1);
			i++;
		}
		rs.close();
		return out;

	}

	private static void makeGraph(String name) throws FileNotFoundException, UnsupportedEncodingException {
		String n1 = name + "_no4.txt";
		name = name + ".txt";
		PrintWriter writer = new PrintWriter(OUT_PATH + name, "UTF-8");
		PrintWriter w1 = new PrintWriter(OUT_PATH + n1, "UTF-8");
		Map<String, Double> curmap = new HashMap<String, Double>();
		int i = 0;
		curmap = mapEquality(normalizedConfMap);
		//
//		PrintWriter temper = new PrintWriter(OUT_PATH + "temp_file.txt", "UTF-8");
//		for (Map.Entry<String, Double> entry : curmap.entrySet()) {
//			String key = (String) entry.getKey();
//			double val = (Double) entry.getValue();
//			temper.write(key+','+Double.toString(val)+" 	\n");
//		}
//		temper.close();
		max_key = Collections.max(curmap.entrySet(), Map.Entry.comparingByValue()).getKey();
		double max_value = curmap.get(max_key);
		while (!curmap.isEmpty()) {
			max_key = Collections.max(curmap.entrySet(), Map.Entry.comparingByValue()).getKey();
//			if (curmap.get(max_key) / max_value < 0.02) //0.2
//				break;
			Map<String, Double> k = curmap.entrySet().stream().filter(x -> {
				if (dist(x.getKey(), max_key) < 2 * radius ) { //*normalizedConfMap.get(max_key)
					return true;
				}
				return false;
			}).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
			writer.println(max_key + separator + curmap.get(max_key));
			w1.println(max_key + ";");
			graph.put(max_key, curmap.get(max_key));
			i++;
			for (Map.Entry<String, Double> entry : k.entrySet()) {
				String key = (String) entry.getKey();
				curmap.remove(key);
			}
		}
		writer.close();
		w1.close();
		p("written: " + i);
	}

	@SuppressWarnings("unused")
	private static void makeGr(String name) throws FileNotFoundException, UnsupportedEncodingException {
		double cdmin = 3;
		double cdmax = cdmin + vox_step * sqrt(3);
		PrintWriter writer = new PrintWriter(name, "UTF-8");
		Map<String, Double> curmap = new HashMap<String, Double>();
		curmap = confMap;
		max_key = Collections.max(curmap.entrySet(), Map.Entry.comparingByValue()).getKey();
		int i = 0;
		do {
			i++;
			String key = Collections.max(confMap.entrySet().stream().filter(x -> {
				if ((dist(x.getKey(), max_key) > cdmin) && (dist(x.getKey(), max_key) < cdmax)) {
					return true;
				}
				return false;
			}).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue())).entrySet(), Map.Entry.comparingByValue())
					.getKey();
			if (!max_key.equals(key)) {
				System.out.println(key + separator + confMap.get(key));
				writer.println(key + separator + confMap.get(key));
				confMap.remove(max_key);
				max_key = key;
			} else
				break;
		} while (i < 40);
		writer.close();
	}

	public static double dist(String key1, String key2) {
		String[] ak1 = key1.split(separator);
		String[] ak2 = key2.split(separator);
		Point3d k1 = new Point3d(Double.parseDouble(ak1[0]), Double.parseDouble(ak1[1]), Double.parseDouble(ak1[2]));
		Point3d k2 = new Point3d(Double.parseDouble(ak2[0]), Double.parseDouble(ak2[1]), Double.parseDouble(ak2[2]));
		Pair<Point3d, Point3d> vec = new Pair<Point3d, Point3d>(k1, k2);
		return lenghtPair(vec);
	}

	private static void createConfMap() {
		for (int i = 0; i < normals.size(); i++) {
			Pair<Point3d, Point3d> normal = normals.get(i);

			String max_key = null;
			double max_value = 0;
			while (lenghtPair(normal) < dacc) {
				double x = normal.getValue2().x;
				double y = normal.getValue2().y;
				double z = normal.getValue2().z;
				String ind;
				ind = getIndexes(x, y, z);
				double value = voxs.get(ind);
				if (value > max_value) {
					max_value = value;
					max_key = ind;
				}
				normal = vectorExpand(normal, vox_step);
			}
			if ((!confMap.containsKey(max_key))&&(max_value != 1)) {
				confMap.put(max_key, max_value);
			}
		}
	}

	private static void createAccMap() {
		for (int i = 0; i < normals.size(); i++) {
			Pair<Point3d, Point3d> normal = normals.get(i);
			int curr_value = 0;
			double curr_value_trias = trias_sq.get(i);
			while (lenghtPair(normal) < dacc) {
				curr_value++;
				double x = normal.getValue2().x;
				double y = normal.getValue2().y;
				double z = normal.getValue2().z;
				String ind;
				ind = getIndexes(x, y, z);
				if (voxs.containsKey(ind)) {
					double val = voxs.get(ind);
					voxs.remove(ind);
					voxs.put(ind, val + curr_value); //curr_value_trias * curr_value
				} else {
					voxs.put(ind, (double) curr_value);
				}
				normal = vectorExpand(normal, vox_step);
			}
		}
	}

	public static void createStart() {
		getMinMax();
		Point3d nul = new Point3d(0, 0, 0);
		Point3d strt = new Point3d(min[0], min[1], min[2]);
		Pair<Point3d, Point3d> qwe = new Pair<Point3d, Point3d>(nul, strt);
		start = vectorExpand(qwe, -extra / 1.732).getValue2();
		start = qwe.getValue2();
	}

	private static void createNormals() {
		for (int i = 0; i < trias.size(); i++) {
			Pair<Point3d, Point3d> normal = new Pair<Point3d, Point3d>(null, null);
			normal = getNormal(trias.get(i));
			normal = vectorExpand(normal, -(1-normSize) * lenghtPair(normal));// -lenghtPair(normal)+vox_step);
			normals.add(normal);
			trias_sq.add(getSquare(trias.get(i)));
		}
	}

	public static void tridbToTriangles(int id) throws SQLException, IOException, ClassNotFoundException {
		extract(id);
		parseTriangularModel();
	}

	@SuppressWarnings("unused")
	private static void out(Map<String, Double> map, String n)
			throws FileNotFoundException, UnsupportedEncodingException {
		String name = "conf" + n + ".txt";
		p("ConfMap saved to: " + name);
		PrintWriter writer = new PrintWriter(name, "UTF-8");
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			String key = (String) entry.getKey();
			Object o = entry.getValue();
			double value = (Double) o;
			writer.println(key + "," + value);
		}
		writer.close();
	}

	private static double getSquare(Point3d[] tr) {
		double a = sqrt(sqr2(tr[0].x - tr[1].x) + sqr2(tr[0].y - tr[1].y) + sqr2(tr[0].z - tr[1].z));
		double b = sqrt(sqr2(tr[1].x - tr[2].x) + sqr2(tr[1].y - tr[2].y) + sqr2(tr[1].z - tr[2].z));
		double c = sqrt(sqr2(tr[2].x - tr[0].x) + sqr2(tr[2].y - tr[0].y) + sqr2(tr[2].z - tr[0].z));
		double s = (a + b + c) / 2;
		double sq = sqrt(s * (s - a) * (s - b) * (s - c));
		return sq;
	}

	private static void getMinMax() {
		double min_x = trias.get(0)[0].x;
		double max_x = trias.get(0)[0].x;
		double min_y = trias.get(0)[0].y;
		double max_y = trias.get(0)[0].y;
		double min_z = trias.get(0)[0].z;
		double max_z = trias.get(0)[0].z;
		for (int i = 0; i < trias.size(); i++) {
			for (int j = 0; j < 3; j++) {
				double currX = trias.get(i)[j].x;
				double currY = trias.get(i)[j].y;
				double currZ = trias.get(i)[j].z;
				if ((currX < min_x))
					min_x = currX;
				if ((currY < min_y))
					min_y = currY;
				if ((currZ < min_z))
					min_z = currZ;
				if ((currX > max_x))
					max_x = currX;
				if ((currY > max_y))
					max_y = currY;
				if ((currZ > max_z))
					max_z = currZ;
			}
		}
		max[0] = max_x;
		max[1] = max_y;
		max[2] = max_z;
		min[0] = min_x;
		min[1] = min_y;
		min[2] = min_z;
	}

	@SuppressWarnings("unused")
	private static List<Point3d[]> read_from_stl(String string) throws IOException {
		List<Facet> flist = new ArrayList<Facet>();
		File f = new File(string);
		Import i = new Import(f);
		CSG s = i.toCSG();
		flist = s.toFacets();
		List<Point3d[]> out = new ArrayList<Point3d[]>();
		for (int j = 0; j < flist.size(); j++) {
			Point3d[] p = new Point3d[3];
			Point3d f1 = new Point3d(flist.get(j).getTriangle().getPoints().get(0).getX(),
					flist.get(j).getTriangle().getPoints().get(0).getY(),
					flist.get(j).getTriangle().getPoints().get(0).getZ());
			Point3d s1 = new Point3d(flist.get(j).getTriangle().getPoints().get(1).getX(),
					flist.get(j).getTriangle().getPoints().get(1).getY(),
					flist.get(j).getTriangle().getPoints().get(1).getZ());
			Point3d t1 = new Point3d(flist.get(j).getTriangle().getPoints().get(2).getX(),
					flist.get(j).getTriangle().getPoints().get(2).getY(),
					flist.get(j).getTriangle().getPoints().get(2).getZ());
			p[0] = f1;
			p[1] = s1;
			p[2] = t1;
			out.add(p);
		}
		return out;
	}

	private static String getIndexes(double x, double y, double z) {
		int[] out = new int[3];
		out[0] = (int) Math.ceil((x - start.x) / vox_step);
		out[1] = (int) Math.ceil((y - start.y) / vox_step);
		out[2] = (int) Math.ceil((z - start.z) / vox_step);
		String s = String.valueOf(out[0]) + separator + String.valueOf(out[1]) + separator + String.valueOf(out[2]);
		return s;
	}

	private static Pair<Point3d, Point3d> getNormal(Point3d[] in) {
		Point3d f1 = in[0];
		Point3d s1 = in[1];
		Point3d t1 = in[2];
		Point3d base = new Point3d((f1.x + s1.x + t1.x) / 3, (f1.y + s1.y + t1.y) / 3, (f1.z + s1.z + t1.z) / 3);
		double wrki;
		Point3d v1 = new Point3d(f1.x - s1.x, f1.y - s1.y, f1.z - s1.z),
				v2 = new Point3d(s1.x - t1.x, s1.y - t1.y, s1.z - t1.z);

		wrki = sqrt(
				sqr2(v1.y * v2.z - v1.z * v2.y) + sqr2(v1.z * v2.x - v1.x * v2.z) + sqr2(v1.x * v2.y - v1.y * v2.x));

		Point3d head = new Point3d(-((v1.y * v2.z - v1.z * v2.y) / wrki) + base.x,
				-((v1.z * v2.x - v1.x * v2.z) / wrki) + base.y, -((v1.x * v2.y - v1.y * v2.x) / wrki) + base.z);

		Pair<Point3d, Point3d> out = new Pair<Point3d, Point3d>(base, head);
		return out;
	}

	@SuppressWarnings("unused")
	private static double lenght(Point3d end) {
		return sqrt(end.x * end.x + end.y * end.y + end.z * end.z);
	}

	private static double lenghtPair(Pair<Point3d, Point3d> end) {
		return sqrt(sqr2(end.getValue2().x - end.getValue1().x) + sqr2(end.getValue2().y - end.getValue1().y)
				+ sqr2(end.getValue2().z - end.getValue1().z));
	}

	private static Pair<Point3d, Point3d> vectorExpand(Pair<Point3d, Point3d> norm, double size) {
		Point3d end = new Point3d(norm.getValue2().x - norm.getValue1().x, norm.getValue2().y - norm.getValue1().y,
				norm.getValue2().z - norm.getValue1().z);

		double absV = 1 / sqrt(end.x * end.x + end.y * end.y + end.z * end.z);

		Point3d new_end = new Point3d(end.x * (1 + size * absV) + norm.getValue1().x,
				end.y * (1 + size * absV) + norm.getValue1().y, end.z * (1 + size * absV) + norm.getValue1().z);

		Pair<Point3d, Point3d> vec = new Pair<Point3d, Point3d>(norm.getValue1(), new_end);

		return vec;
	}

	private static void extract(int id) throws SQLException, IOException {
		String query = "SELECT Geometry FROM 'Geometry' WHERE id = ?;";
		PreparedStatement s = conn.prepareStatement(query);
		s.setInt(1, id);
		ResultSet rs = s.executeQuery();
		while (rs.next()) {
			InputStream input = rs.getBinaryStream("Geometry");
			buffer1 = new byte[5570560];
			input.read(buffer1);
		}
	}

	private static void connect2tridb(String name) throws SQLException {
		String url = "jdbc:sqlite:" + name;
		conn = DriverManager.getConnection(url);
		String out = "sql: JDBC connected to " + name;
		p(out);
	}

	private static void parseTriangularModel() throws SQLException {
		ByteBuffer buffer = ByteBuffer.wrap(buffer1).order(ByteOrder.LITTLE_ENDIAN);
		int i = 24;
		long size1 = buffer.getLong(8);
		long size = buffer.getLong(8);
		p("bytes read: " + size);
		int y = (int) size + 24 + 16;
		int z = (int) size + (int) size + 40 + 16;

		for (; size > 0; size -= 8) {
			Point3d curr = new Point3d(buffer.getDouble(i), buffer.getDouble(y), buffer.getDouble(z));
			i += 8;
			y += 8;
			z += 8;
			points.add(curr);
		}
		int tre = (int) size1 + (int) size1 + (int) size1 + 40 + 16;
		size = buffer.getLong(tre);
		tre += 16;
		for (; size > 0; size -= 4) {
			Point3d[] tria = new Point3d[3];
			int a = buffer.getInt(tre);
			tre += 4;
			size -= 4;
			int b = buffer.getInt(tre);
			tre += 4;
			size -= 4;
			int c = buffer.getInt(tre);
			tre += 4;
			tria[0] = points.get(a);
			tria[1] = points.get(b);
			tria[2] = points.get(c);
			trias.add(tria);
		}
		// buffer.clear();
	}

	private static double sqrt(double in) {
		return Math.pow(in, 0.5);
	}

	private static double sqr2(double in) {
		return Math.pow(in, 2);
	}

	private static void p(Object o) {
		System.out.println(o);
	}
}
