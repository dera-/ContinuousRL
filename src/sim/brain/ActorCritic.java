package sim.brain;

import java.util.ArrayList;

import sim.MTRandom;
import sim.Parameters;

/** �s���̌�����s���I�u�W�F�N�g�B */
public class ActorCritic {
	/**�G�[�W�F���g�̓�����Ԃ̉��l�𐄑�����RBF�l�b�g���[�N�̃p�����[�^�[�Q*/
	private static final int Numbers_State_Type = 3;  /** ��Ԃ̎�����(��Ԃ̎�ނ̐�) */
	private static final int inputs_agent[]={Parameters.Agent_InfraredNums,1,1};  /**���͂̎�ނ��Ƃ̌�*/
	private static final double Min_Input_Agent[]={0,0,0};  /**�e���͒l�̍ŏ��l*/
	private static final double Max_Input_Agent[]={Parameters.Agent_Distance,Parameters.MaxSpeed,180};  /**�e���͒l�̍ő�l*/
	private static final int RBFs_agent[]={5,5,10};  /**�e���͒l�ɑ΂���RBF�f�q��*/
	
	/** �s���Ɋւ���p�����[�^�[�Q */
	private static final int Numbers_Act_Type = 2; /** �s���̎�ސ� */
	private static final double Act_Maxs[] = {Parameters.Max_Decide,Parameters.ChangeSpeed,Parameters.ChangeAngle};
	private static final double Act_Mins[] = {Parameters.Min_Decide,-1.0*Parameters.ChangeSpeed,-1.0*Parameters.ChangeAngle};
	private static final double Act_Speed[] = {-1.0*Parameters.ChangeSpeed,0,Parameters.ChangeSpeed};
	private static final double Act_Angle[] = {-1.0*Parameters.ChangeAngle,0,Parameters.ChangeAngle};
	private static final double Act_Speed_Splits[];
	private static final double Act_Angle_Splits[];
	
	private RbfNetwork Critic;  /** �G�[�W�F���g�̓����̏�Ԃ̉��l�𐄑�����RBF�l�b�g���[�N */
	private ArrayList<Actor> ActorList = new ArrayList<Actor>(); /** �s���̐�����actor�����쐬���� */
	
	private long EXP=0; /** ����actor-critic�̃p�����[�^�[���X�V������ */
	private int GoalCount=0;
	
	static{
		//Act_Speed_Splits��Act_Angle_Splits�̂��ꂼ��ɃI�u�W�F�N�g���i�[���鏈��
		Act_Speed_Splits = getSplitArray(Act_Speed.length,-1.0*Parameters.ChangeSpeed,Parameters.ChangeSpeed);
		Act_Angle_Splits = getSplitArray(Act_Angle.length,-1.0*Parameters.ChangeAngle,Parameters.ChangeAngle);
	}
	
	public ActorCritic(String name){
		int splits[]={ inputs_agent[0]/2 , inputs_agent[0]-inputs_agent[0]/2 , inputs_agent[1] , inputs_agent[2] };
		Critic = new RbfNetwork(name,inputs_agent,Min_Input_Agent,Max_Input_Agent,RBFs_agent,splits);
		
		/** actor�ɂ������d�z�������̂��߂̔z��(splits_arrays)�̍쐬 */
		double[][] splits_arrays = new double[Numbers_Act_Type+1][getTotal_Array(inputs_agent)];
		double[][] elems_split = new double[Numbers_Act_Type+1][Numbers_State_Type];
		String[] split_origin = {"0.333,0.333,0.333","0.333,0.333,0.333","0.333,0.333,0.333"};
		for(int i=0;i<elems_split.length;i++){
			String[] origin = split_origin[i].split(",");
			for(int j=0;j<elems_split[i].length;j++){
				elems_split[i][j] = Double.parseDouble(origin[j]);
			}
		}
		
		for(int index=0;index<splits_arrays.length;index++){
			for(int i=0;i<splits_arrays[index].length;i++){
				if(i<Parameters.Agent_InfraredNums){
					splits_arrays[index][i] = elems_split[index][0]/Parameters.Agent_InfraredNums;
					//System.out.println("index:"+index+",i:"+i+",value:"+splits_arrays[index][i]);
				}
				else{
					splits_arrays[index][i] = elems_split[index][i-Parameters.Agent_InfraredNums+1];
					//System.out.println("index:"+index+",i:"+i+",value:"+splits_arrays[index][i]);
				}
			}
		}
		
		//ActorList�̍쐬
		createActorList(Act_Mins,Act_Maxs,inputs_agent,Min_Input_Agent,Max_Input_Agent,splits_arrays);
	}
	
