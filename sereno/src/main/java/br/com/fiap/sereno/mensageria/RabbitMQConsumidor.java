package br.com.fiap.sereno.mensageria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import br.com.fiap.sereno.service.IndiceCalmaService;

@Service
public class RabbitMQConsumidor {

	private static final Logger log = LoggerFactory.getLogger(RabbitMQConsumidor.class);

	private final IndiceCalmaService indiceCalmaService;

	public RabbitMQConsumidor(IndiceCalmaService indiceCalmaService) {
		this.indiceCalmaService = indiceCalmaService;
	}

	@RabbitListener(queues = RabbitMQConfig.FILA)
	public void consumir(AvaliacaoAlteradaEvent evento) {
		log.info("Evento recebido da fila {}: {}", RabbitMQConfig.FILA, evento);
		indiceCalmaService.recalcular(evento.localId());
	}

}
