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
	private ArrayList<WallData> WallList = new ArrayList<WallData>();  //�e�ǂ��i�[�������X�g
	private Agent agent; /** �G�[�W�F���g��\���I�u�W�F�N�g */
	//private Point Goal = new Point(400,100); /** �S�[���̍��W */
	private int NowEpisode = 0;  /** ���݂̃G�s�\�[�h�� */
	private int NowStep = 0;  /** ���݂̃X�e�b�v�� */
	private FileWriter Result; /**�e�G�s�\�[�h�ɂ����ď����STEP�����t�@�C���ɏ������ނ��߂̃I�u�W�F�N�g*/
	private final int FinalStep = 3000000;  //���̃X�e�b�v���𒴂�����A1�G�s�\�[�h�I��.
	private BufferedImage TracksImage;  /** �G�[�W�F���g�̈ړ��̋O�Ղ����������摜 */
	
	/** �ȉ��A��V�Ɋւ���t�B�[���h */
	private final double GoalReward=Parameters.MaxReward;  //3
	private final double CollisionPenalty=-1.0*GoalReward/100;  //-0.5
	//private final double AnglePenalty=-1.0*GoalReward/50;  //-0.5
	private final double Normal=Parameters.NormalReward;  //-0.001
	
	private final int Cycle=1;   //�t�@�C���ɏ������ގ���(�P�ʁFepisode)
	private final int GraphicsCycle = 10; //�摜���o�͂������(�P�ʁFepisode)
	private final int WriteClusterCycle = 1; //�N���X�^�[�f�[�^���o�͂������(�P�ʁFepisode)
	private long StartTime;  //�V�~�����[�V�����̊J�n����
	
	/** �R���X�g���N�^ */
	public Simulation(){
		agent = new Agent();
		createWallList();
	}
	
	/** WallList�Ɍ����Ȃ���(�O�ɏo�Ȃ��悤�ɂ����)����郁�\�b�h */
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
	
	/** �V�~�����[�V������1STEP�����s�����\�b�h */
	public boolean running(){
		//System.out.println();
		//System.out.println(NowStep+"STEP");
		//if(NowStep>0)agent.forgetMemory();  /** �S�������X�g�̈�ԏ�̗v�f���폜���� */
		agent.understandState();  /* �O�����Ɠ������̔c�����s�� */
		//�G�[�W�F���g�̈ړ��O�̍��W���l��
		Point before = getPoint_Clone(agent.getPlace());
		agent.selectAction(); //�G�[�W�F���g�ɍs�����s�킹��
		//System.out.println();
		Point after = getPoint_Clone(agent.getPlace());
		boolean collision = revisionCollision(before,after);   //�ړ��̋O�Ղ�p���āA�Ԃ������ꏊ�̗\���ƕ␳
		updateTracksImage(before,agent.getPlace());
		boolean[] PenaltyArray ={collision};
		double reward = getReword(agent.getPlace(),PenaltyArray);  //��V�̎擾
		//��V�ɂ���Ċw�K�@��ς���
		if(reward==GoalReward) agent.updateBrain_WhenGoal(reward);
		else if(reward==CollisionPenalty) agent.updateBrain_WhenCollision(reward);
		else agent.updateBrain(reward);
		Result();
		return (NowEpisode>=Parameters.Numbers_Episode); /* �V�~�����[�V�������I��点�鎞��true��Ԃ� */
	}
	
	/** �ǂɏՓ˂��Ă�����A�G�[�W�F���g�̈ʒu��␳���郁�\�b�h�B�Փ˂����ꍇ�A�Ԃ�l��true */
	private boolean revisionCollision(Point before,Point after){
		Point place=WallData.getCollisionPoint(WallList, before, after,Parameters.Radius_Agent);
		if(place!=null){
			agent.whenCollision(place);
			return true;
		}
		else return false;
	}
	
	/** �G�[�W�F���g�ɕ�V��^���� */
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
	
	/** 1STEP�I�����ɌĂяo�����\�b�h */
	private void Result(){
		NowStep++;
		/* �G�[�W�F���g���S�[�������ꍇ�̏��� */
		if(isReachGoal() || NowStep >= FinalStep ){
			System.out.println(NowEpisode+"episode�I��,"+NowStep+"step����");
			addWriteFile();
			outputTracksImage();
			formatTracksImage();
			//if(NowEpisode%WriteClusterCycle==0)agent.writeClusterDatas(NowEpisode+1); //�����O�̃N���X�^�̏�����������
			//agent.doUnityClusters();
			agent.writeOutput_EnviromentRBF(NowEpisode);
			if(NowEpisode%WriteClusterCycle==0)agent.writeClusterDatas(NowEpisode+1);
			NowStep=0;
			NowEpisode++;
			agent.format();
		}
	}
	
	/** �G�[�W�F���g���S�[���ɒ��������ǂ����𔻒f���郁�\�b�h */
	public boolean isReachGoal(){
		Point Place_A=agent.getPlace();
		int distance=(int)Math.round( Math.sqrt( Math.pow(Place_A.x-Parameters.Goal_Place.x,2)+Math.pow(Place_A.y-Parameters.Goal_Place.y,2) ) );
		return (distance<Parameters.Radius_Agent+Parameters.Radius_Goal);
	}
	
	/** WallList�̗v�f�Q��n�����\�b�h */
	public ArrayList<WallData> getWallList(){
		ArrayList<WallData> list = new ArrayList<WallData>();
		list.addAll(WallList);
		return list;
	}
	
	/** WallList�ɗv�f�������郁�\�b�h */
	public void addWall(WallData wall){
		WallList.add(wall);
		agent.rewriteWallList(WallList);
	}
	
	/** WallList�̍Ō�̗v�f�������̃I�u�W�F�N�g�ɏ��������郁�\�b�h */
	public void rewriteLastElement(WallData wall){
		WallList.remove(WallList.size()-1);
		WallList.add(wall);
		agent.rewriteWallList(WallList);
	}
	
	/** WallList����ɂ��郁�\�b�h */
	public void clearWallList(){
		WallList.clear();
		agent.rewriteWallList(WallList);
	}
	
	/** �������W���܂�ł���Ǘv�f��S�ď������\�b�h */
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
	
	/** �S�[����ݒu���郁�\�b�h */
	public void createGoal(int x,int y){
		if(!Parameters.judgeCanPut(Parameters.Radius_Goal, x, y))return;
		Parameters.Goal_Place = new Point(x,y);
	}
	
	/** �S�[�����������郁�\�b�h */
	public void removeGoal(){
		Parameters.Goal_Place = null;
	}
	
	/** �V�~�����[�V�������n�߂Ă������ǂ��� */
	public boolean okStart(){
		if(Parameters.Goal_Place != null)return true;
		else return false;
	}
	
	/** �V�~�����[�V�����̕`�� */
	public void draw(Graphics g){
		drawWalls(g);
		agent.drawArea(g);
		agent.draw(g);
		if(Parameters.Goal_Place != null)drawGoal(g);
	}
	
	/** �ǂ̕`�� */
	private void drawWalls(Graphics g){
		for(WallData wall : WallList) {
			if(isInSimulator(wall)) wall.draw(g);
		}
	}
	
	/** �ǂ��V�~�����[�^�[��ʓ��ɂ��邩�ǂ����𔻒肷�郁�\�b�h */
	private boolean isInSimulator(WallData wall){
		return (Parameters.StartX<=wall.X && wall.X+wall.Width<=Parameters.EndX && Parameters.StartY<=wall.Y && wall.Y+wall.Height<=Parameters.EndY);
	}
	
	
	/** �S�[���̕`�� */
	private void drawGoal(Graphics g){
		g.setColor(Color.white);
		int x=(int)Math.round(Parameters.Goal_Place.x-Parameters.Radius_Goal);
		int y=(int)Math.round(Parameters.Goal_Place.y-Parameters.Radius_Goal);
		int r=(int)Math.round(2*Parameters.Radius_Goal);
		g.fillOval(x,y,r,r);
		g.setColor(Color.black);
		g.drawOval(x,y,r,r);
		g.setFont(new Font("������",Font.BOLD,20));
		g.drawString("G",(int)Math.round(x+0.3*r),(int)Math.round(y+0.7*r));
	}
	
	/** ���W�̃N���[���f�[�^����郁�\�b�h */
	public static Point getPoint_Clone(Point pt){
		Point P = new Point(pt.x,pt.y);
		return P;
	}
	
	/** �e�G�s�\�[�h�ɂ����ď����STEP�����������ނ��߂̃t�@�C���̍쐬 */
	private void createOutputFile(){
    	String Pass=Parameters.ResultFilePass;
    	try{
    		Result=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result);
    		String Header="Episode,STEP��,������,����";
    		pWriter.println(Header);
    	}catch(IOException e){
    		System.out.println("ERROR!");
    	}
	}
	
	/** �t�@�C���ɏ����STEP������ۑ� */
	private void addWriteFile(){
        if (NowEpisode%Cycle!=0 || Result==null) return;
        PrintWriter pWriter = new PrintWriter(Result);
        long time=(System.currentTimeMillis()-StartTime)/1000;
        String isOk;
        if(isReachGoal())isOk="��";
        else isOk="�~";	
        String str=NowEpisode+","+NowStep+","+time+","+isOk;
        pWriter.println(str);
		try{
			Result.flush();
		}catch(IOException e){}
	}
	
	/** �t�B�[���h�̏����� */
	public void format(){
		if(NowEpisode!=0 || NowStep!=0)return;
		StartTime = System.currentTimeMillis();
		createOutputFile();
		formatTracksImage();
	}
	
	/**�@�t�@�C������� */
	public void fileClose(){
		try{
			if(Result!=null)Result.close();
		}catch(IOException e){}
	}
	
	/** TracksImage�̏����� */
	private void formatTracksImage(){
		TracksImage = new BufferedImage(Parameters.SimulatorWidth,Parameters.SimulatorHeight,BufferedImage.TYPE_3BYTE_BGR);
		
		//���̕`��
		Graphics g = TracksImage.createGraphics();
		drawWalls(g);
		drawGoal(g);
	}
	
	/** TrackImage�̍X�V(�G�[�W�F���g�̋O�Ղ̕`��) */
	private void updateTracksImage(Point before,Point after){
		Graphics g = TracksImage.createGraphics();
		g.drawLine(before.x, before.y, after.x, after.y);
	}
	
	/** TracksImage���摜�Ƃ��ďo�͂��郁�\�b�h */
	private void outputTracksImage(){
		if(NowEpisode%GraphicsCycle!=0) return;
		/** �t�@�C���ւ̏������݂��s�� */
		try{
			String fileName=Parameters.ResultFoldr+File.separatorChar+Parameters.PicturePrefix+"_episode"+NowEpisode+".png";
			ImageIO.write(TracksImage, "png", new File(fileName));
		}catch(IOException e){}
	}

}
