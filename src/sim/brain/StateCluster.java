package sim.brain;

import java.util.ArrayList;
import java.util.HashMap;

import sim.MTRandom;
import sim.Parameters;

/** 状態クラスタを表現しているクラス */
public class StateCluster {
	private StateRange[] BasicRange;  /** 状態クラスタの基本的な領域(あとでリストに変更するかも)*/
	private HashMap<Integer,StateRange[]> ExceptionRanges = new HashMap<Integer,StateRange[]>();  /** BasicRange内にあるが例外的に範囲外となる領域を要素とするリスト */
	private ArrayList<State> StateList = new ArrayList<State>();  /** この状態クラスタに属している状態群 */
	//private long Total_State = 0;  /** 今までに経験した状態の累計数 */
	private ActorCritic DecisionMaker;  /** 行動の選択を行うオブジェクト。actor-criticアルゴリズムを使用 */
	private static int Total = 0; /** 状態クラスタが今までに作成された数 */
	private final int ID;  /** このクラスタの番号 */
	private final double decay = 0.9; /** 減衰値 */
	private MTRandom RandomCollect = new MTRandom(2918); //ランダムに代表の状態を集める時に使う乱数
	private Integer Step_Least = null;  //最後にこのクラスタにとどまっていたstepを記録
	
	public StateCluster(double[][] ranges){
		Total++;
		ID = Total;
		BasicRange = new StateRange[ranges.length];
		for(int i=0 ; i<BasicRange.length ; i++){			
			BasicRange[i] = new StateRange(ranges[i][0],ranges[i][1]);
		}
		DecisionMaker = new ActorCritic("Cluster_No"+ID);
		//System.out.println("合計クラスタ数："+Total);
	}
	
	/** 引数の状態の価値の推定を行うメソッド */
	public void runCritic(double[] state_inside){
		DecisionMaker.runCritic(state_inside);
	}
	
	/** 実際に行う行動を取得するメソッド */
	public double[] getAction(double[] state){
		return DecisionMaker.selectAction(state);
	}
	
	/** ActorCriticの学習を行うメソッド(前まで使っていたメソッド) */
	public void updateActorCritic(int n,double comp,double td,double[] past){
		double td_error =  Math.pow(decay,n) * td;
		DecisionMaker.updateActor(td_error, past);
		DecisionMaker.updateCritic(td_error, past);
		DecisionMaker.addEXP();
	}
	
	/** ActorCriticの学習を行うメソッド */
	public void updateActorCritic(double td){
		if(StateList.size()==0)return;
		double[] past = StateList.get(0).getInsideState();
		DecisionMaker.updateActor(td, past);
		DecisionMaker.updateCritic(td, past);
		DecisionMaker.addEXP();
	}
	
	/** ActorCriticの学習を行うメソッド(モンテカルロ法を使用) */
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
	
	/** ActorCriticの学習を行うメソッド(モンテカルロ法を使用) */
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
	
	/** DecisionMakerのフィールドGoalCountに1を加算するメソッド */
	public void addGoalCount_DecisionMaker(){
		DecisionMaker.addGoalCount();
	}
	
	/** ActorCriticの学習を行うメソッド */
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
	
	/** 与えられた報酬と状態に対するTD誤差を返すメソッド */
	public double getTDError(double comp,double[] states){
		return DecisionMaker.getTDError(comp, states);
	}
	
	/** 引数の状態が状態クラスタ内にあるかどうかを判定するメソッド */
	//TODO 引数の状態が例外範囲内の場合の処理
	public boolean isInCluster(double[] states){
		if(!isInRange(BasicRange,states)){
			//System.out.println("範囲外");
			return false; //先に基本的な領域に属しているかどうかのチェックを行う
		}
		//例外領域のチェック
		for(StateRange[] ranges : ExceptionRanges.values()){
			if(isInRange(ranges,states)){
				//System.out.println("min_x:"+ranges[0].getMin()+",max_x:"+ranges[0].getMax());
				//System.out.println("min_y:"+ranges[1].getMin()+",max_y:"+ranges[1].getMax());
				//System.out.println("例外領域内");
				return false;
			}
		}
		return true;
	}
	
	/** 第2引数の状態が第1引数の範囲内にあるかどうかを判定するメソッド */
	private boolean isInRange(StateRange[] ranges,double[] states){
		for(int i=0;i<ranges.length;i++){
			if(!ranges[i].isInRange(states[i])) return false;
		}
		return true;
	}
	
	/** このクラスタの拡張を行うメソッド.返り値は拡張が可能かどうか． */
	public void enlarge(double[] states){
		//クラスタ範囲の拡張処理
		for(int i=0;i<BasicRange.length;i++){
			BasicRange[i].enlargeRange(states[i]);
		}
	}
	
	/** このクラスタの範囲を二次元配列に変換して返すメソッド */
	public double[][] getRange(){
		double[][] range = new double[BasicRange.length][2];
		for(int i=0;i<range.length;i++){
			range[i][0] = BasicRange[i].getMin();
			range[i][1] = BasicRange[i].getMax();
		}
		return range;
	}
	
