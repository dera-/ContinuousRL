package sim.brain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import sim.Parameters;
import sim.brain.RbfUnit;

public class RbfNetwork {
	private final String Name;  /** このRBFネットワークの名前の決定 */
	private ArrayList<RbfUnit> UnitList = new ArrayList<RbfUnit>();  /** RBF素子リスト */
	private final int Nums_Distribution;  /** 1つのRBF素子の持つ確率分布の数 */
	private ArrayList<SplitData> SplitList = new ArrayList<SplitData>();
	private ArrayList<Double> TotalList = new ArrayList<Double>(); /** RBF素子の出力の合計値の履歴リスト */
	private ArrayList<Double> ScoreList = new ArrayList<Double>(); /** RBFネットワークの出力結果の履歴リスト */
	private static final double gamma = 0.9;  //割引率
	private static final int LimitSize_ScoreList = 1; /** ScoreListに格納可能な最大要素数 */
	private static final int LimitSize_TotalList = Parameters.HistoricalListLimit;
	private double NowTotalValue = 0;  /** 現在算出されたRBF素子の出力の合計値 */
	
	public RbfNetwork(String name,int[] nums,double[] mins,double[] maxs,int[] splits,int[] dists){
		Name = name;
		Nums_Distribution=dists.length;
		createSplitList(nums,mins,maxs,splits);
		createUnitList(dists);
	}
	
	public RbfNetwork(String name,String fileName){
		Name = name;
		Nums_Distribution = 0;
		createUnitList(fileName);
	}
	
	/** SplitListを構築するメソッド */
	private void createSplitList(int[] nums,double[] mins,double[] maxs,int[] splits){
		for(int i=0;i<nums.length;i++){
			for(int j=0;j<nums[i];j++){
				SplitList.add(new SplitData(mins[i],maxs[i],splits[i]));
			}
		}
	}
	
	/** UnitListを構築するメソッド */
	private void createUnitList(int[] elements){
		int[] MaxSplits = decideMaxSplits(elements);
		int[] now = new int[Nums_Distribution];
		for(int i=0;i<now .length;i++){
			now[i]=0;
		}
		
		while(true){
			double[] aves = new double[SplitList.size()];
			double[] diss = new double[SplitList.size()];
			int index=0;
			for(int i=0;i<elements.length;i++){
				for(int j=0;j<elements[i];j++,index++){
					SplitData data = SplitList.get(index);
					aves[index] = data.getSplitElement(now[i]);
					diss[index] = data.getDispersion();
				}
			}
			UnitList.add(new RbfUnit(aves,diss));
			if(isFinish(now,MaxSplits,now.length-1))break;
		}
	}
	
	/** UnitListを構築するメソッド。指定したファイルに従って各RBF素子に値を与えていく */
	private void createUnitList(String fileN){
		try{
			File file = new File(fileN);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  // ファイルを開く
            br.readLine(); //ヘッダの読み飛ばし
            String line;
            while((line=br.readLine())!=null){
            	String[] strs=line.split(",");
            	double w=Double.parseDouble(strs[1]);
            	int nums = (strs.length-2)/2;
            	double[] aves = new double[nums];
            	double[] diss = new double[nums];
            	for(int i=0;i<nums;i++){
            		aves[i] = Double.parseDouble(strs[2*i+2]);
            		diss[i] = Double.parseDouble(strs[2*i+3]);
            	}
            	UnitList.add(new RbfUnit(w,aves,diss));
            }
            br.close();
		}catch(Exception ex){}
	}
	
	/** 正規分布の数を返すメソッド */
	private int getGaussListLength(String fileN){
		try{
			File file = new File(fileN);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  // ファイルを開く
            String line = br.readLine();
            String[] strs=line.split(",");
            int num = (strs.length-2)/2;
            br.close();
            return num;
		}catch(Exception ex){
			System.out.println("Don't know length");
			return 0;
		}
	}
	
	
	/** 各カテゴリの要素数を配列にして返すメソッド(createUnitListメソッドのみで使用) */
	/*
	private int[] decideElements(){
		int[] elems = new int[Nums_Distribution];
		int ave=(int)Math.round(1.0*SplitList.size()/Nums_Distribution);
		for(int i=0;i<Nums_Distribution;i++){
			if(i==Nums_Distribution-1) elems[i]=SplitList.size()-ave*(Nums_Distribution-1);
			else elems[i]=ave;
		}
		return elems;
	}
	*/
	
	/** 各カテゴリにおける最大分割数を配列にして返すメソッド(createUnitListメソッドのみで使用) */
	private int[] decideMaxSplits(int[] elem){
		int[] maxs = new int[Nums_Distribution];
		int index=0;
		for(int i=0; i<maxs.length ;i++){
			int max=0;
			for(int j=0;j<elem[i];j++,index++){
				SplitData data = SplitList.get(index);
				int now = data.getSplitsLength();
				if(now>max)max=now;
			}
			maxs[i]=max;
		}
		return maxs;
	}
	