	/** ActorList�̍쐬 */
	private final void createActorList(double[] act_mins,double[] act_maxs,int[] inputs,double[] mins,double[] maxs,double[][] splits){
		int vectors = getTotal_Array(inputs);
		double maxs_V[] = new double[vectors];
		double mins_V[] = new double[vectors];
		int index=0;
		for(int i=0;i<inputs.length;i++){
			for(int j=0;j<inputs[i];j++,index++){
				maxs_V[index] = maxs[i];
				mins_V[index] = mins[i];
			}
		}
		
		int actors = act_mins.length;
		for(int i=0;i<actors;i++){
			ActorList.add(new Actor(act_mins[i],act_maxs[i],mins_V,maxs_V,splits[i]));
		}
	}
	
	/** �z��̍��v�l�����߂郁�\�b�h */
	private int getTotal_Array(int[] array){
		int total=0;
		for(int i : array){
			total+=i;
		}
		return total;
	}
	
	/** Splits(�s���̋�؂�)�p�̔z��𐶐����ĕԂ����\�b�h */
	private static double[] getSplitArray(int num_acts,double min,double max){
		double[] array = new double[num_acts-1];
		for(int i=0;i<array.length;i++){
			array[i] = (1.0*(i+1)/num_acts)*(max-min)+min;
		}
		return array;
	}
	
	/** ������double2�d�z���1��double�z��ɕϊ����郁�\�b�h */
	private double[] createDoubleArray(double[]... arrays){
		return createDoubleArray(new int[0],new int[0],arrays);
	}
	
	/** ������double2�d�z���1��double�z��ɕϊ����郁�\�b�h */
	private double[] createDoubleArray(int[] xs,int[] ys,double[]... arrays){
		ArrayList<Double> list = new ArrayList<Double>();
		for(int i=0;i<arrays.length;i++){
			for(int j=0;j<arrays[i].length;j++){
				if(isIncluded(j,i,xs,ys)) continue;
				double d=arrays[i][j];
				list.add(d);
			}
		}
		double[] array = new double[list.size()];
		for(int i=0;i<array.length;i++){
			array[i]=list.get(i);
		}
		return array;
	}
	
	/** ������double�ϐ��Q��1��double�z��ɂ܂Ƃ߂鏈�� */
	private double[] createDoubleArray(double... ds){
		return ds;
	}
	
	/** ��1�����Ƒ�2�����̑g���z�񒆂ɑ��݂��邩�ǂ������肷�郁�\�b�h */
	private boolean isIncluded(int x,int y,int[] xs,int[] ys){
		for(int i=0;i<ys.length;i++){
			for(int j=0;j<xs.length;j++){
				if(xs[j]==x && ys[i]==y)return true;
			}
		}
		return false;
	}
	
	/** EXP�ɉ��Z���郁�\�b�h */
	public void addEXP(int i){
		EXP+=i;
	}
	
	/** EXP�ɉ��Z���郁�\�b�h */
	public void addEXP(){
		addEXP(1);
	}
	
	/** EXP��Ԃ����\�b�h */
	public long getEXP(){
		return EXP;
	}
	
	public int getGoalCount(){
		return GoalCount;
	}
	
	public void addGoalCount(){
		GoalCount++;
	}
	
	
	/** �����̏�Ԃ̉��l�𐄑����郁�\�b�h */
	public void runCritic(double[] states){
		//System.out.println("output:"+Environment.getOutput(0)+",sigmoid:"+Environment.getSigmoidOutput());
		//System.out.println("�o�͒l�F"+Critic.getRBFOutput(states));
		Critic.runRBF(states);
	}
	
	/** �������(critic)�̐������l��Ԃ����\�b�h */
	public double getCriticValue(double[] states){
		return Critic.getRBFOutput(states);
	}
	
	/** ���ۂɍs���s�����擾���郁�\�b�h */
	public double[] selectAction(double[] states){
		
		/*
		Actor actor = ActorList.get(0);
		int number = decideAction(actor.getAction(states));
		//System.out.println("�s���ԍ��F"+number);
		return getAction(number,states);
		*/
		Actor Speed = ActorList.get(1);
		Actor Angle = ActorList.get(2);
		double[] acts = {Speed.getAction(states) , Angle.getAction(states)};
		return acts;
	}
	
	/** RBF�l�b�g���[�N�̏o�͌��ʂ���s���s���̔ԍ���Ԃ����\�b�h */
	private int decideAction(double s){
		double central = (Parameters.Max_Decide + Parameters.Min_Decide) / 2 ;
		if(s<=central) return Parameters.Number_Accel;
		else return Parameters.Number_Angle;
	}
	
