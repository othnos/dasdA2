package agents;

import helpers.PathGui;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.StaleProxyException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

public class TestAgent extends Agent {
    // The GUI
    private PathGui myGui;

    /**
     * Setup the test agent
     */
    protected void setup() {
        System.out.println("Hello. My name is "+getLocalName());

        // Create and show the GUI
        myGui = new PathGui(this);
        myGui.showGui();

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

        // Create couple proxy test agents
        try {
            getContainerController().createNewAgent("proxy1",
                    "agents.ProxyTestAgent",
                    new Object[]{"proxy2"}).start();
            getContainerController().createNewAgent("proxy2",
                    "agents.ProxyTestAgent",
                    new Object[]{"proxy3"}).start();
            getContainerController().createNewAgent("proxy3",
                    "agents.ProxyTestAgent",
                    new Object[]{""}).start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

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

            JSONObject asd = new JSONObject();
            asd.put("name", "cnv_8");
            asd.put("hasWorkstation", "false");

            it2.add(asd);

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

            JSONArray newPath = new JSONArray();
            newPath.add("Joo");
            newPath.add("Jaa");

            configJson.put("NewPath", newPath);

            System.out.println(configJson.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void testi() {
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


}
