package sim;

import java.awt.Point;

/** パラメータ等を格納しているクラス */
public class Parameters {
	public static final int SimulatorWidth=600;  /**シミュレーターのサイズ(横の長さ)*/
	public static final int SimulatorHeight=600;  /**シミュレーターのサイズ(縦の長さ)*/
	public static final double MaxDistance_Simulator = Math.sqrt(Math.pow(SimulatorWidth, 2)+Math.pow(SimulatorHeight, 2));  /**  シミュレーター内の最大距離 */
	public static final int Width=(int)Math.round(4.0*SimulatorWidth/3); /** ウィンドウサイズ(横の長さ) */
	public static final int Height=SimulatorHeight; /** ウィンドウサイズ(縦の長さ) */
	public static final int ButtonWidth = 100;
	public static final int ButtonHeight = 50;
	
	public static final int ButtonNumbers=4; /** ボタンの数 */
	/** 以下ボタンナンバー */
	public static final int Number_Wall=0;  /** 壁作成ボタン */
	public static final int Number_Goal=1;  /** ゴール設置ボタン */
	public static final int Number_UI=2; /** GUIかCUIを選択するボタン */
	public static final int Number_Simulate=3; /** シミュレーターの開始/停止を行うボタン */
	
	/** 各ボタンのラベル */
	public static String[] NonPushed={"壁作成","ゴール設置","GUI","START"};
	public static String[] Pushed={"壁作成中","ゴール設置中","CUI","STOP"};
	
	public static double Radius_Agent=20;/** エージェントの大きさ(半径) */
	public static double Radius_Goal=30; /** ゴールの大きさ(半径) */
	public static Point Goal_Place = new Point(430,520);  /** ゴールの位置 */
	
	/** シミュレーターに関するフィールド */
	public static final int StartX=0;
	public static final int StartY=0;
	public static final int EndX=SimulatorWidth;
	public static final int EndY=SimulatorHeight;
	
	public static final int FPS=60;  /** シミュレーターのfps */
	public static final int OneFrame=(int)Math.round(1000.0/FPS); /** 1/FPS(単位はミリ秒) */
	
	public static int Numbers_Episode=200;  /** 何エピソード学習させるかという指標 */
	
	/** 以下、エージェントに関するフィールド */
	public static final int Default_Agent_X = 50; /** デフォルトのエージェントの初期x座標 */
	public static final int Default_Agent_Y = 500;  /** デフォルトのエージェントの初期y座標 */
	public static final double ChangeSpeed = 2;  /** 1STEPで変更可能な速度 */
	public static final double MaxSpeed = 20;  /** 最大速度 */
	public static final double ChangeAngle = 10;   /** 1STEPで変更可能な角度 */
	public static final double Agent_Sight = 90;  /**エージェントの視界(単位は度)*/
	public static final double Agent_Distance = 50;   /**エージェントの視界(奥行き)*/
	public static final int Agent_InfraredNums = 4; /**赤外線の数*/
	public static double Agent_RandomAction = 0.05; /** エージェントがランダムに行動する確率 */
	
	
	/** 報酬に関するフィールド */
	public static final double MaxReward = 10;  /** 報酬の最大値(100でうまくいかなかった気がする) */
	public static final double NormalReward = -1.0*MaxReward/300;  /** 通常の報酬(最大値の-1/1000倍)(6/24追加 ) */
	public static final double MaxReward_Environment = 1;  /** ゴール時に外部環境用のRBFネットワークに与える報酬 */
	public static final double Penalty_Environment = -0.1; /** 障害物衝突時に外部環境用のRBFネットワークに与える罰 */
	public static final double Normal_Environment = 0;  /** 通常時に外部環境用のRBFネットワークに与える報酬 */
	
	public static final double RD = Math.PI/180;  //度をラジアンに変換する
	public static final double SigmoidOutput_Min = 0;   /* シグモイド関数の最小値 */
	public static final double SigmoidOutput_Max = 1;   /* シグモイド関数の最大値 */
	
	public static final int HistoricalListLimit=1000;  /** 履歴に関するリストの限界容量(限界格納要素数)(元は1000) */
	
