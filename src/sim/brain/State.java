package sim.brain;

/** ��Ԃ��܂Ƃ߂Ċi�[���Ă���N���X */
public class State {
	private double[] InsideState;   //������Ԃ̔z��
	private double[] OutsideState;  //�O����Ԃ̔z��
	
	public State(double[] outside,double[] inside){
		InsideState = new double[inside.length];
		OutsideState = new double[outside.length];
		for(int i=0;i<inside.length;i++)InsideState[i]=inside[i];
		for(int i=0;i<outside.length;i++)OutsideState[i]=outside[i];
	}
	
	/** InsideState��Ԃ����\�b�h */
	public double[] getInsideState(){
		return InsideState;
	}
	
	/** OutsideState��Ԃ����\�b�h */
	public double[] getOutsideState(){
		return OutsideState;
	}
}
