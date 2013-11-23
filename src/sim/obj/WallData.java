package sim.obj;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import sim.Parameters;

/** 壁のデータを格納しているクラス */
public class WallData {
	public final int X; /** 左上端のx座標　*/
	public final int Y; /** 左上端のy座標 */
	public final int Width; /** 横の長さ */
	public final int Height; /** 縦の長さ */
	
	private final static int LEFT=0;  /** 四角形の左端 */
	private final static int RIGHT=1; /** 四角形の右端 */
	private final static int UP=2;    /** 四角形の上端 */
	private final static int DOWN=3;  /** 四角形の下端 */
	
	/** コンストラクタ */
	public WallData(int x,int y,int w,int h){
		X=x;
		Y=y;
		Width=w;
		Height=h;
	}
	
	/** エージェントが壁に衝突した場所を返す */
	public Point getCollisionPlace(Point p1,Point p2,double slope,double slice){
		return getCollisionPlace(p1,p2,slope,slice,0);
	}
	
	/** 壁に衝突した場所を返す */
	private Point getCollisionPlace(Point p1,Point p2,double slope,double slice,double R){
		int sign=(p2.x-p1.x)/Math.abs(p2.x-p1.x);
		if( !judgeCollision(p1,sign,sign*slope,R,Math.abs(p2.x-p1.x)) )return null;
		Point[] collisions = new Point[4];  //ぶつかった場合、壁に最初に当たる座標
		collisions[LEFT] = collisionX(slope,X,(int)Math.round(p1.x+R),p1);
		collisions[RIGHT] = collisionX(slope,X+Width,(int)Math.round(p1.x-R),p1);
		collisions[UP] = collisionY(slope,Y,(int)Math.round(p1.y+R),p1);
		collisions[DOWN] = collisionY(slope,Y+Height,(int)Math.round(p1.y-R),p1);
		Integer index =null;
		for(int i=0;i<collisions.length;i++){
			if(collisions[i]==null)continue;
			if(index==null)index=i;
			else{
				int min=Math.abs(collisions[index].x-p1.x)+Math.abs(collisions[index].y-p1.y);
				int now=Math.abs(collisions[i].x-p1.x)+Math.abs(collisions[i].y-p1.y);
				if(min>now)index=i;
			}
		}
		if(index==null)return null;
		return collisions[index];
		/*
		if(R==0) return collisions[index];
		else {
			Point P=decidePlace(index,collisions[index],slope,slice,R);
			System.out.println("collision:"+collisions[index]);
			System.out.println("stop:"+P);
			System.out.println("start");
			System.out.println(P);
			switch(index){
			case LEFT: System.out.println(X);
						break;
			case RIGHT: System.out.println(X+Width);
						break;
			case UP: System.out.println(Y);
					 break;
			case DOWN: System.out.println(Y+Height);
						break;
			default:break;
			}
			System.out.println("end");
			return P;
		}
		*/
	}
	
	/** 壁に接した時のagentの中心点を求める */
	private Point decidePlace(int index,Point place,double slope,double slice,double R){
		int error=3;
		double x,y,a,b,c;
		x=0; y=0;  //一応初期化
		if( index==LEFT || index==RIGHT ){
			a=Math.pow(slope, 2)+1;
			b=-2*place.x+2*slope*slice-2*slope*place.y;
			c=Math.pow(place.x,2)+Math.pow(slice,2)-2*slice*place.y+Math.pow(place.y,2)-Math.pow(R,2);
			double root=Math.sqrt(Math.pow(b,2)-4*a*c);
			int sign;
			if(index==LEFT)sign=-1;
			else sign=1;
			x=(-b+sign*root)/(2*a);
			y=slope*x+slice;
			int X=(int)Math.round(place.x+sign*R);
			int Y=(int)Math.round(y);
			return new Point(X,Y);
		}
		else if( index==UP || index==DOWN ){
			a = 1+1.0/Math.pow(slope, 2);
			b = -2.0*(place.y+slice/Math.pow(slope,2)+1.0*place.x/slope);
			c = Math.pow(1.0*place.y,2)+Math.pow(slice,2)/Math.pow(slope,2)+2.0*slice*place.x/slope+Math.pow(1.0*place.x,2)-Math.pow(R,2);
			double root=Math.sqrt(Math.pow(b,2)-4*a*c);
			int sign;
			if(index==UP)sign=-1;
			else sign=1;
			y=(-b+sign*root)/(2*a);
			x=(y-slice)/slope;
			int X = (int)Math.round(x);
			int Y = (int)Math.round(place.y+sign*R);
			return new Point(X,Y);
		}
		else return null;
	}
	
	
	/** 壁にぶつかったかどうかの判定 */
	private boolean judgeCollision(Point before,Point after,Point collision,double R){
		int distance = (int)Math.round( Math.sqrt( Math.pow(Math.abs(after.x-before.x),2) + Math.pow(Math.abs(after.y-before.y),2) ) );
		int distance_wall = (int)Math.round( Math.sqrt( Math.pow(Math.abs(collision.x-before.x),2) + Math.pow(Math.abs(collision.y-before.y),2) ) );
		return (distance_wall-R < distance); 
	}
	
