package sim.obj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import sim.Parameters;
import sim.brain.NewBrain;

public class Agent {
	private Point Place = new Point(Parameters.Default_Agent_X,Parameters.Default_Agent_Y); /** このエージェントの現在の座標 */
	private final double FirstSpeed=0;  //速度の初期状態
	private final double FirstAngle=270;  //角度の初期状態
	private double NowSpeed = FirstSpeed;  //現在のエージェントの速度
	private double NowAngle = FirstAngle;  //現在のエージェントの角度
	private final double Distance = 50;
	private final double Sight = 90;
	private ArrayList<WallData> WallList = new ArrayList<WallData>();  //シミュレーション上の壁の情報を格納しているリスト
	//private ArrayList<Double> SpeedList = new ArrayList<Double>();  //エージェントの速度の履歴
	//private ArrayList<Point> PlaceList = new ArrayList<Point>();  //エージェントの位置の履歴
	//private ArrayList<Double> AngleList = new ArrayList<Double>();   //エージェントの向きの履歴
	//private ArrayList<Integer> ActionList = new ArrayList<Integer>();  //エージェントの行動の履歴
	
	private NewBrain Brain;  //エージェントの脳部分
	
	/** コンストラクタ */
	public Agent(){
		//エージェントの脳(Actor-Critic)の構築
		Brain = new NewBrain();
	}
	
	/** 行動を選択するメソッド */
	public void selectAction(){
		/** 速度の変更や角度の変更等 */
		double[] states_inside = getState_Inside();
		double[] acts = Brain.getAction(states_inside);
		double accel = acts[0];
		double angle = acts[1];
		NowSpeed+=accel;
		if(NowSpeed>Parameters.MaxSpeed)NowSpeed=Parameters.MaxSpeed;
		if(NowSpeed<0)NowSpeed=0;
		NowAngle+=angle;
		if(NowAngle > 360)NowAngle-=360;
		if(NowAngle < 0)NowAngle+=360;
		int vx = (int)Math.round(NowSpeed*Math.cos(NowAngle*Parameters.RD));
		int vy = (int)Math.round(NowSpeed*Math.sin(NowAngle*Parameters.RD));
		setPlace(Place.x+vx,Place.y+vy);  /* 行動後の座標 */
	}
	
	/** エージェントの座標を返すメソッド */
	public Point getPlace(){
		return Place;
	}
	
	/** 壁にぶつかった時に呼び出すメソッド */
	public void whenCollision(Point pt){
		double distance=Math.sqrt(Math.pow(Math.abs(Place.x-pt.x),2)+Math.pow(Math.abs(Place.y-pt.y),2));
		double reduce=-2.0*NowSpeed*distance/Parameters.Radius_Agent;
		changeSpeed(reduce);
		setPlace(pt.x,pt.y);
	}
	
	
	/** 引数の値をエージェントの座標にするメソッド */
	public void setPlace(int x,int y){
		Place.x=x;
		Place.y=y;
	}
	
	/** エージェントの速度の変更 */
	private void changeSpeed(double vx){
		NowSpeed+=vx;
		if(NowSpeed<0)NowSpeed=0;
		else if(NowSpeed>Parameters.MaxSpeed)NowSpeed=Parameters.MaxSpeed;
	}
	
	/** エージェントの描画メソッド */
	public void draw(Graphics g){
		g.setColor(Color.cyan);
		int x=(int)Math.round(Place.x-Parameters.Radius_Agent);
		int y=(int)Math.round(Place.y-Parameters.Radius_Agent);
		int r=(int)Math.round(2*Parameters.Radius_Agent);
		g.fillOval(x,y,r,r);
		g.setColor(Color.black);
		g.drawOval(x,y,r,r);
		g.setFont(new Font("明朝体",Font.BOLD,20));
		g.drawString("A",(int)Math.round(x+0.3*r),(int)Math.round(y+0.7*r));
		//g.drawRect(x, y, r, r);
		
		//視覚野の描画も行う
		int Vx=(int)Math.round(Place.x-Distance);
		int Vy=(int)Math.round(Place.y-Distance);
		int Vr=(int)Math.round(2*Distance);
		int start = (int)Math.round(-1*(NowAngle+Sight/2));
		int size = (int)Math.round(Sight);
		g.setColor(Color.orange);
		g.fillArc(Vx, Vy, Vr, Vr, start, size);
		g.setColor(Color.black);
		g.drawArc(Vx, Vy, Vr, Vr, start, size);
	}
	
