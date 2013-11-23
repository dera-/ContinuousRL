package sim.brain;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import sim.Parameters;

/** �G�[�W�F���g�̎v�l���� */
public class NewBrain {
	private RbfNetwork Environment;  /** ���̏�Ԃ̉��l�𐄑�����RBF�l�b�g���[�N */
	private ArrayList<StateCluster> ClusterList = new ArrayList<StateCluster>(); /** ��ԃN���X�^���X�g */
	private ArrayList<Integer> ClusterNumberList = new ArrayList<Integer>();  /** �G�[�W�F���g�����݂��Ă�����ԃN���X�^�̗��� */
	private ArrayList<State> StateList = new ArrayList<State>(); /** �G�[�W�F���g���o��������Ԃ̗��� */
	private double[] Ranges_First = {2*Parameters.Radius_Agent,2*Parameters.Radius_Agent}; /** ��ԃN���X�^�̏����N���X�^�͈́i�f�t�H���g��30,30�j */
	private final int HistoricalLimit = Parameters.HistoricalListLimit; /** ClusterNumberList��OutsideStates�̗v�f���̏���l */
	private FileWriter Result_Average; /** �eEpisode�ɂ�����O��RBF�̏o�͒l���������܂��t�@�C���̃I�u�W�F�N�g */
	private double Threshold_Distance = Math.sqrt(2)*Parameters.Radius_Agent;  /** �������邩�ǂ�����臒l */
	private ArrayList<StateCluster> DeletedClusterList = new ArrayList<StateCluster>();  /** ��x�폜���ꂽ��ԃN���X�^�̃��X�g */
	private final int DeletedClustersNum = 1000; /** DeletedClusterList�Ɋi�[�\�ȍő�v�f�� */
	private final long Threshold_Add_DeletedClusterList = 300; /** DeletedClusterList�ɒǉ����鎞�̊w�K�񐔂Ɋւ���臒l(���Ƃ�200) */
	private final int Threshold_GoalCount = 1; /** DeletedClusterList�ɒǉ����鎞�̃S�[���񐔂Ɋւ���臒l */
	//private final int FirstStateNumbers = 9;
	
	private final int OutConfidence_Left = -1; //�����̐M����ԊO�Ɉʒu���邱�Ƃ������ϐ�
	private final int InConfidence = 0;        //�M����ԓ��Ɉʒu���邱�Ƃ������ϐ�
	private final int OutConfidence_Right = 1;  //�E���̐M����ԊO�Ɉʒu���邱�Ƃ������ϐ�
	
	private int NowStep=0; /** ���݂�step */
	//private final int NeccesaryNumbers_Split = 2*FirstStateNumbers;
	
	/** �R���X�g���N�^ */
	public NewBrain(){
		//�O����Ԃ�RBF�l�b�g���[�N�̃p�����[�^�[�Q
		int inputs_estimate[]={1,1};
		double Min_estimate[]={0,0};
		double Max_estimate[]={Parameters.SimulatorWidth,Parameters.SimulatorHeight};
		int Split_estimate[]={20,20};
		
		Environment = new RbfNetwork("Environment",inputs_estimate,Min_estimate,Max_estimate,Split_estimate,inputs_estimate);
		createOutputFile();
		/* �ȉ��e�X�g��ClusterList��\�ߍ쐬 */
		/*
		double crush = Parameters.MaxSpeed/5 + Parameters.Radius_Agent;
		
		double[] x1 = {0,0.4*Parameters.SimulatorWidth};
		double[] x2 = {0.5*Parameters.SimulatorWidth,0.9*Parameters.SimulatorWidth};
		double[] x3 = {0.15*Parameters.SimulatorWidth,0.75*Parameters.SimulatorWidth};
		double[] x4 = {crush,0.9*Parameters.SimulatorWidth-crush};
		double[] x5 = {0,crush};
		double[] x6 = {0.4*Parameters.SimulatorWidth-crush,0.4*Parameters.SimulatorWidth};
		double[] x7 = {0.5*Parameters.SimulatorWidth,0.5*Parameters.SimulatorWidth+crush};
		double[] x8 = {0.9*Parameters.SimulatorWidth-crush,0.9*Parameters.SimulatorWidth};
		double[] x9 = {crush,0.4*Parameters.SimulatorWidth-crush};
		double[] x10 = {0.5*Parameters.SimulatorWidth+crush,0.9*Parameters.SimulatorWidth-crush};
		
		double[] y1 = {0,Parameters.SimulatorHeight};
		double[] y2 = {0,100};
		double[] y3 = {Parameters.SimulatorHeight-crush,Parameters.SimulatorHeight};
		double[] y4 = {100,Parameters.SimulatorHeight};
		double[] y5 = {0,crush};
		
		double[][] range0 = {x1,y1};
		double[][] range1 = {x2,y1};
		double[][] range2 = {x3,y2};
		double[][] range3 = {x9,y3};
		double[][] range4 = {x4,y5};
		double[][] range5 = {x5,y1};
		double[][] range6 = {x6,y4};
		double[][] range7 = {x7,y4};
		double[][] range8 = {x8,y1};
		double[][] range9 = {x10,y3};
		
		StateCluster clust0 = new StateCluster(range0);
		StateCluster clust1 = new StateCluster(range1);
		StateCluster clust2 = new StateCluster(range2);
		StateCluster clust3 = new StateCluster(range3);
		StateCluster clust4 = new StateCluster(range4);
		StateCluster clust5 = new StateCluster(range5);
		StateCluster clust6 = new StateCluster(range6);
		StateCluster clust7 = new StateCluster(range7);
		StateCluster clust8 = new StateCluster(range8);
		StateCluster clust9 = new StateCluster(range9);
		
		clust0.addExceptionRanges(clust2.getID(), clust2.getRange());
		clust0.addExceptionRanges(clust3.getID(), clust3.getRange());
		clust0.addExceptionRanges(clust4.getID(), clust4.getRange());
		clust0.addExceptionRanges(clust5.getID(), clust5.getRange());
		clust0.addExceptionRanges(clust6.getID(), clust6.getRange());
		
		clust1.addExceptionRanges(clust2.getID(), clust2.getRange());
		clust1.addExceptionRanges(clust9.getID(), clust9.getRange());
		clust1.addExceptionRanges(clust4.getID(), clust4.getRange());
		clust1.addExceptionRanges(clust7.getID(), clust7.getRange());
		clust1.addExceptionRanges(clust8.getID(), clust8.getRange());
		
		clust2.addExceptionRanges(clust4.getID(), clust4.getRange());
		
		
		ClusterList.add(clust0);
		ClusterList.add(clust1);
		ClusterList.add(clust2);
		ClusterList.add(clust3);
		ClusterList.add(clust4);
		ClusterList.add(clust5);
		ClusterList.add(clust6);
		ClusterList.add(clust7);
		ClusterList.add(clust8);
		ClusterList.add(clust9);
		*/
		
		/*
		double[] x1 = {0,Parameters.SimulatorWidth};
		double[] y1 = {0,Parameters.SimulatorHeight};
		double[][] range0 = {x1,y1};
		StateCluster clust0 = new StateCluster(range0);
		ClusterList.add(clust0);
		*/
		
		/*
		double[] x1 = {0,Parameters.SimulatorWidth};
		double[] x2 = {0,30};
		double[] x3 = {Parameters.SimulatorWidth-30,Parameters.SimulatorWidth};
		double[] x4 = {Parameters.SimulatorWidth-300,Parameters.SimulatorWidth-50};
		double[] x5 = {50,Parameters.SimulatorWidth-300};
		double[] x6 = {30,Parameters.SimulatorWidth-30};
		
		double[] y1 = {0,Parameters.SimulatorHeight};
		double[] y2 = {0,30};
		double[] y3 = {Parameters.SimulatorHeight-30,Parameters.SimulatorHeight};
		double[] y4 = {Parameters.SimulatorHeight-300,Parameters.SimulatorHeight-50};
		double[] y5 = {50,Parameters.SimulatorHeight-300};
		double[] y6 = {30,Parameters.SimulatorHeight-30};
		
		StateCluster[] clusts = new StateCluster[5];
		double[][] range0 = {x1,y1};
		double[][] range1 = {x1,y2};
		double[][] range2 = {x3,y1};
		double[][] range3 = {x1,y3};
		double[][] range4 = {x2,y1};
		double[][] range5 = {x5,y5};
		double[][] range6 = {x4,y5};
		double[][] range7 = {x5,y4};
		double[][] range8 = {x4,y4};
		double[][] range9 = {x6,y6};
		
		//StateCluster clust0 = new StateCluster(range0);
		//ClusterList.add(clust0);
		clusts[0] = new StateCluster(range1);
		clusts[1] = new StateCluster(range2);
		clusts[2] = new StateCluster(range3);
		clusts[3] = new StateCluster(range4);
		//clusts[4] = new StateCluster(range5);
		//clusts[5] = new StateCluster(range6);
		//clusts[6] = new StateCluster(range7);
		//clusts[7] = new StateCluster(range8);
		clusts[4] = new StateCluster(range9);
		
		for(int i=0;i<clusts.length;i++){
			ClusterList.add(clusts[i]);
		}
		*/
	}
	
