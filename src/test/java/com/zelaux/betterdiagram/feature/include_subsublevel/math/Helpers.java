package com.zelaux.betterdiagram.feature.include_subsublevel.math;

import com.zelaux.betterdiagram.BetterContraptionDiagram;
import com.zelaux.betterdiagram.DebugRuntime;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import org.apache.commons.lang3.DoubleRange;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Helpers {

    public static OnTestInvoked onInvoked = (testName, a, b, p1, p2, force, resultForce) -> {
        PreviewGenerator.generate(testName, a, b, p1, p2, force,resultForce);
        InteractivePreviewPanel.showInFrame(testName, a, b, p1, p2, force,resultForce);
    };

    Vector3d half() {
        return vec3(0.5, 0.5, 0.5);
    }

    Vector3d zero() {
        return vec3(0, 0, 0);
    }

    Vector3d one() {
        return vec3(1, 1, 1);
    }

    double[] arr(Vector3dc vec) {
        return new double[]{vec.x(), vec.y(), vec.z()};
    }

    String str(Vector3dc vec) {
        return "%.6f\n%.6f\n%.6f"
            .formatted(vec.x(), vec.y(), vec.z())
            .replace("%.6f".formatted(-0.0),"%.6f".formatted(0.0))
            ;
    }

    QueuedForceGroup.PointForce pointForce(Vector3d point, Vector3d force) {
        return new QueuedForceGroup.PointForce(point, force);
    }

    Pose3d pose(Vector3d position, Quaterniond orientation, Vector3d rotationPoint) {
        return new Pose3d(position, orientation, rotationPoint, new Vector3d(1, 1, 1));
    }

    Vector3d vec3(double x, double y, double z) {
        return new Vector3d(x, y, z);
    }

    Quaterniond quatIdt() {
        return new Quaterniond().identity();
    }

    QueuedForceGroup.PointForce project(
        Pose3dc a, Pose3dc b,
        Vector3dc p1, Vector3dc p2,
        Quaterniondc o1, Quaterniondc o2,
        Set<ConstraintJointAxis> fixed,
        @Nullable
        Map<ConstraintJointAxis, DoubleRange> ranges,
        QueuedForceGroup.PointForce force
    ) {


        ConstraintCalculator calculator = new ConstraintCalculator();
        calculator.generic(
            a, b, p1, p2, o1, o2, force, fixed, ranges
        );
        Vector3d point = new Vector3d();
        Vector3d forceDest = new Vector3d();
        calculator.getForceAndPoint(a, forceDest, point);
        QueuedForceGroup.PointForce result = new QueuedForceGroup.PointForce(point, forceDest);

        doSomething(a, b, p1, p2, force, result);

        return result;

    }

    private void doSomething(Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce force, QueuedForceGroup.PointForce result) {
        if(!BetterContraptionDiagram.isDebug_test())return;
        Optional<StackWalker.StackFrame> walk = StackWalker.getInstance().walk(it ->
            it.filter(x -> !x.getClassName().equals(Helpers.class.getName())).findFirst()
        );

        if(walk.isPresent()){
            StackWalker.StackFrame frame = walk.get();
            String className = frame.getClassName();
            className= className.substring(className.lastIndexOf('.')+1);
            String testName = className + "/" + frame.getMethodName();
            System.out.println(frame.toStackTraceElement());
            onInvoked.doSomething(testName, a, b, p1, p2, force, result);
        }
    }


    protected void assertEquals(QueuedForceGroup.PointForce expected, QueuedForceGroup.PointForce actual, String message) {
        try {
            Assertions.assertEquals(str(expected), str(actual), message);
        } catch(AssertionFailedError e) {
            removeHelperEquals(e);
            throw e;
        }
    }
    protected void assertEqualsWithTorque(QueuedForceGroup.PointForce expected, QueuedForceGroup.PointForce actual, Vector3dc CoM, String message) {
        try {
            Assertions.assertEquals(str(expected,CoM), str(actual,CoM), message);
        } catch(AssertionFailedError e) {
            removeHelperEquals(e);
            throw e;
        }
    }

    private void removeHelperEquals(AssertionFailedError e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        int startI=-1;
        for(int i = stackTrace.length - 1; i >= 0; i--) {
            if(stackTrace[i].getClassName().equals(Helpers.class.getName())) {
                startI=i+1;
                break;
            }
        }
        if(startI!=-1){
            StackTraceElement[] newTrace=new StackTraceElement[stackTrace.length-startI];
            System.arraycopy(stackTrace,startI,newTrace,0,newTrace.length);
            e.setStackTrace(newTrace);
        }
    }

    protected String str(QueuedForceGroup.PointForce pointForce) {
        return "point\n"+str(pointForce.point())+"\n\nforce\n"+str(pointForce.force());
    }
    protected String str(QueuedForceGroup.PointForce pointForce,Vector3dc CoM) {
        return "torque\n"+str(new Vector3d(pointForce.point()).sub(CoM).cross(pointForce.force()))+"\n\nforce\n"+str(pointForce.force());
    }

    @FunctionalInterface
    public interface OnTestInvoked{
        void doSomething(String testName, Pose3dc a, Pose3dc b, Vector3dc p1, Vector3dc p2, QueuedForceGroup.PointForce force, QueuedForceGroup.PointForce resultForce);
    }
}
