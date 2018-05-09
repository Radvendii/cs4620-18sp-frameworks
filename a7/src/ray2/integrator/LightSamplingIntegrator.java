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
import ray2.material.BSDF;
import ray2.material.BSDFSamplingRecord;
import ray2.surface.Surface;

/**
 * An Integrator that works by sampling light sources. It accounts for light
 * that illuminates all surfaces directly from point or area sources, and from
 * the environment. It also includes recursive reflections for polished surfaces
 * (Glass and Glazed), but not for other surfaces.
 * 
 * @author srm
 */
public class LightSamplingIntegrator extends Integrator {
	/*
	 * The illumination algorithm is:
	 * 
	 * 0. light source emission: if the surface is a light source: add the source's
	 * radiance 
	 * 1. light sources: for each light in the scene choose a point on the
	 * light evaluate the BRDF do a shadow test compute the estimate of this light's
	 * contribution as (source radiance) * brdf * attenuation * (cos theta) / pdf,
	 * and add it 
	 * 2. environment: choose a direction from the environment evaluate
	 * the BRDF do a shadow test compute the estimate of the environment's
	 * contribution as (env radiance) * brdf * (cos theta) / pdf, and add it 
	 * 3.
	 * mirror reflections and refractions: choose a direction from the BSDF,
	 * continuing only if it is discrete trace a recursive ray add the recursive
	 * radiance weighted by (cos theta) * (brdf value) / (probability)
	 * 
	 * Step 3 is violating the idea of light source sampling a bit, but it is needed
	 * because it's impossible to choose a light source point exactly in the
	 * reflection or refraction direction, and we do like to be able to see the
	 * reflections. By making the recursive call only for directions chosen
	 * discretely (that is, directions belonging to perfectly sharp reflection and
	 * refraction components) we are leaving out diffuse and glossy
	 * interreflections.
	 * 
	 * In Step 1 note that the attenuation includes the inverse square law and also
	 * the cosine at the source's end of the ray that is required by the
	 * illumination integral for an area source. This is taken care of by the Light
	 * subclasses: Point light sets its attenuation to 1 / r^2 whereas
	 * RectangleLight sets the attenuation to (cos theta_source) / r^2.
	 * 
	 * @see ray2.integrator.Integrator#shade(egl.math.Colord, ray2.Scene, ray2.Ray,
	 * ray2.IntersectionRecord, int)
	 */

	@Override
	public void shade(Colord outRadiance, Scene scene, Ray ray, IntersectionRecord iRec, int depth) {
		// TODO#A7: Calculate outRadiance at current shading point.
		// You need to add contribution from each light
		// add contribution from environment light if there is any
		// add mirror reflection and refraction
		Surface surface = iRec.surface;
		Vector3d normal = iRec.normal.clone().normalize();
		Vector3d v = ray.direction.clone().negate().normalize();
		Colord c = new Colord();
		LightSamplingRecord record = new LightSamplingRecord();
		Colord I = new Colord();

		Vector2d seed = new Vector2d(Math.random(), Math.random());
		Vector2d seed1 = new Vector2d(Math.random(), Math.random());

		Colord cout1 = new Colord();
		Colord cout2 = new Colord();
		Colord cout3 = new Colord();
		
		// If surface has a light, take that and evaluate the intensity
		Colord out1 = new Colord();
		if (surface.getLight() != null) {
			surface.getLight().eval(ray, out1);
		}

		BSDF object = surface.getBSDF();
		Colord color = new Colord();
		//Take the set of lights that aren't shadowed from the List of lights from the surface
		for (Light light : scene.getLights()) {
			light.sample(record, iRec.location);
			Ray shadowray = new Ray(iRec.location, record.direction);
			if (isShadowed(scene, record, iRec, shadowray) == false) {
				Vector3d l = record.direction.normalize();
				double r = record.attenuation;

				object.eval(l, v, normal, c);
				light.eval(shadowray, I);
				double dot = l.dot(normal);
				if (dot < 0) {
					continue;
				}
				cout1.set(I.clone().mul(c).mul(dot).mul(r).div(record.probability));
				color.add(cout1);
			}
		}
		
		//Take the environment light and evaluate the one sample
		Vector3d outdir = new Vector3d();
		Colord out = new Colord();
		Colord c1 = new Colord();
		
		if (scene.getEnvironment() != null) {
			double prob = scene.getEnvironment().sample(seed1, outdir, out);
			
			Ray shadow = new Ray(iRec.location, outdir);
			shadow.makeOffsetRay();
			shadow.makeOffsetSegment(Double.POSITIVE_INFINITY);
			
			if (!scene.getAnyIntersection(shadow)) {
				object.eval(outdir, v, normal, c1);
				double angle = Math.abs(outdir.dot(normal));
				cout2.set(out.mul(c1).mul(angle).div(prob));
			}
		}

		//Evaluate the reflection/refraction
		Colord c2 = new Colord();
		BSDFSamplingRecord sRec = new BSDFSamplingRecord();
		sRec.dir1.set(v);
		sRec.normal.set(iRec.normal);
		Colord outValue = new Colord();
		double probability = object.sample(sRec, seed, outValue);
		
		double theta = Math.abs(sRec.dir2.normalize().dot(sRec.normal));
		
		
		if (theta >= 0) {
			if (sRec.isDiscrete) {
				Ray newray = new Ray(iRec.location, sRec.dir2);
				newray.makeOffsetRay();
				newray.makeOffsetSegment(Double.POSITIVE_INFINITY);
				
				RayTracer.shadeRay(c2, scene, newray, depth + 1);
				
				Vector3d outvalue = outValue.clone().mul(c2);
				cout3.set(outvalue.mul(theta).div(probability));
			}
		}
		
		//Add all the components
		outRadiance.add(out1).add(color).add(cout2).add(cout3);
	}

	/**
	 * A utility method to check if there is any surface between the given
	 * intersection point and the given light. shadowRay is set to point from the
	 * intersection point towards the light.
	 * 
	 * @param scene
	 *            The scene in which the surface exists.
	 * @param light
	 *            A light in the scene.
	 * @param iRec
	 *            The intersection point on a surface.
	 * @param shadowRay
	 *            A ray that is set to point from the intersection point towards the
	 *            given light.
	 * @return true if there is any surface between the intersection point and the
	 *         light; false otherwise.
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
