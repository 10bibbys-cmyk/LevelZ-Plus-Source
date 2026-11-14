package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.CombatContributorAccess;
import com.pathoftheascended.levelzplus.CombatLifeAccess;
import com.pathoftheascended.levelzplus.CompatConfig;
import com.pathoftheascended.levelzplus.LevelZExperienceBridge;
import com.pathoftheascended.levelzplus.ProgressionXpConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.levelz.access.MobEntityAccess;
import net.levelz.access.PlayerStatsManagerAccess;
import net.levelz.init.ConfigInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * One credit per player per mob, locked to the connection's survival token
 * on their FIRST successful hit. This first token is never overwritten.
 *
 * A disconnect creates a new connection token; a death rotates the existing
 * connection token. Thus later hits cannot restore forfeited eligibility,
 * even if the victim was unloaded and later reloaded from NBT.
 */
@Mixin(LivingEntity.class)
public abstract class CombatContributionMixin extends Entity implements CombatContributorAccess {
   @Unique
   private static final String levelzplus$CREDITS = "LevelZPlusCombatCreditsV3";
   @Unique
   private static final String levelzplus$AWARDED = "LevelZPlusCombatAwardedV3";

   @Unique
   private final LinkedHashMap<UUID, UUID> levelzplus$contributors = new LinkedHashMap<>();
   @Unique
   private boolean levelzplus$combatXpAwarded;

   public CombatContributionMixin(EntityType<?> type, World world) {
      super(type, world);
   }

   @Shadow
   protected abstract int getXpToDrop();

