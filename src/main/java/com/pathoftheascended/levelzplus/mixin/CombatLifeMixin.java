package com.pathoftheascended.levelzplus.mixin;

import com.pathoftheascended.levelzplus.CombatLifeAccess;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Tied to the network connection rather than player-entity identity:
 * changing dimension or respawning must not silently restore old credits.
 * Connection tokens are intentionally not persisted across a server restart.
 */
@Mixin(ServerPlayNetworkHandler.class)
public abstract class CombatLifeMixin implements CombatLifeAccess {
   @Unique
   private UUID levelzplus$combatLifeToken = UUID.randomUUID();

   @Override
   public UUID levelzplus$getCombatLifeToken() {
      return levelzplus$combatLifeToken;
   }

   @Override
   public void levelzplus$invalidateCombatLife() {
      levelzplus$combatLifeToken = UUID.randomUUID();
   }
}
