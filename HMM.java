import java.util.Random;

/**
 * Created by dexter on 10/10/14.
 */
public class HMM {
    private double[][] A;
    private double[][] B;
    private double[][] pi;

    public HMM(int states, int emissions) {
        A = new double[states][states];
        B = new double[states][emissions];
        pi = new double[1][states];
        fillMatrix(A);
        fillMatrix(B);
        fillMatrix(pi);
    }

    private void fillMatrix(double[][] matrix){
        Random r = new Random();
        for(int i = 0; i<matrix.length; i++){
            double sum = 0;
            for(int j = 0; j < matrix[0].length; j++){
                double rand = r.nextDouble();
                matrix[i][j] = rand;
                sum += rand;
            }

            for(int j = 0; j < matrix[0].length; j++){
                double temp = matrix[i][j];
                matrix[i][j] = temp/sum;
            }
        }
    }

    public int predictNextEmission() {
        double[] tempRow = new double[B.length];
//        double tempRowSum = 0;
//        for (int i = 0; i < tempRow.length; i++) {
//            tempRow[i] = B[i][]
//        }

        double[] nextState = new double[pi[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[i].length; j++) {
                nextState[j] += A[i][j] * pi[0][i];
            }
        }

        double[] nextEmmisions = new double[B[0].length];
        for (int i = 0; i < B.length; i++) {
            for (int j = 0; j < B[i].length; j++) {
                nextEmmisions[j] += B[i][j] * nextState[i];
            }
        }

        double max = Integer.MIN_VALUE;
        int prediction = -1;
        for (int i = 0; i < nextEmmisions.length; i++) {
            if (nextEmmisions[i] > max) {
                max = nextEmmisions[i];
                prediction = i;
            }
        }
        return prediction;
    }

    public void baumWelch(int iterations, int[] sequence) {
        for (int i = 0; i < iterations; i++) {
            double[][] alpha = forward(sequence);
            double[][] beta = backward(sequence);
            update(sequence, alpha, beta);
        }

        for (int i = 0; i < B.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                System.err.println(B[i][j]);
            }
        }
    }

    private double[][] forward(int[] sequence){

        double[][] alpha = new double[sequence.length][A[0].length];

        // Initialization
        double tempTotalProb = 0;
        for(int i = 0; i < A[0].length; i++){
            alpha[0][i] = B[i][sequence[0]]* pi[0][i];
            tempTotalProb += alpha[0][i];
        }

        // Normalization to avoid underflow
        for(int i = 0; i < A[0].length; i++){
            double temp = alpha[0][i];
            alpha[0][i] = temp/tempTotalProb;
        }

        // Recursion part
        for(int i = 1; i < sequence.length; i++){
            double totalProb = 0;
            for(int j = 0; j < A[0].length; j++){
                alpha[i][j] = B[j][sequence[i]]*calcAlphaSum(i, j, A[0].length, alpha);
                totalProb += alpha[i][j];
            }

            for( int j = 0; j< A[0].length; j++){
                double temp = alpha[i][j];
                alpha[i][j] = temp/totalProb;
            }
        }

        return alpha;
    }

    private double[][] backward(int[] sequence){
        double[][] beta = new double[sequence.length][A[0].length];

        for(int i = 0; i < A[0].length; i++){
            beta[sequence.length-1][i] = 1;
        }

        for(int t= sequence.length-1; t>0; t--){
            double totalProb = 0;
            for(int i=0; i< A[0].length; i++){
                double probSum = 0;
                for(int j=0; j< A[0].length; j++){
                    probSum += beta[t][j]* A[i][j]* B[j][sequence[t]];
                }
                totalProb += probSum;
                beta[t-1][i] = probSum;
            }

            for(int i = 0; i< A[0].length; i++){
                double temp = beta[t-1][i];
                beta[t-1][i] = temp/totalProb;
            }

        }

        return beta;
    }

    private float calcAlphaSum(int t, int row, int N, double[][] alpha){
        float sum = 0;
        for(int j = 0; j < N; j++){
            sum += A[j][row]*alpha[t-1][j];
        }

        return sum;
    }

    private void update(int[] sequence, double[][] alpha, double[][] beta){
        double[][] gamma = new double[A[0].length][sequence.length];
        for(int t = 0; t < sequence.length; t++){
            for(int i = 0; i < A[0].length; i++){
                double alphaBeta = alpha[t][i] * beta[t][i];
                double sum = 0;
                for(int j = 0; j < A[0].length; j++){
                    sum += alpha[t][j]*beta[t][j];
                }
                gamma[i][t] = alphaBeta/sum;
            }
        }


        double [][][] xi = new double[A[0].length][A[0].length][sequence.length];

        for(int t = 0; t < sequence.length-1; t++){
            double mul =0;
            for(int i = 0; i < A[0].length; i++){
                for(int j = 0; j < A[0].length; j++){
                    mul += alpha[t][i] * A[i][j] * beta[t+1][j]* B[j][sequence[t+1]];
                }
            }

            for(int i = 0; i < A[0].length; i++){
                for(int j = 0; j< A[0].length; j++){
                    double mulsum = alpha[t][i]* A[i][j]*beta[t+1][j]* B[j][sequence[t+1]];
                    xi[i][j][t] = mulsum / mul;
                }
            }
        }

        for(int i = 0; i< A[0].length; i++){
            pi[0][i] = gamma[i][0];
        }

        for(int i=0; i< A[0].length; i++){
            for(int j=0; j< A[0].length; j++){
                double sum = 0;
                double gammasum = 0;
                for(int t = 0; t< sequence.length; t++){
                    sum += xi[i][j][t];
                    gammasum += gamma[i][t];
                }
                A[i][j] = sum/gammasum;
            }
        }

        for(int i=0; i< A[0].length; i++){
            for(int k = 0; k< B[0].length; k++){
                double sum = 0;
                double gammasum = 0;

                for(int t=0; t< sequence.length; t++){
                    if(sequence[t] == k){
                        sum += gamma[i][t];
                    }
                    gammasum += gamma[i][t];
                }
                B[i][k] = sum/gammasum;
            }
        }
    }
}
