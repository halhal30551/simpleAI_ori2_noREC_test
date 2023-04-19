
public class Positioning {
	
	public static PositionType CalculatePosition(int x1, int y1, int x2, int y2) {
		int distance = calculateDistance((double)x1, (double)y1, (double)x2, (double)y2);
		if(distance <= 100)
			return PositionType.Type1;
		else if(distance <= 200)
			return PositionType.Type2;
		else if(distance <= 400)
			return PositionType.Type3;
		else if(distance <= 600)
			return PositionType.Type4;
		else if(distance <= 800)
			return PositionType.Type5;
		else
			return PositionType.Type6;
	}
	
	private static int calculateDistance(double x1, double y1, double x2, double y2) {
		return (int)Math.sqrt(Math.pow((x2-x1), 2) + Math.pow((y2-y1), 2));
	}
}
