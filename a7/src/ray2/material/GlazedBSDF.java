package ray2.material;

import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * Clear coat over diffuse glazed BSDF
 *
 *@author mx
 */
public class GlazedBSDF extends BSDF {
	
	/** The index of refraction of this material. Used when calculating Fresnel factor. */
	protected double refractiveIndex = 1.5;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
	
	/**
	 * The underlying material beneath the glaze.
	 */
	protected BSDF substrate;
	public void setSubstrate(BSDF substrate) {
		this.substrate = substrate; 
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "glazed " + refractiveIndex + " " +substrate.toString();
	}
	
	public GlazedBSDF() { }
	
	public GlazedBSDF(double refractiveIndex) {
		this.refractiveIndex = refractiveIndex;
	}
	
	// Old implementation from Shader hierarchy, for reference
//	/**
//	 * Evaluate the intensity for a given intersection using the Glass shading model.
//	 *
//	 * @param outIntensity The color returned towards the source of the incoming ray.
//	 * @param scene The scene in which the surface exists.
//	 * @param ray The ray which intersected the surface.
//	 * @param record The intersection record of where the ray intersected the surface.
//	 * @param depth The recursion depth.
//	 */
//	@Override
//	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
//		// You may find it helpful to create helper methods if the code here gets too long.
//		
//		Colord color = new Colord();
//		Vector3d outgoing = new Vector3d();
//		outgoing.set(ray.origin).sub(record.location).normalize();
//		
//		double R = fresnel(record.normal, outgoing, refractiveIndex);
//		substrate.shade(color, scene, ray, record, depth);
//		color.mul(1-R);
//		
//		double cos_1 = outgoing.dot(record.normal);
//		if (cos_1 < 0 || R < 1e-9) {
//			outIntensity.set(color);
//			return; // No reflection. We're done!
//		}
//
//		Ray incomingSpecular = new Ray();
//		
//		incomingSpecular.makeOffsetRay();
//		incomingSpecular.origin.set(record.location);
//		incomingSpecular.direction.addMultiple(2*cos_1, record.normal)
//								  .sub(outgoing)
//								  .normalize();
//				
//		Colord recursiveColor = new Colord();
//		RayTracer.shadeRay(recursiveColor, scene, incomingSpecular, depth+1);
//		recursiveColor.mul(R);
//		color.add(recursiveColor);
//		
//		outIntensity.set(color);
//	}

	
	@Override
	public
	void eval(Vector3d dir1, Vector3d dir2, Vector3d normal, Colord outValue) {
		substrate.eval(dir1, dir2, normal, outValue);
	}
	
	static void cosineHemisphere(Vector2d seed, Vector3d outDir) {
		double r = Math.sqrt(seed.x);
		outDir.z = Math.sqrt(1 - seed.x);
		outDir.x = r * Math.cos(2 * Math.PI * seed.y);
		outDir.y = r * Math.sin(2 * Math.PI * seed.y);		
	}
	
	/** Construct a frame using a single vector, 
	 * used for transforming hemisphere direction from local to world*/
	static void basisFromW(Vector3d w, Vector3d outU, Vector3d outV) {
		if (Math.abs(w.x) <= Math.abs(w.y) && Math.abs(w.x) <= Math.abs(w.z))
			outV.set(1, 0, 0);
		else if (Math.abs(w.y) <= Math.abs(w.x) && Math.abs(w.y) <= Math.abs(w.z))
			outV.set(0, 1, 0);
		else
			outV.set(0, 0, 1);
		outU.set(outV).cross(w).normalize();
		outV.set(w).cross(outU).normalize();
	}
	
	@Override
	/**
	 * This glazed material is a clear coat over a diffuse material, 
	 * we choose between the specular reflection direction and the diffuse direction based on fresenel
	 * for specular reflection, the discrete probability is R
	 * for diffuse reflection, the probability is (1-R) * cos(theta) / pi   
	 * */
	public
	double sample(BSDFSamplingRecord sampleRecord, Vector2d seed, Colord outValue) {
		
		double prob = 0.0;
		Vector3d IncomingVec = sampleRecord.dir1;
		Vector3d normal = sampleRecord.normal;
		
		double cos_1 = IncomingVec.dot(normal);
		double R = fresnel(normal, IncomingVec, refractiveIndex);
		
		double xi = Math.random();
		if (xi <= R) {
			// Compute specular reflected ray direction
			sampleRecord.dir2.addMultiple(2*cos_1, normal).sub(IncomingVec).normalize();
			outValue.set(R/cos_1);
			prob = R;
			sampleRecord.isDiscrete = true;
		} else {
			// Randomly generate diffuse direction
			Vector3d outDirLocal = new Vector3d();
			cosineHemisphere(seed, outDirLocal);
			Vector3d u = new Vector3d();
			Vector3d v = new Vector3d();
			basisFromW(sampleRecord.normal, u, v);
			sampleRecord.dir2.set(sampleRecord.normal).mul(outDirLocal.z);
			sampleRecord.dir2.addMultiple(outDirLocal.x, u);
			sampleRecord.dir2.addMultiple(outDirLocal.y, v);
			
			outValue.set(R).div(cos_1);
			prob = R * cos_1 / Math.PI;
			sampleRecord.isDiscrete = false;
		}
		
		
		
		// Compute reflected ray direction
		sampleRecord.dir2.addMultiple(2*cos_1, normal).sub(IncomingVec).normalize();
		outValue.set(R).div(cos_1);
		prob = 1;
		
		sampleRecord.isDiscrete = true;

		return prob;
	}
	
	@Override
	public
	double pdf(Vector3d dir1, Vector3d dir2, Vector3d normal) {
		return 0.0;
	}
	
	@Override
	public Colord getDiffuseReflectance() {
		return substrate.getDiffuseReflectance();
	}
	
	

}
