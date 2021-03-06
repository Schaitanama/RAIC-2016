import model.Projectile;
import model.Wizard;
import model.World;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dragoon on 14.11.16.
 */
public class Variables {

	public static Wizard self;
	public static World world;

	public static List<AbstractMap.SimpleEntry<Projectile, Double>> projectilesSim = new LinkedList<>();
	public static HashMap<Long, Double> fireballHitDamageCheck = new HashMap<>();

	public static List<Long> projectiles = new ArrayList<>();

	public static WizardsInfo wizardsInfo;
	public static EnemyPositionCalc enemyPositionCalc;

	public static int aggressiveDamageMultiplyer;

	public static double maxTurnAngle;

	public static Point attackPoint;

	public static Long attackWizardId;

	public static double maxDangerMatrixScore;

	public static CurrentAction.ActionType prevActionType = CurrentAction.ActionType.FIGHT;
}
