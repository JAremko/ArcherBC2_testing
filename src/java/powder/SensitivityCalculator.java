package powder;

public final class SensitivityCalculator {

    /**
     * Calculates the sensitivity based on the provided temperature and velocity data.
     *
     * @param data A two-dimensional array where each row represents a pair of temperature and velocity.
     * @return The calculated sensitivity as a double value.
     */
    public static double calculateSensitivity(double[][] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data array must not be null or empty.");
        }

        double sum = 0;
        int count = 0;
        for (int i = 0; i < data.length; i++) {
            // Assuming the first column is temperature and the second column is velocity.
            // You can modify the calculation logic based on your sensitivity calculation needs.
            double temperature = data[i][0];
            double velocity = data[i][1];

            // Example calculation: simply averaging the sum of temperatures and velocities
            sum += temperature + velocity;
            count += 2; // Since we're adding two values for each row
        }

        // Calculate the average as a simplistic form of sensitivity
        return sum / count;
    }
}
