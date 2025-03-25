import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Container {
    public static void main(String[] args) {
        try {
            // Démarrage de JADE
            Runtime rt = Runtime.instance();
            ProfileImpl pc = new ProfileImpl(false);
            pc.setParameter(ProfileImpl.MAIN_HOST, "localhost");
            AgentContainer ac = rt.createAgentContainer(pc);

            // Création d'un agent Acheteur avec des paramètres : nom, porte-monnaie, liste des achats
            Object[] paramsAcheteur = new Object[]{"Acheteur1", 1000.0, new String[]{"Paracétamol:5:1000.0", "Ibuprofène:5:1000.0"}};
            AgentController acheteur = ac.createNewAgent("Acheteur1", "ag.AcheteurFSM", paramsAcheteur);
            acheteur.start();

            // Création d'un agent Acheteur avec des paramètres : nom, porte-monnaie, liste des achats
            Object[] paramsAcheteur2 = new Object[]{"Acheteur2", 500.0, new String[]{"Paracétamol:2:500.0", "Ibuprofène:3:500.0"}};
            AgentController acheteur2 = ac.createNewAgent("Acheteur2", "ag.AcheteurFSM", paramsAcheteur2);
            acheteur2.start();

            // Création de l'agent Pharmacie avec une liste de produits, stocks et prix
            Object[] paramsPharmacie = new Object[]{
                new String[]{"Paracétamol", "Ibuprofène", "Aspirine"},  // Produits
                new Integer[]{10, 5, 8},  // Stocks
                new Double[]{50.0, 80.0, 30.0} // Prix
            };
            AgentController pharmacie = ac.createNewAgent("Pharmacie", "ag.Pharmacie", paramsPharmacie);
            pharmacie.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
