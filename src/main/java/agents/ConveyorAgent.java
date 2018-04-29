package agents;

import FIPA.AgentID;
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
    //private boolean hasWorkStation;
    private int conveyorStatus;
    private HashSet<String> neighbours;

    protected void setup() {

        conveyorStatus = 0;
        neighbours = new HashSet<String>();

        addBehaviour(new ReceiveRefuse(this));
    }

    //behaviour to act upon the json:s
    private class jsonBehaviourSend extends Behaviour {

        private JSONObject route;

        private jsonBehaviourSend(JSONObject route_){

            route = route_;

        }
        //runs once on call up
        public void onStart() {

        }

        public void action() {

            if (myAgent.getLocalName() == route.get("source")){
                ACLMessage req = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                req.addReceiver((AID)route.get("source"));
                myAgent.send(req);
            }

            //sends the route to the original source if no neighbours
            else if(neighbours.isEmpty()){
                ACLMessage req = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                req.addReceiver((AID)route.get("source"));
                myAgent.send(req);
            }

            else {
                boolean found = false;

                for (String neighbour : neighbours) {

                    //handle the "found destination"
                    if (neighbour.equals(route.get("destination").toString())) {
                        found = true;
                        ACLMessage req = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        req.addReceiver((AID)route.get("source"));
                        try {
                            req.setContentObject(route.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        myAgent.send(req);
                    }
                }

                if (found == false) {
                    JSONArray it2 = (JSONArray) route.get("paths");
                    //route.put("path", this.name);
                    it2.add(myAgent.getLocalName());

                    for (String neighbour : neighbours) {
                        ACLMessage req = new ACLMessage(ACLMessage.REQUEST);

                        //adds he neighbour as receiver of the message
                        req.addReceiver(getAID(neighbour));
                        try {
                            req.setContentObject(route.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        myAgent.send(req);
                    }
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

        private ReceiveRequest() {
        }

        public void action() {
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                try {
                    JSONObject route_ = (JSONObject) parser.parse(msg.getContent());
                    addBehaviour(new jsonBehaviourSend(route_));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //for acquiring possible paths for the workpiece
    private class ReceiveAccept extends CyclicBehaviour{
        private MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        JSONParser parser = new JSONParser();
        private ReceiveAccept(){
        }
        public void action() {
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                try {
                    JSONObject route_ = (JSONObject) parser.parse(msg.getContent());
                    addBehaviour(new jsonBehaviourSend(route_));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            else {
                block();
            }
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
                System.out.println("Tulostuuko tämä viesti?");

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
}

