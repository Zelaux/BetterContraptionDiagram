package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.CountDownLatch;

public class InteractivePreviewPanel extends JPanel {
    // Данные для отрисовки
    private Pose3dc a, b;
    private Vector3dc p1, p2;
    private QueuedForceGroup.PointForce forceB,resultForce;

    // Параметры камеры
    private double rotationX = Math.toRadians(-25);
    private double rotationY = Math.toRadians(45);
    private double currentZoom = 1.5;
    private Point lastMousePos;

    // Внутренний генератор, который мы переиспользуем
    private final PreviewGenerator renderer;

    public InteractivePreviewPanel() {
        this.renderer = new PreviewGenerator(null, 1, 1){
            @Override
            public void setup(Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce forceB, QueuedForceGroup.PointForce resultForce) {
                super.setup(a, b, p1, p2, forceB, resultForce);
                setupCustomCamera();
            }
        };
        setBackground(Color.WHITE);

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null) {
                    double dx = -(e.getX() - lastMousePos.x) * 0.01;
                    double dy = (e.getY() - lastMousePos.y) * 0.01;
                    rotationY += dx;
                    rotationX = Math.clamp(rotationX + dy,-Math.PI/2+0.001,Math.PI/2-0.001);
                    lastMousePos = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                currentZoom -= e.getPreciseWheelRotation() * 0.1;
                currentZoom = Math.max(0.1, Math.min(10, currentZoom));
                repaint();
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    public void setData(Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce forceB, QueuedForceGroup.PointForce resultForce) {
        this.a = a;
        this.b = b;
        this.p1 = p1;
        this.p2 = p2;
        this.forceB = forceB;
        this.resultForce = resultForce;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (a == null || b == null) return;

        Graphics2D g2 = (Graphics2D) g;

        // Обновляем состояние генератора под текущий размер и графику
        renderer.setGraphics(g2,getWidth(),getHeight());
        // Рефлексия или изменение доступа к полям PreviewGenerator не требуется,
        // так как мы просто вызываем методы.

        // Подменяем логику setup прямо здесь или через наследника
        //setupCustomCamera();

        // Вызываем оригинальный метод отрисовки из PreviewGenerator
        renderer.generate(a, b, p1, p2, forceB, resultForce);
    }

    private void setupCustomCamera() {
        // Пересчитываем матрицу вида на основе rotationX, rotationY
        renderer.origin = a.position().lerp(b.position(), 0.5, new Vector3d());

        // Вычисляем позицию камеры на сфере
        double dist = 10.0 / currentZoom;
        Vector3d cameraPos = new Vector3d(
            dist * Math.cos(rotationX) * Math.sin(rotationY),
            dist * Math.sin(rotationX),
            dist * Math.cos(rotationX) * Math.cos(rotationY)
        ).add(renderer.origin);

        renderer.viewProj = new Matrix4d()
            .perspective(Math.toRadians(45), (double) getWidth() / getHeight(), 0.1, 100.0)
            .lookAt(cameraPos, renderer.origin, new Vector3d(0, 1, 0));
    }

    // Статический метод для быстрого запуска окна
    public static void showInFrame(String testName, Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce forceB, QueuedForceGroup.PointForce resultForce) {
        JFrame frame = new JFrame("3D Preview - Interactive: "+testName);

        CountDownLatch latch = new CountDownLatch(1);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                latch.countDown();
            }
        });

        InteractivePreviewPanel panel = new InteractivePreviewPanel();
        panel.setData(a, b, p1, p2, forceB,resultForce);

        frame.add(panel);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


    }
}