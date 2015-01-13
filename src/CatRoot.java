import java.util.List;

/*
 * CatRoot
 *
 * Our structure
 */
public class CatRoot {
	private String cible;
	private List<CatRoot> dependances;
	private String commande;

	public CatRoot(String commande, String cible, List<CatRoot> dependances) {
		this.cible = cible;
		this.dependances = dependances;
		this.commande = commande;
	}

	public CatRoot(String cible, List<CatRoot> dependances) {
		this.cible = cible;
		this.dependances = dependances;
	}

	public CatRoot(String cible) {
		this.cible = cible;
	}

	@Override
	public String toString() {
		String sortie = "";
		sortie = sortie + cible + ": ";
		if (dependances != null) {
			for (CatRoot c : dependances) {

				sortie = sortie + c.getCible() + " ";
			}
		}

		sortie = sortie + "\n" + commande + "\n" + "\n";
		return sortie;
	}

	public String getCible() {
		return cible;
	}
	
	public CatRoot getDep(String name) {
		//TODO return the dep with the name "name"
		return null;
	}

}
