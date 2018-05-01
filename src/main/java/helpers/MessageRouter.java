package helpers;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * MessageRouter abstract class
 */
public abstract class MessageRouter extends CyclicBehaviour {
    /**
     * Message template for filtering which messages to
     * access from message queue
     */
    protected MessageTemplate mt;

    /**
     * JSON parser
     */
    protected JSONParser jsonParser;

    /**
     * Agent
     */
    protected Agent agent;

    /**
     * Constructor
     * @param mt_
     * @param jsonParser_
     * @param agent_
     */
    protected MessageRouter(MessageTemplate mt_,
                  JSONParser jsonParser_,
                  Agent agent_) {
        mt = mt_;
        jsonParser = jsonParser_;
        agent = agent_;
    }

    @Override
    public void action() {
        ACLMessage msgRx = agent.receive(mt);

        // Block if not message received
        if (msgRx == null) {
            block();
            return;
        }

        try {
            //System.out.println(msgRx.toString());
            JSONObject data = (JSONObject) jsonParser.parse(
                    msgRx.getContent()
            );
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
    public abstract void route(JSONObject data) throws Exception;

    /**
     * Get the action string from the data
     * @param data: Should contain value named "action"
     * @return action string
     * @throws Exception: If action is null
     */
    protected String getAction(JSONObject data) throws Exception {
        Object action = data.get("action");

        if (action == null) {
            throw new Exception("Empty action");
        }

        return action.toString();
    }
}
