package anim;

import java.util.TreeSet;

import common.SceneObject;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Quat;
import egl.math.Vector3;

/**
 * A timeline for a particular object in the scene. The timeline holds a
 * sequence of keyframes and a reference to the object that they pertain to. Via
 * linear interpolation between keyframes, the timeline can compute the object's
 * transformation at any point in time.
 * 
 * @author Cristian
 */
public class AnimTimeline {

	/**
	 * A sorted set of keyframes. Invariant: there is at least one keyframe.
	 */
	public final TreeSet<AnimKeyframe> frames = new TreeSet<>(AnimKeyframe.COMPARATOR);

	/**
	 * The object that this timeline animates
	 */
	public final SceneObject object;

	/**
	 * Create a new timeline for an object. The new timeline initially has the
	 * object stationary, with the same transformation it currently has at all
	 * times. This is achieve by createing a timeline with a single keyframe at time
	 * zero.
	 * 
	 * @param o
	 *            Object
	 */
	public AnimTimeline(SceneObject o) {
		object = o;

		// Create A Default Keyframe
		AnimKeyframe f = new AnimKeyframe(0);
		f.transformation.set(o.transformation);
		frames.add(f);
	}

	/**
	 * Add A keyframe to the timeline.
	 * 
	 * @param frame
	 *            Frame number
	 * @param t
	 *            Transformation
	 */
	public void addKeyFrame(int frame, Matrix4 t) {
		// TODO#A6: Add an AnimKeyframe to frames and set its transformation
		AnimKeyframe a = new AnimKeyframe(frame);
		for (AnimKeyframe x : frames) {
			if (x.frame == frame) {
				x.transformation.set(t);
				return;
			}
		}
		a.transformation.set(t);
		frames.add(a);
	}

	/**
	 * Remove a keyframe from the timeline. If the timeline is empty, maintain the
	 * invariant by adding a single keyframe with the given transformation.
	 * 
	 * @param frame
	 *            Frame number
	 * @param t
	 *            Transformation
	 */
	public void removeKeyFrame(int frame, Matrix4 t) {
		// TODO#A6: Delete a frame, you might want to use Treeset.remove
		// If there is no frame after deletion, add back this frame.
		// AnimKeyframe a = new AnimKeyframe(frame);
		// a.transformation.set(t);
		// frames.remove(a);
		// if (frames.size() == 0) {
		// addKeyFrame(frame, t);
		AnimKeyframe x = new AnimKeyframe(frame);
		for (AnimKeyframe a : frames) {
			if (a.frame == frame) {
				x = a;
				break;
			}
		}
		frames.remove(x);
		if (frames.size() == 0) {
			addKeyFrame(frame, t);
		}
	}

	/**
	 * Takes a rotation matrix and decomposes into Euler angles. Returns a Vector3
	 * containing the X, Y, and Z degrees in radians. Formulas from
	 * http://nghiaho.com/?page_id=846
	 */
	public static Vector3 eulerDecomp(Matrix3 mat) {
		double theta_x = Math.atan2(mat.get(2, 1), mat.get(2, 2));
		double theta_y = Math.atan2(-mat.get(2, 0), Math.sqrt(Math.pow(mat.get(2, 1), 2) + Math.pow(mat.get(2, 2), 2)));
		double theta_z = Math.atan2(mat.get(1, 0), mat.get(0, 0));

		return new Vector3((float) theta_x, (float) theta_y, (float) theta_z);
	}

