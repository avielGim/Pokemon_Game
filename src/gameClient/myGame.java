package gameClient;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import api.*;
import gameClient.util.Point3D;
import gameClient.util.Range;
import gameClient.util.Range2D;
import gameClient.util.Range2Range;

/**
 * this class save the data of the graph, the pokemon and the agent from the server.
 * @author liadn7
 * @author avielc11
 *
 */
public class myGame {
	public static final double EPS1 = 0.001, EPS2=EPS1*EPS1, EPS=EPS2;
	private DWGraph_Algo algo;
	private LinkedList<CL_Agent> ash;
	private LinkedList<CL_Pokemon> poke;
	private int numA;
	
	/**
	 * constructor that get server
	 * @param game - type game_service
	 */
	public myGame(game_service game) {
		setGraph(game);
		setPokemons(game);
		numOfAgent(game);
		Iterator<node_data> moveNode = null;
		Iterator<CL_Pokemon> movep = poke.iterator() ;
		//add agent by "numA" - add by find the location for one of the "poke"
		for(int i = 0 ; i < this.numA ; i++) {
			if(movep.hasNext()) {
				CL_Pokemon startPoke = movep.next();
				int src = startPoke.get_edge().getSrc();
				game.addAgent(src);
			}
			else if(moveNode == null){
				moveNode = this.algo.getGraph().getV().iterator();
				game.addAgent(moveNode.next().getKey());
			}
			else {
				game.addAgent(moveNode.next().getKey());
			}
			
		} 
		setAgent(game);
		if(numA <= poke.size()) {
			movep = poke.iterator();
			for(CL_Agent coach : ash) {
				CL_Pokemon pok = movep.next();
				pok.setMin_ro(coach.getID());
				coach.set_curr_fruit(pok);
			}
		}
	}
	
