package manip;

import egl.math.*;
import gl.RenderObject;

public class RotationManipulator extends Manipulator {

  protected String meshPath = "Rotate.obj";

  public RotationManipulator(ManipulatorAxis axis) {
    super();
    this.axis = axis;
  }

  public RotationManipulator(RenderObject reference, ManipulatorAxis axis) {
    super(reference);
    this.axis = axis;
  }

  //assume X, Y, Z on stack in that order
  @Override
  protected Matrix4 getReferencedTransform() {
    Matrix4 m = new Matrix4();
    switch (this.axis) {
      case X:
        m.set(reference.rotationX).mulAfter(reference.translation);
        break;
      case Y:
        m.set(reference.rotationY)
          .mulAfter(reference.rotationX)
          .mulAfter(reference.translation);
        break;
      case Z:
        m.set(reference.rotationZ)
          .mulAfter(reference.rotationY)
          .mulAfter(reference.rotationX)
          .mulAfter(reference.translation);
        break;
    }
    return m;
  }

  public Matrix3 replaceCol(Matrix3 m, int c, Vector3 replace){
    for(int r=0; r<3; r++){
      m.set(r, c, replace.get(r));
    }
    return m;
  }

  public Vector3 cramer(Matrix3 m, Vector3 b){
    float det = m.determinant();
    Vector3 ret = new Vector3();
    for(int i=0; i<3; i++){
      ret.set(i, replaceCol(m.clone(), i, b).determinant() / det);
    }
    return ret;
  }

  @Override
  public void applyTransformation(Vector2 lastMousePos, Vector2 curMousePos, Matrix4 viewProjection) {
    // TODO#A3: Modify this.reference.rotationX, this.reference.rotationY, or this.reference.rotationZ
    //   given the mouse input.
    // Use this.axis to determine the axis of the transformation.
    // Note that the mouse positions are given in coordinates that are normalized to the range [-1, 1]
    //   for both X and Y. That is, the origin is the center of the screen, (-1,-1) is the bottom left
    //   corner of the screen, and (1, 1) is the top right corner of the screen.
    Vector3 axis = new Vector3(this.axis == ManipulatorAxis.X ? 1.0f : 0.0f, this.axis == ManipulatorAxis.Y ? 1.0f : 0.0f, this.axis == ManipulatorAxis.Z ? 1.0f : 0.0f);
    Vector3 otherAxis = new Vector3(this.axis == ManipulatorAxis.Y ? 1.0f : 0.0f, this.axis == ManipulatorAxis.Z ? 1.0f : 0.0f, this.axis == ManipulatorAxis.X ? 1.0f : 0.0f);
    Vector3 axisT = getReferencedTransform().mulDir(axis);
    Vector3 otherAxisT = getReferencedTransform().mulDir(otherAxis);

    Matrix4 viewToWorld = viewProjection.clone().invert();

    Vector2[] mView = new Vector2[2];
    mView[0] = lastMousePos;
    mView[1] = curMousePos;

    Vector3[] mWorld = new Vector3[2];
    Vector3[] mDir = new Vector3[2];
    for(int i=0; i<2; i++){
      mWorld[i] = viewToWorld.mulPos(new Vector3(mView[i].x, mView[i].y, -1.0f));
      mDir[i] = viewToWorld.mulPos(new Vector3(mView[i].x, mView[i].y, -0.8f)).sub(mWorld[i]);
    }
    Vector3 manipOrig = getReferencedTransform().mulPos(new Vector3());
    Vector3 e1 = axisT.clone().cross(otherAxisT);

    if(Math.abs(mDir[0].dot(axisT)) < 0.001 || Math.abs(mDir[1].dot(axisT)) < 0.001){
      return;
    }
    Vector3 e2 = axisT.clone().cross(e1);

    Vector3[] sol = new Vector3[2];
    for(int i=0; i<2; i++){
      sol[i] = cramer(
          new Matrix3(
            e1.x, e2.x, -mDir[i].x,
            e1.y, e2.y, -mDir[i].y,
            e1.z, e2.z, -mDir[i].z),
          mWorld[i].clone().sub(manipOrig));
    }

    Vector3[] sol2 = new Vector3[2];
    for(int i=0; i<2; i++){
      sol2[i] = mWorld[i].add(mDir[i].clone().mul(sol[i].z));
    }

    float sign = (int)Math.signum(sol2[0].clone().cross(sol2[1]).dot(axisT));
    float ang = sign * sol2[0].angle(sol2[1]);


    switch (this.axis) {
      case X:
        this.reference.rotationX.mulAfter(Matrix4.createRotationX(ang));
        break;
      case Y:
        this.reference.rotationY.mulAfter(Matrix4.createRotationY(ang));
        break;
      case Z:
        this.reference.rotationZ.mulAfter(Matrix4.createRotationZ(ang));
        break;
    }

  }

  @Override
  protected String meshPath () {
    return "data/meshes/Rotate.obj";
  }
}
