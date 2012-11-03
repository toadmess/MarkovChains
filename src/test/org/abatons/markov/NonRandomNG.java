package org.abatons.markov;

import java.util.Random;

@SuppressWarnings("serial") class NonRandomNG extends Random {
    int unrandomTarget = 0;
    @Override
    public int nextInt(int n) { return unrandomTarget; }
}