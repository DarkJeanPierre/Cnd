/**
 * Joueur.
 * 
 * N. B. : il s'agit du code développé par les étudiants.
 */
package cortexcape.modele;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Joueur.
 * 
 * N. B. : il s'agit du code développé par les étudiants.
 * 
 * @author Subileau--Langlois Titouan 
 */
public class JoueurETD extends Joueur {

	/**
	 * Création (sans paramètre) d'un joueur.
	 */
	public JoueurETD() {
		super();
	}

	/**
	 * Création d'un joueur.
	 * 
	 * @param labyrinthe Labyrinthe.
	 */
	public JoueurETD(final Labyrinthe labyrinthe) {
		super(labyrinthe);
	}

	/* (non-Javadoc)
	 * @see cortexcape.modele.Joueur#existerChemin(cortexcape.modele.Couloir, cortexcape.modele.Sortie)
	 */
	@Override
	public boolean existerChemin(final Couloir couloirDepart, final Sortie sortieArrivee) {
		ArrayList<Emplacement> traite = new ArrayList<>();
		traiterVoisins(couloirDepart.getEmplacement1reExtremite(), traite);
		return traite.contains(sortieArrivee);
	}

	/**traite les voisins et les ajoute à la liste des Emplacements reliés
	 * @param emplacementDepart
	 * @param traite
	 */
	private void traiterVoisins(final Emplacement emplacementDepart, ArrayList<Emplacement> traite) {
		traite.add(emplacementDepart); // On ajoute
		emplacementDepart.getCouloirsAdjacents().forEach(couloir -> {

			Emplacement emplacementSuivant = emplacementSuivant(couloir, emplacementDepart);

			if (estDefense(emplacementSuivant)) { // on ne regarde pas ses voisins si c'est une defense
				traite.add(emplacementSuivant);
			}

			if (!traite.contains(emplacementSuivant)) { // Si l'emplacement suivant n'a pas déjÃ  été traité on
														// traite ses voisins
				traiterVoisins(emplacementSuivant, traite);
			}
		});
	}

	/* (non-Javadoc)
	 * @see cortexcape.modele.Joueur#determinerChemin(cortexcape.modele.Couloir, cortexcape.modele.Sortie)
	 */
	@Override
	public List<Emplacement> determinerChemin(final Couloir couloirDepart, final Sortie sortieArrivee) {
		HashMap<Emplacement, Emplacement> predecesseurs = initPrede();
		HashMap<Emplacement, Integer> etats = initEtats();
		HashMap<Emplacement, Integer> couts = initCouts();
		
		algoDjikstra(sortieArrivee,predecesseurs, etats, couts);// on calcul les couts et prédécessueurs

		HashMap<Integer, Emplacement> chemin = calculCheminDepuisPreds(
				determinerEmplacementDeDepart(couloirDepart, couts), sortieArrivee, predecesseurs);//on calcul le chemin à partir des résultats précédents
		
		return new ArrayList<Emplacement>(chemin.values());
	}



	/**calcul le chemin grace au predecesseur 
	 * @param emplacementDepart
	 * @param sortieArrivee
	 * @param predecesseurs
	 * @return une HashMap décrivant le chemin 
	 */
	private HashMap<Integer, Emplacement> calculCheminDepuisPreds(final Emplacement emplacementDepart,
			final Sortie sortieArrivee, HashMap<Emplacement, Emplacement> predecesseurs) {
		HashMap<Integer, Emplacement> chemin = new HashMap<>();
		int i = 0;
		boolean fini = false;
		Emplacement predecesseur = emplacementDepart;
		while (i < predecesseurs.size() && !fini) {	//tant que tout les predeccesseurs n'ont pas été vu ou que le chemin n'a pas été trouvé
			chemin.put(chemin.size(), predecesseur);	//on ajoute le predecesseur au chemin

			if (predecesseur == sortieArrivee)	
				fini = true;
			predecesseur = predecesseurs.get(predecesseur);//on prend le predecesseur du predecesseur
			i++;
		}
		return chemin;

	}

	
	/**calcul le cout à partir de l'emplacement entré
	 * @param entre
	 * @param predecesseurs
	 * @param etats
	 * @param couts
	 */
	private void algoDjikstra(Emplacement entre ,HashMap<Emplacement, Emplacement> predecesseurs, HashMap<Emplacement, Integer> etats,
			HashMap<Emplacement, Integer> couts) {
		etats.replace(entre, 0);
		couts.replace(entre, 0);
		boolean fini = false; // 1=traité : 0 = atteint : -1= non atteint
		int i = 0;
		Emplacement emplacement;
		int nbEmplacement = labyrinthe.getEmplacements().size();
		while (i < nbEmplacement && !fini) {
			emplacement = getAtteint(etats); // on prend un emplacement atteint
			if (emplacement == null) {// si tout les emplacements sont traités, on arrête
				fini = true;
			} else {
				etats.replace(emplacement, 1);// on passe le emplacement courant en traité
				for (Emplacement empl : emplacement.getEmplacementsAdjacents()) {// Pour chaqu'un de ses voisins
					if (etats.get(empl) != 1 && !estDefense(empl))
						if (couts.get(empl) > couts.get(emplacement) + empl.getNbPoints()) { // si le voisin n'est pas
																								// traité et que son
																								// couts est inférieur à
																								// celui du sommet
																								// courant
							couts.replace(empl, couts.get(emplacement) + empl.getNbPoints());	//alors on change son cout
							predecesseurs.replace(empl, emplacement);							//et son predecesseur 
							etats.replace(empl, 0);												// et on le note comme traité
						}	
				}

			}
			i++;
		}
	}

