package sim.brain;

/** 状態をまとめて格納しているクラス */
public class State {
	private double[] InsideState;   //内部状態の配列
	private double[] OutsideState;  //外部状態の配列
	
	public State(double[] outside,double[] inside){
		InsideState = new double[inside.length];
		OutsideState = new double[outside.length];
		for(int i=0;i<inside.length;i++)InsideState[i]=inside[i];
		for(int i=0;i<outside.length;i++)OutsideState[i]=outside[i];
	}
	
	/** InsideStateを返すメソッド */
	public double[] getInsideState(){
		return InsideState;
	}
	
	/** OutsideStateを返すメソッド */
	public double[] getOutsideState(){
		return OutsideState;
	}
}