	/** �O���̏�Ԃ̉��l�𐄑����郁�\�b�h */
	public void estimateOutsideState(double[] states){
		Environment.runRBF(states);
	}

	/** �����̏�Ԃ̉��l�𐄑����郁�\�b�h */
	public void estimateInsideState(double[] states){
		int id = ClusterNumberList.get(0);
		StateCluster clust = getCluster(id);
		if(clust==null)return; //���̏ꂵ�̂�
		clust.runCritic(states);
	}
	
	/** ���ۂɍs���s�����擾���郁�\�b�h */
	public double[] getAction(double[] state){
		int id = ClusterNumberList.get(0);
		StateCluster clust = getCluster(id);
		//�N���X�^�[�����݂��Ȃ��ꑫ�̓����_���ȍs����Ԃ����Ƃɂ���
		if(clust==null){
			double[] array = {Parameters.getRandomValue(Parameters.ChangeSpeed,-1*Parameters.ChangeSpeed),Parameters.getRandomValue(Parameters.ChangeAngle, -1*Parameters.ChangeAngle)};
			return array;
		}
		return clust.getAction(state);
	}
	
	/** �O�����Ɋւ���RBF�l�b�g���[�N�̃p�����[�^�[�̍X�V(�w�K) */
	public void updateEnvironment(double comp,double[] states){
		boolean[] updates={false,false};
		double[] before = StateList.get(0).getOutsideState();
		Environment.updateParameters(comp,states,before,updates);
	}
	
	/** �O�����Ɋւ���RBF�l�b�g���[�N�̃p�����[�^�[�̍X�V(�����e�J�����@�̏ꍇ) */
	public void updateEnvironment_MonteCarlo(double comp,double[] states){
		boolean[] updates={false,false};
		if(StateList.size()==0)return;
		int states_num = StateList.get(0).getOutsideState().length;
		double[][] BeforeStateArray = new double[StateList.size()][states_num];
		for(int i=0;i<StateList.size();i++){
			BeforeStateArray[i] = StateList.get(i).getOutsideState();
		}
		Environment.updateParameters(comp,states,BeforeStateArray,updates);
	}
	
	/** ���݃G�[�W�F���g�����݂��Ă���N���X�^��Actor-Critic�̊w�K���s�� */
	public void updateOneCluster(double comp,double[] states){
		//updateDomestic(comp,states,1);
		Double TD = getTDError(comp, states);
		if( TD==null || ClusterList.size()==0)return;
		StateCluster cluster = ClusterList.get(0);
		cluster.updateActorCritic(TD.doubleValue());
	}
	
	/** ���݃G�[�W�F���g�����݂��Ă���N���X�^��Actor-Critic�̊w�K���s�� */
	public void updateOneCluster(double comp){
		//updateClusters_MonteCarlo(comp,1);
		//updateClusters_MonteCarlo(comp,1,false,1);
		
		if(ClusterList.size()==0)return;
		StateCluster cluster = ClusterList.get(0);
		cluster.updateActorCritic_MonteCarlo_OnlyReward(comp,false);
	}
	
	/** ClusterNumberList���̑S�ẴN���X�^��Actor-Critic�̊w�K���s�� */
	public void updateAllCluster(double comp,double[] states){
		//updateDomestic(comp,states,ClusterNumberList.size());
		Double TD = getTDError(comp, states);
		if(TD==null)return;
		double TD_Value = TD.doubleValue();
		double lambda = 0.8;
		for(int i=0;i<ClusterList.size();i++){
			StateCluster cluster = ClusterList.get(i);
			double td = TD_Value * Math.pow(lambda,i);
			cluster.updateActorCritic_MonteCarlo_WithTDError(td);
		}
	}
	
	/** ClusterNumberList���̑S�ẴN���X�^��Actor-Critic�̊w�K���s��(��V�l�����Ɏg�p���郁�\�b�h) */
	public void updateAllCluster(double comp){
		updateClusters_MonteCarlo(comp,HistoricalLimit);
		//updateClusters_MonteCarlo(comp,ClusterList.size(),true,0.95);
		/*
		for(StateCluster cluster : ClusterList){
			Integer STEP = cluster.getStep_Least();
			if(STEP==null){
				continue;
			}
			else{
				double lambda = 1.0 - 1.0*(NowStep-STEP.intValue())/NowStep;
				cluster.updateActorCritic_MonteCarlo_OnlyReward(lambda*comp,true);
			}
			//System.out.println("ID:"+id+",Lambda:"+lambda);
		}
		*/
		/*
		double lambda = 0.5;  //�f�t�H��0.8
		for(int i=0;i<nums;i++){
			StateCluster cluster = ClusterList.get(i);
			double Comp_Real = comp * Math.pow(lambda,i);
			cluster.updateActorCritic_MonteCarlo_OnlyReward(Comp_Real);
		}
		*/
	}
	
	/** �w�肳�ꂽ���̃N���X�^��actor-citic�̊w�K���s�� */
	private void updateClusters_MonteCarlo(double comp,int nums,boolean goal,double gamma){
		int length;
		if(nums>ClusterList.size())length=ClusterList.size();
		else length=nums;
		for(int i=0;i<length;i++){
			StateCluster cluster = ClusterList.get(i);
			Integer STEP = cluster.getStep_Least();
			if(STEP==null){
				continue;
				//cluster.updateActorCritic_OnlyReward(min*comp,1);
			}
			else{
				//double lambda = 1.0 - 1.0*(NowStep-STEP.intValue())/NowStep;
				int n = NowStep-STEP.intValue();
				double comp_real = comp*Math.pow(gamma, n);
				cluster.updateActorCritic_MonteCarlo_OnlyReward(comp_real,goal);
				if(n<HistoricalLimit)cluster.addGoalCount_DecisionMaker();
			}
			//System.out.println("ID:"+id+",Lambda:"+lambda);
		}
		
		
		//double lambda = 0.5;  //�f�t�H��0.8
		//for(int i=0;i<nums;i++){
		//	StateCluster cluster = ClusterList.get(i);
		//	double Comp_Real = comp * Math.pow(lambda,i);
		//	cluster.updateActorCritic_MonteCarlo_OnlyReward(Comp_Real);
		//}
	}
	