	/**permet de récupérer un  emplacement atteint
	 * @param etats
	 * @return un emplacement atteint
	 */
	private Emplacement getAtteint(HashMap<Emplacement, Integer> etats) {
		for (Emplacement emp : etats.keySet()) {
			if (etats.get(emp) == 0)
				return emp;
		}
		return null;
	}

	/**
	 * @return une hash map de couts initialisés à 0 pour toutes les portes
	 */
	private HashMap<Emplacement, Integer> initCouts() {
		HashMap<Emplacement, Integer> couts = new HashMap<>();
		labyrinthe.getEmplacements().forEach((k, v) -> {
			couts.put(v, 100000);
		});
		return couts;
	}

	/**
	 * @return une hash map d'états initialisés à -1 pour toutes les portes
	 */
	private HashMap<Emplacement, Integer> initEtats() {
		HashMap<Emplacement, Integer> etats = new HashMap<>();
		labyrinthe.getEmplacements().forEach((k, v) -> {
			if (estDefense(v))
				etats.put(v, 1);
			else
				etats.put(v, -1);
		});
		return etats;
	}

	/**
	 * @return une hash map de predecesseurs liant chaque sommet à lui même
	 */
	private HashMap<Emplacement, Emplacement> initPrede() {
		HashMap<Emplacement, Emplacement> predecessseurs = new HashMap<>();
		labyrinthe.getEmplacements().forEach((k, v) -> {
			predecessseurs.put(v, v);
		});
		return predecessseurs;
	}

	/**determine l'emplacement de moindre cout à au bout d'un couloir
	 * @param couloir
	 * @param distances
	 * @return l'emplacement le moins distant de la sortie
	 */
	private Emplacement determinerEmplacementDeDepart(Couloir couloir, HashMap<Emplacement, Integer> couts) {
		Emplacement emplacementDepart;
		Emplacement emplacement1 = couloir.getEmplacement1reExtremite();
		Emplacement emplacement2 = couloir.getEmplacement2deExtremite();
		if (estDefense(emplacement1))			//si emplacement1 est une défense on retourne le deuxième
			emplacementDepart = emplacement2;
		else if (estDefense(emplacement2))		// et inversement
			emplacementDepart = emplacement1;
		else									// et finallement on calcul celui de moindre cout
			emplacementDepart = couts.get(couloir.getEmplacement1reExtremite()) > couts
					.get(couloir.getEmplacement2deExtremite()) ? couloir.getEmplacement2deExtremite()
							: couloir.getEmplacement1reExtremite();
		return emplacementDepart;
	}

	/** calcul l'emplacement suivant à partir d'un couloir et d'une des ses extrémitées 
	 * @param couloir
	 * @param emplacementDepart
	 * @return l'emplacement suivant
	 */
	private Emplacement emplacementSuivant(Couloir couloir, Emplacement emplacementDepart) {
		return couloir.getEmplacement1reExtremite() == emplacementDepart ? couloir.getEmplacement2deExtremite()
				: couloir.getEmplacement1reExtremite();
	}

	/**
	 * @param emplacement
	 * @return si l'emplacement est une défense
	 */
	private boolean estDefense(Emplacement emplacement) {
		if (!emplacement.estSortie()) {
			return (((Porte) (emplacement)).estDefense());
		}
		return false;
	}

}
