import model.SkillType;
import model.Status;
import model.Wizard;
import model.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by dragoon on 11/29/16.
 */
public class WizardsInfo {

	private WizardInfo[] wizardInfos;

	private WizardInfo me;

	public WizardsInfo() {
		wizardInfos = new WizardInfo[11];
		for (int i = 1; i != 11; ++i) {
			wizardInfos[i] = new WizardInfo();
		}
	}

	private WizardsInfo(WizardInfo[] wizardInfos, WizardInfo me) {
		this.wizardInfos = new WizardInfo[11];
		for (int i = 1; i != 11; ++i) {
			this.wizardInfos[i] = wizardInfos[i].makeClone();
			if (wizardInfos[i] == me) {
				this.me = this.wizardInfos[i];
			}
		}
	}

	public void updateData(World world, EnemyPositionCalc enemyPositionCalc) {
		Variables.wizardsInfo = this;
		if (me == null) {
			for (Wizard wizard : world.getWizards()) {
				if (wizard.isMe()) {
					me = wizardInfos[(int) wizard.getId()];
					break;
				}
			}
		}
		List<Wizard> allyWizards = new ArrayList<>();
		List<Wizard> enemyWizards = new ArrayList<>();
		for (Wizard wizard : world.getWizards()) {
			if (wizard.getFaction() == Constants.getCurrentFaction()) {
				allyWizards.add(wizard);
			} else {
				enemyWizards.add(wizard);
			}
			wizardInfos[(int) wizard.getId()].updateSkills(wizard.getSkills());
		}

		updateWizardsStatuses(allyWizards);
		updateWizardsStatuses(enemyWizards);
	}

	private void updateWizardsStatuses(List<Wizard> allyWizards) {
		for (int i = 0; i != allyWizards.size(); ++i) {
			Wizard first = allyWizards.get(i);
			WizardInfo currWizardInfo = wizardInfos[(int) first.getId()];
			for (int j = i + 1; j < allyWizards.size(); ++j) {
				Wizard second = allyWizards.get(j);
				if (Constants.getGame().getAuraSkillRange() > FastMath.hypot(first, second)) {
					currWizardInfo.updateAuras(wizardInfos[(int) second.getId()]);
				}
			}
			currWizardInfo.finalCalculation(first);
		}
	}

	public WizardInfo getMe() {
		return me;
	}

	public WizardInfo getWizardInfo(long wizardId) {
		return wizardInfos[(int) wizardId];
	}

	public WizardsInfo makeClone() {
		return new WizardsInfo(wizardInfos, me);
	}

	public static class WizardInfo {
		private int hastened;
		private int shielded;
		private int frozen;
		private int empowered;
		private double moveFactor;
		private double turnFactor;
		private double castRange;

		private int staffDamage;
		private int staffDamageBonus;
		private int magicalMissileDamage;
		private int magicDamageBonus;
		private int frostBoltDamage;
		private int fireballMaxDamage;
		private int fireballMinDamage;

		private boolean hasFrostBolt;
		private boolean hasFireball;
		private boolean hasHasteSkill;
		private boolean hasShieldSkill;
		private boolean hasFastMissileCooldown;

		private HashSet<SkillType> knownSkills;

		private int[] skillsCount;
		private int[] aurasCount;
		private int[] otherAurasCount;

		public WizardInfo() {
			knownSkills = new HashSet<>();

			skillsCount = new int[5];
			aurasCount = new int[5];
			otherAurasCount = new int[5];
			castRange = 500.;
		}

