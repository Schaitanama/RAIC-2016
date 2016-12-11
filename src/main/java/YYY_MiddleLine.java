import model.Unit;
import model.World;

/**
 * Created by by.dragoon on 11/8/16.
 */
public class YYY_MiddleLine extends YYY_BaseLine {

	private double centerLine;

	private double moveDirection = -Math.PI / 4.; //always up right

	private double sqrtOfTwo = 1. / Math.sqrt(2.); //ax + by + c = 0, a=1, b=1, c = -centerLine

	private YYY_Point[] segmentPoints = new YYY_Point[]{new YYY_Point(0, 4000), new YYY_Point(4000, 0)};

	public YYY_MiddleLine() {
		centerLine = YYY_Constants.getGame().getMapSize();
		fightPoint.update(2000, 2000);
	}

	public double getCenterLine() {
		return centerLine;
	}

	@Override
	public YYY_Point getNearestPoint(double x, double y) {
		return YYY_Utils.nearestSegmentPoint(new YYY_Point(x, y), segmentPoints[0], segmentPoints[1]);
	}

	@Override
	public double getDistanceTo(Unit unit) {
		return getDistanceTo(unit.getX(), unit.getY());
	}

	@Override
	public double getDistanceTo(double x, double y) {
		return Math.abs(x + y - centerLine) * sqrtOfTwo;
	}

	public double getMoveDirection(Unit unit) {
		return moveDirection;
	}

	@Override
	public double getMoveDirection(YYY_Point point) {
		return moveDirection;
	}

	@Override
	public void updateFightPoint(World world, YYY_EnemyPositionCalc enemyPositionCalc) {
		double minDistance = 1e6;
		YYY_Point nearestPoint = null;
		for (YYY_MinionPhantom minionPhantom : enemyPositionCalc.getDetectedMinions().values()) {
			if (minionPhantom.getLine() != 1) {
				continue;
			}
			double tmp = YYY_FastMath.hypot(minionPhantom.getPosition().getX(), 4000 - minionPhantom.getPosition().getY());
			if (tmp < minDistance) {
				minDistance = tmp;
				nearestPoint = minionPhantom.getPosition();
			}
		}

		for (YYY_WizardPhantom wizardPhantom : enemyPositionCalc.getDetectedWizards().values()) {
			if (wizardPhantom.getLastSeenTick() == 0 || YYY_Variables.wizardsInfo.getWizardInfo(wizardPhantom.getId()).getLineNo() != 1) {
				continue;
			}
			double tmp = YYY_FastMath.hypot(wizardPhantom.getPosition().getX(), 4000 - wizardPhantom.getPosition().getY());
			if (tmp < minDistance) {
				minDistance = tmp;
				nearestPoint = wizardPhantom.getPosition();
			}
		}

		if (nearestPoint != null) {
			fightPoint.update(getNearestPoint(nearestPoint.getX(), nearestPoint.getY()));
		}
	}
}
