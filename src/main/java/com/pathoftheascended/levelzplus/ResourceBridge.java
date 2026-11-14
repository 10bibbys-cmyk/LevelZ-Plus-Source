package com.pathoftheascended.levelzplus;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.stats.PlayerStatsManager;
import net.levelz.stats.Skill;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class ResourceBridge {
   private static final Spec MAX_STAMINA = new Spec(
      "staminaattributes", "max_stamina", "e04a426d-7232-4bf1-9988-07dc8a860fab", "LevelZ+ Endurance -> Max Stamina", Operation.ADDITION
   );
   private static final Spec STAMINA_REGEN = new Spec(
      "staminaattributes", "stamina_regeneration", "f0f92901-55c1-4ecf-b413-d94e204c97d7", "LevelZ+ Endurance -> Stamina Regen", Operation.ADDITION
   );
   private static final Spec MAX_MANA = new Spec(
      "rpgmana", "mana", "653ccb8f-2027-4d29-a6b9-ff685fdc40e4", "LevelZ+ Arcana -> Max Mana", Operation.ADDITION
   );
   private static final Spec MANA_REGEN = new Spec(
      "rpgmana", "manaregen", "075b578c-b43c-49e7-8146-1fcc8e71607d", "LevelZ+ Arcana -> Mana Regen", Operation.ADDITION
   );
   private static final Spec ATTACK_STAMINA = new Spec(
      "generic", "attack_stamina_cost", "ef19260a-c2ee-4188-a3da-35b73faaf74d", "LevelZ+ Vigour -> Melee Stamina Cost", Operation.MULTIPLY_TOTAL
   );
   private static final Spec ROLL_DISTANCE = new Spec(
      "combat_roll", "distance", "2985579b-2502-4afc-8efc-a462b7a61143", "LevelZ+ Dexterity -> Roll Distance", Operation.ADDITION
   );
   private static final Spec ROLL_RECHARGE = new Spec(
      "combat_roll", "recharge", "706b25a6-b841-4a62-b4c7-2a599a01fb8c", "LevelZ+ Dexterity -> Roll Recharge", Operation.ADDITION
   );
   private static final Spec ROLL_COUNT = new Spec(
      "combat_roll", "count", "77782e9a-f20d-4ad5-807d-e81fb6ad9421", "LevelZ+ Dexterity -> Roll Count", Operation.ADDITION
   );
   private static final Spec CRIT_CHANCE = new Spec(
      "zenith_attributes", "crit_chance", "ac89b032-d031-4f23-a5f2-4bd4c741923f", "LevelZ+ Chance -> Crit Chance", Operation.ADDITION
   );
   private static final Spec DRAW_SPEED = new Spec(
      "zenith_attributes", "draw_speed", "e894bd8b-74f5-4119-996f-427fca6d627c", "LevelZ+ Marksmanship -> Draw Speed", Operation.ADDITION
   );

   private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();
   private static final Set<String> ANNOUNCED = ConcurrentHashMap.newKeySet();

   private ResourceBridge() {
   }

   public static void refresh(PlayerStatsManager manager) {
      if (manager != null) {
         refreshAll(manager.getPlayerEntity(), manager);
      }
   }

   public static void refresh(PlayerStatsManager manager, int ignoredSkill, int ignoredLevel) {
      refresh(manager);
   }

   public static void refresh(PlayerEntity player) {
      PlayerStatsManager manager = managerFromPlayer(player);
      if (manager != null) {
         refreshAll(player, manager);
      }
   }

   public static void refresh(PlayerEntity player, int ignoredSkill, int ignoredLevel) {
      refresh(player);
   }

   public static void refreshEndurance(PlayerStatsManager manager, int level) {
      if (manager != null) {
         refreshEndurance(manager.getPlayerEntity(), level);
      }
   }

   public static void refreshAlchemy(PlayerStatsManager manager, int level) {
      if (manager != null) {
         refreshAlchemy(manager.getPlayerEntity(), level);
      }
   }

   public static void refreshEndurance(PlayerEntity player, int level) {
      apply(MAX_STAMINA, player, Math.max(0, level) * CompatConfig.maxStaminaPerEnduranceLevel());
      apply(STAMINA_REGEN, player, Math.max(0, level) * CompatConfig.staminaRegenPerEnduranceLevel());
   }

   public static void refreshAlchemy(PlayerEntity player, int level) {
      apply(MAX_MANA, player, Math.max(0, level) * CompatConfig.maxManaPerAlchemyLevel());
      apply(MANA_REGEN, player, Math.max(0, level) * CompatConfig.manaRegenPerAlchemyLevel());
   }

   static void refreshCrit(PlayerStatsManager manager) {
      if (manager != null) {
         refreshCritObject(manager.getPlayerEntity(), manager.getSkillLevel(Skill.LUCK));
      }
   }

   static void refreshCrit(PlayerStatsManager manager, int level) {
      if (manager != null) {
         refreshCritObject(manager.getPlayerEntity(), level);
      }
   }

   static void refreshCrit(PlayerEntity player) {
      PlayerStatsManager manager = managerFromPlayer(player);
      if (manager != null) {
         refreshCritObject(player, manager.getSkillLevel(Skill.LUCK));
      }
   }

   static void refreshCrit(PlayerEntity player, int level) {
      refreshCritObject(player, level);
   }

   private static void refreshCritObject(PlayerEntity player, int level) {
      if (player != null) {
         apply(CRIT_CHANCE, player, Math.max(0, level) * CompatConfig.zenithCritChancePerLuckLevel());
      }
   }

   private static void refreshAll(PlayerEntity player, PlayerStatsManager manager) {
      if (player == null || manager == null) {
         return;
      }

      int strength = Math.max(0, manager.getSkillLevel(Skill.STRENGTH));
      int agility = Math.max(0, manager.getSkillLevel(Skill.AGILITY));
      int stamina = Math.max(0, manager.getSkillLevel(Skill.STAMINA));
      int luck = Math.max(0, manager.getSkillLevel(Skill.LUCK));
      int archery = Math.max(0, manager.getSkillLevel(Skill.ARCHERY));
      int alchemy = Math.max(0, manager.getSkillLevel(Skill.ALCHEMY));

      apply(MAX_STAMINA, player, stamina * CompatConfig.maxStaminaPerEnduranceLevel());
      apply(STAMINA_REGEN, player, stamina * CompatConfig.staminaRegenPerEnduranceLevel());
      apply(MAX_MANA, player, alchemy * CompatConfig.maxManaPerAlchemyLevel());
      apply(MANA_REGEN, player, alchemy * CompatConfig.manaRegenPerAlchemyLevel());

      double staminaReduction = Math.min(0.95, strength * CompatConfig.meleeStaminaCostReductionPerLevel());
      apply(ATTACK_STAMINA, player, -staminaReduction);
      apply(ROLL_DISTANCE, player, agility * CompatConfig.rollDistancePerAgilityLevel());
      apply(ROLL_RECHARGE, player, agility * CompatConfig.rollRechargePerAgilityLevel());
      apply(ROLL_COUNT, player, CompatConfig.extraRolls(agility));
      apply(CRIT_CHANCE, player, luck * CompatConfig.zenithCritChancePerLuckLevel());
      apply(DRAW_SPEED, player, archery * CompatConfig.zenithDrawSpeedPerArcheryLevel());

      announceOnce("manager", "[LevelZ+] Typed LevelZ manager bridge active; periodic attribute synchronization is running.");
   }

   private static PlayerStatsManager managerFromPlayer(PlayerEntity player) {
      if (player instanceof PlayerStatsManagerAccess access) {
         return access.getPlayerStatsManager();
      }

      warnOnce("player-manager", "[LevelZ+] Server player does not expose LevelZ PlayerStatsManagerAccess.");
      return null;
   }

   private static EntityAttribute resolve(Spec spec) {
      if (spec.resolved) {
         return spec.attribute;
      }

      synchronized (spec) {
         if (spec.resolved) {
            return spec.attribute;
         }

         for (String[] candidate : candidates(spec)) {
            EntityAttribute attribute = Registries.ATTRIBUTE.get(new Identifier(candidate[0], candidate[1]));
            if (attribute != null) {
               spec.attribute = attribute;
               announceOnce("resolve:" + spec.key(), "[LevelZ+] Resolved attribute " + candidate[0] + ":" + candidate[1]);
               break;
            }
         }

         spec.resolved = true;
         if (spec.attribute == null) {
            warnOnce("resolve:" + spec.key(), "[LevelZ+] Attribute not found: " + spec.key());
         }
         return spec.attribute;
      }
   }

   private static String[][] candidates(Spec spec) {
      if (spec == ROLL_DISTANCE || spec == ROLL_RECHARGE || spec == ROLL_COUNT) {
         return new String[][]{{"combat_roll", spec.path}, {"combatroll", spec.path}};
      }
      if (spec == MAX_STAMINA) {
         return new String[][]{{"staminaattributes", "max_stamina"}, {"staminaattributes", "generic.max_stamina"}};
      }
      if (spec == STAMINA_REGEN) {
         return new String[][]{{"staminaattributes", "stamina_regeneration"}, {"staminaattributes", "generic.stamina_regeneration"}};
      }
      if (spec == ATTACK_STAMINA) {
         return new String[][]{{"generic", "attack_stamina_cost"}, {"bettercombatextension", "generic.attack_stamina_cost"}};
      }
      return new String[][]{{spec.ns, spec.path}};
   }

   private static void apply(Spec spec, PlayerEntity player, double value) {
      if (player == null) {
         return;
      }

      EntityAttribute attribute = resolve(spec);
      if (attribute == null) {
         return;
      }

      try {
         EntityAttributeInstance instance = player.getAttributeInstance(attribute);
         if (instance == null) {
            warnOnce("instance:" + spec.key(), "[LevelZ+] Player has no attribute instance for " + spec.key());
            return;
         }

         // The 100-tick safety refresh must not dirty unchanged attributes.
         // Update only when the desired modifier is absent, removed or different.
         EntityAttributeModifier existing = instance.getModifier(spec.id);
         if (value == 0.0) {
            if (existing != null) {
               instance.removeModifier(spec.id);
            }
            return;
         }

         EntityAttributeModifier.Operation operation = spec.operation == Operation.MULTIPLY_TOTAL
            ? EntityAttributeModifier.Operation.MULTIPLY_TOTAL
            : EntityAttributeModifier.Operation.ADDITION;
         if (existing != null && Double.compare(existing.getValue(), value) == 0
               && existing.getOperation() == operation) {
            return;
         }

         if (existing != null) {
            instance.removeModifier(spec.id);
         }
         instance.addTemporaryModifier(new EntityAttributeModifier(spec.id, spec.name, value, operation));
      } catch (Throwable error) {
         warnOnce("apply:" + spec.key(), "[LevelZ+] Failed to apply " + spec.key() + ": " + rootMessage(error));
      }
   }

   private static String rootMessage(Throwable error) {
      Throwable root = error;
      while (root.getCause() != null && root.getCause() != root) {
         root = root.getCause();
      }
      String message = root.getMessage();
      return root.getClass().getSimpleName() + (message == null ? "" : ": " + message);
   }

   private static void warnOnce(String key, String message) {
      if (WARNED.add(key)) {
         System.err.println(message);
      }
   }

   private static void announceOnce(String key, String message) {
      if (ANNOUNCED.add(key)) {
         System.out.println(message);
      }
   }

   private enum Operation {
      ADDITION,
      MULTIPLY_TOTAL
   }

   private static final class Spec {
      final String ns;
      final String path;
      final String name;
      final UUID id;
      final Operation operation;
      volatile EntityAttribute attribute;
      volatile boolean resolved;

      Spec(String ns, String path, String uuid, String name, Operation operation) {
         this.ns = ns;
         this.path = path;
         this.id = UUID.fromString(uuid);
         this.name = name;
         this.operation = operation;
      }

      String key() {
         return this.ns + ":" + this.path;
      }
   }
}