	/** �w�肳�ꂽ���̃N���X�^��actor-citic�̊w�K���s�� */
	private void updateClusters_MonteCarlo(double comp,int nums){
		HashMap<Integer,Double> HistoricalPair = new HashMap<Integer,Double>();
		int NUMS;
		if(nums>ClusterNumberList.size())NUMS = ClusterNumberList.size();
		else NUMS = nums;
		double max = 1.0;
		double min = 0;
		for(int i=0;i<NUMS;i++){
			if(i>=ClusterNumberList.size())break;
			Integer ID = ClusterNumberList.get(i);
			if(ID==null)continue;
			if(!HistoricalPair.containsKey(ID)){
				double value = max - i*(max-min)/NUMS;
				HistoricalPair.put(ID, value);
			}
			
			//else{
			//	System.out.println(ID.intValue());
			//}
		}
		
		for(StateCluster cluster : ClusterList){
			int id = cluster.getID();
			double lambda;
			if(!HistoricalPair.containsKey(id)){
				continue;
				//cluster.updateActorCritic_OnlyReward(min*comp,1);
			}
			else{
				lambda = HistoricalPair.get(id);
				cluster.updateActorCritic_MonteCarlo_OnlyReward(lambda*comp,true);
			}
			//System.out.println("ID:"+id+",Lambda:"+lambda);
		}
		
		
		//double lambda = 0.5;  //�f�t�H��0.8
		//for(int i=0;i<nums;i++){
		//	StateCluster cluster = ClusterList.get(i);
		//	double Comp_Real = comp * Math.pow(lambda,i);
		//	cluster.updateActorCritic_MonteCarlo_OnlyReward(Comp_Real);
		//}
	}
	
	/** TD�덷���擾���郁�\�b�h */
	private Double getTDError(double comp,double[] state){
		int id_least = ClusterNumberList.get(0);
		StateCluster clust_least = getCluster(id_least);
		if(clust_least==null)return null;
		return clust_least.getTDError(comp, state);
	}
	
	/** 
	 * �e�N���X�^��Actor-Critic�̊w�K 
	 * */
	private void updateDomestic(double comp,double[] states,int num){
		//boolean[] updates={false,false};
		int id_least = ClusterNumberList.get(0);
		StateCluster clust_least = getCluster(id_least);
		if(clust_least==null)return;
		double TD = clust_least.getTDError(comp, states);
		int loop;
		if( num > ClusterNumberList.size() )loop=ClusterNumberList.size();
		else loop=num;
		
		//ArrayList<Integer> list = new ArrayList<Integer>(); //�w�K�ς݂̃N���X�^�̔ԍ���ۑ����郊�X�g
		for(int i=0;i<loop;i++){
			int id = ClusterNumberList.get(i);
			//if( isContain(list,id))continue;
			StateCluster clust = getCluster(id);
			if(clust==null)continue;
			double[] past = StateList.get(i).getInsideState();
			clust.updateActorCritic(i,comp,TD,past);
			//list.add(0,id);
		}
	}
	
	/** ��P�����̃��X�g���ɑ�Q�����̗v�f�����݂��邩�ǂ������`�F�b�N���郁�\�b�h */
	/*
	private boolean isContain(ArrayList<Integer> list,int id){
		for(int elem : list){
			if(id==elem)return true;
		}
		return false;
	}
	*/
	
	
	/** ��ԃN���X�^�̍\�z���s�����\�b�h */
	public void createClusterList(double[] state_outside , double[] state_inside){
		//getCluster(state_outside);
		//System.out.println("�N���X�^�[���F"+ClusterList.size());
		//System.out.println("�폜���ꂽ�N���X�^�[���F"+DeletedClusterList.size());
		NowStep++;
		Integer before=null;
		StateCluster cluster_before = null;
		if(ClusterNumberList.size()>0){
			before = ClusterNumberList.get(0);
			if(before!=null) cluster_before = getCluster(before);
		}
		Integer now = getNowCluster(state_outside);
		//System.out.println("x:"+state_outside[0]+",y:"+state_outside[1]);
		//System.out.println("���݂̔ԍ��F"+now);
		StateCluster cluster_now; //test
		/* �ǉ�or�g�� */
		if(now==null){
			if(cluster_before!=null && canEnlarge(state_outside,cluster_before) ){
				//�N���X�^�̊g��
				cluster_before.enlarge(state_outside);
				decideExceptionRanges(cluster_before);
				cluster_now = cluster_before;
				//System.out.println("�g��");
			}
			else{
				//�V�����N���X�^�̒ǉ�
				cluster_now = createNewCluster(state_outside);
				//System.out.println("�ǉ�");
			}
		}
		/*����or����*/
		else{
			cluster_now = getCluster(now);
			if(!isInConfidence(state_outside,cluster_now)){
				StateCluster clust = searchEnlargeCluster(state_outside);  //��������O�ɋ߂��ɂ��̏�Ԃ܂Ŋg���\�ȃN���X�^���Ȃ����ǂ������ׂ�
				if(clust!=null){
					cluster_now.removeState(clust.getRange());  //��O�̈���̏�Ԃ�S�č폜����
					cluster_now = clust;
				}
				else{
					StateCluster split = splitCluster(state_outside,cluster_now);
					if( split != null ){
						cluster_now = split; //��������
						//System.out.println("����");
					}
				}
			}
			//else{
			//	unifyAllClusters(cluster_now.getID());
			//}
			
			else if(cluster_before!=null && now.intValue()!=before.intValue()){ //��������
				StateCluster unified = judge_unify_Clusters(cluster_before,cluster_now);
				if(unified!=null)cluster_now = unified;
			//	System.out.println("����");
			}
			
		}
		State state = new State(state_outside,state_inside);
		addOutsideStates(state);
		int id;
		if(cluster_now==null){
			id=-1;
			//System.out.println("�͈͖���");
		}
		else {
			id=cluster_now.getID();
			cluster_now.addStateList(state);
			cluster_now.setStep_Least(NowStep);
			//double[][] ranges=cluster_now.getRange();
			//System.out.println("x_min:"+ranges[0][0]+",x_max:"+ranges[0][1]);
			//System.out.println("y_min:"+ranges[1][0]+",y_max:"+ranges[1][1]);
		}
		//System.out.println("�N���X�^�[�ԍ��F"+id);
		addClusterNumberList(id);
		updateClusterList(cluster_now);
		//��������
		/*
		if(ClusterNumberList.size() == HistoricalLimit){
		    int id = ClusterNumberList.get(HistoricalLimit-1);
			unifyAllClusters(id);
		}
		*/
		//State state = new State(state_outside,state_inside);
		//cluster_now.addStateList(state);
		deleteCluster();
		//System.out.println("�N���X�^�[�T�C�Y�F"+ClusterList.size());
	}
	
	/** �����̏�Ԃ��܂ޏ�ԃN���X�^��Ԃ����\�b�h(test)*/
	/*
	private StateCluster getCluster(double[] states){
		ArrayList<StateCluster> list = new ArrayList<StateCluster>();
		for(StateCluster cluster : ClusterList){
			if(cluster.isInCluster(states))list.add(cluster);
		}
		//System.out.println("�������܂ޏ�ԃN���X�^�̐�:"+list.size());  //test
		if(list.size()>1){
			System.out.println("x:"+states[0]+",y:"+states[1]);
			for(int i=0;i<list.size();i++){
				System.out.println(i);
				double[][] ranges = list.get(i).getRange();
				System.out.println("x_min:"+ranges[0][0]+",x_max:"+ranges[0][1]);
				System.out.println("y_min:"+ranges[1][0]+",y_max:"+ranges[1][1]);
			}
		}
		if(list.size()>0)return list.get(0);
		else return null;
	}
	*/
	
	/** �����̏�Ԃ܂Ŋg���\�ȃN���X�^�[��T�����\�b�h */
	public StateCluster searchEnlargeCluster(double[] states){
		ArrayList<StateCluster> list = new ArrayList<StateCluster>();
		ArrayList<Double> list_Distance = new ArrayList<Double>();
		for(StateCluster cluster : ClusterList){
			double[][] ranges = cluster.getRange();
			double distance = 0;
			for(int i=0;i<ranges.length;i++){
				double toMin = Math.abs(states[i]-ranges[i][0]);
				double toMax = Math.abs(states[i]-ranges[i][1]);
				if(toMin<toMax)distance+=toMin;
				else distance+=toMax;
			}
			if( distance > Threshold_Distance )continue;
			int index;
			for( index=0; index<list.size(); index++ ){
				double elem_distance = list_Distance.get(index);
				if(distance<elem_distance){
					list.add(index,cluster);
					list_Distance.add(index,distance);
					break;
				}
			}
			if(index==list.size()){
				list.add(cluster);
				list_Distance.add(distance);
			}
		}
		
		for(StateCluster clust : list){
			if(canEnlarge(states,clust)){
				clust.enlarge(states);
				decideExceptionRanges(clust);
				return clust;
			}
		}
		return null;
	}
	
