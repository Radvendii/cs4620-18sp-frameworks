package ray1.shader;

import ray1.IntersectionRecord;
import ray1.Light;
import ray1.Ray;
import ray1.Scene;

import java.util.ArrayList;
import java.util.List;

import egl.math.Color;
import egl.math.Colorf;
import egl.math.Vector3;
import egl.math.Vector3d;

/**
 * A Lambertian material scatters light equally in all directions. BRDF value is
 * a constant
 *
 * @author ags, zz
 */
public class Lambertian extends Shader {

	/** The color of the surface. */
	protected final Colorf diffuseColor = new Colorf(Color.White);
	public void setDiffuseColor(Colorf inDiffuseColor) { diffuseColor.set(inDiffuseColor); }
	public Colorf getDiffuseColor() {return new Colorf(diffuseColor);}

	public Lambertian() { }
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Lambertian: " + diffuseColor;
	}

	/**
	 * Evaluate the intensity for a given intersection using the Lambert shading model.
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
		// 4) Compute the color of the point using the Lambert shading model. Add this value
		//    to the output.
		List<Light> lights = scene.getLights();
		Vector3 diffuse = diffuseColor;
		Vector3 ref = diffuse.clone().div((float)Math.PI);
		Vector3d norm = record.normal;
		outIntensity.setZero();
		for (int i=0; i < lights.size(); i++) {
			Vector3d pos = new Vector3d(lights.get(i).position);
			Vector3d dist = (pos.clone().sub(record.location));
			Ray rayshad = new Ray(pos, dist);
			boolean shadow = isShadowed(scene, lights.get(i), record, rayshad);
			if (shadow == false) {
				Vector3 intensity = lights.get(i).intensity;
				double r2 = dist.lenSq();
				Vector3 div1 = intensity.clone().div((float)r2);
				double dot = norm.dot(dist.normalize());
				if(dot < 0){
					dot = 0.0;
				}
				Vector3 temp1 = div1.clone().mul(ref);
				Vector3 temp = temp1.clone().mul((float)dot);
				outIntensity.add(temp);
				System.out.println(outIntensity);
				
			}
		}
	}

}
