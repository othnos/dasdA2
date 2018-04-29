package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PathGui extends JFrame {
    private Agent myAgent;

    private JTextField titleField;

    PathGui(Agent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Proxy local name:"));
        titleField = new JTextField(15);
        p.add(titleField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Search shortest path");
        addButton.addActionListener(ev -> {
            try {
                // Get agent's local name
                String agentLocalName = titleField.getText().trim();

                // Throws error if agent not found
                try {
                    myAgent.getContainerController().getAgent(agentLocalName);
                } catch (Exception e) {
                    System.out.println("Agent " + agentLocalName + " not found");
                    return;
                }

                // Test print which agent the message is send
                System.out.println("Agent " + agentLocalName + " received " +
                        "REQUEST.");

                // Get aid by agent's local name
                AID target = new AID(agentLocalName, AID.ISLOCALNAME);

                // Messages to the agents needs to be in JSON format
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("action", "get-shortest-path");

                // Create REQUEST message
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(target);
                // Convert JSON Object to string
                msg.setContent(jsonObject.toJSONString());

                myAgent.send(msg);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(PathGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