	/** �����̓Y������Actor�v�f����s�����擾���郁�\�b�h */
	private double[] getAction(int index,double[] states){
		double[] acts = {0,0};
		if(index>ActorList.size())return acts;
		Actor actor=ActorList.get(index);
		double act = actor.getAction(states);
		switch(index){
		case Parameters.Number_Accel:
			acts[0] = act; //getRealAction(act,Act_Speed,Act_Speed_Splits);
			//System.out.println("����:"+acts[0]);
			break;
		case Parameters.Number_Angle:
			acts[1] = act; //getRealAction(act,Act_Angle,Act_Angle_Splits);
			//System.out.println("�����ϊ�:"+acts[1]);
			break;
		default: break;
		}
		return acts;
	}
	
	/** ���ۂɍs���s���������̔z��(array_act)����I�����郁�\�b�h */
	private double getRealAction(double act,double[] array_act,double[] array_split){
		int index;
		for(index=0;index<array_split.length;index++){
			if(act<array_split[index])return array_act[index];
		}
		return array_act[index];
	}
	
	/** Actor��Critic���Z�b�g���\�b�h */
	public void setActorCritic(ActorCritic ac){
		ac.format();  //�܂��͗����̏������s���B
		ActorList.clear();
		ActorList.addAll(ac.ActorList);
		Critic.setData(ac.Critic);
		EXP = ac.EXP;
		GoalCount = ac.GoalCount;
	}
	
	/** Critic�ŋ��߂�ꂽTD�덷��Ԃ����\�b�h */
	public double getTDError(double comp,double[] states){
		return Critic.getTDErorr(comp,states);
	}
	
	/** Actor���̍X�V���s�����\�b�h */
	public void updateActor(double td_error,double[] states){
		double[] tds = new double[states.length];
		for(int i=0;i<tds.length;i++){
			tds[i] = td_error;
		}
		/*
		Actor actor = ActorList.get(0);
		actor.updateParameters(tds, states);
		int number = decideAction(actor.getLeastAction());
		Actor act_real = ActorList.get(number);
		act_real.updateParameters(tds,states);
		*/
		
		Actor Speed = ActorList.get(1);
		Speed.updateParameters_Trace(tds,states);
		Actor Angle = ActorList.get(2);
		Angle.updateParameters_Trace(tds,states);
	}
	
	/** Actor���̍X�V���s�����\�b�h */
	public void updateActor_OnlyReward(double comp,double[] states){
		double td_error = Critic.getTDErorr(comp, null);
		updateActor(td_error,states);
	}
	
	/** Actor���̍X�V���s�����\�b�h(�����e�J�����@) */
	/*
	public void updateActor_MonteCarlo(double comp,double speed,double angle){
		double TD = Domestic.getTDErorr(comp);
		double output = Environment.getSigmoidOutput(1);
		double states[] = {output,speed,angle};
		for(Actor act : ActorList){
			act.updateParameters_Trace(TD,states);
		}
	}
	*/
	
	/**�@Critic���̃p�����[�^�[�̍X�V(�w�K)�@*/
	public void updateCritic(double td_error,double[] states){
		boolean[] updates={false,false};
		Critic.updateParameters_withTDError(td_error,states,updates);
	}
	
	/**�@Critic���̃p�����[�^�[�̍X�V(�����e�J�����@���g�p)�@*/
	public void updateCritic(double td_error,double[][] states){
		boolean[] updates={false,false};
		Critic.updateParameters_withTDError(td_error,states,updates);
	}
	
	/**�@Critic���̃p�����[�^�[�̍X�V(�����e�J�����@���g�p)�@*/
	public void updateCritic_OnlyReward(double comp,double[][] states){
		boolean[] updates={false,false};
		Critic.updateParameters(comp,states,updates);
	}
	
	/** Critic��ScoreList�̃T�C�Y��Ԃ����\�b�h */
	public int getScoreList_Critic(){
		return Critic.ScoreListSize();
	}
	
	/** �t�B�[���h���̏����� */
	public void format(){
		for(Actor act : ActorList){
			act.formatActionList();
		}
		Critic.formatScoreList();
	}
	
	/** Actor���̕��ϗp����x�N�g����Ԃ����\�b�h.������ActorList�̑Ώۂ�index */
	public double[] getAve_Vectors(int index){
		return ActorList.get(index).getAve_Vectors();
	}
	
