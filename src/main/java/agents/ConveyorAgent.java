package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import javax.print.attribute.IntegerSyntax;
import java.util.*;

public class ConveyorAgent extends Agent {
    private ArrayList<Integer> neighbours;
    private boolean palletStatus;
    private boolean hasWorkStation;
    private int conveyorStatus;

    protected void setup(){
        neighbours = new ArrayList<Integer>();
        palletStatus = false;
        hasWorkStation = false;
        conveyorStatus = 0;
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


}
