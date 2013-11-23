package sim.brain;

import java.util.ArrayList;
import java.util.HashMap;

import sim.MTRandom;
import sim.Parameters;

/** ��ԃN���X�^��\�����Ă���N���X */
public class StateCluster {
	private StateRange[] BasicRange;  /** ��ԃN���X�^�̊�{�I�ȗ̈�(���ƂŃ��X�g�ɕύX���邩��)*/
	private HashMap<Integer,StateRange[]> ExceptionRanges = new HashMap<Integer,StateRange[]>();  /** BasicRange���ɂ��邪��O�I�ɔ͈͊O�ƂȂ�̈��v�f�Ƃ��郊�X�g */
	private ArrayList<State> StateList = new ArrayList<State>();  /** ���̏�ԃN���X�^�ɑ����Ă����ԌQ */
	//private long Total_State = 0;  /** ���܂łɌo��������Ԃ̗݌v�� */
	private ActorCritic DecisionMaker;  /** �s���̑I�����s���I�u�W�F�N�g�Bactor-critic�A���S���Y�����g�p */
	private static int Total = 0; /** ��ԃN���X�^�����܂łɍ쐬���ꂽ�� */
	private final int ID;  /** ���̃N���X�^�̔ԍ� */
	private final double decay = 0.9; /** �����l */
	private MTRandom RandomCollect = new MTRandom(2918); //�����_���ɑ�\�̏�Ԃ��W�߂鎞�Ɏg������
	private Integer Step_Least = null;  //�Ō�ɂ��̃N���X�^�ɂƂǂ܂��Ă���step���L�^
	
	public StateCluster(double[][] ranges){
		Total++;
		ID = Total;
		BasicRange = new StateRange[ranges.length];
		for(int i=0 ; i<BasicRange.length ; i++){			
			BasicRange[i] = new StateRange(ranges[i][0],ranges[i][1]);
		}
		DecisionMaker = new ActorCritic("Cluster_No"+ID);
		//System.out.println("���v�N���X�^���F"+Total);
	}
	
	/** �����̏�Ԃ̉��l�̐�����s�����\�b�h */
	public void runCritic(double[] state_inside){
		DecisionMaker.runCritic(state_inside);
	}
	
	/** ���ۂɍs���s�����擾���郁�\�b�h */
	public double[] getAction(double[] state){
		return DecisionMaker.selectAction(state);
	}
	
	/** ActorCritic�̊w�K���s�����\�b�h(�O�܂Ŏg���Ă������\�b�h) */
	public void updateActorCritic(int n,double comp,double td,double[] past){
		double td_error =  Math.pow(decay,n) * td;
		DecisionMaker.updateActor(td_error, past);
		DecisionMaker.updateCritic(td_error, past);
		DecisionMaker.addEXP();
	}
	
	/** ActorCritic�̊w�K���s�����\�b�h */
	public void updateActorCritic(double td){
		if(StateList.size()==0)return;
		double[] past = StateList.get(0).getInsideState();
		DecisionMaker.updateActor(td, past);
		DecisionMaker.updateCritic(td, past);
		DecisionMaker.addEXP();
	}
	
	/** ActorCritic�̊w�K���s�����\�b�h(�����e�J�����@���g�p) */
	public void updateActorCritic_MonteCarlo_WithTDError(double td){
		if(StateList.size()==0)return;
		double[][] pasts = new double[StateList.size()][StateList.get(0).getInsideState().length];
		for(int i=0;i<pasts.length;i++){
			State elem = StateList.get(i);
			pasts[i] = elem.getInsideState();
		}
		DecisionMaker.updateActor(td, pasts[0]);
		DecisionMaker.updateCritic(td, pasts);
		DecisionMaker.addEXP();
	}
	
