package api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * this class present all the algorithms that can make on graph
 * find the shortest path from one node to another
 * save to information of the current graph on file
 * and load from file the information of graph 
 * @author liadn7
 * @author avielc11
 *
 */
public class DWGraph_Algo implements dw_graph_algorithms{
	private directed_weighted_graph myGraph;
	private boolean bool;
	private int tag;

	/**
	 * default constructor
	 */
	public DWGraph_Algo() {
		this.myGraph = new DWGraph_DS();
		bool = false;
		tag = 0;
	}

	@Override
	public void init(directed_weighted_graph g) {
		myGraph = g;
	}

	@Override
	public directed_weighted_graph getGraph() {
		return myGraph;
	}

	@Override
	public directed_weighted_graph copy() {
		directed_weighted_graph w1 = new DWGraph_DS();
		for(node_data node : myGraph.getV()) {
			node_data temp = new Nodes(node);
			w1.addNode(temp);
		}
		for(node_data node : myGraph.getV()) {
			for(edge_data edge : myGraph.getE(node.getKey())) {
				w1.connect(node.getKey(), edge.getDest(), edge.getWeight());
			}
		}
		return w1;
	}

	@Override
	public boolean isConnected() {
		if(myGraph.nodeSize() <= 1) {
			return true;
		}
		LinkedList<Integer> moves = new LinkedList<Integer>();
		HashMap<Integer,String> in = new HashMap<Integer,String>();
		int num = myGraph.getV().iterator().next().getKey();
		moves.add(num);
		while(!moves.isEmpty()) {
			int tempNum = moves.poll();
			if(myGraph.getE(tempNum).size() == 0) {
				return false;
			}
			for(edge_data n : myGraph.getE(tempNum)) {
				num = n.getDest();
				if(!in.containsKey(num)) {
					moves.add(num);
					in.put(num, "v");
				} 
			}
		}
		if(myGraph.nodeSize() == in.size()) {
			return true;
		}
		return false;
	}

	@Override
	public double shortestPathDist(int src, int dest) {
		if(myGraph.getNode(src) == null || myGraph.getNode(dest) == null) {
			return -1;
		}
		if(src == dest) return 0;
		HashMap<Integer,Point> map = bfs(src,dest);
		try {
			if(map.containsKey(dest))
				return map.get(dest).getWeight();
		}
		catch (Exception e){
			System.out.println("there is no path between them");
		}

		return -1;
	}

	@Override
	public List<node_data> shortestPath(int src, int dest) {
		node_data t1 = myGraph.getNode(src);
		LinkedList<node_data> list = new LinkedList<node_data>();
		if(t1 == null || myGraph.getNode(dest) == null) {
			return null;
		}
		if(src == dest) {
			list.add(t1);
			return list;
		}
		HashMap<Integer,Point> map = bfs(src,dest);
		if(map == null) {
			return null;
		}
		Point point = map.get(dest);
		node_data t3 = myGraph.getNode(dest);
		list.add(t3);
		while(point.getPrev() != src) {
			t3 = myGraph.getNode(point.getPrev());
			list.addFirst(t3);
			point = map.get(point.getPrev());

		}
		return list;
	}

	public List<node_data> connected_component(int id){
		List<node_data> list = new LinkedList<node_data>();
		if(this.myGraph.getNode(id) != null) {
			for(node_data n : this.myGraph.getV()) {
				if(scc(id,n.getKey())) {
					list.add(n);
				}
			}
		}
		if (!bool) {
			clearTag();
		}
		return list;	
	}

	public List<List<node_data>> connected_components(){
		List<List<node_data>> list = new LinkedList<List<node_data>>();
		if(myGraph.nodeSize() <= 1)
			return null;
		bool = true;
		for(node_data n : myGraph.getV()) {
			List<node_data> l = connected_component(n.getKey());
			list.add(l);
		}
		bool = false;
		clearTag();
		return list;	
	}

