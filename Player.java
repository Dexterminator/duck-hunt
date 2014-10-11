import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Player {
    private static final int STATES = 5;
    public static final int ITERATIONS = 30;
    private List<HMM> hmms = new ArrayList<HMM>();
    private HMM[] speciesHMMs = new HMM[Constants.COUNT_SPECIES];
    int round = -1;
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
        if (pState.getRound() != round) {
            round = pState.getRound();
            hmms.clear();
        }

        int sequenceLength = pState.getBird(0).getSeqLength();
        if (sequenceLength < 50)
            return cDontShoot;
        /*
         * Here you should write your clever algorithms to get the best action.
         * This skeleton never shoots.
         */
        if (hmms.size() == 0) {
            for (int i = 0; i < pState.getNumBirds(); i++) {
                hmms.add(new HMM(STATES, Constants.COUNT_MOVE));
            }
        }

        int birdToShoot = -1;
        int nextTargetBirdMove = -1;
        double bestProbability = 0.7;
        for (int i = 0; i < hmms.size(); i++) {
            if (pState.getBird(i).isDead())
                continue;
            int[] sequence = getSequence(pState.getBird(i));
            HMM hmm = new HMM(STATES, Constants.COUNT_MOVE);
            hmm.baumWelch(ITERATIONS, sequence);
            hmms.set(i, hmm);
            double[] nextProbs = hmm.predictNextEmissions(sequence);
            int mostProbable = hmm.getMostProbableObservation(nextProbs);
            double prob = nextProbs[mostProbable];
            if (prob > bestProbability) {
                birdToShoot = i;
                nextTargetBirdMove = mostProbable;
            }
        }

        if (birdToShoot == -1) {
            return cDontShoot;
        } else {
            return new Action(birdToShoot, nextTargetBirdMove);
        }
    }

    private int[] getSequence (Bird bird) {
        int[] sequence = new int[bird.getSeqLength()];
        int to = sequence.length;

        for (int i = 0; i < sequence.length; i++) {
            sequence[i] = bird.getObservation(i);
            if(sequence[i] < 0) {
                to = i;
                break;
            }
        }

        int from = sequence.length - 100;
        from = from < 0 ? 0 : from;

        return Arrays.copyOfRange(sequence, from, to);
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
        int[] lGuess = new int[pState.getNumBirds()];
        if (pState.getRound() == 0) {
            for (int i = 0; i < pState.getNumBirds(); ++i)
                lGuess[i] = Constants.SPECIES_PIGEON;
        } else {
            for (int i = 0; i < pState.getNumBirds(); i++) {
                int probableSpecies = -1;
                Bird bird = pState.getBird(i);
                double max = 0.8;
                for (int j = 0; j < speciesHMMs.length; j++) {
                    if (speciesHMMs[j] != null) {
                        double prob = speciesHMMs[j].getSequenceProbability(getSequence(bird));
                        if (prob > max) {
                            max = prob;
                            probableSpecies = j;
                        }
                    }
                }
                lGuess[i] = probableSpecies;
            }
        }
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
            if (pSpecies[i] == -1)
                continue;
            if (speciesHMMs[pSpecies[i]] == null) {
                speciesHMMs[pSpecies[i]] = hmms.get(i);
            }
        }
    }

    public static final Action cDontShoot = new Action(-1, -1);
}