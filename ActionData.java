import enumerate.Action;

public class ActionData {
	public Action action;
	public float score;
	
	private int _energy; //..energy consumed
	private int _framecount;
	
	public ActionData(Action action) {
		this.action = action;		
		this.score = 0;
		_energy = 0;
		_framecount = 0;
	}
	
	public ActionData(Action action, float score) {
		this.action = action;
		this.score = score;
		_energy = 0;
		_framecount = 0;
	}
	
	private ActionData(Action action, float score, int energyAmt, int frameCount) {
		this.action = action;
		this.score = score;
		_energy = energyAmt;
		_framecount = frameCount;
	}
	
	public ActionData unsaveClone()
	{
		return new ActionData(this.action, this.score, this._energy, this._framecount);
	}
	
	public void setEnergyConsumed(int energy) {
		_energy = energy;
	}
	
	public int getEnergyConsumed() {
		return _energy;
	}
	
	public void setFrameCount(int frameCount) {
		_framecount = frameCount;
	}
	
	public int getFrameCount() {
		return _framecount;
	}
}
