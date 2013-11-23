package sim.obj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

import sim.Parameters;
import sim.brain.NewBrain;

public class Agent {
	private Point Place = new Point(Parameters.Default_Agent_X,Parameters.Default_Agent_Y); /** ���̃G�[�W�F���g�̌��݂̍��W */
	private final double FirstSpeed=0;  //���x�̏������
	private final double FirstAngle=270;  //�p�x�̏������
	private double NowSpeed = FirstSpeed;  //���݂̃G�[�W�F���g�̑��x
	private double NowAngle = FirstAngle;  //���݂̃G�[�W�F���g�̊p�x
	private final double Distance = 50;
	private final double Sight = 90;
	private ArrayList<WallData> WallList = new ArrayList<WallData>();  //�V�~�����[�V������̕ǂ̏����i�[���Ă��郊�X�g
	//private ArrayList<Double> SpeedList = new ArrayList<Double>();  //�G�[�W�F���g�̑��x�̗���
	//private ArrayList<Point> PlaceList = new ArrayList<Point>();  //�G�[�W�F���g�̈ʒu�̗���
	//private ArrayList<Double> AngleList = new ArrayList<Double>();   //�G�[�W�F���g�̌����̗���
	//private ArrayList<Integer> ActionList = new ArrayList<Integer>();  //�G�[�W�F���g�̍s���̗���
	
	private NewBrain Brain;  //�G�[�W�F���g�̔]����
	
	/** �R���X�g���N�^ */
	public Agent(){
		//�G�[�W�F���g�̔](Actor-Critic)�̍\�z
		Brain = new NewBrain();
	}
	
	/** �s����I�����郁�\�b�h */
	public void selectAction(){
		/** ���x�̕ύX��p�x�̕ύX�� */
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
		setPlace(Place.x+vx,Place.y+vy);  /* �s����̍��W */
	}
	
	/** �G�[�W�F���g�̍��W��Ԃ����\�b�h */
	public Point getPlace(){
		return Place;
	}
	
	/** �ǂɂԂ��������ɌĂяo�����\�b�h */
	public void whenCollision(Point pt){
		double distance=Math.sqrt(Math.pow(Math.abs(Place.x-pt.x),2)+Math.pow(Math.abs(Place.y-pt.y),2));
		double reduce=-2.0*NowSpeed*distance/Parameters.Radius_Agent;
		changeSpeed(reduce);
		setPlace(pt.x,pt.y);
	}
	
	
	/** �����̒l���G�[�W�F���g�̍��W�ɂ��郁�\�b�h */
	public void setPlace(int x,int y){
		Place.x=x;
		Place.y=y;
	}
	
	/** �G�[�W�F���g�̑��x�̕ύX */
	private void changeSpeed(double vx){
		NowSpeed+=vx;
		if(NowSpeed<0)NowSpeed=0;
		else if(NowSpeed>Parameters.MaxSpeed)NowSpeed=Parameters.MaxSpeed;
	}
	
