package agents;

import jade.core.*;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.CreateAgent;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class TestAgent extends Agent {

    /**
     * JSON decoding example
     *
     * Decoding examples:
     * https://code.google.com/archive/p/json-simple/wikis/DecodingExamples.wiki
     */
    private void printFileExample() {
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

            // Value of the JSON key can be then accessed like this
            String idleTime = configJson.get("idleTime").toString();
            System.out.println(idleTime);

            // Retrieving JSONArray
            JSONArray it2 = (JSONArray)configJson.get("paths");

            // We can check if element exists by checking if it's not null
            if (it2 != null) {
                // Iterating through JSONArray
                for (Object o : it2) {
                    // In this case we have array of objects
                    JSONObject jo = (JSONObject)o;

                    // Print conveyor and if it has workstation
                    System.out.println(
                            "Conveyor: " + jo.get("name").toString() +
                                    ", Has Workstation: " + jo.get("hasWorkstation").toString()
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setup() {
        System.out.println("Hello. My name is "+getLocalName());
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

        printFileExample();

        if (false) {
            int i = 1;

            try {
                System.out.println(getContainerController().getContainerName());

                HashSet<String> neighbours = new HashSet<>();
                neighbours.add("cnv_1");
                neighbours.add("cnv_2");

                System.out.println(Boolean.parseBoolean(""));
                System.out.println(Boolean.parseBoolean("     true".trim()));
                System.out.println(Boolean.parseBoolean("true    "));
                System.out.println(Boolean.parseBoolean("true"));

                // Creating new agent
                getContainerController().createNewAgent("cnv_" + i,
                        "agents.TestAgent2",
                        new Object[]{"cnv_1", false, neighbours}).start();
                System.out.println(
                        getContainerController().getAgent("cnv_" + i).getName()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*
        try {
            System.out.println(getArguments()[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

    }


}
