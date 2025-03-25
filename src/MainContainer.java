import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

public class MainContainer {
    public static void main(String[] args) {
        try {
            // 🔹 1. Obtenir une instance de l'environnement JADE
            Runtime runtime = Runtime.instance();

            // 🔹 2. Créer un profil pour le conteneur principal
            ProfileImpl profile = new ProfileImpl();
            profile.setParameter(Profile.GUI, "true"); // Active l'interface graphique

            // 🔹 3. Créer et démarrer le conteneur principal
            AgentContainer mainContainer = runtime.createMainContainer(profile);
            mainContainer.start();
            
            System.out.println("Main Container lancé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
