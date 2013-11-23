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
	private final String Name;  /** ����RBF�l�b�g���[�N�̖��O�̌��� */
	private ArrayList<RbfUnit> UnitList = new ArrayList<RbfUnit>();  /** RBF�f�q���X�g */
	private final int Nums_Distribution;  /** 1��RBF�f�q�̎��m�����z�̐� */
	private ArrayList<SplitData> SplitList = new ArrayList<SplitData>();
	private ArrayList<Double> TotalList = new ArrayList<Double>(); /** RBF�f�q�̏o�͂̍��v�l�̗������X�g */
	private ArrayList<Double> ScoreList = new ArrayList<Double>(); /** RBF�l�b�g���[�N�̏o�͌��ʂ̗������X�g */
	private static final double gamma = 0.9;  //������
	private static final int LimitSize_ScoreList = 1; /** ScoreList�Ɋi�[�\�ȍő�v�f�� */
	private static final int LimitSize_TotalList = Parameters.HistoricalListLimit;
	private double NowTotalValue = 0;  /** ���ݎZ�o���ꂽRBF�f�q�̏o�͂̍��v�l */
	
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
	
	/** SplitList���\�z���郁�\�b�h */
	private void createSplitList(int[] nums,double[] mins,double[] maxs,int[] splits){
		for(int i=0;i<nums.length;i++){
			for(int j=0;j<nums[i];j++){
				SplitList.add(new SplitData(mins[i],maxs[i],splits[i]));
			}
		}
	}
	
	/** UnitList���\�z���郁�\�b�h */
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
	
	/** UnitList���\�z���郁�\�b�h�B�w�肵���t�@�C���ɏ]���ĊeRBF�f�q�ɒl��^���Ă��� */
	private void createUnitList(String fileN){
		try{
			File file = new File(fileN);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  // �t�@�C�����J��
            br.readLine(); //�w�b�_�̓ǂݔ�΂�
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
	
	/** ���K���z�̐���Ԃ����\�b�h */
	private int getGaussListLength(String fileN){
		try{
			File file = new File(fileN);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));  // �t�@�C�����J��
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
	
	
	/** �e�J�e�S���̗v�f����z��ɂ��ĕԂ����\�b�h(createUnitList���\�b�h�݂̂Ŏg�p) */
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
	
	/** �e�J�e�S���ɂ�����ő啪������z��ɂ��ĕԂ����\�b�h(createUnitList���\�b�h�݂̂Ŏg�p) */
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
	
	/** UnitList�̍\�z���I�����邩�ǂ������肷�郁�\�b�h  */
	private boolean isFinish(int[] now,int[] max,int num){
		now[num]++;
		if(now[num]>=max[num]){
			now[num]=0;
			if(num==0)return true;
			else return isFinish(now,max,num-1);
		}
		else return false;
	}
	
	/** ����RBF�l�b�g���[�N�𔭉΂����郁�\�b�h */
	public void runRBF(double[] states){
		double score = getRBFOutput(states);
		addScoreList(score);
		return;
	}
	
	/** �����̏�Ԃ���͂�������RBF�l�b�g���[�N�̏o�͒l��Ԃ����\�b�h */
	public double getRBFOutput(double[] states){
		double total=0;
		for(RbfUnit unit : UnitList){
			total+=unit.getWeightOutput(states);
		}
		return total;
	}
	
	/** ����RBF�l�b�g���[�N�𔭉΂����郁�\�b�h(���K��������) */
	public void runRBF_Normalized(double[] states){
        double score = getRBFOutput_Normalized(states);
        addTotalList(NowTotalValue);
		addScoreList(score);
	}

    /** �����̏�Ԃ���͂�������RBF�l�b�g���[�N�̐��K�������o�͒l��Ԃ����\�b�h */
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
	
	/** ScoreList�ɗv�f��ǉ����� */
	private void addScoreList(double total){
		ScoreList.add(0, total);
		if(ScoreList.size()>LimitSize_ScoreList)
			ScoreList.remove(ScoreList.size()-1);
	}
	
	/** TotalList�ɗv�f��ǉ����� */
	private void addTotalList(double total){
		TotalList.add(0, total);
		if(TotalList.size()>LimitSize_TotalList)
			TotalList.remove(TotalList.size()-1);
	}
	
	/** RBF�l�b�g���[�N��n�X�e�b�v�O�̏o�͌��ʂ�Ԃ����\�b�h */
	public double getOutput(int n){
		if(n>=ScoreList.size())return 0;
		return ScoreList.get(n);
	}
	
	/** RBF�l�b�g���[�N�̌��݂̏o�͌��ʂ��V�O���C�h�֐��ŕϊ����ĕԂ����\�b�h */
	public double getSigmoidOutput(){
		return getSigmoidOutput(0);
	}
	
	/** RBF�l�b�g���[�N�̎w�肳�ꂽ�����̏o�͌��ʂ��V�O���C�h�֐��ŕϊ����ĕԂ����\�b�h */
	public double getSigmoidOutput(int n){
		double output=getOutput(n);
		return Parameters.SigmoidFunction(output,10000); //test
	}
	
	/** ����start�`end�܂ł�RBF�l�b�g���[�N�̏o�͌��ʂ��V�O���C�h�֐��ŕϊ����Ĕz��Ƃ��ĕԂ����\�b�h */
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
	
	/** ����start�`end�܂ł�RBF�l�b�g���[�N�̏o�͌��ʂ��V�O���C�h�֐��ŕϊ����Ĕz��Ƃ��ĕԂ����\�b�h */
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
	
	/** ����start�`HistoricalListLimit�܂ł�RBF�l�b�g���[�N�̏o�͌��ʂ��V�O���C�h�֐��ŕϊ����Ĕz��Ƃ��ĕԂ����\�b�h */
	public double[] getSigmoidOutputs(int start){
		return getSigmoidOutputs(start,Parameters.HistoricalListLimit-1);
	}
	
	/** TD�덷���擾���郁�\�b�h */
	/*
	public double getTDErorr(double comp){
		double now = ScoreList.get(1);
		double next = ScoreList.get(0);
		return comp+gamma*next-now;
	}
	*/
	
	/** TD�덷���擾���郁�\�b�h */
	public double getTDErorr(double comp,double[] inputs){
		double now = ScoreList.get(0);
		double next;
		if(inputs==null)next = 0;
		else next = getRBFOutput(inputs);
		return comp+gamma*next-now;
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V */
	public void updateParameters(double comp,double[] inputs,double[] past,boolean[] updates){
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, past, updates);
		}
		return;
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V(��Ԃ̓��͂��s��Ȃ��ꍇ) */
	public void updateParameters(double comp,double[] past,boolean[] updates){
		updateParameters(comp,null,past,updates);
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V�B�������A�����͕�V�̑����TD�덷���g�p���� */
	public void updateParameters_withTDError(double td,double[] past,boolean[] updates){
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, past, updates);
		}
		return;
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V�B�������A�����͕�V�̑����TD�덷���g�p����.�����e�J�����@���g�p�B */
	public void updateParameters_withTDError(double td,double[][] pasts,boolean[] updates){
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, pasts, updates);
		}
		return;
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V(�����e�J�����@�̏ꍇ) */
	public void updateParameters(double comp,double[] inputs,double[][] pasts,boolean[] updates){
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, pasts, updates);
		}
		return;
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V(�����e�J�����@�̏ꍇ) (��Ԃ̓��͂��s��Ȃ��ꍇ)*/
	public void updateParameters(double comp,double[][] pasts,boolean[] updates){
		updateParameters(comp,null,pasts,updates);
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V(���K��) */
	public void updateParameters_Normalized(double comp,double[] inputs,double[] past,boolean[] updates){
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, past, updates, TotalList.get(0));
		}
		return;
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V(���K��) (��Ԃ̓��͂��s��Ȃ��ꍇ)*/
	public void updateParameters_Normalized(double comp,double[] past,boolean[] updates){
		updateParameters_Normalized(comp,null,past,updates);
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V(���K��+�����e�J�����@�̏ꍇ) */
	public void updateParameters_Normalized(double comp,double[] inputs,double[][] pasts,boolean[] updates){
		if(TotalList.size()==0)return;
		double td = getTDErorr(comp,inputs);
		for(RbfUnit unit : UnitList){
			unit.updateParameters(td, pasts, updates, getTotalArray(0,TotalList.size()-1));
		}
		return;
	}
	
	/** �eRBF�f�q�̃p�����[�^�[�̍X�V(���K��+�����e�J�����@�̏ꍇ)(��Ԃ̓��͂��s��Ȃ��ꍇ) */
	public void updateParameters_Normalized(double comp,double[][] pasts,boolean[] updates){
		updateParameters_Normalized(comp,null,pasts,updates);
	}
	
	/** ScoreList�̑傫����Ԃ����\�b�h */
	public int ScoreListSize(){
		return ScoreList.size();
	}
	
	/** ������rbf�l�b�g���[�N�̃f�[�^�����̃I�u�W�F�N�g�Ɏʂ����\�b�h */
	public void setData(RbfNetwork rbf){
		UnitList.clear();
		SplitList.clear();
		UnitList.addAll(rbf.UnitList);
		SplitList.addAll(rbf.SplitList);
	}
	
	/** �t�B�[���hName��Ԃ����\�b�h */
	public String getName(){
		return Name;
	}
	
	/** ScoreList�̏����� */
	public void formatScoreList(){
		ScoreList.clear();
		TotalList.clear();
	}
	
	/** �eRBF�f�q�̃f�[�^���t�@�C���ɏ������ނ��߂̃��\�b�h */
	public void writeRbfUnits(){
		String Pass=Parameters.ResultFoldr+"\\"+Name+"_1121test_"+Parameters.Numbers_Episode+"episode.csv";
		FileWriter Result=null;
    	try{
    		Result=new FileWriter(Pass);
    		PrintWriter pWriter = new PrintWriter(Result);
    		String Header="�ԍ�,�d��";
    		RbfUnit sample=UnitList.get(0);
    		int nums = sample.getGaussNumbers();
    		for(int i=0;i<nums;i++){
    			Header+=",����"+i+",���U"+i;
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

//�P�̏�Ԃ̎�蓾��l��𕪊����ĕێ�����N���X
class SplitData{
	private double[] splits; /** �l�� */
	private double Dispersion; /** ���U�l */
	private static final double phi=1.96; 
	
	public SplitData(double min,double max,int split){
		splits = new double[split];
		Dispersion=(max-min)/(2*phi)/split;
		for(int i=0;i<split;i++){
			splits[i]=(2*i+1)*(phi*Dispersion)+min;
		}
	}
	
	/** �����̓Y������splits�̗v�f��Ԃ����\�b�h */
	public double getSplitElement(int index){
		if(index>=splits.length) return splits[splits.length-1];  //TODO �����������_���ȗv�f��Ԃ�����������悤�ɕς���
		return splits[index];
	}
	
	/** splits�̗v�f����Ԃ����\�b�h */
	public int getSplitsLength(){
		return splits.length;
	}
	
	
	/** ���U�l��Ԃ����\�b�h */
	public double getDispersion(){
		return Dispersion;
	}
	
}