		public WizardInfo(int hastened,
						  int shielded,
						  int frozen,
						  int empowered,
						  double moveFactor,
						  double turnFactor,
						  int staffDamage,
						  int staffDamageBonus,
						  int magicalMissileDamage,
						  int magicDamageBonus,
						  int frostBoltDamage,
						  int fireballMaxDamage,
						  int fireballMinDamage,
						  boolean hasFrostBolt,
						  boolean hasFireball,
						  boolean hasHasteSkill,
						  boolean hasShieldSkill,
						  boolean hasFastMissileCooldown,
						  HashSet<SkillType> knownSkills,
						  int[] skillsCount,
						  int[] aurasCount,
						  int[] otherAurasCount,
						  double castRange) {
			this.hastened = hastened;
			this.shielded = shielded;
			this.frozen = frozen;
			this.empowered = empowered;
			this.moveFactor = moveFactor;
			this.turnFactor = turnFactor;
			this.staffDamage = staffDamage;
			this.staffDamageBonus = staffDamageBonus;
			this.magicalMissileDamage = magicalMissileDamage;
			this.magicDamageBonus = magicDamageBonus;
			this.frostBoltDamage = frostBoltDamage;
			this.fireballMaxDamage = fireballMaxDamage;
			this.fireballMinDamage = fireballMinDamage;
			this.hasFrostBolt = hasFrostBolt;
			this.hasFireball = hasFireball;
			this.hasHasteSkill = hasHasteSkill;
			this.hasShieldSkill = hasShieldSkill;
			this.hasFastMissileCooldown = hasFastMissileCooldown;
			this.knownSkills = new HashSet<>(knownSkills);
			this.skillsCount = Arrays.copyOf(skillsCount, skillsCount.length);
			this.aurasCount = Arrays.copyOf(aurasCount, aurasCount.length);
			this.otherAurasCount = Arrays.copyOf(otherAurasCount, otherAurasCount.length);
			this.castRange = castRange;
		}

		public void finalCalculation(Wizard wizard) {
			empowered = 0;
			frozen = 0;
			hastened = 0;
			shielded = 0;
			for (Status status : wizard.getStatuses()) {
				switch (status.getType()) {
					case BURNING:
						break;
					case EMPOWERED:
						empowered = Math.max(empowered, status.getRemainingDurationTicks());
						break;
					case FROZEN:
						frozen = Math.max(frozen, status.getRemainingDurationTicks());
						break;
					case HASTENED:
						hastened = Math.max(hastened, status.getRemainingDurationTicks());
						break;
					case SHIELDED:
						shielded = Math.max(shielded, status.getRemainingDurationTicks());
						break;
				}
			}
			turnFactor = 1. + (hastened > 0 ? Constants.getGame().getHastenedRotationBonusFactor() : 0.);
			staffDamageBonus = (skillsCount[SkillFork.STAFF_DAMAGE.ordinal()] +
					Math.max(aurasCount[SkillFork.STAFF_DAMAGE.ordinal()], otherAurasCount[SkillFork.STAFF_DAMAGE.ordinal()])) *
					Constants.getGame().getStaffDamageBonusPerSkillLevel();
			staffDamage = Constants.getGame().getStaffDamage() + staffDamageBonus;

			magicDamageBonus = (skillsCount[SkillFork.MAGICAL_DAMAGE.ordinal()] +
					Math.max(aurasCount[SkillFork.MAGICAL_DAMAGE.ordinal()], otherAurasCount[SkillFork.MAGICAL_DAMAGE.ordinal()])) *
					Constants.getGame().getMagicalDamageBonusPerSkillLevel();
			magicalMissileDamage = Constants.getGame().getMagicMissileDirectDamage() + magicDamageBonus;
			if (hasFireball) {
				fireballMaxDamage = Constants.getGame().getFireballExplosionMaxDamage() + magicDamageBonus;
				fireballMinDamage = Constants.getGame().getFireballExplosionMinDamage() + magicDamageBonus;
			}

			if (hasFrostBolt) {
				frostBoltDamage = Constants.getGame().getFrostBoltDirectDamage() + magicDamageBonus;
			}

			if (empowered > 0) {
				staffDamage *= Constants.getGame().getEmpoweredDamageFactor();
				magicalMissileDamage *= Constants.getGame().getEmpoweredDamageFactor();
				frostBoltDamage *= Constants.getGame().getEmpoweredDamageFactor();
				fireballMaxDamage *= Constants.getGame().getEmpoweredDamageFactor();
				fireballMinDamage *= Constants.getGame().getEmpoweredDamageFactor();
			}

			moveFactor = 1. + (hastened > 0 ? Constants.getGame().getHastenedMovementBonusFactor() : 0.) +
					(skillsCount[SkillFork.MOVEMENT.ordinal()] +
							Math.max(aurasCount[SkillFork.MOVEMENT.ordinal()], otherAurasCount[SkillFork.MOVEMENT.ordinal()])) *
							Constants.getGame().getMovementBonusFactorPerSkillLevel();

			castRange = wizard.getCastRange();
		}

