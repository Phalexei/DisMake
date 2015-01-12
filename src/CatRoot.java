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

    public CatRoot(String cible,List<CatRoot> dependances) {
        this.cible = cible;
        this.dependances = dependances;
    }
    
    public CatRoot(String cible) {
        this.cible = cible;
    }


    public void MytoString() {

      System.out.print(cible + ": ");
      if (dependances != null) {
    	   for (CatRoot c : dependances) {

    	        System.out.print(c.getCible()+ " ");
    	      }
      }
   

      System.out.println("\n" + commande);
    }

    public String getCible() {
      return cible;
    }
    
}
