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

    private String name;

    protected void setup() {

        conveyorStatus = 0;
        name = "";
        neighbours = new HashSet<String>();

    }



        //behaviour to act upon the json:s
    private class jsonBehaviourSend extends Behaviour {

        private JSONObject route;
        private Integer name;

        private jsonBehaviourSend(JSONObject route_, Integer name_){

            route = route_;
            name = name_;

        }
        //runs once on call up
        public void onStart() {

        }

        public void action() {

            if (this.name == route.get("source")){

            }

            //sends the route to the original source if no neighbours
            else if(neighbours.isEmpty()){
                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                req.addReceiver((AID)route.get("source"));
                myAgent.send(req);
            }


            else {
                JSONArray it2 = (JSONArray)route.get("paths");
                //route.put("path", this.name);
                it2.add(this.name);

                for (String neighbour : neighbours) {
                    ACLMessage req = new ACLMessage(ACLMessage.REQUEST);

                    //adds he neighbour as receiver of the message
                    req.addReceiver(getAID(neighbour));
                    try {
                        req.setContentObject(route.toJSONString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    myAgent.send(req);
                }
            }
        }
        public boolean done() {
            return true;
        }
    }

    private class ReceiveAccept extends CyclicBehaviour{
        private ArrayList<JSONArray> pathList;
        private MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
        private ReceiveAccept(){
        }
        public void action() {
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                pathList.add((JSONArray)route.get("paths"));
                // ACCEPT received. Process it ...
                }
            else {
                block();
            }
        }
    }
    private class ReceiveRefuse extends CyclicBehaviour{
        private MessageTemplate mt2 = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
        private ReceiveRefuse(){
        }
        public void action() {
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                // REJECT RECEIVED, ignore it...
            }
            else {
                block();
            }
        }
    }

    public void choosingPath(){
        addBehaviour(new WakerBehaviour(this, 5000) {
            protected void onWake() {

            }
        });
    }
    public void actUponJSON(JSONObject route_, Integer name_){
        addBehaviour(new jsonBehaviourSend(route_, name_));
    }
    public void setName(String name_){
        name = name_;
    }

    public void receiveMessages() {
        ACLMessage msg = receive();
        if (msg != null) {

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
