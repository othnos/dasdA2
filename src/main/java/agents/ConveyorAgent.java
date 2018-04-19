package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jdk.nashorn.internal.parser.JSONParser;
import netscape.javascript.JSObject;

import javax.print.attribute.IntegerSyntax;
import java.util.*;

public class ConveyorAgent extends Agent {


    private ArrayList<Integer> neighbours;
    private boolean palletStatus;
    private boolean hasWorkStation;
    private int conveyorStatus;
    private JSObject route;
    private Integer name;

    //way to implement behaviours
    private class pingBehaviour extends Behaviour{

        public void action(){

        }
        public boolean done(){

        }

    }



    protected void setup(){
        System.out.println("Hello World. I’m a conveyoragent!");
        System.out.println("My local-name is "+getAID().getLocalName());
        System.out.println("My GUID is "+getAID().getName()); System.out.println("My addresses are:");
        Iterator it = getAID().getAllAddresses(); while (it.hasNext()) { System.out.println("- "+it.next()); }
        neighbours = new ArrayList<Integer>();
        palletStatus = false;
        hasWorkStation = false;
        conveyorStatus = 0;
        name = 0;
    }

    public void setName(int name_){
        name = name_;
    }

     public void addNeighbour(int number){
        neighbours.add(number);
    }

    public ArrayList<Integer> getNeighbours(){
        return neighbours;
    }

    public void setWorkstation(){
        hasWorkStation = true;
    }

    public void resetWorkstation(){
        hasWorkStation = false;
    }

    public void setPalletStatus(){
        palletStatus = true;
    }

    public void resetPalletStatus(){
        palletStatus = false;
    }

    public void setConveyorStatus(int status){
        conveyorStatus = status;
    }

    public void setJSONobject(JSObject route_){
        route = route_;
    }

    public void addToJSON(){

    }
}
