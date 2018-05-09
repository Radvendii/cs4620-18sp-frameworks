package ray2.integrator;
import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;
import ray2.IntersectionRecord;
import ray2.Ray;
import ray2.RayTracer;
import ray2.Scene;
import ray2.light.Environment;
import ray2.light.Light;
import ray2.light.LightSamplingRecord;
import ray2.light.PointLight;
import ray2.material.BSDF;
import ray2.material.BSDFSamplingRecord;
import ray2.surface.Surface;

/**
 * An Integrator that works by multiple importance sampling both light sources and BSDF using balance heuristic 
 * (See [Veach & Guibas 1995]).  
 * 
 * @author mx
 */

//A7 SOULTION START
public class MISIntegrator extends Integrator {
	
	/* 
	 * The illumination algorithm is:
	 * 
	 *   0. light source emission:
	 *      if the surface is a light source:
	 *        add the source's radiance
	 *        
	 *   1. light sources:
	 *      for each light in the scene
	 *        choose a point on the light   
	 *        do a shadow test
	 *        evaluate the BSDF
	 *        if the light is point light
	 *        	calculate its contribution as in LightSamplingIntegrator
	 *        else
	 *          a) sample from light:
	 *        		convert the probability of current light generating this direction to solid angle domain
	 *        		calculate the probability BSDF generates this direction
	 *        		compute the estimate of this light's contribution
	 *          		as (source radiance) * bsdf * (cos theta) / (lightpdf + bsdfpdf), and add it
	 *          b) sample from BSDF:
	 *          		generate a sample from BSDF 
	 *          		if this sample is not discrete
	 *          			determine source radiance (it is zero if the bsdf ray does not hit this particular source)
	 *          			calculate the probability that the current light generates this direction
	 *          			convert the probability to solid angle domain
	 *          			compute the estimate of this light's contribution
	 *          			as (source radiance) * bsdf * (cos theta) / (lightpdf + bsdfpdf), and add it     		       
	 *          
	 *   2. environment:
	 *   	a) sample from environment:
	 *      		choose a direction from the environment
	 *      		do a shadow test
	 *      		evaluate the BSDF
	 *      		calculate the probability that BSDF sample this direction
	 *      		compute the estimate of the environment's contribution
	 *        	as (env radiance) * brdf * (cos theta) / (envpdf + bsdfpdf), and add it
	 *      b) sample from BSDF:
	 *      		generate a sample from BSDF
	 *      		if this sample is not discrete
	 *      			determine env radiance by checking for shadow, then evaluating environment
	 *      			calculate the probability that the environment generates this direction
	 *      			compute the estimate of this light's contribution
	 *          		as (env radiance) * bsdf * (cos theta) / (envpdf + bsdfpdf), and add it     		
	 *        
	 *   3. mirror reflections and refractions:
	 *      choose a direction from the BSDF, continuing only if it is discrete
	 *      trace a recursive ray
	 *      add the recursive radiance weighted by (cos theta) * (bsdf value) / (bsdfpdf)
	 *   
	 *   Since we are looping over all light sources, we are implicitly breaking up the problem into a sum
	 *   of subproblems, one for each light.  The incident radiance for each subproblem includes only the 
	 *   radiance due to one source.
	 *   
	 *   The Light class returns a pdf in terms of surface area on the source, which was exactly right for
	 *   LightSamplingIntegrator, but in order to perform MIS between light source and BSDF samples, they 
	 *   need to be samples from the same domain.  This means we need to convert the surface-area probability
	 *   into a solid-angle probability before we can use the two probabilities together in computing 
	 *   estimates of the integral.
	 *   
	 *   
	 * @see ray2.integrator.Integrator#shade(egl.math.Colord, ray2.Scene, ray2.Ray, ray2.IntersectionRecord, int)
	 */

	@Override
	public void shade(Colord outRadiance, Scene scene, Ray ray, IntersectionRecord iRec, int depth) {
		 // TODO#A7
		
		
	}

	/**
	 * A utility method to check if there is any surface between the given intersection
	 * point and the given light. shadowRay is set to point from the intersection point
	 * towards the light.
	 * 
	 * @param scene The scene in which the surface exists.
	 * @param light A light in the scene.
	 * @param iRec The intersection point on a surface.
	 * @param shadowRay A ray that is set to point from the intersection point towards
	 * the given light.
	 * @return true if there is any surface between the intersection point and the light;
	 * false otherwise.
	 */
	protected boolean isShadowed(Scene scene, LightSamplingRecord lRec, IntersectionRecord iRec, Ray shadowRay) {		
		// Setup the shadow ray to start at surface and end at light
		shadowRay.origin.set(iRec.location);
		shadowRay.direction.set(lRec.direction);
		
		// Set the ray to end at the light
		shadowRay.direction.normalize();
		shadowRay.makeOffsetSegment(lRec.distance);
		
		return scene.getAnyIntersection(shadowRay);
	}

}