	/** UnitListの構築を終了するかどうか判定するメソッド  */
	private boolean isFinish(int[] now,int[] max,int num){
		now[num]++;
		if(now[num]>=max[num]){
			now[num]=0;
			if(num==0)return true;
			else return isFinish(now,max,num-1);
		}
		else return false;
	}
	
	/** このRBFネットワークを発火させるメソッド */
	public void runRBF(double[] states){
		double score = getRBFOutput(states);
		addScoreList(score);
		return;
	}
	
	/** 引数の状態を入力した時のRBFネットワークの出力値を返すメソッド */
	public double getRBFOutput(double[] states){
		double total=0;
		for(RbfUnit unit : UnitList){
			total+=unit.getWeightOutput(states);
		}
		return total;
	}
	
	/** このRBFネットワークを発火させるメソッド(正規化を実装) */
	public void runRBF_Normalized(double[] states){
        double score = getRBFOutput_Normalized(states);
        addTotalList(NowTotalValue);
		addScoreList(score);
	}

    /** 引数の状態を入力した時のRBFネットワークの正規化した出力値を返すメソッド */
    public double getRBFOutput_Normalized(double[] states)
    {
        double[] scores = new double[UnitList.size()];
        double total = 0;
        double score = 0;
        for (int i = 0; i < scores.length; i++)
        {
            RbfUnit unit = UnitList.get(i);
            scores[i] = unit.getOutput(states);
            total += scores[i];
        }
        NowTotalValue = total;
        if (total == 0) return 0;
        for (int i = 0; i < scores.length; i++)
        {
            RbfUnit unit = UnitList.get(i);
            score += unit.getWeight() * (scores[i] / total);
        }
        return score;
    }
	
	/** ScoreListに要素を追加する */
	private void addScoreList(double total){
		ScoreList.add(0, total);
		if(ScoreList.size()>LimitSize_ScoreList)
			ScoreList.remove(ScoreList.size()-1);
	}
	
	/** TotalListに要素を追加する */
	private void addTotalList(double total){
		TotalList.add(0, total);
		if(TotalList.size()>LimitSize_TotalList)
			TotalList.remove(TotalList.size()-1);
	}
	
	/** RBFネットワークのnステップ前の出力結果を返すメソッド */
	public double getOutput(int n){
		if(n>=ScoreList.size())return 0;
		return ScoreList.get(n);
	}
	
	/** RBFネットワークの現在の出力結果をシグモイド関数で変換して返すメソッド */
	public double getSigmoidOutput(){
		return getSigmoidOutput(0);
	}
	
	/** RBFネットワークの指定された時刻の出力結果をシグモイド関数で変換して返すメソッド */
	public double getSigmoidOutput(int n){
		double output=getOutput(n);
		return Parameters.SigmoidFunction(output,10000); //test
	}
	
	/** 時刻start～endまでのRBFネットワークの出力結果をシグモイド関数で変換して配列として返すメソッド */
	public double[] getSigmoidOutputs(int start,int end){
		int nums;
		if(end-start+1>ScoreList.size())nums=ScoreList.size();
		else nums=end-start+1;
		double[] array = new double[nums];
		for(int i=0;i<array.length;i++){
			array[i] = getSigmoidOutput(start+i);
		}
		return array;
	}
	
	/** 時刻start～endまでのRBFネットワークの出力結果をシグモイド関数で変換して配列として返すメソッド */
	public double[] getTotalArray(int start,int end){
		int nums;
		if(end-start+1>TotalList.size())nums=TotalList.size();
		else nums=end-start+1;
		double[] array = new double[nums];
		for(int i=0;i<array.length;i++){
			array[i] = TotalList.get(start+i);
		}
		return array;
	}
	
	/** 時刻start～HistoricalListLimitまでのRBFネットワークの出力結果をシグモイド関数で変換して配列として返すメソッド */
	public double[] getSigmoidOutputs(int start){
		return getSigmoidOutputs(start,Parameters.HistoricalListLimit-1);
	}
	
	/** TD誤差を取得するメソッド */
	/*
	public double getTDErorr(double comp){
		double now = ScoreList.get(1);
		double next = ScoreList.get(0);
		return comp+gamma*next-now;
	}
	*/
	
	/** TD誤差を取得するメソッド */
	public double getTDErorr(double comp,double[] inputs){
		double now = ScoreList.get(0);
		double next;
		if(inputs==null)next = 0;
		else next = getRBFOutput(inputs);
		return comp+gamma*next-now;
	}
	
