package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class PathGui extends JFrame {
    private Agent myAgent;

    private JTextField srcField, destField;

    /**
     * Message queue
     */
    private HashMap<AID, AID> messageQueue = new HashMap<>();

    PathGui(Agent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));

        p.add(new JLabel("Source:"));
        srcField = new JTextField(15);
        p.add(srcField);

        p.add(new JLabel("Destination:"));
        destField = new JTextField(15);
        p.add(destField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add msg");
        addButton.addActionListener(ev -> {
            try {
                // Get agent's local name
                String src = srcField.getText().trim();
                String dest = destField.getText().trim();

                // Throws error if agent not found
                try {
                    myAgent.getContainerController().getAgent(src);
                    myAgent.getContainerController().getAgent(dest);
                } catch (Exception e) {
                    System.out.println("Source " + src + " or destination " + dest +
                            " not found");
                    return;
                }

                // Push source/destination AID pair to the messageQueue to send
                messageQueue.put(
                    new AID(src, AID.ISLOCALNAME),
                    new AID(dest, AID.ISLOCALNAME)
                );

                System.out.println("Message queue: " + messageQueue);

                //System.out.println("Src: " + src + ", Dst: " + dest + " added to " +
                //        "message queue");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(PathGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        p = new JPanel();
        p.add(addButton);

        JButton sendButton = new JButton("Search shortest path");
        sendButton.addActionListener(ev -> {
            for (Map.Entry<AID, AID> entry : messageQueue.entrySet()) {
                try {
                    // Messages to the agents needs to be in JSON format
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("action", "get-shortest-path");
                    jsonObject.put("source", entry.getKey().toString());
                    jsonObject.put("destination", entry.getValue().toString());

                    // Create REQUEST message
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(entry.getKey());
                    // Convert JSON Object to string
                    msg.setContent(jsonObject.toJSONString());

                    myAgent.send(msg);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PathGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        p.add(sendButton);

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
