package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.HashSet;

public class TestAgent2 extends Agent {

    protected void setup() {
        System.out.println("Hello. My name is "+getLocalName());

        Object[] args = getArguments();

        if (args != null) {
            for (Object arg : args) {
                System.out.println("- " + arg);
            }
        }

        HashSet<String> neighbours = null;

        if (args.length >= 2 && args[1] != null) {
            neighbours = (HashSet<String>) args[1];
        }

        System.out.println(neighbours);

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

        /*
        try {
            System.out.println(getArguments()[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */

    }


}