	/** 引数の環境から現在の状態を把握するメソッド */
	public void understandState(){
		double[] outside = getState_Outside();
		double[] inside = getState_Inside();
		Brain.estimateOutsideState(outside);  /*　外部状態の価値の推測 */
		Brain.createClusterList(outside, inside);  /* クラスタの構築 */
		Brain.estimateInsideState(inside);    /* 内部状態の価値の推測 */
	}
	
	/** 脳部分の更新(学習)を行うメソッド */
	public void updateBrain(double reward){
		/** 6/24追加 */
		if(reward==Parameters.NormalReward){
			reward = reward - reward*(NowSpeed/Parameters.MaxSpeed); 
		}
		double[] outsides = getState_Outside();
		double[] insides = getState_Inside();
		Brain.updateEnvironment(Parameters.Normal_Environment,outsides);  //外部環境の更新(testでrewardを使用。本来ここの報酬値は0)
		Brain.updateOneCluster(reward,insides);  //内部環境の更新
	}
	
	/** 脳部分の更新を行うメソッド(モンテカルロ法を用いる場合) */
	/*
	public void updateBrain_MonteCarlo(double reward){
		//double[] outsides = getState_Outside();
		//double[] insides = getState_Inside();
		Brain.updateEnvironment_MonteCarlo(reward,null);  //Critic部(外部環境)の更新
		Brain.updateAllCluster(reward,null);  //Critic部(内部環境)の更新
	}
	*/
	
	/** 目的地到達時に脳部分の更新を行うメソッド(モンテカルロ法を用いる) */
	public void updateBrain_WhenGoal(double reward){
		//double[] outsides = getState_Outside();
		//double[] insides = getState_Inside();
		Brain.updateEnvironment_MonteCarlo(Parameters.MaxReward_Environment,null);  //外部環境の更新
		//doUnityClusters(); //test
		Brain.updateAllCluster(reward);  //内部環境の更新
	}
	
	/** 障害物にぶつかった時に脳部分の更新を行うメソッド */
	public void updateBrain_WhenCollision(double reward){
		//double[] outsides = getState_Outside();
		//double[] insides = getState_Inside();
		Brain.updateEnvironment(Parameters.Penalty_Environment,null);  //外部環境の更新
		Brain.updateOneCluster(reward);  //内部環境の更新
	}
	
	/** 現在のエージェントの内部状態を返すメソッド */
	private double[] getState_Inside(){
		//double PlusMinus;
		//if(NowAngle==0)PlusMinus=1;
		//else PlusMinus = NowAngle/Math.abs(NowAngle);
		double[] views = knowViewInformation(WallList);
		double[] insides = new double[views.length+2];
		for(int i=0;i<views.length;i++)insides[i] = views[i];
		insides[insides.length-2] = NowSpeed;
		insides[insides.length-1] = getAngleToGoal();
		return insides;
	}
	
	/**  視界情報を獲得するメソッド */
	private double[] knowViewInformation(ArrayList<WallData> list){
		double[] views = new double[Parameters.Agent_InfraredNums];
		double interval=Sight/Parameters.Agent_InfraredNums;   /* 各赤外線の間隔 */
		double start = NowAngle+Sight/2;  /* 左端の赤外線の角度 */
		
		for(int i=0;i<Parameters.Agent_InfraredNums;i++){
			double now = start-interval*i;
			int x = (int)Math.round(Place.x+Distance*Math.cos(now*Parameters.RD));
			int y = (int)Math.round(Place.y+Distance*Math.sin(now*Parameters.RD));
			Point depth = new Point(x,y);
			Point collision = WallData.getCollisionPoint(list, Place, depth, 0);
			if(collision==null) views[i]=Distance;
			else views[i]=Math.sqrt( Math.pow(Math.abs(collision.x-Place.x),2) + Math.pow(Math.abs(collision.y-Place.y),2) );
		}
		return views;
	}
	
