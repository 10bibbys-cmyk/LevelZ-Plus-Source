package com.pathoftheascended.levelzplus;

import java.util.UUID;

/**
 * A connection-scoped survival token. Reconnecting creates a new token;
 * dying rotates it even when the same connection remains open.
 */
public interface CombatLifeAccess {
   UUID levelzplus$getCombatLifeToken();
   void levelzplus$invalidateCombatLife();
}
