package web.actions;

/**
 * Factory des Actions — MVC2 (Pattern Factory + Command).
 *
 * Le FrontController appelle ActionFactory.getAction("lister") pour
 * obtenir l'instance de l'Action à exécuter, sans connaître les
 * classes concrètes (faible couplage).
 */

public class ActionImpl {

	public static Action getAction(String actionName) {
        switch (actionName.toLowerCase()) {
            case "pv": return new PvAction();
            default:
                throw new IllegalArgumentException("Action inconnue : '" + actionName + "'");
        }
    }

}
