package ray1.surface;

import ray1.IntersectionRecord;
import ray1.Ray;
import egl.math.Vector3;
import egl.math.Vector3d;
import egl.math.Vector2d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

  /** The center of the sphere. */
  protected final Vector3 center = new Vector3();
  public void setCenter(Vector3 center) { this.center.set(center); }

  /** The radius of the sphere. */
  protected float radius = 1.0f;
  public void setRadius(float radius) { this.radius = radius; }

  protected final double M_2PI = 2 * Math.PI;

  public Sphere() { }

  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param ray the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
    // TODO#A2: fill in this function.
    Vector3d oMinC = rayIn.origin.clone().sub(center);
    // using the quadratic equation x = (-p +- sqrt(p^2 - ac)) / a, where p = b/2
    // in this case, a = 1, so it can be ignored
    // center = 0,5,0
    // radius = 2
    // origin = 0,5,-3
    // direction = 0,0,1
    // intersection should be at (0,5,-2)
    double p = rayIn.direction.dot(oMinC);
    System.out.print("p (-3): ");
    System.out.println(p);
    double c = oMinC.lenSq() - Math.pow(radius, 2);
    System.out.print("c (5): ");
    System.out.println(c);
    double discr = Math.pow(p, 2) - c;
    System.out.print("discr (4): ");
    System.out.println(discr);
    if(discr<0){
      return false;
    }
    double t1 = -p + Math.sqrt(discr);
    System.out.print("t1 (5): ");
    System.out.println(t1);
    double t2 = -p - Math.sqrt(discr);
    System.out.print("t2 (1): ");
    System.out.println(t2);
    double t;
    // outRecord.t = -p - Math.sqrt(discr);
    if(t2<rayIn.start){
      if(t1<rayIn.start){
        return false;
      }
      t = t1;
    }
    else{
      t = t2; //Unless t2 is negative, it will be valid and closer to the ray origin than t1
    }
    if(t > rayIn.end){
      return false;
    }
    outRecord.t = t;
    outRecord.location.set(rayIn.origin.clone().add(rayIn.direction.clone().mul(outRecord.t)));
    outRecord.normal.set(outRecord.location.clone().sub(center).normalize());
    double v = outRecord.normal.angle(new Vector3d(0,-1,0)) / Math.PI;
    double u;
    if(v < 0.001 || v > 0.999){
      u = 0;
    }
    else{
      u = outRecord.normal.clone().sub(new Vector3d(0, outRecord.normal.y, 0)).angle(new Vector3d(0,0,-1)) / M_2PI + (outRecord.normal.x < 0 ? 0.5 : 0);
    }
    outRecord.texCoords.set(new Vector2d(u, v));
    outRecord.surface = this;
    return true;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "sphere " + center + " " + radius + " " + shader + " end";
  }

}