	/** 壁にぶつかったかどうかの判定(より精度がいいはず) */
	private boolean judgeCollision(Point after,double R){
		boolean aboutX=( X<(int)Math.floor(after.x+R) && X+Width>(int)Math.ceil(after.x-R) );
		boolean aboutY=( Y<(int)Math.floor(after.y+R) && Y+Height>(int)Math.ceil(after.y-R) );
		return (aboutX && aboutY);
	}
	
	/** 壁にぶつかったかどうかの判定(精度は一番いいけど、時間がかかるかも) */
	private boolean judgeCollision(Point before,double dx,double dy,double R,int n){
		double x=before.x;
		double y=before.y;
		for(int i=0;i<n;i++){
			x+=dx;
			y+=dy;
			Point pt = new Point((int)Math.round(x),(int)Math.round(y));
			if(judgeCollision(pt,R))return true;
		}
		return false;
	}
	
	
	/** 壁の縦線にぶつかった場合 */
	private Point collisionX(double slope,double slice,int x,double R){
		int y = (int)Math.round(slope*x+slice);
		int r = (int)R;
		if( Y<y+r && y-r<Y+Height )return new Point(x,y);
		else return null;
	}
	
	/** 壁の縦線にぶつかった場合 */
	private Point collisionX(double slope,int x,int bx,Point p1){
		int dx = x-bx;
		int dy = (int)Math.round(slope*dx);
		return new Point(p1.x+dx,p1.y+dy);
	}
	
	/** 壁の横線にぶつかった場合 */
	private Point collisionY(double slope,double slice,int y,double R){
		int x = (int)Math.round(1.0*(y-slice)/slope);
		int r = (int)R;
		if( X<x+r && x-r<X+Width )return new Point(x,y);
		else return null;
	}
	
	/** 壁の横線にぶつかった場合 */
	private Point collisionY(double slope,int y,int by,Point p1){
		int dy = y-by;
		int dx = (int)Math.round(1.0*dy/slope);
		return new Point(p1.x+dx,p1.y+dy);
	}
	
	/** この壁の描画を行うメソッド */
	public void draw(Graphics g){
		g.setColor(Color.green);
		g.fillRect(X, Y, Width, Height);
		g.setColor(Color.black);
		g.drawRect(X, Y, Width, Height);
	}
	
	/** 移動前と移動後の座標から衝突地点を返すメソッド */
	public static Point getCollisionPoint(ArrayList<WallData> WallList, Point before,Point after,double R){
		Point place=null; //壁と衝突した時のエージェントの場所 
		for(WallData wall : WallList){
			Point elem = null;
			if(after.x-before.x==0){
				if(after.y-before.y==0 || !wall.judgeCollision(before,0,(after.y-before.y)/Math.abs(after.y-before.y),R,Math.abs(after.y-before.y)))
					elem=null;
				else if(after.y-before.y>0) elem=new Point(after.x,(int)Math.floor(wall.Y-R));
				else if(after.y-before.y<0)elem=new Point(after.x,(int)Math.ceil(wall.Y+wall.Height+R));
			}
			else {
				double slope = 1.0*(after.y-before.y)/(after.x-before.x);
				double slice = before.y - slope*before.x ;
				elem=wall.getCollisionPlace(before, after, slope, slice ,R);
			}
			if(elem==null)continue;
			if(place==null)place=elem;
			else{
				int min = Math.abs(place.x-before.x)+Math.abs(place.y-before.y);
				int now = Math.abs(elem.x-before.x)+Math.abs(elem.y-before.y);
				if(min>now)place=elem;
			}
		}
		return place;
	}
	
}
