package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ProxyTestAgent extends Agent {
    /**
     * JSON parser
     */
    private JSONParser jsonParser = new JSONParser();

    /**
     * MessageRouter abstract class
     */
    protected abstract class MessageRouter extends CyclicBehaviour {
        /**
         * Message template for filtering which messages to
         * access from message queue
         */
        MessageTemplate mt_;

        /**
         * Constructor
         * @param mt:
         */
        MessageRouter(MessageTemplate mt) {
            mt_ = mt;
        }

        @Override
        public void action() {
            ACLMessage msgRx = receive(mt_);

            // Block if not message received
            if (msgRx == null) {
                block();
                return;
            }

            try {
                //System.out.println(msgRx.toString());
                JSONObject data = (JSONObject) jsonParser.parse(msgRx.getContent());
                route(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Call the right action defined in data JSONObject
         * @param data: Should contain value named "action"
         * @throws Exception: If action is null or not found
         */
        abstract void route(JSONObject data) throws Exception;

        /**
         * Get the action string from the data
         * @param data: Should contain value named "action"
         * @return action string
         * @throws Exception: If action is null
         */
        String getAction(JSONObject data) throws Exception {
            Object action = data.get("action");

            if (action == null) {
                throw new Exception("Empty action");
            }

            return action.toString();
        }
    }

    /**
     * Router for ACLMessage.REQUEST messages
     */
    private class RequestRouter extends MessageRouter {
        /**
         * Constructor
         */
        private RequestRouter() {
            // Access only ACLMessage.REQUEST from message queue
            super(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        }

        /**
         *
         * @param data
         * @throws Exception
         */
        void route(JSONObject data) throws Exception {
            String action = getAction(data);

            switch (action.toLowerCase()) {
                case "get-shortest-path":
                    System.out.println("Shortest path gotten");
                    break;
                default:
                    throw new Exception("Action not found.");
            }
        }
    }

    /**
     * Router for ACLMessage.PROXY messages
     */
    private class ProxyRouter extends MessageRouter {
        /**
         * Constructor
         */
        private ProxyRouter() {
            // Access only ACLMessage.PROXY from message queue
            super(MessageTemplate.MatchPerformative(ACLMessage.PROXY));
        }

        /**
         *
         * @param data
         * @throws Exception
         */
        void route(JSONObject data) throws Exception {
            String action = getAction(data);

            switch (action.toLowerCase()) {
                case "find-shortest-path":
                    System.out.println("Shortest path found");
                    break;
                default:
                    throw new Exception("Action not found.");
            }
        }
    }

    protected void setup() {
        addBehaviour(new RequestRouter());
        addBehaviour(new ProxyRouter());
    }
}