   @Inject(method = "damage", at = @At("RETURN"))
   private void levelzplus$successfulDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
      if (amount > 0.0F && Boolean.TRUE.equals(cir.getReturnValue())) {
         levelzplus$recordCombatHit(source);
      }
   }

   /** Also invoked by DragonDamageMixin because multipart hits bypass LivingEntity.damage. */
   @Override
   public void levelzplus$recordCombatHit(DamageSource source) {
      if (this.getWorld().isClient() || !ProgressionXpConfig.combatEnabled() || levelzplus$combatXpAwarded) {
         return;
      }
      if ((Object) this instanceof PlayerEntity) {
         return;
      }
      ServerPlayerEntity player = levelzplus$resolvePlayer(source);
      if (player == null || !player.isAlive() || player.isSpectator() || player.networkHandler == null) {
         if (player == null) {
            CompatConfig.debugLog("CombatXP", "accepted damage without identifiable player: " + getType());
         }
         return;
      }

      // No overwrite. Death and disconnect permanently disqualify this UUID
      // for this particular mob, including after a subsequent hit.
      UUID life = ((CombatLifeAccess) player.networkHandler).levelzplus$getCombatLifeToken();
      UUID original = levelzplus$contributors.putIfAbsent(player.getUuid(), life);
      if (original == null) {
         CompatConfig.debugLog("CombatXP", "credited " + player.getEntityName() + " against " + getUuid());
      } else if (!original.equals(life)) {
         CompatConfig.debugLog("CombatXP", "rejected post-death/reconnect hit by " + player.getEntityName());
      }
   }

   /**
    * HEAD runs before a lethal LivingEntity.damage call returns, so the
    * finisher is credited even though the damage RETURN hook has not run.
    */
   @Inject(method = "onDeath", at = @At("HEAD"))
   private void levelzplus$awardOnDeath(DamageSource source, CallbackInfo ci) {
      if (this.getWorld().isClient() || levelzplus$combatXpAwarded || !ProgressionXpConfig.combatEnabled()) {
         return;
      }
      LivingEntity victim = (LivingEntity) (Object) this;
      if (victim instanceof PlayerEntity || !(this.getWorld() instanceof ServerWorld world)) {
         return;
      }

      // Only the actual fatal damage source is used here. Never fall back to
      // getAttackingPlayer(): it can refer to a previous, unrelated attacker.
      levelzplus$recordCombatHit(source);

      if (!ProgressionXpConfig.independentXpReady()
            || !world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
         CompatConfig.debugLog("CombatXP", "skipped: independent XP disabled or doMobLoot=false");
         return;
      }
      if (victim instanceof MobEntity mob && !ConfigInit.CONFIG.spawnerMobXP
            && ((MobEntityAccess) mob).isSpawnerMob()) {
         CompatConfig.debugLog("CombatXP", "skipped spawner mob: " + getType());
         return;
      }
      if (levelzplus$contributors.isEmpty()) {
         CompatConfig.debugLog("CombatXP", "skipped no contributors: " + getType());
         return;
      }

      double rawReward;
      if (victim instanceof EnderDragonEntity dragon) {
         // LevelZ 1.4.13 awards 12,000 dragon base XP on the first kill and
         // 500 on subsequent kills, multiplied by dragonXPMultiplier.
         // Native dragon XP must be zero to avoid duplicate LevelZ orbs.
         if (ConfigInit.CONFIG.dragonXPMultiplier > 0.0F) {
            CompatConfig.debugLog("CombatXP", "dragon uses native XP; disable it to use shared dragon rewards");
            return;
         }
         EnderDragonFight fight = dragon.getFight();
         if (fight == null) {
            System.err.println("[LevelZ+] [CombatXP] Dragon has no EnderDragonFight; XP not awarded.");
            return;
         }
         int base = fight.hasPreviouslyKilled() ? 500 : 12000;
         rawReward = base * ProgressionXpConfig.combatDragonXpMultiplier()
               + Math.max(0.0, victim.getMaxHealth()) * ProgressionXpConfig.combatMaxHealthFraction();
      } else {
         int base = Math.max(0, this.getXpToDrop());
         rawReward = base * ProgressionXpConfig.combatMobXpMultiplier()
               + Math.max(0.0, victim.getMaxHealth()) * ProgressionXpConfig.combatMaxHealthFraction();
      }

      if (!Double.isFinite(rawReward) || rawReward <= 0.0) {
         CompatConfig.debugLog("CombatXP", "skipped zero/invalid reward: " + getType());
         return;
      }

      // Mark once before distribution. Never create a pending/offline queue.
      levelzplus$combatXpAwarded = true;
      int paid = 0;
      int ineligible = 0;
      int failed = 0;

      for (Map.Entry<UUID, UUID> credit : levelzplus$contributors.entrySet()) {
         ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(credit.getKey());
         if (player == null || player.networkHandler == null || !player.isAlive() || player.isSpectator()
               || !credit.getValue().equals(
                     ((CombatLifeAccess) player.networkHandler).levelzplus$getCombatLifeToken())) {
            ineligible++;
            continue;
         }
         double levelScale = 1.0;
         if (ConfigInit.CONFIG.dropXPbasedOnLvl && player instanceof PlayerStatsManagerAccess stats) {
            levelScale += ConfigInit.CONFIG.basedOnMultiplier
                  * stats.getPlayerStatsManager().getOverallLevel();
         }
         double rewardScale = levelScale * ProgressionXpConfig.rewardFractionPerContributor();
         if (!Double.isFinite(rewardScale) || rewardScale <= 0.0) {
            continue;
         }
         // Preserve a minimum of 1 XP for a positive configured reward.
         int reward = Math.max(1, LevelZExperienceBridge.scale(rawReward, rewardScale));
         if (LevelZExperienceBridge.award(player, reward, "combat:" + getType())) {
            paid++;
         } else {
            failed++;
            System.err.println("[LevelZ+] [CombatXP] Could not grant " + reward + " XP to "
                  + player.getEntityName() + " after killing " + getType() + ".");
         }
      }

      CompatConfig.debugLog("CombatXP", getType() + " rawXP=" + rawReward
            + " credits=" + levelzplus$contributors.size() + " paid=" + paid
            + " ineligible=" + ineligible + " failed=" + failed);
   }

   @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
   private void levelzplus$writeCredits(NbtCompound nbt, CallbackInfo ci) {
      if (levelzplus$combatXpAwarded) {
         nbt.putBoolean(levelzplus$AWARDED, true);
      }
      if (levelzplus$contributors.isEmpty()) {
         return;
      }
      NbtList list = new NbtList();
      for (Map.Entry<UUID, UUID> entry : levelzplus$contributors.entrySet()) {
         NbtCompound credit = new NbtCompound();
         credit.putUuid("player", entry.getKey());
         credit.putUuid("life", entry.getValue());
         list.add(credit);
      }
      nbt.put(levelzplus$CREDITS, list);
   }

   @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
   private void levelzplus$readCredits(NbtCompound nbt, CallbackInfo ci) {
      levelzplus$contributors.clear();
      levelzplus$combatXpAwarded = nbt.getBoolean(levelzplus$AWARDED);
      if (!nbt.contains(levelzplus$CREDITS, NbtElement.LIST_TYPE)) {
         return;
      }
      NbtList list = nbt.getList(levelzplus$CREDITS, NbtElement.COMPOUND_TYPE);
      for (int i = 0; i < list.size(); i++) {
         NbtCompound credit = list.getCompound(i);
         if (credit.containsUuid("player") && credit.containsUuid("life")) {
            levelzplus$contributors.putIfAbsent(credit.getUuid("player"), credit.getUuid("life"));
         }
      }
   }

   @Unique
   private ServerPlayerEntity levelzplus$resolvePlayer(DamageSource source) {
      if (source == null) return null;
      ServerPlayerEntity player = levelzplus$resolveOwner(source.getAttacker(), 0);
      return player != null ? player : levelzplus$resolveOwner(source.getSource(), 0);
   }

   @Unique
   private ServerPlayerEntity levelzplus$resolveOwner(Entity source, int depth) {
      if (source == null || depth >= 8) return null;
      if (source instanceof ServerPlayerEntity player) return player;
      if (source instanceof ProjectileEntity projectile)
         return levelzplus$resolveOwner(projectile.getOwner(), depth + 1);
      if (source instanceof TntEntity tnt)
         return levelzplus$resolveOwner(tnt.getOwner(), depth + 1);
      if (source instanceof TameableEntity tameable)
         return levelzplus$resolveOwner(tameable.getOwner(), depth + 1);
      return null;
   }
}