	/** ActorCritic�̊w�K���s�����\�b�h(�����e�J�����@���g�p) */
	public void updateActorCritic_MonteCarlo_OnlyReward(double comp,boolean goal){
		if(StateList.size()==0)return;
		double[][] pasts = new double[StateList.size()][StateList.get(0).getInsideState().length];
		for(int i=0;i<pasts.length;i++){
			State elem = StateList.get(i);
			pasts[i] = elem.getInsideState();
		}
		DecisionMaker.updateCritic_OnlyReward(comp, pasts);
		DecisionMaker.updateActor_OnlyReward(comp, pasts[0]);
		if(goal){
			DecisionMaker.addEXP(StateList.size());
			DecisionMaker.addGoalCount();
		}
		else{
			DecisionMaker.addEXP();
		}
	}
	
	/** DecisionMaker�̃t�B�[���hGoalCount��1�����Z���郁�\�b�h */
	public void addGoalCount_DecisionMaker(){
		DecisionMaker.addGoalCount();
	}
	
	/** ActorCritic�̊w�K���s�����\�b�h */
	/*
	public void updateActorCritic_OnlyReward(double comp,int nums){
		if(StateList.size()==0)return;
		double[][] pasts = new double[nums][StateList.get(0).getInsideState().length];
		for(int i=0;i<pasts.length;i++){
			State elem = StateList.get(i);
			pasts[i] = elem.getInsideState();
		}
		DecisionMaker.updateCritic_OnlyReward(comp, pasts);
		DecisionMaker.updateActor_OnlyReward(comp, pasts[0]);
		DecisionMaker.addEXP(StateList.size());
	}
	*/
	
	/** �^����ꂽ��V�Ə�Ԃɑ΂���TD�덷��Ԃ����\�b�h */
	public double getTDError(double comp,double[] states){
		return DecisionMaker.getTDError(comp, states);
	}
	
	/** �����̏�Ԃ���ԃN���X�^���ɂ��邩�ǂ����𔻒肷�郁�\�b�h */
	//TODO �����̏�Ԃ���O�͈͓��̏ꍇ�̏���
	public boolean isInCluster(double[] states){
		if(!isInRange(BasicRange,states)){
			//System.out.println("�͈͊O");
			return false; //��Ɋ�{�I�ȗ̈�ɑ����Ă��邩�ǂ����̃`�F�b�N���s��
		}
		//��O�̈�̃`�F�b�N
		for(StateRange[] ranges : ExceptionRanges.values()){
			if(isInRange(ranges,states)){
				//System.out.println("min_x:"+ranges[0].getMin()+",max_x:"+ranges[0].getMax());
				//System.out.println("min_y:"+ranges[1].getMin()+",max_y:"+ranges[1].getMax());
				//System.out.println("��O�̈��");
				return false;
			}
		}
		return true;
	}
	
	/** ��2�����̏�Ԃ���1�����͈͓̔��ɂ��邩�ǂ����𔻒肷�郁�\�b�h */
	private boolean isInRange(StateRange[] ranges,double[] states){
		for(int i=0;i<ranges.length;i++){
			if(!ranges[i].isInRange(states[i])) return false;
		}
		return true;
	}
	
	/** ���̃N���X�^�̊g�����s�����\�b�h.�Ԃ�l�͊g�����\���ǂ����D */
	public void enlarge(double[] states){
		//�N���X�^�͈͂̊g������
		for(int i=0;i<BasicRange.length;i++){
			BasicRange[i].enlargeRange(states[i]);
		}
	}
	
	/** ���̃N���X�^�͈̔͂�񎟌��z��ɕϊ����ĕԂ����\�b�h */
	public double[][] getRange(){
		double[][] range = new double[BasicRange.length][2];
		for(int i=0;i<range.length;i++){
			range[i][0] = BasicRange[i].getMin();
			range[i][1] = BasicRange[i].getMax();
		}
		return range;
	}
	
