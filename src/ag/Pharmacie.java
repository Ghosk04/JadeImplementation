package ag;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.Map;

public class Pharmacie extends Agent {
    private Map<String, Integer> stockProduits; // Stock des produits
    private Map<String, Double> prixProduits;  // Prix des produits

    @Override
    protected void setup() {
        // Récupération des paramètres envoyés depuis le Container
        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            String[] produits = (String[]) args[0];
            Integer[] stocks = (Integer[]) args[1];
            Double[] prix = (Double[]) args[2];  // Correction : pas de redéclaration de prixProduits

            stockProduits = new HashMap<>();
            prixProduits = new HashMap<>();

            for (int i = 0; i < produits.length; i++) {
                stockProduits.put(produits[i], stocks[i]);
                prixProduits.put(produits[i], prix[i]);  // Correction ici
            }
            
            System.out.println("📦 Pharmacie en ligne avec les produits suivants :");
            for (String produit : stockProduits.keySet()) {
                System.out.println(produit + " - Stock : " + stockProduits.get(produit) + " - Prix : " + prixProduits.get(produit) + " FCFA");
                System.out.println(" ");
            }
        } else {
            System.out.println("⚠️ Erreur : Pas de paramètres reçus pour la pharmacie !");
            System.out.println(" ");
        }

        try {
            Thread.sleep(5000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Ajouter un comportement qui répond aux demandes d'achat
        addBehaviour(new GestionAchats());
    }

    private class GestionAchats extends CyclicBehaviour {
        @Override
        public void action() {

            afficherStock();

            try {
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage demande = receive(mt);
            try {
                Thread.sleep(5000); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            if (demande != null) {
                System.out.println(getLocalName() + " : Demande reçue de " + demande.getSender().getLocalName());
                System.out.println(" ");
                String contenu = demande.getContent();
                String[] items = contenu.split(",");

                StringBuilder reponseContent = new StringBuilder();
                boolean disponible = true;
                double totalPrix = 0.0;

                for (String item : items) {
                    String[] details = item.split(":");
                    String nomMedicament = details[0].trim();

                    if (details.length < 2) {
                        System.out.println("❌ Format incorrect pour l'item : " + item);
                        System.out.println(" ");
                        continue; // Passe à l'élément suivant sans planter l'agent
                    }

                    int quantiteDemandee;
                    try {
                        quantiteDemandee = Integer.parseInt(details[1].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Quantité invalide pour : " + item);
                        System.out.println(" ");
                        continue;
                    }

                    Double porteMonnaie = Double.parseDouble(details[2].trim()); 
                    if (stockProduits.containsKey(nomMedicament) && stockProduits.get(nomMedicament) >= quantiteDemandee) {
                        double prixTotal = prixProduits.get(nomMedicament) * quantiteDemandee;
                        reponseContent.append(nomMedicament)
                                .append(":").append(quantiteDemandee)
                                .append(":").append(prixTotal).append(",");
                        totalPrix += prixTotal;
                        
                    } else {
                        disponible = false;
                        break;
                    }
                    if (disponible){
                        if (porteMonnaie >= totalPrix){
                            mettreAJourStock(nomMedicament, quantiteDemandee);
                        }
                    }
                }

                ACLMessage reponse = demande.createReply();

                if (disponible) {
                    reponse.setPerformative(ACLMessage.INFORM);
                    reponse.setContent(reponseContent.toString() + "TOTAL:" + totalPrix);
                    System.out.println(reponseContent.toString() + "TOTAL:" + totalPrix);
                    System.out.println(" ");
                    try {
                        Thread.sleep(5000); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (totalPrix == 0.0){
                        reponse.setPerformative(ACLMessage.REFUSE);
                        reponse.setContent("Stock insuffisant ou médicament non disponible");
                        System.out.println("Stock insuffisant ou médicament non disponible");
                        System.out.println(" ");
                    } else {
                        reponse.setPerformative(ACLMessage.INFORM);
                        reponse.setContent(reponseContent.toString() + "TOTAL:" + totalPrix);
                        System.out.println(reponseContent.toString() + "TOTAL:" + totalPrix);
                        System.out.println(" ");
                    }
                    
                }
                send(reponse);

                try {
                    Thread.sleep(5000); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                block();
            }
        }
    }

    public void mettreAJourStock(String medicament, int quantiteAchetee) {
        if (stockProduits.containsKey(medicament)) {
            stockProduits.put(medicament, stockProduits.get(medicament) - quantiteAchetee);
        }
        
    }

    // Afficher le stock de chaque médicament
    public void afficherStock() {
        System.out.println("📊 Stock actuel : ");
        for (Map.Entry<String, Integer> entry : stockProduits.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue() + " en stock");
        }
        System.out.println(" ");
    }
}
