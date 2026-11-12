package br.com.fiap.sereno.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class IndiceCalmaCalculadoraTest {

	@Test
	void ambienteTotalmenteTranquiloValeDez() {
		assertEquals(new BigDecimal("10.00"), IndiceCalmaCalculadora.calcular(1, 1, 1));
	}

	@Test
	void ambienteTotalmenteAgitadoValeZero() {
		assertEquals(new BigDecimal("0.00"), IndiceCalmaCalculadora.calcular(5, 5, 5));
	}

	@Test
	void ambienteIntermediarioValeCinco() {
		assertEquals(new BigDecimal("5.00"), IndiceCalmaCalculadora.calcular(3, 3, 3));
	}

	@Test
	void ruidoPesaMaisQueIluminacao() {
		assertEquals(new BigDecimal("5.00"), IndiceCalmaCalculadora.calcular(5, 1, 1));
		assertEquals(new BigDecimal("8.00"), IndiceCalmaCalculadora.calcular(1, 5, 1));
	}

	@Test
	void aceitaMediasDecimais() {

		assertEquals(new BigDecimal("7.50"), IndiceCalmaCalculadora.calcular(1.5, 2.5, 2.5));
	}

	@Test
	void semDadosRetornaNulo() {
		assertNull(IndiceCalmaCalculadora.calcular(null, 1, 1));
		assertEquals("SEM_DADOS", IndiceCalmaCalculadora.classificar(null));
	}

	@Test
	void classificaFaixas() {
		assertEquals("MUITO_CALMO", IndiceCalmaCalculadora.classificar(new BigDecimal("8.00")));
		assertEquals("CALMO", IndiceCalmaCalculadora.classificar(new BigDecimal("6.50")));
		assertEquals("MODERADO", IndiceCalmaCalculadora.classificar(new BigDecimal("4.00")));
		assertEquals("AGITADO", IndiceCalmaCalculadora.classificar(new BigDecimal("1.25")));
	}

}
