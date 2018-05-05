package agents;

//import com.sun.org.apache.xml.internal.resolver.Catalog;
import helpers.PathGui;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;


public class LayOutBuilderAgent extends Agent {

    private HashMap<String, Conveyor> layout = new HashMap<>();

    /**
     * Path GUI
     */
    private PathGui pathGUI;

    private class Conveyor{
        private String nick_;
        private boolean hasWorkstation_;
        private HashSet<String> neighbours_ = new HashSet<>();
        private HashMap<String, Integer> config_;


        public Conveyor(String nick, boolean ws, int wt, int tpt, int to){

            nick_ = nick;
            hasWorkstation_ = ws;
            config_ = new HashMap<>();
            config_.put("workTime", wt);
            config_.put("throughputTime", tpt);
            config_.put("timeout", to);
        }

        public void addNeighbour(String neighbour){
            neighbours_.add(neighbour);
        }

        public HashSet<String> getNeighbours(){
            return neighbours_;
        }

        public String getNick(){
            return nick_;
        }

        public Object [] getCreateData(){
            Object [] data = new Object[]{hasWorkstation_, neighbours_,config_};

            return data;
        }
    }

    protected void createAgents(){
                                        //new Object[]{"cnv_1", false, neighbours}).start();
        for (String cnv: layout.keySet()) {
            // Creating new agent
            try {
                getContainerController().createNewAgent(layout.get(cnv).getNick(), "agents.ConveyorAgent",
                        layout.get(cnv).getCreateData()).start();

                System.out.println(
                        getContainerController().getAgent(cnv).getName()
                );

            } catch (StaleProxyException e) {
                e.printStackTrace();
            } catch (ControllerException e) {
                e.printStackTrace();
            }

        }


    }

    protected JSONObject getConfigData(){
        //get json config data

            // Init new parser to parse the config.json file
            JSONParser parser = new JSONParser();

            try {
                // Config's filepath
                String filepath = "./testLayouts/config.json";

                // Read the whole config file to String s
                String s = String.join("", // Join List<String> with "" as delimiter
                        Files.readAllLines(         // Read rows to the List<String>
                                Paths.get(filepath) // Form the Path from the filepath string
                        )
                );

                // Use JSONParser to parse string to JSONObject
                JSONObject configJson = (JSONObject)parser.parse(s);

                /* Value of the JSON key can be then accessed like this
                String idleTime = configJson.get("idleTime").toString();
                System.out.println(idleTime);*/

                return configJson;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new JSONObject();
    }


    protected void read(/*String[] args*/)throws IOException{

        JSONObject data = getConfigData();


        //String path = "C:\\Skole\\Distributed Automation Systems design\\Assignments\\Assignment2\\JadeMaven-master\\JadeMaven-master\\testLayouts";
        //File fileToRead = new File(path + "\\layout1.csv");Paths.get(filepath)

        String filepath = "./testLayouts/layout1.csv";

        File fileToRead = new File(filepath);

        BufferedReader b = new BufferedReader(new FileReader(fileToRead));
        String readLine = "";
        while ((readLine = b.readLine()) != null){
            String[] items = readLine.split(";");

            if(items.length > 2 || items.length < 1) {
                System.out.println("error, wrong amount of parameters, "+ readLine);
                continue;
            }

            //skip the title row
            if (items[0].trim().equals("Cnv")){
                continue;
            }
                layout.put(items[0],
                        new Conveyor(
                                items[0].trim(),
                                Boolean.parseBoolean(items[1].trim()),
                                Integer.parseInt(data.get("workTime").toString()),
                                Integer.parseInt(data.get("throughputTime").toString()),
                                Integer.parseInt(data.get("timeout").toString())
                                )
                );

            for (String piece: items) {
                //System.out.print(piece + " ");
            }
            //System.out.println();
        }


        // read neighbour information from other csv file
        //path = "C:\\Skole\\Distributed Automation Systems design\\Assignments\\Assignment2\\JadeMaven-master\\JadeMaven-master\\testLayouts";
        //fileToRead = new File(path + "\\layout1neighbors.csv");

        filepath = "./testLayouts/layout1neighbors.csv";

        fileToRead = new File(filepath);


        b = new BufferedReader(new FileReader(fileToRead));
        readLine = "";
        while ((readLine = b.readLine()) != null){
            String[] items = readLine.split(";");

            if (items[0].trim().equals("Cnv")) {
                continue;
            }

            if(items.length > 2 || items.length < 1) {
                System.out.println("error, wrong amount of parameters, "+ readLine);
                continue;
            }

            if (layout.get(items[0]) == null){
                System.out.println("conveyor " + items[0] + " does not exist");
                        continue;
            }

            //skip the title row
            if (items[0].trim().equals("Cnv")) {
                continue;
            }

            layout.get(items[0]).addNeighbour(items[1]);
        }

        if (false) {
            for (String cnv : layout.keySet()) {
                System.out.print(cnv + " has neighbours ");
                for (String instance : layout.get(cnv).getNeighbours()) {
                    System.out.print(instance + " ");
                }
                System.out.println();
            }
        }
    }

    protected void setup() {
        //System.out.println("Hello. My name is " + getLocalName());

        // Create and show the GUI
        pathGUI = new PathGui(this);
        pathGUI.showGui();

        try {
            read();
            createAgents();
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

        //sendMsg();
    }

    private void sendMsg() {
        // Messages to the agents needs to be in JSON format
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "get-shortest-path");
        jsonObject.put("source", "cnv_1");
        jsonObject.put("destination", "cnv_10");

        // Create REQUEST message
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(new AID("cnv_1", AID.ISLOCALNAME));
        // Convert JSON Object to string
        msg.setContent(jsonObject.toJSONString());

        this.send(msg);
    }
}

