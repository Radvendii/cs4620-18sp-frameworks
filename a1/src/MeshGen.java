import math.Vector2;
import math.Vector3;
import meshgen.OBJFace;
import meshgen.OBJMesh;
import java.io.IOException;
class MeshGen {  
  public static void main(String args[]) throws IOException {
    Args pArgs = new Args(args);
    
    switch(Args.g){
      case CYLINDER:
        cylinder(pArgs.n).writeOBJ(pArgs.o);
        break;
      case SPHERE:
      default:
        System.exit(-1);
    }
  }

  public static OBJMesh cylinder(int n){
    //vertices will be 2x(n+1) array, denoting the point in the center followed by the circle, top face then bottom face
    //uvs      will be 2x(nx2+2) array, denoting the same, 
    //         but the last coordinate denotes whether the uv is for the side face or the top face. 
    //         The +2 is for the edge condition of the side uv, and the center vertex uvs
    //normals  will be n+2 array, denoting the same, but the second dimension is redundant except at the centers of the circles
    OBJMesh cylinder = new OBJMesh();
    for(int f=0;f<2;f++){ //top Face or bottom Face
      cylinder.positions.add(new Vector3(0, 1-f*2, 0)); //center point
      cylinder.normals.add(new Vector3(0, 1-f*2, 0)); //center point
      cylinder.uvs.add(new Vector2(0.75f - f*0.5f, 0.75f));
      for(int i=0;i<n;i++){
        double rads = (Math.PI / 2) + (i * 2 * Math.PI / n);
        float x = (float)Math.cos(rads);
        float z = -(float)Math.sin(rads);
        cylinder.positions.add(new Vector3(x, 1-f*2, z));
        cylinder.uvs.add(new Vector2((float) i / n, (1-f)*0.5f));
        cylinder.uvs.add(new Vector2(x / 4 + 0.75f-f*0.5f, (f*2-1)*z / 4 + 0.75f));
        if(f == 0){ // no need to repeat
          cylinder.normals.add(new Vector3(x, 0, z));
        }

        OBJFace horz = new OBJFace(3, true, true);
        horz.positions[f*2]     = (n+1)*f;
        horz.positions[1]       = (n+1)*f + i                      +1;
        horz.positions[(1-f)*2] = (n+1)*f + ((i+1) % n)            +1;
        horz.uvs[f*2]           = (n*2+2)*f;
        horz.uvs[1]             = (n*2+2)*f + 2*i           + 1    +1;
        horz.uvs[(1-f)*2]       = (n*2+2)*f + 2*((i+1) % n) + 1    +1;
        for(int j=0;j<3;j++){
          horz.normals[j]       = (n+1)*f;
        }

        OBJFace side = new OBJFace(3, true, true);
        side.positions[0]       = (n+1)*f       +   ((i+f)   % n)  +1;
        side.positions[1]       = (n+1)*(1-f)   +   ((i+f)   % n)  +1;
        side.positions[2]       = (n+1)*f       +   ((i+1-f) % n)  +1;
        side.uvs[0]             = (n*2+2)*f     + 2*(i+f)          +1;
        side.uvs[1]             = (n*2+2)*(1-f) + 2*(i+f)          +1;
        side.uvs[2]             = (n*2+2)*f     + 2*(i+1-f)        +1;
        side.normals[0]         = (i+f)   % n                      +1;
        side.normals[1]         = (i+f)   % n                      +1;
        side.normals[2]         = (i+1-f) % n                      +1; 

        cylinder.faces.add(horz);
        cylinder.faces.add(side);
      }
      cylinder.uvs.add(new Vector2(0, (1-f)*0.5f));
    }
    return cylinder;
  }
}

// class to process and store the command line arguments.
// modified from a post on piazza
class Args {
  public static enum MeshType { SPHERE, CYLINDER };
  public static MeshType g;   // -g <sphere|cylinder>
  public static int n;    // [-n <divisionsU>]
  public static int m;    // [-n <divisionsV>]
  public static String o; // -o <outfile.obj>
  public static String i; // -i <infile.obj>

  public Args(String[] args) {
    //when undefined
    g = null;
    o = null;
    i = null;
    //default values
    n = 32;
    m = 16;
    try {
      for(int j=0; j < args.length; j++){
        switch(args[j]) {
          case "-i": i = args[++j];
                     break;
          case "-o": o = args[++j];
                     break;
          case "-n": n = Integer.parseInt(args[++j]);
                     break;
          case "-m": m = Integer.parseInt(args[++j]);
                     break;
          case "-g":
                     switch(args[++j]){
                       case "sphere":
                         g = MeshType.SPHERE;
                         break;
                       case "cylinder":
                         g = MeshType.CYLINDER;
                         break;
                       default:
                         throw new Exception("unrecognized argument");
                     }
                     break;
          default:
                     throw new Exception("unrecognized argument");
        }
      }
      if( o == null // -o must be specified for either invocation
          || (g == null && i == null) || (g != null && i != null) // -g and -i are separate invocations and cannot both be specified
          || n < 2 || m < 2 ){ // we are promised n and m will be at least 2
        // System.out.println(g);
        // System.out.println(n);
        // System.out.println(m);
        // System.out.println(o);
        // System.out.println(i);
        throw new Exception("invalid arguments");
          }
      //Add tests for correct usage and throw exception if not
    } catch(Throwable e) {
      System.out.println("Usage:");
      System.out.println("(1) java MeshGen -g <sphere|cylinder> [-n <divisionsU>] [-m <divisionsV>] -o <outfile.obj>");
      System.out.println("(2) java MeshGen -i <infile.obj> -o <outfile.obj>");
      System.exit(-1);
    }
  }
}
