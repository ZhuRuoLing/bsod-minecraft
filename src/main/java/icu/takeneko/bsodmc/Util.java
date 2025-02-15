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

package icu.takeneko.bsodmc;

public class Util {
    public static int propertyIntOrDefault(String key, int defaultValue) {
        String colorString = System.getProperty(key);
        try {
            if (colorString == null) return defaultValue;
            return Integer.parseInt(colorString);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int findBSODColor() {
        //TODO: return GREEN when Minecraft snapshot version is active
        return ModConstants.BLUE;
    }
}
