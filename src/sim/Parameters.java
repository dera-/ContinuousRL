package sim;

import java.awt.Point;

/** �p�����[�^�����i�[���Ă���N���X */
public class Parameters {
	public static final int SimulatorWidth=600;  /**�V�~�����[�^�[�̃T�C�Y(���̒���)*/
	public static final int SimulatorHeight=600;  /**�V�~�����[�^�[�̃T�C�Y(�c�̒���)*/
	public static final double MaxDistance_Simulator = Math.sqrt(Math.pow(SimulatorWidth, 2)+Math.pow(SimulatorHeight, 2));  /**  �V�~�����[�^�[���̍ő勗�� */
	public static final int Width=(int)Math.round(4.0*SimulatorWidth/3); /** �E�B���h�E�T�C�Y(���̒���) */
	public static final int Height=SimulatorHeight; /** �E�B���h�E�T�C�Y(�c�̒���) */
	public static final int ButtonWidth = 100;
	public static final int ButtonHeight = 50;
	
	public static final int ButtonNumbers=4; /** �{�^���̐� */
	/** �ȉ��{�^���i���o�[ */
	public static final int Number_Wall=0;  /** �Ǎ쐬�{�^�� */
	public static final int Number_Goal=1;  /** �S�[���ݒu�{�^�� */
	public static final int Number_UI=2; /** GUI��CUI��I������{�^�� */
	public static final int Number_Simulate=3; /** �V�~�����[�^�[�̊J�n/��~���s���{�^�� */
	
	/** �e�{�^���̃��x�� */
	public static String[] NonPushed={"�Ǎ쐬","�S�[���ݒu","GUI","START"};
	public static String[] Pushed={"�Ǎ쐬��","�S�[���ݒu��","CUI","STOP"};
	
	public static double Radius_Agent=20;/** �G�[�W�F���g�̑傫��(���a) */
	public static double Radius_Goal=30; /** �S�[���̑傫��(���a) */
	public static Point Goal_Place = new Point(430,520);  /** �S�[���̈ʒu */
	
	/** �V�~�����[�^�[�Ɋւ���t�B�[���h */
	public static final int StartX=0;
	public static final int StartY=0;
	public static final int EndX=SimulatorWidth;
	public static final int EndY=SimulatorHeight;
	
	public static final int FPS=60;  /** �V�~�����[�^�[��fps */
	public static final int OneFrame=(int)Math.round(1000.0/FPS); /** 1/FPS(�P�ʂ̓~���b) */
	
	public static int Numbers_Episode=200;  /** ���G�s�\�[�h�w�K�����邩�Ƃ����w�W */
	
	/** �ȉ��A�G�[�W�F���g�Ɋւ���t�B�[���h */
	public static final int Default_Agent_X = 50; /** �f�t�H���g�̃G�[�W�F���g�̏���x���W */
	public static final int Default_Agent_Y = 500;  /** �f�t�H���g�̃G�[�W�F���g�̏���y���W */
	public static final double ChangeSpeed = 2;  /** 1STEP�ŕύX�\�ȑ��x */
	public static final double MaxSpeed = 20;  /** �ő呬�x */
	public static final double ChangeAngle = 10;   /** 1STEP�ŕύX�\�Ȋp�x */
	public static final double Agent_Sight = 90;  /**�G�[�W�F���g�̎��E(�P�ʂ͓x)*/
	public static final double Agent_Distance = 50;   /**�G�[�W�F���g�̎��E(���s��)*/
	public static final int Agent_InfraredNums = 4; /**�ԊO���̐�*/
	public static double Agent_RandomAction = 0.05; /** �G�[�W�F���g�������_���ɍs������m�� */
	
	
	/** ��V�Ɋւ���t�B�[���h */
	public static final double MaxReward = 10;  /** ��V�̍ő�l(100�ł��܂������Ȃ������C������) */
	public static final double NormalReward = -1.0*MaxReward/300;  /** �ʏ�̕�V(�ő�l��-1/1000�{)(6/24�ǉ� ) */
	public static final double MaxReward_Environment = 1;  /** �S�[�����ɊO�����p��RBF�l�b�g���[�N�ɗ^�����V */
	public static final double Penalty_Environment = -0.1; /** ��Q���Փˎ��ɊO�����p��RBF�l�b�g���[�N�ɗ^���锱 */
	public static final double Normal_Environment = 0;  /** �ʏ펞�ɊO�����p��RBF�l�b�g���[�N�ɗ^�����V */
	