	/** Actor���̕��U�p����x�N�g����Ԃ����\�b�h.������ActorList�̑Ώۂ�index  */
	public double getDis_Vector(int index){
		return ActorList.get(index).getDis_Vector();
	}
	
}

/** Actor����\���N���X */
class Actor{
	private double[] Ave_Vectors;  //���ϗp����x�N�g��
	//private double[] Ave_Rests;
	private double Dis_Vector;  //���U�p����x�N�g��
	private double Dis_Coefficient;
	private final double Vector_Min;  /* �x�N�g���v�f�̍ŏ��l */
	private final double Vector_Max;  /* �x�N�g���v�f�̍ő�l */
	private final double MaxAction; /* �s���̍ő�l */
	private final double MinAction; /* �s���̍ŏ��l */
	private ArrayList<Double> ActionList = new ArrayList<Double>();  /** �s���̗������X�g */
	private final double learning=0.01;    //actor�̊w�K�萔
	private double[] Mins_State; /**�e��Ԃ̍ŏ��l���܂Ƃ߂��z��*/
	private double[] Maxs_State; /**�e��Ԃ̍ő�l���܂Ƃ߂��z��*/
	private static MTRandom Random = new MTRandom(111);  /** ���K���z���g������l���z���g�������߂邽�߂̗���(80) */
	private static MTRandom RandomAction = new MTRandom(2500); /** �s�������̍ۂɗ��p���闐��(100) */
	private final double epsilon = Parameters.Agent_RandomAction;  /** �s���������_���s���ɂ���m�� */
	private double[] ProperPoints; /** �e����x�N�g���̓K���x�̗��� */
	private final double beta=0.9; /** �K���x�̗����̊����� */	
	private double[] Splits_Vectors; /** ����x�N�g�����쐬����ۂɎg�p����z��B�v�f�ɂ͔z���������i�[�B */
	private final int LimitListSize=1; /** ActionList�Ɋi�[�ł���v�f�� */
	
	public Actor(double min,double max,double[] mins,double[] maxs,double[] splits){
		
		int nums = mins.length;
		
		/* �萔�̐ݒ� */
		MaxAction=max;
		MinAction=min;
		Vector_Min = -2;
		Vector_Max = 2;
		
		/* �O���[�o���ϐ��̐ݒ� */
		//Mins_State��Maxs_State�ɒl��^���� 
		Mins_State = new double[nums];
		Maxs_State = new double[nums];
		Ave_Vectors = new double[nums];
		ProperPoints = new double[nums+1];
		Splits_Vectors = new double[nums];
		//Ave_Rests = new double[nums];
		for(int i=0;i<nums;i++){
			Mins_State[i] = mins[i];
			Maxs_State[i] = maxs[i];
			Splits_Vectors[i] = splits[i];
			Ave_Vectors[i]=0;
			//System.out.println(i+"�Ԗڂ̗v�f,"+"max:"+Maxs_State[i]+",min:"+Mins_State[i]);
			//Ave_Rests[i]=max/nums-Ave_Vectors[i]*maxs[i];
		}
		for(int i=0;i<ProperPoints.length;i++){
			ProperPoints[i]=0;
		}
		int n=2;
		double sigma=(max-min)/(2*n);
		Dis_Vector = 0;
		Dis_Coefficient = 2*sigma;
	}
	
	/** �s����Ԃ����\�b�h */
	public double getAction(double[] array){
		double random = Random.nextDouble();
		double act;
		if(random<epsilon) act=randomAction();
		else act=normalizedAction(array);
		addActionList(act);
		return act;
	}
	
	/** ���K���z�Ɋ�Â��čs�������肷�郁�\�b�h */
	private double normalizedAction(double[] array){
		double[] states = exchangeArray(array);
		double ave = getAve(states);
		//System.out.println("ave:"+ave);
		double sigma = getSigma();
		//System.out.println("sigma:"+sigma);
		double act=Parameters.NormalDistribution(ave, sigma);
		//System.out.println("act:"+act);
		if(act>MaxAction) act=MaxAction;
		if(act<MinAction) act=MinAction;
		return act;
	}
	
	/** �����_���ɍs����Ԃ����\�b�h */
	private double randomAction(){
		double random = RandomAction.nextDouble();
		double gap = MaxAction-MinAction;
		return (gap*random+MinAction);
	}
	
	
	/** ���K���z�̕��ς�Ԃ����\�b�h */
	private double getAve(double[] states){
		double ave=0;
		for(int i=0;i<states.length;i++){
			ave+=(Ave_Vectors[i]*states[i]);
			//System.out.println(i+"�Ԗڂ̗v�f");
			//System.out.println("vec:"+Ave_Vectors[i]);
			//System.out.println("state:"+states[i]);
			//System.out.println("rest:"+Ave_Rests[i]);
		}
		return ave;
	}
	
