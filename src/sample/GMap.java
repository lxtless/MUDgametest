package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static java.lang.Math.random;

public class GMap {
    protected int ID;
    protected int ox,oy;     /*地图原点坐标*/
    protected int size = 100;
    protected Color[][] items;

    public GMap() {
        ID = 1;
        items = new Color[100][100];
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                items[i][j] = Color.color(random(),0.9,random(),0.5);
            }
        }
    }

    public void draw(GraphicsContext gc,int ox,int oy) {
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                gc.setLineWidth(0);
                gc.setFill(items[i][j]);
                gc.fillOval((i-ox)*5,(j-oy)*5,5,5);
            }
        }
    }
}
