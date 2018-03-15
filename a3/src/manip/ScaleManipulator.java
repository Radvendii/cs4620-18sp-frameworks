package manip;

import egl.math.*;
import gl.RenderObject;

public class ScaleManipulator extends Manipulator {

	public ScaleManipulator(ManipulatorAxis axis) {
		super();
		this.axis = axis;
	}

	public ScaleManipulator(RenderObject reference, ManipulatorAxis axis) {
		super(reference);
		this.axis = axis;
	}

	@Override
	protected Matrix4 getReferencedTransform() {
		if (this.reference == null) {
			throw new RuntimeException("Manipulator has no controlled object!");
		}
		return new Matrix4().set(reference.scale).mulAfter(reference.rotationZ).mulAfter(reference.rotationY)
				.mulAfter(reference.rotationX).mulAfter(reference.translation);
	}

	@Override
	public void applyTransformation(Vector2 lastMousePos, Vector2 curMousePos, Matrix4 viewProjection) {
		// TODO#A3: Modify this.reference.scale given the mouse input.
		// Use this.axis to determine the axis of the transformation.
		// Note that the mouse positions are given in coordinates that are normalized to
		// the range [-1, 1]
		// for both X and Y. That is, the origin is the center of the screen, (-1,-1) is
		// the bottom left
		// corner of the screen, and (1, 1) is the top right corner of the screen.
		Matrix4 inverse = viewProjection.invert();

		Vector3 c1 = new Vector3(lastMousePos.x, lastMousePos.y, -1);
		Vector3 c2 = new Vector3(lastMousePos.x, lastMousePos.y, 1);
		c1 = inverse.clone().mulPos(c1);
		c2 = inverse.clone().mulPos(c2);
		Vector3 ray1 = c2.clone().sub(c1);

		Vector3 c3 = new Vector3(curMousePos.x, curMousePos.y, -1);
		Vector3 c4 = new Vector3(curMousePos.x, curMousePos.y, 1);
		c3 = inverse.clone().mulPos(c3);
		c4 = inverse.clone().mulPos(c4);
		Vector3 ray2 = c4.clone().sub(c3);

		Vector3 origin = getReferencedTransform().clone().mulPos(new Vector3(0, 0, 0));
		Vector3 dirx = getReferencedTransform().clone().mulDir(new Vector3(1, 0, 0));
		Vector3 diry = getReferencedTransform().clone().mulDir(new Vector3(0, 1, 0));
		Vector3 dirz = getReferencedTransform().clone().mulDir(new Vector3(0, 0, 1));
		Vector3 dir = new Vector3();

		if (axis == ManipulatorAxis.X) {
			dir = dirx;
		} else if (axis == ManipulatorAxis.Y) {
			dir = diry;
		} else {
			dir = dirz;
		}

		Vector3 u1 = dir.clone().cross(ray1);
		Vector3 w1 = dir.clone().cross(u1);
		Vector3 temps1 = origin.clone().sub(c1);
		float s1 = (temps1.dot(w1)) / (ray1.clone().dot(w1));
		Vector3 p1 = ray1.clone().mul(s1).clone().add(c1);

		Vector3 u2 = dir.clone().cross(ray2);
		Vector3 w2 = dir.clone().cross(u2);
		Vector3 temps2 = origin.clone().sub(c3);
		float s2 = (temps2.dot(w2)) / (ray2.clone().dot(w2));
		Vector3 p2 = ray2.clone().mul(s2).clone().add(c3);

		float tempt1 = (p1.clone().sub(origin)).clone().dot(dir);
		float t1 = tempt1 / dir.lenSq();

		float tempt2 = (p2.clone().sub(origin)).clone().dot(dir);
		float t2 = tempt2 / dir.lenSq();
		if (t1 == 0)
			return;
		float t = t2 / t1;
		if (t == 0)
			return;

		Matrix4 temp = new Matrix4();
		if (axis == ManipulatorAxis.X) {
			temp = Matrix4.createScale(t, 1, 1);
			this.reference.scale.mulAfter(temp);
		} else if (axis == ManipulatorAxis.Y) {
			temp = Matrix4.createScale(1, t, 1);
			this.reference.scale.mulAfter(temp);
		} else {
			temp = Matrix4.createScale(1, 1, t);
			this.reference.scale.mulAfter(temp);
		}
	}

	@Override
	protected String meshPath() {
		return "data/meshes/Scale.obj";
	}

}