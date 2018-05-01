package agents;

import FIPA.AgentID;
import helpers.MessageRouter;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.print.attribute.IntegerSyntax;
import java.io.IOException;
import java.util.*;

public class ConveyorAgent extends Agent {

    //private boolean palletStatus;
    private boolean hasWorkStation;
    private int conveyorStatus;

    private HashSet<String> neighbours;
    private String nick;
    private int workTime;
    private int thruputTime;
    private int timeOut;
    private JSONArray shortestpath;
    private JSONParser jsonParser = new JSONParser();

    protected void setup() {
        conveyorStatus = 0;
        neighbours = new HashSet<String>();
        try {
            // Get constructing arguments from ContainerController's
            // createNewAgent-method
            Object[] args = getArguments();

            // Throw Exception if arguments are null
            if (args == null) {
                throw new Exception("Arguments were null");
            }

            // Arguments passed are in LayOutBuilderAgent createAgents-method
            //nick = args[0].toString();
            hasWorkStation = Boolean.parseBoolean(args[0].toString());

            neighbours = (HashSet<String>) args[1];

            HashMap<String, Integer> config_ = (HashMap<String, Integer>) args[2];

            workTime = config_.get("workTime");
            thruputTime = config_.get("throughputTime");
            timeOut = config_.get("timeout");
            

        } catch (Exception e) {
            System.out.print("Conveyor couldn't be created. Stack trace: ");
            e.printStackTrace();

            // Delete this agent since it couldn't be initialized correctly
            this.doDelete();
        }

        addBehaviour(new RequestRouter(jsonParser, this));
        //addBehaviour(new ReceiveRequest(this));
        //addBehaviour(new ReceiveAccept(this));
        //addBehaviour(new ReceiveRefuse(this));

    }

    //behaviour to play with the workpieces
    private class movingStuff extends Behaviour{
        private AID target;
        private JSONArray stripdRoute;


