package tropical.client.Render;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;

public class MeshManager {
    private static String meshLocation = "car.obj";
    public static void cycleMesh() {
        File f = new File("./");
        File[] files = f.listFiles((dir, name) -> name.endsWith(".obj"));
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(meshLocation)) {
                meshLocation = files[(i+1)%files.length].getName();
                return;
            }
        }
    }

    @Nullable
    public static Mesh getDefaultMesh() {
        try {
            return loadMeshFromFile(meshLocation);
        } catch (Exception e) {
            System.out.println("failed to load mesh: " + e.toString());
            for (var ele : e.getStackTrace()) {
                System.out.println(ele.toString());
            }

            return null;
        }
    }

    public static Mesh loadMeshFromFile(String path) throws Exception {
        ArrayList<Vec3> vertices = new ArrayList<>();
        ArrayList<ArrayList<Integer>> faces = new ArrayList<>();
        FileReader r = new FileReader(path);
        BufferedReader read = new BufferedReader(r);
        String line;
        while ((line = read.readLine()) != null) {
            readObjLine(line, vertices, faces); 
        }

        read.close();

        Vec3[] v = vertices.toArray(new Vec3[0]);

        ArrayList<int[]> fTemp = new ArrayList<>();
        for (ArrayList<Integer> face : faces) {
            fTemp.add(face.stream().mapToInt(i->i).toArray());
        }
        int[][] f = new int[fTemp.size()][];
        for (int i = 0; i < fTemp.size(); i++) {
            f[i] = fTemp.get(i);
        }

        return new Mesh(v, f);
    }    

    private static void readObjLine(String line, ArrayList<Vec3> vertices, ArrayList<ArrayList<Integer>> faces) throws Exception {
        if (line.startsWith("v ")) {
            String[] coordsStr = line.substring(2).split(" ");

            Double[] coords = new Double[3];
            int count = 0;
            for (String coordStr : coordsStr) {
                try {
                    coords[count] = Double.parseDouble(coordStr); 
                } catch (Exception e) {
                    continue;
                }

                count++;
                if (count == 3) break;
            }


            Vec3 vertex;
            vertex = new Vec3(
                coords[0],
                coords[1],
                coords[2]
            );

            vertices.add(vertex);
            return;
        } 

        if (line.startsWith("f ")) {
            String[] faceVerticies = line.substring(2).split(" ");
            ArrayList<Integer> fV = new ArrayList<>();
            for (String vertex : faceVerticies) {
                Integer vIndex = Integer.parseInt(vertex.split("/")[0]);
                if (vIndex < 0) {
                    fV.add(vertices.size() + vIndex + 1);
                } else {
                    fV.add(vIndex); 
                }
            }

            faces.add(fV);
            return;
        }
    }
}