	/** ���K���z�̕��U��Ԃ����\�b�h */
	private double getSigma(){
		return Dis_Coefficient/(1+Math.exp(-1.0*Dis_Vector));
	}
	
	/** ActionList�̐擪�Ɉ����v�f��ǉ����鏈�� */
	private void addActionList(double act){
		ActionList.add(0,act);
		if(ActionList.size()>LimitListSize)
			ActionList.remove( ActionList.size()-1 );
	}
	
	/** �e�p�����[�^�[�̍X�V���s�����\�b�h */
	public void updateParameters(double[] tds,double[] array){
		double[] states=exchangeArray(array);
		double ave=getAve(states);
		double act=ActionList.get(0);
		double sigma=getSigma();
		for(int i=0;i<states.length;i++){
			double td_real=tds[i]/Parameters.MaxReward;   //td_real=tds[i]/Parameters.MaxReward;
			Ave_Vectors[i]+=learning*td_real*(act-ave)*states[i];
			if(Ave_Vectors[i]>Vector_Max)Ave_Vectors[i]=Vector_Max;
			if(Ave_Vectors[i]<Vector_Min)Ave_Vectors[i]=Vector_Min;
		}
		Dis_Vector+=learning*(tds[tds.length-1]/Parameters.MaxReward)*(Math.pow(act-ave,2)-Math.pow(sigma, 2))*(1-sigma);
	}
	
	/** �e�p�����[�^�[�̍X�V���s�����\�b�h(�K���x�̗������g�p) */
	public void updateParameters_Trace(double[] tds,double[] array){
		double[] states=exchangeArray(array);
		double ave=getAve(states);
		double act=ActionList.get(0);
		double sigma=getSigma();
		for(int i=0;i<states.length;i++){
			double td_real=tds[i]/Parameters.MaxReward;  //td_real=tds[i]/Parameters.MaxReward;
			double e=(act-ave)*states[i];
			ProperPoints[i] = e+beta*ProperPoints[i];
			Ave_Vectors[i]+=learning*td_real*ProperPoints[i];
			if(Ave_Vectors[i]>Vector_Max)Ave_Vectors[i]=Vector_Max;
			if(Ave_Vectors[i]<Vector_Min)Ave_Vectors[i]=Vector_Min;
		}
		double e=(Math.pow(act-ave,2)-Math.pow(sigma, 2))*(1-sigma);
		ProperPoints[ProperPoints.length-1] = e+beta*ProperPoints[ProperPoints.length-1];
		Dis_Vector+=learning*(tds[tds.length-1]/Parameters.MaxReward)*ProperPoints[ProperPoints.length-1];
	}
	
	/** ProperPoints�̊e�v�f�̍X�V�̂ݍs�����\�b�h */
	public void updateProperPoints(){
		//TODO �����̋L�q
	}
	
	
	
	/** ��Ԃ̔z�������x�N�g���ɕϊ����郁�\�b�h */
	private double[] exchangeArray(double[] array){
		double[] normalized = new double[array.length];
		for(int i=0;i<normalized.length;i++){
			if(Splits_Vectors[i] == 0){
				normalized[i]=0;
				continue;
			}
			double max = MaxAction*Splits_Vectors[i];
			double min = MinAction*Splits_Vectors[i];
			double slope = (max-min)/(Maxs_State[i]-Mins_State[i]);
			normalized[i] = slope*(array[i]-Mins_State[i])+min;
			/*
			if(normalized[i]>max){
				System.out.println(i+"�ԖځF"+normalized[i]);
				System.out.println("�����F"+Splits_Vectors[i]);
				System.out.println("��ԍő�l�F"+Maxs_State[i]);
				System.out.println("��ԍŏ��l�F"+Mins_State[i]);
				System.out.println("��Ԃ̒l�F"+array[i]);
			}
			*/
		}
		return normalized;
	}
	
	/** ActionList�̏����� */
	public void formatActionList(){
		ActionList.clear();
	}
	
	/** �ŋ߂̍s����Ԃ����\�b�h */
	public double getLeastAction(){
		return ActionList.get(0);
	}
	
	/** ���ϗp����x�N�g�����擾���郁�\�b�h */
	public double[] getAve_Vectors(){
		return Ave_Vectors;
	}
	
	/** ���U�p����x�N�g�����擾���郁�\�b�h */
	public double getDis_Vector(){
		return Dis_Vector;
	}
	
}