	/** �S�ẴN���X�^�[������id�̃N���X�^�[�Ɠ��������郁�\�b�h */
	private void unifyAllClusters(int id){
		StateCluster cluster = getCluster(id);
		if(cluster==null)return;
		int ThisIndex = ClusterList.indexOf(cluster);
		int i=0;
		while(i<ClusterList.size()){
			if(i==ThisIndex){
				i++;
				continue;
			}
			StateCluster elem = ClusterList.get(i);
			StateCluster unified = judge_unify_Clusters(cluster,elem);
			if(unified==null){
				i++;
				continue;
			}
			cluster = unified;
			if(i<ThisIndex){
				ThisIndex = i;
				i++;
			}
		}
	}
	

	/** �S�ẴN���X�^�[������id�̃N���X�^�[�Ɠ��������郁�\�b�h */
	/*
	private void unifyAllClusters(int id){
		StateCluster cluster = getCluster(id);
		if(cluster==null)return;
		int ThisIndex = ClusterList.indexOf(cluster);
		int i=0;
		while(i<ClusterList.size()){
			if(i==ThisIndex){
				i++;
				continue;
			}
			StateCluster elem = ClusterList.get(i);
			if(doUnity(cluster,elem)){
				cluster = unifyClusters(cluster,elem);
				if(i<ThisIndex){
					ThisIndex = i;
					i++;
				}
			}
			else i++;
		}
	}
	*/
	
	/** ��P�����̃N���X�^�Ƒ�Q�����̃N���X�^�𓝍��ł��邩�ǂ����𒲂ׂ郁�\�b�h */
	private boolean doUnity(StateCluster cluster1,StateCluster cluster2){
		if(isExceptionArea(cluster1,cluster2)|| isExceptionArea(cluster2,cluster1))return false;
		double[][] states_Cluster1 = cluster1.getOutsideStateList_Random();
		double[][] states_Cluster2 = cluster2.getOutsideStateList_Random();
		if(states_Cluster1==null || states_Cluster2==null)return false; //�Е��̃N���X�^������������ꍇ�͖������œ���������
		//double threshold_distance = 20; //���Ȃ�K���Ɍ��߂��N���X�^�Ԃ̋�����臒l
		//if( states_Cluster1.length < Parameters.Min_State_Numbers || states_Cluster2.length < Parameters.Min_State_Numbers )
			//return false;
		//�N���X�^���m���ߐڂ��Ă��邩�ǂ����̃`�F�b�N
		if( !cluster1.isInExceptionRanges(cluster2.getID()) && !cluster2.isInExceptionRanges(cluster1.getID()) &&
				StateCluster.getDistance(cluster1, cluster2)>Threshold_Distance )
			return false;
		double[] StateValues1 = new double[states_Cluster1.length];  //�N���X�^�P�̊e��Ԃ̐������l���i�[����z��
		double[] StateValues2 = new double[states_Cluster2.length];  //�N���X�^�Q�̊e��Ԃ̐������l���i�[����z��
		for(int i=0;i<StateValues1.length;i++){
			StateValues1[i] = Environment.getRBFOutput_Normalized(states_Cluster1[i]);
		}
		for(int i=0;i<StateValues2.length;i++){
			StateValues2[i] = Environment.getRBFOutput_Normalized(states_Cluster2[i]);
		}
		double ave1 = getAverage(StateValues1);
		double ave2 = getAverage(StateValues2);
		if( isInConfidenceInterval(ave1,StateValues2)!=InConfidence || isInConfidenceInterval(ave2,StateValues1)!=InConfidence )return false;
		return true;
	}
	
	
	/** ��Q�����̃N���X�^�̕������s���邩�ǂ����𒲂ׂ郁�\�b�h */
	private StateCluster splitCluster(double[] states,StateCluster cluster){
		double[][] states_Cluster = cluster.getOutsideStateList_Random();//getOutsideStateList(FirstStateNumbers-1);
		if(states_Cluster==null /*|| states_Cluster.length<FirstStateNumbers*/ )return null;
		double[] StateValues = new double[states_Cluster.length];  //�e��Ԃ̐������l���i�[����z��
		for(int i=0;i<StateValues.length;i++){
			StateValues[i] = Environment.getRBFOutput_Normalized(states_Cluster[i]);
		}
		double ave_values = getAverage(StateValues);
		double diss_values = getDispersion(ave_values,StateValues);
		
		//�ߋ�10STEP�O�̏�Ԃ̉��l�Ɣ�r���āA�ǂ̉��l���M����ԊO�Ɉʒu����ꍇ�͕������s���B
		/*
		double[][] states_least = cluster.getOutsideStateList(0,FirstStateNumbers-1);
		if(states_least==null)return null;
		for(int i=0;i<states_least.length+1;i++){
			double value;
			if(i==states_least.length) value = Environment.getRBFOutput_Normalized(states);
			else value=Environment.getRBFOutput_Normalized(states_least[i]);
			if(isInConfidenceInterval(value,ave_values,diss_values)){
				//System.out.println(i+"��ڂŎ��s");
				//System.out.println("value:"+value+",ave:"+ave_values+",diss:"+diss_values);
				return null;
			}
		}
		
		//�N���X�^�͈͂̌���
		double[][] NewRange = new double[states.length][2];
		for(int j=0;j<NewRange.length;j++){
			NewRange[j][0] = states[j];
		    NewRange[j][1] = states[j];
		}
		
		for(int i=0;i<states_least.length;i++){
			for(int j=0;j<NewRange.length;j++){
				double elem = states_least[i][j];
				if(elem < NewRange[j][0]) NewRange[j][0]=elem;
				else if(elem > NewRange[j][1]) NewRange[j][1]=elem;
			}
		}
		*/
		
		//�N���X�^�͈͂̌���
		double[][] NewRange = new double[states.length][2];
		for(int j=0;j<NewRange.length;j++){
			NewRange[j][0] = states[j];
		    NewRange[j][1] = states[j];
		}
		double maxsize;
		double size_SplitedCluster = cluster.getSimpleClusterSize()/2;
		if( size_SplitedCluster < getMinClusterSize() ) return null;//maxsize = getMinClusterSize();
		else maxsize = size_SplitedCluster;
		int outconfidence = isInConfidenceInterval(Environment.getRBFOutput_Normalized(states),ave_values,diss_values);
		if(outconfidence!=InConfidence){
			
			//StateCluster deleted = searchDeletedClusters(states);
			//if(deleted!=null){
			//	ClusterList.add(0,deleted);
			//	return deleted;
			//}
			decideSplitRange(states,NewRange,cluster.getRange(),ave_values,diss_values,maxsize,outconfidence);
		}
		
		//��O�̈�N���X�^�̍쐬
		StateCluster NewCluster = new StateCluster(NewRange);
		if( NewCluster.getSimpleClusterSize() < getMinClusterSize() ){
			//System.out.println(NewCluster.getSimpleClusterSize());
			return null;
		}
		useActorCriticFormDeletedList(NewCluster,states);
		//NewCluster.setDicisionMaker(cluster); //�w�K���ʂ̈����p��
		decideExceptionRanges(NewCluster); //��O�̈�̌���
		/* NewCluster������N���X�^�́A�V����NewCluster���O�̈�Ƃ��� */
		createExceptionArea(NewCluster);
		//ArrayList<StateCluster> clusts = new ArrayList<StateCluster>();
		//clusts.add(cluster);
		//decideExceptionRanges(NewCluster); //��O�̈�̌���
		//StateCluster.moveState(NewCluster,cluster);
		ClusterList.add(0,NewCluster);
		return NewCluster;
	}
	
