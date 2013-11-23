package sim.brain;

import java.util.ArrayList;

import sim.MTRandom;
import sim.Parameters;

/** 行動の決定を行うオブジェクト。 */
public class ActorCritic {
	/**エージェントの内部状態の価値を推測するRBFネットワークのパラメーター群*/
	private static final int Numbers_State_Type = 3;  /** 状態の次元数(状態の種類の数) */
	private static final int inputs_agent[]={Parameters.Agent_InfraredNums,1,1};  /**入力の種類ごとの個数*/
	private static final double Min_Input_Agent[]={0,0,0};  /**各入力値の最小値*/
	private static final double Max_Input_Agent[]={Parameters.Agent_Distance,Parameters.MaxSpeed,180};  /**各入力値の最大値*/
	private static final int RBFs_agent[]={5,5,10};  /**各入力値に対するRBF素子数*/
	
	/** 行動に関するパラメーター群 */
	private static final int Numbers_Act_Type = 2; /** 行動の種類数 */
	private static final double Act_Maxs[] = {Parameters.Max_Decide,Parameters.ChangeSpeed,Parameters.ChangeAngle};
	private static final double Act_Mins[] = {Parameters.Min_Decide,-1.0*Parameters.ChangeSpeed,-1.0*Parameters.ChangeAngle};
	private static final double Act_Speed[] = {-1.0*Parameters.ChangeSpeed,0,Parameters.ChangeSpeed};
	private static final double Act_Angle[] = {-1.0*Parameters.ChangeAngle,0,Parameters.ChangeAngle};
	private static final double Act_Speed_Splits[];
	private static final double Act_Angle_Splits[];
	
	private RbfNetwork Critic;  /** エージェントの内部の状態の価値を推測するRBFネットワーク */
	private ArrayList<Actor> ActorList = new ArrayList<Actor>(); /** 行動の数だけactor部を作成する */
	
	private long EXP=0; /** このactor-criticのパラメーターを更新した数 */
	private int GoalCount=0;
	
	static{
		//Act_Speed_SplitsとAct_Angle_Splitsのそれぞれにオブジェクトを格納する処理
		Act_Speed_Splits = getSplitArray(Act_Speed.length,-1.0*Parameters.ChangeSpeed,Parameters.ChangeSpeed);
		Act_Angle_Splits = getSplitArray(Act_Angle.length,-1.0*Parameters.ChangeAngle,Parameters.ChangeAngle);
	}
	
	public ActorCritic(String name){
		int splits[]={ inputs_agent[0]/2 , inputs_agent[0]-inputs_agent[0]/2 , inputs_agent[1] , inputs_agent[2] };
		Critic = new RbfNetwork(name,inputs_agent,Min_Input_Agent,Max_Input_Agent,RBFs_agent,splits);
		
		/** actorにおける比重配分分割のための配列(splits_arrays)の作成 */
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
		
		//ActorListの作成
		createActorList(Act_Mins,Act_Maxs,inputs_agent,Min_Input_Agent,Max_Input_Agent,splits_arrays);
	}
	
	/** ActorListの作成 */
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
	
	/** 配列の合計値を求めるメソッド */
	private int getTotal_Array(int[] array){
		int total=0;
		for(int i : array){
			total+=i;
		}
		return total;
	}
	
	/** Splits(行動の区切り)用の配列を生成して返すメソッド */
	private static double[] getSplitArray(int num_acts,double min,double max){
		double[] array = new double[num_acts-1];
		for(int i=0;i<array.length;i++){
			array[i] = (1.0*(i+1)/num_acts)*(max-min)+min;
		}
		return array;
	}
	
	/** 引数のdouble2重配列を1つのdouble配列に変換するメソッド */
	private double[] createDoubleArray(double[]... arrays){
		return createDoubleArray(new int[0],new int[0],arrays);
	}
	
	/** 引数のdouble2重配列を1つのdouble配列に変換するメソッド */
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
	
	/** 引数のdouble変数群を1つのdouble配列にまとめる処理 */
	private double[] createDoubleArray(double... ds){
		return ds;
	}
	
	/** 第1引数と第2引数の組が配列中に存在するかどうか判定するメソッド */
	private boolean isIncluded(int x,int y,int[] xs,int[] ys){
		for(int i=0;i<ys.length;i++){
			for(int j=0;j<xs.length;j++){
				if(xs[j]==x && ys[i]==y)return true;
			}
		}
		return false;
	}
	
	/** EXPに加算するメソッド */
	public void addEXP(int i){
		EXP+=i;
	}
	
	/** EXPに加算するメソッド */
	public void addEXP(){
		addEXP(1);
	}
	
	/** EXPを返すメソッド */
	public long getEXP(){
		return EXP;
	}
	