	/** このクラスタのサイズを返すメソッド */
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
	
	/** このクラスタのIDを返すメソッド */
	public int getID(){
		return ID;
	}
	
	/** このクラスタのIDが引数と同一かどうかを判断するメソッド */
	public boolean isSameID(int id){
		return (ID==id);
	}
	
	/** StateListの外部状態を２次元配列に変換して返すメソッド */
	public double[][] getOutsideStateList(){
		return getOutsideStateList(0,StateList.size());
		
		//ランダムに状態を取得していくパターン
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
			//例外領域のチェック
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
		
		//格子状に状態を取得していくパターン
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
	
	/** StateListの外部状態を２次元配列に変換して返すメソッド */
	public double[][] getOutsideStateList_Random(){
		//ランダムに状態を取得していくパターン
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
			//例外領域のチェック
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
	
	
	/** StateListの外部状態を２次元配列に変換して返すメソッド */
	public double[][] getOutsideStateList(int start){
		return getOutsideStateList(start,StateList.size()-start);
	}
	
	/** StateListの外部状態を２次元配列に変換して返すメソッド */
	public double[][] getOutsideStateList(int start,int size){
		//StateListから外部状態の配列を取得する
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
	
	/** 他の状態クラスタと範囲が被ってしまう場合、そのクラスタのIDをExceptionRangesに追加するメソッド。クラスタ範囲が被る場合はtrueを、そうでない場合はfalseを返す */
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
	
	/** 引数のIDをキーとするペアがExceptionRanges内にあるかどうかを判断するメソッド */
	public boolean isInExceptionRanges(int id){
		return ExceptionRanges.containsKey(id);
	}
	
	/** 引数のIDをキーとするペアをExceptionRanges内から削除するメソッド */
	public void removeFromExceptionRanges(int[] ids){
		for(int i=0;i<ids.length;i++){
			ExceptionRanges.remove(ids[i]);
		}
	}
	
	/** 統合後のStateListの決定を行うメソッド */
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
	
	/** 引数のクラスタのactor-criticを使用するメソッド */
	public void setDicisionMaker(StateCluster cl){
		DecisionMaker.setActorCritic(cl.DecisionMaker);
	}
	
	/** 統合後にどちらのクラスタのactor-criticを使用するか決定するメソッド */
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
	
	/** StateListに引数の状態を追加するメソッド */
	public void addStateList(State state){
		//Total_State++;
		StateList.add(0,state);
		if(StateList.size()>Parameters.Max_State_Numbers){
			StateList.remove(StateList.size()-1);
		}
	}
	
	/** 引数の範囲内の全ての状態をStateListから除外するメソッド */
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
	
	
	/** 第１引数のクラスタに第２引数のクラスタの所持する状態を渡すメソッド */
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
	
	/** 第１引数のと第２引数のクラスタの距離を算出して返すメソッド */
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
	
	/** 第１引数と第２引数のクラスタを統合した時のクラスタ範囲を返すメソッド */
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
	
	/** ExceptionRangesの中身を空にするメソッド */
	public void formatExceptionRanges(){
		ExceptionRanges.clear();
	}
	
	/** 所持しているactor-criticの学習数を返すメソッド */
	public long getLearnedNum(){
		return DecisionMaker.getEXP();
	}
	
	/** 所持しているactor-criticのゴール回数を返すメソッド */
	public int getReachedGoal(){
		return DecisionMaker.getGoalCount();
	}
	
	/** このオブジェクトの持つ履歴リストを初期化するメソッド */
	public void format(){
		Step_Least=null;
		StateList.clear();
		//Total_State = 0;
		DecisionMaker.format();
	}
	
	/** ActorListのindex番目のActor中の平均用ベクトルを取得するメソッド */
	public double[] getAve_Vectors(int index){
		return DecisionMaker.getAve_Vectors(index);
	}
	
	/** ActorListのindex番目のActor中の分散用ベクトルを取得するメソッド */
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

/** 状態クラスタの範囲(1次元)を表現しているクラス */
class StateRange{
	private double Min; //最小値
	private double Max; //最大値
	
	public StateRange(double min,double max){
		Min = min;
		Max = max;
	}
	
	/** 引数の値が範囲内にあるかどうかを判別するメソッド */
	public boolean isInRange(double x){
		return (Min<=x && x<=Max);
	}
	
	/** 引数の値までクラスタ範囲を拡張させるメソッド */
	public void enlargeRange(double x){
		if(x<Min)Min=x;
		else if(Max<x)Max=x;
	}
	
	/** 範囲の最小値を返すメソッド */
	public double getMin(){
		return Min;
	}
	
	/** 範囲の最大値を返すメソッド */
	public double getMax(){
		return Max;
	}
	
	/** Minの値を引数の値にするメソッド */
	public void setMin(double min){
		Min = min;
	}
	
	/** Maxの値を引数の値にするメソッド */
	public void setMax(double max){
		Max = max;
	}
	
	/** MaxとMinの差を返すメソッド */
	public double getDistance(){
		return (Max-Min);
	}
	
}