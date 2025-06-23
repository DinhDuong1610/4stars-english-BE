package com.fourstars.FourStars.unit.service;

import com.fourstars.FourStars.service.SM2Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SM2ServiceTest {

    private SM2Service sm2Service;

    @BeforeEach
    void setUp() {
        sm2Service = new SM2Service();
    }

    @Test
    @DisplayName("Khi trả lời đúng (q=4) lần đầu tiên, interval phải là 1")
    void calculate_whenFirstCorrectResponse_shouldSetIntervalToOne() {
        SM2Service.SM2InputData input = new SM2Service.SM2InputData();
        input.setRepetitions(0);
        input.setInterval(0);
        input.setEaseFactor(2.5);
        input.setQuality(4);

        SM2Service.SM2Result result = sm2Service.calculate(input);

        assertEquals(1, result.getNewRepetitions());
        assertEquals(1, result.getNewInterval());
        assertEquals(2.5, result.getNewEaseFactor());
    }

    @Test
    @DisplayName("Khi trả lời đúng (q=5) lần thứ hai, interval phải là 6")
    void calculate_whenSecondPerfectResponse_shouldSetIntervalToSix() {
        SM2Service.SM2InputData input = new SM2Service.SM2InputData();
        input.setRepetitions(1);
        input.setInterval(1);
        input.setEaseFactor(2.5);
        input.setQuality(5);

        SM2Service.SM2Result result = sm2Service.calculate(input);

        assertEquals(2, result.getNewRepetitions());
        assertEquals(6, result.getNewInterval());
        assertEquals(2.6, result.getNewEaseFactor(), 0.001);
    }

    @Test
    @DisplayName("Khi trả lời sai (q=2), interval phải reset về 1 và repetitions về 0")
    void calculate_whenIncorrectResponse_shouldResetIntervalAndRepetitions() {
        SM2Service.SM2InputData input = new SM2Service.SM2InputData();
        input.setRepetitions(5);
        input.setInterval(30);
        input.setEaseFactor(2.7);
        input.setQuality(2);

        SM2Service.SM2Result result = sm2Service.calculate(input);

        assertEquals(0, result.getNewRepetitions());
        assertEquals(1, result.getNewInterval());
        assertEquals(2.38, result.getNewEaseFactor(), 0.001);
    }

    @Test
    @DisplayName("Ease Factor không bao giờ được nhỏ hơn 1.3")
    void calculate_whenEaseFactorDrops_shouldNotGoBelowThreshold() {
        SM2Service.SM2InputData input = new SM2Service.SM2InputData();
        input.setRepetitions(5);
        input.setInterval(30);
        input.setEaseFactor(1.35);
        input.setQuality(0);

        SM2Service.SM2Result result = sm2Service.calculate(input);

        assertEquals(1.3, result.getNewEaseFactor());
    }
}
