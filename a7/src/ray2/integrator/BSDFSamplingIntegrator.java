package ray2.integrator;
import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.Scene;
import ray2.material.BSDF;
import ray2.material.BSDFSamplingRecord;
import ray2.surface.Surface;
import ray2.RayTracer;

/**
 * An Integrator that works by sampling light sources.  It accounts for light that illuminates all surfaces
 * directly from point or area sources, and from the environment.  It also includes recursive reflections
 * for polished surfaces (Glass and Glazed), but not for other surfaces.
 *
 * @author srm
 */
public class BSDFSamplingIntegrator extends Integrator {
	
	/* 
	 * The illumination algorithm is:
	 * 
	 *   0. light source emission:
	 *      if the surface is a light source:
	 *        add the source's radiance
	 *   1. reflected radiance:
	 *      generate a sample from the BSDF
	 *      trace a ray in that direction
	 *      if you hit nothing:
	 *        look up incident radiance from the environment
	 *      if you hit a surface:
	 *        for discrete directions, shade the ray recursively to get incident radiance
	 *        for non-discrete, incident radiance is source radiance if you hit a source (else 0)
	 *      compute the estimate for reflected radiance as incident radiance * brdf * cos theta / pdf
	 *   2. point light source:
	 *      for each point light in the scene:
	 *        compute the light direction and distance
	 *        evaluate the BRDF
	 *        add a contribution to the reflected radiance due to that source
	 * 
	 * For this integrator, step 1 automatically includes light from all sources (area sources and
	 * the environment) but step 2 is needed because point lights can't be hit by rays.
	 * 
	 * In step 1, by making the recursive call only for directions chosen discretely (that is, 
	 * directions belonging to perfectly sharp reflection and refraction components) we are leaving 
	 * out diffuse and glossy interreflections.
	 * 
	 * @see ray2.integrator.Integrator#shade(egl.math.Colord, ray2.Scene, ray2.Ray, ray2.IntersectionRecord, int)
	 */
	
	@Override
	public void shade(Colord outRadiance, Scene scene, Ray ray, IntersectionRecord iRec, int depth) {
      // TODO#A7: Calculate outRadiance at current shading point
      // You need to add contribution from source emission if the current surface has a light source,
      // generate a sample from the BSDF,
      // look up lighting in that direction and get incident radiance.
      // Before you calculate the reflected radiance, you need to check whether the probability value
      // from bsdf sample is 0.
		
		if(iRec.surface.getLight() != null) {
			// If we're looking at a light, use its radiance
			Colord outR = new Colord();
			iRec.surface.getLight().eval(ray, outR);
			outRadiance.add(outR);
		}

		// Calculate a sample ray from the surface.
		BSDFSamplingRecord sRec = new BSDFSamplingRecord();
		sRec.dir1 = ray.direction.clone().negate();
		sRec.normal = iRec.normal;
		Colord f_r = new Colord();
		double prob = iRec.surface.getBSDF().sample(sRec, new Vector2d(Math.random(), Math.random()), f_r);

		// Recursively trace the ray
		Ray bounceRay = new Ray(iRec.location, sRec.dir2);
		bounceRay.makeOffsetRay();
		Colord outR = new Colord();
		boolean hitLight = RayTracer.shadeRayExtra(outR, scene, bounceRay, depth + 1);
		if((sRec.isDiscrete || hitLight) && prob > 1e-6) {
			// If the sample was discrete, or if the next iteration hits a light, color based on the radiance of the recursive call
			outRadiance.add(outR.clone().mul(f_r).mul(Math.abs(iRec.normal.clone().dot(bounceRay.direction))).div(prob));
		}
			
		// Reuse other code to do point light shading
		PointLightIntegrator pointLights = new PointLightIntegrator();
		pointLights.shade(outRadiance, scene, ray, iRec, depth);

	}

}