	/** ��������ۂ͈̔͂��ċA�I�Ɍ��߂郁�\�b�h */
	private void decideSplitRange(double[] state,double[][] range,double[][] Range_Original,double ave,double diss,double maxsize,int outconfidence){
		//state��Range_Original�͈͓̔����ǂ����̔���
		for(int i=0;i<state.length;i++){
			if( Range_Original[i][0]>state[i] || state[i]>Range_Original[i][1])return;
		}
		//���݂̏�Ԃ̉��l���N���X�^�̐M����ԓ����ǂ����𒲂ׂ�
		double value=Environment.getRBFOutput_Normalized(state);
		if(isInConfidenceInterval(value,ave,diss)!=outconfidence)return;
		
		//�����͈͂̐ݒ�
		//TODO ���͈̔͂�菬�����Ȃ�ꍇ�͍X�V���Ȃ�
		double size=0;
		for(int i=0;i<state.length;i++){
			if(state[i]<range[i][0])range[i][0]=state[i];
			else if(state[i]>range[i][1])range[i][1]=state[i];
			//System.out.println("min:"+range[i][0]+",max:"+range[i][1]);
			double gap=(range[i][1]-range[i][0]);
			if(i==0)size = gap;
			else size = size*gap;
		}
		//System.out.println(size);
		if(size>=maxsize)return;
		
		//state�̃R�s�[�z��̍쐬
		double[] state_copy = new double[state.length];
		for(int i=0;i<state_copy.length;i++){
			state_copy[i] = state[i];
		}
		
		//TODO state�̑S�Ă̗v�f��ω�������
		int interval=3;
		for(int i=0;i<state.length;i++){
			for(int j=-1;j<=1;j+=2){
				state[i] += 1.0*j*interval; 
				//System.out.println("state["+i+"]:"+state[i]);
				if( !isInRange(state,range) ) decideSplitRange(state,range,Range_Original,ave,diss,maxsize,outconfidence);
				state[i] = state_copy[i];
			}
		}
	}
	
	/** ��1�����̏�Ԃ���2�����͈͓̔����ǂ����𔻒肷�郁�\�b�h */
	private boolean isInRange(double[] state,double[][] range){
		for(int i=0;i<range.length;i++){
			if( state[i]<range[i][0] || state[i]>range[i][1] )
				return false;
		}
		return true;
	}
	
	
	/** �N���X�^�[�̍ŏ��T�C�Y��Ԃ����\�b�h */
	private double getMinClusterSize(){
		double MinSize;
		if(Ranges_First.length > 0)MinSize = Ranges_First[0];
		else MinSize=0;
		for(int i=1;i<Ranges_First.length;i++){
			MinSize = MinSize*Ranges_First[i];
		}
		return MinSize;
	}
	
	/** ��Q�����̃N���X�^�̊g�����\���ǂ����𒲂ׂ郁�\�b�h */
	private boolean canEnlarge(double[] states,StateCluster cluster){
		return (isInConfidence(states,cluster) && !isTooLarge(cluster));
	}
	
	/** ��P�����̏�Ԃ̉��l����Q�����̃N���X�^�́u���l�̐M����ԓ��ɓ���v���ǂ����𔻒f���郁�\�b�h */
	private boolean isInConfidence(double[] states,StateCluster cluster){
		double[][] states_Cluster = cluster.getOutsideStateList_Random();
		if(states_Cluster==null)return true; //�N���X�^�[������������ꍇ�͖������Ŋg�傷��
		//if(states_Cluster.length < Parameters.Min_State_Numbers) return true;
		double[] StateValues = new double[states_Cluster.length];  //�e��Ԃ̐������l���i�[����z��
		for(int i=0;i<StateValues.length;i++){
			StateValues[i] = Environment.getRBFOutput_Normalized(states_Cluster[i]);
		}
		double value_now = Environment.getRBFOutput_Normalized(states);
		int can = isInConfidenceInterval(value_now,StateValues);
		return (can==InConfidence);
	}
	
	
	/** ��P�����̒l���M����ԓ��ɓ��邩�ǂ����𔻒肷�郁�\�b�h */
	private int isInConfidenceInterval(double value_now,double[] values){
		double ave = getAverage(values);  //��ԃN���X�^���̏�Ԃ̉��l�̕��ϒl
		//TODO ���U�l����ŕύX���邩��
		double diss = getDispersion(ave,values);  //��ԃN���X�^���̏�Ԃ̉��l�̕��U�l
		return isInConfidenceInterval(value_now,ave,diss);
	}
	
	/** ��P�����̒l���M����ԓ��ɓ��邩�ǂ����𔻒肷�郁�\�b�h */
	private int isInConfidenceInterval(double value_now,double ave,double diss){
		final double beta = 3.0; //1.96;
		//final double interval_test = 0.15;
		double min_confidence = ave - beta*Math.sqrt(diss); //ave - beta*diss;   //�M����Ԃ̉����l
		double max_confidence = ave + beta*Math.sqrt(diss); //ave + beta*diss;   //�M����Ԃ̏���l
		//double diss_threshold = Math.abs(ave);
		//System.out.println("now:"+value_now);
		//System.out.println("min:"+min_confidence);
		//System.out.println("max:"+max_confidence);
		//System.out.println("ave:"+ave);
		//System.out.println("diss:"+diss);
		//if(Math.abs(ave)<Math.pow(10, -4))return true;
		if(value_now < min_confidence)return OutConfidence_Left;   //( min_confidence <= value_now && value_now <= max_confidence );
		else if(value_now<=max_confidence)return InConfidence;
		else return OutConfidence_Right;
	}
	
	/** ������double�z��̕��ϒl��Ԃ����\�b�h */
	private double getAverage(double[] values){
		double ave = 0;
		//���ϒl�̎Z�o
		for(int i=0;i<values.length;i++){
			ave += values[i];
		}
		ave = ave/values.length;
		return ave;
	}
	
	/** ��Q������double�z��̕��U�l��Ԃ����\�b�h */
	private double getDispersion(double ave,double[] values){
		double diss = 0;
		//���U�l�̎Z�o
		for(int i=0;i<values.length;i++){
			diss += Math.pow(values[i]-ave,2);
		}
		diss = diss/values.length;
		return diss;
	}
	
	/** �����N���X�^�̗�O�̈�����肷�郁�\�b�h */
	private void decideExceptionRanges(StateCluster cluster){
		//cluster.formatExceptionRanges();
		for(StateCluster clust : ClusterList){
			if( cluster == clust || clust.isInExceptionRanges(cluster.getID()) )continue;
			cluster.addExceptionRanges(clust.getID(), clust.getRange());
			//if(exist)clust.addExceptionRanges(id,ranges);
		}
	}
	
	/** �����̂Q�̃N���X�^�𓝍������郁�\�b�h */
	
//	private StateCluster unifyClusters(StateCluster cluster1,StateCluster cluster2){
//		/* �V�����N���X�^�̐��� */
//		double[][] ranges = StateCluster.getNewRanges(cluster1, cluster2); //�N���X�^�͈͂̌���
//		StateCluster NewCluster = new StateCluster(ranges);
//		int index1=ClusterList.indexOf(cluster1);
//		int index2=ClusterList.indexOf(cluster2);
//		if( index1<0 || index2<0 )ClusterList.add(0,NewCluster);
//		else if(index1>index2)ClusterList.add(index2,NewCluster);
//		else ClusterList.add(index1,NewCluster);
//		/* StateList�̌��� */
//		//NewCluster.setStateList(cluster1,cluster2);
//		/* DecisionMaker(Actor-Critic)�̌��� */
//		NewCluster.setDecisionMaker(cluster1,cluster2);
//		
//		/* �Q�̃N���X�^��ClusterList���珜�O���� */
//		ClusterList.remove(cluster1);
//		ClusterList.remove(cluster2);
//		
//		/* �Q�̃N���X�^��DeletedClusterList�ɒǉ����� */
//		//TODO
//		/*
//		StateCluster cluster_delete;
//		if(cluster1.getSimpleClusterSize()<cluster2.getSimpleClusterSize())cluster_delete = cluster1;
//		else cluster_delete = cluster2;
//		
//		addDeletedClusterList(cluster1);
//		addDeletedClusterList(cluster2);
//		*/
//		//addDeletedClusterList(cluster_delete);
//		
//		/* ClusterNumberList���̂Q�̃N���X�^��ID�𓝍���̃N���X�^��ID�ɕϊ����� */
//		int id_cl1 = cluster1.getID();
//		int id_cl2 = cluster2.getID();
//		int NewID = NewCluster.getID();
//		for(int i=0;i<ClusterNumberList.size();i++){
//			int id = ClusterNumberList.get(i);
//			if( id==id_cl1 || id==id_cl2 )ClusterNumberList.set(i, NewID);
//		}
//		
//		/* ClusterList���̊e�N���X�^�̗�O�̈�̒�����Q�̃N���X�^�����O���� */
//		int[] array_delete = {cluster1.getID(),cluster2.getID()};
//		for(StateCluster clust : ClusterList){
//			clust.removeFromExceptionRanges(array_delete);
//		}
//		
//		/* �V�����N���X�^�̗�O�̈�̌��� */
//		decideExceptionRanges(NewCluster);
//		
//		/* NewCluster������N���X�^�́A�V����NewCluster���O�̈�Ƃ��� */
//		createExceptionArea(NewCluster);
//		return NewCluster;
//	}
	
