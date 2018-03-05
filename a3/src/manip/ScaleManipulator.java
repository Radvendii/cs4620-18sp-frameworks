package manip;

import egl.math.*;
import gl.RenderObject;

public class ScaleManipulator extends Manipulator {

  public ScaleManipulator (ManipulatorAxis axis) {
    super();
    this.axis = axis;
  }

  public ScaleManipulator (RenderObject reference, ManipulatorAxis axis) {
    super(reference);
    this.axis = axis;
  }

  @Override
  protected Matrix4 getReferencedTransform () {
    if (this.reference == null) {
      throw new RuntimeException ("Manipulator has no controlled object!");
    }
    return new Matrix4().set(reference.scale)
      .mulAfter(reference.rotationZ)
      .mulAfter(reference.rotationY)
      .mulAfter(reference.rotationX)
      .mulAfter(reference.translation);
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
    // TODO#A3: Modify this.reference.scale given the mouse input.
    // Use this.axis to determine the axis of the transformation.
    // Note that the mouse positions are given in coordinates that are normalized to the range [-1, 1]
    //   for both X and Y. That is, the origin is the center of the screen, (-1,-1) is the bottom left
    //   corner of the screen, and (1, 1) is the top right corner of the screen.
    Vector3 axis = new Vector3(this.axis == ManipulatorAxis.X ? 1.0f : 0.0f, this.axis == ManipulatorAxis.Y ? 1.0f : 0.0f, this.axis == ManipulatorAxis.Z ? 1.0f : 0.0f);
    Vector3 axisT = getReferencedTransform().mulDir(axis);

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
    Vector3 toNear = viewToWorld.mulDir(new Vector3(0.0f,0.0f,1.0f)).normalize();
    Vector3 perp = toNear.clone().cross(axisT).normalize();

    Vector3[] sol = new Vector3[2];
    for(int i=0; i<2; i++){
      sol[i] = cramer(
          new Matrix3(
            axisT.x, perp.x, -mDir[i].x,
            axisT.y, perp.y, -mDir[i].y,
            axisT.z, perp.z, -mDir[i].z),
          mWorld[i].clone().sub(manipOrig));
    }

    float sc = sol[1].x / sol[0].x;

    this.reference.scale.mulAfter(Matrix4.createScale(axis.clone().mul(sc).add(new Vector3(1.0f)).sub(axis)));

  }

  @Override
  protected String meshPath () {
    return "data/meshes/Scale.obj";
  }

}
