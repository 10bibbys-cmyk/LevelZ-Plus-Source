package com.pathoftheascended.levelzplus.compat.jobs.mixin;

import com.pathoftheascended.levelzplus.compat.jobs.JobsAddonCompatConfig;
import java.lang.reflect.Field;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the JobsAddon profession screen readable but not deselectable. */
@Pseudo
@Mixin(targets = "net.jobsaddon.screen.JobScreen", remap = false)
public abstract class JobsAddonJobScreenMixin {
    /**
     * Replace only the two employment/cooldown labels. All profession
     * icons, titles, XP bars and tooltip hitboxes remain JobsAddon's own.
     *
     * JobScreen.render calls DrawContext.drawText for the title first,
     * then cooldown (ordinal 1), then employed jobs (ordinal 2).
     * Fabric 1.20.1 production intermediary DrawContext method_51439 is
     * used directly because the third-party JobScreen mixin has remap=false.
     * Unknown target revisions fail open rather than breaking screen load.
     */
    @ModifyArg(
        method = {"method_25394", "render"},
        at = @At(value = "INVOKE", target =
            "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)I",
            ordinal = 1),
        index = 1, require = 0, remap = false
    )
    private Text levelzplus$replaceCooldown(Text original) {
        return JobsAddonCompatConfig.lockAllJobs() && JobsAddonCompatConfig.customJobsScreenHeader()
            ? Text.translatable("text.levelz_plus.jobsaddon.active_header") : original;
    }

    @ModifyArg(
        method = {"method_25394", "render"},
        at = @At(value = "INVOKE", target =
            "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)I",
            ordinal = 2),
        index = 1, require = 0, remap = false
    )
    private Text levelzplus$replaceEmployed(Text original) {
        return JobsAddonCompatConfig.lockAllJobs() && JobsAddonCompatConfig.customJobsScreenHeader()
            ? Text.translatable("text.levelz_plus.jobsaddon.hover_hint") : original;
    }

    @Inject(method = {"method_25426", "init"}, at = @At("TAIL"), require = 0, remap = false)
    private void levelzplus$disableJobButtons(CallbackInfo ci) {
        if (!JobsAddonCompatConfig.lockAllJobs()) return;
        try {
            Field buttonsField = this.getClass().getDeclaredField("jobButtons");
            buttonsField.setAccessible(true);
            Object buttons = buttonsField.get(this);
            if (buttons instanceof Object[] jobs) {
                for (Object button : jobs) {
                    if (button instanceof ClickableWidget widget) widget.active = false;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // Server-side JobsManager.quitJob guard remains authoritative.
        }
    }
}
