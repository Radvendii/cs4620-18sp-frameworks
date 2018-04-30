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

public class BSDFSamplingIntegrator extends Integrator {

	@Override
	public void shade(Colord outRadiance, Scene scene, Ray ray, IntersectionRecord iRec, int depth) {
      // TODO#A7: Calculate outRadiance at current shading point
      // You need to add contribution from source emission if the current surface has a light source,
      // generate a sample from the BSDF,
      // look up lighting in that direction and get incident radiance.
      // Before you calculate the reflected radiance, you need to check whether the probability value
      // from bsdf sample is 0.

	}

}
