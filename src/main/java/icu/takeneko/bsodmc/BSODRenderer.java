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

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.client.Minecraft.ON_OSX;

public class BSODRenderer {
    public static final String CRYING_FACE = ":(";
    public static final String HAPPY_FACE = ":)";

    private static boolean ACTIVE = false;
    private static BSODRenderer INSTANCE;

    private final Window window;
    private final CrashReport crashReport;
    private final float backgroundColorR;
    private final float backgroundColorG;
    private final float backgroundColorB;
    private final RenderTarget frameBuffer;
    private final boolean useMinecraftFont;
    private final Font font;
    private final List<String> stackTraceLines = new ArrayList<>();
    private final boolean triggeredCrash;


    private BSODRenderer(Window window, CrashReport crashReport) {
        this.window = window;
        this.crashReport = crashReport;
        int clearColor = Util.propertyIntOrDefault("bsod.bgColor", Util.findBSODColor());
        backgroundColorR = FastColor.ARGB32.red(clearColor) / 255f;
        backgroundColorG = FastColor.ARGB32.green(clearColor) / 255f;
        backgroundColorB = FastColor.ARGB32.blue(clearColor) / 255f;
        System.out.println("clearColor = " + clearColor);
        frameBuffer = new MainTarget(window.getWidth(), window.getHeight());
        frameBuffer.setClearColor(backgroundColorR, backgroundColorG, backgroundColorB, 1f);
        frameBuffer.clear(true);
        triggeredCrash = crashReport.getTitle().equals("Manually triggered debug crash");
        stackTraceLines.addAll(Arrays.stream(crashReport.getExceptionMessage().split("\n")).toList());

        if (Minecraft.getInstance() == null) {
            font = null;
            useMinecraftFont = false;
            return;
        }
        font = Minecraft.getInstance().font;
        useMinecraftFont = font != null;
    }

    private void renderFace(PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(10, 10, 0);
        //poseStack.scale(10, 10, 10);
        font.draw(poseStack, triggeredCrash ? HAPPY_FACE : CRYING_FACE, 0, 0, 0xffffffff);
        poseStack.popPose();
    }

    public void render(Tesselator tesselator, PoseStack poseStack) {
        Matrix4f projMat = new Matrix4f()
            .setOrtho(
                0,
                (float) window.getWidth(),
                0,
                (float) window.getHeight(),
                0.1F,
                1000f
            );

        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setProjectionMatrix(projMat);
        poseStack.pushPose();
        if (useMinecraftFont) {
            renderFace(poseStack);
        }
        poseStack.popPose();
    }

    public void renderLoop() {
        ACTIVE = true;
        frameBuffer.unbindWrite();
        while (!GLFW.glfwWindowShouldClose(window.getWindow())) {
            Tesselator tesselator = Tesselator.getInstance();
            RenderSystem.clearColor(backgroundColorR, backgroundColorG, backgroundColorB, 1);
            RenderSystem.clear(16640, ON_OSX);
            frameBuffer.bindWrite(true);
            frameBuffer.setClearColor(backgroundColorR, backgroundColorG, backgroundColorB, 1);
            frameBuffer.clear(ON_OSX);
            this.render(tesselator, new PoseStack());
            frameBuffer.unbindWrite();
            frameBuffer.blitToScreen(this.window.getWidth(), this.window.getHeight());
            tesselator.getBuilder().clear();
            window.updateDisplay();
        }
    }

    public static BSODRenderer createInstance(Window window, CrashReport crashReport) {
        INSTANCE = new BSODRenderer(window, crashReport);
        return INSTANCE;
    }

    public static BSODRenderer getInstance() {
        return INSTANCE;
    }

    public static boolean isActive() {
        return ACTIVE;
    }

    public void resize() {
        this.frameBuffer.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
    }
}