	/**
	 * get data of the graph as a jason type
	 * @param game - game service
	 */
	public void setGraph(game_service game) {
		DWGraph_DS grp =new DWGraph_DS();
		try {
			JSONObject first = new JSONObject(game.getGraph());
			JSONArray nodes = first.getJSONArray("Nodes");
			JSONArray edges = first.getJSONArray("Edges");
			for(int i = 0 ; i < nodes.length() ; i++) {
				JSONObject temp = nodes.getJSONObject(i);
				int id = temp.getInt("id");
				GeoLocation pos =new  GeoLocation(temp.getString("pos"));
				Nodes f = new Nodes(id, pos);
				grp.addNode(f);
			}
			for(int i = 0 ; i < edges.length() ; i++) {
				JSONObject temp = edges.getJSONObject(i);
				int src = temp.getInt("src");
				int dest = temp.getInt("dest");
				double w = temp.getDouble("w");
				grp.connect(src, dest, w);
			}
			this.algo = new DWGraph_Algo();
			this.algo.init(grp);
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get data of the pokemon as a jason type
	 * @param game - game service
	 */
	public void setPokemons(game_service game) {
		this.poke = new  LinkedList<CL_Pokemon>();
		try {
			JSONObject first = new JSONObject(game.getPokemons());
			JSONArray second = first.getJSONArray("Pokemons");
			for(int i = 0 ; i < second.length() ; i++) {
				JSONObject third = second.getJSONObject(i);
				JSONObject fourth = third.getJSONObject("Pokemon");
				int t = fourth.getInt("type");
				double v = fourth.getDouble("value");
				String p = fourth.getString("pos");
				CL_Pokemon f = new CL_Pokemon(new Point3D(p), t, v, 0, null);
				updateEdge(f,algo.getGraph());
				this.poke.add(f);
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get data of the agent as a jason type
	 * @param game - game service
	 */
	public void setAgent(game_service game) {
		ash =new LinkedList<CL_Agent>();
		try {
			JSONObject ttt = new JSONObject(game.getAgents());
			JSONArray ags = ttt.getJSONArray("Agents");
			for(int i=0;i<ags.length();i++) {
				CL_Agent c = new CL_Agent(algo.getGraph(),0);
				c.update(ags.get(i).toString());
				ash.add(c);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * get number of the agent that aloud to be on the graph as a jason type
	 * @param game - type game_service
	 */
	public void numOfAgent(game_service game) {
		String info = game.toString();
		JSONObject line;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			numA = ttt.getInt("agents");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * find the shortest path from the agent to the pokemon
	 * @param a - the given agent
	 * @return LinkedList of all node_data that go between the agent to the pokemon
	 */
	public LinkedList<node_data> NearestPoke(CL_Agent a){
		int start = a.getSrcNode();
		if(a.get_curr_fruit() == null) {
			double dist = -1;
			for(CL_Pokemon pok : poke) {
				if(pok.getMin_ro() == -1) {
					int  srcPoke = pok.get_edge().getSrc();
					double end = this.algo.shortestPathDist(start, srcPoke);
					if(dist == -1 || end < dist){
						dist = end;
						a.set_curr_fruit(pok);
						pok.setMin_ro(a.getID());
					}
				}
			}
		}
		if(a.get_curr_fruit() != null) {
			int srcPoke = a.get_curr_fruit().get_edge().getSrc();
			int destPoke = a.get_curr_fruit().get_edge().getDest();
			LinkedList<node_data> q = (LinkedList<node_data>) algo.shortestPath(start, srcPoke);
			q.add(algo.getGraph().getNode(destPoke));
			return q;
		}
		LinkedList<node_data> q = new LinkedList<node_data>();
		return q;
	}
	
	/**
	 * check which edge_data of the graph the pokemon is.
	 * @param fr - the given pokemon
	 * @param g - the given graph
	 * @return - true if successfully find the edge_data then set for the give pokemon the edge_data and return true.
	 * else return false.
	 */
	public boolean updateEdge(CL_Pokemon fr, directed_weighted_graph g) {
		Iterator<node_data> itr = g.getV().iterator();
		while(itr.hasNext()) {
			node_data v = itr.next();
			Iterator<edge_data> iter = g.getE(v.getKey()).iterator();
			while(iter.hasNext()) {
				edge_data e = iter.next();
				boolean f = isOnEdge1(fr.getLocation(), e,fr.getType(), g);
				if(f) {
					fr.set_edge(e);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if the distance from src to p  plus the distance from the p
	 * to dest is equal to the the distance from src to dest
	 * @param p - the location of the pokemon.
	 * @param src - the location of the start node.
	 * @param dest - the location of the destination node.
	 * @return - true if equals
	 */
	private static boolean isOnEdge3(geo_location p, geo_location src, geo_location dest ) {
		boolean ans = false;
		double dist = src.distance(dest);
		double d1 = src.distance(p) + p.distance(dest);
		if(dist>d1-EPS2) {ans = true;}
		return ans;
	}
	
	/**
	 * @param p - the location of the pokemon.
	 * @param s - the key of the start node.
	 * @param d - the key of the destination node.
	 * @param g - - the graph.
	 * @return true or false from the function isOnEdge3(p,src,dest);
	 */
	private static boolean isOnEdge2(geo_location p, int s, int d, directed_weighted_graph g) {
		geo_location src = g.getNode(s).getLocation();
		geo_location dest = g.getNode(d).getLocation();
		return isOnEdge3(p,src,dest);
	}
	
	/**
	 * @param p - the location of the pokemon.
	 * @param e - the current edge need to check.
	 * @param type - represent if the pokemon on edge from the high node to the low node.
	 * @param g - the graph.
	 * @return true or false from the function isOnEdge2(p,src, dest, g);
	 */
	private static boolean isOnEdge1(geo_location p, edge_data e, int type, directed_weighted_graph g) {
		int src = g.getNode(e.getSrc()).getKey();
		int dest = g.getNode(e.getDest()).getKey();
		if(type<0 && dest>src) {return false;}
		if(type>0 && src>dest) {return false;}
		return isOnEdge2(p,src, dest, g);
	}	
	
	public void updateAge(CL_Agent a,int src , int dest) {
		a.setNextNode(dest);
	}

	/**
	 * @return the directed_weighted_graph this game work
	 */
	public directed_weighted_graph getGraph() {
		return algo.getGraph();
	}

	/**
	 * @return the agents as a LinkedList
	 */
	public LinkedList<CL_Agent> getAsh() {
		return ash;
	}

	/**
	 * @return the Pokemons as a LinkedList
	 */
	public LinkedList<CL_Pokemon> getPoke() {
		return poke;
	}

	/**
	 * @return the number of the agent
	 */
	public int getNumA() {
		return numA;
	}
	
	/**
	 * Translate the location of the nodes, the pokemon and the agent to the location on the screen(panel)
	 * @param g - graph
	 * @return
	 */
	private static Range2D GraphRange(directed_weighted_graph g) {
		Iterator<node_data> itr = g.getV().iterator();
		double x0=0,x1=0,y0=0,y1=0;
		boolean first = true;
		while(itr.hasNext()) {
			geo_location p = itr.next().getLocation();
			if(first) {
				x0=p.x(); x1=x0;
				y0=p.y(); y1=y0;
				first = false;
			}
			else {
				if(p.x()<x0) {x0=p.x();}
				if(p.x()>x1) {x1=p.x();}
				if(p.y()<y0) {y0=p.y();}
				if(p.y()>y1) {y1=p.y();}
			}
		}
		Range xr = new Range(x0,x1);
		Range yr = new Range(y0,y1);
		return new Range2D(xr,yr);
	}
	public static Range2Range w2f(directed_weighted_graph g, Range2D frame) {
		Range2D world = GraphRange(g);
		Range2Range ans = new Range2Range(world, frame);
		return ans;
	}
	
}
