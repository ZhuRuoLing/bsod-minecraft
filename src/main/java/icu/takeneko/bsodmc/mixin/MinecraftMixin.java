/*
 * This file is part of the BSOD Minecraft project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Fallen_Breath and contributors
 *
 * BSOD Minecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BSOD Minecraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BSOD Minecraft.  If not, see <https://www.gnu.org/licenses/>.
 */

package icu.takeneko.bsodmc.mixin;

import com.mojang.blaze3d.platform.Window;
import icu.takeneko.bsodmc.BSODRenderer;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public static Minecraft getInstance() {
        return null;
    }

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow @Final private Window window;

    @Inject(method = "crash", at = @At(value = "INVOKE", target = "Ljava/lang/System;exit(I)V"))
    private static void onMinecraftCrashed(
        //#if MC >= 12000
        //$$net.minecraft.client.Minecraft minecraft,
        //$$java.io.File file,
        //#endif
        CrashReport crashReport,
        CallbackInfo ci
    ) {
        Window window = getInstance().getWindow();
        if (window == null) return;
        LOGGER.info("Entering BSOD render loop.");
        BSODRenderer.createInstance(window, crashReport).renderLoop();
    }

    @Inject(method = "resizeDisplay", at = @At(value = "HEAD"), cancellable = true)
    private void onResizeDisplay(CallbackInfo ci) {
        if (BSODRenderer.isActive()) {
            this.window.setGuiScale(1);
            BSODRenderer.getInstance().resize();
            ci.cancel();
        }
    }
}