	/** �G�[�W�F���g�̕`�惁�\�b�h */
	public void draw(Graphics g){
		g.setColor(Color.cyan);
		int x=(int)Math.round(Place.x-Parameters.Radius_Agent);
		int y=(int)Math.round(Place.y-Parameters.Radius_Agent);
		int r=(int)Math.round(2*Parameters.Radius_Agent);
		g.fillOval(x,y,r,r);
		g.setColor(Color.black);
		g.drawOval(x,y,r,r);
		g.setFont(new Font("������",Font.BOLD,20));
		g.drawString("A",(int)Math.round(x+0.3*r),(int)Math.round(y+0.7*r));
		//g.drawRect(x, y, r, r);
		
		//���o��̕`����s��
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
	
	/** �����̊����猻�݂̏�Ԃ�c�����郁�\�b�h */
	public void understandState(){
		double[] outside = getState_Outside();
		double[] inside = getState_Inside();
		Brain.estimateOutsideState(outside);  /*�@�O����Ԃ̉��l�̐��� */
		Brain.createClusterList(outside, inside);  /* �N���X�^�̍\�z */
		Brain.estimateInsideState(inside);    /* ������Ԃ̉��l�̐��� */
	}
	
	/** �]�����̍X�V(�w�K)���s�����\�b�h */
	public void updateBrain(double reward){
		/** 6/24�ǉ� */
		if(reward==Parameters.NormalReward){
			reward = reward - reward*(NowSpeed/Parameters.MaxSpeed); 
		}
		double[] outsides = getState_Outside();
		double[] insides = getState_Inside();
		Brain.updateEnvironment(Parameters.Normal_Environment,outsides);  //�O�����̍X�V(test��reward���g�p�B�{�������̕�V�l��0)
		Brain.updateOneCluster(reward,insides);  //�������̍X�V
	}
	
	/** �]�����̍X�V���s�����\�b�h(�����e�J�����@��p����ꍇ) */
	/*
	public void updateBrain_MonteCarlo(double reward){
		//double[] outsides = getState_Outside();
		//double[] insides = getState_Inside();
		Brain.updateEnvironment_MonteCarlo(reward,null);  //Critic��(�O����)�̍X�V
		Brain.updateAllCluster(reward,null);  //Critic��(������)�̍X�V
	}
	*/
	
	/** �ړI�n���B���ɔ]�����̍X�V���s�����\�b�h(�����e�J�����@��p����) */
	public void updateBrain_WhenGoal(double reward){
		//double[] outsides = getState_Outside();
		//double[] insides = getState_Inside();
		Brain.updateEnvironment_MonteCarlo(Parameters.MaxReward_Environment,null);  //�O�����̍X�V
		//doUnityClusters(); //test
		Brain.updateAllCluster(reward);  //�������̍X�V
	}
	
	/** ��Q���ɂԂ��������ɔ]�����̍X�V���s�����\�b�h */
	public void updateBrain_WhenCollision(double reward){
		//double[] outsides = getState_Outside();
		//double[] insides = getState_Inside();
		Brain.updateEnvironment(Parameters.Penalty_Environment,null);  //�O�����̍X�V
		Brain.updateOneCluster(reward);  //�������̍X�V
	}
	
	/** ���݂̃G�[�W�F���g�̓�����Ԃ�Ԃ����\�b�h */
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
	
	/**  ���E�����l�����郁�\�b�h */
	private double[] knowViewInformation(ArrayList<WallData> list){
		double[] views = new double[Parameters.Agent_InfraredNums];
		double interval=Sight/Parameters.Agent_InfraredNums;   /* �e�ԊO���̊Ԋu */
		double start = NowAngle+Sight/2;  /* ���[�̐ԊO���̊p�x */
		
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
	
	/** �S�[���Ƃ̑��ΓI�Ȋp�x���l�����郁�\�b�h */
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
	
	/** �S�[���܂ł̋������l�����郁�\�b�h */
	/*
	private double getDistanceToGoal(){
		double distance = Math.sqrt(Math.pow(Parameters.Goal_Place.x-Place.x, 2)+Math.pow(Parameters.Goal_Place.y-Place.y, 2));
		return distance;
	}
	*/
	
	/** ���݂̊O����Ԃ�Ԃ����\�b�h */
	private double[] getState_Outside(){
		double[] outsides = {Place.x,Place.y};
		return outsides;
	}
	
	/** �t�B�[���h�̏��������s�� */
	public void format(){
		Place.x=Parameters.Default_Agent_X;
		Place.y=Parameters.Default_Agent_Y;
		NowSpeed = FirstSpeed;
		NowAngle = FirstAngle;
		Brain.format();
	}
	
	/** WallList�̒��g�����������郁�\�b�h */
	public void rewriteWallList(ArrayList<WallData> list){
		WallList.clear();
		WallList.addAll(list);
	}
	
	/** �S�N���X�^�[�̓������������s���郁�\�b�h */
	public void doUnityClusters(){
		Brain.unityAllClusters();
	}
	
	/** �e�N���X�^�͈̔͂�`�悷�郁�\�b�h */
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
	
	/** �O��RBF�l�b�g���[�N�̏o�͒l���t�@�C���o�͂��郁�\�b�h */
	public void writeOutput_EnviromentRBF(int episode){
		Brain.writeAverageOutput(episode);
	}
	
	/** �e�N���X�^�[�̏����t�@�C���ɏ������ރ��\�b�h */
	public void writeClusterDatas(int episode){
		Brain.writeAboutClusters(episode);
	}
	
}