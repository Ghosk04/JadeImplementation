package ag;

import java.util.Arrays;

import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AcheteurFSM extends Agent {
    private static final String STATE_RECHERCHE = "Recherche";
    private static final String STATE_ATTENTE = "Attente";
    private static final String STATE_VERIFICATION = "Verification";
    private static final String STATE_ACHAT = "Achat";
    private static final String STATE_FIN = "Fin";

    private String[] medicaments;  // Liste des médicaments demandés
    private double porteMonnaie;   // Solde de l'acheteur
    private String reponseVendeur;
    private double totalAPayer;
    private String nom; 

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            porteMonnaie = (Double)args[1];  // Solde initial
            nom = (String)args[0];
            medicaments = new String[3];
            medicaments = (String[])args[2];
            System.out.println("Acheteur : " + nom + " | Porte-monnaie : " + porteMonnaie + " | Achats : " + Arrays.toString(medicaments));
            System.out.println(" ");

            System.out.println(getLocalName() + " : J'ai " + porteMonnaie + " FCFA et je veux acheter :");
            System.out.println(" ");
            for (String med : medicaments) {
                System.out.println("- " + med);
            }
            System.out.println(" ");

            FSMBehaviour fsm = new FSMBehaviour(this) {
                @Override
                public int onEnd() {
                    System.out.println(getLocalName() + " : Fin du processus.");
                    doDelete();
                    return super.onEnd();
                }
            };

            fsm.registerFirstState(new RechercheMedicament(), STATE_RECHERCHE);
            fsm.registerState(new AttenteReponse(), STATE_ATTENTE);
            fsm.registerState(new VerificationFonds(), STATE_VERIFICATION);
            fsm.registerState(new AchatMedicament(), STATE_ACHAT);
            fsm.registerLastState(new FinProcessus(), STATE_FIN);

            fsm.registerTransition(STATE_RECHERCHE, STATE_ATTENTE, 1);
            fsm.registerTransition(STATE_ATTENTE, STATE_VERIFICATION, 1);
            fsm.registerTransition(STATE_VERIFICATION, STATE_ACHAT, 1);
            fsm.registerTransition(STATE_VERIFICATION, STATE_FIN, 0);
            fsm.registerTransition(STATE_ACHAT, STATE_FIN, 1);

            addBehaviour(fsm);
        } else {
            System.out.println("Arguments manquants !");
            System.out.println(" ");
            doDelete();
        }
    }

    private class RechercheMedicament extends OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage demande = new ACLMessage(ACLMessage.REQUEST);
            demande.addReceiver(getAID("Pharmacie"));
            demande.setContent(String.join(",", medicaments));
            send(demande);

            try {
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int onEnd() {
            return 1;
        }
    }

    private class AttenteReponse extends SimpleBehaviour {
        private boolean recu = false;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage reponse = receive(mt);

            if (reponse != null) {
                reponseVendeur = reponse.getContent();
                String[] details = reponseVendeur.split("TOTAL:");
                totalAPayer = Double.parseDouble(details[1]);
                recu = true;

                try {
                    Thread.sleep(5000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return recu;
        }

        @Override
        public int onEnd() {
            return 1;
        }
    }

    private class VerificationFonds extends OneShotBehaviour {
        @Override
        public void action() {
            if (totalAPayer <= porteMonnaie) {
                porteMonnaie -= totalAPayer;
                System.out.println("Porte monnaie de " + nom + " suffisant");
                System.out.println(" ");
                System.out.println("Solde restant : " + porteMonnaie);
                System.out.println(" ");
            } else {
                System.out.println("Porte monnaie de " + nom + " insuffisant");
                System.out.println(" ");
            }
        }

        @Override
        public int onEnd() {
            return (totalAPayer <= porteMonnaie) ? 1 : 0;
        }
    }

    private class AchatMedicament extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println(getLocalName() + " : Achat réussi !");
            System.out.println(" ");
            try {
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int onEnd() {
            return 1;
        }
    }

    private class FinProcessus extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println(getLocalName() + " : Processus terminé.");
            System.out.println(" ");
        }
    }
}