	@Override
	public boolean save(String file) {
		try {
			StringBuilder sn = new StringBuilder();
			StringBuilder se = new StringBuilder();
			PrintWriter pw=new PrintWriter(new File(file));
			for(node_data node : myGraph.getV()) {
				sn.append(node.getKey());
				sn.append(",");
				sn.append(node.getLocation().x());
				sn.append(",");
				sn.append(node.getLocation().y());
				sn.append(",");
				sn.append(node.getLocation().z());
				sn.append("-");
				for(edge_data edge : myGraph.getE(node.getKey())) {
					se.append(edge.getSrc());
					se.append(",");
					se.append(edge.getDest());
					se.append(",");
					se.append(edge.getWeight());
					se.append("-");
				}
			}
			sn.append("\n");
			sn.append(se);
			pw.write(sn.toString());
			pw.close();
			return true;
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean load(String file) {
		try 
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			directed_weighted_graph grp = new DWGraph_DS();
			if((line = br.readLine()) != null){
				String [] str = line.split("-");
				for(int i = 0 ; i < str.length ; i++) {
					String [] str1 = str[i].split(",");
					int key = Integer.parseInt(str1[0]);
					double x = Double.parseDouble(str1[1]);
					double y = Double.parseDouble(str1[2]); 
					double z = Double.parseDouble(str1[3]);
					geo_location local = new GeoLocation(x,y,z);
					System.out.println(key);
					node_data node = new Nodes(key,local);
					grp.addNode(node);
				}
			}				
			if((line = br.readLine()) != null) {
				String [] str = line.split("-");
				for(int i = 0 ; i < str.length ; i++) {
					String [] str1 = str[i].split(",");
					int src = Integer.parseInt(str1[0]);
					int dest = Integer.parseInt(str1[1]);
					double w = Double.parseDouble(str1[2]);
					grp.connect(src, dest, w);
				}
			}
			br.close();
			init(grp);
			return true;
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * get two keys - one for the start node and the other for the node need to 
	 * find the path to it
	 * @param start node.
	 * @param dest - end (target) node.
	 * @returns the weight of the shortest path between src to dest.
	 * if no such path --> returns -1
	 */

	private HashMap<Integer,Point> bfs(int src , int dest){
		HashMap<Integer,Point> map = new HashMap<Integer,Point>();
		LinkedList<Integer> qtemp = new LinkedList<Integer>();
		Point curr = new Point(src,0,-1);
		map.put(src, curr);
		qtemp.add(src);
		double count = 0;
		double way = -1;
		while(!qtemp.isEmpty()) {
			boolean bool = false;
			int poll = qtemp.poll();
			double weight = map.get(poll).getWeight();
			for(edge_data temp : myGraph.getE(poll)) {
				int TempN = temp.getDest();
				count = temp.getWeight() + weight;
				if(TempN == dest && (way > count || way == -1)) {
					Point end = new Point(dest);
					if(!map.containsKey(dest)) {
						map.put(dest, end);
					}
					map.get(dest).setWeight(count);
					map.get(dest).setPrev(poll);
					way = count;
					bool = true;
				}
				else if(update(TempN,count,poll,map) && !bool) {
					qtemp.add(TempN);
				}
			}
		}
		if(map.containsKey(dest)) {
			return map;
		}
		return null;
	}

	/**
	 * check if the hashmap(from function bfs) is already see the node if not then create new one
	 * else check the weight is smaller than the one inside
	 * @param node - the key of the node
	 * @param weight - the weight from src until here
	 * @param prev - the node connect to the current node and has the shortest path
	 * @param map - the hashmap contains all the node(as a Point)
	 * @return - true if not contains
	 */
	private boolean update(int node, double weight, int prev, HashMap<Integer,Point> map) {
		if(!map.containsKey(node)) {
			Point temp = new Point(node,weight,prev);
			map.put(node, temp);
			return true;
		}
		else {
			Point temp = map.get(node);
			if(temp.getWeight() > weight){
				temp.setWeight(weight);
				temp.setPrev(prev);
			}
		}
		return false;
	}

	private boolean scc(int i, int j) {
		if (i == j)
			return false;
		node_data n1 = myGraph.getNode(i);
		node_data n2 = myGraph.getNode(j);
		if(n1.getTag() != 0 && n2.getTag() != 0) {
			if(n1.getTag() == n2.getTag())
				return true;
			return false;
		}
		if(!bfs1(i, j))
			return false;
		if (!bfs1(j, i))
			return false;
		if(n1.getTag() == 0) {
			if(n2.getTag() == 0) {
				n1.setTag(tag++);
				n2.setTag(n1.getTag());
			}
			else 
				n1.setTag(n2.getTag());
		}
		else 
			n2.setTag(n1.getTag());
		return true;
	}

	private boolean bfs1(int src,int dest) {
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		LinkedList<Integer> q = new LinkedList<Integer>();
		map.put(src, src);
		q.add(src);
		while(!q.isEmpty()) {
			int poll = q.poll();
			for(edge_data edge : myGraph.getE(poll)) {
				int temp = edge.getDest();
				if(temp == dest) {
					return true;
				}
				else if(!map.containsKey(temp)) {

					q.add(temp);
					map.put(temp, temp);
				}
			}
		}
		return false;
	}

	public void setGraph(String file) {
		DWGraph_DS grp =new DWGraph_DS();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			if((line = br.readLine()) != null){
				JSONObject first = new JSONObject(line);
				JSONArray nodes = first.getJSONArray("Nodes");
				JSONArray edges = first.getJSONArray("Edges");
				for(int i = 0 ; i < nodes.length() ; i++) {
					JSONObject temp = nodes.getJSONObject(i);
					int id = temp.getInt("id");
					Nodes f = new Nodes(id);
					grp.addNode(f);
				}
				for(int i = 0 ; i < edges.length() ; i++) {
					JSONObject temp = edges.getJSONObject(i);
					int src = temp.getInt("src");
					int dest = temp.getInt("dest");
					double w = temp.getDouble("w");
					grp.connect(src, dest, w);
				}
			}
			br.close();
			this.myGraph = grp;
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void clearTag() {
		for(node_data n : myGraph.getV())
			n.setTag(0);
		this.tag = 0;
	}
}
