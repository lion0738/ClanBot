package kr.panda.bot.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import kr.panda.bot.object.AssociatedScore;

public class PPCalculator {
	private static final List<Double> ACC_LIST = Arrays.asList(1.0, 0.999, 0.9975, 0.995, 0.9925, 0.99, 0.9875, 0.985,
			0.9825, 0.98, 0.9775, 0.975, 0.9725, 0.97, 0.965, 0.96, 0.955, 0.95, 0.94, 0.93, 0.92, 0.91, 0.9, 0.875,
			0.85, 0.825, 0.8, 0.75, 0.7, 0.65, 0.6, 0.0);

	private static final List<Double> ACC_SCORE_LIST = Arrays.asList(7.424, 6.241, 5.158, 4.01, 3.241, 2.7, 2.303,
			2.007, 1.786, 1.618, 1.49, 1.392, 1.315, 1.256, 1.167, 1.094, 1.039, 1.0, 0.931, 0.867, 0.813, 0.768, 0.729,
			0.65, 0.581, 0.522, 0.473, 0.404, 0.345, 0.296, 0.256, 0.0);

	/**
	 * Calculates the score curve based on accuracy.
	 *
	 * @param acc The accuracy value.
	 * @return The score curve value.
	 */
	private static double curve2(double acc) {
		int i = 0;
		while (i < ACC_LIST.size() && ACC_LIST.get(i) > acc) {
			i += 1;
		}

		if (i == 0) {
			i = 1;
		}

		double middleDis = (acc - ACC_LIST.get(i - 1)) / (ACC_LIST.get(i) - ACC_LIST.get(i - 1));
		return (ACC_SCORE_LIST.get(i - 1) + middleDis * (ACC_SCORE_LIST.get(i) - ACC_SCORE_LIST.get(i - 1)));
	}

	/**
	 * Calculates the accuracy pp based on accuracy and accRating.
	 *
	 * @param acc       The accuracy value.
	 * @param accRating The accuracy rating.
	 * @return The accuracy pp.
	 */
	private static double getAccPp(double acc, double accRating) {
		return curve2(acc) * accRating * 34;
	}

	/**
	 * Calculates the pass pp based on accuracy and passRating.
	 *
	 * @param acc        The accuracy value.
	 * @param passRating The pass rating.
	 * @return The pass pp.
	 */
	private static double getPassPp(double acc, double passRating) {
		double passPp = 15.2 * Math.exp(Math.pow(passRating, 1 / 2.62)) - 30;
		if (Double.isInfinite(passPp) || Double.isNaN(passPp) || passPp < 0) {
			passPp = 0;
		}

		return passPp;
	}

	/**
	 * Calculates the tech pp based on accuracy and techRating.
	 *
	 * @param acc        The accuracy value.
	 * @param techRating The tech rating.
	 * @return The tech pp.
	 */
	private static double getTechPp(double acc, double techRating) {
		return Math.exp(1.9 * acc) * 1.08 * techRating;
	}

	/**
	 * Inflates the value based on the inflation formula.
	 *
	 * @param value The value to be inflated.
	 * @return The inflated value.
	 */
	private static double inflate(double value) {
		return (650 * Math.pow(value, 1.3)) / Math.pow(650, 1.3);
	}

	/**
	 * Calculates the total pp based on accuracy, accRating, passRating, and
	 * techRating.
	 *
	 * @param acc        The accuracy value.
	 * @param accRating  The accuracy rating.
	 * @param passRating The pass rating.
	 * @param techRating The tech rating.
	 * @return The total pp.
	 */
	public static double calculatePp(double acc, double accRating, double passRating, double techRating) {
		double accPp = getAccPp(acc, accRating);
		double passPp = getPassPp(acc, passRating);
		double techPp = getTechPp(acc, techRating);

		return inflate(accPp + passPp + techPp);
	}

	/**
	 * Calculates the required accuracy based on the total pp, accRating,
	 * passRating, and techRating.
	 * 
	 * @param pp         The total pp.
	 * @param accRating  The accuracy rating.
	 * @param passRating The pass rating.
	 * @param techRating The tech rating.
	 * @return The required accuracy.
	 */
	public static double calculateAcc(double pp, double accRating, double passRating, double techRating) {
		double low = 0.0;
		double high = 100.0;
		double tolerance = 1e-5;

		while (high - low > tolerance) {
			double mid = (low + high) / 2.0;
			double totalPp = calculatePp(mid, accRating, passRating, techRating);

			if (totalPp < pp) {
				low = mid;
			} else {
				high = mid;
			}
		}

		return (low + high) / 2.0;
	}

	/**
	 * Calculates the required pp for capturing a specific map based on the list of
	 * scores, target score, and requiredPp.
	 * 
	 * @param scores     The list of scores.
	 * @param target     The target score.
	 * @param requiredPp The required pp.
	 * @return The required pp for capturing the specific map.
	 */
	public static double calculateRequiredPp(List<AssociatedScore> scores, AssociatedScore target, double requiredPp) {
		List<Double> ppList = new ArrayList<>();
		for (AssociatedScore score : scores) {
			ppList.add(score.getPp());
		}

		double targetPp = target == null ? 0.0 : target.getPp();
		double totalPp, tolerance;
		while (true) {
			totalPp = 0;

			ppList.sort(Collections.reverseOrder());
			for (int i = 0; i < ppList.size(); i++) {
				totalPp += ppList.get(i) * Math.pow(0.8, i);
			}

			if (totalPp >= requiredPp) {
				break;
			}

			ppList.remove(targetPp);
			tolerance = requiredPp - totalPp + 0.1;
			targetPp += tolerance;
			ppList.add(targetPp);
		}

		return targetPp;
	}
}