	public int getGoalCount(){
		return GoalCount;
	}
	
	public void addGoalCount(){
		GoalCount++;
	}
	
	
	/** 内部の状態の価値を推測するメソッド */
	public void runCritic(double[] states){
		//System.out.println("output:"+Environment.getOutput(0)+",sigmoid:"+Environment.getSigmoidOutput());
		//System.out.println("出力値："+Critic.getRBFOutput(states));
		Critic.runRBF(states);
	}
	
	/** 内部状態(critic)の推測価値を返すメソッド */
	public double getCriticValue(double[] states){
		return Critic.getRBFOutput(states);
	}
	
	/** 実際に行う行動を取得するメソッド */
	public double[] selectAction(double[] states){
		
		/*
		Actor actor = ActorList.get(0);
		int number = decideAction(actor.getAction(states));
		//System.out.println("行動番号："+number);
		return getAction(number,states);
		*/
		Actor Speed = ActorList.get(1);
		Actor Angle = ActorList.get(2);
		double[] acts = {Speed.getAction(states) , Angle.getAction(states)};
		return acts;
	}
	
	/** RBFネットワークの出力結果から行う行動の番号を返すメソッド */
	private int decideAction(double s){
		double central = (Parameters.Max_Decide + Parameters.Min_Decide) / 2 ;
		if(s<=central) return Parameters.Number_Accel;
		else return Parameters.Number_Angle;
	}
	
	/** 引数の添え字のActor要素から行動を取得するメソッド */
	private double[] getAction(int index,double[] states){
		double[] acts = {0,0};
		if(index>ActorList.size())return acts;
		Actor actor=ActorList.get(index);
		double act = actor.getAction(states);
		switch(index){
		case Parameters.Number_Accel:
			acts[0] = act; //getRealAction(act,Act_Speed,Act_Speed_Splits);
			//System.out.println("加速:"+acts[0]);
			break;
		case Parameters.Number_Angle:
			acts[1] = act; //getRealAction(act,Act_Angle,Act_Angle_Splits);
			//System.out.println("方向変換:"+acts[1]);
			break;
		default: break;
		}
		return acts;
	}
	
	/** 実際に行う行動を引数の配列(array_act)から選択するメソッド */
	private double getRealAction(double act,double[] array_act,double[] array_split){
		int index;
		for(index=0;index<array_split.length;index++){
			if(act<array_split[index])return array_act[index];
		}
		return array_act[index];
	}
	
	/** ActorとCriticをセットメソッド */
	public void setActorCritic(ActorCritic ac){
		ac.format();  //まずは履歴の消去を行う。
		ActorList.clear();
		ActorList.addAll(ac.ActorList);
		Critic.setData(ac.Critic);
		EXP = ac.EXP;
		GoalCount = ac.GoalCount;
	}
	
	/** Criticで求められたTD誤差を返すメソッド */
	public double getTDError(double comp,double[] states){
		return Critic.getTDErorr(comp,states);
	}
	
	/** Actor部の更新を行うメソッド */
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
	
	/** Actor部の更新を行うメソッド */
	public void updateActor_OnlyReward(double comp,double[] states){
		double td_error = Critic.getTDErorr(comp, null);
		updateActor(td_error,states);
	}
	
	/** Actor部の更新を行うメソッド(モンテカルロ法) */
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
	
	/**　Critic部のパラメーターの更新(学習)　*/
	public void updateCritic(double td_error,double[] states){
		boolean[] updates={false,false};
		Critic.updateParameters_withTDError(td_error,states,updates);
	}
	
	/**　Critic部のパラメーターの更新(モンテカルロ法を使用)　*/
	public void updateCritic(double td_error,double[][] states){
		boolean[] updates={false,false};
		Critic.updateParameters_withTDError(td_error,states,updates);
	}
	
	/**　Critic部のパラメーターの更新(モンテカルロ法を使用)　*/
	public void updateCritic_OnlyReward(double comp,double[][] states){
		boolean[] updates={false,false};
		Critic.updateParameters(comp,states,updates);
	}
	
	/** CriticのScoreListのサイズを返すメソッド */
	public int getScoreList_Critic(){
		return Critic.ScoreListSize();
	}
	
	/** フィールド等の初期化 */
	public void format(){
		for(Actor act : ActorList){
			act.formatActionList();
		}
		Critic.formatScoreList();
	}
	
	/** Actor部の平均用政策ベクトルを返すメソッド.引数はActorListの対象のindex */
	public double[] getAve_Vectors(int index){
		return ActorList.get(index).getAve_Vectors();
	}
	
	/** Actor部の分散用政策ベクトルを返すメソッド.引数はActorListの対象のindex  */
	public double getDis_Vector(int index){
		return ActorList.get(index).getDis_Vector();
	}
	
}