	/** ���̃N���X�^�̃T�C�Y��Ԃ����\�b�h */
	public double getSimpleClusterSize(){
		double size=0;
		for(int i=0;i<BasicRange.length;i++){
			double min = BasicRange[i].getMin();
			double max = BasicRange[i].getMax();
			double gap = (max-min);
			if(i==0)size = gap;
			else size = size*gap;
		}
		return size;
	}
	
	/** ���̃N���X�^��ID��Ԃ����\�b�h */
	public int getID(){
		return ID;
	}
	
	/** ���̃N���X�^��ID�������Ɠ��ꂩ�ǂ����𔻒f���郁�\�b�h */
	public boolean isSameID(int id){
		return (ID==id);
	}
	
	/** StateList�̊O����Ԃ��Q�����z��ɕϊ����ĕԂ����\�b�h */
	public double[][] getOutsideStateList(){
		return getOutsideStateList(0,StateList.size());
		
		//�����_���ɏ�Ԃ��擾���Ă����p�^�[��
		/*
		int num = 100;
		double[][] outsides = new double[num][BasicRange.length];
		int index=0;
		double[] gaps = new double[BasicRange.length];
		double[] mins = new double[BasicRange.length];
		for(int i=0;i<BasicRange.length;i++){
			gaps[i] = BasicRange[i].getMax()-BasicRange[i].getMin();
			mins[i] = BasicRange[i].getMin();
		}
		int now=0;
		int limit = 10*num;
		while(index<num){
			now++;
			if(now>limit)return null;
			double[] state_elem = new double[BasicRange.length];
			for(int i=0;i<state_elem.length;i++){
				double random = RandomCollect.nextDouble();
				state_elem[i] = mins[i] + random*gaps[i];
			}
			//��O�̈�̃`�F�b�N
			boolean ContinueFlag = false;
			for(StateRange[] ranges : ExceptionRanges.values()){
				if(isInRange(ranges,state_elem)){
					ContinueFlag=true;
					break;
				}
			}
			if(ContinueFlag)continue;
			outsides[index] = state_elem;
			index++;
		}
		return outsides;
		*/
		
		//�i�q��ɏ�Ԃ��擾���Ă����p�^�[��
		/*
		int width=10;
		int height=10;
		double[][] outsides = new double[width*height][BasicRange.length];
		double interval_x = (BasicRange[0].getMax()-BasicRange[0].getMin())/(width-1);
		double interval_y = (BasicRange[1].getMax()-BasicRange[1].getMin())/(height-1);
		double min_x = BasicRange[0].getMin();
		double min_y = BasicRange[1].getMin();
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				outsides[y*width+x][0] = min_x + x*interval_x;
				outsides[y*width+x][1] = min_y + y*interval_y;
			}
		}
		return outsides;
		*/
		
	}
	
	/** StateList�̊O����Ԃ��Q�����z��ɕϊ����ĕԂ����\�b�h */
	public double[][] getOutsideStateList_Random(){
		//�����_���ɏ�Ԃ��擾���Ă����p�^�[��
		int num = 100;
		double[][] outsides = new double[num][BasicRange.length];
		int index=0;
		double[] gaps = new double[BasicRange.length];
		double[] mins = new double[BasicRange.length];
		for(int i=0;i<BasicRange.length;i++){
			gaps[i] = BasicRange[i].getMax()-BasicRange[i].getMin();
			mins[i] = BasicRange[i].getMin();
		}
		int now=0;
		int limit = 10*num;
		while(index<num){
			now++;
			if(now>limit)return null;
			double[] state_elem = new double[BasicRange.length];
			for(int i=0;i<state_elem.length;i++){
				double random = RandomCollect.nextDouble();
				state_elem[i] = mins[i] + random*gaps[i];
			}
			//��O�̈�̃`�F�b�N
			boolean ContinueFlag = false;
			for(StateRange[] ranges : ExceptionRanges.values()){
				if(isInRange(ranges,state_elem)){
					ContinueFlag=true;
					break;
				}
			}
			if(ContinueFlag)continue;
			outsides[index] = state_elem;
			index++;
		}
		return outsides;
	}
	
	
	/** StateList�̊O����Ԃ��Q�����z��ɕϊ����ĕԂ����\�b�h */
	public double[][] getOutsideStateList(int start){
		return getOutsideStateList(start,StateList.size()-start);
	}
	
