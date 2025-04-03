package ag;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class AgentPharmacie extends Agent {
    private String medicamentDisponible = "Paracétamol";
    private double distance; 
    private int prix;

    @Override
    protected void setup() {
        prix = new Random().nextInt(5000) + 1000; // Prix entre 1000 et 6000 FCFA
        distance = new Random().nextDouble(100) + 0.5;
        System.out.println(getLocalName() + " : Je vends " + medicamentDisponible + " à " + prix + " FCFA et je suis situé à " + distance + "m.");
        addBehaviour(new GestionDemande());
    }

    private class GestionDemande extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive();
            if (message != null) {

                if (message.getPerformative() == ACLMessage.REQUEST){
                    ACLMessage reponse = message.createReply();

                    if (message.getContent().equalsIgnoreCase(medicamentDisponible)) {
                        reponse.setPerformative(ACLMessage.PROPOSE);
                        reponse.setContent(String.valueOf(prix) + ";" + String.valueOf(distance));
                    } else {
                        reponse.setPerformative(ACLMessage.REFUSE);
                        reponse.setContent("Indisponible");
                    }
                    send(reponse);
                    try {
                        Thread.sleep(5000); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    System.out.println(getLocalName() + " : Médicament vendu !");
                }
                
            } else {
                block();
            }
        }
    }
}