	private static boolean Flag=true;   /** 正規分布乱数を生成する際に利用する変数 */
	private static MTRandom NormalizedRandom = new MTRandom(810);  /** 正規分布乱数を生成する際に利用する乱数(2000) */
	private static MTRandom RandomAction = new MTRandom(1123);  /** ランダムに行動する際に使用する乱数 */
	private static MTRandom RandomSelect = new MTRandom(10); /** ランダムに行動を決定する際に使用する乱数 */
	
	
	/** 以下、状態クラスタに関するフィールド(定数) */
	public static final double FirstRange_Place = 30; /** クラスタ範囲の初期値。x座標或いはy座標にはこの定数を適用する */
	public static final double FirstRange_Angle = 18; /** クラスタ範囲の初期値。角度にはこの定数を適用する */
	public static final int Min_State_Numbers = 30; /** 状態クラスタの要素数(状態数)の最小値 */
	public static final int Max_State_Numbers = 100; /** 状態クラスタの要素数(状態数)の最大値 */
	public static final int Limit_Clusters = 200; /** 記憶できる最大クラスタ数 */
	
	public static String ResultFoldr = "C:\\Users\\tahara\\Desktop\\result\\result0111";
	public static String ResultFilePass = ResultFoldr+"\\test1228_100episode.csv";
	public static String PicturePrefix = "1228Track_100episode";
	public static String RBFOutputFilePass = ResultFoldr+"\\RbfOutput_test1228_100episode.csv";
	
	public static final int Number_Accel = 1;  /** 行動(加速)を意味する整数 */
	public static final int Number_Angle = 2;  /** 行動(角度変更)を意味する整数 */
	public static final double Min_Decide = -1;  /** 行動の選択における分布の最小値 */
	public static final double Max_Decide = 1;  /** 行動の選択における分布の最大値 */
	
	
	//public static final double OneCircle = 360; /** 1回転に必要な角度 */
	//public static final double MaxAngle = 100*OneCircle;  /** このシミュレーションにおける最大角度 */
	//public static final double MinAngle = -100*OneCircle;    /** このシミュレーションにおける最小角度 */
	
	/** シミュレーター内にあるかどうかを判定するメソッド */
	public static boolean judgeCanPut(double r,int x,int y){
		//System.out.println("半径:"+r+",x:"+x+",y:"+y);
		return (x-r>=StartX && x+r<=EndX && y-r>=StartY && y+r<=EndY);
	}
	
	/** シグモイド関数 */
	public static double SigmoidFunction(double x,double a){
		return 1.0/(1+Math.pow(Math.E,-1*a*x));
	}
	
	/** 標準シグモイド関数 */
	public static double SigmoidFunction(double x){
		return SigmoidFunction(x,1);
	}
	
    /// <summary>
    /// 平均mu, 標準偏差sigmaの正規分布乱数を得る。Box-Muller法による。
    /// </summary>
    /// <param name="mu">平均値</param>
    /// <param name="sigma">標準偏差</param>
    /// <returns>指定した正規分布に即した乱数</returns>
    public static double NormalDistribution(double mu, double sigma)
    {	
    	double Alpha = NormalizedRandom.nextDouble();
    	double Beta = NormalizedRandom.nextDouble() * Math.PI * 2;
    	double BoxMuller1 = Math.sqrt(-2 * Math.log(Alpha));
    	double BoxMuller2;
        if(Flag) BoxMuller2 = Math.sin(Beta);
        else BoxMuller2 = Math.cos(Beta);
        Flag = !Flag;
        return sigma * (BoxMuller1 * BoxMuller2) + mu;
    }
    
    /** 引数のmaxとminの間の数値をランダムに返すメソッド */
    public static double getRandomValue(double max,double min){
    	double random = RandomAction.nextDouble();
    	return (max-min)*random + min;
    }
    
    /** 0から引数の自然数-1の間の値をランダムに返すメソッド */
    public static double getRandomInteger(int num){
    	double random = RandomSelect.nextDouble();
    	return (int)random*num;
    }
    
}
