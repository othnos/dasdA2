package helpers;

import agents.ConveyorAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

public class RouteFinder {
    Agent myAgent;

    JSONArray shortestPath = null;

    private HashSet<JSONArray> shortestPaths = new HashSet<>();

    public RouteFinder(Agent a) {
        myAgent = a;
    }

    public void addRoute(JSONArray path) {
        //System.out.println("Added route");
        if(shortestPath == null){
            shortestPath = path;
            shortestPaths.clear();
            shortestPaths.add(shortestPath);
            return;
        }

        if(path.size() < shortestPath.size()){
            shortestPaths.clear();
            shortestPath = path;
        } else if (path.size() == shortestPath.size()) {
            shortestPaths.add(path);
        }
    }

    public JSONArray getShortestRoute() {
        return shortestPath;
    }

    public void clearShortestPath() {
        shortestPaths.clear();
        shortestPath.clear();
        shortestPath = null;
    }

    public String getShortestPaths() {
        return shortestPaths.toString();
    }
    /*
    private boolean running = false;
    private Agent agent;
    private Behaviour decider;

    private HashMap<String, JSONArray> shortestRoutes;

    public RouteFinder(Agent a) {
        agent = a;
        shortestRoutes = new HashMap<>();
    }

    public WakerBehaviour addRoute(JSONObject route, String srcDst) {
        JSONArray paths = (JSONArray) route.get("paths");

        if (shortestRoutes.get(srcDst) == null ||
                paths.size() < shortestRoutes.get(srcDst).size()) {
            shortestRoutes.put(srcDst, paths);
        }

        if (running) {
            return null;
        }

        running = true;

        return null;

        decider = new WakerBehaviour(agent, timeOut){
            protected void onWake(){
                System.out.println("Decider wakes (293)");
                //decide target
                agent.addBehaviour(new ConveyorAgent.movingStuff());
                running = false;
            }
        };
        agent.addBehaviour(decider);
    }
    */
}