	/** StateList�̊O����Ԃ��Q�����z��ɕϊ����ĕԂ����\�b�h */
	public double[][] getOutsideStateList(int start,int size){
		//StateList����O����Ԃ̔z����擾����
		if(StateList.size()-start <= 0)return null;
		if(size>StateList.size()-start) size = StateList.size()-start;
		State state = StateList.get(0);
		double[][] outsides = new double[size][state.getOutsideState().length];
		for(int i=0;i<size;i++){
			State elem = StateList.get(i+start);
			outsides[i] = elem.getOutsideState();
		}
		return outsides;
	}
	
	/** ���̏�ԃN���X�^�Ɣ͈͂�����Ă��܂��ꍇ�A���̃N���X�^��ID��ExceptionRanges�ɒǉ����郁�\�b�h�B�N���X�^�͈͂����ꍇ��true���A�����łȂ��ꍇ��false��Ԃ� */
	public boolean addExceptionRanges(int id,double[][] range){
		StateRange[] exceptions = new StateRange[range.length];
		for(int i=0;i<range.length;i++){
			exceptions[i] = new StateRange(range[i][0],range[i][1]);
			StateRange R = BasicRange[i];
			double max = R.getMax();
			double min = R.getMin();
			if( range[i][1]<min || max<range[i][0] )return false;
			else{
				if(range[i][0]<min && min<range[i][1])exceptions[i].setMin(min);
				if(range[i][0]<max && max<range[i][1])exceptions[i].setMax(max);
			}
		}
		double distance_basic = 0;
		double distance_exception = 0;
		for(int i=0;i<exceptions.length;i++){
			distance_basic += BasicRange[i].getDistance();
			distance_exception += exceptions[i].getDistance();
		}
		if(distance_exception>=distance_basic)return false;
		ExceptionRanges.put(id, exceptions);
		return true;
	}
	
	/** ������ID���L�[�Ƃ���y�A��ExceptionRanges���ɂ��邩�ǂ����𔻒f���郁�\�b�h */
	public boolean isInExceptionRanges(int id){
		return ExceptionRanges.containsKey(id);
	}
	
	/** ������ID���L�[�Ƃ���y�A��ExceptionRanges������폜���郁�\�b�h */
	public void removeFromExceptionRanges(int[] ids){
		for(int i=0;i<ids.length;i++){
			ExceptionRanges.remove(ids[i]);
		}
	}
	
	/** �������StateList�̌�����s�����\�b�h */
	/*
	public void setStateList(StateCluster cl1,StateCluster cl2){
		ArrayList<State> states_cl1 = cl1.StateList;
		ArrayList<State> states_cl2 = cl2.StateList;
		int length1 = states_cl1.size();
		int length2 = states_cl2.size();
		if(length1+length2<Parameters.Max_State_Numbers){
			StateList.addAll(states_cl1);
			StateList.addAll(states_cl2);
		}
		else{
			int length2_new = (int)Math.round(Parameters.Max_State_Numbers*length2/(length1+length2));
			int length1_new = Parameters.Max_State_Numbers - length2_new;
			for(int i=0;i<length1_new;i++){
				StateList.add(states_cl1.get(i));
			}
			for(int i=0;i<length2_new;i++){
				StateList.add(states_cl2.get(i));
			}
		}
		Total_State+=cl1.Total_State;
		Total_State+=cl2.Total_State;
	}
	*/
	
	/** �����̃N���X�^��actor-critic���g�p���郁�\�b�h */
	public void setDicisionMaker(StateCluster cl){
		DecisionMaker.setActorCritic(cl.DecisionMaker);
	}
	
