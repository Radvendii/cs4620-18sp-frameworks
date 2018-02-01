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
    OBJMesh cylinder = new OBJMesh();
    cylinder.positions.add(new Vector3(0,  1, 0));
    cylinder.positions.add(new Vector3(0, -1, 0));
    for(int i=0;i<n;i++){
      double rads = (-Math.PI / 2) + (i * 2 * Math.PI / n);
      float x = (float)Math.cos(rads);
      float z = (float)Math.sin(rads);
      cylinder.positions.add(new Vector3(x,  1, z));
      cylinder.positions.add(new Vector3(x, -1, z));
      OBJFace topFace = new OBJFace(3, false, false);
      topFace.positions[2] = 0;
      topFace.positions[1] = i*2+2;
      topFace.positions[0] = ((i*2+2) % (2*n))+2;
      cylinder.faces.add(topFace);
      OBJFace botFace = new OBJFace(3, false, false);
      botFace.positions[0] = 1;
      botFace.positions[1] = i*2+3;
      botFace.positions[2] = ((i*2+3) % (2*n))+2;
      cylinder.faces.add(botFace);
      OBJFace rightFace = new OBJFace(3, false, false);
      rightFace.positions[0] = i*2+2;
      rightFace.positions[1] = ((i*2+2) % (2*n))+2;
      rightFace.positions[2] = ((i*2+3) % (2*n))+2;
      cylinder.faces.add(rightFace);
      OBJFace leftFace = new OBJFace(3, false, false);
      leftFace.positions[2] = i*2+2;
      leftFace.positions[1] = i*2+3;
      leftFace.positions[0] = ((i*2+3) % (2*n))+2;
      cylinder.faces.add(leftFace);
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

  public static Args(String[] args) {
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
