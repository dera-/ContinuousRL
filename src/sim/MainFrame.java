package sim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;


public class MainFrame extends JFrame implements ActionListener{
	private JButton[] EventButtons = new JButton[Parameters.ButtonNumbers];  //�������ɂ���ăC�x���g���N����{�^���Q
	private MainPanel Simulator;  //�V�~�����[�^�[�{��
	private WallCreate Wall; //�Ǎ쐬�I�u�W�F�N�g
	private GoalCreate Goal; //�S�[���쐬�I�u�W�F�N�g
	
	public static void main(String[] args) {
		new MainFrame();
	}
	
	/** �R���X�g���N�^ */
	public MainFrame(){
		super("���{�b�g�V�~�����[�^�[");  //�^�C�g��
		createObject();  //�e�I�u�W�F�N�g�̐���
		Layout(); //�e�R���|�[�l���g�̃��C�A�E�g���s��
		setSize(Parameters.Width,Parameters.Height);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false); //�V�~�����[�^�[��ʂ̃T�C�Y�̌Œ�
        setVisible(true);
        //�E�B���h�E�E��́~�{�^�������������̓���̓o�^
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	confirmExiting();
            }
        });
		
	}
	
	/** �I�u�W�F�N�g�̐������s�����\�b�h */
	private void createObject(){
		Simulator = new MainPanel(EventButtons);  /** �V�~�����[�^�[�̐��� */
		/** �{�^���I�u�W�F�N�g�̐��� */
		for(int i=0; i<EventButtons.length ;i++){
			EventButtons[i] = new JButton(Parameters.NonPushed[i]);
			EventButtons[i].addActionListener(this);
		}
		EventButtons[Parameters.Number_Simulate].setEnabled(Simulator.isPermitStart());
		
		/** ���̃t�B�[���h�̃I�u�W�F�N�g�̐������s�� */
		Wall = new WallCreate(Simulator); 
		Goal = new GoalCreate(Simulator);
	}
	
	/** �e�R���|�[�l���g�̃��C�A�E�g���s�����\�b�h */
	private void Layout(){
	    JPanel layout = new JPanel(); //���C�A�E�g�p�̃p�l��
	    layout.setLayout(null);
	    
	    //�V�~�����[�^�[�̔z�u
	    Simulator.setBounds(0, 0, Parameters.SimulatorWidth, Parameters.SimulatorHeight);
	    layout.add(Simulator);
	    
	    //�{�^���̔z�u
	    int startX=(int)Math.round(0.5*((Parameters.Width-Parameters.SimulatorWidth)-Parameters.ButtonWidth)); //�{�^���ݒux���W
	    int interval=(int)Math.round(1.0*(Parameters.Height-Parameters.ButtonHeight*EventButtons.length)/(EventButtons.length+1));  //�{�^���̊Ԋu 
	    for(int i=0; i<EventButtons.length ;i++){
	    	int startY=interval*(i+1)+Parameters.ButtonHeight*i; //�{�^���ݒuy���W
	    	EventButtons[i].setBounds(Parameters.SimulatorWidth+startX,startY,Parameters.ButtonWidth,Parameters.ButtonHeight);
	    	layout.add(EventButtons[i]);
	    }
	    
	    getContentPane().add(layout);
	}
	
	
	/** �E�B���h�E����郁�\�b�h */
    private void confirmExiting() {
        int retValue = JOptionPane.showConfirmDialog(this, "�I�����܂����H",
                "�E�B���h�E�̏I��", JOptionPane.OK_CANCEL_OPTION);
        if (retValue == JOptionPane.YES_OPTION) {
        	Simulator.fileClose();
            System.exit(0);
        }
    }
    
    /** �C�x���g���N���������ɌĂяo����郁�\�b�h */
    public void actionPerformed(ActionEvent e){
    	for(int i=0;i<EventButtons.length;i++){
    		if(e.getSource()==EventButtons[i]){
    			changeButtonLabel(i);
    			boolean on=Parameters.Pushed[i].equals(EventButtons[i].getText());
    			if(i!=Parameters.Number_UI){
    				canUseButton(!on,i);
    			}
    			switch(i){
    			case Parameters.Number_Wall: 
    				if(on)Wall.startEdit();
    				else Wall.endEdit();
    				break;
    			case Parameters.Number_Goal: 
    				if(on)Goal.startEdit();
    				else Goal.endEdit();
    				break;
    			case Parameters.Number_UI: 
    				Simulator.changeGUI(!on);
    				break;
    			case Parameters.Number_Simulate:
    				if(on) Simulator.startSimulate();
    				else Simulator.stopSimulate();
    				break;
    			default: break;
    			}
    			break;
    		}
    	}
    }

    /** �����̓Y�����ȊO�̗v�f(�{�^��)���g�p���邢�͕s�ɂ��郁�\�b�h */
    private void canUseButton(boolean flag,int index){
    	for(int i=0;i<EventButtons.length;i++){
    		if(flag && i==Parameters.Number_Simulate) EventButtons[i].setEnabled(Simulator.isPermitStart());
    		else if(i!=index)EventButtons[i].setEnabled(flag);
    	}
    }
    
    /** �����̓Y�����̗v�f(�{�^��)�̕\�L��ς��郁�\�b�h */
    private void changeButtonLabel(int index){
    	String text=EventButtons[index].getText();
    	if(text.equals(Parameters.NonPushed[index])){
    		EventButtons[index].setText(Parameters.Pushed[index]); 
    	}
    	else if(text.equals(Parameters.Pushed[index])){
    		EventButtons[index].setText(Parameters.NonPushed[index]); 
    	}
    }
    
}
