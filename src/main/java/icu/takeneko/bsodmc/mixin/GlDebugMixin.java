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

import com.mojang.blaze3d.platform.GlDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlDebug.class)
public class GlDebugMixin {
    private static final boolean DEBUG = System.getProperty("bsod.debug") != null;

    @Inject(method = "printDebugLog", at = @At("HEAD"), cancellable = true)
    private static void noLogsPlease(int i, int j, int k, int l, int m, long n, long o, CallbackInfo ci) {
        if (DEBUG) ci.cancel();
    }
}
