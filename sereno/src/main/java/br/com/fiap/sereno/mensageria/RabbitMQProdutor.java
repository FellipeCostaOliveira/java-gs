package br.com.fiap.sereno.mensageria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import br.com.fiap.sereno.service.IndiceCalmaService;

@Service
public class RabbitMQProdutor {

	private static final Logger log = LoggerFactory.getLogger(RabbitMQProdutor.class);

	private final RabbitTemplate produtor;
	private final IndiceCalmaService indiceCalmaService;

	public RabbitMQProdutor(RabbitTemplate produtor, IndiceCalmaService indiceCalmaService) {
		this.produtor = produtor;
		this.indiceCalmaService = indiceCalmaService;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void publicar(AvaliacaoAlteradaEvent evento) {
		try {
			produtor.convertAndSend(RabbitMQConfig.ROTEADOR, RabbitMQConfig.CHAVE_ROTA, evento);
			log.info("Evento publicado na fila {}: {}", RabbitMQConfig.FILA, evento);
		} catch (AmqpException ex) {
			log.warn("RabbitMQ indisponível ({}). Recalculando indice_calma do local {} de forma síncrona.",
					ex.getMessage(), evento.localId());
			indiceCalmaService.recalcular(evento.localId());
		}
	}

}