	/** �����̂Q�̃N���X�^�������ł��邩�ǂ����̔���Ɠ����𓯎��ɍs�����\�b�h */
	private StateCluster judge_unify_Clusters(StateCluster cluster1,StateCluster cluster2){
		if(!doUnity(cluster1,cluster2))return null;
		/* �V�����N���X�^�̐��� */
		double[][] ranges = StateCluster.getNewRanges(cluster1, cluster2); //�N���X�^�͈͂̌���
		StateCluster NewCluster = new StateCluster(ranges);
		if(isTooLarge(NewCluster))return null;
		int index1=ClusterList.indexOf(cluster1);
		int index2=ClusterList.indexOf(cluster2);
		if( index1<0 || index2<0 )ClusterList.add(0,NewCluster);
		else if(index1>index2)ClusterList.add(index2,NewCluster);
		else ClusterList.add(index1,NewCluster);
		/* StateList�̌��� */
		//NewCluster.setStateList(cluster1,cluster2);
		/* DecisionMaker(Actor-Critic)�̌��� */
		NewCluster.setDecisionMaker(cluster1,cluster2);
		
		/* �Q�̃N���X�^��ClusterList���珜�O���� */
		ClusterList.remove(cluster1);
		ClusterList.remove(cluster2);
		
		/* �Q�̃N���X�^��DeletedClusterList�ɒǉ����� */
		//TODO
		StateCluster cluster_delete;
		if(cluster1.getSimpleClusterSize()<cluster2.getSimpleClusterSize())cluster_delete = cluster1;
		else cluster_delete = cluster2;
		
		//addDeletedClusterList(cluster1);
		//addDeletedClusterList(cluster2);
		addDeletedClusterList(cluster_delete);
		
		/* ClusterNumberList���̂Q�̃N���X�^��ID�𓝍���̃N���X�^��ID�ɕϊ����� */
		int id_cl1 = cluster1.getID();
		int id_cl2 = cluster2.getID();
		int NewID = NewCluster.getID();
		for(int i=0;i<ClusterNumberList.size();i++){
			int id = ClusterNumberList.get(i);
			if( id==id_cl1 || id==id_cl2 )ClusterNumberList.set(i, NewID);
		}
		
		/* ClusterList���̊e�N���X�^�̗�O�̈�̒�����Q�̃N���X�^�����O���� */
		int[] array_delete = {cluster1.getID(),cluster2.getID()};
		for(StateCluster clust : ClusterList){
			clust.removeFromExceptionRanges(array_delete);
		}
		
		/* �V�����N���X�^�̗�O�̈�̌��� */
		decideExceptionRanges(NewCluster);
		
		/* NewCluster������N���X�^�́A�V����NewCluster���O�̈�Ƃ��� */
		createExceptionArea(NewCluster);
		return NewCluster;
	}
	
	/** �Q�̃N���X�^�𓝍��������ɂł���N���X�^��Ԃ����\�b�h(��O�̈�Ȃ�) */
	/*
	private StateCluster getUnityCluster(StateCluster cluster1,StateCluster cluster2){
		//�V�����N���X�^�̐���
		double[][] ranges = StateCluster.getNewRanges(cluster1, cluster2); //�N���X�^�͈͂̌���
		StateCluster NewCluster = new StateCluster(ranges);
		return NewCluster;
	}
	*/
	
	
	/** �����̃N���X�^�̕������s�����\�b�h�B�Ԃ��I�u�W�F�N�g�͂��̕����ɂ���ĐV���ɍ��ꂽ�N���X�^�B */
	/*
	private StateCluster splitCluster(double[] states,StateCluster cluster){
		//���DletedClusterList�̒��ɂ��̏�Ԃ��܂ރN���X�^�����邩��T������
		StateCluster deleted = searchDeletedClusters(states);
		//if(deleted!=null){
		//	ClusterList.add(0,deleted);
		//	return deleted;
		//}
		if(deleted!=null && deleted.getSimpleClusterSize()<cluster.getSimpleClusterSize()){
			//System.out.println(deleted.getID());
			//double[][] ranges = deleted.getRange();
			//System.out.println("minX:"+ranges[0][0]+",maxX:"+ranges[0][1]+",minY:"+ranges[1][0]+",maxY:"+ranges[1][1]);
			//System.out.println();
			
			cluster.addExceptionRanges(deleted.getID(), deleted.getRange()); //��O�̈�̒ǉ�
			cluster.removeState(deleted.getRange());  //��O�̈���̏�Ԃ�S�č폜����
			ClusterList.add(0,deleted);
			return deleted;
			//System.out.println("�폜�N���X�^�̍ė��p");
		}
		//final double reduction = 0.25; //�P�������̏�Ԃ̏k���T�C�Y
		double[][] ranges = cluster.getRange();
		double[][] NewRanges = new double[states.length][2];
		for(int i=0;i<ranges.length;i++){
			//double Length = reduction*(ranges[i][1]-ranges[i][0]);
			double min = states[i]-Ranges_First[i]/2;
			double max = states[i]+Ranges_First[i]/2;
			double gap=0;
			if(max>ranges[i][1]) gap=(ranges[i][1]-max);
			else if(min<ranges[i][0]) gap=(ranges[i][0]-min);
			min+=gap;
			max+=gap;
			NewRanges[i][0] = min;
			NewRanges[i][1] = max;
		}
		StateCluster NewCluster = new StateCluster(NewRanges);
		NewCluster.setDicisionMaker(cluster); //�w�K���ʂ̈����p��
		decideExceptionRanges(NewCluster); //��O�̈�̌���
		cluster.addExceptionRanges(NewCluster.getID(), NewCluster.getRange()); //��O�̈�̒ǉ�
		cluster.removeState(NewCluster.getRange());  //��O�̈���̏�Ԃ�S�č폜����
		//ArrayList<StateCluster> clusts = new ArrayList<StateCluster>();
		//clusts.add(cluster);
		//decideExceptionRanges(NewCluster); //��O�̈�̌���
		//StateCluster.moveState(NewCluster,cluster);
		ClusterList.add(0,NewCluster);
		return NewCluster;
	}
	*/
	
	/** ������ID������ԃN���X�^��Ԃ����\�b�h */
	private StateCluster getCluster(int id){
		for(StateCluster clust : ClusterList){
			if(clust.isSameID(id))return clust;
		}
		return null;
	}
	
	/** �V������ԃN���X�^���쐬���郁�\�b�h.�Ԃ��I�u�W�F�N�g�͂��̐V�������ꂽ�N���X�^�B */
	private StateCluster createNewCluster(double[] states){
		//���DletedClusterList�̒��ɂ��̏�Ԃ��܂ނ��̂����邩��T������
		//StateCluster deleted = searchDeletedClusters(states);
		//if(deleted!=null){
		//	ClusterList.add(0,deleted);
		//	return deleted;
		//}
		
		//�N���X�^�͈͂̏����l�̐ݒ�
		double[][] ranges = new double[states.length][2]; /** ��ԃN���X�^�͈̔͂��Q�����z��ŕ\�� .2��ڂ�index0�͉���,index1�͏����\���Ă��� */
		for(int i=0;i<ranges.length;i++){
			ranges[i][0] = states[i]-Ranges_First[i]/2;
			ranges[i][1] = states[i]+Ranges_First[i]/2;
		}
		//�N���X�^�͈͂̌���
		for(StateCluster clust : ClusterList){
			double[][] elems = clust.getRange();
			if(!isIsolate(states,elems)) return null;
		}
		
		StateCluster cluster = new StateCluster(ranges);
		useActorCriticFormDeletedList(cluster,states);
		/*
		if(cluster.getSimpleClusterSize() < getMinClusterSize()){
			//System.out.println("���ɏ������N���X�^�̂��ߍ폜");
			System.out.println(cluster.getSimpleClusterSize());
			return null;
		}
		*/
		ClusterList.add(0,cluster);
		return cluster;
	}
	
