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
import helpers.ConveyorAgent.*;
import helpers.RouteFinder;

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

    //
    private int msgId = 1;

    private int getMsgId() {
        return msgId++;
    }


    RouteFinder routeFinder = new RouteFinder(this);

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

    private class FindPath extends Behaviour {
        JSONArray shortestPath;
        String destination;
        boolean moveToNext;

        FindPath(JSONArray shortestPath_,
                 String destination_,
                 boolean moveToNext_) {
            shortestPath = shortestPath_;
            moveToNext = moveToNext_;
            destination = destination_;
        }

        @Override
        public void action() {
            if (shortestPath == null ||
                    shortestPath.isEmpty()) {
                System.out.println("Path does not exist");
                return;
            }

            System.out.println("shortest path is:" + shortestPath);

            if (moveToNext) {
                String target = shortestPath.get(1).toString();
                System.out.println("Started to move from conveyor " + getLocalName() +
                    " to conveyor " + target);
                Behaviour nextBehaviour = new MoveToNext(target, destination);
                addBehaviour(nextBehaviour);
            }

            routeFinder.getShortestRoute().clear();
        }

        @Override
        public boolean done() {
            return true;
        }
    }

    private class MoveToNext extends Behaviour {
        String destination;
        String target;

        MoveToNext(String target_, String destination_) {
            target = target_;
            destination = destination_;
        }

        @Override
        public void action() {
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(new AID(target, AID.ISLOCALNAME));
            PathFindingMessage pfm;

            try {
                pfm = new PathFindingMessage(
                        target,
                        destination,
                        "move"
                );
                req.setContent(pfm.getAsJSONObject().toJSONString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            myAgent.send(req);
            System.out.println("Agent " + getLocalName() + " moved the pallet to the " +
                    "conveyor " + target + " with destination " + destination);
        }

        @Override
        public boolean done() {
            return true;
        }
    }

    //behaviour to act upon the json:s
    private class sendPathFindingMessage extends Behaviour {
        private PathFindingMessage pfm;

        private sendPathFindingMessage(PathFindingMessage pfm_) {
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
            pfm.setAction("findShortestPath");
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
         * Whitelist for methods which can be called
         * by received messages
         */
        HashSet<String> methodWhitelist = new HashSet<String>(Arrays.asList(
                "getShortestPath",
                "findShortestPath",
                "receiveAccept",
                "move"
        ));

        /**
         * Constructor
         *
         * @param jsonParser: JSON parser
         * @param agent:      Agent
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
         * Chooses the correct action
         *
         * @param data: Data as JSON object
         * @throws Exception
         */
        public void route(JSONObject data) throws Exception {
            // Get the action from the data
            String action = getAction(data);

            pfm = new PathFindingMessage(data);

            System.out.println("Conveyor " + getLocalName() +
                    " received data: " + data);

            java.lang.reflect.Method method;

            try {
                // Throw an exception if action couldn't be found
                // from the whitelist
                if (!methodWhitelist.contains(action)) {
                    throw new Exception("Called action " + action +
                            " not whitelisted");
                }

                // Get the wanted method
                method = this.getClass().getDeclaredMethod(action
                        // , JSONObject.class
                );
                // Call the method
                method.invoke(this
                        // , data
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * @throws Exception
         */
        void getShortestPath() throws Exception {
            addBehaviour(
                    new WakerBehaviour(myAgent, timeOut) {
                        protected void onWake() {
                            //decide target
                            addBehaviour(new FindPath(shortestpath,
                                    pfm.getDestination(),
                                    false)
                            );
                        }
                    });

            // Populate neighbours with find shortest path
            findShortestPath();
        }

        void move() throws Exception {
            // If conveyor is not ready or the conveyor
            // is the destination we won't start the path
            // finding
            if (!isConveyorReady() ||
                    check_ifDest()) {
                return;
            }

            addBehaviour(
                    new WakerBehaviour(myAgent, timeOut) {
                        protected void onWake() {
                            //decide target
                            addBehaviour(new FindPath(shortestpath,
                                    pfm.getDestination(),
                                    true)
                            );
                        }
                    });

            // Populate neighbours with find shortest path
            findShortestPath();
        }

        /**
         *
         */
        void findShortestPath() {
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
        }

        /**
         *
         */
        void receiveAccept() {
            routeFinder.addRoute((JSONArray) pfm.getPath());
            shortestpath = routeFinder.getShortestRoute();
        }

        /**
         * @return
         */
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

        /**
         * @return
         */
        boolean check_ifDest() {
            if (pfm.getSource().equals(pfm.getDestination())) {
                System.out.println("Arrived to destination.");
                return true;
            }

            return false;
        }

        /**
         * Returns if the path finding message has gone through a loop
         *
         * @return
         */
        boolean checkIfLoop() {
            boolean sourceEqualsThisAgent = pfm.getSource().equals(agent.getLocalName());

            HashSet<String> foundConveyors = new HashSet<>();

            for (Object instance : pfm.getPath()) {
                if (
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

        /**
         * @return
         */
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
}

