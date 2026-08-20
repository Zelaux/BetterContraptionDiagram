package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Math;

public class PreviewGenerator {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public int width;
    public int height;
    protected static double zoom = 1.5;
    public Graphics2D g;
    public Vector3d origin;
    public Matrix4d viewProj;

    public PreviewGenerator(Graphics2D graphics, int width, int height) {
        setGraphics(graphics, width, height);
    }

    public void setGraphics(Graphics2D graphics, int width, int height) {
        this.g = graphics;
        if(graphics != null){
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        this.width = width;
        this.height = height;
    }

    public static void generate(String testName, Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce forceB, QueuedForceGroup.PointForce resultForce) {

        var image = generateImage(a, b, p1, p2, forceB,resultForce);

        try {
            File output = new File("build/test-previews/" + testName + ".png");
            output.getParentFile().mkdirs();
            ImageIO.write(image, "png", output);
            System.out.println("Preview saved to: " + output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static @NotNull BufferedImage generateImage(Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce forceB, QueuedForceGroup.PointForce resultForce) {
        var  image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        PreviewGenerator generator = new PreviewGenerator(image.createGraphics(), WIDTH, HEIGHT);
        generator.generate(a, b, p1, p2, forceB,resultForce);
        return image;
    }

    public void generate(Pose3dc a, Pose3dc b, Vector3dc raw1, Vector3dc raw2, QueuedForceGroup.PointForce forceB, QueuedForceGroup.PointForce resultForce) {
        background();
        // Настройка камеры (Изометрия/Перспектива)
        setup(a, b,raw1,raw2,forceB,resultForce);


        renderGrid(origin, viewProj);
        drawPoint(viewProj, origin, new Color(0, 255, 0,128), 14);

        drawBody(viewProj, a, Color.BLUE, "A");
        drawBody(viewProj, b, Color.RED, "B");

        // Рисуем якоря (Anchors)
        drawPoint(viewProj, localToWorld(a, raw1), Color.BLUE, 12);
        drawPoint(viewProj, localToWorld(b, raw2), Color.RED, 8);


        renderForceArrow(a, resultForce, Color.ORANGE.darker(), 12);
        renderForceArrow(b, forceB, Color.ORANGE, 6);

        g.dispose();
    }

    private void renderForceArrow(Pose3dc b, QueuedForceGroup.PointForce forceB, Color orange, int width) {
        Vector3d forceStart = localToWorld(b, forceB.point());

        Vector3d worldForceDir = b.transformNormal(forceB.force(),new Vector3d());
        Vector3d forceEnd = new Vector3d(forceStart).add(worldForceDir);

        g.setStroke(new BasicStroke(width));
        drawArrow(viewProj, forceStart, forceEnd, orange, width + 4);
    }

    public void setup(Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce forceB, QueuedForceGroup.PointForce resultForce) {
        origin = a.position().lerp(b.position(), 0.5, new Vector3d());
        Vector3d cameraPos = new Vector3d(5, 4, 8).div(zoom).add(origin);
        viewProj = new Matrix4d()
            .perspective(Math.toRadians(45), (double) width / height, 0.1, 100.0)
            .lookAt(cameraPos, origin, new Vector3d(0, 1, 0));
    }

    public void background() {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
    }

    public void renderGrid(Vector3d ORIGIN, Matrix4d viewProj) {

        g.setColor(new Color(220, 220, 220));
        Vector3d s = new Vector3d();
        Vector3d e = new Vector3d();
        for (int i = -5; i <= 5; i++) {
            drawLine(viewProj, ORIGIN.add(i, 0, -5,s), ORIGIN.add(i, 0, 5,e));
            drawLine(viewProj, ORIGIN.add(-5, 0, i,s), ORIGIN.add(5, 0, i,e));
        }
    }

    public  void drawBody(Matrix4d vp, Pose3dc pose, Color color, String label) {

        // Рисуем куб 0.5 (радиус 0.25)
        double r = 0.25;
        Vector3d[] corners = {
            new Vector3d(-r, -r, -r), new Vector3d(r, -r, -r), new Vector3d(r, r, -r), new Vector3d(-r, r, -r),
            new Vector3d(-r, -r, r), new Vector3d(r, -r, r), new Vector3d(r, r, r), new Vector3d(-r, r, r)
        };

        for (Vector3d c : corners) {
            pose.transformPosition(c.add(pose.rotationPoint()));
        }

        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        for(int i = 0; i < 4; i++) {
            int j=i+4;
            int i1 = (i + 1) & 0b11;
            int j1=i1+4;
            drawLine(vp, corners[i], corners[i1]);
            drawLine(vp, corners[j], corners[j1]);
            drawLine(vp, corners[i], corners[j]);
        }

        // Локальные оси
        Vector3dc origin = pose.position();
        drawArrow(vp, origin, localToWorld(pose, new Vector3d(0.6,0,0).add(pose.rotationPoint())), Color.RED); // X
        drawArrow(vp, origin, localToWorld(pose, new Vector3d(0,0.6,0).add(pose.rotationPoint())), Color.GREEN); // Y
        drawArrow(vp, origin, localToWorld(pose, new Vector3d(0,0,0.6).add(pose.rotationPoint())), Color.BLUE); // Z

        Point p = project(vp, pose.position());
        g.setColor(color);
        g.drawString(label, p.x + 5, p.y - 5);
    }

    public  Vector3d localToWorld(Pose3dc pose, Vector3dc local) {
        return pose.transformPosition(local, new Vector3d());
    }
    public  Vector3d localToWorld(Pose3dc pose, Vector3d local) {
        return pose.transformPosition(local);
    }

    public  void drawLine(Matrix4dc vp, Vector3dc start, Vector3dc end) {
        Point p1 = project(vp, start);
        Point p2 = project(vp, end);
        if (p1 != null && p2 != null) g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    public void drawArrow(Matrix4dc vp, Vector3dc start, Vector3dc end, Color color) {
        drawArrow(vp, start, end, color, 4);
    }

    private void drawArrow(Matrix4dc vp, Vector3dc start, Vector3dc end, Color color, int size) {
        g.setColor(color);
        drawLine(vp, start, end);
        drawPoint(vp, start, color, size);
    }

    public void drawPoint(Matrix4dc vp, Vector3dc worldPos, Color color, int size) {
        Point p = project(vp, worldPos);
        if (p != null) {
            g.setColor(color);
            g.fillOval(p.x - size / 2, p.y - size / 2, size, size);
        }
    }

    public Point project(Matrix4dc vp, Vector3dc pos) {
        Vector4d res = new Vector4d(pos, 1.0).mul(vp);
        if (res.w <= 0) return null;
        double x = (res.x / res.w + 1.0) * 0.5 * width;
        double y = (1.0 - (res.y / res.w + 1.0) * 0.5) * height;
        return new Point((int) x, (int) y);
    }
}