		public void updateAuras(WizardInfo other) {
			for (int i = 0; i != aurasCount.length; ++i) {
				otherAurasCount[i] = Math.max(otherAurasCount[i], other.aurasCount[i]);
				other.otherAurasCount[i] = Math.max(other.otherAurasCount[i], aurasCount[i]);
			}
		}

		public void updateSkills(SkillType[] skills) {
			Arrays.fill(otherAurasCount, 0);
			if (knownSkills.size() == skills.length) {
				return;
			}
			for (SkillType skill : skills) {
				if (!knownSkills.contains(skill)) {
					knownSkills.add(skill);
					switch (skill) {
						case RANGE_BONUS_PASSIVE_1:
							skillsCount[SkillFork.RANGE.ordinal()] = 1;
							break;
						case RANGE_BONUS_AURA_1:
							aurasCount[SkillFork.RANGE.ordinal()] = 1;
							break;
						case RANGE_BONUS_PASSIVE_2:
							skillsCount[SkillFork.RANGE.ordinal()] = 2;
							break;
						case RANGE_BONUS_AURA_2:
							aurasCount[SkillFork.RANGE.ordinal()] = 2;
							break;
						case ADVANCED_MAGIC_MISSILE:
							hasFastMissileCooldown = true;
							break;

						case MAGICAL_DAMAGE_BONUS_PASSIVE_1:
							skillsCount[SkillFork.MAGICAL_DAMAGE.ordinal()] = 1;
							break;
						case MAGICAL_DAMAGE_BONUS_AURA_1:
							aurasCount[SkillFork.MAGICAL_DAMAGE.ordinal()] = 1;
							break;
						case MAGICAL_DAMAGE_BONUS_PASSIVE_2:
							skillsCount[SkillFork.MAGICAL_DAMAGE.ordinal()] = 2;
							break;
						case MAGICAL_DAMAGE_BONUS_AURA_2:
							aurasCount[SkillFork.MAGICAL_DAMAGE.ordinal()] = 2;
							break;
						case FROST_BOLT:
							hasFrostBolt = true;
							break;

						case STAFF_DAMAGE_BONUS_PASSIVE_1:
							skillsCount[SkillFork.STAFF_DAMAGE.ordinal()] = 1;
							break;
						case STAFF_DAMAGE_BONUS_AURA_1:
							aurasCount[SkillFork.STAFF_DAMAGE.ordinal()] = 1;
							break;
						case STAFF_DAMAGE_BONUS_PASSIVE_2:
							skillsCount[SkillFork.STAFF_DAMAGE.ordinal()] = 2;
							break;
						case STAFF_DAMAGE_BONUS_AURA_2:
							aurasCount[SkillFork.STAFF_DAMAGE.ordinal()] = 2;
							break;
						case FIREBALL:
							hasFireball = true;
							break;

						case MOVEMENT_BONUS_FACTOR_PASSIVE_1:
							skillsCount[SkillFork.MOVEMENT.ordinal()] = 1;
							break;
						case MOVEMENT_BONUS_FACTOR_AURA_1:
							aurasCount[SkillFork.MOVEMENT.ordinal()] = 1;
							break;
						case MOVEMENT_BONUS_FACTOR_PASSIVE_2:
							skillsCount[SkillFork.MOVEMENT.ordinal()] = 2;
							break;
						case MOVEMENT_BONUS_FACTOR_AURA_2:
							aurasCount[SkillFork.MOVEMENT.ordinal()] = 2;
							break;
						case HASTE:
							hasHasteSkill = true;
							break;

						case MAGICAL_DAMAGE_ABSORPTION_PASSIVE_1:
							skillsCount[SkillFork.MAGICAL_DAMAGE_ABSORPTION.ordinal()] = 1;
							break;
						case MAGICAL_DAMAGE_ABSORPTION_AURA_1:
							aurasCount[SkillFork.MAGICAL_DAMAGE_ABSORPTION.ordinal()] = 1;
							break;
						case MAGICAL_DAMAGE_ABSORPTION_PASSIVE_2:
							skillsCount[SkillFork.MAGICAL_DAMAGE_ABSORPTION.ordinal()] = 2;
							break;
						case MAGICAL_DAMAGE_ABSORPTION_AURA_2:
							aurasCount[SkillFork.MAGICAL_DAMAGE_ABSORPTION.ordinal()] = 2;
							break;
						case SHIELD:
							hasShieldSkill = true;
							break;
					}
				}
			}
		}

