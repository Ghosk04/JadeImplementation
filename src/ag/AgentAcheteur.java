package ag;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;
import java.util.Map;

public class AgentAcheteur extends Agent {
    private String medicamentRecherche = "Paracétamol";
    private Map<AID, Offre> offres = new HashMap<>();
    private int reponsesRecues = 0;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " : Recherche " + medicamentRecherche + " avec une optimisation d’utilité.");

        AID[] pharmacies = {
            new AID("Pharmacie1", AID.ISLOCALNAME),
            new AID("Pharmacie2", AID.ISLOCALNAME),
            new AID("Pharmacie3", AID.ISLOCALNAME)
        };

        for (AID pharmacie : pharmacies) {
            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setContent(medicamentRecherche);
            message.addReceiver(pharmacie);
            send(message);
            try {
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        addBehaviour(new CollecteOffres(pharmacies.length));
    }

    private class CollecteOffres extends Behaviour {
        private int nbAttendus;

        public CollecteOffres(int nbAttendus) {
            this.nbAttendus = nbAttendus;
        }

        @Override
        public void action() {
            ACLMessage reponse = receive();
            if (reponse != null) {
                reponsesRecues++;
                if (reponse.getPerformative() == ACLMessage.PROPOSE) {
                    String[] data = reponse.getContent().split(";");
                    int prix = Integer.parseInt(data[0].trim());
                    double distance = Double.parseDouble(data[1].trim());

                    offres.put(reponse.getSender(), new Offre(prix, distance));
                }
                try {
                    Thread.sleep(5000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean done() {
            try {
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (reponsesRecues >= nbAttendus) {
                AID meilleurePharmacie = null;
                double meilleureUtilité = Double.NEGATIVE_INFINITY;

                for (Map.Entry<AID, Offre> entry : offres.entrySet()) {
                    double utilité = calculerUtilité(entry.getValue());
                    if (utilité > meilleureUtilité) {
                        meilleureUtilité = utilité;
                        meilleurePharmacie = entry.getKey();
                    }
                }

                if (meilleurePharmacie != null) {
                    System.out.println(getLocalName() + " : Meilleure offre chez " + meilleurePharmacie.getLocalName());
                    ACLMessage achat = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    achat.addReceiver(meilleurePharmacie);
                    send(achat);
                }
                return true;
            }
            return false;
        }

        private double calculerUtilité(Offre offre) {
            return -(offre.prix) + 2 * (100 - offre.distance); // Pondération
        }
    }

    class Offre {
        int prix;
        double distance; 
        Offre(int prix, double distance2) { this.prix = prix; this.distance = distance2; }
    }
}