	/** ������ɂǂ���̃N���X�^��actor-critic���g�p���邩���肷�郁�\�b�h */
	public void setDecisionMaker(StateCluster cl1,StateCluster cl2){
		//StateRange[] ranges_cl1 = cl1.BasicRange;
		//StateRange[] ranges_cl2 = cl2.BasicRange;
		int goal_cl1 = cl1.DecisionMaker.getGoalCount();
		int goal_cl2 = cl2.DecisionMaker.getGoalCount();
		long exp_cl1 = cl1.DecisionMaker.getEXP();
		long exp_cl2 = cl2.DecisionMaker.getEXP();
		
		//int ListSize1 = cl1.DecisionMaker.getScoreList_Critic();
		//int ListSize2 = cl2.DecisionMaker.getScoreList_Critic();
		
		/*
		for(int i=0;i<BasicRange.length;i++){
			size1 += ranges_cl1[i].getMax()-ranges_cl1[i].getMin();
			size2 += ranges_cl2[i].getMax()-ranges_cl2[i].getMin();
		}
		*/
		//if(ListSize1<ListSize2){
		//	DecisionMaker.setActorCritic(cl2.DecisionMaker);
		//}
		//else if(ListSize2<ListSize1){
		//	DecisionMaker.setActorCritic(cl1.DecisionMaker);
		//}
		if(goal_cl1<goal_cl2){
			DecisionMaker.setActorCritic(cl2.DecisionMaker);
		}
		else if(goal_cl2<goal_cl1){
			DecisionMaker.setActorCritic(cl1.DecisionMaker);
		}
		else{
			if(exp_cl1<exp_cl2) DecisionMaker.setActorCritic(cl2.DecisionMaker);
			else DecisionMaker.setActorCritic(cl1.DecisionMaker);
		}	
	}
	
	/** StateList�Ɉ����̏�Ԃ�ǉ����郁�\�b�h */
	public void addStateList(State state){
		//Total_State++;
		StateList.add(0,state);
		if(StateList.size()>Parameters.Max_State_Numbers){
			StateList.remove(StateList.size()-1);
		}
	}
	
	/** �����͈͓̔��̑S�Ă̏�Ԃ�StateList���珜�O���郁�\�b�h */
	public void removeState(double[][] ranges){
		int I=0;
		while(I<StateList.size()){
			State state = StateList.get(I);
			double[] outsides = state.getOutsideState();
			int index;
			for(index=0; index<ranges.length; index++){
				if( ranges[index][0] > outsides[index] || outsides[index] > ranges[index][1] )
					break;
			}
			if(index==ranges.length)StateList.remove(state);
			else I++;
		}
	}
	
	
	/** ��P�����̃N���X�^�ɑ�Q�����̃N���X�^�̏��������Ԃ�n�����\�b�h */
	/*
	public static void moveState(StateCluster receive,StateCluster send){
		ArrayList<State> states_send = send.StateList;
		ArrayList<State> states_receive = receive.StateList;
		int index=0;
		while(index<states_send.size()){
			State S = states_send.get(index);
			double[] elem = S.getOutsideState();
			if(receive.isInCluster(elem)){
				states_receive.add(S);
				states_send.remove(S);
			}
			else index++;
		}
	}
	*/
	
	/** ��P�����̂Ƒ�Q�����̃N���X�^�̋������Z�o���ĕԂ����\�b�h */
	public static double getDistance(StateCluster cl1,StateCluster cl2){
		double distance=0;
		StateRange[] range_cl1 = cl1.BasicRange;
		StateRange[] range_cl2 = cl2.BasicRange;
		for(int i=0;i<range_cl1.length;i++){
			double min1 = range_cl1[i].getMin();
			double max1 = range_cl1[i].getMax();
			double min2 = range_cl2[i].getMin();
			double max2 = range_cl2[i].getMax();
			if((min2<min1 && min1<max2) || (min2<max1 && max1<max2))continue;
			if(max1>max2)distance+=(min1-max2);
			else distance+=(min2-max1);
		}
		return distance;
	}
	
