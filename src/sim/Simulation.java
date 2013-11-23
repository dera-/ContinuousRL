package sim;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import sim.Parameters;
import sim.obj.Agent;
import sim.obj.WallData;

public class Simulation {
	private ArrayList<WallData> WallList = new ArrayList<WallData>();  //各壁を格納したリスト
	private Agent agent; /** エージェントを表すオブジェクト */
	//private Point Goal = new Point(400,100); /** ゴールの座標 */
	private int NowEpisode = 0;  /** 現在のエピソード数 */
	private int NowStep = 0;  /** 現在のステップ数 */
	private FileWriter Result; /**各エピソードにおいて消費したSTEP数をファイルに書き込むためのオブジェクト*/
	private final int FinalStep = 3000000;  //このステップ数を超えたら、1エピソード終了.
	private BufferedImage TracksImage;  /** エージェントの移動の軌跡を可視化した画像 */
	
	/** 以下、報酬に関するフィールド */
	private final double GoalReward=Parameters.MaxReward;  //3
	private final double CollisionPenalty=-1.0*GoalReward/100;  //-0.5
	//private final double AnglePenalty=-1.0*GoalReward/50;  //-0.5
	private final double Normal=Parameters.NormalReward;  //-0.001
	
	private final int Cycle=1;   //ファイルに書き込む周期(単位：episode)
	private final int GraphicsCycle = 10; //画像を出力する周期(単位：episode)
	private final int WriteClusterCycle = 1; //クラスターデータを出力する周期(単位：episode)
	private long StartTime;  //シミュレーションの開始時間
	
	/** コンストラクタ */
	public Simulation(){
		agent = new Agent();
		createWallList();
	}
	
	/** WallListに見えない壁(外に出ないようにする壁)を作るメソッド */
	private final void createWallList(){
		int num_add = 2;
		WallData walls[] = new WallData[4+num_add];
		walls[0] = new WallData((int)(-1*Parameters.MaxSpeed),(int)(-1*Parameters.MaxSpeed),(int)Parameters.MaxSpeed,(int)(Parameters.SimulatorHeight+2*Parameters.MaxSpeed));
		walls[1] = new WallData(0,(int)(-1*Parameters.MaxSpeed),Parameters.SimulatorWidth,(int)Parameters.MaxSpeed);
		walls[2] = new WallData(Parameters.EndX,(int)(-1*Parameters.MaxSpeed),(int)Parameters.MaxSpeed,(int)(Parameters.SimulatorHeight+2*Parameters.MaxSpeed));
		walls[3] = new WallData(0,Parameters.EndY,Parameters.SimulatorWidth,(int)Parameters.MaxSpeed);
		walls[4] = new WallData((int)Math.round(0.9*Parameters.SimulatorWidth),0,(int)Math.round(0.1*Parameters.SimulatorWidth),Parameters.SimulatorHeight);
		walls[5] = new WallData((int)Math.round(0.4*Parameters.SimulatorWidth),100,(int)Math.round(0.1*Parameters.SimulatorWidth),Parameters.SimulatorHeight-130);
		
		for(WallData w : walls){
			addWall(w);
		}
	}
	
	/** シミュレーションを1STEPだけ行うメソッド */
	public boolean running(){
		//System.out.println();
		//System.out.println(NowStep+"STEP");
		//if(NowStep>0)agent.forgetMemory();  /** 全履歴リストの一番上の要素を削除する */
		agent.understandState();  /* 外部環境と内部環境の把握を行う */
		//エージェントの移動前の座標を獲得
		Point before = getPoint_Clone(agent.getPlace());
		agent.selectAction(); //エージェントに行動を行わせる
		//System.out.println();
		Point after = getPoint_Clone(agent.getPlace());
		boolean collision = revisionCollision(before,after);   //移動の軌跡を用いて、ぶつかった場所の予測と補正
		updateTracksImage(before,agent.getPlace());
		boolean[] PenaltyArray ={collision};
		double reward = getReword(agent.getPlace(),PenaltyArray);  //報酬の取得
		//報酬によって学習法を変える
		if(reward==GoalReward) agent.updateBrain_WhenGoal(reward);
		else if(reward==CollisionPenalty) agent.updateBrain_WhenCollision(reward);
		else agent.updateBrain(reward);
		Result();
		return (NowEpisode>=Parameters.Numbers_Episode); /* シミュレーションを終わらせる時にtrueを返す */
	}
	