/** Actor部を表すクラス */
class Actor{
	private double[] Ave_Vectors;  //平均用政策ベクトル
	//private double[] Ave_Rests;
	private double Dis_Vector;  //分散用政策ベクトル
	private double Dis_Coefficient;
	private final double Vector_Min;  /* ベクトル要素の最小値 */
	private final double Vector_Max;  /* ベクトル要素の最大値 */
	private final double MaxAction; /* 行動の最大値 */
	private final double MinAction; /* 行動の最小値 */
	private ArrayList<Double> ActionList = new ArrayList<Double>();  /** 行動の履歴リスト */
	private final double learning=0.01;    //actorの学習定数
	private double[] Mins_State; /**各状態の最小値をまとめた配列*/
	private double[] Maxs_State; /**各状態の最大値をまとめた配列*/
	private static MTRandom Random = new MTRandom(111);  /** 正規分布を使うか一様分布を使うか決めるための乱数(80) */
	private static MTRandom RandomAction = new MTRandom(2500); /** 行動生成の際に利用する乱数(100) */
	private final double epsilon = Parameters.Agent_RandomAction;  /** 行動をランダム行動にする確率 */
	private double[] ProperPoints; /** 各政策ベクトルの適正度の履歴 */
	private final double beta=0.9; /** 適正度の履歴の割引率 */	
	private double[] Splits_Vectors; /** 政策ベクトルを作成する際に使用する配列。要素には配分割合が格納。 */
	private final int LimitListSize=1; /** ActionListに格納できる要素数 */
	
	public Actor(double min,double max,double[] mins,double[] maxs,double[] splits){
		
		int nums = mins.length;
		
		/* 定数の設定 */
		MaxAction=max;
		MinAction=min;
		Vector_Min = -2;
		Vector_Max = 2;
		
		/* グローバル変数の設定 */
		//Mins_StateとMaxs_Stateに値を与える 
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
			//System.out.println(i+"番目の要素,"+"max:"+Maxs_State[i]+",min:"+Mins_State[i]);
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
	
	/** 行動を返すメソッド */
	public double getAction(double[] array){
		double random = Random.nextDouble();
		double act;
		if(random<epsilon) act=randomAction();
		else act=normalizedAction(array);
		addActionList(act);
		return act;
	}
	
	/** 正規分布に基づいて行動を決定するメソッド */
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
	
	/** ランダムに行動を返すメソッド */
	private double randomAction(){
		double random = RandomAction.nextDouble();
		double gap = MaxAction-MinAction;
		return (gap*random+MinAction);
	}
	
	
	/** 正規分布の平均を返すメソッド */
	private double getAve(double[] states){
		double ave=0;
		for(int i=0;i<states.length;i++){
			ave+=(Ave_Vectors[i]*states[i]);
			//System.out.println(i+"番目の要素");
			//System.out.println("vec:"+Ave_Vectors[i]);
			//System.out.println("state:"+states[i]);
			//System.out.println("rest:"+Ave_Rests[i]);
		}
		return ave;
	}
	
	/** 正規分布の分散を返すメソッド */
	private double getSigma(){
		return Dis_Coefficient/(1+Math.exp(-1.0*Dis_Vector));
	}
	
	/** ActionListの先頭に引数要素を追加する処理 */
	private void addActionList(double act){
		ActionList.add(0,act);
		if(ActionList.size()>LimitListSize)
			ActionList.remove( ActionList.size()-1 );
	}
	
	/** 各パラメーターの更新を行うメソッド */
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
	
	/** 各パラメーターの更新を行うメソッド(適正度の履歴を使用) */
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
	
	/** ProperPointsの各要素の更新のみ行うメソッド */
	public void updateProperPoints(){
		//TODO 処理の記述
	}
	
	
	
	/** 状態の配列を特徴ベクトルに変換するメソッド */
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
				System.out.println(i+"番目："+normalized[i]);
				System.out.println("割合："+Splits_Vectors[i]);
				System.out.println("状態最大値："+Maxs_State[i]);
				System.out.println("状態最小値："+Mins_State[i]);
				System.out.println("状態の値："+array[i]);
			}
			*/
		}
		return normalized;
	}
	
	/** ActionListの初期化 */
	public void formatActionList(){
		ActionList.clear();
	}
	
	/** 最近の行動を返すメソッド */
	public double getLeastAction(){
		return ActionList.get(0);
	}
	
	/** 平均用政策ベクトルを取得するメソッド */
	public double[] getAve_Vectors(){
		return Ave_Vectors;
	}
	
	/** 分散用政策ベクトルを取得するメソッド */
	public double getDis_Vector(){
		return Dis_Vector;
	}
	
}
