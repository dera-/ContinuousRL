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
	private JButton[] EventButtons = new JButton[Parameters.ButtonNumbers];  //押す事によってイベントが起こるボタン群
	private MainPanel Simulator;  //シミュレーター本体
	private WallCreate Wall; //壁作成オブジェクト
	private GoalCreate Goal; //ゴール作成オブジェクト
	
	public static void main(String[] args) {
		new MainFrame();
	}
	
	/** コンストラクタ */
	public MainFrame(){
		super("ロボットシミュレーター");  //タイトル
		createObject();  //各オブジェクトの生成
		Layout(); //各コンポーネントのレイアウトを行う
		setSize(Parameters.Width,Parameters.Height);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false); //シミュレーター画面のサイズの固定
        setVisible(true);
        //ウィンドウ右上の×ボタンを押した時の動作の登録
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	confirmExiting();
            }
        });
		
	}
	
	/** オブジェクトの生成を行うメソッド */
	private void createObject(){
		Simulator = new MainPanel(EventButtons);  /** シミュレーターの生成 */
		/** ボタンオブジェクトの生成 */
		for(int i=0; i<EventButtons.length ;i++){
			EventButtons[i] = new JButton(Parameters.NonPushed[i]);
			EventButtons[i].addActionListener(this);
		}
		EventButtons[Parameters.Number_Simulate].setEnabled(Simulator.isPermitStart());
		
		/** 他のフィールドのオブジェクトの生成も行う */
		Wall = new WallCreate(Simulator); 
		Goal = new GoalCreate(Simulator);
	}
	
	/** 各コンポーネントのレイアウトを行うメソッド */
	private void Layout(){
	    JPanel layout = new JPanel(); //レイアウト用のパネル
	    layout.setLayout(null);
	    
	    //シミュレーターの配置
	    Simulator.setBounds(0, 0, Parameters.SimulatorWidth, Parameters.SimulatorHeight);
	    layout.add(Simulator);
	    
	    //ボタンの配置
	    int startX=(int)Math.round(0.5*((Parameters.Width-Parameters.SimulatorWidth)-Parameters.ButtonWidth)); //ボタン設置x座標
	    int interval=(int)Math.round(1.0*(Parameters.Height-Parameters.ButtonHeight*EventButtons.length)/(EventButtons.length+1));  //ボタンの間隔 
	    for(int i=0; i<EventButtons.length ;i++){
	    	int startY=interval*(i+1)+Parameters.ButtonHeight*i; //ボタン設置y座標
	    	EventButtons[i].setBounds(Parameters.SimulatorWidth+startX,startY,Parameters.ButtonWidth,Parameters.ButtonHeight);
	    	layout.add(EventButtons[i]);
	    }
	    
	    getContentPane().add(layout);
	}
	
	
	/** ウィンドウを閉じるメソッド */
    private void confirmExiting() {
        int retValue = JOptionPane.showConfirmDialog(this, "終了しますか？",
                "ウィンドウの終了", JOptionPane.OK_CANCEL_OPTION);
        if (retValue == JOptionPane.YES_OPTION) {
        	Simulator.fileClose();
            System.exit(0);
        }
    }
    
    /** イベントが起こった時に呼び出されるメソッド */
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

    /** 引数の添え字以外の要素(ボタン)を使用可あるいは不可にするメソッド */
    private void canUseButton(boolean flag,int index){
    	for(int i=0;i<EventButtons.length;i++){
    		if(flag && i==Parameters.Number_Simulate) EventButtons[i].setEnabled(Simulator.isPermitStart());
    		else if(i!=index)EventButtons[i].setEnabled(flag);
    	}
    }
    
    /** 引数の添え字の要素(ボタン)の表記を変えるメソッド */
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
