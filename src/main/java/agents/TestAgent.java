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

public class TestAgent extends Agent {

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

        int i = 1;

        try {
            System.out.println(getContainerController().getContainerName());

            // Creating new agent
            getContainerController().createNewAgent("cnv_" + i,
                    "agents.TestAgent2",
                    null).start();
        } catch (Exception e) {
            e.printStackTrace();
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