	/** 壁に衝突していたら、エージェントの位置を補正するメソッド。衝突した場合、返り値はtrue */
	private boolean revisionCollision(Point before,Point after){
		Point place=WallData.getCollisionPoint(WallList, before, after,Parameters.Radius_Agent);
		if(place!=null){
			agent.whenCollision(place);
			return true;
		}
		else return false;
	}
	
	/** エージェントに報酬を与える */
	private double getReword(Point place,boolean[] penalties){
		double penalty=0;
		if(penalties[0]) penalty += CollisionPenalty;
		//if(penalties[1]) penalty += AnglePenalty;
		if(penalty<0) return penalty;
		else{
			if(isReachGoal())return GoalReward;
			else return Normal;
		}
	}
	
	/** 1STEP終了毎に呼び出すメソッド */
	private void Result(){
		NowStep++;
		/* エージェントがゴールした場合の処理 */
		if(isReachGoal() || NowStep >= FinalStep ){
			System.out.println(NowEpisode+"episode終了,"+NowStep+"step消費");
			addWriteFile();
			outputTracksImage();
			formatTracksImage();
			//if(NowEpisode%WriteClusterCycle==0)agent.writeClusterDatas(NowEpisode+1); //統合前のクラスタの情報を書き込む
			//agent.doUnityClusters();
			agent.writeOutput_EnviromentRBF(NowEpisode);
			if(NowEpisode%WriteClusterCycle==0)agent.writeClusterDatas(NowEpisode+1);
			NowStep=0;
			NowEpisode++;
			agent.format();
		}
	}
	
	/** エージェントがゴールに着いたかどうかを判断するメソッド */
	public boolean isReachGoal(){
		Point Place_A=agent.getPlace();
		int distance=(int)Math.round( Math.sqrt( Math.pow(Place_A.x-Parameters.Goal_Place.x,2)+Math.pow(Place_A.y-Parameters.Goal_Place.y,2) ) );
		return (distance<Parameters.Radius_Agent+Parameters.Radius_Goal);
	}
	
	/** WallListの要素群を渡すメソッド */
	public ArrayList<WallData> getWallList(){
		ArrayList<WallData> list = new ArrayList<WallData>();
		list.addAll(WallList);
		return list;
	}
	
	/** WallListに要素を加えるメソッド */
	public void addWall(WallData wall){
		WallList.add(wall);
		agent.rewriteWallList(WallList);
	}
	
	/** WallListの最後の要素を引数のオブジェクトに書き換えるメソッド */
	public void rewriteLastElement(WallData wall){
		WallList.remove(WallList.size()-1);
		WallList.add(wall);
		agent.rewriteWallList(WallList);
	}
	
	/** WallListを空にするメソッド */
	public void clearWallList(){
		WallList.clear();
		agent.rewriteWallList(WallList);
	}
	
	/** 引数座標を含んでいる壁要素を全て消すメソッド */
	public void removeWallElement(int x,int y){
		int index=0;
		while(index<WallList.size()){
			WallData elem = WallList.get(index);
			if(elem.X<=x && x<=elem.X+elem.Width && elem.Y<=y && y<=elem.Y+elem.Height)
				WallList.remove(elem);
			else index++;
		}
		agent.rewriteWallList(WallList);
	}
	
	/** ゴールを設置するメソッド */
	public void createGoal(int x,int y){
		if(!Parameters.judgeCanPut(Parameters.Radius_Goal, x, y))return;
		Parameters.Goal_Place = new Point(x,y);
	}
	
	/** ゴールを消去するメソッド */
	public void removeGoal(){
		Parameters.Goal_Place = null;
	}
	
	/** シミュレーションを始めていいかどうか */
	public boolean okStart(){
		if(Parameters.Goal_Place != null)return true;
		else return false;
	}
	
