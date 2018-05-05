package agents;

import helpers.MessageRouter;
import helpers.PathFindingMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;

public class ConveyorAgent extends Agent {
    /**
     * Possible statuses of the conveyor
     */
    public enum Status {
        Ready,
        Busy,
        NotInUse
    }

    /**
     * Conveyor's current status
     */
    private Status status;

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

        // Conveyor is ready by default
        status = Status.Ready;
        try {
            // Get constructing arguments from ContainerController's
            // createNewAgent-method
            Object[] args = getArguments();

            // Throw Exception if arguments are null
            if (args == null) {
                throw new Exception("Arguments were null");
            }

            // Arguments passed are in LayOutBuilderAgent createAgents-method
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
        addBehaviour(new RejectProposalRouter(jsonParser, this));
    }

    //behaviour to play with the workpieces
    private class movingStuff extends Behaviour{
        private AID target;
        private JSONArray stripdRoute;

        PathFindingMessage pfm;

        private movingStuff(PathFindingMessage pfm_){
            pfm = pfm_;
        }

        //ticker behaviour for the thruput time simulation
        Behaviour thruPut = new WakerBehaviour(myAgent, thruputTime){
            protected void onWake(){
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver(target);

                try {
                    pfm.setSource(target.getLocalName());
                    pfm.setAction("get-shortest-path");
                    pfm.getPath().clear();
                    req.setContent(pfm.getAsJSONObject().toJSONString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                myAgent.send(req);
                System.out.println("Agent " + getLocalName() + " moved the pallet to the " +
                        "next conveyor with route " + stripdRoute);
                shortestpath.clear();
                shortestpath = null;
                stripdRoute.clear();
                stripdRoute = null;
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
            }
        };

        //startup sets the target agent from the jsonobject targets and strips it from the path which gets
        //assigned on the next conveyor again. Leaves(?) soruce and destination.
        public void onStart() {
        }

        public void action() {

            if(shortestpath == null){
                System.out.println("Path does not exist");
                return;
            }
            target = new AID(shortestpath.get(1).toString(), AID.ISLOCALNAME);
            stripdRoute = shortestpath;
            System.out.println("shortest path is:" + shortestpath);
            addBehaviour(thruPut);

        }
        public boolean done() {
            return true;
        }
    }

    //behaviour to act upon the json:s
    private class sendPathFindingMessage extends Behaviour {
        private PathFindingMessage pfm;

        private sendPathFindingMessage(PathFindingMessage pfm_){
            pfm = pfm_;
        }

        public void action() {
            // Set this conveyor to the path
            pfm.getPath().add(myAgent.getLocalName());

            for (String neighbour : neighbours) {
                //handle the "found destination"
                if (neighbour.equals(pfm.getDestination())) {
                    pfm.setAction("receiveAccept");
                    pfm.getPath().add(neighbour);

                    ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                    req.addReceiver(new AID(pfm.getSource(), AID.ISLOCALNAME));
                    req.setContent(pfm.getAsJSONObject().toJSONString());
                    myAgent.send(req);

                    return;
                }
            }

            // Since destination wasn't found, populate the
            // path finding to all of the neighbours
            pfm.setAction("find-shortest-path");
            for (String neighbour : neighbours) {
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                //adds a neighbour as receiver of the message
                req.addReceiver(getAID(neighbour));
                try {
                    req.setContent(pfm.getAsJSONObject().toJSONString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                myAgent.send(req);
            }
        }

        public boolean done() {
            return true;
        }
    }

    /**
     * Router for ACLMessage.REQUEST messages
     */
    class RequestRouter extends MessageRouter {
        /**
         * Store the current path finding message
         */
        PathFindingMessage pfm = null;

        /**
         * Constructor
         * @param jsonParser
         * @param agent
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

            pfm = null;

            System.out.println("Conveyor " + getLocalName() + " received data: " + data);

            // Select the wanted action
            switch (action) {
                // Replies the shortest path to the sender
                case "get-shortest-path":
                    pfm = new PathFindingMessage(data);

                    // If conveyor is not ready or the conveyor
                    // is the destination we won't start the path
                    // finding
                    if (!isConveyorReady() ||
                            check_ifDest()) {
                        return;
                    }

                    // Need to "finalize" pfm so that we can pass it
                    // to the WakerBehaviour
                    PathFindingMessage finalPfm = pfm;
                    addBehaviour(
                        new WakerBehaviour(myAgent, timeOut){
                            protected void onWake(){
                                //decide target
                                addBehaviour(new movingStuff(finalPfm));
                            }
                    });
                case "find-shortest-path":
                    if (pfm == null) {
                        pfm = new PathFindingMessage(data);
                    }

                    if (checkIfLoop()) {
                        System.out.println("Loop found with path " + pfm.getPath().toString());
                         return;
                    }

                    if (!hasNeighbours()) {
                        return;
                    }

                    // Populates message to neighbours or if destination found
                    // send the route to the source conveyor
                    addBehaviour(new sendPathFindingMessage(pfm));
                    break;
                // When route is found, add it to routeFinder
                case "receiveAccept":
                    pfm = new PathFindingMessage(data);

                    routeFinder.addRoute(pfm);
                    break;
                default:
                    throw new Exception("Action not found.");
            }
        }

        boolean isConveyorReady() {
            if (status == Status.Ready) {
                return true;
            }

            // Reply to the sender that conveyor is busy and
            // cannot start path finding because of that
            ACLMessage reply = msgRx.createReply();
            // TODO: Check for more suitable performative
            reply.setPerformative(ACLMessage.REFUSE);
            JSONObject jobj = new JSONObject();
            jobj.put("action", "conveyor-not-ready");
            reply.setContent(jobj.toJSONString());
            agent.send(reply);

            return false;
        }

        boolean check_ifDest() {
            if (pfm.getSource().equals(pfm.getDestination())) {
                System.out.println("Arrived to destination.");
                return true;
            }

            return false;
        }

        /**
         * Returns if the path finding message has gone through a loop
         * @return
         */
        boolean checkIfLoop() {
            boolean sourceEqualsThisAgent = pfm.getSource().equals(agent.getLocalName());

            HashSet<String> foundConveyors = new HashSet<>();

            for(Object instance: pfm.getPath()){
                if(
                    // If source conveyor is found from the path
                    // we know it must be a loop
                    (
                    sourceEqualsThisAgent &&
                    instance.toString().equals(pfm.getSource())
                    ) ||
                    // If same conveyor already exists in foundConveyors
                    // it must be a loop
                    foundConveyors.contains(instance.toString())
                ) {
                    return true;
                }

                foundConveyors.add(instance.toString());
            }

            return false;
        }

        boolean hasNeighbours() {
            if (!neighbours.isEmpty()) {
                return true;
            }

            System.out.println("no neighbours");
            ACLMessage req = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
            JSONObject jobj = new JSONObject();
            jobj.put("action", "no-neighbours");
            req.setContent(jobj.toJSONString());
            req.addReceiver(new AID(pfm.getSource(), AID.ISLOCALNAME));
            myAgent.send(req);

            return false;
        }
    }

    /**
     * Router for ACLMessage.REJECT_PROPOSAL messages
     */
    class RejectProposalRouter extends MessageRouter {
        /**
         * Constructor
         */
        private RejectProposalRouter(JSONParser jsonParser, Agent agent) {
            // Access only ACLMessage.REQUEST from message queue
            super(
                    MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
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
            try {
                String action = getAction(data);

                switch (action) {
                    default:
                        // Just ignore the found message
                }
            } catch (Exception e) {
                // Catch the "action not found" exception
                // and do not print any debugging
            }
        }
    }

    RouteFinder routeFinder = new RouteFinder(this);

    class RouteFinder {
        Agent myAgent;

        RouteFinder(Agent a) {
            myAgent = a;
        }

        void addRoute(PathFindingMessage pfm) {
            //System.out.println("Added route");
            if(shortestpath == null){
                shortestpath = (JSONArray) pfm.getPath();
            }

            if(pfm.getPath().size() < shortestpath.size()){
                shortestpath = (JSONArray) pfm.getPath();
            }
        }
    }
}

