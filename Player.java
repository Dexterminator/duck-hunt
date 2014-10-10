import java.util.ArrayList;
import java.util.List;

class Player {
    private static final int STATES = 5;
    public static final int ITERATIONS = 40;
    private List<HMM> hmms = new ArrayList<HMM>();
    // /constructor

    // /There is no data in the beginning, so not much should be done here.
    public Player() {
    }

    /**
     * Shoot!
     *
     * This is the function where you start your work.
     *
     * You will receive a variable pState, which contains information about all
     * birds, both dead and alive. Each birds contains all past actions.
     *
     * The state also contains the scores for all players and the number of
     * time steps elapsed since the last time this function was called.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return the prediction of a bird we want to shoot at, or cDontShoot to pass
     */
    public Action shoot(GameState pState, Deadline pDue) {
        int sequenceLength = pState.getBird(0).getSeqLength();
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */
        if (hmms.size() == 0) {
            for (int i = 0; i < pState.getNumBirds(); i++) {
                hmms.add(new HMM(STATES, Constants.COUNT_MOVE));
            }
        }
        if (sequenceLength > 50) {
            for (int i = 0; i < hmms.size(); i++) {
                int[] sequence = getSequence(pState.getBird(i));
                hmms.get(i).baumWelch(ITERATIONS, sequence);
                int next = hmms.get(i).predictNextEmission();
                System.err.println("prediction: " + next);
            }
        }



        // This line choose not to shoot
        return cDontShoot;

        // This line would predict that bird 0 will move right and shoot at it
//        return new Action(0, Constants.MOVE_RIGHT);
    }

    private int[] getSequence (Bird bird) {
        int[] sequence = new int[bird.getSeqLength()];
        for (int i = 0; i < bird.getSeqLength(); i++) {
            sequence[i] = bird.getObservation(i);
        }
        return sequence;
    }

    /**
     * Guess the species!
     * This function will be called at the end of each round, to give you
     * a chance to identify the species of the birds for extra points.
     *
     * Fill the vector with guesses for the all birds.
     * Use SPECIES_UNKNOWN to avoid guessing.
     *
     * @param pState the GameState object with observations etc
     * @param pDue time before which we must have returned
     * @return a vector with guesses for all the birds
     */
    public int[] guess(GameState pState, Deadline pDue) {
        /*
         * Here you should write your clever algorithms to guess the species of
         * each bird. This skeleton makes no guesses, better safe than sorry!
         */

        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); ++i)
            lGuess[i] = Constants.SPECIES_UNKNOWN;
        return lGuess;
    }

    /**
     * If you hit the bird you were trying to shoot, you will be notified
     * through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pBird the bird you hit
     * @param pDue time before which we must have returned
     */
    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
    }

    /**
     * If you made any guesses, you will find out the true species of those
     * birds through this function.
     *
     * @param pState the GameState object with observations etc
     * @param pSpecies the vector with species
     * @param pDue time before which we must have returned
     */
    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
        for (int i = 0; i < pSpecies.length; i++) {
            System.err.println("Guess " + i + ": " + pSpecies[i]);
        }
    }

    public static final Action cDontShoot = new Action(-1, -1);
}
