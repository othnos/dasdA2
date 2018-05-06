package helpers.ConveyorAgent;

import helpers.MessageRouter;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Router for ACLMessage.REJECT_PROPOSAL messages
 */
public class RejectProposalRouter extends MessageRouter {
    /**
     * Constructor
     */
    public RejectProposalRouter(JSONParser jsonParser, Agent agent) {
        // Access only ACLMessage.REQUEST from message queue
        super(
                MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL),
                jsonParser,
                agent
        );
    }

    /**
     *
     * @param data
     * @throws Exception
     */
    public void route(JSONObject data) throws Exception {
        try {
            String action = getAction(data);

            switch (action) {
                default:
                    // Just ignore the found message
            }
        } catch (Exception e) {
            // Catch the "action not found" exception
            // and do not print any debugging
        }
    }
}