        private movingStuff(){

        }
        //ticker behaviour for the thruput time simulation
        Behaviour thruPut = new WakerBehaviour(myAgent, thruputTime){
            protected void onWake(){
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(target);
                JSONObject routet = new JSONObject();
                try {
                    routet.put("action", "get-shortest-path");
                    routet.put("source", target.getLocalName());
                    int i = stripdRoute.size();
                    routet.put("destination", stripdRoute.get(i-1));
                    req.setContent(routet.toJSONString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(routet.toJSONString());
                myAgent.send(req);
                System.out.println("Agent " + getLocalName() + " moved the pallet to the " +
                        "next conveyor with route " + stripdRoute);
                //System.out.println("Pallet moved to next conveyor.");
                //shortestpath.clear();
                //System.out.println(shortestpath);

            }
        };

        Behaviour working = new WakerBehaviour(myAgent, workTime){
            protected void onWake(){

                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(target);
                try {
                    req.setContent(stripdRoute.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                myAgent.send(req);
                //System.out.println("Pallet has been worked on.");

            }
        };

        //startup sets the target agent from the jsonobject targets and strips it from the path which gets
        //assigned on the next conveyor again. Leaves(?) soruce and destination.
        public void onStart() {
            //System.out.println("Shortest path (130): " + shortestpath.toString());
            target = new AID(shortestpath.get(1).toString(), AID.ISLOCALNAME);
            stripdRoute = shortestpath;
            System.out.println("shortest path is:" + shortestpath);
        }

        public void action() {
            addBehaviour(thruPut);

        }
        public boolean done() {
            return true;
        }
    }

    //behaviour to act upon the json:s
    private class jsonMessage extends Behaviour {
        private JSONObject route;

        private jsonMessage(JSONObject route_){
            route = route_;
        }


        //runs once on call up
        public void onStart() {

        }

        public void action() {

            JSONArray it2 = (JSONArray) route.get("paths");

            if(it2 == null){
                it2 = new JSONArray();
                route.put("paths", it2);
            }
            boolean isLoop = false;
            int i = 0;
            for(Object instance: it2){
                if(instance.toString().equals(route.get("source").toString())) {
                    ++i;
                    if(i == 2) {
                        isLoop = true;
                        break;
                    }
                }

            }


            if (isLoop){//myAgent.getLocalName().equals(route.get("source").toString()) /*== route.get("source")*/){
                //System.out.println("source found");
                ACLMessage req = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                req.addReceiver(new AID(route.get("source").toString(), AID.ISLOCALNAME));
                myAgent.send(req);
            }

            else if(route.get("source").toString().equals(route.get("destination").toString()) ){
                System.out.println("Arrived to destination.");
                ACLMessage req = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                req.addReceiver(new AID(route.get("source").toString(), AID.ISLOCALNAME));
                myAgent.send(req);
            }

            //sends the route to the original source if no neighbours
            else if(neighbours.isEmpty()){
                System.out.println("no neighbours");
                ACLMessage req = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                req.addReceiver(new AID(route.get("source").toString(), AID.ISLOCALNAME));
                myAgent.send(req);
            }

            else {
                //System.out.println("orElse");
                boolean found = false;
                try {
                    for (String neighbour : neighbours) {

                        //handle the "found destination"
                        if (neighbour.equals(route.get("destination").toString())) {
                            found = true;
                            /*JSONArray */it2 = (JSONArray) route.get("paths");

                            if(it2 == null){
                                it2 = new JSONArray();
                                route.put("paths", it2);
                            }
                            //route.put("path", this.name);
                            it2.add(myAgent.getLocalName());
                            it2.add(route.get("destination"));
                            //ACLMessage req = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                            route.replace("action", "receiveAccept");
                            req.addReceiver(new AID(route.get("source").toString(), AID.ISLOCALNAME));
                            try {
                                req.setContent(route.toJSONString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            myAgent.send(req);
                        }
                    }

                    if (!found) {
                        /*JSONArray */it2 = (JSONArray) route.get("paths");

                        if(it2 == null){
                            it2 = new JSONArray();
                            route.put("paths", it2);
                        }
                        //route.put("path", this.name);
                        it2.add(myAgent.getLocalName());

                        for (String neighbour : neighbours) {
                            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);

                            //adds he neighbour as receiver of the message
                            req.addReceiver(getAID(neighbour));
                            try {
                                //req.setContentObject(route.toString());

                                req.setContent(route.toJSONString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            myAgent.send(req);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        public boolean done() {
            return true;
        }
    }

    //for acquiring messages of JSON to be checked
    private class ReceiveRequest extends CyclicBehaviour {
        private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        JSONParser parser = new JSONParser();
        private ReceiveRequest(Agent a) {
            super(a);
        }
        public void action() {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    System.out.println(msg.getContent());
                    JSONObject route_ = (JSONObject) parser.parse(msg.getContent());
                    addBehaviour(new jsonMessage(route_));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //for acquiring possible paths for the workpiece
    private class ReceiveAccept extends Behaviour{
        private MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        JSONParser parser = new JSONParser();
        Boolean running = false;
        JSONObject route_;
        private ReceiveAccept(Agent a, JSONObject route){
            super(a);
            route_ = route;
        }
        Behaviour decider = new WakerBehaviour(myAgent, timeOut){
            protected void onWake(){
                System.out.println("Decider wakes (293)");
                //decide target
                addBehaviour(new movingStuff());
                running = false;
            }
        };
        public void onStart() {
            if (!running) {
                addBehaviour(decider);
            }
            running = true;
        }
        public void action() {
            if(shortestpath == null){
                shortestpath = (JSONArray) route_.get("paths");
            }
            JSONArray it2 = (JSONArray) route_.get("paths");
            if(it2.size() < shortestpath.size()){
                shortestpath = (JSONArray) route_.get("paths");
            }

            /*
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {

                try {
                    JSONObject route_ = (JSONObject) parser.parse(msg.getContent());
                    if(shortestpath == null){
                        shortestpath = (JSONArray) route_.get("paths");
                    }
                    JSONArray it2 = (JSONArray) route_.get("paths");
                    if(it2.size() < shortestpath.size()){
                        shortestpath = (JSONArray) route_.get("paths");
                    }

                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            else {
                block();
            }
            */
        }

        @Override
        public boolean done() {
            return true;
        }
    }

    //for acquiring messages to be rejected
    private class ReceiveRefuse extends CyclicBehaviour{
        private MessageTemplate mt3 = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        private ReceiveRefuse(Agent a) {
            super(a);
        }

        public void action() {
            ACLMessage msg = myAgent.receive(mt3);
            if (msg != null) {
                //refuse message

                // TODO: This reply message is for testing purposes only
                //System.out.println("Tulostuuko tämä viesti?");

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("pung");
                send(reply);
            }

            else {
                block();
            }
        }
    }

     public void addNeighbour(String neighbour){
        neighbours.add(neighbour);
    }

    public Set getNeighbours(){
        return neighbours;
    }

    public void setConveyorStatus(int status){
        conveyorStatus = status;
    }

    /**
     * Router for ACLMessage.REQUEST messages
     */
    class RequestRouter extends MessageRouter {
        /**
         * Constructor
         */
        private RequestRouter(JSONParser jsonParser, Agent agent) {
            // Access only ACLMessage.REQUEST from message queue
            super(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    jsonParser,
                    agent
            );
        }

        /**
         *
         * @param data
         * @throws Exception
         */
        public void route(JSONObject data) throws Exception {
            String action = getAction(data);

            //System.out.println(data.toString());

            switch (action) {
                case "get-shortest-path":
                    // GUI can ask for shortest path with this action
                    // TODO: Put the on wake behaviour here
                    addBehaviour(
                        new WakerBehaviour(myAgent, timeOut){
                            protected void onWake(){
                                System.out.println("Decider wakes (293)");
                                //decide target
                                addBehaviour(new movingStuff());
                            }
                    });
                    // Change action to find-shortest-path
                    data.replace("action", "find-shortest-path");
                case "find-shortest-path":
                    // Populates message to neighbours or if destination found
                    // send the route to the source conveyor
                    addBehaviour(new jsonMessage(data));
                    break;
                case "receiveAccept":
                    routeFinder.addRoute(data);
                    //addBehaviour(new ReceiveAccept(agent, data));
                    break;
                default:
                    throw new Exception("Action not found.");
            }
        }
    }

    RouteFinder routeFinder = new RouteFinder(this);

    class RouteFinder {
        boolean running = false;
        Agent myAgent;
        Behaviour decider;

        RouteFinder(Agent a) {
            myAgent = a;
        }

        void addRoute(JSONObject route) {
            //System.out.println("Added route");
            if(shortestpath == null){
                shortestpath = (JSONArray) route.get("paths");
            }
            JSONArray it2 = (JSONArray) route.get("paths");
            if(it2.size() < shortestpath.size()){
                shortestpath = (JSONArray) route.get("paths");
            }

            if (running) {
                return;
            }

            running = true;

            /*
            decider = new WakerBehaviour(myAgent, timeOut){
                protected void onWake(){
                    System.out.println("Decider wakes (293)");
                    //decide target
                    addBehaviour(new movingStuff());
                    running = false;
                }
            };
            addBehaviour(decider);
            */
        }
    }
}

