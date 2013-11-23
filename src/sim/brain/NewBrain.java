package sim.brain;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import sim.Parameters;

/** エージェントの思考部分 */
public class NewBrain {
	private RbfNetwork Environment;  /** 環境の状態の価値を推測するRBFネットワーク */
	private ArrayList<StateCluster> ClusterList = new ArrayList<StateCluster>(); /** 状態クラスタリスト */
	private ArrayList<Integer> ClusterNumberList = new ArrayList<Integer>();  /** エージェントが存在していた状態クラスタの履歴 */
	private ArrayList<State> StateList = new ArrayList<State>(); /** エージェントが経験した状態の履歴 */
	private double[] Ranges_First = {2*Parameters.Radius_Agent,2*Parameters.Radius_Agent}; /** 状態クラスタの初期クラスタ範囲（デフォルトは30,30） */
	private final int HistoricalLimit = Parameters.HistoricalListLimit; /** ClusterNumberListとOutsideStatesの要素数の上限値 */
	private FileWriter Result_Average; /** 各Episodeにおける外部RBFの出力値が書き込まれるファイルのオブジェクト */
	private double Threshold_Distance = Math.sqrt(2)*Parameters.Radius_Agent;  /** 統合するかどうかの閾値 */
	private ArrayList<StateCluster> DeletedClusterList = new ArrayList<StateCluster>();  /** 一度削除された状態クラスタのリスト */
	private final int DeletedClustersNum = 1000; /** DeletedClusterListに格納可能な最大要素数 */
	private final long Threshold_Add_DeletedClusterList = 300; /** DeletedClusterListに追加する時の学習回数に関する閾値(もとは200) */
	private final int Threshold_GoalCount = 1; /** DeletedClusterListに追加する時のゴール回数に関する閾値 */
	//private final int FirstStateNumbers = 9;
	
	private final int OutConfidence_Left = -1; //左側の信頼区間外に位置することを示す変数
	private final int InConfidence = 0;        //信頼区間内に位置することを示す変数
	private final int OutConfidence_Right = 1;  //右側の信頼区間外に位置することを示す変数
	
	private int NowStep=0; /** 現在のstep */
	//private final int NeccesaryNumbers_Split = 2*FirstStateNumbers;
	
	/** コンストラクタ */
	public NewBrain(){
		//外部状態のRBFネットワークのパラメーター群
		int inputs_estimate[]={1,1};
		double Min_estimate[]={0,0};
		double Max_estimate[]={Parameters.SimulatorWidth,Parameters.SimulatorHeight};
		int Split_estimate[]={20,20};
		
		Environment = new RbfNetwork("Environment",inputs_estimate,Min_estimate,Max_estimate,Split_estimate,inputs_estimate);
		createOutputFile();
		/* 以下テストでClusterListを予め作成 */
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
	
	/** 外部の状態の価値を推測するメソッド */
	public void estimateOutsideState(double[] states){
		Environment.runRBF(states);
	}

	/** 内部の状態の価値を推測するメソッド */
	public void estimateInsideState(double[] states){
		int id = ClusterNumberList.get(0);
		StateCluster clust = getCluster(id);
		if(clust==null)return; //その場しのぎ
		clust.runCritic(states);
	}
	
	/** 実際に行う行動を取得するメソッド */
	public double[] getAction(double[] state){
		int id = ClusterNumberList.get(0);
		StateCluster clust = getCluster(id);
		//クラスターが存在しない場足はランダムな行動を返すことにする
		if(clust==null){
			double[] array = {Parameters.getRandomValue(Parameters.ChangeSpeed,-1*Parameters.ChangeSpeed),Parameters.getRandomValue(Parameters.ChangeAngle, -1*Parameters.ChangeAngle)};
			return array;
		}
		return clust.getAction(state);
	}
	
	/** 外部環境に関するRBFネットワークのパラメーターの更新(学習) */
	public void updateEnvironment(double comp,double[] states){
		boolean[] updates={false,false};
		double[] before = StateList.get(0).getOutsideState();
		Environment.updateParameters(comp,states,before,updates);
	}
	
	/** 外部環境に関するRBFネットワークのパラメーターの更新(モンテカルロ法の場合) */
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
	
	/** 現在エージェントが存在しているクラスタのActor-Criticの学習を行う */
	public void updateOneCluster(double comp,double[] states){
		//updateDomestic(comp,states,1);
		Double TD = getTDError(comp, states);
		if( TD==null || ClusterList.size()==0)return;
		StateCluster cluster = ClusterList.get(0);
		cluster.updateActorCritic(TD.doubleValue());
	}
	
	/** 現在エージェントが存在しているクラスタのActor-Criticの学習を行う */
	public void updateOneCluster(double comp){
		//updateClusters_MonteCarlo(comp,1);
		//updateClusters_MonteCarlo(comp,1,false,1);
		
		if(ClusterList.size()==0)return;
		StateCluster cluster = ClusterList.get(0);
		cluster.updateActorCritic_MonteCarlo_OnlyReward(comp,false);
	}
	
	/** ClusterNumberList中の全てのクラスタのActor-Criticの学習を行う */
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
	
	/** ClusterNumberList中の全てのクラスタのActor-Criticの学習を行う(報酬獲得時に使用するメソッド) */
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
		double lambda = 0.5;  //デフォは0.8
		for(int i=0;i<nums;i++){
			StateCluster cluster = ClusterList.get(i);
			double Comp_Real = comp * Math.pow(lambda,i);
			cluster.updateActorCritic_MonteCarlo_OnlyReward(Comp_Real);
		}
		*/
	}
	
