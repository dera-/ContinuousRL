package sim.obj;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import sim.Parameters;

/** �ǂ̃f�[�^���i�[���Ă���N���X */
public class WallData {
	public final int X; /** ����[��x���W�@*/
	public final int Y; /** ����[��y���W */
	public final int Width; /** ���̒��� */
	public final int Height; /** �c�̒��� */
	
	private final static int LEFT=0;  /** �l�p�`�̍��[ */
	private final static int RIGHT=1; /** �l�p�`�̉E�[ */
	private final static int UP=2;    /** �l�p�`�̏�[ */
	private final static int DOWN=3;  /** �l�p�`�̉��[ */
	
	/** �R���X�g���N�^ */
	public WallData(int x,int y,int w,int h){
		X=x;
		Y=y;
		Width=w;
		Height=h;
	}
	
	/** �G�[�W�F���g���ǂɏՓ˂����ꏊ��Ԃ� */
	public Point getCollisionPlace(Point p1,Point p2,double slope,double slice){
		return getCollisionPlace(p1,p2,slope,slice,0);
	}
	
	/** �ǂɏՓ˂����ꏊ��Ԃ� */
	private Point getCollisionPlace(Point p1,Point p2,double slope,double slice,double R){
		int sign=(p2.x-p1.x)/Math.abs(p2.x-p1.x);
		if( !judgeCollision(p1,sign,sign*slope,R,Math.abs(p2.x-p1.x)) )return null;
		Point[] collisions = new Point[4];  //�Ԃ������ꍇ�A�ǂɍŏ��ɓ�������W
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
	
	/** �ǂɐڂ�������agent�̒��S�_�����߂� */
	private Point decidePlace(int index,Point place,double slope,double slice,double R){
		int error=3;
		double x,y,a,b,c;
		x=0; y=0;  //�ꉞ������
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
	
	
	/** �ǂɂԂ��������ǂ����̔��� */
	private boolean judgeCollision(Point before,Point after,Point collision,double R){
		int distance = (int)Math.round( Math.sqrt( Math.pow(Math.abs(after.x-before.x),2) + Math.pow(Math.abs(after.y-before.y),2) ) );
		int distance_wall = (int)Math.round( Math.sqrt( Math.pow(Math.abs(collision.x-before.x),2) + Math.pow(Math.abs(collision.y-before.y),2) ) );
		return (distance_wall-R < distance); 
	}
	
	/** �ǂɂԂ��������ǂ����̔���(��萸�x�������͂�) */
	private boolean judgeCollision(Point after,double R){
		boolean aboutX=( X<(int)Math.floor(after.x+R) && X+Width>(int)Math.ceil(after.x-R) );
		boolean aboutY=( Y<(int)Math.floor(after.y+R) && Y+Height>(int)Math.ceil(after.y-R) );
		return (aboutX && aboutY);
	}
	
	/** �ǂɂԂ��������ǂ����̔���(���x�͈�Ԃ������ǁA���Ԃ������邩��) */
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
	
	
	/** �ǂ̏c���ɂԂ������ꍇ */
	private Point collisionX(double slope,double slice,int x,double R){
		int y = (int)Math.round(slope*x+slice);
		int r = (int)R;
		if( Y<y+r && y-r<Y+Height )return new Point(x,y);
		else return null;
	}
	
	/** �ǂ̏c���ɂԂ������ꍇ */
	private Point collisionX(double slope,int x,int bx,Point p1){
		int dx = x-bx;
		int dy = (int)Math.round(slope*dx);
		return new Point(p1.x+dx,p1.y+dy);
	}
	
	/** �ǂ̉����ɂԂ������ꍇ */
	private Point collisionY(double slope,double slice,int y,double R){
		int x = (int)Math.round(1.0*(y-slice)/slope);
		int r = (int)R;
		if( X<x+r && x-r<X+Width )return new Point(x,y);
		else return null;
	}
	
	/** �ǂ̉����ɂԂ������ꍇ */
	private Point collisionY(double slope,int y,int by,Point p1){
		int dy = y-by;
		int dx = (int)Math.round(1.0*dy/slope);
		return new Point(p1.x+dx,p1.y+dy);
	}
	
	/** ���̕ǂ̕`����s�����\�b�h */
	public void draw(Graphics g){
		g.setColor(Color.green);
		g.fillRect(X, Y, Width, Height);
		g.setColor(Color.black);
		g.drawRect(X, Y, Width, Height);
	}
	
	/** �ړ��O�ƈړ���̍��W����Փ˒n�_��Ԃ����\�b�h */
	public static Point getCollisionPoint(ArrayList<WallData> WallList, Point before,Point after,double R){
		Point place=null; //�ǂƏՓ˂������̃G�[�W�F���g�̏ꏊ 
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
