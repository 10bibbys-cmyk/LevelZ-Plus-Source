package com.pathoftheascended.levelzplus;

import net.minecraft.entity.damage.DamageSource;

/** Allows multipart bosses to report verified damage to the common credit ledger. */
public interface CombatContributorAccess {
   void levelzplus$recordCombatHit(DamageSource source);
}
