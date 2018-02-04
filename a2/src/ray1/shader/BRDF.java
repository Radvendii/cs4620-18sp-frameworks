package ray1.shader;

import egl.math.Vector3;

/**
 * This interface specifies what is necessary for BRDF function object
 * @author zechen
 */
public abstract class BRDF {
	
	/**refractive index of material, used in Fresnel factor*/
	protected float nt;
	public void setNt(float nt) {this.nt = nt;}
	public float getNt() {return nt;}	
	
	/**
	 * The width parameter of the BRDF function of microfacet model
	 */
	protected float alpha;
	public void setAlpha(float t) { alpha = t; }
	public float getAlpha() { return alpha; }
	
	public String toString() {    
		return "BRDF with " + " alpha " + alpha + " nt " + nt + " end";
	}
	
	/**
	 * Evaluate the BRDF function value in microfacet model
	 *
	 * @param IncomingVec Direction vector of the incoming ray.
	 * @param OutgoingVec Direction vector of the outgoing ray.
	 * @param SurfaceNormal Normal vector of the surface at the shaded point.
	 */
	public abstract float EvalBRDF(Vector3 IncomingVec, Vector3 OutgoingVec, Vector3 SurfaceNormal);
	
	/**
	* Initialize method
	*/
	public void init() {
		// do nothing
	};

}