import model.Minion;
import model.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by dragoon on 28.11.16.
 */
public class TeammateIdsContainer {

	private HashMap<Long, Integer> teammatesIds;

	public TeammateIdsContainer() {
		teammatesIds = new HashMap<>();
	}

	public TeammateIdsContainer(HashMap<Long, Integer> teammatesIds) {
		this.teammatesIds = new HashMap<>(teammatesIds);
	}

	public void updateTeammatesIds(World world) {
		for (Minion minion : world.getMinions()) {
			teammatesIds.put(minion.getId(), world.getTickIndex());
		}
		if (world.getTickIndex() % 100 == 0) {
			Iterator<Map.Entry<Long, Integer>> iterator = teammatesIds.entrySet().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getValue() + 50 < world.getTickIndex()) {
					iterator.remove();
				}
			}
		}
	}

	public boolean isTeammate(Long id) {
		return teammatesIds.containsKey(id);
	}

	public TeammateIdsContainer makeClone() {
		return new TeammateIdsContainer(teammatesIds);
	}
}