	/** DeletedClusterList�̒���������̏�Ԃ��܂�ł����ԃN���X�^��T�����āA�����Ԃ����\�b�h */
	/*
	private StateCluster searchDeletedClusters(double[] states){
		StateCluster cluster_found = null;
		for(StateCluster clust : DeletedClusterList){
			if(clust==null)continue;
			if(clust.isInCluster(states)){
				//��菬�����N���X�^���̗p����
				if( cluster_found==null || cluster_found.getSimpleClusterSize() > clust.getSimpleClusterSize())
					cluster_found = clust;
			}
		}
		if(cluster_found==null)return null;
		DeletedClusterList.remove(cluster_found);
		decideExceptionRanges(cluster_found);
		//NewCluster������N���X�^�́A�V����NewCluster���O�̈�Ƃ���
		createExceptionArea(cluster_found);
		return cluster_found;
	}
	*/
	
	/** DeletedClusterList�̒���������̏�Ԃ��܂�ł����ԃN���X�^�̊w�K����ė��p���郁�\�b�h */
	private void useActorCriticFormDeletedList(StateCluster cluster,double[] states){
		StateCluster cluster_found = null;
		for(StateCluster clust : DeletedClusterList){
			if(clust==null)continue;
			if(clust.isInCluster(states)){
				//��菬�����N���X�^���̗p����
				if( cluster_found==null || cluster_found.getSimpleClusterSize() > clust.getSimpleClusterSize())
					cluster_found = clust;
			}
		}
		if(cluster_found==null)return;
		DeletedClusterList.remove(cluster_found);
		cluster.setDicisionMaker(cluster_found);
	}
	
	/** ��1��������2�����͈̔͂Ə\���ɋ���������Ă��邩�ǂ����𒲂ׂ郁�\�b�h */
	private boolean isIsolate(double[] state, double[][] ranges){
		double distance=0;
		for(int i=0;i<state.length;i++){
			if(ranges[i][0]<=state[i] && state[i]<=ranges[i][1])continue;
			double elem;
			double toMin = Math.abs(state[i]-ranges[i][0]);
			double toMax = Math.abs(state[i]-ranges[i][1]);
			if(toMin<toMax)elem = toMin;
			else elem = toMax;
			if(elem>distance)distance=elem;
		}
		return (distance > Parameters.Radius_Agent);
	}
	
	
	/** ��P�����͈̔͂��Q�����͈̔͂ɔ��Ȃ��悤�ɏC�����郁�\�b�h */
	/*
	private boolean modifyRange_Move(double[][] targets, double[][] borders, double[] gaps){
		double[] gaps_copy = new double[gaps.length];
		for(int i=0;i<gaps_copy.length;i++)gaps_copy[i]=gaps[i];
		
		for(int i=0;i<targets.length;i++){
			if(targets[i][0]<borders[i][1] && targets[i][1]>borders[i][1]){
				gaps_copy[i] = borders[i][1]-targets[i][0];
			}
			else if(targets[i][1]>borders[i][0] && targets[i][0]<borders[i][0]){
				gaps_copy[i] = borders[i][0]-targets[i][1];
			}
			else return true;
		}
		for(int i=0;i<gaps.length;i++){
			if(gaps[i]*gaps_copy[i] < 0)return false;
			else if( Math.abs(gaps_copy[i]) > Math.abs(gaps[i]) ) gaps[i]= gaps_copy[i];
		}
		
		return true;
	}
	*/
	
	
	/** ��P�����͈̔͂��Q�����͈̔͂ɔ��Ȃ��悤�ɏC�����郁�\�b�h */
	/*
	private void modifyRange_MiniSize(double[][] targets, double[][] borders){
		double[][] copies = new double[targets.length][2];
		for(int i=0;i<targets.length;i++){
			if(targets[i][0]<borders[i][1] && targets[i][1]>borders[i][1]){
				System.out.println("change1:"+borders[i][1]);
				copies[i][0] = borders[i][1];
				copies[i][1] = targets[i][1];
			}
			else if(targets[i][1]>borders[i][0] && targets[i][0]<borders[i][0]){
				System.out.println("change2:"+borders[i][0]);
				copies[i][0] = targets[i][0];
				copies[i][1] = borders[i][0];
			}
			else return;
		}
		for(int i=0;i<targets.length;i++){
			for(int j=0;j<targets[i].length;j++){
				targets[i][j] = copies[i][j];
			}
		}
	}
	*/
	
	/** ���݃G�[�W�F���g�����݂��Ă����ԃN���X�^��Integer�^�ϐ��ɕϊ����ĕԂ����\�b�h */
	private Integer getNowCluster(double[] states){
		for(StateCluster cluster : ClusterList){
			if(cluster.isInCluster(states))return cluster.getID();
		}
		return null;
	}
	
	/** ClusterNumberList�ɗv�f��ǉ����郁�\�b�h */
	private void addClusterNumberList(int id){
		ClusterNumberList.add(0,id);
		if( ClusterNumberList.size() > HistoricalLimit ){
			ClusterNumberList.remove( ClusterNumberList.size()-1 );
		}
	}
	
	/** OutsideStates�ɗv�f��ǉ����郁�\�b�h */
	private void addOutsideStates(State state){
		StateList.add(0,state);
		if(StateList.size()>HistoricalLimit){
			StateList.remove( StateList.size()-1 );
		}
	}
	
	/** DeletedClusterList�ɗv�f��ǉ����郁�\�b�h */
	private void addDeletedClusterList(StateCluster clust){
		long learned = clust.getLearnedNum();
		int reached = clust.getReachedGoal();
		if( learned>=Threshold_Add_DeletedClusterList || reached>=Threshold_GoalCount )DeletedClusterList.add(0,clust);
		if( DeletedClusterList.size() > DeletedClustersNum ){
			DeletedClusterList.remove( DeletedClusterList.size()-1 );
		}
	}
	
	/** ClusterList�̈�ԏ�Ɍ��݂̏�ԃN���X�^��u������ */
	private void updateClusterList(StateCluster cluster){
		if(cluster==null)return;
		ClusterList.remove(cluster);
		ClusterList.add(0,cluster);
	}
	
	/** �S�ẴN���X�^�ɂ��ē����ł��邩�ǂ������ׂ郁�\�b�h */
	public void unityAllClusters(){
		unityClusters(ClusterList.size());
	}
	
	/** �����̌��̃N���X�^�ɂ��ē����ł��邩���ׂāA�ł���ꍇ�͓������s���B */
	private void unityClusters(int num){
		ArrayList<StateCluster> DamyList = new ArrayList<StateCluster>();
		DamyList.addAll(ClusterList);
		int start = DamyList.size()-1;
		int end;
		if(start-num+1 > 0)end = start-num+1;
		else end = 0;
		for(int i=start; i>=end ; i--){
			StateCluster clust = DamyList.get(i);
			int id = clust.getID();
			unifyAllClusters(id);
		}
	}
	
	/** ClusterList�̗v�f����납��폜���郁�\�b�h */
	private void deleteCluster(){
		/* �폜�����O�ɓ������s�� */
		//unityClusters(5);
		deleteMiniCluster();
		
		int[] deletes;
		if( Parameters.Limit_Clusters < ClusterList.size() ) deletes = new int[ClusterList.size()-Parameters.Limit_Clusters];
		else return;
		for(int i=0;i<deletes.length;i++){
			StateCluster clust = ClusterList.remove(ClusterList.size()-1);
			//addDeletedClusterList(clust); /* �폜�N���X�^���X�g�ɒǉ�����B11/19�V���ɒǉ� */
			deletes[i] = clust.getID();
		}
		for(StateCluster clust : ClusterList){
			clust.removeFromExceptionRanges(deletes);
		}
	}
	
