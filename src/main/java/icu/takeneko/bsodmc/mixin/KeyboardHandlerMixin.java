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

import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    private static final boolean DEBUG = System.getProperty("bsod.debug") != null;
    @Unique
    private static int count = 0;
    @Unique
    private boolean crashed = false;

    @Inject(method = "keyPress", at = @At("HEAD"))
    void handleCrashKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        if (DEBUG && !crashed && action != GLFW.GLFW_RELEASE) {
            //C x10
            if (key == 67) {
                if (count++ > 5) {
                    crashed = true;
                    throw new RuntimeException("Debug crash");
                }
            }
        }
    }
}
