package br.com.fiap.sereno.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class IndiceCalmaCalculadora {

	private static final BigDecimal PESO_RUIDO = new BigDecimal("0.5");
	private static final BigDecimal PESO_ILUMINACAO = new BigDecimal("0.2");
	private static final BigDecimal PESO_LOTACAO = new BigDecimal("0.3");
	private static final BigDecimal CINCO = new BigDecimal("5");
	private static final BigDecimal FATOR = new BigDecimal("2.5");
	private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
	private static final BigDecimal DEZ = BigDecimal.TEN.setScale(2);

	private IndiceCalmaCalculadora() {

	}

	public static BigDecimal calcular(Number ruido, Number iluminacao, Number lotacao) {
		if (ruido == null || iluminacao == null || lotacao == null) {
			return null;
		}
		BigDecimal estimulo = paraDecimal(ruido).multiply(PESO_RUIDO)
				.add(paraDecimal(iluminacao).multiply(PESO_ILUMINACAO))
				.add(paraDecimal(lotacao).multiply(PESO_LOTACAO));
		BigDecimal indice = CINCO.subtract(estimulo).multiply(FATOR).setScale(2, RoundingMode.HALF_UP);
		if (indice.compareTo(ZERO) < 0) {
			return ZERO;
		}
		if (indice.compareTo(DEZ) > 0) {
			return DEZ;
		}
		return indice;
	}

	public static String classificar(BigDecimal indice) {
		if (indice == null) {
			return "SEM_DADOS";
		}
		if (indice.compareTo(new BigDecimal("8")) >= 0) {
			return "MUITO_CALMO";
		}
		if (indice.compareTo(new BigDecimal("6")) >= 0) {
			return "CALMO";
		}
		if (indice.compareTo(new BigDecimal("4")) >= 0) {
			return "MODERADO";
		}
		return "AGITADO";
	}

	private static BigDecimal paraDecimal(Number valor) {
		if (valor instanceof BigDecimal decimal) {
			return decimal;
		}
		if (valor instanceof Integer || valor instanceof Long) {
			return BigDecimal.valueOf(valor.longValue());
		}
		return BigDecimal.valueOf(valor.doubleValue());
	}

}