	/**
	 * Update the transformation for the object connected to this timeline to the
	 * current frame
	 * 
	 * @curFrame Current frame number
	 * @rotation Rotation interpolation mode: 0 - Euler angles, 1 - Linear
	 *           interpolation of quaternions, 2 - Spherical linear interpolation of
	 *           quaternions.
	 */
	public void updateTransformation(int curFrame, int rotation) {
		// TODO#A6: You need to get pair of surrounding frames,
		// calculate interpolation ratio,
		// calculate Translation, Scale and Rotation Interpolation,
		// and combine them.
		// Argument curFrame is current frame number
		// Argument rotation is rotation interpolation mode
		// 0 - Euler angles,
		// 1 - Linear interpolation of quaternions,
		// 2 - Spherical linear interpolation of quaternions.
                AnimKeyframe fakeFrame = new AnimKeyframe(curFrame);
                AnimKeyframe ceil = frames.ceiling(fakeFrame);
                AnimKeyframe floor = frames.lower(fakeFrame);
                System.out.println("\nceil:");
                System.out.println(ceil);
                System.out.println("\nfloor:");
                System.out.println(floor);
                if(ceil == null){
                  object.transformation.set(floor.transformation);
                  return;
                }
                if(floor == null){
                  object.transformation.set(ceil.transformation);
                  return;
                }
                float ceilRatio = (float)(curFrame - floor.frame) / (float)(ceil.frame - floor.frame);
                System.out.println("\nceilRatio:");
                System.out.println(ceilRatio);
                float floorRatio = 1.0f - ceilRatio;
                Vector3 ceilT = new Vector3(ceil.transformation.get(0,3), ceil.transformation.get(1,3), ceil.transformation.get(2,3));
                System.out.println("\nceilT:");
                System.out.println(ceilT);
                Vector3 floorT = new Vector3(floor.transformation.get(0,3), floor.transformation.get(1,3), floor.transformation.get(2,3));
                Matrix3 ceilUpLeft = ceil.transformation.getAxes();
                System.out.println("\nceilUpLeft:");
                System.out.println(ceilUpLeft);
                Matrix3 floorUpLeft = floor.transformation.getAxes();
                Matrix3 ceilR = new Matrix3();
                Matrix3 ceilS = new Matrix3();
                ceilUpLeft.polar_decomp(ceilR, ceilS);
                System.out.println("\nceilR:");
                System.out.println(ceilR);
                Matrix3 floorR = new Matrix3();
                Matrix3 floorS = new Matrix3();
                floorUpLeft.polar_decomp(floorR, floorS);

                Vector3 interpT = ceilT.clone().mul(ceilRatio).add(floorT.clone().mul(floorRatio));
                System.out.println("\ninterpT:");
                System.out.println(interpT);
                Vector3 ceilSc = new Vector3(ceilS.get(0,0), ceilS.get(1,1), ceilS.get(2,2));
                System.out.println("\nceilSc:");
                System.out.println(ceilSc);
                Vector3 floorSc = new Vector3(floorS.get(0,0), floorS.get(1,1), floorS.get(2,2));
                Vector3 interpSc = ceilSc.clone().mul(ceilRatio).add(floorSc.clone().mul(floorRatio));
                System.out.println("\ninterpSc:");
                System.out.println(interpSc);

                object.transformation.setIdentity();
                object.addScale(interpSc);
                if(rotation == 0){
                    Vector3 ceilTh = eulerDecomp(ceilR);
                    System.out.println("\nceilTh:");
                    System.out.println(ceilTh);
                    Vector3 floorTh = eulerDecomp(floorR);
                    Vector3 interpTh = ceilTh.clone().mul(ceilRatio).add(floorTh.clone().mul(floorRatio));
                    System.out.println("\ninterpTh:");
                    System.out.println(interpTh);
                    object.addRotation(interpTh.mul((float)(180.0/Math.PI)));
                }
                else{
                    Quat floorQuat = new Quat(floorR);
                    Quat ceilQuat = new Quat(ceilR);
                    Quat interpQuat;
                    if(rotation == 1){
                        interpQuat = ceilQuat.clone().mul(ceilRatio, 0.0f, 0.0f, 0.0f).add(floorQuat.clone().mul(floorRatio, 0.0f, 0.0f, 0.0f)).normalize();
                    }
                    else{
                        interpQuat = Quat.slerp(ceilQuat, floorQuat, floorRatio);
                    }
                    Matrix4 interpR = new Matrix4();
                    object.transformation.mulAfter(interpQuat.toRotationMatrix(interpR));
                }
                object.addTranslation(interpT);
                System.out.println("\nobject.transformation:");
                System.out.println(object.transformation);
	}
}
