import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import enumerate.Action;

public class Rule {
	public PositionType positionType;
	public Action enemyAction;
	public ArrayList<ActionData> acts;
	
	private Random _random = new Random();
	private static int Strong_i=1;
	
	public int getThresholdedActionID(int playerEnergy) {	//アクションの閾値計算？//使われてない？
		try {
			ArrayList<Integer> availdata = new ArrayList<Integer>();
			ArrayList<Integer> val = new ArrayList<Integer>();
			float avgscore = 0;
			float thresh = 0;
						
			for(int i = 0 ; i < acts.size() ; i++) {
				if(acts.get(i).getEnergyConsumed() <= playerEnergy) {
					availdata.add(i);
					avgscore += acts.get(i).score;
				}
			}

			avgscore /= (float)availdata.size();
			thresh = avgscore * SimpleTraining.ruleThreshold;		//=平均score＊0.8
			
			for(int i = 0 ; i < availdata.size() ; i++) {
				int id = availdata.get(i);
				if(acts.get(id).score >= thresh)
					val.add(id);		//スコアが閾値を超えるアクションのリスト
			}
			
			if(val.size() <= 0) {
				return -1;
			}
			else {
				return val.get(_random.nextInt(val.size()));		//ランダム選択
			}
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int getBestActionID(int playerEnergy, int rid, ArrayList<Rule> rules, int mcount) {		//行動決定
		Map<Float, Integer> map=new HashMap<>();
		ArrayList<Float> list=new ArrayList<>();
		try {
			if(acts == null || acts.size() <= 0)
				return -1;
			
			int val = -1;
			for(int i = 0 ; i < acts.size() ; i++) {
				if(acts.get(i).getEnergyConsumed() <= playerEnergy) {		//エネルギー消費量チェック
					list.add(acts.get(i).score);
					map.put(acts.get(i).score,i);
					
					//continue;
				
					/*if(val < 0)		//最初val=i(>0)
						val = i;
					else if(acts.get(i).score > acts.get(val).score)	//よりスコアの高い選択肢
						val = i;
					else if(acts.get(i).score == acts.get(val).score && _random.nextInt(10) > 5) //..may be replaced if scores are equals
						val = i;		//スコア同値のときはランダム*/
				}
			}
			Collections.sort(list, Collections.reverseOrder());	//降順ソート
			int i=Strong_i;		//i番目に強い選択肢
			/*Random rand =new Random();
			if (mcount<400) {
				int r=rand.nextInt(399)+1-mcount;	//1~400::::-399~0
				if(r>0) {
					i=rand.nextInt(list.size())+1;		//ランダム行動100%~0.5%(200回)
				}
			}*/
			if (i-1>=list.size()) {
				i=list.size();
			}
			val=map.get(list.get(i-1));
			//int best=map.get(list.get(0));
			//System.out.println("best: "+rules.get(rid).acts.get(best).action+",		score: "+rules.get(rid).acts.get(best).score);
			System.out.println(i+"番目: "+rules.get(rid).acts.get(val).action+",		score: "+rules.get(rid).acts.get(val).score);
			return val;		//val=actionID
			
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public Rule unsaveClone() {		//copy
		Rule val = new Rule();
		val.positionType = this.positionType;
		val.enemyAction = this.enemyAction;
		val.acts = new ArrayList<ActionData>();
		for(ActionData a : this.acts)
			val.acts.add(a.unsaveClone());
		return val;
	}
}