	/** ゴールとの相対的な角度を獲得するメソッド */
	private double getAngleToGoal(){
		int distance_Y = Parameters.Goal_Place.y-Place.y;
		int distance_X = Parameters.Goal_Place.x-Place.x;
		double angle = Math.atan2(distance_Y,distance_X)/Parameters.RD;
		/*
		if(distance_X==0){
			if(distance_Y>=0)angle=90;
			else angle=270;
		}
		else{
			double tan=1.0*(goal.y-Place.y)/(goal.x-Place.x);
			angle=Math.atan(tan)/Parameters.RD;
		}
		*/
		if(angle<0)angle+=360;
		//if(distance_X<0)angle+=180;
		angle = Math.abs( angle - NowAngle);
		if(angle>180) angle = 360-angle;
		if(!(0<=angle && angle<=180))System.out.println(angle);
		return angle;
	}
	
	/** ゴールまでの距離を獲得するメソッド */
	/*
	private double getDistanceToGoal(){
		double distance = Math.sqrt(Math.pow(Parameters.Goal_Place.x-Place.x, 2)+Math.pow(Parameters.Goal_Place.y-Place.y, 2));
		return distance;
	}
	*/
	
	/** 現在の外部状態を返すメソッド */
	private double[] getState_Outside(){
		double[] outsides = {Place.x,Place.y};
		return outsides;
	}
	
	/** フィールドの初期化を行う */
	public void format(){
		Place.x=Parameters.Default_Agent_X;
		Place.y=Parameters.Default_Agent_Y;
		NowSpeed = FirstSpeed;
		NowAngle = FirstAngle;
		Brain.format();
	}
	
	/** WallListの中身を書き換えるメソッド */
	public void rewriteWallList(ArrayList<WallData> list){
		WallList.clear();
		WallList.addAll(list);
	}
	
	/** 全クラスターの統合処理を実行するメソッド */
	public void doUnityClusters(){
		Brain.unityAllClusters();
	}
	
	/** 各クラスタの範囲を描画するメソッド */
	public void drawArea(Graphics g){
		ArrayList<double[][]> list = Brain.getClusterRanges();
		for(int i=0;i<list.size();i++){
			double[][] range = list.get(i);
			/*
			Color col;
			switch(i){
			case 0: col=Color.blue;
					break;
			case 1: col=Color.cyan;
					break;
			case 2: col=Color.darkGray;
					break;
			case 3: col=Color.gray;
					break;
			case 4: col=Color.yellow;
					break;
			case 5: col=Color.lightGray;
					break;
			case 6: col=Color.magenta;
					break;
			case 7: col=Color.orange;
					break;
			case 8: col=Color.pink;
					break;
			case 9: col=Color.red;
					break;
			default: col = Color.white;
					 break;
			}
			g.setColor(col);
			g.fillRect((int)range[0][0], (int)range[1][0], (int)(range[0][1]-range[0][0]), (int)(range[1][1]-range[1][0]));
			*/
			g.setColor(Color.black);
			g.drawRect((int)range[0][0], (int)range[1][0], (int)(range[0][1]-range[0][0]), (int)(range[1][1]-range[1][0]));
		}
	}
	
	/** 外部RBFネットワークの出力値をファイル出力するメソッド */
	public void writeOutput_EnviromentRBF(int episode){
		Brain.writeAverageOutput(episode);
	}
	
	/** 各クラスターの情報をファイルに書き込むメソッド */
	public void writeClusterDatas(int episode){
		Brain.writeAboutClusters(episode);
	}
	
}