		public boolean isHasFrostBolt() {
			return hasFrostBolt;
		}

		public boolean isHasFireball() {
			return hasFireball;
		}

		public boolean isHasHasteSkill() {
			return hasHasteSkill;
		}

		public boolean isHasShieldSkill() {
			return hasShieldSkill;
		}

		public boolean isHasFastMissileCooldown() {
			return hasFastMissileCooldown;
		}

		public int getHastened() {
			return hastened;
		}

		public int getShielded() {
			return shielded;
		}

		public int getFrozen() {
			return frozen;
		}

		public int getEmpowered() {
			return empowered;
		}

		public double getMoveFactor() {
			return moveFactor;
		}

		public double getTurnFactor() {
			return turnFactor;
		}

		public int getStaffDamage() {
			return staffDamage;
		}

		public int getMagicalMissileDamage() {
			return magicalMissileDamage;
		}

		public int getFrostBoltDamage() {
			return frostBoltDamage;
		}

		public int getFireballMaxDamage() {
			return fireballMaxDamage;
		}

		public int getFireballMinDamage() {
			return fireballMinDamage;
		}

		public double getCastRange() {
			return castRange;
		}

		public int getStaffDamage(int addTicks) {
			if (empowered != 0 && addTicks >= empowered) {
				return staffDamageBonus + Constants.getGame().getStaffDamage();
			}
			return staffDamage;
		}

		public int getMagicalMissileDamage(int addTicks) {
			if (empowered != 0 && addTicks >= empowered) {
				return magicDamageBonus + Constants.getGame().getMagicMissileDirectDamage();
			}
			return magicalMissileDamage;
		}

		public int getFrostBoltDamage(int addTicks) {
			if (empowered != 0 && addTicks >= empowered) {
				return magicDamageBonus + Constants.getGame().getFrostBoltDirectDamage();
			}
			return frostBoltDamage;
		}

		public int getFireballMaxDamage(int addTicks) {
			if (empowered != 0 && addTicks >= empowered) {
				return magicDamageBonus + Constants.getGame().getFireballExplosionMaxDamage();
			}
			return fireballMaxDamage;
		}

		public int getFireballMinDamage(int addTicks) {
			if (empowered != 0 && addTicks >= empowered) {
				return magicDamageBonus + Constants.getGame().getFireballExplosionMinDamage();
			}
			return fireballMinDamage;
		}

		public WizardInfo makeClone() {
			return new WizardInfo(
					hastened,
					shielded,
					frozen,
					empowered,
					moveFactor,
					turnFactor,
					staffDamage,
					staffDamageBonus,
					magicalMissileDamage,
					magicDamageBonus,
					frostBoltDamage,
					fireballMaxDamage,
					fireballMinDamage,
					hasFrostBolt,
					hasFireball,
					hasHasteSkill,
					hasShieldSkill,
					hasFastMissileCooldown,
					knownSkills,
					skillsCount,
					aurasCount,
					otherAurasCount,
					castRange);
		}
	}
}