	/** ��P�����Ƒ�Q�����̃N���X�^�𓝍��������̃N���X�^�͈͂�Ԃ����\�b�h */
	public static double[][] getNewRanges(StateCluster cl1,StateCluster cl2){
		StateRange[] range_cl1 = cl1.BasicRange;
		StateRange[] range_cl2 = cl2.BasicRange;
		double[][] NewRanges = new double[range_cl1.length][2];
		for(int i=0;i<range_cl1.length;i++){
			double min1 = range_cl1[i].getMin();
			double max1 = range_cl1[i].getMax();
			double min2 = range_cl2[i].getMin();
			double max2 = range_cl2[i].getMax();
			if(min1<min2) NewRanges[i][0] = min1;
			else NewRanges[i][0] = min2;
			if(max1<max2) NewRanges[i][1] = max2;
			else NewRanges[i][1] = max1;
		}
		return NewRanges;
	}
	
	/** ExceptionRanges�̒��g����ɂ��郁�\�b�h */
	public void formatExceptionRanges(){
		ExceptionRanges.clear();
	}
	
	/** �������Ă���actor-critic�̊w�K����Ԃ����\�b�h */
	public long getLearnedNum(){
		return DecisionMaker.getEXP();
	}
	
	/** �������Ă���actor-critic�̃S�[���񐔂�Ԃ����\�b�h */
	public int getReachedGoal(){
		return DecisionMaker.getGoalCount();
	}
	
	/** ���̃I�u�W�F�N�g�̎��������X�g�����������郁�\�b�h */
	public void format(){
		Step_Least=null;
		StateList.clear();
		//Total_State = 0;
		DecisionMaker.format();
	}
	
	/** ActorList��index�Ԗڂ�Actor���̕��ϗp�x�N�g�����擾���郁�\�b�h */
	public double[] getAve_Vectors(int index){
		return DecisionMaker.getAve_Vectors(index);
	}
	
	/** ActorList��index�Ԗڂ�Actor���̕��U�p�x�N�g�����擾���郁�\�b�h */
	public double getDis_Vector(int index){
		return DecisionMaker.getDis_Vector(index);
	}
	
	public Integer getStep_Least(){
		return Step_Least;
	}
	
	public void setStep_Least(int step){
		Step_Least = step;
	}
	
}

/** ��ԃN���X�^�͈̔�(1����)��\�����Ă���N���X */
class StateRange{
	private double Min; //�ŏ��l
	private double Max; //�ő�l
	
	public StateRange(double min,double max){
		Min = min;
		Max = max;
	}
	
	/** �����̒l���͈͓��ɂ��邩�ǂ����𔻕ʂ��郁�\�b�h */
	public boolean isInRange(double x){
		return (Min<=x && x<=Max);
	}
	
	/** �����̒l�܂ŃN���X�^�͈͂��g�������郁�\�b�h */
	public void enlargeRange(double x){
		if(x<Min)Min=x;
		else if(Max<x)Max=x;
	}
	
	/** �͈͂̍ŏ��l��Ԃ����\�b�h */
	public double getMin(){
		return Min;
	}
	
	/** �͈͂̍ő�l��Ԃ����\�b�h */
	public double getMax(){
		return Max;
	}
	
	/** Min�̒l�������̒l�ɂ��郁�\�b�h */
	public void setMin(double min){
		Min = min;
	}
	
	/** Max�̒l�������̒l�ɂ��郁�\�b�h */
	public void setMax(double max){
		Max = max;
	}
	
	/** Max��Min�̍���Ԃ����\�b�h */
	public double getDistance(){
		return (Max-Min);
	}
	
}