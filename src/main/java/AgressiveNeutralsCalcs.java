import model.Faction;
import model.Minion;
import model.World;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by dragoon on 24.11.16.
 */
public class AgressiveNeutralsCalcs {

	private HashMap<Long, Minion> phantoms;

	private HashSet<Long> agressive;

	public AgressiveNeutralsCalcs() {
		phantoms = new HashMap<>();
		agressive = new HashSet<>();
	}

	public AgressiveNeutralsCalcs(HashMap<Long, Minion> phantoms, HashSet<Long> agressive) {
		this.phantoms = new HashMap<>(phantoms);
		this.agressive = new HashSet<>(agressive);
	}

	public void updateMap(World world) {
		if (world.getTickIndex() < 700 && !SchemeSelector.mortido && !SchemeSelector.antmsu) {
			for (Minion minion : world.getMinions()) {
				if (minion.getFaction() == Faction.NEUTRAL) {
					agressive.add(minion.getId());
				}
			}
		}
		if (world.getTickIndex() == 700) {
			agressive.clear();
		}
		for (Minion minion : world.getMinions()) {
			if (minion.getFaction() == Faction.NEUTRAL) {
				Minion phantom = phantoms.get(minion.getId());
				if (phantom == null) {
					phantoms.put(minion.getId(), minion);
				} else {
					if (!agressive.contains(minion.getId()) && Utils.isNeutralActive(phantom, minion)) {
						agressive.add(minion.getId());
					}
				}
			}
		}
	}

	public boolean isMinionAgressive(long id) {
		return agressive.contains(id);
	}

	public AgressiveNeutralsCalcs makeClone() {
		return new AgressiveNeutralsCalcs(phantoms, agressive);
	}
}