	/** 各RBF素子のパラメーターの更新 */
	public void updateParameters(double comp,double[] inputs,double[] past,boolean[] updates){
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, past, updates);
		}
		return;
	}
	
	/** 各RBF素子のパラメーターの更新(状態の入力を行わない場合) */
	public void updateParameters(double comp,double[] past,boolean[] updates){
		updateParameters(comp,null,past,updates);
	}
	
	/** 各RBF素子のパラメーターの更新。ただし、引数は報酬の代わりにTD誤差を使用する */
	public void updateParameters_withTDError(double td,double[] past,boolean[] updates){
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, past, updates);
		}
		return;
	}
	
	/** 各RBF素子のパラメーターの更新。ただし、引数は報酬の代わりにTD誤差を使用する.モンテカルロ法を使用。 */
	public void updateParameters_withTDError(double td,double[][] pasts,boolean[] updates){
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, pasts, updates);
		}
		return;
	}
	
	/** 各RBF素子のパラメーターの更新(モンテカルロ法の場合) */
	public void updateParameters(double comp,double[] inputs,double[][] pasts,boolean[] updates){
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, pasts, updates);
		}
		return;
	}
	
	/** 各RBF素子のパラメーターの更新(モンテカルロ法の場合) (状態の入力を行わない場合)*/
	public void updateParameters(double comp,double[][] pasts,boolean[] updates){
		updateParameters(comp,null,pasts,updates);
	}
	
	/** 各RBF素子のパラメーターの更新(正規化) */
	public void updateParameters_Normalized(double comp,double[] inputs,double[] past,boolean[] updates){
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, past, updates, TotalList.get(0));
		}
		return;
	}
	
	/** 各RBF素子のパラメーターの更新(正規化) (状態の入力を行わない場合)*/
	public void updateParameters_Normalized(double comp,double[] past,boolean[] updates){
		updateParameters_Normalized(comp,null,past,updates);
	}
	
	/** 各RBF素子のパラメーターの更新(正規化+モンテカルロ法の場合) */
	public void updateParameters_Normalized(double comp,double[] inputs,double[][] pasts,boolean[] updates){
		if(TotalList.size()==0)return;
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, pasts, updates, getTotalArray(0,TotalList.size()-1));
		}
		return;
	}
	
	/** 各RBF素子のパラメーターの更新(正規化+モンテカルロ法の場合)(状態の入力を行わない場合) */
	public void updateParameters_Normalized(double comp,double[][] pasts,boolean[] updates){
		updateParameters_Normalized(comp,null,pasts,updates);
	}
	
	/** ScoreListの大きさを返すメソッド */
	public int ScoreListSize(){
		return ScoreList.size();
	}
	
	/** 引数のrbfネットワークのデータをこのオブジェクトに写すメソッド */
	public void setData(RbfNetwork rbf){
		UnitList.clear();
		SplitList.clear();
		UnitList.addAll(rbf.UnitList);
		SplitList.addAll(rbf.SplitList);
	}
	
	/** フィールドNameを返すメソッド */
	public String getName(){
		return Name;
	}
	
	/** ScoreListの初期化 */
	public void formatScoreList(){
		ScoreList.clear();
		TotalList.clear();
	}
	
	/** 各RBF素子のデータをファイルに書き込むためのメソッド */
	public void writeRbfUnits(){
		String Pass=Parameters.ResultFoldr+"\\"+Name+"_1121test_"+Parameters.Numbers_Episode+"episode.csv";
		FileWriter Result=null;
    	try{
    		Result=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result);
    		String Header="番号,重み";
    		RbfUnit sample=UnitList.get(0);
    		int nums = sample.getGaussNumbers();
    		for(int i=0;i<nums;i++){
    			Header+=",平均"+i+",分散"+i;
    		}
    		pWriter.println(Header);
    		for(int i=0;i<UnitList.size();i++){
    			RbfUnit unit=UnitList.get(i);
    			String line=i+","+unit.getWeight();
    			for(int j=0;j<nums;j++){
    				double params[] = unit.getParameters(j);
    				line+=","+params[0]+","+params[1];
    			}
    			pWriter.println(line);
    		}
    		Result.close();
    	}catch(IOException e){
    		System.out.println("ERROR!");
    	}
	}

}

//１つの状態の取り得る値域を分割して保持するクラス
class SplitData{
	private double[] splits; /** 値域 */
	private double Dispersion; /** 分散値 */
	private static final double phi=1.96; 
	
	public SplitData(double min,double max,int split){
		splits = new double[split];
		Dispersion=(max-min)/(2*phi)/split;
		for(int i=0;i<split;i++){
			splits[i]=(2*i+1)*(phi*Dispersion)+min;
		}
	}
	
	/** 引数の添え字のsplitsの要素を返すメソッド */
	public double getSplitElement(int index){
		if(index>=splits.length) return splits[splits.length-1];  //TODO ここをランダムな要素を返す処理をするように変える
		return splits[index];
	}
	
	/** splitsの要素数を返すメソッド */
	public int getSplitsLength(){
		return splits.length;
	}
	
	
	/** 分散値を返すメソッド */
	public double getDispersion(){
		return Dispersion;
	}
	
}