	/** シミュレーションの描画 */
	public void draw(Graphics g){
		drawWalls(g);
		agent.drawArea(g);
		agent.draw(g);
		if(Parameters.Goal_Place != null)drawGoal(g);
	}
	
	/** 壁の描画 */
	private void drawWalls(Graphics g){
		for(WallData wall : WallList) {
			if(isInSimulator(wall)) wall.draw(g);
		}
	}
	
	/** 壁がシミュレーター画面内にあるかどうかを判定するメソッド */
	private boolean isInSimulator(WallData wall){
		return (Parameters.StartX<=wall.X && wall.X+wall.Width<=Parameters.EndX && Parameters.StartY<=wall.Y && wall.Y+wall.Height<=Parameters.EndY);
	}
	
	
	/** ゴールの描画 */
	private void drawGoal(Graphics g){
		g.setColor(Color.white);
		int x=(int)Math.round(Parameters.Goal_Place.x-Parameters.Radius_Goal);
		int y=(int)Math.round(Parameters.Goal_Place.y-Parameters.Radius_Goal);
		int r=(int)Math.round(2*Parameters.Radius_Goal);
		g.fillOval(x,y,r,r);
		g.setColor(Color.black);
		g.drawOval(x,y,r,r);
		g.setFont(new Font("明朝体",Font.BOLD,20));
		g.drawString("G",(int)Math.round(x+0.3*r),(int)Math.round(y+0.7*r));
	}
	
	/** 座標のクローンデータを作るメソッド */
	public static Point getPoint_Clone(Point pt){
		Point P = new Point(pt.x,pt.y);
		return P;
	}
	
	/** 各エピソードにおいて消費したSTEP数を書き込むためのファイルの作成 */
	private void createOutputFile(){
    	String Pass=Parameters.ResultFilePass;
    	try{
    		Result=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result);
    		String Header="Episode,STEP数,実時間,成功";
    		pWriter.println(Header);
    	}catch(IOException e){
    		System.out.println("ERROR!");
    	}
	}
	
	/** ファイルに消費したSTEP数等を保存 */
	private void addWriteFile(){
        if (NowEpisode%Cycle!=0 || Result==null) return;
        PrintWriter pWriter = new PrintWriter(Result);
        long time=(System.currentTimeMillis()-StartTime)/1000;
        String isOk;
        if(isReachGoal())isOk="○";
        else isOk="×";	
        String str=NowEpisode+","+NowStep+","+time+","+isOk;
        pWriter.println(str);
		try{
			Result.flush();
		}catch(IOException e){}
	}
	
	/** フィールドの初期化 */
	public void format(){
		if(NowEpisode!=0 || NowStep!=0)return;
		StartTime = System.currentTimeMillis();
		createOutputFile();
		formatTracksImage();
	}
	
	/**　ファイルを閉じる */
	public void fileClose(){
		try{
			if(Result!=null)Result.close();
		}catch(IOException e){}
	}
	
	/** TracksImageの初期化 */
	private void formatTracksImage(){
		TracksImage = new BufferedImage(Parameters.SimulatorWidth,Parameters.SimulatorHeight,BufferedImage.TYPE_3BYTE_BGR);
		
		//環境の描画
		Graphics g = TracksImage.createGraphics();
		drawWalls(g);
		drawGoal(g);
	}
	
	/** TrackImageの更新(エージェントの軌跡の描画) */
	private void updateTracksImage(Point before,Point after){
		Graphics g = TracksImage.createGraphics();
		g.drawLine(before.x, before.y, after.x, after.y);
	}
	
	/** TracksImageを画像として出力するメソッド */
	private void outputTracksImage(){
		if(NowEpisode%GraphicsCycle!=0) return;
		/** ファイルへの書き込みを行う */
		try{
			String fileName=Parameters.ResultFoldr+File.separatorChar+Parameters.PicturePrefix+"_episode"+NowEpisode+".png";
			ImageIO.write(TracksImage, "png", new File(fileName));
		}catch(IOException e){}
	}

}
