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
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.client.Minecraft.ON_OSX;

public class BSODRenderer {
    public static final String CRYING_FACE = ":(  #@!@# Game crashed!";
    public static final String HAPPY_FACE = ":)  #@!@# Game crashed!";

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
    private float guiScale = 2;
    private int textHue = 1;
    private int hueIncrement = 1;
    //#if MC >= 12100
    //$$ private final com.mojang.blaze3d.vertex.ByteBufferBuilder builder = new com.mojang.blaze3d.vertex.ByteBufferBuilder(65536);
    //#endif


    private BSODRenderer(Window window, CrashReport crashReport) {
        this.window = window;
        this.crashReport = crashReport;
        int clearColor = Util.propertyIntOrDefault("bsod.bgColor", Util.findBSODColor());
        backgroundColorR = Util.argbRed(clearColor) / 255f;
        backgroundColorG = Util.argbGreen(clearColor) / 255f;
        backgroundColorB = Util.argbBlue(clearColor) / 255f;
        System.out.println("clearColor = " + clearColor);
        frameBuffer = new MainTarget(window.getWidth(), window.getHeight());
        frameBuffer.setClearColor(backgroundColorR, backgroundColorG, backgroundColorB, 1f);
        //#if MC >= 12103
        //$$frameBuffer.clear();
        //#else
        frameBuffer.clear(true);
        //#endif
        this.resize();
        triggeredCrash = crashReport.getTitle().equals("Manually triggered debug crash");
        stackTraceLines.addAll(
            Arrays.stream(crashReport.getExceptionMessage().split("\n"))
                .map(it -> it.replace("\r", "").replace("\t", "  "))
                .toList()
        );

        if (Minecraft.getInstance() == null) {
            font = null;
            useMinecraftFont = false;
            return;
        }
        font = Minecraft.getInstance().font;
        useMinecraftFont = font != null;
    }

    private void drawText(PoseStack poseStack, String text, int color) {
        //#if MC < 12100
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        //#else
        //$$MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(builder);
        //#endif
        font.drawInBatch(text, 0, 0, color, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        bufferSource.endBatch();
    }

    public void render(Tesselator tesselator, PoseStack poseStack) {
        Matrix4f projMat = new Matrix4f()
            .setOrtho(
                0,
                (float) window.getWidth() / guiScale,
                (float) window.getHeight() / guiScale,
                0,
                -1000F,
                1000f
            );
        RenderSystem.disableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.getModelViewMatrix().set(new Matrix4f());
        RenderSystem.getProjectionMatrix().set(projMat);
        poseStack.pushPose();
        if (useMinecraftFont) {
            poseStack.pushPose();
            poseStack.translate(10, 10, 10);

            poseStack.pushPose();
            poseStack.scale(4, 4, 4);
            drawText(poseStack, triggeredCrash ? HAPPY_FACE : CRYING_FACE, 0xffffffff);

            poseStack.popPose();

            poseStack.translate(0, (font.lineHeight + 2) * 4, 0);
            int importantColor = hsvToRgb(textHue / 512f, 1, 1);
            drawText(
                poseStack,
                "When seeking help, please provide them with your crash-report file",
                importantColor
            );
            poseStack.translate(0, font.lineHeight, 0);
            drawText(
                poseStack,
                "instead of a screenshot of the window!",
                importantColor
            );

            poseStack.translate(0, font.lineHeight, 0);
            for (String line : stackTraceLines) {
                poseStack.translate(0, font.lineHeight + 2, 0);
                drawText(poseStack, line, 0xffffffff);
            }

            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static int hsvToRgb(float hue, float saturation, float value) {
        Color color = Color.getHSBColor(hue, saturation, value);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return Util.argbCompose(255, red, green, blue);
    }

    public void renderLoop() {
        ACTIVE = true;
        frameBuffer.unbindWrite();
        GLFW.glfwSwapInterval(1);
        while (!GLFW.glfwWindowShouldClose(window.getWindow())) {
            textHue += hueIncrement;
            if (textHue == 512) hueIncrement = -1;
            if (textHue == 0) hueIncrement = 1;
            Tesselator tesselator = Tesselator.getInstance();
            RenderSystem.clearColor(backgroundColorR, backgroundColorG, backgroundColorB, 1);
            //#if MC >= 12103
            //$$RenderSystem.clear(16640);
            //#else
            RenderSystem.clear(16640, ON_OSX);
            //#endif
            frameBuffer.setClearColor(backgroundColorR, backgroundColorG, backgroundColorB, 1);
            //#if MC >= 12103
            //$$frameBuffer.clear();
            //#else
            frameBuffer.clear(ON_OSX);
            //#endif
            frameBuffer.bindWrite(true);
            this.render(tesselator, new PoseStack());
            frameBuffer.unbindWrite();
            frameBuffer.blitToScreen(this.window.getWidth(), this.window.getHeight());
            //#if MC < 12100
            tesselator.getBuilder().clear();
            //#else
            //$$builder.clear();
            //#endif
            //#if MC >= 12103
            //$$window.updateDisplay(null);
            //#else
            window.updateDisplay();
            //#endif

        }
        //#if MC >= 12100
        //$$builder.close();
        //#endif
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
        int width = this.window.getWidth();
        int height = this.window.getHeight();
        //#if MC >= 12103
        //$$this.frameBuffer.resize(width, height);
        //#else
        this.frameBuffer.resize(width, height, ON_OSX);
        //#endif
        this.guiScale = this.window.calculateScale(2, true);
    }
}
