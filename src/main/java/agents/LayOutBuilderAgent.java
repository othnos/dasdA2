package agents;

//import com.sun.org.apache.xml.internal.resolver.Catalog;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.*;
/*import jade.core.behaviours.*;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import javafx.util.Pair;

import java.nio.channels.FileChannel;
import java.util.ArrayList;*/
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;



public class LayOutBuilderAgent extends Agent {

    private HashMap<String, Conveyor> layout = new HashMap<>();

    private class Conveyor{
        private String nick_;
        private boolean hasWorkstation_;
        private HashSet<String> neighbours_;

        public Conveyor(String nick, boolean ws){

            nick_ = nick;
            hasWorkstation_ = ws;
            neighbours_ = new HashSet<>();
        }

        public void addNeighbour(String neighbour){
            neighbours_.add(neighbour);
        }
    }


    protected void read(/*String[] args*/)throws IOException{
        String path = "C:\\Skole\\Distributed Automation Systems design\\Assignments\\Assignment2\\JadeMaven-master\\JadeMaven-master\\testLayouts";
        File fileToRead = new File(path + "\\layout1.csv");

        BufferedReader b = new BufferedReader(new FileReader(fileToRead));
        String readLine = "";
        while ((readLine = b.readLine()) != null){
            String[] items = readLine.split(";");

            if(items.length < 2){
                continue;
            }

            //skip the title row
            if (items[0].trim().equals("Cnv")){
                continue;
            }
                layout.put(items[0],
                        new Conveyor(
                                items[0].trim(),
                                Boolean.parseBoolean(items[1].trim())));

            for (String piece: items) {
                System.out.print(piece + " ");
            }
            System.out.println();
        }

    }

    protected void setup() {
        System.out.println("Hello. My name is " + getLocalName());
        try {
            read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msgRx = receive();
                if (msgRx != null) {
                    System.out.println(msgRx);
                    ACLMessage msgTx = msgRx.createReply();
                    msgTx.setContent("Hello!");
                    send(msgTx);
                } else {
                    block();
                }
            }
        });
    }
}