	/** 指定された数のクラスタのactor-citicの学習を行う */
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
		
		
		//double lambda = 0.5;  //デフォは0.8
		//for(int i=0;i<nums;i++){
		//	StateCluster cluster = ClusterList.get(i);
		//	double Comp_Real = comp * Math.pow(lambda,i);
		//	cluster.updateActorCritic_MonteCarlo_OnlyReward(Comp_Real);
		//}
	}
	
	/** 指定された数のクラスタのactor-citicの学習を行う */
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
		
		
		//double lambda = 0.5;  //デフォは0.8
		//for(int i=0;i<nums;i++){
		//	StateCluster cluster = ClusterList.get(i);
		//	double Comp_Real = comp * Math.pow(lambda,i);
		//	cluster.updateActorCritic_MonteCarlo_OnlyReward(Comp_Real);
		//}
	}
	
	/** TD誤差を取得するメソッド */
	private Double getTDError(double comp,double[] state){
		int id_least = ClusterNumberList.get(0);
		StateCluster clust_least = getCluster(id_least);
		if(clust_least==null)return null;
		return clust_least.getTDError(comp, state);
	}
	
	/** 
	 * 各クラスタのActor-Criticの学習 
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
		
		//ArrayList<Integer> list = new ArrayList<Integer>(); //学習済みのクラスタの番号を保存するリスト
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
	
	/** 第１引数のリスト中に第２引数の要素が存在するかどうかをチェックするメソッド */
	/*
	private boolean isContain(ArrayList<Integer> list,int id){
		for(int elem : list){
			if(id==elem)return true;
		}
		return false;
	}
	*/
	
	
	/** 状態クラスタの構築を行うメソッド */
	public void createClusterList(double[] state_outside , double[] state_inside){
		//getCluster(state_outside);
		//System.out.println("クラスター数："+ClusterList.size());
		//System.out.println("削除されたクラスター数："+DeletedClusterList.size());
		NowStep++;
		Integer before=null;
		StateCluster cluster_before = null;
		if(ClusterNumberList.size()>0){
			before = ClusterNumberList.get(0);
			if(before!=null) cluster_before = getCluster(before);
		}
		Integer now = getNowCluster(state_outside);
		//System.out.println("x:"+state_outside[0]+",y:"+state_outside[1]);
		//System.out.println("現在の番号："+now);
		StateCluster cluster_now; //test
		/* 追加or拡張 */
		if(now==null){
			if(cluster_before!=null && canEnlarge(state_outside,cluster_before) ){
				//クラスタの拡張
				cluster_before.enlarge(state_outside);
				decideExceptionRanges(cluster_before);
				cluster_now = cluster_before;
				//System.out.println("拡張");
			}
			else{
				//新しいクラスタの追加
				cluster_now = createNewCluster(state_outside);
				//System.out.println("追加");
			}
		}
		/*分離or統合*/
		else{
			cluster_now = getCluster(now);
			if(!isInConfidence(state_outside,cluster_now)){
				StateCluster clust = searchEnlargeCluster(state_outside);  //分割する前に近くにこの状態まで拡張可能なクラスタがないかどうか調べる
				if(clust!=null){
					cluster_now.removeState(clust.getRange());  //例外領域内の状態を全て削除する
					cluster_now = clust;
				}
				else{
					StateCluster split = splitCluster(state_outside,cluster_now);
					if( split != null ){
						cluster_now = split; //分離処理
						//System.out.println("分離");
					}
				}
			}
			//else{
			//	unifyAllClusters(cluster_now.getID());
			//}
			
			else if(cluster_before!=null && now.intValue()!=before.intValue()){ //統合処理
				StateCluster unified = judge_unify_Clusters(cluster_before,cluster_now);
				if(unified!=null)cluster_now = unified;
			//	System.out.println("統合");
			}
			
		}
		State state = new State(state_outside,state_inside);
		addOutsideStates(state);
		int id;
		if(cluster_now==null){
			id=-1;
			//System.out.println("範囲無し");
		}
		else {
			id=cluster_now.getID();
			cluster_now.addStateList(state);
			cluster_now.setStep_Least(NowStep);
			//double[][] ranges=cluster_now.getRange();
			//System.out.println("x_min:"+ranges[0][0]+",x_max:"+ranges[0][1]);
			//System.out.println("y_min:"+ranges[1][0]+",y_max:"+ranges[1][1]);
		}
		//System.out.println("クラスター番号："+id);
		addClusterNumberList(id);
		updateClusterList(cluster_now);
		//統合処理
		/*
		if(ClusterNumberList.size() == HistoricalLimit){
		    int id = ClusterNumberList.get(HistoricalLimit-1);
			unifyAllClusters(id);
		}
		*/
		//State state = new State(state_outside,state_inside);
		//cluster_now.addStateList(state);
		deleteCluster();
		//System.out.println("クラスターサイズ："+ClusterList.size());
	}
	
	/** 引数の状態を含む状態クラスタを返すメソッド(test)*/
	/*
	private StateCluster getCluster(double[] states){
		ArrayList<StateCluster> list = new ArrayList<StateCluster>();
		for(StateCluster cluster : ClusterList){
			if(cluster.isInCluster(states))list.add(cluster);
		}
		//System.out.println("引数を含む状態クラスタの数:"+list.size());  //test
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
	
	/** 引数の状態まで拡張可能なクラスターを探すメソッド */
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
	
	/** 全てのクラスターを引数idのクラスターと統合させるメソッド */
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
	

	/** 全てのクラスターを引数idのクラスターと統合させるメソッド */
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
	
	/** 第１引数のクラスタと第２引数のクラスタを統合できるかどうかを調べるメソッド */
	private boolean doUnity(StateCluster cluster1,StateCluster cluster2){
		if(isExceptionArea(cluster1,cluster2)|| isExceptionArea(cluster2,cluster1))return false;
		double[][] states_Cluster1 = cluster1.getOutsideStateList_Random();
		double[][] states_Cluster2 = cluster2.getOutsideStateList_Random();
		if(states_Cluster1==null || states_Cluster2==null)return false; //片方のクラスタが小さすぎる場合は無条件で統合させる
		//double threshold_distance = 20; //かなり適当に決めたクラスタ間の距離の閾値
		//if( states_Cluster1.length < Parameters.Min_State_Numbers || states_Cluster2.length < Parameters.Min_State_Numbers )
			//return false;
		//クラスタ同士が近接しているかどうかのチェック
		if( !cluster1.isInExceptionRanges(cluster2.getID()) && !cluster2.isInExceptionRanges(cluster1.getID()) &&
				StateCluster.getDistance(cluster1, cluster2)>Threshold_Distance )
			return false;
		double[] StateValues1 = new double[states_Cluster1.length];  //クラスタ１の各状態の推測価値を格納する配列
		double[] StateValues2 = new double[states_Cluster2.length];  //クラスタ２の各状態の推測価値を格納する配列
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
	
	
	/** 第２引数のクラスタの分離が行われるかどうかを調べるメソッド */
	private StateCluster splitCluster(double[] states,StateCluster cluster){
		double[][] states_Cluster = cluster.getOutsideStateList_Random();//getOutsideStateList(FirstStateNumbers-1);
		if(states_Cluster==null /*|| states_Cluster.length<FirstStateNumbers*/ )return null;
		double[] StateValues = new double[states_Cluster.length];  //各状態の推測価値を格納する配列
		for(int i=0;i<StateValues.length;i++){
			StateValues[i] = Environment.getRBFOutput_Normalized(states_Cluster[i]);
		}
		double ave_values = getAverage(StateValues);
		double diss_values = getDispersion(ave_values,StateValues);
		
		//過去10STEP前の状態の価値と比較して、どの価値も信頼区間外に位置する場合は分離を行う。
		/*
		double[][] states_least = cluster.getOutsideStateList(0,FirstStateNumbers-1);
		if(states_least==null)return null;
		for(int i=0;i<states_least.length+1;i++){
			double value;
			if(i==states_least.length) value = Environment.getRBFOutput_Normalized(states);
			else value=Environment.getRBFOutput_Normalized(states_least[i]);
			if(isInConfidenceInterval(value,ave_values,diss_values)){
				//System.out.println(i+"回目で失敗");
				//System.out.println("value:"+value+",ave:"+ave_values+",diss:"+diss_values);
				return null;
			}
		}
		
		//クラスタ範囲の決定
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
		
		//クラスタ範囲の決定
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
		
		//例外領域クラスタの作成
		StateCluster NewCluster = new StateCluster(NewRange);
		if( NewCluster.getSimpleClusterSize() < getMinClusterSize() ){
			//System.out.println(NewCluster.getSimpleClusterSize());
			return null;
		}
		useActorCriticFormDeletedList(NewCluster,states);
		//NewCluster.setDicisionMaker(cluster); //学習結果の引き継ぎ
		decideExceptionRanges(NewCluster); //例外領域の決定
		/* NewClusterを内包するクラスタは、新たにNewClusterを例外領域とする */
		createExceptionArea(NewCluster);
		//ArrayList<StateCluster> clusts = new ArrayList<StateCluster>();
		//clusts.add(cluster);
		//decideExceptionRanges(NewCluster); //例外領域の決定
		//StateCluster.moveState(NewCluster,cluster);
		ClusterList.add(0,NewCluster);
		return NewCluster;
	}
	
	/** 分離する際の範囲を再帰的に決めるメソッド */
	private void decideSplitRange(double[] state,double[][] range,double[][] Range_Original,double ave,double diss,double maxsize,int outconfidence){
		//stateがRange_Originalの範囲内かどうかの判定
		for(int i=0;i<state.length;i++){
			if( Range_Original[i][0]>state[i] || state[i]>Range_Original[i][1])return;
		}
		//現在の状態の価値がクラスタの信頼区間内かどうかを調べる
		double value=Environment.getRBFOutput_Normalized(state);
		if(isInConfidenceInterval(value,ave,diss)!=outconfidence)return;
		
		//分離範囲の設定
		//TODO 元の範囲より小さくなる場合は更新しない
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
		
		//stateのコピー配列の作成
		double[] state_copy = new double[state.length];
		for(int i=0;i<state_copy.length;i++){
			state_copy[i] = state[i];
		}
		
		//TODO stateの全ての要素を変化させる
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
	
	/** 第1引数の状態が第2引数の範囲内かどうかを判定するメソッド */
	private boolean isInRange(double[] state,double[][] range){
		for(int i=0;i<range.length;i++){
			if( state[i]<range[i][0] || state[i]>range[i][1] )
				return false;
		}
		return true;
	}
	
	
	/** クラスターの最小サイズを返すメソッド */
	private double getMinClusterSize(){
		double MinSize;
		if(Ranges_First.length > 0)MinSize = Ranges_First[0];
		else MinSize=0;
		for(int i=1;i<Ranges_First.length;i++){
			MinSize = MinSize*Ranges_First[i];
		}
		return MinSize;
	}
	
	/** 第２引数のクラスタの拡張が可能かどうかを調べるメソッド */
	private boolean canEnlarge(double[] states,StateCluster cluster){
		return (isInConfidence(states,cluster) && !isTooLarge(cluster));
	}
	
	/** 第１引数の状態の価値が第２引数のクラスタの「価値の信頼区間内に入る」かどうかを判断するメソッド */
	private boolean isInConfidence(double[] states,StateCluster cluster){
		double[][] states_Cluster = cluster.getOutsideStateList_Random();
		if(states_Cluster==null)return true; //クラスターが小さすぎる場合は無条件で拡大する
		//if(states_Cluster.length < Parameters.Min_State_Numbers) return true;
		double[] StateValues = new double[states_Cluster.length];  //各状態の推測価値を格納する配列
		for(int i=0;i<StateValues.length;i++){
			StateValues[i] = Environment.getRBFOutput_Normalized(states_Cluster[i]);
		}
		double value_now = Environment.getRBFOutput_Normalized(states);
		int can = isInConfidenceInterval(value_now,StateValues);
		return (can==InConfidence);
	}
	
	
	/** 第１引数の値が信頼区間内に入るかどうかを判定するメソッド */
	private int isInConfidenceInterval(double value_now,double[] values){
		double ave = getAverage(values);  //状態クラスタ内の状態の価値の平均値
		//TODO 分散値を後で変更するかも
		double diss = getDispersion(ave,values);  //状態クラスタ内の状態の価値の分散値
		return isInConfidenceInterval(value_now,ave,diss);
	}
	
	/** 第１引数の値が信頼区間内に入るかどうかを判定するメソッド */
	private int isInConfidenceInterval(double value_now,double ave,double diss){
		final double beta = 3.0; //1.96;
		//final double interval_test = 0.15;
		double min_confidence = ave - beta*Math.sqrt(diss); //ave - beta*diss;   //信頼区間の下限値
		double max_confidence = ave + beta*Math.sqrt(diss); //ave + beta*diss;   //信頼区間の上限値
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
	
	/** 引数のdouble配列の平均値を返すメソッド */
	private double getAverage(double[] values){
		double ave = 0;
		//平均値の算出
		for(int i=0;i<values.length;i++){
			ave += values[i];
		}
		ave = ave/values.length;
		return ave;
	}
	
	/** 第２引数のdouble配列の分散値を返すメソッド */
	private double getDispersion(double ave,double[] values){
		double diss = 0;
		//分散値の算出
		for(int i=0;i<values.length;i++){
			diss += Math.pow(values[i]-ave,2);
		}
		diss = diss/values.length;
		return diss;
	}
	
	/** 引数クラスタの例外領域を決定するメソッド */
	private void decideExceptionRanges(StateCluster cluster){
		//cluster.formatExceptionRanges();
		for(StateCluster clust : ClusterList){
			if( cluster == clust || clust.isInExceptionRanges(cluster.getID()) )continue;
			cluster.addExceptionRanges(clust.getID(), clust.getRange());
			//if(exist)clust.addExceptionRanges(id,ranges);
		}
	}
	
	/** 引数の２つのクラスタを統合させるメソッド */
	
//	private StateCluster unifyClusters(StateCluster cluster1,StateCluster cluster2){
//		/* 新しいクラスタの生成 */
//		double[][] ranges = StateCluster.getNewRanges(cluster1, cluster2); //クラスタ範囲の決定
//		StateCluster NewCluster = new StateCluster(ranges);
//		int index1=ClusterList.indexOf(cluster1);
//		int index2=ClusterList.indexOf(cluster2);
//		if( index1<0 || index2<0 )ClusterList.add(0,NewCluster);
//		else if(index1>index2)ClusterList.add(index2,NewCluster);
//		else ClusterList.add(index1,NewCluster);
//		/* StateListの決定 */
//		//NewCluster.setStateList(cluster1,cluster2);
//		/* DecisionMaker(Actor-Critic)の決定 */
//		NewCluster.setDecisionMaker(cluster1,cluster2);
//		
//		/* ２つのクラスタをClusterListから除外する */
//		ClusterList.remove(cluster1);
//		ClusterList.remove(cluster2);
//		
//		/* ２つのクラスタをDeletedClusterListに追加する */
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
//		/* ClusterNumberList中の２つのクラスタのIDを統合後のクラスタのIDに変換する */
//		int id_cl1 = cluster1.getID();
//		int id_cl2 = cluster2.getID();
//		int NewID = NewCluster.getID();
//		for(int i=0;i<ClusterNumberList.size();i++){
//			int id = ClusterNumberList.get(i);
//			if( id==id_cl1 || id==id_cl2 )ClusterNumberList.set(i, NewID);
//		}
//		
//		/* ClusterList中の各クラスタの例外領域の中から２つのクラスタを除外する */
//		int[] array_delete = {cluster1.getID(),cluster2.getID()};
//		for(StateCluster clust : ClusterList){
//			clust.removeFromExceptionRanges(array_delete);
//		}
//		
//		/* 新しいクラスタの例外領域の決定 */
//		decideExceptionRanges(NewCluster);
//		
//		/* NewClusterを内包するクラスタは、新たにNewClusterを例外領域とする */
//		createExceptionArea(NewCluster);
//		return NewCluster;
//	}
	
	/** 引数の２つのクラスタが統合できるかどうかの判定と統合を同時に行うメソッド */
	private StateCluster judge_unify_Clusters(StateCluster cluster1,StateCluster cluster2){
		if(!doUnity(cluster1,cluster2))return null;
		/* 新しいクラスタの生成 */
		double[][] ranges = StateCluster.getNewRanges(cluster1, cluster2); //クラスタ範囲の決定
		StateCluster NewCluster = new StateCluster(ranges);
		if(isTooLarge(NewCluster))return null;
		int index1=ClusterList.indexOf(cluster1);
		int index2=ClusterList.indexOf(cluster2);
		if( index1<0 || index2<0 )ClusterList.add(0,NewCluster);
		else if(index1>index2)ClusterList.add(index2,NewCluster);
		else ClusterList.add(index1,NewCluster);
		/* StateListの決定 */
		//NewCluster.setStateList(cluster1,cluster2);
		/* DecisionMaker(Actor-Critic)の決定 */
		NewCluster.setDecisionMaker(cluster1,cluster2);
		
		/* ２つのクラスタをClusterListから除外する */
		ClusterList.remove(cluster1);
		ClusterList.remove(cluster2);
		
		/* ２つのクラスタをDeletedClusterListに追加する */
		//TODO
		StateCluster cluster_delete;
		if(cluster1.getSimpleClusterSize()<cluster2.getSimpleClusterSize())cluster_delete = cluster1;
		else cluster_delete = cluster2;
		
		//addDeletedClusterList(cluster1);
		//addDeletedClusterList(cluster2);
		addDeletedClusterList(cluster_delete);
		
		/* ClusterNumberList中の２つのクラスタのIDを統合後のクラスタのIDに変換する */
		int id_cl1 = cluster1.getID();
		int id_cl2 = cluster2.getID();
		int NewID = NewCluster.getID();
		for(int i=0;i<ClusterNumberList.size();i++){
			int id = ClusterNumberList.get(i);
			if( id==id_cl1 || id==id_cl2 )ClusterNumberList.set(i, NewID);
		}
		
		/* ClusterList中の各クラスタの例外領域の中から２つのクラスタを除外する */
		int[] array_delete = {cluster1.getID(),cluster2.getID()};
		for(StateCluster clust : ClusterList){
			clust.removeFromExceptionRanges(array_delete);
		}
		
		/* 新しいクラスタの例外領域の決定 */
		decideExceptionRanges(NewCluster);
		
		/* NewClusterを内包するクラスタは、新たにNewClusterを例外領域とする */
		createExceptionArea(NewCluster);
		return NewCluster;
	}
	
	/** ２つのクラスタを統合した時にできるクラスタを返すメソッド(例外領域なし) */
	/*
	private StateCluster getUnityCluster(StateCluster cluster1,StateCluster cluster2){
		//新しいクラスタの生成
		double[][] ranges = StateCluster.getNewRanges(cluster1, cluster2); //クラスタ範囲の決定
		StateCluster NewCluster = new StateCluster(ranges);
		return NewCluster;
	}
	*/
	
	
	/** 引数のクラスタの分離を行うメソッド。返すオブジェクトはその分離によって新たに作られたクラスタ。 */
	/*
	private StateCluster splitCluster(double[] states,StateCluster cluster){
		//先にDletedClusterListの中にこの状態を含むクラスタがあるかを探索する
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
			
			cluster.addExceptionRanges(deleted.getID(), deleted.getRange()); //例外領域の追加
			cluster.removeState(deleted.getRange());  //例外領域内の状態を全て削除する
			ClusterList.add(0,deleted);
			return deleted;
			//System.out.println("削除クラスタの再利用");
		}
		//final double reduction = 0.25; //１次元毎の状態の縮小サイズ
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
		NewCluster.setDicisionMaker(cluster); //学習結果の引き継ぎ
		decideExceptionRanges(NewCluster); //例外領域の決定
		cluster.addExceptionRanges(NewCluster.getID(), NewCluster.getRange()); //例外領域の追加
		cluster.removeState(NewCluster.getRange());  //例外領域内の状態を全て削除する
		//ArrayList<StateCluster> clusts = new ArrayList<StateCluster>();
		//clusts.add(cluster);
		//decideExceptionRanges(NewCluster); //例外領域の決定
		//StateCluster.moveState(NewCluster,cluster);
		ClusterList.add(0,NewCluster);
		return NewCluster;
	}
	*/
	
	/** 引数のIDを持つ状態クラスタを返すメソッド */
	private StateCluster getCluster(int id){
		for(StateCluster clust : ClusterList){
			if(clust.isSameID(id))return clust;
		}
		return null;
	}
	
	/** 新しい状態クラスタを作成するメソッド.返すオブジェクトはその新しく作られたクラスタ。 */
	private StateCluster createNewCluster(double[] states){
		//先にDletedClusterListの中にこの状態を含むものがあるかを探索する
		//StateCluster deleted = searchDeletedClusters(states);
		//if(deleted!=null){
		//	ClusterList.add(0,deleted);
		//	return deleted;
		//}
		
		//クラスタ範囲の初期値の設定
		double[][] ranges = new double[states.length][2]; /** 状態クラスタの範囲を２次元配列で表現 .2列目のindex0は下限,index1は上限を表している */
		for(int i=0;i<ranges.length;i++){
			ranges[i][0] = states[i]-Ranges_First[i]/2;
			ranges[i][1] = states[i]+Ranges_First[i]/2;
		}
		//クラスタ範囲の決定
		for(StateCluster clust : ClusterList){
			double[][] elems = clust.getRange();
			if(!isIsolate(states,elems)) return null;
		}
		
		StateCluster cluster = new StateCluster(ranges);
		useActorCriticFormDeletedList(cluster,states);
		/*
		if(cluster.getSimpleClusterSize() < getMinClusterSize()){
			//System.out.println("非常に小さいクラスタのため削除");
			System.out.println(cluster.getSimpleClusterSize());
			return null;
		}
		*/
		ClusterList.add(0,cluster);
		return cluster;
	}
	
	/** DeletedClusterListの中から引数の状態を含んでいる状態クラスタを探索して、それを返すメソッド */
	/*
	private StateCluster searchDeletedClusters(double[] states){
		StateCluster cluster_found = null;
		for(StateCluster clust : DeletedClusterList){
			if(clust==null)continue;
			if(clust.isInCluster(states)){
				//より小さいクラスタを採用する
				if( cluster_found==null || cluster_found.getSimpleClusterSize() > clust.getSimpleClusterSize())
					cluster_found = clust;
			}
		}
		if(cluster_found==null)return null;
		DeletedClusterList.remove(cluster_found);
		decideExceptionRanges(cluster_found);
		//NewClusterを内包するクラスタは、新たにNewClusterを例外領域とする
		createExceptionArea(cluster_found);
		return cluster_found;
	}
	*/
	
	/** DeletedClusterListの中から引数の状態を含んでいる状態クラスタの学習器を再利用するメソッド */
	private void useActorCriticFormDeletedList(StateCluster cluster,double[] states){
		StateCluster cluster_found = null;
		for(StateCluster clust : DeletedClusterList){
			if(clust==null)continue;
			if(clust.isInCluster(states)){
				//より小さいクラスタを採用する
				if( cluster_found==null || cluster_found.getSimpleClusterSize() > clust.getSimpleClusterSize())
					cluster_found = clust;
			}
		}
		if(cluster_found==null)return;
		DeletedClusterList.remove(cluster_found);
		cluster.setDicisionMaker(cluster_found);
	}
	
	/** 第1引数が第2引数の範囲と十分に距離が離れているかどうかを調べるメソッド */
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
	
	
	/** 第１引数の範囲を第２引数の範囲に被らないように修正するメソッド */
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
	
	
	/** 第１引数の範囲を第２引数の範囲に被らないように修正するメソッド */
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
	
	/** 現在エージェントが存在している状態クラスタをInteger型変数に変換して返すメソッド */
	private Integer getNowCluster(double[] states){
		for(StateCluster cluster : ClusterList){
			if(cluster.isInCluster(states))return cluster.getID();
		}
		return null;
	}
	
	/** ClusterNumberListに要素を追加するメソッド */
	private void addClusterNumberList(int id){
		ClusterNumberList.add(0,id);
		if( ClusterNumberList.size() > HistoricalLimit ){
			ClusterNumberList.remove( ClusterNumberList.size()-1 );
		}
	}
	
	/** OutsideStatesに要素を追加するメソッド */
	private void addOutsideStates(State state){
		StateList.add(0,state);
		if(StateList.size()>HistoricalLimit){
			StateList.remove( StateList.size()-1 );
		}
	}
	
	/** DeletedClusterListに要素を追加するメソッド */
	private void addDeletedClusterList(StateCluster clust){
		long learned = clust.getLearnedNum();
		int reached = clust.getReachedGoal();
		if( learned>=Threshold_Add_DeletedClusterList || reached>=Threshold_GoalCount )DeletedClusterList.add(0,clust);
		if( DeletedClusterList.size() > DeletedClustersNum ){
			DeletedClusterList.remove( DeletedClusterList.size()-1 );
		}
	}
	
	/** ClusterListの一番上に現在の状態クラスタを置く処理 */
	private void updateClusterList(StateCluster cluster){
		if(cluster==null)return;
		ClusterList.remove(cluster);
		ClusterList.add(0,cluster);
	}
	
	/** 全てのクラスタについて統合できるかどうか調べるメソッド */
	public void unityAllClusters(){
		unityClusters(ClusterList.size());
	}
	
	/** 引数の個数のクラスタについて統合できるか調べて、できる場合は統合を行う。 */
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
	
	/** ClusterListの要素を後ろから削除するメソッド */
	private void deleteCluster(){
		/* 削除される前に統合を行う */
		//unityClusters(5);
		deleteMiniCluster();
		
		int[] deletes;
		if( Parameters.Limit_Clusters < ClusterList.size() ) deletes = new int[ClusterList.size()-Parameters.Limit_Clusters];
		else return;
		for(int i=0;i<deletes.length;i++){
			StateCluster clust = ClusterList.remove(ClusterList.size()-1);
			//addDeletedClusterList(clust); /* 削除クラスタリストに追加する。11/19新たに追加 */
			deletes[i] = clust.getID();
		}
		for(StateCluster clust : ClusterList){
			clust.removeFromExceptionRanges(deletes);
		}
	}
	
	/** 小さいクラスタを削除するメソッド */
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
	

	/** 各クラスタの範囲を返すメソッド */
	public ArrayList<double[][]> getClusterRanges(){
		ArrayList<double[][]> list = new ArrayList<double[][]>();
		for(StateCluster clust : ClusterList){
			list.add(clust.getRange());
		}
		return list;
	}
	
	/** 引数exceptionが引数clusterから分離してできたクラスタかどうかを判断するメソッド */
	private boolean isExceptionArea(StateCluster cluster,StateCluster exception){
		if(!cluster.isInExceptionRanges(exception.getID()))return false;
		return isContain(cluster,exception);
	}
	
	
	/** 引数exceptionが引数clusterの部分集合かどうかを判断するメソッド */
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
	
	/** 引数のクラスタが「大きすぎる内包クラスタ」かどうか判断するメソッド */
	private boolean isTooLarge(StateCluster clust){
		double threshold_per = 0.5;
		for(StateCluster elem : ClusterList){
			if(elem==clust || !isContain(elem,clust))continue;
			if( clust.getSimpleClusterSize() > threshold_per*elem.getSimpleClusterSize() )
				return true;
		}
		return false;
	}
	
	/** あるクラスタにおいて、引数のクラスタが内包クラスタの場合例外領域に指定するメソッド */
	private void createExceptionArea(StateCluster NewCluster){
		for(StateCluster clust : ClusterList){
			if(isContain(clust,NewCluster)){
				clust.addExceptionRanges(NewCluster.getID(), NewCluster.getRange()); //例外領域の追加
				clust.removeState(NewCluster.getRange());  //例外領域内の状態を全て削除する
			}
		}
	}
	
	
	/** このオブジェクトの持つ履歴リストを初期化するメソッド */
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
	
	/** 引数のエピソード数経過後に各クラスターの情報をファイルに書き込むメソッド */
	public void writeAboutClusters(int episode){
		String Pass=Parameters.ResultFoldr+"\\"+"1227test_Clusters_after"+episode+"episode.csv";
		FileWriter Result=null;
    	try{
    		Result=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result);
    		String Header="番号,平均,分散";
    		StateCluster OneCluster = ClusterList.get(0);
    		double[][] ranges = OneCluster.getRange();
    		for(int i=0;i<ranges.length;i++){
    			Header += ",min"+i+",max"+i;
    		}
			for(int act=1;act<3;act++){
				double[] aves = OneCluster.getAve_Vectors(act);
				for(int i=0;i<aves.length;i++){
					Header +=",act"+act+"_平均"+i;
				}
				Header +=",act"+act+"_分散";
			}
			Header+=",学習回数,ゴール回数";
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
    				double[] StateValues = new double[states.length];  //クラスタ１の各状態の推測価値を格納する配列
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
	
	/** 各エピソードにおいて消費したSTEP数を書き込むためのファイルの作成 */
	private void createOutputFile(){
    	String Pass=Parameters.RBFOutputFilePass;
    	try{
    		Result_Average=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result_Average);
    		String Header="Episode,平均値,分散値,クラスタ数";
    		pWriter.println(Header);
    	}catch(IOException e){
    		System.out.println("ERROR(NewBrain)!");
    	}
	}
	
	/** ファイルに追加書き込みを行うメソッド */
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
	
	/** 各Episode毎の外部RBFネットワークの平均出力値をファイルに出力するメソッド */
	public void writeAverageOutput(int episode){
		addAverageOutputFile(episode);
		if(episode==Parameters.Numbers_Episode-1){
			Environment.writeRbfUnits(); //外部RBFネットワークの情報も出力する
			closeFile();
		}
	}
	
	/** ファイルを閉じるメソッド */
	private void closeFile(){
		try{
			if(Result_Average!=null)Result_Average.close();
		}catch(IOException e){}
	}
}
