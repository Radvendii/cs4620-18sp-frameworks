package ray1.shader;

import ray1.IntersectionRecord;
import ray1.Ray;
import ray1.Scene;
import ray1.Light;
import ray1.shader.BRDF;
import egl.math.Color;
import egl.math.Colorf;
import egl.math.Vector3d;

/**
 * Microfacet-based shader
 *
 * @author zechen
 */
public class Microfacet extends Shader {
	
	protected BRDF brdf = null;
	public void setBrdf(BRDF t) { brdf = t; }
	public BRDF getBrdf() { return brdf; }

	/** The color of the microfacet reflection. */
	protected final Colorf microfacetColor = new Colorf(Color.Black);
	public void setMicrofacetColor(Colorf microfacetColor) { this.microfacetColor.set(microfacetColor); }
	public Colorf getMicrofacetColor() {return new Colorf(microfacetColor);}
	
	/** The color of the diffuse reflection. */
	protected final Colorf diffuseColor = new Colorf(Color.Black);
	public void setDiffuseColor(Colorf diffuseColor) { this.diffuseColor.set(diffuseColor); }
	public Colorf getDiffuseColor() {return new Colorf(diffuseColor);}
	
	public Microfacet() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "Microfacet model, microfacet color " + microfacetColor + " diffuseColor " + diffuseColor + brdf.toString();
	}

	/**
	 * Evaluate the intensity for a given intersection using the Microfacet shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 */
	@Override
	public void shade(Colorf outIntensity, Scene scene, Ray ray, IntersectionRecord record) {
		// TODO#A2: Fill in this function.
		// 1) Loop through each light in the scene.
		// 2) If the intersection point is shadowed, skip the calculation for the light.
		//	  See Shader.java for a useful shadowing function.
		// 3) Compute the incoming direction by subtracting
		//    the intersection point from the light's position.
		// 4) Compute the color of the point using the microfacet shading model. 
		//	  EvalBRDF method of brdf object should be called to evaluate BRDF value at the shaded surface point.
		// 5) Add the computed color value to the output.
          outIntensity.setZero();
          Vector3d ret = new Vector3d();
          double alpha = brdf.getAlpha();
          double nt = brdf.getNt();
          for(Light light: scene.getLights()){
            Vector3d wi = new Vector3d(light.position).clone().sub(new Vector3d(record.location));
            double r = wi.len();
            wi.normalize();
            if(isShadowed(scene, light, record, new Ray(new Vector3d(light.position), wi))){
              continue;
            }
            Vector3d wo = ray.direction.clone();
            Vector3d n = record.normal;
            Vector3d h = wi.clone().add(wo).normalize();
            double c = Math.abs(wi.dot(h));
            double pre_g = nt * nt - 1 + c * c;
            double F;
            if(pre_g < 0){
              F = 1;
            }
            else{
              double g = Math.sqrt(pre_g);
              F = 1.0 / 2.0 * Math.pow(g - c, 2) / Math.pow(g + c, 2) * (1 + Math.pow(c*(g+c)-1, 2) / Math.pow(c*(g-c)+1, 2));
            }
            double theta = h.angle(n);
            double D = (h.dot(n) > 0 ? 1 : 0) / (Math.PI * alpha * alpha * Math.pow(Math.cos(theta), 4)) * Math.exp(-Math.pow(Math.tan(theta), 2) / (alpha * alpha));
            double ai = alpha * Math.tan(wi.angle(n));
            double ao = alpha * Math.tan(wo.angle(n));
            double G1i = (wi.dot(h) / wi.dot(n) > 0 ? 1 : 0) * (ai > 1.6 ? ((3.535*ai + 2.181*ai*ai) / (1 + 2.276*ai + 2.577*ai*ai)) : 1);
            double G1o = (wo.dot(h) / wo.dot(n) > 0 ? 1 : 0) * (ao > 1.6 ? ((3.535*ao + 2.181*ao*ao) / (1 + 2.276*ao + 2.577*ao*ao)) : 1);
            double G = G1i * G1o;
            double fr = F * G * D / (4 * Math.abs(wi.dot(n)) * Math.abs(wo.dot(n)));
            Vector3d Rd = new Vector3d(diffuseColor.r(), diffuseColor.g(), diffuseColor.b());
            Vector3d Rm = new Vector3d(microfacetColor.r(), microfacetColor.g(), microfacetColor.b());
            Vector3d IL = new Vector3d(light.intensity.r(), light.intensity.g(), light.intensity.b());
            ret.add(IL.clone().mul(1 / r * r).mul(Rd.clone().mul(1.0/Math.PI).add(Rm.mul(fr))).mul(Math.max(0, n.dot(wi))));
          }
          outIntensity.set((float)ret.x, (float)ret.y, (float)ret.z);
	}

}
