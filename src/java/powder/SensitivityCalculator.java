package powder;

public final class SensitivityCalculator {

    /**
     * Calculates the sensitivity based on the provided temperature and velocity data.
     *
     * @param data A two-dimensional array where each row represents a pair of temperature and velocity.
     * @return The calculated sensitivity as a double value.
     */
    public static double calculateSensitivity(double[][] data) {
//        double sum = 0;
        int count = 0;

        if ( data != null && data.length > 1) {
            double z_t = data[0][0];
            double z_v = data[0][1];
            double percentile = 0.0;
            for (int i = 1; i < data.length; i++) {
                // Assuming the first column is temperature and the second column is velocity.
                // You can modify the calculation logic based on your sensitivity calculation needs.
                double temperature = data[i][0];
                double velocity = data[i][1];
                double divider = (temperature - z_t)/15.0;
                double delta_v = (velocity - z_v)/divider;
                System.out.print("idx: ");
                System.out.println(i);
                System.out.print("Div: ");
                System.out.print(divider);
                System.out.print("  delta: ");
                System.out.print(delta_v);
                percentile += delta_v*100.0/velocity;
                System.out.print("  precentile: ");
                System.out.println(percentile);
                ++count;
            }
            return percentile/count;
        }


        return 0.0;
    }
}