	public static final double RD = Math.PI/180;  //�x�����W�A���ɕϊ�����
	public static final double SigmoidOutput_Min = 0;   /* �V�O���C�h�֐��̍ŏ��l */
	public static final double SigmoidOutput_Max = 1;   /* �V�O���C�h�֐��̍ő�l */
	
	public static final int HistoricalListLimit=1000;  /** �����Ɋւ��郊�X�g�̌��E�e��(���E�i�[�v�f��)(����1000) */
	
	private static boolean Flag=true;   /** ���K���z�����𐶐�����ۂɗ��p����ϐ� */
	private static MTRandom NormalizedRandom = new MTRandom(810);  /** ���K���z�����𐶐�����ۂɗ��p���闐��(2000) */
	private static MTRandom RandomAction = new MTRandom(1123);  /** �����_���ɍs������ۂɎg�p���闐�� */
	private static MTRandom RandomSelect = new MTRandom(10); /** �����_���ɍs�������肷��ۂɎg�p���闐�� */
	
	
	/** �ȉ��A��ԃN���X�^�Ɋւ���t�B�[���h(�萔) */
	public static final double FirstRange_Place = 30; /** �N���X�^�͈͂̏����l�Bx���W������y���W�ɂ͂��̒萔��K�p���� */
	public static final double FirstRange_Angle = 18; /** �N���X�^�͈͂̏����l�B�p�x�ɂ͂��̒萔��K�p���� */
	public static final int Min_State_Numbers = 30; /** ��ԃN���X�^�̗v�f��(��Ԑ�)�̍ŏ��l */
	public static final int Max_State_Numbers = 100; /** ��ԃN���X�^�̗v�f��(��Ԑ�)�̍ő�l */
	public static final int Limit_Clusters = 200; /** �L���ł���ő�N���X�^�� */
	
	public static String ResultFoldr = "C:\\Users\\tahara\\Desktop\\result\\result0111";
	public static String ResultFilePass = ResultFoldr+"\\test1228_100episode.csv";
	public static String PicturePrefix = "1228Track_100episode";
	public static String RBFOutputFilePass = ResultFoldr+"\\RbfOutput_test1228_100episode.csv";
	
	public static final int Number_Accel = 1;  /** �s��(����)���Ӗ����鐮�� */
	public static final int Number_Angle = 2;  /** �s��(�p�x�ύX)���Ӗ����鐮�� */
	public static final double Min_Decide = -1;  /** �s���̑I���ɂ����镪�z�̍ŏ��l */
	public static final double Max_Decide = 1;  /** �s���̑I���ɂ����镪�z�̍ő�l */
	
	
	//public static final double OneCircle = 360; /** 1��]�ɕK�v�Ȋp�x */
	//public static final double MaxAngle = 100*OneCircle;  /** ���̃V�~�����[�V�����ɂ�����ő�p�x */
	//public static final double MinAngle = -100*OneCircle;    /** ���̃V�~�����[�V�����ɂ�����ŏ��p�x */
	
	/** �V�~�����[�^�[���ɂ��邩�ǂ����𔻒肷�郁�\�b�h */
	public static boolean judgeCanPut(double r,int x,int y){
		//System.out.println("���a:"+r+",x:"+x+",y:"+y);
		return (x-r>=StartX && x+r<=EndX && y-r>=StartY && y+r<=EndY);
	}
	
	/** �V�O���C�h�֐� */
	public static double SigmoidFunction(double x,double a){
		return 1.0/(1+Math.pow(Math.E,-1*a*x));
	}
	
	/** �W���V�O���C�h�֐� */
	public static double SigmoidFunction(double x){
		return SigmoidFunction(x,1);
	}
	
    /// <summary>
    /// ����mu, �W���΍�sigma�̐��K���z�����𓾂�BBox-Muller�@�ɂ��B
    /// </summary>
    /// <param name="mu">���ϒl</param>
    /// <param name="sigma">�W���΍�</param>
    /// <returns>�w�肵�����K���z�ɑ���������</returns>
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
    
    /** ������max��min�̊Ԃ̐��l�������_���ɕԂ����\�b�h */
    public static double getRandomValue(double max,double min){
    	double random = RandomAction.nextDouble();
    	return (max-min)*random + min;
    }
    
    /** 0��������̎��R��-1�̊Ԃ̒l�������_���ɕԂ����\�b�h */
    public static double getRandomInteger(int num){
    	double random = RandomSelect.nextDouble();
    	return (int)random*num;
    }
    
}
