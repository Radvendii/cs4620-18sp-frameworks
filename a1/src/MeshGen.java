import math.Vector2;
import math.Vector3;
import meshgen.OBJFace;
import meshgen.OBJMesh;
import java.io.IOException;
class MeshGen {  
  public static void main(String args[]) throws IOException {
    // Args pArgs = new Args(args);
    OBJMesh mesh = new OBJMesh();
    mesh.positions.add(new Vector3(1,0,0));
    mesh.positions.add(new Vector3(0,1,0));
    mesh.positions.add(new Vector3(0,0,1));
    OBJFace face = new OBJFace(3, false, false);
    face.positions[0] = 0;
    face.positions[1] = 1;
    face.positions[2] = 2;
    mesh.faces.add(face);
    mesh.writeOBJ("foo.obj");
  }
}

// class to process and store the command line arguments.
// modified from a post on piazza
class Args {
  public enum MeshType { SPHERE, CYLINDER };
  MeshType g;   // -g <sphere|cylinder>
  int n;    // [-n <divisionsU>]
  int m;    // [-n <divisionsV>]
  String o; // -o <outfile.obj>
  String i; // -i <infile.obj>

  Args(String[] args) {
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
        System.out.println(g);
        System.out.println(n);
        System.out.println(m);
        System.out.println(o);
        System.out.println(i);
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