	/** �������N���X�^���폜���郁�\�b�h */
	private void deleteMiniCluster(){
		ArrayList<Integer> deletes = new ArrayList<Integer>();
		for(int i=0;i<ClusterList.size();){
			StateCluster clust = ClusterList.get(i);
			if(clust.getOutsideStateList_Random()!=null){
				i++;
				continue;
			}
			ClusterList.remove(clust);
			//addDeletedClusterList(clust);
			deletes.add(clust.getID());
		}
		int[] delete_array = new int[deletes.size()]; 
		for(int i=0;i<delete_array.length;i++) delete_array[i] = deletes.get(i);
		
		for(StateCluster clust : ClusterList){
			clust.removeFromExceptionRanges(delete_array);
		}		
	}
	

	/** �e�N���X�^�͈̔͂�Ԃ����\�b�h */
	public ArrayList<double[][]> getClusterRanges(){
		ArrayList<double[][]> list = new ArrayList<double[][]>();
		for(StateCluster clust : ClusterList){
			list.add(clust.getRange());
		}
		return list;
	}
	
	/** ����exception������cluster���番�����Ăł����N���X�^���ǂ����𔻒f���郁�\�b�h */
	private boolean isExceptionArea(StateCluster cluster,StateCluster exception){
		if(!cluster.isInExceptionRanges(exception.getID()))return false;
		return isContain(cluster,exception);
	}
	
	
	/** ����exception������cluster�̕����W�����ǂ����𔻒f���郁�\�b�h */
	private boolean isContain(StateCluster cluster,StateCluster exception){
		double[][] range_cluster = cluster.getRange();
		double[][] range_exception = exception.getRange();
		for(int i=0;i<range_cluster.length;i++){
			if(range_exception[i][0]<range_cluster[i][0] || range_exception[i][0]>range_cluster[i][1] || 
					range_exception[i][1]<range_cluster[i][0] || range_exception[i][1]>range_cluster[i][1])
				return false;
		}
		return true;
	}
	
	/** �����̃N���X�^���u�傫���������N���X�^�v���ǂ������f���郁�\�b�h */
	private boolean isTooLarge(StateCluster clust){
		double threshold_per = 0.5;
		for(StateCluster elem : ClusterList){
			if(elem==clust || !isContain(elem,clust))continue;
			if( clust.getSimpleClusterSize() > threshold_per*elem.getSimpleClusterSize() )
				return true;
		}
		return false;
	}
	
	/** ����N���X�^�ɂ����āA�����̃N���X�^������N���X�^�̏ꍇ��O�̈�Ɏw�肷�郁�\�b�h */
	private void createExceptionArea(StateCluster NewCluster){
		for(StateCluster clust : ClusterList){
			if(isContain(clust,NewCluster)){
				clust.addExceptionRanges(NewCluster.getID(), NewCluster.getRange()); //��O�̈�̒ǉ�
				clust.removeState(NewCluster.getRange());  //��O�̈���̏�Ԃ�S�č폜����
			}
		}
	}
	
	
	/** ���̃I�u�W�F�N�g�̎��������X�g�����������郁�\�b�h */
	public void format(){
		NowStep=0;
		ClusterNumberList.clear();
		StateList.clear();
		Environment.formatScoreList();
		for(StateCluster clust : ClusterList){
			clust.format();
		}
		for(StateCluster clust : DeletedClusterList){
			clust.format();
		}
	}
	
	/** �����̃G�s�\�[�h���o�ߌ�Ɋe�N���X�^�[�̏����t�@�C���ɏ������ރ��\�b�h */
	public void writeAboutClusters(int episode){
		String Pass=Parameters.ResultFoldr+"\\"+"1227test_Clusters_after"+episode+"episode.csv";
		FileWriter Result=null;
    	try{
    		Result=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result);
    		String Header="�ԍ�,����,���U";
    		StateCluster OneCluster = ClusterList.get(0);
    		double[][] ranges = OneCluster.getRange();
    		for(int i=0;i<ranges.length;i++){
    			Header += ",min"+i+",max"+i;
    		}
			for(int act=1;act<3;act++){
				double[] aves = OneCluster.getAve_Vectors(act);
				for(int i=0;i<aves.length;i++){
					Header +=",act"+act+"_����"+i;
				}
				Header +=",act"+act+"_���U";
			}
			Header+=",�w�K��,�S�[����";
    		pWriter.println(Header);
    		for(int i=0;i<ClusterList.size();i++){
    			StateCluster cluster = ClusterList.get(i);
    			String line = Integer.toString(cluster.getID());
    			double[][] states = cluster.getOutsideStateList();
    			String ave_diss="";
    			if(states==null){
    				ave_diss = ",null,null";
    			}
    			else{
    				double[] StateValues = new double[states.length];  //�N���X�^�P�̊e��Ԃ̐������l���i�[����z��
    				for(int j=0;j<StateValues.length;j++){
    					StateValues[j] = Environment.getRBFOutput_Normalized(states[j]);
    				}
    				double ave = getAverage(StateValues);
    				double diss = getDispersion(ave,StateValues);
    				ave_diss = ","+ave+","+diss;
    			}
    			line+=ave_diss;
    			double[][] ranges_elem = cluster.getRange();
    			for(int j=0;j<ranges_elem.length;j++){
    				line+=","+ranges_elem[j][0]+","+ranges_elem[j][1];
    			}
    			for(int act=1;act<3;act++){
    				double[] aves = cluster.getAve_Vectors(act);
    				for(int index=0;index<aves.length;index++){
    					line +=","+aves[index];
    				}
    				line +=","+cluster.getDis_Vector(act);
    			}
    			line+=","+cluster.getLearnedNum();
    			line+=","+cluster.getReachedGoal();
    			pWriter.println(line);
    		}
    		Result.close();
    	}catch(IOException e){
    		System.out.println("ERROR!");
    	}
	}
	
	/** �e�G�s�\�[�h�ɂ����ď����STEP�����������ނ��߂̃t�@�C���̍쐬 */
	private void createOutputFile(){
    	String Pass=Parameters.RBFOutputFilePass;
    	try{
    		Result_Average=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result_Average);
    		String Header="Episode,���ϒl,���U�l,�N���X�^��";
    		pWriter.println(Header);
    	}catch(IOException e){
    		System.out.println("ERROR(NewBrain)!");
    	}
	}
	
	/** �t�@�C���ɒǉ��������݂��s�����\�b�h */
	private void addAverageOutputFile(int episode){
        if (Result_Average==null) return;
        PrintWriter pWriter = new PrintWriter(Result_Average);
        double[] outputs = new double[StateList.size()];
        for(int i=0;i<outputs.length;i++){
        	State state = StateList.get(i);
        	double[] outside = state.getOutsideState();
        	outputs[i] = Environment.getRBFOutput_Normalized(outside);
        }
        double average=getAverage(outputs);
        double dispersion=getDispersion(average,outputs);
        
        String str=episode+","+average+","+dispersion+","+ClusterList.size();
        pWriter.println(str);
		try{
			Result_Average.flush();
		}catch(IOException e){}
	}
	
	/** �eEpisode���̊O��RBF�l�b�g���[�N�̕��Ϗo�͒l���t�@�C���ɏo�͂��郁�\�b�h */
	public void writeAverageOutput(int episode){
		addAverageOutputFile(episode);
		if(episode==Parameters.Numbers_Episode-1){
			Environment.writeRbfUnits(); //�O��RBF�l�b�g���[�N�̏����o�͂���
			closeFile();
		}
	}
	
	/** �t�@�C������郁�\�b�h */
	private void closeFile(){
		try{
			if(Result_Average!=null)Result_Average.close();
		}catch(IOException e){}
	}
}
