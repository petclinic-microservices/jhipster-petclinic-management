package org.petclinic.domain;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class VetSpecialtyTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static VetSpecialty getVetSpecialtySample1() {
        return new VetSpecialty().id(1L);
    }

    public static VetSpecialty getVetSpecialtySample2() {
        return new VetSpecialty().id(2L);
    }

    public static VetSpecialty getVetSpecialtyRandomSampleGenerator() {
        return new VetSpecialty().id(longCount.incrementAndGet());
    }
}
