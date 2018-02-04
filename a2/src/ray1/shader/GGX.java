package ray1.shader;

import egl.math.Vector3;

/**
 * Evaluate microfacet BRDF function with GGX distribution
 * @author zechen
 */
public class GGX extends BRDF 
{

	public String toString() {    
		return "GGX microfacet " + super.toString();
	}

	/**
	 * Evaluate the BRDF function value in microfacet model with GGX distribution
	 *
	 * @param IncomingVec Direction vector of the incoming ray.
	 * @param OutgoingVec Direction vector of the outgoing ray.
	 * @param SurfaceNormal Normal vector of the surface at the shaded point.
	 * @return evaluated BRDF function value
	 */
	public float EvalBRDF(Vector3 IncomingVec, Vector3 OutgoingVec, Vector3 SurfaceNormal)
	{
		// TODO#A2: (Extra credit) Evaluate the BRDF function of microfacet-based model with GGX distribution
		// Walter, Bruce, et al. 
		// "Microfacet models for refraction through rough surfaces." 
		// Proceedings of the 18th Eurographics conference on Rendering Techniques. Eurographics Association, 2007.
		
		return 0.0f;
	}
	
}
