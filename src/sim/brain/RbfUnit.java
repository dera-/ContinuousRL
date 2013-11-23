package sim.brain;

/** RBF�f�q�Ɋւ���N���X */
public class RbfUnit {
	protected Gauss[] GaussList;  //���K���z�̔z��
	protected double Weight;  //�d��
	protected final double alpha=0.1; //�w�K��
	protected final double lambda=0.5;  //�K���x
	
	public RbfUnit(double[] aves , double[] diss){
		Weight=0;  //�����ɏd�݂̏����l�͑S��0�ɂ��Ă݂�
		GaussList = new Gauss[diss.length];
		for(int i=0;i<GaussList.length;i++){
			GaussList[i] = new Gauss(aves[i],diss[i]);
		}
	}
	
	public RbfUnit(double w , double[] aves , double[] diss){
		Weight=w;
		GaussList = new Gauss[diss.length];
		for(int i=0;i<GaussList.length;i++){
			GaussList[i] = new Gauss(aves[i],diss[i]);
		}
	}
	
	/** �o�͌��ʂ݂̂��o�� */
	public double getOutput(double[] inputs){
		double total=0;
		for(int i=0;i<GaussList.length;i++){
			total+=GaussList[i].getSolution(inputs[i]);
		}
		return Math.exp(-1*total);
	}
	
	/** �d�݂�Ԃ����\�b�h */
	public double getWeight(){
		return Weight;
	}
	
	/** �u�d�݁~�o�͌��ʁv��Ԃ����\�b�h */
	public double getWeightOutput(double[] inputs){
		double output=getOutput(inputs);
		return Weight*output;
	}
	
	/** �e�p�����[�^�[�̍X�V */
	public void updateParameters(double td,double[] inputs,boolean[] updates){
		double output=getOutput(inputs);
		double basic = Math.pow(alpha,2)*td*output;
		double weight=Weight;
		Weight+=basic;  //�d�݂̍X�V
		if(!updates[0] && !updates[1])return;

		//���ρE���U�̍X�V
		for(int i=0;i<GaussList.length;i++){
			double ave = GaussList[i].Average;
			double dis = GaussList[i].Dispersion;
			if(updates[0])GaussList[i].Average+=basic*weight*( (inputs[i]-ave)/Math.pow(dis,2) );
			if(updates[1])GaussList[i].Dispersion+=basic*weight*( Math.pow((inputs[i]-ave),2)/Math.pow(dis,3) );
		}
	}
	
	/** �e�p�����[�^�[�̍X�V(���K���o�[�W����) */
	public void updateParameters(double td,double[] inputs,boolean[] updates,double total){
		double output=getOutput(inputs);
		double basic = Math.pow(alpha,2)*td;
		double weight=Weight;
		Weight+=basic*(output/total);  //�d�݂̍X�V(basic*(output/total)�����Z����)
		if(!updates[0] && !updates[1])return;

		//���ρE���U�̍X�V
		for(int i=0;i<GaussList.length;i++){
			double ave = GaussList[i].Average;
			double dis = GaussList[i].Dispersion;
			if(updates[0])GaussList[i].Average+=basic*weight*output*( (inputs[i]-ave)/Math.pow(dis,2) )*Math.pow(total,-2)*(total-output);
		}
	}
	
	/** �e�p�����[�^�[�̍X�V(�����e�J�����p) */
	public void updateParameters(double td,double[][] inputs,boolean[] updates){	
		/* �d�݂̍X�V */
		double add_weight=0;
		for(int i=0;i<inputs.length;i++){
			//if(!used[i])continue;
			double output=getOutput(inputs[i]);
			double L=Math.pow(lambda, i);
			double basic = Math.pow(alpha,2)*L*td*output;
			add_weight+=basic;
		}
		Weight+=add_weight;
		
		
		/* ���ρE���U�̍X�V */
		if(!updates[0] && !updates[1])return;
		double weight=Weight;
		double aves[] = new double[GaussList.length];
		double diss[] = new double[GaussList.length];
		double add_aves[] = new double[GaussList.length];
		double add_diss[] = new double[GaussList.length];
		for(int i=0;i<GaussList.length;i++){
			aves[i] = GaussList[i].Average;
			diss[i] = GaussList[i].Dispersion;
			add_aves[i] = 0;
			add_diss[i] = 0;
		}
		for(int i=0;i<inputs.length;i++){
			//if(!used[i])continue;
			double output=getOutput(inputs[i]);
			double L=Math.pow(lambda, i);
			double basic = Math.pow(alpha,2)*L*td*output;
			for(int j=0;j<inputs[i].length;j++){
				if(updates[0])add_aves[j]+=basic*weight*( (inputs[i][j]-aves[j])/Math.pow(diss[j],2) );
				if(updates[1])add_diss[j]+=basic*weight*( Math.pow((inputs[i][j]-aves[j]),2)/Math.pow(diss[j],3) );
			}
		}
		for(int i=0;i<GaussList.length;i++){
			GaussList[i].Average+=add_aves[i];
			GaussList[i].Dispersion+=add_diss[i];
		}
	}
	
	/** �e�p�����[�^�[�̍X�V(���K��+�����e�J�����p) */
	public void updateParameters(double td,double[][] inputs,boolean[] updates,double[] totals){	
		/* �d�݂̍X�V */
		double add_weight=0;
		for(int i=0;i<inputs.length;i++){
			double output=getOutput(inputs[i]);
			double L=Math.pow(lambda, i);
			double basic = Math.pow(alpha,2)*L*td;
			add_weight+=basic*(output/totals[i]);
		}
		Weight+=add_weight;
		
		
		/* ���ρE���U�̍X�V */
		if(!updates[0] && !updates[1])return;
		double weight=Weight;
		double aves[] = new double[GaussList.length];
		double diss[] = new double[GaussList.length];
		double add_aves[] = new double[GaussList.length];
		double add_diss[] = new double[GaussList.length];
		for(int i=0;i<GaussList.length;i++){
			aves[i] = GaussList[i].Average;
			diss[i] = GaussList[i].Dispersion;
			add_aves[i] = 0;
			add_diss[i] = 0;
		}
		for(int i=0;i<inputs.length;i++){
			double output=getOutput(inputs[i]);
			double L=Math.pow(lambda, i);
			double basic = Math.pow(alpha,2)*L*td;
			for(int j=0;j<inputs[i].length;j++){
				if(updates[0])add_aves[j]+=basic*weight*output*( (inputs[i][j]-aves[j])/Math.pow(diss[j],2) )*Math.pow(totals[i],-2)*(totals[i]-output);
			}
		}
		for(int i=0;i<GaussList.length;i++){
			GaussList[i].Average+=add_aves[i];
			GaussList[i].Dispersion+=add_diss[i];
		}
	}
	
	/** GaussList��index�Ԗڂ̐��K���z�̕��ςƕ��U��Ԃ����\�b�h */
	public double[] getParameters(int index){
		if(index > GaussList.length) return null;
		double[] params = new double[2];
		params[0] = GaussList[index].Average;
		params[1] = GaussList[index].Dispersion;
		return params;
	}
	
	/** ���K���z�̐���Ԃ����\�b�h */
	public int getGaussNumbers(){
		return GaussList.length;
	}
	
}

/** ���K���z��\���N���X */
class Gauss{
	double Average;
	double Dispersion;
	
	public Gauss(double a,double d){
		Average = a;
		Dispersion = d;
	}
	
	/** (x-u)^2/��^2�̉���Ԃ����\�b�h */
	public double getSolution(double x){
		return Math.pow((x-Average), 2)/(2 * Math.pow(Dispersion, 2));
	}
	
}