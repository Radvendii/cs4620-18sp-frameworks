package ray1.surface;

import ray1.IntersectionRecord;
import ray1.Ray;
import egl.math.Matrix3;
import egl.math.Matrix3d;
import egl.math.Vector2;
import egl.math.Vector3;
import egl.math.Vector3d;
import ray1.shader.Shader;
import ray1.OBJFace;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
  /** The normal vector of this triangle, if vertex normals are not specified */
  Vector3 norm;
  
  /** The mesh that contains this triangle */
  Mesh owner;
  
  /** The face that contains this triangle */
  OBJFace face = null;
  
  double a, b, c, d, e, f, g, h, i;
  Vector3 v0, v1, v2;
  public Triangle(Mesh owner, OBJFace face, Shader shader) {
    this.owner = owner;
    this.face = face;

    v0 = owner.getMesh().getPosition(face,0);
    v1 = owner.getMesh().getPosition(face,1);
    v2 = owner.getMesh().getPosition(face,2);
    
  //  if (!face.hasNormals()) {
      Vector3 e0 = new Vector3(), e1 = new Vector3();
      e0.set(v1).sub(v0);
      e1.set(v2).sub(v0);
      norm = new Vector3();
      norm.set(e0).cross(e1).normalize();
  //  }

    a = v0.x-v1.x;
    b = v0.y-v1.y;
    c = v0.z-v1.z;
    
    d = v0.x-v2.x;
    e = v0.y-v2.y;
    f = v0.z-v2.z;

    
    this.setShader(shader);
  }

  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param rayIn the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
    // TODO#A2: fill in this function.

    Vector3d origin = rayIn.origin;
    Vector3d dir = rayIn.direction;
    if(dir.dot(norm) == 0) {
    	return false;
    }

    double g = v0.x-origin.x;
    double h = v0.y-origin.y;
    double i = v0.z-origin.z;
    
    Matrix3d A = new Matrix3d(a, d, dir.x, b, e, dir.y, c, f, dir.z);
    double det = A.determinant();
    if(det==0) {
    	return false;
    }
    Matrix3d tmat = new Matrix3d(a, d, g, b, e, h, c, f, i);
    double t = (tmat.determinant())/det;
    if(t < rayIn.start || t > rayIn.end)  {
    	return false;
    }   
    Vector3d d1 = new Vector3d();
    d1.addMultiple(t, dir);
    Vector3d P = origin.clone().add(d1.clone());
        
    Matrix3d betamat = new Matrix3d(g,d,dir.x, h, e, dir.y, i, f, dir.z);
    double beta = (betamat.determinant())/det;
    
    Matrix3d gammamat = new Matrix3d(a, g, dir.x, b, h, dir.y, c, i, dir.z);
    double gamma = (gammamat.determinant())/det;
    
    double alpha = 1-beta-gamma;
    
    if(beta<0 || gamma <0 || alpha < 0) {
    	return false;
    }
    
    Vector3 curnorm = new Vector3();
    if(this.face.hasNormals()==true) {
        Vector3 tn0, tn1, tn2;
        Vector3 n0 = new Vector3();
        Vector3 n1 = new Vector3();
        Vector3 n2 = new Vector3();
        
	    tn0 = owner.getMesh().getNormal(face,0);
		tn1 = owner.getMesh().getNormal(face,1);
		tn2 = owner.getMesh().getNormal(face,2);
	 
		n0.addMultiple((float)alpha, tn0);
		n1.addMultiple((float)beta, tn1);
		n2.addMultiple((float)gamma, tn2);
		curnorm.add(n0).add(n1).add(n2);
		curnorm.normalize();
		System.out.println(curnorm);
    }
    else {
    	curnorm = norm;
    }
	
	Vector2 tex = new Vector2();
	if(this.face.hasUVs()==true) {
    Vector2 t0, t1, t2;
    t0 = owner.getMesh().getUV(face,0);
	t1 = owner.getMesh().getUV(face,1);
	t2 = owner.getMesh().getUV(face,2);
	
	t0.addMultiple((float)alpha, t0);
	t1.addMultiple((float)beta, t1);
	t2.addMultiple((float)gamma, t2);
	tex = tex.add(t0).add(t1).add(t2);
	}
	else {
		tex.addMultiple(0, tex);
	}
	
    IntersectionRecord rec = new IntersectionRecord();
    rec.location.set(P);
    rec.normal.set(curnorm);
    rec.texCoords.set(tex);
    rec.surface = this;
    rec.t = t;
    
    outRecord.set(rec);
    return true;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Triangle ";
  }
}